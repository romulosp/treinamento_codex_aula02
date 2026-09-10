package session

import (
	"testing"
	"time"
)

type fakeClock struct{ now time.Time }

func (c *fakeClock) Now() time.Time { return c.now }
func TestManagerOwnershipAndExpiration(t *testing.T) {
	clock := &fakeClock{now: time.Unix(0, 0)}
	m := New(300*time.Second, clock)
	if !m.Claim("a") || m.Claim("b") {
		t.Fatal("claim behavior")
	}
	if !m.Renew("a") {
		t.Fatal("renew")
	}
	clock.now = clock.now.Add(301 * time.Second)
	if m.HasOwner() {
		t.Fatal("owner should expire")
	}
	if !m.Claim("b") {
		t.Fatal("new claim")
	}
	m.Release("a")
	if !m.IsOwner("b") {
		t.Fatal("wrong release changed owner")
	}
	m.ForceRelease()
	if m.HasOwner() {
		t.Fatal("force release")
	}
}
