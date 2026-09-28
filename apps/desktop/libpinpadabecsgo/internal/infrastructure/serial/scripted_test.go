package serial

import (
	"context"
	"testing"
)

func TestDiagnosticScriptedTransportReplaysTranscript(t *testing.T) {
	transport := NewDiagnosticScriptedTransport()
	if err := transport.Open(); err != nil {
		t.Fatal(err)
	}
	for _, step := range transport.steps {
		if err := transport.Write(step.ExpectedWrite); err != nil {
			t.Fatal(err)
		}
		for range step.Reads {
			if _, err := transport.Read(context.Background()); err != nil {
				t.Fatal(err)
			}
		}
	}
	if !transport.Completed() {
		t.Fatal("expected scripted transcript to complete")
	}
}

func TestScriptedTransportRejectsUnexpectedWrite(t *testing.T) {
	transport := NewScriptedTransport([]ScriptStep{{ExpectedWrite: []byte("expected")}})
	if err := transport.Open(); err != nil {
		t.Fatal(err)
	}
	if err := transport.Write([]byte("unexpected")); err == nil {
		t.Fatal("expected transcript mismatch")
	}
}
