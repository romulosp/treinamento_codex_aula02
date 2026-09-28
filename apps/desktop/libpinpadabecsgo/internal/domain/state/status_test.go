package state

import "testing"

func TestGetStatusDescription(t *testing.T) {
	if GetStatusDescription(StatusOK) != "Sucesso" {
		t.Fatal("status description")
	}
	if GetStatusDescription("999") != "Código desconhecido" {
		t.Fatal("unknown status")
	}
	if GetStatusDescription(StatusNoSAM) == GetStatusDescription("999") {
		t.Fatal("status 051 deve estar no catálogo")
	}
	if GetStatusDescription("047") != GetStatusDescription("999") {
		t.Fatal("status 047 não pertence ao catálogo ABECS 2.12")
	}
}
