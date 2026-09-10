package state

import "testing"

func TestGetStatusDescription(t *testing.T) {
	if GetStatusDescription(StatusOK) != "Sucesso" {
		t.Fatal("status description")
	}
	if GetStatusDescription("999") != "Código desconhecido" {
		t.Fatal("unknown status")
	}
}
