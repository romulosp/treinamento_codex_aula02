//go:build !windows

package ownership

import (
	"context"
	"sync"
)

var processLocks = struct {
	sync.Mutex
	items map[string]*sync.Mutex
}{items: make(map[string]*sync.Mutex)}

func acquirePlatform(ctx context.Context, resource string) (func() error, error) {
	processLocks.Lock()
	lock := processLocks.items[resource]
	if lock == nil {
		lock = &sync.Mutex{}
		processLocks.items[resource] = lock
	}
	processLocks.Unlock()
	select {
	case <-ctx.Done():
		return nil, ctx.Err()
	default:
	}
	if !lock.TryLock() {
		return nil, ErrBusy
	}
	return func() error { lock.Unlock(); return nil }, nil
}
