package state

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"sync"
	"testing"
)

func TestStoreStartsClosedAndUpdates(t *testing.T) {
	store := New()
	if got := store.Get(); got != model.StateClosed {
		t.Fatalf("initial state = %q", got)
	}
	store.Set(model.StateOpen)
	if got := store.Get(); got != model.StateOpen {
		t.Fatalf("updated state = %q", got)
	}
}

func TestStoreSupportsConcurrentAccess(t *testing.T) {
	store := New()
	var group sync.WaitGroup
	for i := 0; i < 32; i++ {
		group.Add(1)
		go func() {
			defer group.Done()
			store.Set(model.StateBusy)
			_ = store.Get()
		}()
	}
	group.Wait()
}
