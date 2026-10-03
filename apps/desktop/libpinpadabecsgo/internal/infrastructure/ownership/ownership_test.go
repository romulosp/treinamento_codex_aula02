package ownership

import (
	"context"
	"errors"
	"testing"
)

func TestManagerRejectsConcurrentOwnership(t *testing.T) {
	m := New()
	release, err := m.Acquire(context.Background(), "COM-TEST")
	if err != nil {
		t.Fatal(err)
	}
	defer release()
	if _, err := m.Acquire(context.Background(), "COM-TEST"); !errors.Is(err, ErrBusy) {
		t.Fatalf("second acquire error = %v, want ErrBusy", err)
	}
}

func TestManagerHonorsCanceledContext(t *testing.T) {
	ctx, cancel := context.WithCancel(context.Background())
	cancel()
	if _, err := New().Acquire(ctx, "COM-TEST"); !errors.Is(err, context.Canceled) {
		t.Fatalf("error = %v, want context.Canceled", err)
	}
}
