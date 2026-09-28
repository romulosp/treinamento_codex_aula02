// Package bridge implementa o servidor local que liga o stream ao transporte
// serial sem interpretar o protocolo ABECS.
package bridge

import (
	"context"
	"errors"
	"fmt"
	"net"
	"strings"
	"sync"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/port"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridgeprotocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/ownership"
)

// Config define o endpoint local e o recurso serial protegido.
type Config struct {
	Bind       string
	Port       int
	SerialName string
	Transport  port.Transport
	Ownership  ownership.Acquirer
}

// Server atende uma sessão Bridge por vez.
type Server struct {
	config Config
	active sync.Mutex
}

// New valida a composição do Bridge.
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
	return &Server{config: config}, nil
}

// Serve aceita conexões até o contexto ser cancelado.
func (s *Server) Serve(ctx context.Context) error {
	if ctx == nil {
		ctx = context.Background()
	}
	listener, err := net.Listen("tcp", fmt.Sprintf("%s:%d", s.config.Bind, s.config.Port))
	if err != nil {
		return fmt.Errorf("listen bridge: %w", err)
	}
	defer listener.Close()
	for {
		if err := listener.(*net.TCPListener).SetDeadline(time.Now().Add(100 * time.Millisecond)); err != nil {
			return fmt.Errorf("set bridge accept deadline: %w", err)
		}
		conn, err := listener.Accept()
		if err != nil {
			if ctx.Err() != nil {
				return nil
			}
			if netErr, ok := err.(net.Error); ok && netErr.Timeout() {
				continue
			}
			return fmt.Errorf("accept bridge connection: %w", err)
		}
		go func() {
			_ = s.handle(ctx, conn)
		}()
	}
}

func (s *Server) handle(ctx context.Context, conn net.Conn) error {
	defer conn.Close()
	frame, err := bridgeprotocol.ReadFrame(ctx, conn)
	if err != nil {
		return err
	}
	if frame.MessageType != bridgeprotocol.Hello {
		return s.sendError(conn, "INVALID_FRAME", "hello required")
	}
	if err := bridgeprotocol.WriteFrame(ctx, conn, bridgeprotocol.Frame{MessageType: bridgeprotocol.HelloOK, CorrelationID: frame.CorrelationID}); err != nil {
		return err
	}
	frame, err = bridgeprotocol.ReadFrame(ctx, conn)
	if err != nil {
		return err
	}
	if frame.MessageType == bridgeprotocol.Ping {
		return bridgeprotocol.WriteFrame(ctx, conn, bridgeprotocol.Frame{
			MessageType:   bridgeprotocol.Pong,
			CorrelationID: frame.CorrelationID,
		})
	}
	if frame.MessageType != bridgeprotocol.Acquire {
		return s.sendError(conn, "INVALID_FRAME", "acquire required")
	}
	if !s.active.TryLock() {
		return s.sendError(conn, "BUSY", "active bridge session")
	}
	defer s.active.Unlock()
	release, err := s.config.Ownership.Acquire(ctx, s.config.SerialName)
	if err != nil {
		if errors.Is(err, ownership.ErrBusy) {
			return s.sendError(conn, "BUSY", "serial ownership unavailable")
		}
		return s.sendError(conn, "OWNERSHIP_ERROR", "serial ownership failed")
	}
	defer release()
	if err := s.config.Transport.Open(); err != nil {
		return s.sendError(conn, "SERIAL_UNAVAILABLE", "serial open failed")
	}
	defer s.config.Transport.Close()
	if err := bridgeprotocol.WriteFrame(ctx, conn, bridgeprotocol.Frame{MessageType: bridgeprotocol.AcquireOK, CorrelationID: frame.CorrelationID}); err != nil {
		return err
	}

	readCtx, cancel := context.WithCancel(ctx)
	defer cancel()
	serialErr := make(chan error, 1)
	var correlationMu sync.RWMutex
	correlationID := ""
	setCorrelation := func(value string) {
		correlationMu.Lock()
		correlationID = value
		correlationMu.Unlock()
	}
	getCorrelation := func() string {
		correlationMu.RLock()
		defer correlationMu.RUnlock()
		return correlationID
	}
	go s.forwardSerial(readCtx, conn, serialErr, getCorrelation)
	for {
		frame, err = bridgeprotocol.ReadFrame(readCtx, conn)
		if err != nil {
			return err
		}
		switch frame.MessageType {
		case bridgeprotocol.Data:
			setCorrelation(frame.CorrelationID)
			if err := s.config.Transport.Write(frame.Payload); err != nil {
				return s.sendError(conn, "SERIAL_UNAVAILABLE", "serial write failed")
			}
		case bridgeprotocol.Ping:
			if err := bridgeprotocol.WriteFrame(readCtx, conn, bridgeprotocol.Frame{MessageType: bridgeprotocol.Pong, CorrelationID: frame.CorrelationID}); err != nil {
				return err
			}
		case bridgeprotocol.Release, bridgeprotocol.Close:
			return nil
		case bridgeprotocol.Error:
			return fmt.Errorf("peer bridge error: %s", strings.TrimSpace(string(frame.Payload)))
		default:
			return s.sendError(conn, "INVALID_FRAME", "unexpected session message")
		}
		select {
		case err := <-serialErr:
			if err != nil {
				return s.sendError(conn, "SERIAL_UNAVAILABLE", "serial read failed")
			}
		default:
		}
	}
}

func (s *Server) forwardSerial(ctx context.Context, conn net.Conn, serialErr chan<- error, correlationID func() string) {
	for {
		data, err := s.config.Transport.Read(ctx)
		if err != nil {
			if ctx.Err() == nil {
				serialErr <- err
			}
			return
		}
		if len(data) == 0 {
			timer := time.NewTimer(10 * time.Millisecond)
			select {
			case <-ctx.Done():
				if !timer.Stop() {
					<-timer.C
				}
				return
			case <-timer.C:
			}
			continue
		}
		if err := bridgeprotocol.WriteFrame(ctx, conn, bridgeprotocol.Frame{
			MessageType:   bridgeprotocol.Data,
			CorrelationID: correlationID(),
			Payload:       data,
		}); err != nil {
			if ctx.Err() == nil {
				serialErr <- err
			}
			return
		}
	}
}

func (s *Server) sendError(conn net.Conn, code, message string) error {
	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()
	return bridgeprotocol.WriteFrame(ctx, conn, bridgeprotocol.Frame{MessageType: bridgeprotocol.Error, Payload: bridgeprotocol.ErrorPayload(code, message)})
}
