package worker

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"context"
	"sync"
)

type result struct {
	response *model.Response
	err      error
}

type CommandQueue interface {
	Enqueue(context.Context, command.Command) error
	Submit(context.Context, command.Command) (*model.Response, error)
	Size() int
	Clear()
	Stop()
	IsEmpty() bool
}
type item struct {
	ctx     context.Context
	command command.Command
	result  chan result
	cleanup func()
}
type Queue struct {
	mu      sync.Mutex
	items   chan item
	done    chan struct{}
	stopCtx context.Context
	cancel  context.CancelFunc
	once    sync.Once
	wg      sync.WaitGroup
	stopped bool
}

func New(capacity int) *Queue {
	if capacity <= 0 {
		capacity = 100
	}
	stopCtx, cancel := context.WithCancel(context.Background())
	q := &Queue{items: make(chan item, capacity), done: make(chan struct{}), stopCtx: stopCtx, cancel: cancel}
	q.wg.Add(1)
	go q.run()
	return q
}
func (q *Queue) run() {
	defer q.wg.Done()
	for {
		select {
		case it := <-q.items:
			q.mu.Lock()
			stopped := q.stopped
			q.mu.Unlock()
			if stopped {
				q.complete(it, result{err: context.Canceled})
				continue
			}
			if it.command.Execute == nil {
				q.complete(it, result{err: domainerror.ErrInvalidResponse})
				continue
			}
			resp, err := it.command.Execute(it.ctx)
			q.complete(it, result{response: resp, err: err})
		case <-q.done:
			return
		}
	}
}
func (q *Queue) complete(it item, out result) {
	if it.cleanup != nil {
		it.cleanup()
	}
	select {
	case it.result <- out:
	default:
	}
	if it.command.Result != nil {
		select {
		case it.command.Result <- command.Result{Response: out.response, Err: out.err}:
		default:
		}
	}
}
func (q *Queue) enqueue(ctx context.Context, cmd command.Command, resultCh chan result) error {
	if ctx == nil {
		ctx = context.Background()
	}
	if err := ctx.Err(); err != nil {
		return err
	}
	operationCtx, cancel := context.WithCancel(ctx)
	stopCancel := context.AfterFunc(q.stopCtx, cancel)
	it := item{ctx: operationCtx, command: cmd, result: resultCh, cleanup: func() { stopCancel(); cancel() }}
	q.mu.Lock()
	stopped := q.stopped
	q.mu.Unlock()
	if stopped {
		return context.Canceled
	}
	select {
	case <-q.done:
		return context.Canceled
	default:
	}
	select {
	case q.items <- it:
	default:
		return domainerror.ErrQueueFull
	}
	return nil
}
func (q *Queue) Submit(ctx context.Context, cmd command.Command) (*model.Response, error) {
	if ctx == nil {
		ctx = context.Background()
	}
	resultCh := make(chan result, 1)
	if err := q.enqueue(ctx, cmd, resultCh); err != nil {
		return nil, err
	}
	select {
	case out := <-resultCh:
		return out.response, out.err
	case <-ctx.Done():
		return nil, ctx.Err()
	}
}
func (q *Queue) Enqueue(ctx context.Context, cmd command.Command) error {
	return q.enqueue(ctx, cmd, make(chan result, 1))
}
func (q *Queue) Size() int     { return len(q.items) }
func (q *Queue) IsEmpty() bool { return q.Size() == 0 }
func (q *Queue) Clear() {
	for {
		select {
		case it := <-q.items:
			q.complete(it, result{err: context.Canceled})
		default:
			return
		}
	}
}
func (q *Queue) Stop() {
	q.once.Do(func() {
		q.mu.Lock()
		q.stopped = true
		q.mu.Unlock()
		q.cancel()
		q.Clear()
		close(q.done)
		q.wg.Wait()
	})
}
