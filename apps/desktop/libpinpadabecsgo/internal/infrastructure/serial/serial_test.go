package serial

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"context"
	"errors"
	"io"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"

	bugserial "go.bug.st/serial"
)

type testPort struct {
	reads        [][]byte
	readErr      error
	writeErr     error
	closeErr     error
	written      [][]byte
	partialWrite bool
}

func TestAdapterRecordsRedactedPinpadReadsAndClose(t *testing.T) {
	path := filepath.Join(t.TempDir(), "trace.log")
	tracer := logging.NewTracer()
	if active, err := tracer.SetLogDestination(path); err != nil || !active {
		t.Fatalf("SetLogDestination active=%t err=%v", active, err)
	}
	defer func() {
		if err := tracer.Close(); err != nil {
			t.Fatal(err)
		}
	}()

	adapter := New("COM1", 19200, time.Second)
	adapter.SetTracer(tracer)
	adapter.port = &testPort{reads: [][]byte{{0x12, 0x34, 0x56}}}
	tracer.RecordOpen("COM1", 19200)
	adapter.SetTraceCommand(command.CommandGPN)
	if err := adapter.Write([]byte{0xAB, 0xCD, 0xEF}); err != nil {
		t.Fatal(err)
	}
	if _, err := adapter.Read(context.Background()); err != nil {
		t.Fatal(err)
	}
	if err := adapter.Close(); err != nil {
		t.Fatal(err)
	}

	content, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	text := string(content)
	if strings.Contains(text, "12 34 56") || strings.Contains(text, "AB CD EF") || strings.Count(text, "**REDACTED(3 bytes)**") != 2 {
		t.Fatalf("unexpected trace: %q", text)
	}
	for _, expected := range []string{"FUNC=serial.Adapter.Write", "FUNC=serial.Adapter.Read", "FUNC=serial.Adapter.Close", "DATA_HORA="} {
		if !strings.Contains(text, expected) {
			t.Fatalf("trace does not contain %q: %q", expected, text)
		}
	}
}

func (p *testPort) SetMode(*bugserial.Mode) error                           { return nil }
func (p *testPort) Drain() error                                            { return nil }
func (p *testPort) ResetInputBuffer() error                                 { return nil }
func (p *testPort) ResetOutputBuffer() error                                { return nil }
func (p *testPort) SetDTR(bool) error                                       { return nil }
func (p *testPort) SetRTS(bool) error                                       { return nil }
func (p *testPort) GetModemStatusBits() (*bugserial.ModemStatusBits, error) { return nil, nil }
func (p *testPort) SetReadTimeout(time.Duration) error                      { return nil }
func (p *testPort) Break(time.Duration) error                               { return nil }
func (p *testPort) Close() error                                            { return p.closeErr }

func (p *testPort) Read(dst []byte) (int, error) {
	if p.readErr != nil {
		return 0, p.readErr
	}
	if len(p.reads) == 0 {
		return 0, nil
	}
	chunk := p.reads[0]
	p.reads = p.reads[1:]
	copy(dst, chunk)
	return len(chunk), nil
}

func (p *testPort) Write(data []byte) (int, error) {
	if p.writeErr != nil {
		return 0, p.writeErr
	}
	p.written = append(p.written, append([]byte(nil), data...))
	if p.partialWrite {
		return len(data) - 1, nil
	}
	return len(data), nil
}

func TestFakePortLifecycleAndIO(t *testing.T) {
	f := NewFakePort([]byte{0x06})
	if err := f.Open(); err != nil || !f.IsOpen() {
		t.Fatal("fake port did not open")
	}
	if err := f.Write([]byte("OPN000")); err != nil {
		t.Fatal(err)
	}
	got, err := f.Read(context.Background())
	if err != nil || len(got) != 1 || got[0] != 0x06 {
		t.Fatalf("read = %v %v", got, err)
	}
	if err := f.Close(); err != nil || f.IsOpen() {
		t.Fatal("fake port did not close")
	}
}

func TestAdapterRejectsIOWhenClosedAndHandlesOpenFailure(t *testing.T) {
	adapter := New("COM99999", 19200, time.Second)
	if adapter.IsOpen() {
		t.Fatal("new adapter must be closed")
	}
	if _, err := adapter.Read(context.Background()); err == nil {
		t.Fatal("expected read error for closed adapter")
	}
	if err := adapter.Write([]byte("x")); err == nil {
		t.Fatal("expected write error for closed adapter")
	}
	if err := adapter.Close(); err != nil {
		t.Fatalf("closing unopened adapter: %v", err)
	}
	if err := adapter.Open(); err == nil {
		t.Fatal("expected unavailable COM port error")
	}
}

func TestFakePortErrors(t *testing.T) {
	f := NewFakePort()
	f.ReadError = errors.New("read")
	if _, err := f.Read(context.Background()); err == nil {
		t.Fatal("expected read error")
	}
	f.WriteError = errors.New("write")
	if err := f.Write(nil); err == nil {
		t.Fatal("expected write error")
	}
}

func TestFakePortHonorsCanceledContext(t *testing.T) {
	ctx, cancel := context.WithCancel(context.Background())
	cancel()
	if _, err := NewFakePort().Read(ctx); err != context.Canceled {
		t.Fatalf("read error = %v", err)
	}
}

func TestAdapterUsesInstalledPortForReadWriteAndClose(t *testing.T) {
	port := &testPort{reads: [][]byte{nil, []byte("ACK")}}
	adapter := New("COM1", 19200, time.Second)
	adapter.port = port

	if err := adapter.Open(); err != nil {
		t.Fatalf("idempotent Open = %v", err)
	}
	got, err := adapter.Read(context.Background())
	if err != nil || string(got) != "ACK" {
		t.Fatalf("Read = %q, %v", got, err)
	}
	if err := adapter.Write([]byte("OPN000")); err != nil {
		t.Fatalf("Write = %v", err)
	}
	if len(port.written) != 1 || string(port.written[0]) != "OPN000" {
		t.Fatalf("written = %#v", port.written)
	}
	if err := adapter.Close(); err != nil || adapter.IsOpen() {
		t.Fatalf("Close = %v, open=%v", err, adapter.IsOpen())
	}
}

func TestAdapterReportsPortFailures(t *testing.T) {
	tests := []struct {
		name string
		port *testPort
		run  func(*Adapter) error
	}{
		{name: "read error", port: &testPort{readErr: io.ErrUnexpectedEOF}, run: func(a *Adapter) error { _, err := a.Read(context.Background()); return err }},
		{name: "write error", port: &testPort{writeErr: io.ErrClosedPipe}, run: func(a *Adapter) error { return a.Write([]byte("x")) }},
		{name: "short write", port: &testPort{partialWrite: true}, run: func(a *Adapter) error { return a.Write([]byte("xy")) }},
		{name: "close error", port: &testPort{closeErr: io.ErrClosedPipe}, run: func(a *Adapter) error { return a.Close() }},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			adapter := New("COM1", 19200, time.Second)
			adapter.port = test.port
			if err := test.run(adapter); err == nil {
				t.Fatal("expected error")
			}
		})
	}
}
