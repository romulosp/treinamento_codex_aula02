package emulator

import (
	"context"
	"sync"
)

// contextLock serializa I/O sem goroutines para esperar por um lock cancelável.
// O token único representa a posse; nunca se fecha o canal enquanto há usuários.
type contextLock struct {
	once  sync.Once
	token chan struct{}
}

func (m *contextLock) lock(ctx context.Context) error {
	m.once.Do(func() { m.token = make(chan struct{}, 1) })
	select {
	case <-ctx.Done():
		return ctx.Err()
	case m.token <- struct{}{}:
		if err := ctx.Err(); err != nil {
			m.unlock()
			return err
		}
		return nil
	}
}

func (m *contextLock) unlock() { <-m.token }
