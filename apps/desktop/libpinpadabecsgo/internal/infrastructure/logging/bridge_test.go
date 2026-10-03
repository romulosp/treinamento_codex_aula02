package logging

import (
	"os"
	"path/filepath"
	"strings"
	"testing"
)

func TestBridgeEventIsImmediatelyPersistedAndDelimited(t *testing.T) {
	tracer := NewTracer()
	path := filepath.Join(t.TempDir(), "trace.txt")
	if _, err := tracer.SetLogDestination(path); err != nil {
		t.Fatal(err)
	}
	defer tracer.Close()
	if err := tracer.RecordBridgeEvent(BridgeEvent{Event: "ping_ok", Phase: "ping", CorrelationID: "one\ntwo", SessionID: strings.Repeat("x", 300), Port: "COM14", Mode: "physical", PID: 72}); err != nil {
		t.Fatal(err)
	}
	data, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	if strings.Count(string(data), "\n") != 2 || !strings.Contains(string(data), `correlationId="one two"`) || strings.Contains(string(data), strings.Repeat("x", 129)) {
		t.Fatalf("unsafe event: %s", data)
	}
	var absent *Tracer
	if err := absent.RecordBridgeEvent(BridgeEvent{}); err != nil {
		t.Fatal(err)
	}
}
