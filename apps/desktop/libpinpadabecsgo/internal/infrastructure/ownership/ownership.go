// Package ownership coordena a posse exclusiva da porta física.
package ownership

import (
	"context"
	"errors"
	"fmt"
	"strings"
	"sync"
)

// ErrBusy informa que outra sessão já possui o recurso.
var ErrBusy = errors.New("serial ownership busy")

// Acquirer representa um mecanismo de ownership por sessão.
type Acquirer interface {
	Acquire(context.Context, string) (func() error, error)
}

// Manager usa um backend específico da plataforma para proteger a porta.
type Manager struct {
	mu     sync.Mutex
	active map[string]struct{}
}

// New cria um coordenador de ownership.
func New() *Manager { return &Manager{active: make(map[string]struct{})} }

// Acquire reserva uma porta até a função release ser chamada.
func (m *Manager) Acquire(ctx context.Context, resource string) (func() error, error) {
	if ctx == nil {
		ctx = context.Background()
	}
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	resource = strings.TrimSpace(resource)
	if resource == "" {
		return nil, fmt.Errorf("serial ownership resource is empty")
	}
	m.mu.Lock()
	if _, exists := m.active[resource]; exists {
		m.mu.Unlock()
		return nil, ErrBusy
	}
	m.active[resource] = struct{}{}
	m.mu.Unlock()
	releasePlatform, err := acquirePlatform(ctx, resource)
	if err != nil {
		m.mu.Lock()
		delete(m.active, resource)
		m.mu.Unlock()
		return nil, err
	}
	return func() error {
		err := releasePlatform()
		m.mu.Lock()
		delete(m.active, resource)
		m.mu.Unlock()
		return err
	}, nil
}
