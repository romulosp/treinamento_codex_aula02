package command

import "testing"

func TestAdvancedBuildersValidateAndBuild(t *testing.T) {
	if got, err := BuildGCXCommand("000000001000", "090926", "121314", 1); err != nil || string(got[:3]) != "GCX" {
		t.Fatalf("GCX: %q %v", got, err)
	}
	if _, err := BuildGCXCommand("", "090926", "121314", 1); err == nil {
		t.Fatal("expected GCX validation error")
	}
	if _, err := BuildGPNCommandMK(1, "1234567890", "Digite"); err != nil {
		t.Fatal(err)
	}
	if _, err := BuildGPNCommandDUKPT("12345678901234567890", "1234", "Digite"); err != nil {
		t.Fatal(err)
	}
	block, ksn, err := ParseGPNResponse(make([]byte, 36))
	if err != nil || len(block) != 16 || len(ksn) != 20 {
		t.Fatalf("GPN response: %v", err)
	}
}

func TestDisplayAndTableBuilders(t *testing.T) {
	if got := BuildDSPCommand("a", "b"); len(got) != 38 {
		t.Fatalf("DSP length = %d", len(got))
	}
	if _, err := BuildMNUCommand(1000, "menu", nil); err == nil {
		t.Fatal("expected timeout validation")
	}
	if _, err := BuildTLRCommand(nil); err == nil {
		t.Fatal("expected records validation")
	}
}
