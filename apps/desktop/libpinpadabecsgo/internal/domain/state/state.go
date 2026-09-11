package state

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"sync"
)

// Store guarda o estado observável do pinpad com acesso seguro para concorrência.
type Store struct {
	mu      sync.RWMutex
	current model.PinpadState
}

// New cria um repositório de estado iniciado como fechado.
func New() *Store { return &Store{current: model.StateClosed} }

// Get devolve o estado atual.
func (s *Store) Get() model.PinpadState {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.current
}

// Set troca o estado atual.
func (s *Store) Set(next model.PinpadState) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.current = next
}
