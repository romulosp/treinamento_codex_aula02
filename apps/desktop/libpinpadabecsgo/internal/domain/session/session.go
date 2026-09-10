package session

import (
	"sync"
	"time"
)

const DefaultTimeout = 300 * time.Second

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

type Clock interface{ Now() time.Time }
type realClock struct{}

func (realClock) Now() time.Time { return time.Now() }

type Manager struct {
	mu           sync.Mutex
	owner        string
	lastActivity time.Time
	timeout      time.Duration
	clock        Clock
}

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
func (m *Manager) Claim(id string) bool {
	if id == "" {
		return false
	}
	m.mu.Lock()
	defer m.mu.Unlock()
	if m.expiredLocked() {
		m.owner = ""
	}
	if m.owner != "" {
		return false
	}
	m.owner, m.lastActivity = id, m.clock.Now()
	return true
}
func (m *Manager) Renew(id string) bool {
	m.mu.Lock()
	defer m.mu.Unlock()
	if m.owner != id || m.expiredLocked() {
		return false
	}
	m.lastActivity = m.clock.Now()
	return true
}
func (m *Manager) Release(id string) {
	m.mu.Lock()
	defer m.mu.Unlock()
	if m.owner == id {
		m.owner = ""
	}
}
func (m *Manager) ForceRelease() { m.mu.Lock(); defer m.mu.Unlock(); m.owner = "" }
func (m *Manager) IsOwner(id string) bool {
	m.mu.Lock()
	defer m.mu.Unlock()
	if m.expiredLocked() {
		m.owner = ""
		return false
	}
	return m.owner != "" && m.owner == id
}
func (m *Manager) HasOwner() bool {
	m.mu.Lock()
	defer m.mu.Unlock()
	if m.expiredLocked() {
		m.owner = ""
	}
	return m.owner != ""
}
func (m *Manager) CurrentOwner() string {
	m.mu.Lock()
	defer m.mu.Unlock()
	if m.expiredLocked() {
		m.owner = ""
	}
	return m.owner
}
func (m *Manager) IsSessionExpired() bool { m.mu.Lock(); defer m.mu.Unlock(); return m.expiredLocked() }
