package emulator

import (
	"context"
	"net"
	"testing"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridgeprotocol"
)

func TestTransportPreservesDataThroughPersistentSession(t *testing.T) {
	client, server := net.Pipe()
	dial := func(context.Context, string, string) (net.Conn, error) { return client, nil }
	transport, err := New(Config{Host: "localhost", Port: 39100, Timeout: time.Second, Dial: dial})
	if err != nil {
		t.Fatal(err)
	}
	serverDone := make(chan error, 1)
	go func() {
		ctx := context.Background()
		frame, err := bridgeprotocol.ReadFrame(ctx, server)
		if err != nil || frame.MessageType != bridgeprotocol.Hello || frame.CorrelationID != "operation-1" {
			serverDone <- err
			return
		}
		if err = bridgeprotocol.WriteFrame(ctx, server, bridgeprotocol.Frame{MessageType: bridgeprotocol.HelloOK}); err != nil {
			serverDone <- err
			return
		}
		if frame, err = bridgeprotocol.ReadFrame(ctx, server); err != nil || frame.MessageType != bridgeprotocol.Acquire || frame.CorrelationID != "operation-1" {
			serverDone <- err
			return
		}
		if err = bridgeprotocol.WriteFrame(ctx, server, bridgeprotocol.Frame{MessageType: bridgeprotocol.AcquireOK}); err != nil {
			serverDone <- err
			return
		}
		if frame, err = bridgeprotocol.ReadFrame(ctx, server); err != nil || frame.MessageType != bridgeprotocol.Data || frame.CorrelationID != "operation-1" {
			serverDone <- err
			return
		}
		if string(frame.Payload) != "ABECS" {
			serverDone <- err
			return
		}
		serverDone <- bridgeprotocol.WriteFrame(ctx, server, bridgeprotocol.Frame{MessageType: bridgeprotocol.Data, Payload: []byte("REPLY")})
	}()
	transport.SetCorrelationID("operation-1")
	if err := transport.Open(); err != nil {
		t.Fatal(err)
	}
	if err := transport.Write([]byte("ABECS")); err != nil {
		t.Fatal(err)
	}
	got, err := transport.Read(context.Background())
	if err != nil || string(got) != "REPLY" {
		t.Fatalf("read = %q, err = %v", got, err)
	}
	if err := <-serverDone; err != nil {
		t.Fatal(err)
	}
	_ = transport.Close()
}

func TestTransportPingUsesCorrelationWithoutSerialData(t *testing.T) {
	client, server := net.Pipe()
	defer client.Close()
	defer server.Close()
	dial := func(context.Context, string, string) (net.Conn, error) { return client, nil }
	transport, err := New(Config{Host: "localhost", Port: 39100, Timeout: time.Second, Dial: dial})
	if err != nil {
		t.Fatal(err)
	}
	go func() {
		ctx := context.Background()
		for range 3 {
			frame, readErr := bridgeprotocol.ReadFrame(ctx, server)
			if readErr != nil {
				return
			}
			switch frame.MessageType {
			case bridgeprotocol.Hello:
				_ = bridgeprotocol.WriteFrame(ctx, server, bridgeprotocol.Frame{MessageType: bridgeprotocol.HelloOK, CorrelationID: frame.CorrelationID})
			case bridgeprotocol.Acquire:
				_ = bridgeprotocol.WriteFrame(ctx, server, bridgeprotocol.Frame{MessageType: bridgeprotocol.AcquireOK, CorrelationID: frame.CorrelationID})
			case bridgeprotocol.Ping:
				_ = bridgeprotocol.WriteFrame(ctx, server, bridgeprotocol.Frame{MessageType: bridgeprotocol.Pong, CorrelationID: frame.CorrelationID})
				return
			}
		}
	}()
	transport.SetCorrelationID("ping-operation")
	if err := transport.Open(); err != nil {
		t.Fatal(err)
	}
	if err := transport.Ping(context.Background(), "ping-operation"); err != nil {
		t.Fatal(err)
	}
	_ = transport.Close()
}
