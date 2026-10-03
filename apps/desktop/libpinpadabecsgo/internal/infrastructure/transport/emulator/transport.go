// Package emulator implementa o transporte TCP usado pelo Android Emulator.
package emulator

import (
	"context"
	"errors"
	"fmt"
	"io"
	"net"
	"strconv"
	"strings"
	"sync"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridgeprotocol"
)

var (
	// ErrDisconnected identifica o encerramento inesperado do Bridge.
	ErrDisconnected = errors.New("bridge disconnected")
	// ErrBusy identifica ownership já atribuído a outra sessão.
	ErrBusy = errors.New("bridge busy")
	// ErrUnreachable indica falha ao conectar antes de adquirir a COM.
	ErrUnreachable = errors.New("bridge unreachable")
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
	mu               sync.RWMutex
	openMu           contextLock
	writeMu          contextLock
	readMu           contextLock
	conn             net.Conn
	sessionContext   context.Context
	sessionCancel    context.CancelFunc
	generation       uint64
	openingCancel    context.CancelFunc
	openingConn      net.Conn
	closing          *closeAttempt
	config           Config
	correlationID    string
	operationContext context.Context
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
	ctx, cancel := context.WithTimeout(t.activeContext(), t.config.Timeout)
	defer cancel()
	t.mu.RLock()
	generation := t.generation
	t.mu.RUnlock()
	if err := t.openMu.lock(ctx); err != nil {
		return err
	}
	defer t.openMu.unlock()
	for {
		t.mu.Lock()
		if generation != t.generation {
			t.mu.Unlock()
			return ErrDisconnected
		}
		closing := t.closing
		if closing == nil {
			break
		}
		t.mu.Unlock()
		select {
		case <-ctx.Done():
			return ctx.Err()
		case <-closing.done:
		}
	}
	if t.conn != nil {
		t.mu.Unlock()
		return ctx.Err()
	}
	config := t.config
	ctx, cancelOpening := context.WithCancel(ctx)
	t.openingCancel = cancelOpening
	t.mu.Unlock()
	defer func() {
		cancelOpening()
		t.mu.Lock()
		t.openingCancel = nil
		t.openingConn = nil
		t.mu.Unlock()
	}()
	conn, err := config.Dial(ctx, "tcp", net.JoinHostPort(config.Host, strconv.Itoa(config.Port)))
	if err != nil {
		return fmt.Errorf("%w: %w", ErrUnreachable, err)
	}
	t.mu.Lock()
	if generation != t.generation || ctx.Err() != nil {
		t.mu.Unlock()
		_ = conn.Close()
		if err := ctx.Err(); err != nil {
			return err
		}
		return ErrDisconnected
	}
	t.openingConn = conn
	t.mu.Unlock()
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
		return fmt.Errorf("%w: unexpected bridge hello response", bridgeprotocol.ErrInvalidFrame)
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
		return fmt.Errorf("%w: unexpected bridge acquire response", bridgeprotocol.ErrInvalidFrame)
	}
	t.mu.Lock()
	if generation != t.generation || ctx.Err() != nil {
		t.mu.Unlock()
		_ = conn.Close()
		if err := ctx.Err(); err != nil {
			return err
		}
		return ErrDisconnected
	}
	t.conn = conn
	t.sessionContext, t.sessionCancel = context.WithCancel(context.Background())
	t.mu.Unlock()
	return nil
}

// Close libera a sessão e fecha o socket de forma idempotente.
func (t *Transport) Close() error {
	ctx, cancel := context.WithTimeout(context.Background(), t.config.Timeout)
	defer cancel()
	t.mu.Lock()
	if closing := t.closing; closing != nil {
		t.mu.Unlock()
		select {
		case <-closing.done:
			return closing.err
		case <-ctx.Done():
			return ctx.Err()
		}
	}
	t.generation++
	conn := t.conn
	openingCancel, openingConn := t.openingCancel, t.openingConn
	sessionCancel := t.sessionCancel
	closing := &closeAttempt{conn: conn, done: make(chan struct{})}
	t.closing = closing
	t.conn = nil
	t.sessionContext, t.sessionCancel = nil, nil
	t.mu.Unlock()
	if openingCancel != nil {
		openingCancel()
	}
	if openingConn != nil && openingConn != conn {
		_ = openingConn.Close()
	}
	if sessionCancel != nil {
		sessionCancel()
	}
	if conn != nil {
		// Acorda I/O anterior; somente Close possui o socket até receber o ACK.
		_ = conn.SetDeadline(time.Now())
		closing.err = t.closeConnection(ctx, conn)
	}
	t.mu.Lock()
	t.closing = nil
	close(closing.done)
	t.mu.Unlock()
	return closing.err
}

type closeAttempt struct {
	conn net.Conn
	done chan struct{}
	err  error
}

func (t *Transport) closeConnection(ctx context.Context, conn net.Conn) (result error) {
	defer func() { result = errors.Join(result, conn.Close()) }()
	if err := t.readMu.lock(ctx); err != nil {
		return err
	}
	defer t.readMu.unlock()
	if err := t.writeMu.lock(ctx); err != nil {
		return err
	}
	defer t.writeMu.unlock()
	if err := conn.SetDeadline(time.Time{}); err != nil {
		return err
	}
	writeErr := bridgeprotocol.WriteFrame(ctx, conn, bridgeprotocol.Frame{MessageType: bridgeprotocol.Release, CorrelationID: t.currentCorrelationID("close")})
	var ackErr error
	if writeErr == nil {
		frame, err := bridgeprotocol.ReadFrame(ctx, conn)
		ackErr = err
		if err == nil && frame.MessageType != bridgeprotocol.Close {
			ackErr = bridgeprotocol.ErrInvalidFrame
		}
		// Bridge v1 anterior pode fechar após RELEASE, sem ACK de controle.
		if errors.Is(ackErr, io.EOF) || errors.Is(ackErr, io.ErrUnexpectedEOF) {
			ackErr = nil
		}
	}
	return errors.Join(writeErr, ackErr)
}

// Read recebe um bloco de bytes ABECS, preservando sua ordem e conteúdo.
func (t *Transport) Read(ctx context.Context) ([]byte, error) {
	ctx, cancel, conn := t.ioContext(ctx)
	defer cancel()
	if err := t.readMu.lock(ctx); err != nil {
		return nil, err
	}
	defer t.readMu.unlock()
	if conn == nil {
		return nil, ErrDisconnected
	}
	for {
		frame, err := bridgeprotocol.ReadFrame(ctx, conn)
		if err == nil {
			err = ctx.Err()
		}
		if err != nil {
			t.invalidate(conn)
			return nil, fmt.Errorf("read emulator transport: %w", err)
		}
		switch frame.MessageType {
		case bridgeprotocol.Data:
			return append([]byte(nil), frame.Payload...), nil
		case bridgeprotocol.Ping:
			if err := t.writeFrameOnConnection(ctx, conn, bridgeprotocol.Frame{MessageType: bridgeprotocol.Pong, CorrelationID: frame.CorrelationID}); err != nil {
				return nil, err
			}
		case bridgeprotocol.Error:
			t.invalidate(conn)
			return nil, mapError(frame.Payload)
		case bridgeprotocol.Close:
			t.invalidate(conn)
			return nil, ErrDisconnected
		default:
			t.invalidate(conn)
			return nil, fmt.Errorf("%w: unexpected bridge data response", bridgeprotocol.ErrInvalidFrame)
		}
	}
}

// Write encaminha um bloco de bytes sem interpretação semântica.
func (t *Transport) Write(data []byte) error {
	if len(data) == 0 {
		return fmt.Errorf("bridge payload is empty")
	}
	ctx, cancel, conn := t.ioContext(t.activeContext())
	defer cancel()
	if conn == nil {
		return ErrDisconnected
	}
	return t.writeFrameOnConnection(ctx, conn, bridgeprotocol.Frame{
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

// SetOperationContext compartilha prazo/cancelamento da fachada com Open e Write.
// O chamador serializa operações e limpa o contexto ao concluir.
func (t *Transport) SetOperationContext(ctx context.Context) {
	t.mu.Lock()
	defer t.mu.Unlock()
	t.operationContext = ctx
}

func (t *Transport) activeContext() context.Context {
	t.mu.RLock()
	defer t.mu.RUnlock()
	if t.operationContext != nil {
		return t.operationContext
	}
	return context.Background()
}

func (t *Transport) invalidate(conn net.Conn) {
	t.mu.Lock()
	if t.closing != nil && t.closing.conn == conn {
		t.mu.Unlock()
		return
	}
	var cancel context.CancelFunc
	if t.conn == conn {
		t.conn = nil
		cancel = t.sessionCancel
		t.sessionContext, t.sessionCancel = nil, nil
	}
	t.mu.Unlock()
	if cancel != nil {
		cancel()
	}
	_ = conn.Close()
}

// ioContext inclui a espera pelos locks no prazo e interrompe a sessão anterior.
func (t *Transport) ioContext(parent context.Context) (context.Context, context.CancelFunc, net.Conn) {
	if parent == nil {
		parent = context.Background()
	}
	ctx, cancel := context.WithTimeout(parent, t.config.Timeout)
	t.mu.RLock()
	conn, session := t.conn, t.sessionContext
	t.mu.RUnlock()
	if session == nil {
		return ctx, cancel, conn
	}
	stop := context.AfterFunc(session, cancel)
	return ctx, func() { stop(); cancel() }, conn
}

// Ping verifica o ciclo de vida do socket sem enviar bytes ABECS ao serial.
func (t *Transport) Ping(ctx context.Context, correlationID string) (result error) {
	ctx, cancel, conn := t.ioContext(ctx)
	defer cancel()
	if correlationID == "" {
		correlationID = t.currentCorrelationID("ping")
	}
	if err := t.readMu.lock(ctx); err != nil {
		return err
	}
	defer t.readMu.unlock()
	if conn == nil {
		var err error
		conn, err = t.dialForPing(ctx, correlationID)
		if err != nil {
			return err
		}
		defer conn.Close()
	}
	defer func() {
		if result != nil {
			t.invalidate(conn)
		}
	}()
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
				return ctx.Err()
			}
		case bridgeprotocol.Error:
			return mapError(frame.Payload)
		case bridgeprotocol.Close:
			return ErrDisconnected
		default:
			return fmt.Errorf("%w: unexpected bridge ping response", bridgeprotocol.ErrInvalidFrame)
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
	if err := t.writeMu.lock(ctx); err != nil {
		return err
	}
	defer t.writeMu.unlock()
	err := bridgeprotocol.WriteFrame(ctx, conn, frame)
	if err == nil {
		err = ctx.Err()
	}
	if err != nil {
		t.invalidate(conn)
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
		return nil, fmt.Errorf("%w: %w", ErrUnreachable, err)
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
		return nil, fmt.Errorf("%w: unexpected bridge ping hello response", bridgeprotocol.ErrInvalidFrame)
	}
	return conn, nil
}

// RemoteError contém somente código/fase reconhecidos, nunca texto livre do peer.
type RemoteError struct{ Code, Phase string }

// Error devolve representação sanitizada para a fachada.
func (e *RemoteError) Error() string { return e.Code + ":" + e.Phase }

// Unwrap mantém as sentinelas de infraestrutura compatíveis.
func (e *RemoteError) Unwrap() error {
	if e.Code == "BUSY" {
		return ErrBusy
	}
	if e.Code == "DISCONNECTED" {
		return ErrDisconnected
	}
	return nil
}

func mapError(payload []byte) error {
	if len(payload) == 0 {
		return ErrDisconnected
	}
	parts := strings.SplitN(string(payload), ":", 2)
	code := parts[0]
	switch code {
	case "BUSY", "OWNERSHIP_ERROR", "SERIAL_UNAVAILABLE", "TIMEOUT", "CANCELED", "DISCONNECTED", "INVALID_FRAME":
	default:
		code = "BINDING_ERROR"
	}
	phase := "bridge"
	if len(parts) == 2 {
		candidate := strings.TrimSuffix(parts[1], " failed")
		switch candidate {
		case "hello", "acquire", "ownership", "serial_open", "serial_read", "serial_write", "session", "ping", "receive", "forward":
			phase = candidate
		}
	}
	return &RemoteError{Code: code, Phase: phase}
}
