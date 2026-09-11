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
	if _, err := BuildDEXCommand(string(make([]byte, 161))); err == nil {
		t.Fatal("expected DEX size validation")
	}
	if _, err := BuildGKYCommand(GKYModeGetKey+1, 1); err == nil {
		t.Fatal("expected GKY mode validation")
	}
	if _, err := BuildMLRCommand(make([]byte, MLRMaxBlockSize+1)); err == nil {
		t.Fatal("expected MLR block validation")
	}
	if _, err := BuildTLICommand("0", "v"); err == nil {
		t.Fatal("expected TLI validation")
	}
	if _, _, err := ParseGPNResponse(nil); err == nil {
		t.Fatal("expected GPN response validation")
	}
	if CalculateFileCRC([]byte("arquivo")) == 0 {
		t.Fatal("expected nonzero file CRC")
	}
	if string(BuildDSICommand("imagem")[:3]) != "DSI" || string(BuildMLICommand("imagem", 12)[:3]) != "MLI" || string(BuildMLECommand("imagem")[:3]) != "MLE" || string(BuildTLECommand("versao")[:3]) != "TLE" || string(BuildRSTCommand()[:3]) != "RST" {
		t.Fatal("expected simple command builders")
	}
	if _, err := BuildGPNCommandMK(100, "p", "m"); err == nil {
		t.Fatal("expected MK validation")
	}
	if _, err := BuildGPNCommandDUKPT("curto", "p", "m"); err == nil {
		t.Fatal("expected DUKPT validation")
	}
}

func TestBuildABECSPayloadSplitsBlocksAndCopiesValues(t *testing.T) {
	value := make([]byte, 995)
	payload, err := BuildABECSPayload(CommandGCX, []Parameter{{ID: SPEDataIn, Value: value}, {ID: SPEMessageIndex, Value: []byte("x")}})
	if err != nil {
		t.Fatal(err)
	}
	if string(payload[:6]) != "GCX999" || string(payload[1005:1008]) != "005" {
		t.Fatalf("unexpected ABECS blocks: %q", payload[:6])
	}
	if _, err := BuildABECSPayload(CommandGCX, []Parameter{{ID: SPEDataIn, Value: make([]byte, 996)}}); err == nil {
		t.Fatal("expected parameter size validation")
	}
}

func TestAdvancedCommandBuildersValidateRequiredFields(t *testing.T) {
	index := 7
	gtk, err := BuildGTKCommand(GTKRequest{Tracks: "1110", DataMethod: "11", OpenDigits: 2, KeyIndex: &index, WorkingKey: make([]byte, 16), IV: make([]byte, 8)})
	if err != nil || string(gtk[:3]) != "GTK" {
		t.Fatalf("GTK: %q %v", gtk, err)
	}
	if _, err := BuildGTKCommand(GTKRequest{DataMethod: "00"}); err == nil {
		t.Fatal("expected GTK key validation")
	}
	if _, err := BuildGTKCommand(GTKRequest{DataMethod: "99", KeyIndex: &index}); err == nil {
		t.Fatal("expected GTK method validation")
	}
	if _, err := BuildCLXCommand(CLXRequest{Message: "fim", MediaName: "media"}); err != nil {
		t.Fatal(err)
	}
	gox, err := BuildGOXCommand(GOXRequest{AcquirerReference: "01", PinMethod: "3", KeyIndex: 7, Amount: "000000000100"})
	if err != nil || string(gox[:3]) != "GOX" {
		t.Fatalf("GOX: %q %v", gox, err)
	}
	if _, err := BuildGOXCommand(GOXRequest{AcquirerReference: "00", PinMethod: "3", KeyIndex: 7}); err == nil {
		t.Fatal("expected GOX acquirer validation")
	}
	if _, err := BuildGOXCommand(GOXRequest{AcquirerReference: "01", PinMethod: "0", KeyIndex: 7}); err == nil {
		t.Fatal("expected GOX working key validation")
	}
	fcx, err := BuildFCXCommand(FCXRequest{Options: "0000", Authorization: "00"})
	if err != nil || string(fcx[:3]) != "FCX" {
		t.Fatalf("FCX: %q %v", fcx, err)
	}
	if _, err := BuildFCXCommand(FCXRequest{Options: "1000"}); err == nil {
		t.Fatal("expected FCX ARC validation")
	}
	if _, err := BuildFCXCommand(FCXRequest{Options: "x000"}); err == nil {
		t.Fatal("expected FCX option validation")
	}
	if _, err := BuildPayload("", "x"); err == nil {
		t.Fatal("expected generic payload validation")
	}
	if payload, err := BuildPayload(CommandDSP, "abc"); err != nil || string(payload) != "DSPabc" {
		t.Fatalf("generic payload = %q, %v", payload, err)
	}
}
