package mobile

import (
	"context"
	"errors"
	"net"
	"testing"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/service"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridgeprotocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/transport/emulator"
)

func TestOpenFailedPreflightDoesNotAcquireSession(t *testing.T) {
	c, err := NewClient("localhost", 39100, 1000)
	if err != nil {
		t.Fatal(err)
	}
	var deadline time.Time
	transport, _ := emulator.New(emulator.Config{Host: "localhost", Port: 39100, Timeout: time.Second, Dial: func(ctx context.Context, _, _ string) (net.Conn, error) {
		deadline, _ = ctx.Deadline()
		return nil, emulator.ErrUnreachable
	}})
	c.transport = transport
	c.service = service.New(model.DefaultConfig(), transport)
	started := time.Now()
	err = c.Open("072-preflight")
	if ErrorCode(err.Error()) != "BRIDGE_UNREACHABLE" || ErrorPhase(err.Error()) != "preflight" || c.GetState() != "CLOSED" {
		t.Fatalf("%v %s", err, c.GetState())
	}
	if deadline.IsZero() || deadline.Sub(started) > time.Second+50*time.Millisecond {
		t.Fatal("missing shared deadline")
	}
}

func TestPreflightPingPreservesClosedStateAndCorrelation(t *testing.T) {
	c, err := NewClient("localhost", 39100, 1000)
	if err != nil {
		t.Fatal(err)
	}
	done := make(chan error, 1)
	transport, _ := emulator.New(emulator.Config{Port: 39100, Dial: func(ctx context.Context, _, _ string) (net.Conn, error) {
		client, peer := net.Pipe()
		go func() {
			defer peer.Close()
			frame, err := bridgeprotocol.ReadFrame(ctx, peer)
			if err == nil && frame.CorrelationID != "072-ping" {
				err = errors.New("lost hello correlation")
			}
			if err == nil {
				err = bridgeprotocol.WriteFrame(ctx, peer, bridgeprotocol.Frame{MessageType: bridgeprotocol.HelloOK})
			}
			if err == nil {
				frame, err = bridgeprotocol.ReadFrame(ctx, peer)
			}
			if err == nil && frame.MessageType != bridgeprotocol.Ping {
				err = errors.New("preflight acquired COM")
			}
			if err == nil {
				err = bridgeprotocol.WriteFrame(ctx, peer, bridgeprotocol.Frame{MessageType: bridgeprotocol.Pong, CorrelationID: frame.CorrelationID})
			}
			done <- err
		}()
		return client, nil
	}})
	c.transport = transport
	c.service = service.New(model.DefaultConfig(), transport)
	if err := c.Ping("072-ping"); err != nil {
		t.Fatal(err)
	}
	if err := <-done; err != nil {
		t.Fatal(err)
	}
	if c.GetState() != "CLOSED" {
		t.Fatal("Ping opened session")
	}
}
