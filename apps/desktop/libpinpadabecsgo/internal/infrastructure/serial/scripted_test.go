package serial

import (
	"bytes"
	"context"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
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

func TestDiagnosticScriptedTransportUsesConfiguredPortInDSP(t *testing.T) {
	tests := []struct {
		name     string
		port     string
		wantLine string
	}{
		{name: "porta efetiva sem fixar COM", port: " COM10 ", wantLine: "HOST COM10"},
		{name: "ambiente ausente", port: "   ", wantLine: "HOST AMBIENTE"},
		{name: "valor maior que o display", port: "PORTA-MUITO-LONGA", wantLine: "HOST CONFIG"},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			transport := NewDiagnosticScriptedTransportForPort(tt.port)
			wantDSP, err := command.BuildDSPCommand("TESTE ANDROID", tt.wantLine)
			if err != nil {
				t.Fatal(err)
			}
			want := protocol.BuildPacket(wantDSP)
			if len(transport.steps) < 4 || !bytes.Equal(transport.steps[3].ExpectedWrite, want) {
				t.Fatalf("DSP não usa configuração efetiva: % X", transport.steps[3].ExpectedWrite)
			}
		})
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
