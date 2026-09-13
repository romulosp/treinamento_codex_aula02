// Package serial implementa o adaptador ABECS para a porta física e o fake
// determinístico usado pelos testes sem pinpad.
package serial

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"context"
	"errors"
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
	redact  bool
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
	a.redact = false
}

// SetTracePolicy informa o comando ativo e se todo o seu frame deve ser
// redigido por estar sob sessão segura ou conter dados sensíveis do consumidor.
func (a *Adapter) SetTracePolicy(kind command.Type, redact bool) {
	a.mu.Lock()
	defer a.mu.Unlock()
	a.command = kind
	a.redact = redact
}

// Open abre a porta física em 8N1 e ativa um timeout curto de leitura cancelável.
func (a *Adapter) Open() error {
	a.mu.Lock()
	defer a.mu.Unlock()
	if a.port != nil {
		return nil
	}
	p, err := bugserial.Open(a.name, &bugserial.Mode{BaudRate: a.baud, DataBits: 8, Parity: bugserial.NoParity, StopBits: bugserial.OneStopBit})
	if err != nil {
		wrapped := fmt.Errorf("open serial port: %w", err)
		return errors.Join(wrapped, a.tracer.RecordOpenFailure(a.name, a.baud, wrapped))
	}
	readTimeout := a.timeout
	if readTimeout > 100*time.Millisecond {
		readTimeout = 100 * time.Millisecond
	}
	if err := p.SetReadTimeout(readTimeout); err != nil {
		wrapped := fmt.Errorf("set serial timeout: %w", err)
		return errors.Join(wrapped, p.Close(), a.tracer.RecordOpenFailure(a.name, a.baud, wrapped))
	}
	a.port = p
	if traceErr := a.tracer.RecordOpen(a.name, a.baud); traceErr != nil {
		a.port = nil
		return errors.Join(fmt.Errorf("record serial open trace: %w", traceErr), p.Close())
	}
	return nil
}

// Close fecha a porta física e registra o encerramento no tracer configurado.
func (a *Adapter) Close() error {
	a.mu.Lock()
	defer a.mu.Unlock()
	if a.port == nil {
		return nil
	}
	err := a.port.Close()
	a.port = nil
	traceErr := a.tracer.RecordClose(err)
	return errors.Join(wrapSerialError("close serial port", err), wrapSerialError("record serial close trace", traceErr))
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
	redact := a.redact
	a.mu.RUnlock()
	if p == nil {
		err := fmt.Errorf("serial port is closed")
		return nil, errors.Join(err, tracer.RecordError(a.name, "read", err))
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
			return nil, errors.Join(wrapped, tracer.RecordError(a.name, "read", wrapped))
		}
		if n > 0 {
			result := append([]byte(nil), dst[:n]...)
			if traceErr := tracer.RecordPPFromPolicy(kind, result, "serial.Adapter.Read", redact); traceErr != nil {
				return nil, fmt.Errorf("record serial read trace: %w", traceErr)
			}
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
	redact := a.redact
	a.mu.RUnlock()
	if p == nil {
		err := fmt.Errorf("serial port is closed")
		return errors.Join(err, tracer.RecordError(a.name, "write", err))
	}
	n, err := p.Write(data)
	if err != nil {
		wrapped := fmt.Errorf("write serial port: %w", err)
		return errors.Join(wrapped, tracer.RecordError(a.name, "write", wrapped))
	}
	if n != len(data) {
		err := fmt.Errorf("short serial write: %d/%d", n, len(data))
		return errors.Join(err, tracer.RecordError(a.name, "write", err))
	}
	if traceErr := tracer.RecordSPEFromPolicy(kind, data, "serial.Adapter.Write", redact); traceErr != nil {
		return fmt.Errorf("record serial write trace: %w", traceErr)
	}
	return nil
}

// IsOpen informa se o adaptador mantém uma porta física aberta.
func (a *Adapter) IsOpen() bool { a.mu.RLock(); defer a.mu.RUnlock(); return a.port != nil }

func wrapSerialError(operation string, err error) error {
	if err == nil {
		return nil
	}
	return fmt.Errorf("%s: %w", operation, err)
}

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

// Open marca o transporte fake como aberto ou devolve OpenError.
func (f *FakePort) Open() error {
	f.mu.Lock()
	defer f.mu.Unlock()
	if f.OpenError != nil {
		return f.OpenError
	}
	f.open = true
	return nil
}

// Close marca o transporte fake como fechado.
func (f *FakePort) Close() error { f.mu.Lock(); defer f.mu.Unlock(); f.open = false; return nil }

// IsOpen informa o estado atual do transporte fake.
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
