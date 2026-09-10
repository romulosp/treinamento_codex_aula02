package serial

import (
	"fmt"
	bugserial "go.bug.st/serial"
	"sync"
	"time"
)

type SerialPort interface {
	Open() error
	Close() error
	Read() ([]byte, error)
	Write([]byte) error
	IsOpen() bool
}

var _ SerialPort = (*Adapter)(nil)

type Adapter struct {
	mu      sync.RWMutex
	name    string
	baud    int
	timeout time.Duration
	port    bugserial.Port
}

func New(name string, baud int, timeout time.Duration) *Adapter {
	return &Adapter{name: name, baud: baud, timeout: timeout}
}
func (a *Adapter) Open() error {
	a.mu.Lock()
	defer a.mu.Unlock()
	if a.port != nil {
		return nil
	}
	p, err := bugserial.Open(a.name, &bugserial.Mode{BaudRate: a.baud, DataBits: 8, Parity: bugserial.NoParity, StopBits: bugserial.OneStopBit})
	if err != nil {
		return fmt.Errorf("open serial port: %w", err)
	}
	if err := p.SetReadTimeout(a.timeout); err != nil {
		_ = p.Close()
		return fmt.Errorf("set serial timeout: %w", err)
	}
	a.port = p
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
	if err != nil {
		return fmt.Errorf("close serial port: %w", err)
	}
	return nil
}
func (a *Adapter) Read() ([]byte, error) {
	a.mu.RLock()
	defer a.mu.RUnlock()
	if a.port == nil {
		return nil, fmt.Errorf("serial port is closed")
	}
	dst := make([]byte, 256)
	n, err := a.port.Read(dst)
	return dst[:n], err
}
func (a *Adapter) Write(data []byte) error {
	a.mu.RLock()
	defer a.mu.RUnlock()
	if a.port == nil {
		return fmt.Errorf("serial port is closed")
	}
	n, err := a.port.Write(data)
	if err != nil {
		return fmt.Errorf("write serial port: %w", err)
	}
	if n != len(data) {
		return fmt.Errorf("short serial write: %d/%d", n, len(data))
	}
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
func (f *FakePort) Read() ([]byte, error) {
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
