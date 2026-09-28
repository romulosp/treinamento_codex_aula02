package bridge

import (
	"context"
	"net"
	"sync"
	"testing"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridgeprotocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/ownership"
)

type fakeTransport struct {
	mu     sync.Mutex
	open   bool
	writes [][]byte
}

func (f *fakeTransport) Open() error  { f.mu.Lock(); defer f.mu.Unlock(); f.open = true; return nil }
func (f *fakeTransport) Close() error { f.mu.Lock(); defer f.mu.Unlock(); f.open = false; return nil }
func (f *fakeTransport) IsOpen() bool { f.mu.Lock(); defer f.mu.Unlock(); return f.open }
func (f *fakeTransport) Read(ctx context.Context) ([]byte, error) {
	timer := time.NewTimer(time.Millisecond)
	defer timer.Stop()
	select {
	case <-ctx.Done():
		return nil, ctx.Err()
	case <-timer.C:
		return nil, nil
	}
}
func (f *fakeTransport) Write(data []byte) error {
	f.mu.Lock()
	defer f.mu.Unlock()
	f.writes = append(f.writes, append([]byte(nil), data...))
	return nil
}

func TestBridgeForwardsDataToFakeSerial(t *testing.T) {
	serial := &fakeTransport{}
	server, err := New(Config{Bind: "127.0.0.1", Port: 39100, SerialName: "COM-TEST", Transport: serial, Ownership: ownership.New()})
	if err != nil {
		t.Fatal(err)
	}
	client, peer := net.Pipe()
	defer client.Close()
	defer peer.Close()
	done := make(chan error, 1)
	go func() { done <- server.handle(context.Background(), peer) }()
	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()
	if err := bridgeprotocol.WriteFrame(ctx, client, bridgeprotocol.Frame{MessageType: bridgeprotocol.Hello}); err != nil {
		t.Fatal(err)
	}
	if frame, err := bridgeprotocol.ReadFrame(ctx, client); err != nil || frame.MessageType != bridgeprotocol.HelloOK {
		t.Fatalf("hello response = %#v, err=%v", frame, err)
	}
	if err := bridgeprotocol.WriteFrame(ctx, client, bridgeprotocol.Frame{MessageType: bridgeprotocol.Acquire}); err != nil {
		t.Fatal(err)
	}
	if frame, err := bridgeprotocol.ReadFrame(ctx, client); err != nil || frame.MessageType != bridgeprotocol.AcquireOK {
		t.Fatalf("acquire response = %#v, err=%v", frame, err)
	}
	if err := bridgeprotocol.WriteFrame(ctx, client, bridgeprotocol.Frame{MessageType: bridgeprotocol.Data, Payload: []byte{0x16, 0x47, 0x49, 0x58}}); err != nil {
		t.Fatal(err)
	}
	if err := bridgeprotocol.WriteFrame(ctx, client, bridgeprotocol.Frame{MessageType: bridgeprotocol.Release}); err != nil {
		t.Fatal(err)
	}
	if err := <-done; err != nil {
		t.Fatal(err)
	}
	serial.mu.Lock()
	defer serial.mu.Unlock()
	if len(serial.writes) != 1 || string(serial.writes[0]) != string([]byte{0x16, 0x47, 0x49, 0x58}) {
		t.Fatalf("writes = %v", serial.writes)
	}
}
