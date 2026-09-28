// Package bridge liga o stream local ao transporte serial sem interpretar ABECS.
package bridge

import (
	"context"
	"errors"
	"fmt"
	"io"
	"log"
	"net"
	"os"
	"strconv"
	"sync"
	"sync/atomic"
	"syscall"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/port"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridgeprotocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/ownership"
)

// Config define endpoint, recurso protegido e observabilidade compartilhada.
// OnListening só é chamado após bind e persistência do evento de readiness.
type Config struct {
	Bind        string
	Port        int
	SerialName  string
	Transport   port.Transport
	Ownership   ownership.Acquirer
	Tracer      *logging.Tracer
	Mode        string
	OnListening func(string)
}

// Server atende uma sessão serial por vez e aguarda os workers ao parar.
type Server struct {
	config Config
	active sync.Mutex
	nextID atomic.Uint64
}

// New valida a composição do servidor restrito ao loopback.
func New(config Config) (*Server, error) {
	if config.Bind == "" {
		config.Bind = "127.0.0.1"
	}
	if config.Bind != "127.0.0.1" {
		return nil, fmt.Errorf("bridge bind must remain loopback")
	}
	if config.Port < 1 || config.Port > 65535 {
		return nil, fmt.Errorf("invalid bridge port")
	}
	if config.SerialName == "" || config.Transport == nil || config.Ownership == nil {
		return nil, fmt.Errorf("bridge dependencies are incomplete")
	}
	if config.Mode == "" {
		config.Mode = "physical"
	}
	return &Server{config: config}, nil
}

// Serve aceita conexões até cancelamento; readiness não indica sessão ABECS.
func (s *Server) Serve(ctx context.Context) error {
	if ctx == nil {
		ctx = context.Background()
	}
	ctx, cancel := context.WithCancel(ctx)
	defer cancel()
	listener, err := net.Listen("tcp", net.JoinHostPort(s.config.Bind, strconv.Itoa(s.config.Port)))
	if err != nil {
		return fmt.Errorf("listen bridge: %w", err)
	}
	defer listener.Close()
	stop := context.AfterFunc(ctx, func() { _ = listener.Close() })
	defer stop()
	if err := s.event("bridge_listening", "listen", "OK", "", "", nil); err != nil {
		return err
	}
	if s.config.OnListening != nil {
		s.config.OnListening(listener.Addr().String())
	}
	var workers sync.WaitGroup
	defer func() { cancel(); workers.Wait() }()
	for {
		conn, err := listener.Accept()
		if err != nil {
			if ctx.Err() != nil {
				return nil
			}
			return fmt.Errorf("accept bridge connection: %w", err)
		}
		workers.Add(1)
		go func() {
			defer workers.Done()
			if err := s.handle(ctx, conn); err != nil {
				log.Printf("bridge session ended: cause=%s", causeCode(err))
			}
		}()
	}
}

func (s *Server) event(event, phase, code, correlation, session string, cause error) error {
	err := s.config.Tracer.RecordBridgeEvent(logging.BridgeEvent{
		Event: event, Phase: phase, Code: code, CorrelationID: correlation,
		SessionID: session, Port: s.config.SerialName, Mode: s.config.Mode,
		PID: os.Getpid(), CauseCode: causeCode(cause),
		Endpoint: net.JoinHostPort(s.config.Bind, strconv.Itoa(s.config.Port)),
	})
	if err != nil {
		log.Print("bridge: rastro indisponivel; sessao interrompida")
	}
	return err
}

// causeCode preserva categoria/errno sem copiar mensagens de terceiros.
func causeCode(err error) string {
	if err == nil {
		return ""
	}
	if errors.Is(err, ownership.ErrAbandoned) {
		return "ABANDONED"
	}
	var errno syscall.Errno
	if errors.As(err, &errno) {
		return fmt.Sprintf("OS_%d", errno)
	}
	switch {
	case errors.Is(err, ownership.ErrBusy):
		return "BUSY"
	case errors.Is(err, context.Canceled):
		return "CANCELED"
	case errors.Is(err, context.DeadlineExceeded):
		return "TIMEOUT"
	case errors.Is(err, io.EOF), errors.Is(err, io.ErrUnexpectedEOF), errors.Is(err, net.ErrClosed):
		return "DISCONNECTED"
	case errors.Is(err, bridgeprotocol.ErrInvalidFrame):
		return "INVALID_FRAME"
	default:
		return "INTERNAL_ERROR"
	}
}

const peerWriteTimeout = 10 * time.Second

// peer serializa frames completos com prazo total, inclusive ao esperar outro
// escritor. Qualquer falha inutiliza o socket: um frame parcial não admite ERROR.
type peer struct {
	conn     net.Conn
	init     sync.Once
	writing  chan struct{}
	unusable atomic.Bool
}

func (p *peer) write(ctx context.Context, frame bridgeprotocol.Frame) error {
	if ctx == nil {
		ctx = context.Background()
	}
	ctx, cancel := context.WithTimeout(ctx, peerWriteTimeout)
	defer cancel()
	p.init.Do(func() { p.writing = make(chan struct{}, 1) })
	select {
	case p.writing <- struct{}{}:
		defer func() { <-p.writing }()
	case <-ctx.Done():
		return p.failWrite(ctx.Err())
	}
	if p.unusable.Load() {
		return net.ErrClosed
	}
	if err := bridgeprotocol.WriteFrame(ctx, p.conn, frame); err != nil {
		return p.failWrite(err)
	}
	return nil
}

// failWrite fecha o socket uma única vez e preserva os erros de escrita/close.
func (p *peer) failWrite(err error) error {
	if p.unusable.CompareAndSwap(false, true) {
		return errors.Join(err, p.conn.Close())
	}
	return err
}

func (s *Server) handle(parent context.Context, conn net.Conn) (result error) {
	ctx, cancel := context.WithCancel(parent)
	defer cancel()
	defer conn.Close()
	p := &peer{conn: conn}
	correlation, session := "", ""
	releaseRequested := false
	// CLOSE confirma cleanup concluído usando o messageType existente. Permite
	// reabertura sem corrida com a sessão anterior, sem replay de operação.
	defer func() {
		if releaseRequested && result == nil {
			ackCtx, stop := context.WithTimeout(context.Background(), time.Second)
			defer stop()
			result = errors.Join(result, p.write(ackCtx, bridgeprotocol.Frame{MessageType: bridgeprotocol.Close, CorrelationID: correlation}))
		}
	}()
	defer func() {
		result = errors.Join(result, s.event("client_disconnected", "connection", causeCode(result), correlation, session, result))
	}()
	if err := s.event("client_connected", "connection", "OK", "", "", nil); err != nil {
		return err
	}
	fail := func(code, phase string, cause error) error {
		traceErr := s.event("session_error", phase, code, correlation, session, cause)
		errCtx, stop := context.WithTimeout(context.Background(), time.Second)
		defer stop()
		writeErr := p.write(errCtx, bridgeprotocol.Frame{MessageType: bridgeprotocol.Error,
			CorrelationID: correlation, Payload: bridgeprotocol.ErrorPayload(code, phase+" failed")})
		return errors.Join(cause, traceErr, writeErr)
	}
	handshake, stop := context.WithTimeout(ctx, 10*time.Second)
	defer stop()
	frame, err := bridgeprotocol.ReadFrame(handshake, conn)
	if err != nil {
		return fail("INVALID_FRAME", "hello", err)
	}
	correlation = frame.CorrelationID
	if frame.MessageType != bridgeprotocol.Hello {
		return fail("INVALID_FRAME", "hello", bridgeprotocol.ErrInvalidFrame)
	}
	if err := p.write(handshake, bridgeprotocol.Frame{MessageType: bridgeprotocol.HelloOK, CorrelationID: correlation}); err != nil {
		return fail("DISCONNECTED", "hello", err)
	}
	if err := s.event("hello_ok", "hello", "OK", correlation, "", nil); err != nil {
		return err
	}
	frame, err = bridgeprotocol.ReadFrame(handshake, conn)
	if err != nil {
		return fail("INVALID_FRAME", "acquire", err)
	}
	correlation = frame.CorrelationID
	if frame.MessageType == bridgeprotocol.Ping {
		if err := s.event("ping_ok", "ping", "OK", correlation, "", nil); err != nil {
			return err
		}
		return p.write(handshake, bridgeprotocol.Frame{MessageType: bridgeprotocol.Pong, CorrelationID: correlation})
	}
	if frame.MessageType != bridgeprotocol.Acquire {
		return fail("INVALID_FRAME", "acquire", bridgeprotocol.ErrInvalidFrame)
	}
	if err := s.event("acquire_requested", "acquire", "OK", correlation, "", nil); err != nil {
		return err
	}
	if !s.active.TryLock() {
		traceErr := s.event("ownership_failed", "ownership", "BUSY", correlation, "", ownership.ErrBusy)
		return fail("BUSY", "ownership", errors.Join(ownership.ErrBusy, traceErr))
	}
	defer s.active.Unlock()
	release, err := s.config.Ownership.Acquire(ctx, s.config.SerialName)
	if err != nil {
		code := "OWNERSHIP_ERROR"
		if errors.Is(err, ownership.ErrBusy) {
			code = "BUSY"
		}
		traceErr := s.event("ownership_failed", "ownership", code, correlation, "", err)
		return fail(code, "ownership", errors.Join(err, traceErr))
	}
	defer func() {
		err := release()
		result = errors.Join(result, err, s.event("session_released", "release", causeCode(err), correlation, session, err))
	}()
	session = fmt.Sprintf("%d-%d", os.Getpid(), s.nextID.Add(1))
	if err := s.event("ownership_acquired", "ownership", "OK", correlation, session, nil); err != nil {
		return err
	}
	if err := s.config.Transport.Open(); err != nil {
		traceErr := s.event("serial_open_failed", "serial_open", "SERIAL_UNAVAILABLE", correlation, session, err)
		return fail("SERIAL_UNAVAILABLE", "serial_open", errors.Join(err, traceErr))
	}
	defer func() {
		err := s.config.Transport.Close()
		if err != nil {
			result = errors.Join(result, err, s.event("session_error", "serial_close", "SERIAL_UNAVAILABLE", correlation, session, err))
		}
	}()
	if err := s.event("session_acquired", "acquire", "OK", correlation, session, nil); err != nil {
		return err
	}
	if err := p.write(handshake, bridgeprotocol.Frame{MessageType: bridgeprotocol.AcquireOK, CorrelationID: correlation}); err != nil {
		return fail("DISCONNECTED", "acquire", err)
	}
	stop()

	type inbound struct {
		frame bridgeprotocol.Frame
		err   error
	}
	type serialChunk struct {
		data []byte
		err  error
	}
	frames, chunks := make(chan inbound), make(chan serialChunk)
	var workers sync.WaitGroup
	workers.Add(2)
	defer func() { cancel(); workers.Wait() }()
	go func() {
		defer workers.Done()
		for {
			frame, err := bridgeprotocol.ReadFrame(ctx, conn)
			select {
			case frames <- inbound{frame, err}:
			case <-ctx.Done():
				return
			}
			if err != nil {
				return
			}
		}
	}()
	go func() {
		defer workers.Done()
		for {
			data, err := s.config.Transport.Read(ctx)
			if len(data) != 0 || err != nil {
				// O transporte pode reutilizar seu buffer na próxima leitura.
				data = append([]byte(nil), data...)
				select {
				case chunks <- serialChunk{data, err}:
				case <-ctx.Done():
					return
				}
			}
			if err != nil {
				return
			}
			if len(data) == 0 {
				timer := time.NewTimer(10 * time.Millisecond)
				select {
				case <-ctx.Done():
					timer.Stop()
					return
				case <-timer.C:
				}
			}
		}
	}()
	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case chunk := <-chunks:
			if chunk.err != nil {
				return fail("SERIAL_UNAVAILABLE", "serial_read", chunk.err)
			}
			if err := p.write(ctx, bridgeprotocol.Frame{MessageType: bridgeprotocol.Data, CorrelationID: correlation, Payload: chunk.data}); err != nil {
				return fail("DISCONNECTED", "forward", err)
			}
		case in := <-frames:
			if in.err != nil {
				return fail("DISCONNECTED", "receive", in.err)
			}
			frame = in.frame
			correlation = frame.CorrelationID
			switch frame.MessageType {
			case bridgeprotocol.Data:
				if err := s.config.Transport.Write(frame.Payload); err != nil {
					return fail("SERIAL_UNAVAILABLE", "serial_write", err)
				}
			case bridgeprotocol.Ping:
				if err := s.event("ping_ok", "ping", "OK", correlation, session, nil); err != nil {
					return err
				}
				if err := p.write(ctx, bridgeprotocol.Frame{MessageType: bridgeprotocol.Pong, CorrelationID: correlation}); err != nil {
					return fail("DISCONNECTED", "ping", err)
				}
			case bridgeprotocol.Release, bridgeprotocol.Close:
				releaseRequested = true
				return nil
			default:
				return fail("INVALID_FRAME", "session", bridgeprotocol.ErrInvalidFrame)
			}
		}
	}
}
