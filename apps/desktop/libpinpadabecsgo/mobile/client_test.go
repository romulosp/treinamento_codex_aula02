package mobile

import "testing"

func TestNewClientRejectsInvalidTimeout(t *testing.T) {
	if _, err := NewClient("localhost", 39100, 0); err == nil {
		t.Fatal("expected invalid timeout")
	}
}

func TestClientStateStartsClosed(t *testing.T) {
	client, err := NewClient("localhost", 39100, 1000)
	if err != nil {
		t.Fatal(err)
	}
	if got := client.GetState(); got != "CLOSED" {
		t.Fatalf("state = %q, want CLOSED", got)
	}
}

func TestCancelUnknownOperationReturnsControlledError(t *testing.T) {
	client, err := NewClient("localhost", 39100, 1000)
	if err != nil {
		t.Fatal(err)
	}
	if err := client.Cancel("missing"); err == nil {
		t.Fatal("expected missing operation error")
	}
}

func TestVersionContainsBuildProvenance(t *testing.T) {
	value := Version()
	if value == "" || value == "version= revision= dirty=" {
		t.Fatalf("unexpected version: %q", value)
	}
}
