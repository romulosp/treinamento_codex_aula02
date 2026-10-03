package mobile

import (
	"context"
	"errors"
	"sync/atomic"
	"testing"
	"time"
)

func waitRegistered(t *testing.T, c *Client, id string) {
	t.Helper()
	timer := time.NewTimer(time.Second)
	defer timer.Stop()
	tick := time.NewTicker(time.Millisecond)
	defer tick.Stop()
	for {
		c.mu.Lock()
		_, ok := c.operations[id]
		c.mu.Unlock()
		if ok {
			return
		}
		select {
		case <-tick.C:
		case <-timer.C:
			t.Fatal("operation not admitted")
		}
	}
}

func TestQueuedAdmissionHonorsTimeoutCancellationAndDuplicateID(t *testing.T) {
	for _, cancelQueue := range []bool{false, true} {
		t.Run(map[bool]string{false: "timeout", true: "cancel"}[cancelQueue], func(t *testing.T) {
			c, err := NewClient("localhost", 39100, 150)
			if err != nil {
				t.Fatal(err)
			}
			gate := c.gateLocked()
			gate <- struct{}{} // Simula operação anterior, sem executar I/O físico.
			defer func() { <-gate }()
			var calls atomic.Int32
			done := make(chan error, 1)
			go func() { done <- c.run("queued", func(context.Context) error { calls.Add(1); return nil }) }()
			waitRegistered(t, c, "queued")
			if err := c.run("queued", func(context.Context) error { calls.Add(1); return nil }); ErrorCode(err.Error()) != "BUSY" {
				t.Fatalf("duplicate: %v", err)
			}
			want := "TIMEOUT"
			if cancelQueue {
				want = "CANCELED"
				if err := c.Cancel("queued"); err != nil {
					t.Fatal(err)
				}
			}
			select {
			case err := <-done:
				if err == nil || ErrorCode(err.Error()) != want {
					t.Fatalf("queue: %v", err)
				}
			case <-time.After(time.Second):
				t.Fatal("admission exceeded timeout")
			}
			if calls.Load() != 0 {
				t.Fatal("pending physical operation executed")
			}
		})
	}
}

func TestCloseCancelsRunningAndQueuedOperationsBeforeReopeningAdmission(t *testing.T) {
	c, err := NewClient("localhost", 39100, 1000)
	if err != nil {
		t.Fatal(err)
	}
	started := make(chan struct{})
	active := make(chan error, 1)
	go func() {
		active <- c.run("active", func(ctx context.Context) error { close(started); <-ctx.Done(); return ctx.Err() })
	}()
	<-started
	var calls atomic.Int32
	queued := make(chan error, 1)
	go func() { queued <- c.run("queued", func(context.Context) error { calls.Add(1); return nil }) }()
	waitRegistered(t, c, "queued")
	if err := c.Close("close"); err != nil {
		t.Fatal(err)
	}
	for _, done := range []chan error{active, queued} {
		select {
		case err := <-done:
			if !errors.Is(err, context.Canceled) {
				t.Fatalf("close: %v", err)
			}
		case <-time.After(time.Second):
			t.Fatal("close did not cancel")
		}
	}
	if calls.Load() != 0 || c.GetState() != "CLOSED" {
		t.Fatal("queued operation crossed close")
	}
	if err := c.run("new", func(context.Context) error { return nil }); err != nil {
		t.Fatal(err)
	}
}
