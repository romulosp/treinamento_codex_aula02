package logging

import (
	"os"
	"path/filepath"
	"strings"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

func TestTracerDetailedDSP(t *testing.T) {
	path := filepath.Join(t.TempDir(), "trace.txt")
	tracer := NewTracer()
	tracer.SetDetailed(true)
	if active, err := tracer.SetLogDestination(path); err != nil || !active {
		t.Fatalf("SetLogDestination active=%t err=%v", active, err)
	}

	packet := protocol.BuildCommand("DSP", []byte("0123456789abcdef0123456789abcdef"))
	if err := tracer.RecordSPE(command.CommandDSP, packet); err != nil {
		t.Fatalf("RecordSPE: %v", err)
	}
	if err := tracer.Close(); err != nil {
		t.Fatalf("Close: %v", err)
	}

	content := readTraceFile(t, path)
	for _, want := range []string{"CMD=DSP", "HEX:", "COMANDO: DSP", "CRC: RECEBIDO=", "STATUS=VALIDO"} {
		if !strings.Contains(content, want) {
			t.Errorf("conteúdo não contém %q:\n%s", want, content)
		}
	}
}

func TestTracerDetailedSensitiveStaysRedacted(t *testing.T) {
	path := filepath.Join(t.TempDir(), "trace.txt")
	tracer := NewTracer()
	tracer.SetDetailed(true)
	if active, err := tracer.SetLogDestination(path); err != nil || !active {
		t.Fatalf("SetLogDestination active=%t err=%v", active, err)
	}

	packet := protocol.BuildCommand("GOX", []byte("dados-sensiveis"))
	if err := tracer.RecordSPE(command.CommandGOX, packet); err != nil {
		t.Fatalf("RecordSPE: %v", err)
	}
	if err := tracer.Close(); err != nil {
		t.Fatalf("Close: %v", err)
	}

	content := readTraceFile(t, path)
	if strings.Contains(content, "HEX:") || strings.Contains(content, "COMANDO: GOX -") {
		t.Errorf("comando sensível não deve ser detalhado:\n%s", content)
	}
	if !strings.Contains(content, "REDACTED") {
		t.Errorf("esperava REDACTED para comando sensível:\n%s", content)
	}
}

func TestTracerLegacyDefault(t *testing.T) {
	path := filepath.Join(t.TempDir(), "trace.txt")
	tracer := NewTracer()
	if active, err := tracer.SetLogDestination(path); err != nil || !active {
		t.Fatalf("SetLogDestination active=%t err=%v", active, err)
	}

	packet := protocol.BuildCommand("DSP", []byte("0123456789abcdef0123456789abcdef"))
	if err := tracer.RecordSPE(command.CommandDSP, packet); err != nil {
		t.Fatalf("RecordSPE: %v", err)
	}
	if err := tracer.Close(); err != nil {
		t.Fatalf("Close: %v", err)
	}

	content := readTraceFile(t, path)
	if strings.Contains(content, "HEX:") {
		t.Errorf("modo LEGACY (padrão) não deve detalhar:\n%s", content)
	}
	if !strings.Contains(content, "CMD=DSP") {
		t.Errorf("esperava CMD=DSP no LEGACY:\n%s", content)
	}
}

func readTraceFile(t *testing.T, path string) string {
	t.Helper()
	data, err := os.ReadFile(path)
	if err != nil {
		t.Fatalf("ReadFile: %v", err)
	}
	return string(data)
}
