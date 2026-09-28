// Package emulator implementa o transporte TCP usado pelo Android Emulator.
package emulator

import (
	"context"
	"errors"
	"fmt"
	"net"
	"strconv"
	"sync"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridgeprotocol"
)

var (
	// ErrDisconnected identifica o encerramento inesperado do Bridge.
	ErrDisconnected = errors.New("bridge disconnected")
	// ErrBusy identifica ownership já atribuído a outra sessão.
	ErrBusy = errors.New("bridge busy")
)

// Dialer permite substituir net.Dialer nos testes.
type Dialer func(context.Context, string, string) (net.Conn, error)

// Config define o endpoint e os prazos do Bridge.
type Config struct {
	Host    string
	Port    int
	Timeout time.Duration
	Dial    Dialer
}

// Transport encapsula uma sessão persistente com o Bridge.
type Transport struct {
	mu            sync.RWMutex
	writeMu       sync.Mutex
	readMu        sync.Mutex
	conn          net.Conn
	config        Config
	correlationID string
}

// New valida e cria um transporte ainda fechado.
func New(config Config) (*Transport, error) {
	if config.Host == "" {
		config.Host = "localhost"
	}
	if config.Port < 1 || config.Port > 65535 {
		return nil, fmt.Errorf("invalid bridge port")
	}
	if config.Timeout <= 0 {
		config.Timeout = 30 * time.Second
	}
	if config.Dial == nil {
		dialer := &net.Dialer{}
		config.Dial = dialer.DialContext
	}
	return &Transport{config: config}, nil
}

// Open negocia a versão e reserva a sessão serial do Bridge.
func (t *Transport) Open() error {
	t.mu.Lock()
	if t.conn != nil {
		t.mu.Unlock()
		return nil
	}
	config := t.config
	t.mu.Unlock()
	ctx, cancel := context.WithTimeout(context.Background(), config.Timeout)
	defer cancel()
	conn, err := config.Dial(ctx, "tcp", net.JoinHostPort(config.Host, strconv.Itoa(config.Port)))
	if err != nil {
		return fmt.Errorf("dial bridge: %w", err)
	}
	correlationID := t.currentCorrelationID("open")
	if err := bridgeprotocol.WriteFrame(ctx, conn, bridgeprotocol.Frame{MessageType: bridgeprotocol.Hello, CorrelationID: correlationID}); err != nil {
		_ = conn.Close()
		return err
	}
	response, err := bridgeprotocol.ReadFrame(ctx, conn)
	if err != nil {
		_ = conn.Close()
		return err
	}
	if response.MessageType == bridgeprotocol.Error {
		_ = conn.Close()
		return mapError(response.Payload)
	}
	if response.MessageType != bridgeprotocol.HelloOK {
		_ = conn.Close()
		return fmt.Errorf("unexpected bridge hello response")
	}
	if err := bridgeprotocol.WriteFrame(ctx, conn, bridgeprotocol.Frame{MessageType: bridgeprotocol.Acquire, CorrelationID: correlationID}); err != nil {
		_ = conn.Close()
		return err
	}
	response, err = bridgeprotocol.ReadFrame(ctx, conn)
	if err != nil {
		_ = conn.Close()
		return err
	}
	if response.MessageType == bridgeprotocol.Error {
		_ = conn.Close()
		return mapError(response.Payload)
	}
	if response.MessageType != bridgeprotocol.AcquireOK {
		_ = conn.Close()
		return fmt.Errorf("unexpected bridge acquire response")
	}
	t.mu.Lock()
	t.conn = conn
	t.mu.Unlock()
	return nil
}

// Close libera a sessão e fecha o socket de forma idempotente.
func (t *Transport) Close() error {
	t.mu.Lock()
	conn := t.conn
	t.conn = nil
	t.mu.Unlock()
	if conn == nil {
		return nil
	}
	ctx, cancel := context.WithTimeout(context.Background(), t.config.Timeout)
	defer cancel()
	t.writeMu.Lock()
	_ = bridgeprotocol.WriteFrame(ctx, conn, bridgeprotocol.Frame{MessageType: bridgeprotocol.Release, CorrelationID: t.currentCorrelationID("close")})
	t.writeMu.Unlock()
	return conn.Close()
}

// Read recebe um bloco de bytes ABECS, preservando sua ordem e conteúdo.
func (t *Transport) Read(ctx context.Context) ([]byte, error) {
	if ctx == nil {
		ctx = context.Background()
	}
	t.readMu.Lock()
	defer t.readMu.Unlock()
	conn, err := t.connection()
	if err != nil {
		return nil, err
	}
	for {
		frame, err := bridgeprotocol.ReadFrame(ctx, conn)
		if err != nil {
			return nil, fmt.Errorf("read emulator transport: %w", err)
		}
		switch frame.MessageType {
		case bridgeprotocol.Data:
			return append([]byte(nil), frame.Payload...), nil
		case bridgeprotocol.Ping:
			if err := t.writeFrame(ctx, bridgeprotocol.Frame{MessageType: bridgeprotocol.Pong, CorrelationID: frame.CorrelationID}); err != nil {
				return nil, err
			}
		case bridgeprotocol.Error:
			return nil, mapError(frame.Payload)
		case bridgeprotocol.Close:
			return nil, ErrDisconnected
		default:
			return nil, fmt.Errorf("unexpected bridge frame: %d", frame.MessageType)
		}
	}
}

// Write encaminha um bloco de bytes sem interpretação semântica.
func (t *Transport) Write(data []byte) error {
	if len(data) == 0 {
		return fmt.Errorf("bridge payload is empty")
	}
	return t.writeFrame(context.Background(), bridgeprotocol.Frame{
		MessageType:   bridgeprotocol.Data,
		CorrelationID: t.currentCorrelationID("operation"),
		Payload:       append([]byte(nil), data...),
	})
}

// SetCorrelationID associa a operação ativa aos frames DATA subsequentes.
// O método existe para a fachada gomobile manter a mesma correlação no Go,
// Bridge e logger sem alterar o layout do envelope PBRG v1.
func (t *Transport) SetCorrelationID(operationID string) {
	t.mu.Lock()
	defer t.mu.Unlock()
	t.correlationID = operationID
}

// Ping verifica o ciclo de vida do socket sem enviar bytes ABECS ao serial.
func (t *Transport) Ping(ctx context.Context, correlationID string) error {
	if ctx == nil {
		ctx = context.Background()
	}
	if correlationID == "" {
		correlationID = t.currentCorrelationID("ping")
	}
	t.readMu.Lock()
	defer t.readMu.Unlock()
	conn, err := t.connection()
	if err != nil {
		conn, err = t.dialForPing(ctx, correlationID)
		if err != nil {
			return err
		}
		defer conn.Close()
	}
	if err := t.writeFrameOnConnection(ctx, conn, bridgeprotocol.Frame{MessageType: bridgeprotocol.Ping, CorrelationID: correlationID}); err != nil {
		return err
	}
	for {
		frame, err := bridgeprotocol.ReadFrame(ctx, conn)
		if err != nil {
			return fmt.Errorf("read bridge ping: %w", err)
		}
		switch frame.MessageType {
		case bridgeprotocol.Pong:
			if frame.CorrelationID == correlationID {
				return nil
			}
		case bridgeprotocol.Error:
			return mapError(frame.Payload)
		case bridgeprotocol.Close:
			return ErrDisconnected
		default:
			return fmt.Errorf("unexpected bridge ping response: %d", frame.MessageType)
		}
	}
}

// IsOpen informa se a sessão foi adquirida.
func (t *Transport) IsOpen() bool {
	t.mu.RLock()
	defer t.mu.RUnlock()
	return t.conn != nil
}

func (t *Transport) connection() (net.Conn, error) {
	t.mu.RLock()
	defer t.mu.RUnlock()
	if t.conn == nil {
		return nil, ErrDisconnected
	}
	return t.conn, nil
}

func (t *Transport) currentCorrelationID(fallback string) string {
	t.mu.RLock()
	defer t.mu.RUnlock()
	if t.correlationID != "" {
		return t.correlationID
	}
	return fallback
}

func (t *Transport) writeFrame(ctx context.Context, frame bridgeprotocol.Frame) error {
	conn, err := t.connection()
	if err != nil {
		return err
	}
	return t.writeFrameOnConnection(ctx, conn, frame)
}

func (t *Transport) writeFrameOnConnection(ctx context.Context, conn net.Conn, frame bridgeprotocol.Frame) error {
	t.writeMu.Lock()
	defer t.writeMu.Unlock()
	if err := bridgeprotocol.WriteFrame(ctx, conn, frame); err != nil {
		return fmt.Errorf("write emulator transport: %w", err)
	}
	return nil
}

func (t *Transport) dialForPing(ctx context.Context, correlationID string) (net.Conn, error) {
	t.mu.RLock()
	config := t.config
	t.mu.RUnlock()
	conn, err := config.Dial(ctx, "tcp", net.JoinHostPort(config.Host, strconv.Itoa(config.Port)))
	if err != nil {
		return nil, fmt.Errorf("dial bridge for ping: %w", err)
	}
	if err := bridgeprotocol.WriteFrame(ctx, conn, bridgeprotocol.Frame{
		MessageType:   bridgeprotocol.Hello,
		CorrelationID: correlationID,
	}); err != nil {
		_ = conn.Close()
		return nil, err
	}
	response, err := bridgeprotocol.ReadFrame(ctx, conn)
	if err != nil {
		_ = conn.Close()
		return nil, err
	}
	if response.MessageType == bridgeprotocol.Error {
		_ = conn.Close()
		return nil, mapError(response.Payload)
	}
	if response.MessageType != bridgeprotocol.HelloOK {
		_ = conn.Close()
		return nil, fmt.Errorf("unexpected bridge ping hello response")
	}
	return conn, nil
}

func mapError(payload []byte) error {
	message := string(payload)
	if len(message) >= len("BUSY:") && message[:len("BUSY:")] == "BUSY:" {
		return ErrBusy
	}
	if message == "" {
		return ErrDisconnected
	}
	return errors.New(message)
}
