package bridge

import (
	"context"
	"errors"
	"net"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridgeprotocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/ownership"
)

type controlledTransport struct {
	fakeTransport
	openErr, writeErr, closeErr error
	readFailure                 chan error
}

func (f *controlledTransport) Open() error {
	if f.openErr != nil {
		return f.openErr
	}
	return f.fakeTransport.Open()
}
func (f *controlledTransport) Close() error { _ = f.fakeTransport.Close(); return f.closeErr }
func (f *controlledTransport) Write(data []byte) error {
	if f.writeErr != nil {
		return f.writeErr
	}
	return f.fakeTransport.Write(data)
}
func (f *controlledTransport) Read(ctx context.Context) ([]byte, error) {
	select {
	case <-ctx.Done():
		return nil, ctx.Err()
	case err := <-f.readFailure:
		return nil, err
	}
}

type controlledOwner struct {
	err      error
	releases int
}

func (o *controlledOwner) Acquire(context.Context, string) (func() error, error) {
	if o.err != nil {
		return nil, o.err
	}
	return func() error { o.releases++; return nil }, nil
}

func newRegressionServer(t *testing.T, transport *controlledTransport, owner *controlledOwner) (*Server, string) {
	t.Helper()
	tracer := logging.NewTracer()
	path := filepath.Join(t.TempDir(), "LogPinpadAbecs.txt")
	if _, err := tracer.SetLogDestination(path); err != nil {
		t.Fatal(err)
	}
	t.Cleanup(func() { _ = tracer.Close() })
	s, err := New(Config{Port: 39100, SerialName: "COM-REGRESSION", Transport: transport, Ownership: owner, Tracer: tracer})
	if err != nil {
		t.Fatal(err)
	}
	return s, path
}

func connectRegression(t *testing.T, s *Server, message bridgeprotocol.MessageType) (net.Conn, <-chan error, bridgeprotocol.Frame) {
	t.Helper()
	client, server := net.Pipe()
	t.Cleanup(func() { _ = client.Close() })
	done := make(chan error, 1)
	go func() { done <- s.handle(context.Background(), server) }()
	ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
	defer cancel()
	if err := bridgeprotocol.WriteFrame(ctx, client, bridgeprotocol.Frame{MessageType: bridgeprotocol.Hello, CorrelationID: "op-072"}); err != nil {
		t.Fatal(err)
	}
	if response, err := bridgeprotocol.ReadFrame(ctx, client); err != nil || response.MessageType != bridgeprotocol.HelloOK {
		t.Fatalf("hello: %v %v", response, err)
	}
	if err := bridgeprotocol.WriteFrame(ctx, client, bridgeprotocol.Frame{MessageType: message, CorrelationID: "op-072"}); err != nil {
		t.Fatal(err)
	}
	response, err := bridgeprotocol.ReadFrame(ctx, client)
	if err != nil {
		t.Fatal(err)
	}
	return client, done, response
}

func TestPingPersistsWhileProcessAliveWithoutOpeningSerial(t *testing.T) {
	transport := &controlledTransport{}
	s, path := newRegressionServer(t, transport, &controlledOwner{})
	_, done, response := connectRegression(t, s, bridgeprotocol.Ping)
	if response.MessageType != bridgeprotocol.Pong {
		t.Fatal(response)
	}
	if err := <-done; err != nil {
		t.Fatal(err)
	}
	if transport.IsOpen() {
		t.Fatal("Ping acquired serial")
	}
	contents, err := os.ReadFile(path)
	if err != nil || !strings.Contains(string(contents), `event="ping_ok"`) {
		t.Fatalf("log not persisted: %s %v", contents, err)
	}
	if strings.Contains(string(contents), "open(") {
		t.Fatal("fabricated serial open")
	}
}

func TestAcquireFailureKeepsCorrelationAndFeedsSharedLog(t *testing.T) {
	for _, tc := range []struct {
		name, code, event string
		ownerErr, openErr error
	}{
		{"busy", "BUSY", "ownership_failed", ownership.ErrBusy, nil},
		{"lock", "OWNERSHIP_ERROR", "ownership_failed", errors.New("PRIVATE_SYNTHETIC"), nil},
		{"driver", "SERIAL_UNAVAILABLE", "serial_open_failed", nil, errors.New("PRIVATE_SYNTHETIC")},
	} {
		t.Run(tc.name, func(t *testing.T) {
			s, path := newRegressionServer(t, &controlledTransport{openErr: tc.openErr}, &controlledOwner{err: tc.ownerErr})
			_, done, response := connectRegression(t, s, bridgeprotocol.Acquire)
			if response.MessageType != bridgeprotocol.Error || response.CorrelationID != "op-072" || !strings.HasPrefix(string(response.Payload), tc.code+":") {
				t.Fatal(response)
			}
			if <-done == nil {
				t.Fatal("lost original cause")
			}
			contents, _ := os.ReadFile(path)
			if !strings.Contains(string(contents), tc.event) || strings.Contains(string(contents), "PRIVATE_SYNTHETIC") || strings.Contains(string(response.Payload), "PRIVATE_SYNTHETIC") {
				t.Fatalf("unsafe/missing event: %s", contents)
			}
		})
	}
}

func TestFatalSerialReadNotifiesIdlePeerAndAllowsReopen(t *testing.T) {
	transport := &controlledTransport{readFailure: make(chan error, 1)}
	owner := &controlledOwner{}
	s, _ := newRegressionServer(t, transport, owner)
	client, done, response := connectRegression(t, s, bridgeprotocol.Acquire)
	if response.MessageType != bridgeprotocol.AcquireOK {
		t.Fatal(response)
	}
	transport.readFailure <- errors.New("read failed")
	ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
	defer cancel()
	response, err := bridgeprotocol.ReadFrame(ctx, client)
	if err != nil || response.MessageType != bridgeprotocol.Error || !strings.HasPrefix(string(response.Payload), "SERIAL_UNAVAILABLE:") {
		t.Fatalf("idle error: %v %v", response, err)
	}
	if <-done == nil {
		t.Fatal("read failure lost")
	}
	if transport.IsOpen() || owner.releases != 1 {
		t.Fatal("cleanup incomplete")
	}
	client, done, response = connectRegression(t, s, bridgeprotocol.Acquire)
	if response.MessageType != bridgeprotocol.AcquireOK {
		t.Fatal(response)
	}
	if err := bridgeprotocol.WriteFrame(ctx, client, bridgeprotocol.Frame{MessageType: bridgeprotocol.Release}); err != nil {
		t.Fatal(err)
	}
	if ack, err := bridgeprotocol.ReadFrame(ctx, client); err != nil || ack.MessageType != bridgeprotocol.Close {
		t.Fatalf("close ack: %v %v", ack, err)
	}
	if err := <-done; err != nil {
		t.Fatal(err)
	}
	if owner.releases != 2 {
		t.Fatal("reopen did not release")
	}
}

func TestInvalidSessionAndWriteErrorAreReported(t *testing.T) {
	for _, tc := range []struct {
		name     string
		message  bridgeprotocol.MessageType
		writeErr error
		code     string
	}{
		{"unexpected", bridgeprotocol.Hello, nil, "INVALID_FRAME"},
		{"write", bridgeprotocol.Data, errors.New("write failed"), "SERIAL_UNAVAILABLE"},
	} {
		t.Run(tc.name, func(t *testing.T) {
			s, _ := newRegressionServer(t, &controlledTransport{writeErr: tc.writeErr}, &controlledOwner{})
			client, done, _ := connectRegression(t, s, bridgeprotocol.Acquire)
			ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
			defer cancel()
			if err := bridgeprotocol.WriteFrame(ctx, client, bridgeprotocol.Frame{MessageType: tc.message, Payload: []byte{1}, CorrelationID: "action-072"}); err != nil {
				t.Fatal(err)
			}
			response, err := bridgeprotocol.ReadFrame(ctx, client)
			if err != nil || response.CorrelationID != "action-072" || !strings.HasPrefix(string(response.Payload), tc.code+":") {
				t.Fatalf("%v %v", response, err)
			}
			if <-done == nil {
				t.Fatal("lost failure")
			}
		})
	}
}

func TestNewRejectsInvalidConfiguration(t *testing.T) {
	for _, tc := range []struct {
		name string
		cfg  Config
	}{
		{"bind", Config{Bind: "0.0.0.0", Port: 39100}}, {"port", Config{Port: 0}}, {"dependencies", Config{Port: 39100}},
	} {
		t.Run(tc.name, func(t *testing.T) {
			if _, err := New(tc.cfg); err == nil {
				t.Fatal("invalid composition accepted")
			}
		})
	}
}

func TestCauseClassificationNeverEchoesError(t *testing.T) {
	for _, err := range []error{nil, context.Canceled, context.DeadlineExceeded, ownership.ErrBusy, bridgeprotocol.ErrInvalidFrame, errors.New("SECRET")} {
		if strings.Contains(causeCode(err), "SECRET") {
			t.Fatal("cause leaked")
		}
	}
}
