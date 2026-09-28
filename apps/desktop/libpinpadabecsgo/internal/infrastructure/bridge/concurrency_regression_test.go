package bridge

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"io"
	"net"
	"os"
	"strings"
	"sync"
	"sync/atomic"
	"syscall"
	"testing"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridgeprotocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/ownership"
)

type concurrencySession struct {
	client net.Conn
	done   chan struct{}
	err    error
}

func startConcurrencySession(t *testing.T, s *Server, wrap func(net.Conn, context.CancelFunc) net.Conn) *concurrencySession {
	t.Helper()
	ctx, cancel := context.WithCancel(context.Background())
	client, server := net.Pipe()
	if wrap != nil {
		server = wrap(server, cancel)
	}
	session := &concurrencySession{client: client, done: make(chan struct{})}
	go func() {
		session.err = s.handle(ctx, server)
		close(session.done)
	}()
	t.Cleanup(func() {
		cancel()
		_ = client.Close()
		select {
		case <-session.done:
		case <-time.After(2 * time.Second):
			t.Error("worker de sessão não terminou no cleanup")
		}
	})
	return session
}

func awaitConcurrencySession(t *testing.T, session *concurrencySession, timeout time.Duration) error {
	t.Helper()
	timer := time.NewTimer(timeout)
	defer timer.Stop()
	select {
	case <-session.done:
		return session.err
	case <-timer.C:
		t.Fatal("sessão não terminou dentro do prazo")
		return nil
	}
}

func exchangeConcurrencyFrame(t *testing.T, client net.Conn, message bridgeprotocol.MessageType, correlation string) bridgeprotocol.Frame {
	t.Helper()
	ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
	defer cancel()
	if err := bridgeprotocol.WriteFrame(ctx, client, bridgeprotocol.Frame{MessageType: message, CorrelationID: correlation}); err != nil {
		t.Fatal(err)
	}
	response, err := bridgeprotocol.ReadFrame(ctx, client)
	if err != nil {
		t.Fatal(err)
	}
	return response
}

func acquireConcurrencySession(t *testing.T, s *Server, wrap func(net.Conn, context.CancelFunc) net.Conn) *concurrencySession {
	t.Helper()
	session := startConcurrencySession(t, s, wrap)
	if response := exchangeConcurrencyFrame(t, session.client, bridgeprotocol.Hello, "hello-072"); response.MessageType != bridgeprotocol.HelloOK {
		t.Fatal(response)
	}
	if response := exchangeConcurrencyFrame(t, session.client, bridgeprotocol.Acquire, "acquire-072"); response.MessageType != bridgeprotocol.AcquireOK {
		t.Fatal(response)
	}
	return session
}

type concurrencyRead struct {
	data []byte
	err  error
}

type chunkTransport struct {
	fakeTransport
	reads chan concurrencyRead
}

func (f *chunkTransport) Read(ctx context.Context) ([]byte, error) {
	select {
	case <-ctx.Done():
		return nil, ctx.Err()
	case chunk := <-f.reads:
		return chunk.data, chunk.err
	}
}

func TestSlowPeerWriteDeadlineReleasesSessionAndAllowsReopen(t *testing.T) {
	owner := &controlledOwner{}
	s, _ := newRegressionServer(t, &controlledTransport{}, owner)
	transport := &chunkTransport{reads: make(chan concurrencyRead, 2)}
	s.config.Transport = transport
	session := acquireConcurrencySession(t, s, nil)
	transport.reads <- concurrencyRead{data: []byte("resposta opaca")}
	transport.reads <- concurrencyRead{err: errors.New("falha serial enquanto TCP está bloqueado")}
	started := time.Now()
	// O cliente mantém o socket aberto sem consumir DATA. Nenhum cancel externo
	// pode encerrar a sessão: o orçamento de peer.write deve disparar o cleanup.
	err := awaitConcurrencySession(t, session, 12*time.Second)
	if !errors.Is(err, context.DeadlineExceeded) {
		t.Fatalf("causa do bloqueio = %v", err)
	}
	if elapsed := time.Since(started); elapsed < 9*time.Second || elapsed > 12*time.Second {
		t.Fatalf("prazo total esperado de 10s, observado %v", elapsed)
	}
	if transport.IsOpen() || owner.releases != 1 {
		t.Fatalf("cleanup incompleto: serial=%v releases=%d", transport.IsOpen(), owner.releases)
	}
	if n, err := session.client.Read(make([]byte, 1)); n != 0 || !(errors.Is(err, io.EOF) || errors.Is(err, net.ErrClosed) || (err != nil && strings.Contains(err.Error(), "closed pipe"))) {
		t.Fatalf("socket não foi fechado sem ERROR: n=%d err=%v", n, err)
	}
	reopened := acquireConcurrencySession(t, s, nil)
	if ack := exchangeConcurrencyFrame(t, reopened.client, bridgeprotocol.Release, "release-072"); ack.MessageType != bridgeprotocol.Close {
		t.Fatal(ack)
	}
	if err := awaitConcurrencySession(t, reopened, 2*time.Second); err != nil || owner.releases != 2 {
		t.Fatalf("reabertura: releases=%d err=%v", owner.releases, err)
	}
}

type partialDataConn struct {
	net.Conn
	cancel      context.CancelFunc
	errorWrites atomic.Int32
}

func (c *partialDataConn) Write(data []byte) (int, error) {
	if len(data) >= 6 && string(data[:4]) == "PBRG" {
		switch bridgeprotocol.MessageType(data[5]) {
		case bridgeprotocol.Error:
			c.errorWrites.Add(1)
		case bridgeprotocol.Data:
			n, err := c.Conn.Write(data[:len(data)-5])
			if err == nil {
				c.cancel()
			}
			return n, err
		}
	}
	return c.Conn.Write(data)
}

func TestPartialDataCancellationClosesSocketWithoutAppendingError(t *testing.T) {
	owner := &controlledOwner{}
	s, _ := newRegressionServer(t, &controlledTransport{}, owner)
	transport := &chunkTransport{reads: make(chan concurrencyRead, 1)}
	s.config.Transport = transport
	var connection *partialDataConn
	session := acquireConcurrencySession(t, s, func(conn net.Conn, cancel context.CancelFunc) net.Conn {
		connection = &partialDataConn{Conn: conn, cancel: cancel}
		return connection
	})
	transport.reads <- concurrencyRead{data: []byte("payload interrompido antes dos cinco bytes finais")}
	ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
	defer cancel()
	if response, err := bridgeprotocol.ReadFrame(ctx, session.client); !errors.Is(err, io.ErrUnexpectedEOF) {
		t.Fatalf("frame parcial completado indevidamente: response=%v err=%v", response, err)
	}
	if err := awaitConcurrencySession(t, session, 2*time.Second); !errors.Is(err, context.Canceled) {
		t.Fatalf("causa original perdida: %v", err)
	}
	if writes := connection.errorWrites.Load(); writes != 0 {
		t.Fatalf("ERROR foi enviado após DATA parcial: %d", writes)
	}
	if transport.IsOpen() || owner.releases != 1 {
		t.Fatal("cancelamento não liberou serial/ownership")
	}
}

type notifyingWriteConn struct {
	net.Conn
	started chan struct{}
	once    sync.Once
}

func (c *notifyingWriteConn) Write(data []byte) (int, error) {
	c.once.Do(func() { close(c.started) })
	return c.Conn.Write(data)
}

func TestPeerWriteCancellationWhileWaitingClosesBlockedWriter(t *testing.T) {
	client, server := net.Pipe()
	defer client.Close()
	defer server.Close()
	connection := &notifyingWriteConn{Conn: server, started: make(chan struct{})}
	p := &peer{conn: connection}
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()
	done := make(chan error, 1)
	go func() {
		done <- p.write(ctx, bridgeprotocol.Frame{MessageType: bridgeprotocol.Data, Payload: []byte{1}})
	}()
	select {
	case <-connection.started:
	case <-time.After(time.Second):
		t.Fatal("primeiro escritor não iniciou")
	}
	waiting, stop := context.WithTimeout(context.Background(), 50*time.Millisecond)
	defer stop()
	if err := p.write(waiting, bridgeprotocol.Frame{MessageType: bridgeprotocol.Error}); !errors.Is(err, context.DeadlineExceeded) {
		t.Fatalf("espera não respeitou contexto: %v", err)
	}
	select {
	case err := <-done:
		if err == nil {
			t.Fatal("escritor bloqueado não detectou fechamento")
		}
	case <-time.After(time.Second):
		t.Fatal("fechamento não acordou escritor bloqueado")
	}
	if err := p.write(context.Background(), bridgeprotocol.Frame{MessageType: bridgeprotocol.Error}); !errors.Is(err, net.ErrClosed) {
		t.Fatalf("peer foi reutilizado após falha: %v", err)
	}
}

func TestConcurrentPeerWritesKeepWholeFrames(t *testing.T) {
	client, server := net.Pipe()
	ctx, cancel := context.WithTimeout(context.Background(), 3*time.Second)
	p := &peer{conn: server}
	const count = 16
	results := make(chan error, count)
	var writers sync.WaitGroup
	writers.Add(count)
	t.Cleanup(func() { cancel(); _ = client.Close(); _ = server.Close(); writers.Wait() })
	for i := range count {
		go func() {
			defer writers.Done()
			results <- p.write(ctx, bridgeprotocol.Frame{MessageType: bridgeprotocol.Data,
				CorrelationID: fmt.Sprint(i), Payload: bytes.Repeat([]byte{byte(i)}, 4096)})
		}()
	}
	seen := make(map[string]bool)
	for range count {
		frame, err := bridgeprotocol.ReadFrame(ctx, client)
		if err != nil {
			t.Fatal(err)
		}
		var id int
		if _, err := fmt.Sscan(frame.CorrelationID, &id); err != nil || id < 0 || id >= count {
			t.Fatalf("correlação inválida: %q", frame.CorrelationID)
		}
		if seen[frame.CorrelationID] || frame.MessageType != bridgeprotocol.Data || !bytes.Equal(frame.Payload, bytes.Repeat([]byte{byte(id)}, 4096)) {
			t.Fatal("frames concorrentes intercalados ou duplicados")
		}
		seen[frame.CorrelationID] = true
	}
	for range count {
		if err := <-results; err != nil {
			t.Fatal(err)
		}
	}
}

func TestOwnershipFailureLogsBusyAndAbandonedCauses(t *testing.T) {
	for _, tc := range []struct {
		name, code, cause string
		ownerErr          error
		active            bool
	}{
		{name: "sessão ativa", code: "BUSY", cause: "BUSY", active: true},
		{name: "mutex abandonado", code: "OWNERSHIP_ERROR", cause: "ABANDONED", ownerErr: ownership.ErrAbandoned},
		{name: "abandono com falha no cleanup", code: "OWNERSHIP_ERROR", cause: "ABANDONED", ownerErr: errors.Join(ownership.ErrAbandoned, syscall.Errno(5))},
	} {
		t.Run(tc.name, func(t *testing.T) {
			s, path := newRegressionServer(t, &controlledTransport{}, &controlledOwner{err: tc.ownerErr})
			if tc.active {
				s.active.Lock()
				t.Cleanup(s.active.Unlock)
			}
			session := startConcurrencySession(t, s, nil)
			exchangeConcurrencyFrame(t, session.client, bridgeprotocol.Hello, "hello-072")
			response := exchangeConcurrencyFrame(t, session.client, bridgeprotocol.Acquire, "failure-072")
			if response.MessageType != bridgeprotocol.Error || response.CorrelationID != "failure-072" || !strings.HasPrefix(string(response.Payload), tc.code+":") {
				t.Fatal(response)
			}
			if err := awaitConcurrencySession(t, session, 2*time.Second); causeCode(err) != tc.cause {
				t.Fatalf("causa final = %v (%s)", err, causeCode(err))
			}
			contents, err := os.ReadFile(path)
			if err != nil {
				t.Fatal(err)
			}
			for _, line := range strings.Split(string(contents), "\n") {
				if strings.Contains(line, `event="ownership_failed"`) {
					if !strings.Contains(line, `code="`+tc.code+`"`) || !strings.Contains(line, `cause="`+tc.cause+`"`) || !strings.Contains(line, `correlationId="failure-072"`) {
						t.Fatalf("evento sem diagnóstico específico: %s", line)
					}
					return
				}
			}
			t.Fatal("evento ownership_failed ausente")
		})
	}
}

type reusingChunkTransport struct {
	fakeTransport
	buffer []byte
	next   byte
	count  int
}

func (f *reusingChunkTransport) Read(ctx context.Context) ([]byte, error) {
	if f.count > 0 {
		f.count--
		f.next++
		for i := range f.buffer {
			f.buffer[i] = f.next
		}
		return f.buffer, nil
	}
	<-ctx.Done()
	return nil, ctx.Err()
}

func TestSerialChunksRemainIntactWhenTransportReusesBuffer(t *testing.T) {
	s, _ := newRegressionServer(t, &controlledTransport{}, &controlledOwner{})
	const count = 16
	transport := &reusingChunkTransport{buffer: make([]byte, 512*1024), count: count}
	s.config.Transport = transport
	session := acquireConcurrencySession(t, s, nil)
	ctx, cancel := context.WithTimeout(context.Background(), 3*time.Second)
	defer cancel()
	for i := range count {
		response, err := bridgeprotocol.ReadFrame(ctx, session.client)
		if err != nil {
			t.Fatal(err)
		}
		if response.MessageType != bridgeprotocol.Data || !bytes.Equal(response.Payload, bytes.Repeat([]byte{byte(i + 1)}, 512*1024)) {
			t.Fatalf("chunk %d foi alterado pela próxima leitura", i)
		}
	}
	if ack := exchangeConcurrencyFrame(t, session.client, bridgeprotocol.Close, "close-072"); ack.MessageType != bridgeprotocol.Close {
		t.Fatal(ack)
	}
	if err := awaitConcurrencySession(t, session, 2*time.Second); err != nil {
		t.Fatal(err)
	}
}

type orderedCleanupTransport struct {
	fakeTransport
	readCanceled, finishRead, readReturned chan struct{}
	closeStarted, finishClose              chan struct{}
}

func (f *orderedCleanupTransport) Read(ctx context.Context) ([]byte, error) {
	<-ctx.Done()
	close(f.readCanceled)
	<-f.finishRead
	close(f.readReturned)
	return nil, ctx.Err()
}

func (f *orderedCleanupTransport) Close() error {
	select {
	case <-f.readReturned:
	default:
		return errors.New("serial fechada antes do retorno de Read")
	}
	close(f.closeStarted)
	<-f.finishClose
	return f.fakeTransport.Close()
}

type orderedCleanupOwner struct {
	started, finish chan struct{}
}

func (o *orderedCleanupOwner) Acquire(context.Context, string) (func() error, error) {
	return func() error { close(o.started); <-o.finish; return nil }, nil
}

func TestCloseACKWaitsForWorkersSerialAndOwnership(t *testing.T) {
	s, _ := newRegressionServer(t, &controlledTransport{}, &controlledOwner{})
	transport := &orderedCleanupTransport{
		readCanceled: make(chan struct{}), finishRead: make(chan struct{}), readReturned: make(chan struct{}),
		closeStarted: make(chan struct{}), finishClose: make(chan struct{}),
	}
	owner := &orderedCleanupOwner{started: make(chan struct{}), finish: make(chan struct{})}
	s.config.Transport, s.config.Ownership = transport, owner
	session := acquireConcurrencySession(t, s, nil)
	finishRead := sync.OnceFunc(func() { close(transport.finishRead) })
	finishClose := sync.OnceFunc(func() { close(transport.finishClose) })
	finishRelease := sync.OnceFunc(func() { close(owner.finish) })
	t.Cleanup(func() { finishRead(); finishClose(); finishRelease() })
	ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
	defer cancel()
	if err := bridgeprotocol.WriteFrame(ctx, session.client, bridgeprotocol.Frame{MessageType: bridgeprotocol.Close, CorrelationID: "close-072"}); err != nil {
		t.Fatal(err)
	}
	awaitStage := func(stage <-chan struct{}) {
		t.Helper()
		select {
		case <-stage:
		case <-ctx.Done():
			t.Fatal("cleanup não alcançou a próxima fase")
		}
	}
	assertNoACK := func() {
		t.Helper()
		if err := session.client.SetReadDeadline(time.Now().Add(30 * time.Millisecond)); err != nil {
			t.Fatal(err)
		}
		_, err := session.client.Read(make([]byte, 1))
		var timeout net.Error
		if !errors.As(err, &timeout) || !timeout.Timeout() {
			t.Fatalf("ACK/socket liberado antes do cleanup: %v", err)
		}
	}
	awaitStage(transport.readCanceled)
	select {
	case <-transport.closeStarted:
		t.Fatal("Close serial não aguardou o worker")
	default:
	}
	assertNoACK()
	finishRead()
	awaitStage(transport.closeStarted)
	assertNoACK()
	finishClose()
	awaitStage(owner.started)
	if transport.IsOpen() {
		t.Fatal("ownership liberado antes da serial")
	}
	assertNoACK()
	finishRelease()
	ack, err := bridgeprotocol.ReadFrame(ctx, session.client)
	if err != nil || ack.MessageType != bridgeprotocol.Close || ack.CorrelationID != "close-072" {
		t.Fatalf("ACK final: %v err=%v", ack, err)
	}
	if err := awaitConcurrencySession(t, session, time.Second); err != nil {
		t.Fatal(err)
	}
	if !s.active.TryLock() {
		t.Fatal("transporte não foi disponibilizado após cleanup")
	}
	s.active.Unlock()
}
