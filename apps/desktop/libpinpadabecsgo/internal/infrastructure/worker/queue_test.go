package worker

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"context"
	"errors"
	"testing"
	"time"
)

func TestQueueFIFO(t *testing.T) {
	q := New(2)
	defer q.Stop()
	r, err := q.Submit(context.Background(), command.Command{Type: command.CommandGIX, Execute: func(context.Context) (*model.Response, error) { return &model.Response{AckType: "ACK"}, nil }})
	if err != nil || r.AckType != "ACK" {
		t.Fatalf("%#v %v", r, err)
	}
}

func TestEnqueueReturnsBeforeCommandCompletes(t *testing.T) {
	q := New(1)
	defer q.Stop()
	started := make(chan struct{})
	release := make(chan struct{})
	result := make(chan command.Result, 1)
	if err := q.Enqueue(context.Background(), command.Command{Type: command.CommandGIX, Result: result, Execute: func(context.Context) (*model.Response, error) {
		close(started)
		<-release
		return &model.Response{AckType: "ACK"}, nil
	}}); err != nil {
		t.Fatal(err)
	}
	select {
	case <-started:
	case <-time.After(time.Second):
		t.Fatal("command did not start")
	}
	select {
	case <-result:
		t.Fatal("Enqueue waited for execution")
	default:
	}
	close(release)
	select {
	case out := <-result:
		if out.Err != nil || out.Response == nil {
			t.Fatalf("result = %#v", out)
		}
	case <-time.After(time.Second):
		t.Fatal("asynchronous result not delivered")
	}
}

func TestStopCancelsRunningCommand(t *testing.T) {
	q := New(1)
	started := make(chan struct{})
	done := make(chan error, 1)
	go func() {
		_, err := q.Submit(context.Background(), command.Command{Type: command.CommandGIX, Execute: func(ctx context.Context) (*model.Response, error) {
			close(started)
			<-ctx.Done()
			return nil, ctx.Err()
		}})
		done <- err
	}()
	<-started
	q.Stop()
	if err := <-done; !errors.Is(err, context.Canceled) {
		t.Fatalf("submit error = %v", err)
	}
}

func TestEnqueueReturnsQueueFullWithoutBlocking(t *testing.T) {
	q := New(1)
	defer q.Stop()
	started := make(chan struct{})
	release := make(chan struct{})
	if err := q.Enqueue(context.Background(), command.Command{Execute: func(context.Context) (*model.Response, error) { close(started); <-release; return nil, nil }}); err != nil {
		t.Fatal(err)
	}
	<-started
	if err := q.Enqueue(context.Background(), command.Command{Execute: func(context.Context) (*model.Response, error) { return nil, nil }}); err != nil {
		t.Fatal(err)
	}
	if err := q.Enqueue(context.Background(), command.Command{Execute: func(context.Context) (*model.Response, error) { return nil, nil }}); !errors.Is(err, domainerror.ErrQueueFull) {
		t.Fatalf("queue error = %v", err)
	}
	close(release)
}
