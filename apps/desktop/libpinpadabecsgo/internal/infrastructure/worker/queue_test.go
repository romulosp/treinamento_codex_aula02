package worker

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"context"
	"testing"
)

func TestQueueFIFO(t *testing.T) {
	q := New(2)
	defer q.Stop()
	r, err := q.Submit(context.Background(), command.Command{Type: command.CommandGIX, Execute: func(context.Context) (*model.Response, error) { return &model.Response{AckType: "ACK"}, nil }})
	if err != nil || r.AckType != "ACK" {
		t.Fatalf("%#v %v", r, err)
	}
}
