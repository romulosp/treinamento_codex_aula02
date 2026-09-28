package session

import (
	"sync"
	"time"
)

// DefaultTimeout é o prazo de inatividade da posse lógica do pinpad.
const DefaultTimeout = 300 * time.Second

// SessionManager define a posse lógica exclusiva, segura para concorrência.
type SessionManager interface {
	Claim(string) bool
	Renew(string) bool
	Release(string)
	ForceRelease()
	IsOwner(string) bool
	HasOwner() bool
	CurrentOwner() string
	IsSessionExpired() bool
}

// Clock fornece o horário para controlar a expiração da sessão.
type Clock interface{ Now() time.Time }
type realClock struct{}

func (realClock) Now() time.Time { return time.Now() }

// Manager implementa SessionManager com expiração por inatividade.
type Manager struct {
	mu           sync.Mutex
	owner        string
	lastActivity time.Time
	timeout      time.Duration
	clock        Clock
}

// New cria um gerenciador; timeout não positivo usa DefaultTimeout.
func New(timeout time.Duration, clock Clock) *Manager {
	if timeout <= 0 {
		timeout = DefaultTimeout
	}
	if clock == nil {
		clock = realClock{}
	}
	return &Manager{timeout: timeout, clock: clock}
}
func (m *Manager) expiredLocked() bool {
	return m.owner != "" && m.clock.Now().Sub(m.lastActivity) >= m.timeout
}

// Claim adquire ou renova a posse para id. Uma nova tentativa do owner atual é
// idempotente e atualiza a atividade; um owner distinto não a obtém.
func (m *Manager) Claim(id string) bool {
	if id == "" {
		return false
	}
	m.mu.Lock()
	defer m.mu.Unlock()
	if m.expiredLocked() {
		m.owner = ""
	}
	if m.owner == id {
		m.lastActivity = m.clock.Now()
		return true
	}
	if m.owner != "" {
		return false
	}
	m.owner, m.lastActivity = id, m.clock.Now()
	return true
}

// Renew atualiza a atividade somente para o owner atual e não expirado.
func (m *Manager) Renew(id string) bool {
	m.mu.Lock()
	defer m.mu.Unlock()
	if m.owner != id || m.expiredLocked() {
		return false
	}
	m.lastActivity = m.clock.Now()
	return true
}

// Release libera a posse somente quando id é o owner atual.
func (m *Manager) Release(id string) {
	m.mu.Lock()
	defer m.mu.Unlock()
	if m.owner == id {
		m.owner = ""
	}
}

// ForceRelease libera a posse independentemente do owner atual.
func (m *Manager) ForceRelease() { m.mu.Lock(); defer m.mu.Unlock(); m.owner = "" }

// IsOwner informa se id possui a sessão ainda não expirada.
func (m *Manager) IsOwner(id string) bool {
	m.mu.Lock()
	defer m.mu.Unlock()
	if m.expiredLocked() {
		m.owner = ""
		return false
	}
	return m.owner != "" && m.owner == id
}

// HasOwner informa se existe owner ainda não expirado.
func (m *Manager) HasOwner() bool {
	m.mu.Lock()
	defer m.mu.Unlock()
	if m.expiredLocked() {
		m.owner = ""
	}
	return m.owner != ""
}

// CurrentOwner devolve o owner atual ou vazio quando a sessão expirou.
func (m *Manager) CurrentOwner() string {
	m.mu.Lock()
	defer m.mu.Unlock()
	if m.expiredLocked() {
		m.owner = ""
	}
	return m.owner
}

// IsSessionExpired informa se há uma posse que excedeu seu prazo de inatividade.
func (m *Manager) IsSessionExpired() bool { m.mu.Lock(); defer m.mu.Unlock(); return m.expiredLocked() }
