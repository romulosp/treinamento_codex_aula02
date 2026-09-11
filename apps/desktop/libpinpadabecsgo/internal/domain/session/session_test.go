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

func TestManagerClaimForCurrentOwnerIsIdempotentAndRenews(t *testing.T) {
	clock := &fakeClock{now: time.Date(2026, 9, 10, 12, 0, 0, 0, time.UTC)}
	manager := New(DefaultTimeout, clock)
	if !manager.Claim("owner") {
		t.Fatal("initial claim failed")
	}
	clock.now = clock.now.Add(DefaultTimeout - time.Second)
	if !manager.Claim("owner") {
		t.Fatal("same owner claim must renew")
	}
	clock.now = clock.now.Add(2 * time.Second)
	if !manager.HasOwner() || !manager.IsOwner("owner") {
		t.Fatal("renewed owner unexpectedly expired")
	}
}

func TestManagerReportsOwnerAndExpirationWithoutMutatingUntilObserved(t *testing.T) {
	clock := &fakeClock{now: time.Unix(0, 0)}
	manager := New(0, clock)
	if !manager.Claim("owner") || manager.CurrentOwner() != "owner" || !manager.IsOwner("owner") {
		t.Fatal("owner should be observable after claim")
	}
	if manager.Renew("other") {
		t.Fatal("another owner must not renew the session")
	}
	clock.now = clock.now.Add(DefaultTimeout)
	if !manager.IsSessionExpired() {
		t.Fatal("session should report expiration at its timeout")
	}
	if manager.CurrentOwner() != "" || manager.IsOwner("owner") {
		t.Fatal("expired session must not retain an observable owner")
	}
}
