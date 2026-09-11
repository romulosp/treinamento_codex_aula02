// Package serial implementa o adaptador ABECS para a porta física e o fake
// determinístico usado pelos testes sem pinpad.
package serial

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"context"
	"fmt"
	bugserial "go.bug.st/serial"
	"sync"
	"time"
)

// SerialPort abstrai leitura cancelável, escrita e ciclo de vida da porta.
type SerialPort interface {
	Open() error
	Close() error
	Read(context.Context) ([]byte, error)
	Write([]byte) error
	IsOpen() bool
}

var _ SerialPort = (*Adapter)(nil)

// Adapter conecta uma porta física go.bug.st/serial ao contrato do domínio.
type Adapter struct {
	mu      sync.RWMutex
	name    string
	baud    int
	timeout time.Duration
	port    bugserial.Port
	tracer  *logging.Tracer
	command command.Type
}

// New cria um adaptador serial ainda fechado para a porta e baud rate informados.
func New(name string, baud int, timeout time.Duration) *Adapter {
	return &Adapter{name: name, baud: baud, timeout: timeout, tracer: logging.NewTracer()}
}

// SetTracer associa o rastro compartilhado à porta. Deve ser chamado antes de
// Open; valor nil desabilita o rastro neste adaptador.
func (a *Adapter) SetTracer(tracer *logging.Tracer) {
	a.mu.Lock()
	defer a.mu.Unlock()
	if tracer == nil {
		tracer = logging.NewTracer()
	}
	a.tracer = tracer
}

// SetTraceCommand informa o comando cuja resposta será lida em seguida. Esse
// metadado é usado exclusivamente para redação do rastro, sem alterar bytes.
func (a *Adapter) SetTraceCommand(kind command.Type) {
	a.mu.Lock()
	defer a.mu.Unlock()
	a.command = kind
}

func (a *Adapter) Open() error {
	a.mu.Lock()
	defer a.mu.Unlock()
	if a.port != nil {
		return nil
	}
	p, err := bugserial.Open(a.name, &bugserial.Mode{BaudRate: a.baud, DataBits: 8, Parity: bugserial.NoParity, StopBits: bugserial.OneStopBit})
	if err != nil {
		a.tracer.RecordOpenFailure(a.name, a.baud, err)
		return fmt.Errorf("open serial port: %w", err)
	}
	readTimeout := a.timeout
	if readTimeout > 100*time.Millisecond {
		readTimeout = 100 * time.Millisecond
	}
	if err := p.SetReadTimeout(readTimeout); err != nil {
		_ = p.Close()
		a.tracer.RecordOpenFailure(a.name, a.baud, err)
		return fmt.Errorf("set serial timeout: %w", err)
	}
	a.port = p
	a.tracer.RecordOpen(a.name, a.baud)
	return nil
}
func (a *Adapter) Close() error {
	a.mu.Lock()
	defer a.mu.Unlock()
	if a.port == nil {
		return nil
	}
	err := a.port.Close()
	a.port = nil
	a.tracer.RecordClose(err)
	if err != nil {
		return fmt.Errorf("close serial port: %w", err)
	}
	return nil
}

// Read waits por bytes da serial respeitando o cancelamento do contexto. A
// porta é configurada com timeout curto para que o loop possa observar ctx sem
// criar goroutine bloqueada; o timeout operacional continua sendo imposto pelo
// contexto recebido da camada de aplicação.
func (a *Adapter) Read(ctx context.Context) ([]byte, error) {
	if ctx == nil {
		ctx = context.Background()
	}
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	a.mu.RLock()
	p := a.port
	tracer := a.tracer
	kind := a.command
	a.mu.RUnlock()
	if p == nil {
		err := fmt.Errorf("serial port is closed")
		tracer.RecordError(a.name, "read", err)
		return nil, err
	}
	dst := make([]byte, 256)
	for {
		if err := ctx.Err(); err != nil {
			return nil, err
		}
		n, err := p.Read(dst)
		if err != nil {
			if ctx.Err() != nil {
				return nil, ctx.Err()
			}
			wrapped := fmt.Errorf("read serial port: %w", err)
			tracer.RecordError(a.name, "read", wrapped)
			return nil, wrapped
		}
		if n > 0 {
			result := append([]byte(nil), dst[:n]...)
			tracer.RecordPPFrom(kind, result, "serial.Adapter.Read")
			return result, nil
		}
		if err := ctx.Err(); err != nil {
			return nil, err
		}
		if !a.IsOpen() {
			return nil, domainerror.ErrPortUnavailable
		}
	}
}
func (a *Adapter) Write(data []byte) error {
	a.mu.RLock()
	p := a.port
	tracer := a.tracer
	kind := a.command
	a.mu.RUnlock()
	if p == nil {
		err := fmt.Errorf("serial port is closed")
		tracer.RecordError(a.name, "write", err)
		return err
	}
	n, err := p.Write(data)
	if err != nil {
		wrapped := fmt.Errorf("write serial port: %w", err)
		tracer.RecordError(a.name, "write", wrapped)
		return wrapped
	}
	if n != len(data) {
		err := fmt.Errorf("short serial write: %d/%d", n, len(data))
		tracer.RecordError(a.name, "write", err)
		return err
	}
	tracer.RecordSPEFrom(kind, data, "serial.Adapter.Write")
	return nil
}
func (a *Adapter) IsOpen() bool { a.mu.RLock(); defer a.mu.RUnlock(); return a.port != nil }

// FakePort implementa a porta serial de forma determinística para testes.
type FakePort struct {
	mu                               sync.Mutex
	OpenError, ReadError, WriteError error
	Reads                            [][]byte
	Writes                           [][]byte
	open                             bool
}

// NewFakePort cria transporte determinístico para testes sem hardware físico.
func NewFakePort(reads ...[]byte) *FakePort { return &FakePort{Reads: reads} }
func (f *FakePort) Open() error {
	f.mu.Lock()
	defer f.mu.Unlock()
	if f.OpenError != nil {
		return f.OpenError
	}
	f.open = true
	return nil
}
func (f *FakePort) Close() error { f.mu.Lock(); defer f.mu.Unlock(); f.open = false; return nil }
func (f *FakePort) IsOpen() bool { f.mu.Lock(); defer f.mu.Unlock(); return f.open }
func (f *FakePort) Read(ctx context.Context) ([]byte, error) {
	if ctx == nil {
		ctx = context.Background()
	}
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	f.mu.Lock()
	defer f.mu.Unlock()
	if f.ReadError != nil {
		return nil, f.ReadError
	}
	if len(f.Reads) == 0 {
		return nil, nil
	}
	value := append([]byte(nil), f.Reads[0]...)
	f.Reads = f.Reads[1:]
	return value, nil
}
func (f *FakePort) Write(data []byte) error {
	f.mu.Lock()
	defer f.mu.Unlock()
	if f.WriteError != nil {
		return f.WriteError
	}
	f.Writes = append(f.Writes, append([]byte(nil), data...))
	return nil
}
