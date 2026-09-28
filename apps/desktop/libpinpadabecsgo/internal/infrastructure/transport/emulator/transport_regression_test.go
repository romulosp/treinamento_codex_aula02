package emulator

import (
	"context"
	"errors"
	"fmt"
	"io"
	"net"
	"strings"
	"sync"
	"sync/atomic"
	"testing"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridgeprotocol"
)

const testWait = 3 * time.Second

type observedConn struct {
	net.Conn
	readStarted, writeStarted chan struct{}
	watchRead, watchWrite     atomic.Bool
	readOnce, writeOnce       sync.Once
}

func (c *observedConn) Read(p []byte) (int, error) {
	if c.watchRead.Load() {
		c.readOnce.Do(func() { close(c.readStarted) })
	}
	return c.Conn.Read(p)
}

func (c *observedConn) Write(p []byte) (int, error) {
	if c.watchWrite.Load() {
		c.writeOnce.Do(func() { close(c.writeStarted) })
	}
	return c.Conn.Write(p)
}

func newPipeTransport(t *testing.T, timeout time.Duration) (*Transport, *observedConn, net.Conn) {
	t.Helper()
	client, peer := net.Pipe()
	conn := &observedConn{Conn: client, readStarted: make(chan struct{}), writeStarted: make(chan struct{})}
	transport, err := New(Config{Port: 39100, Timeout: timeout, Dial: func(context.Context, string, string) (net.Conn, error) { return conn, nil }})
	if err != nil {
		t.Fatal(err)
	}
	t.Cleanup(func() { _ = client.Close(); _ = peer.Close() })
	return transport, conn, peer
}

// runPeer sempre aguarda a goroutine, inclusive se uma asserção encerrar o teste.
func runPeer(t *testing.T, peer net.Conn, run func(context.Context) error) func() error {
	t.Helper()
	ctx, cancel := context.WithTimeout(context.Background(), testWait)
	done := make(chan error, 1)
	go func() { defer peer.Close(); done <- run(ctx) }()
	var once sync.Once
	var result error
	wait := func() error {
		once.Do(func() { result = awaitError(t, done) })
		return result
	}
	t.Cleanup(func() { cancel(); _ = peer.Close(); _ = wait() })
	return wait
}

func awaitError(t *testing.T, done <-chan error) error {
	t.Helper()
	timer := time.NewTimer(testWait)
	defer timer.Stop()
	select {
	case err := <-done:
		return err
	case <-timer.C:
		t.Fatal("goroutine não terminou dentro do prazo")
		return nil
	}
}

func awaitSignal(t *testing.T, signal <-chan struct{}) {
	t.Helper()
	timer := time.NewTimer(testWait)
	defer timer.Stop()
	select {
	case <-signal:
	case <-timer.C:
		t.Fatal("operação não alcançou o ponto de sincronização")
	}
}

func expectFrame(ctx context.Context, peer net.Conn, want bridgeprotocol.MessageType) (bridgeprotocol.Frame, error) {
	frame, err := bridgeprotocol.ReadFrame(ctx, peer)
	if err == nil && frame.MessageType != want {
		err = fmt.Errorf("frame = %d, esperado %d", frame.MessageType, want)
	}
	return frame, err
}

func handshake(ctx context.Context, peer net.Conn) error {
	for _, step := range []struct{ request, response bridgeprotocol.MessageType }{
		{bridgeprotocol.Hello, bridgeprotocol.HelloOK},
		{bridgeprotocol.Acquire, bridgeprotocol.AcquireOK},
	} {
		frame, err := expectFrame(ctx, peer, step.request)
		if err != nil {
			return err
		}
		if err := bridgeprotocol.WriteFrame(ctx, peer, bridgeprotocol.Frame{MessageType: step.response, CorrelationID: frame.CorrelationID}); err != nil {
			return err
		}
	}
	return nil
}

func releaseAck(ctx context.Context, peer net.Conn) error {
	frame, err := expectFrame(ctx, peer, bridgeprotocol.Release)
	if err != nil {
		return err
	}
	return bridgeprotocol.WriteFrame(ctx, peer, bridgeprotocol.Frame{MessageType: bridgeprotocol.Close, CorrelationID: frame.CorrelationID})
}

func TestCloseInterruptsBlockedReadAndPreservesAck(t *testing.T) {
	transport, conn, peer := newPipeTransport(t, time.Second)
	wait := runPeer(t, peer, func(ctx context.Context) error {
		if err := handshake(ctx, peer); err != nil {
			return err
		}
		return releaseAck(ctx, peer)
	})
	if err := transport.Open(); err != nil {
		t.Fatal(err)
	}
	conn.watchRead.Store(true)
	readDone := make(chan error, 1)
	go func() { _, err := transport.Read(nil); readDone <- err }()
	awaitSignal(t, conn.readStarted)
	if err := transport.Close(); err != nil {
		t.Fatalf("Close perdeu ACK: %v", err)
	}
	if err := awaitError(t, readDone); !errors.Is(err, context.Canceled) {
		t.Fatalf("Read interrompido = %v", err)
	}
	if transport.IsOpen() {
		t.Fatal("Close manteve a sessão aberta")
	}
	if err := wait(); err != nil {
		t.Fatal(err)
	}
	if err := transport.Close(); err != nil {
		t.Fatalf("Close repetido = %v", err)
	}
}

func TestCloseInterruptsBlockedWriteAndPreservesAck(t *testing.T) {
	transport, conn, peer := newPipeTransport(t, time.Second)
	resume := make(chan struct{})
	wait := runPeer(t, peer, func(ctx context.Context) error {
		if err := handshake(ctx, peer); err != nil {
			return err
		}
		select {
		case <-resume:
		case <-ctx.Done():
			return ctx.Err()
		}
		return releaseAck(ctx, peer)
	})
	if err := transport.Open(); err != nil {
		t.Fatal(err)
	}
	conn.watchWrite.Store(true)
	writeDone, closeDone := make(chan error, 1), make(chan error, 1)
	go func() { writeDone <- transport.Write([]byte("ABECS")) }()
	awaitSignal(t, conn.writeStarted)
	go func() { closeDone <- transport.Close() }()
	if err := awaitError(t, writeDone); !errors.Is(err, context.Canceled) {
		t.Fatalf("Write interrompido = %v", err)
	}
	close(resume)
	if err := awaitError(t, closeDone); err != nil {
		t.Fatal(err)
	}
	if err := wait(); err != nil {
		t.Fatal(err)
	}
}

func TestReadAndPingWaitRespectDeadlineAndCancel(t *testing.T) {
	transport, conn, peer := newPipeTransport(t, time.Second)
	wait := runPeer(t, peer, func(ctx context.Context) error {
		if err := handshake(ctx, peer); err != nil {
			return err
		}
		return releaseAck(ctx, peer)
	})
	if err := transport.Open(); err != nil {
		t.Fatal(err)
	}
	conn.watchRead.Store(true)
	done := make(chan error, 1)
	go func() { _, err := transport.Read(nil); done <- err }()
	awaitSignal(t, conn.readStarted)
	ctx, cancel := context.WithTimeout(context.Background(), 20*time.Millisecond)
	defer cancel()
	if _, err := transport.Read(ctx); !errors.Is(err, context.DeadlineExceeded) {
		t.Fatalf("Read esperando lock = %v", err)
	}
	ctx, cancelPing := context.WithCancel(context.Background())
	cancelPing()
	if err := transport.Ping(ctx, "cancelado"); !errors.Is(err, context.Canceled) {
		t.Fatalf("Ping esperando lock = %v", err)
	}
	if !transport.IsOpen() {
		t.Fatal("operação que não iniciou I/O invalidou a sessão")
	}
	if err := transport.Close(); err != nil {
		t.Fatal(err)
	}
	_ = awaitError(t, done)
	if err := wait(); err != nil {
		t.Fatal(err)
	}
}

func TestWriteWaitRespectsOperationDeadlineWithoutSending(t *testing.T) {
	transport, conn, peer := newPipeTransport(t, time.Second)
	resume := make(chan struct{})
	wait := runPeer(t, peer, func(ctx context.Context) error {
		if err := handshake(ctx, peer); err != nil {
			return err
		}
		select {
		case <-resume:
		case <-ctx.Done():
			return ctx.Err()
		}
		frame, err := expectFrame(ctx, peer, bridgeprotocol.Data)
		if err != nil {
			return err
		}
		if string(frame.Payload) != "primeiro" {
			return fmt.Errorf("payload inesperado: %q", frame.Payload)
		}
		return releaseAck(ctx, peer)
	})
	if err := transport.Open(); err != nil {
		t.Fatal(err)
	}
	conn.watchWrite.Store(true)
	done := make(chan error, 1)
	go func() { done <- transport.Write([]byte("primeiro")) }()
	awaitSignal(t, conn.writeStarted)
	ctx, cancel := context.WithTimeout(context.Background(), 20*time.Millisecond)
	defer cancel()
	transport.SetOperationContext(ctx)
	if err := transport.Write([]byte("não enviar")); !errors.Is(err, context.DeadlineExceeded) {
		t.Fatalf("Write esperando lock = %v", err)
	}
	transport.SetOperationContext(nil)
	close(resume)
	if err := awaitError(t, done); err != nil {
		t.Fatal(err)
	}
	if err := transport.Close(); err != nil {
		t.Fatal(err)
	}
	if err := wait(); err != nil {
		t.Fatal(err)
	}
}

func TestReadNilHasBoundedTimeoutAndInvalidates(t *testing.T) {
	transport, _, peer := newPipeTransport(t, 30*time.Millisecond)
	wait := runPeer(t, peer, func(ctx context.Context) error {
		if err := handshake(ctx, peer); err != nil {
			return err
		}
		_, err := bridgeprotocol.ReadFrame(ctx, peer)
		if err == nil {
			return errors.New("sessão não foi encerrada")
		}
		return nil
	})
	if err := transport.Open(); err != nil {
		t.Fatal(err)
	}
	if _, err := transport.Read(nil); !errors.Is(err, context.DeadlineExceeded) {
		t.Fatalf("Read(nil) = %v", err)
	}
	if transport.IsOpen() {
		t.Fatal("timeout manteve a sessão aberta")
	}
	if err := wait(); err != nil {
		t.Fatal(err)
	}
}

func TestOpenConcurrentCallsAcquireOnlyOnce(t *testing.T) {
	transport, conn, peer := newPipeTransport(t, time.Second)
	var dials atomic.Int32
	transport.config.Dial = func(context.Context, string, string) (net.Conn, error) { dials.Add(1); return conn, nil }
	wait := runPeer(t, peer, func(ctx context.Context) error {
		if err := handshake(ctx, peer); err != nil {
			return err
		}
		return releaseAck(ctx, peer)
	})
	done := make(chan error, 16)
	for range 16 {
		go func() { done <- transport.Open() }()
	}
	for range 16 {
		if err := awaitError(t, done); err != nil {
			t.Fatal(err)
		}
	}
	if dials.Load() != 1 {
		t.Fatalf("aberturas simultâneas fizeram %d dials", dials.Load())
	}
	if err := transport.Close(); err != nil {
		t.Fatal(err)
	}
	if err := wait(); err != nil {
		t.Fatal(err)
	}
}

func TestCloseDuringDialPreventsLatePublication(t *testing.T) {
	transport, conn, _ := newPipeTransport(t, time.Second)
	entered, resume := make(chan struct{}), make(chan struct{})
	// Dial deliberadamente ignora cancelamento para provar a barreira de geração.
	transport.config.Dial = func(context.Context, string, string) (net.Conn, error) {
		close(entered)
		<-resume
		return conn, nil
	}
	done := make(chan error, 1)
	go func() { done <- transport.Open() }()
	awaitSignal(t, entered)
	if err := transport.Close(); err != nil {
		t.Fatal(err)
	}
	close(resume)
	if err := awaitError(t, done); !errors.Is(err, context.Canceled) {
		t.Fatalf("Open depois de Close = %v", err)
	}
	if transport.IsOpen() {
		t.Fatal("Open publicou conexão de geração encerrada")
	}
	if _, err := conn.Write([]byte("x")); err == nil {
		t.Fatal("socket tardio não foi fechado")
	}
}

func TestOpenWaitCanBeCanceled(t *testing.T) {
	transport, _, _ := newPipeTransport(t, time.Second)
	entered := make(chan struct{})
	transport.config.Dial = func(ctx context.Context, _, _ string) (net.Conn, error) {
		close(entered)
		<-ctx.Done()
		return nil, ctx.Err()
	}
	done := make(chan error, 1)
	go func() { done <- transport.Open() }()
	awaitSignal(t, entered)
	ctx, cancel := context.WithCancel(context.Background())
	transport.SetOperationContext(ctx)
	cancel()
	if err := transport.Open(); !errors.Is(err, context.Canceled) {
		t.Fatalf("Open esperando lock = %v", err)
	}
	if err := transport.Close(); err != nil {
		t.Fatal(err)
	}
	if err := awaitError(t, done); !errors.Is(err, context.Canceled) {
		t.Fatalf("dial cancelado = %v", err)
	}
}

func TestCloseDuringHandshakeAbortsPendingSocket(t *testing.T) {
	transport, _, peer := newPipeTransport(t, time.Second)
	acquired := make(chan struct{})
	wait := runPeer(t, peer, func(ctx context.Context) error {
		if _, err := expectFrame(ctx, peer, bridgeprotocol.Hello); err != nil {
			return err
		}
		if err := bridgeprotocol.WriteFrame(ctx, peer, bridgeprotocol.Frame{MessageType: bridgeprotocol.HelloOK}); err != nil {
			return err
		}
		if _, err := expectFrame(ctx, peer, bridgeprotocol.Acquire); err != nil {
			return err
		}
		close(acquired)
		_, err := bridgeprotocol.ReadFrame(ctx, peer)
		if err == nil {
			return errors.New("socket pendente permaneceu aberto")
		}
		return nil
	})
	done := make(chan error, 1)
	go func() { done <- transport.Open() }()
	awaitSignal(t, acquired)
	if err := transport.Close(); err != nil {
		t.Fatal(err)
	}
	if err := awaitError(t, done); err == nil {
		t.Fatal("handshake interrompido retornou sucesso")
	}
	if transport.IsOpen() {
		t.Fatal("handshake interrompido publicou sessão")
	}
	if err := wait(); err != nil {
		t.Fatal(err)
	}
}

func TestUnexpectedAndRemoteFramesAreSafeAndInvalidate(t *testing.T) {
	const secret = "PAN=4111111111111111\nC:/driver/private"
	for _, action := range []string{"hello", "acquire", "read", "ping", "ping_hello"} {
		for _, remote := range []bool{false, true} {
			t.Run(fmt.Sprintf("%s/remote=%t", action, remote), func(t *testing.T) {
				transport, _, peer := newPipeTransport(t, time.Second)
				frame := bridgeprotocol.Frame{MessageType: bridgeprotocol.Data, CorrelationID: secret, Payload: []byte(secret)}
				if remote {
					frame.MessageType = bridgeprotocol.Error
					frame.Payload = []byte("SERIAL_UNAVAILABLE:" + secret)
				}
				wait := runPeer(t, peer, func(ctx context.Context) error {
					if action == "hello" || action == "ping_hello" {
						if _, err := expectFrame(ctx, peer, bridgeprotocol.Hello); err != nil {
							return err
						}
					} else if action == "acquire" {
						if _, err := expectFrame(ctx, peer, bridgeprotocol.Hello); err != nil {
							return err
						}
						if err := bridgeprotocol.WriteFrame(ctx, peer, bridgeprotocol.Frame{MessageType: bridgeprotocol.HelloOK}); err != nil {
							return err
						}
						if _, err := expectFrame(ctx, peer, bridgeprotocol.Acquire); err != nil {
							return err
						}
					} else {
						if err := handshake(ctx, peer); err != nil {
							return err
						}
						if action == "ping" {
							if _, err := expectFrame(ctx, peer, bridgeprotocol.Ping); err != nil {
								return err
							}
						} else if !remote {
							frame.MessageType = bridgeprotocol.HelloOK
						}
					}
					return bridgeprotocol.WriteFrame(ctx, peer, frame)
				})
				var err error
				switch action {
				case "hello", "acquire":
					err = transport.Open()
				case "ping_hello":
					err = transport.Ping(nil, "preflight")
				default:
					if err = transport.Open(); err != nil {
						t.Fatal(err)
					}
					if action == "read" {
						_, err = transport.Read(nil)
					} else {
						err = transport.Ping(nil, "ping")
					}
				}
				if err == nil || strings.Contains(err.Error(), "411111") || strings.Contains(err.Error(), "private") || strings.Contains(err.Error(), "\n") {
					t.Fatalf("erro não sanitizado: %v", err)
				}
				if !remote && !errors.Is(err, bridgeprotocol.ErrInvalidFrame) {
					t.Fatalf("sentinela de protocolo ausente: %v", err)
				}
				if remote && err.Error() != "SERIAL_UNAVAILABLE:bridge" {
					t.Fatalf("erro remoto = %v", err)
				}
				if transport.IsOpen() {
					t.Fatal("falha manteve sessão aberta")
				}
				if err := wait(); err != nil {
					t.Fatal(err)
				}
			})
		}
	}
}

func TestCloseAckVariants(t *testing.T) {
	for _, variant := range []string{"ack", "legacy_eof", "unexpected", "timeout"} {
		t.Run(variant, func(t *testing.T) {
			transport, _, peer := newPipeTransport(t, 100*time.Millisecond)
			wait := runPeer(t, peer, func(ctx context.Context) error {
				if err := handshake(ctx, peer); err != nil {
					return err
				}
				if _, err := expectFrame(ctx, peer, bridgeprotocol.Release); err != nil {
					return err
				}
				switch variant {
				case "ack":
					return bridgeprotocol.WriteFrame(ctx, peer, bridgeprotocol.Frame{MessageType: bridgeprotocol.Close})
				case "unexpected":
					return bridgeprotocol.WriteFrame(ctx, peer, bridgeprotocol.Frame{MessageType: bridgeprotocol.Data, Payload: []byte("segredo")})
				case "timeout":
					_, err := bridgeprotocol.ReadFrame(ctx, peer)
					if err == nil {
						return errors.New("Close não fechou socket após timeout")
					}
				}
				return nil
			})
			if err := transport.Open(); err != nil {
				t.Fatal(err)
			}
			err := transport.Close()
			switch variant {
			case "ack", "legacy_eof":
				if err != nil {
					t.Fatal(err)
				}
			case "unexpected":
				if !errors.Is(err, bridgeprotocol.ErrInvalidFrame) || strings.Contains(err.Error(), "segredo") {
					t.Fatalf("ACK inválido = %v", err)
				}
			case "timeout":
				if !errors.Is(err, context.DeadlineExceeded) {
					t.Fatalf("ACK sem resposta = %v", err)
				}
			}
			if err := wait(); err != nil {
				t.Fatal(err)
			}
			if err := transport.Close(); err != nil {
				t.Fatal(err)
			}
		})
	}
}

func TestRemoteErrorAllowlist(t *testing.T) {
	for _, code := range []string{"BUSY", "OWNERSHIP_ERROR", "SERIAL_UNAVAILABLE", "TIMEOUT", "CANCELED", "DISCONNECTED", "INVALID_FRAME"} {
		for _, phase := range []string{"hello", "acquire", "ownership", "serial_open", "serial_read", "serial_write", "session", "ping", "receive", "forward"} {
			err := mapError([]byte(code + ":" + phase + " failed"))
			if err.Error() != code+":"+phase {
				t.Fatalf("erro sanitizado = %v", err)
			}
			if code == "BUSY" && !errors.Is(err, ErrBusy) || code == "DISCONNECTED" && !errors.Is(err, ErrDisconnected) {
				t.Fatalf("sentinela perdida: %v", err)
			}
		}
	}
	if err := mapError(nil); !errors.Is(err, ErrDisconnected) {
		t.Fatal(err)
	}
	if err := mapError([]byte("segredo:driver\nPAN")); err.Error() != "BINDING_ERROR:bridge" {
		t.Fatal(err)
	}
	if err := mapError([]byte("BUSY")); err.Error() != "BUSY:bridge" {
		t.Fatal(err)
	}
}

func TestReadCloseFrameInvalidates(t *testing.T) {
	transport, _, peer := newPipeTransport(t, time.Second)
	wait := runPeer(t, peer, func(ctx context.Context) error {
		if err := handshake(ctx, peer); err != nil {
			return err
		}
		return bridgeprotocol.WriteFrame(ctx, peer, bridgeprotocol.Frame{MessageType: bridgeprotocol.Close})
	})
	if err := transport.Open(); err != nil {
		t.Fatal(err)
	}
	if _, err := transport.Read(nil); !errors.Is(err, ErrDisconnected) {
		t.Fatal(err)
	}
	if err := wait(); err != nil {
		t.Fatal(err)
	}
}

func TestDialAndClosedErrors(t *testing.T) {
	transport, err := New(Config{Port: 39100, Dial: func(context.Context, string, string) (net.Conn, error) { return nil, io.EOF }})
	if err != nil {
		t.Fatal(err)
	}
	if err := transport.Open(); !errors.Is(err, ErrUnreachable) || !errors.Is(err, io.EOF) {
		t.Fatal(err)
	}
	if err := transport.Ping(nil, ""); !errors.Is(err, ErrUnreachable) {
		t.Fatal(err)
	}
	if _, err := transport.Read(nil); !errors.Is(err, ErrDisconnected) {
		t.Fatal(err)
	}
	if err := transport.Write([]byte("x")); !errors.Is(err, ErrDisconnected) {
		t.Fatal(err)
	}
	if err := transport.Write(nil); err == nil {
		t.Fatal("Write vazio retornou sucesso")
	}
	if _, err := New(Config{Port: -1}); err == nil {
		t.Fatal("porta inválida aceita")
	}
	if _, err := New(Config{Port: 39100}); err != nil {
		t.Fatal(err)
	}
}
