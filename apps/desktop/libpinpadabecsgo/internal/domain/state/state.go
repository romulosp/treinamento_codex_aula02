package state

import "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"

type Store struct{ current model.PinpadState }

func New() *Store                           { return &Store{current: model.StateClosed} }
func (s *Store) Get() model.PinpadState     { return s.current }
func (s *Store) Set(next model.PinpadState) { s.current = next }
