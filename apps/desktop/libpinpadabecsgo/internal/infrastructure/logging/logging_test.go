package logging

import (
	"errors"
	"os"
	"path/filepath"
	"strings"
	"sync"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
)

func TestRedactPayload(t *testing.T) {
	if New() == nil {
		t.Fatal("structured logger must be created")
	}
	if got := RedactPayload(command.CommandGPN, []byte{1, 2, 3}); got != "**REDACTED(3 bytes)**" {
		t.Fatalf("sensitive payload = %q", got)
	}
	if got := RedactPayload(command.CommandGIX, []byte{0xAA}); got != "AA" {
		t.Fatalf("ordinary payload = %q", got)
	}
}

func TestTracerRecordsIOFailuresAndSanitizesLines(t *testing.T) {
	path := filepath.Join(t.TempDir(), "trace.log")
	tracer := NewTracer()
	if active, err := tracer.SetLogDestination(path); err != nil || !active {
		t.Fatalf("SetLogDestination active=%t err=%v", active, err)
	}
	if err := tracer.RecordOpenFailure("COM\t9", 19200, errors.New("falha\ndo SO")); err != nil {
		t.Fatal(err)
	}
	if err := tracer.RecordError("COM9", "read", errors.New("porta\rfechada")); err != nil {
		t.Fatal(err)
	}
	if err := tracer.RecordOpen("COM9", 19200); err != nil {
		t.Fatal(err)
	}
	if err := tracer.RecordClose(errors.New("close\nfailed")); err != nil {
		t.Fatal(err)
	}
	if err := tracer.Close(); err != nil {
		t.Fatal(err)
	}

	content, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	text := string(content)
	for _, expected := range []string{
		"[COM 9#OPEN] open(COM 9,19200,8,N,1)=>ERRO: falha do SO",
		"[COM9#000] read()=>ERRO: porta fechada",
		"[COM9#001] close()=>ERRO: close failed",
	} {
		if !strings.Contains(text, expected) {
			t.Fatalf("trace does not contain %q: %q", expected, text)
		}
	}
}

func TestTracerReplacesAndDisablesDestinations(t *testing.T) {
	directory := t.TempDir()
	firstPath := filepath.Join(directory, "first.log")
	secondPath := filepath.Join(directory, "second.log")
	tracer := NewTracer()
	if active, err := tracer.SetLogDestination(firstPath); err != nil || !active {
		t.Fatalf("first destination active=%t err=%v", active, err)
	}
	if active, err := tracer.SetLogDestination(secondPath); err != nil || !active {
		t.Fatalf("second destination active=%t err=%v", active, err)
	}
	renamed := filepath.Join(directory, "first-renamed.log")
	if err := os.Rename(firstPath, renamed); err != nil {
		t.Fatalf("previous destination remained open: %v", err)
	}
	if active, err := tracer.SetLogDestination(""); err != nil || !active {
		t.Fatalf("disable active=%t err=%v", active, err)
	}
	if active, err := tracer.SetLogDestination(""); err != nil || !active {
		t.Fatalf("second disable active=%t err=%v", active, err)
	}
}

func TestNilAndDisabledTracerAreNoop(t *testing.T) {
	var nilTracer *Tracer
	if nilTracer.Err() != nil || nilTracer.Close() != nil || nilTracer.RecordOpen("COM7", 19200) != nil || nilTracer.RecordOpenFailure("COM7", 19200, errors.New("x")) != nil || nilTracer.RecordClose(nil) != nil || nilTracer.RecordSPE(command.CommandGIX, []byte{1}) != nil || nilTracer.RecordPP(command.CommandGIX, []byte{1}) != nil || nilTracer.RecordResponse(command.CommandGIX, "000") != nil || nilTracer.RecordError("COM7", "read", errors.New("x")) != nil {
		t.Fatal("nil tracer must be a no-op")
	}
	disabled := NewTracer()
	if err := disabled.RecordSPE(command.CommandGIX, []byte{1}); err != nil {
		t.Fatal(err)
	}
	if err := disabled.RecordPP(command.CommandGIX, nil); err != nil {
		t.Fatal(err)
	}
	if err := disabled.RecordResponse("", ""); err != nil {
		t.Fatal(err)
	}
	if err := disabled.RecordError("COM7", "read", nil); err != nil {
		t.Fatal(err)
	}
}

func TestTracerRecordsSessionAndAppends(t *testing.T) {
	path := filepath.Join(t.TempDir(), "trace.log")
	tracer := NewTracer()
	if active, err := tracer.SetLogDestination(path); err != nil || !active {
		t.Fatalf("SetLogDestination active=%t err=%v", active, err)
	}
	tracer.RecordOpen("COM7", 19200)
	tracer.RecordSPE(command.CommandGIX, []byte{0x16, 0x47, 0x49, 0x58})
	tracer.RecordPP(command.CommandGIX, []byte{0x06})
	tracer.RecordResponse(command.CommandGIX, "000")
	tracer.RecordClose(nil)
	if err := tracer.Close(); err != nil {
		t.Fatal(err)
	}

	content, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	text := string(content)
	for _, expected := range []string{
		"[COM7#001] open(COM7,19200,8,N,1)=>OK FUNC=serial.Adapter.Open DATA_HORA=",
		"[COM7#001] SPE 16 47 49 58 CMD=GIX FUNC=logging.Tracer.RecordSPE DATA_HORA=",
		"[COM7#001] PP  06 FUNC=logging.Tracer.RecordPP DATA_HORA=",
		"[COM7#001] RSP CMD=GIX STATUS=000 FUNC=service.exchangeCommand DATA_HORA=",
		"[COM7#001] close() FUNC=serial.Adapter.Close DATA_HORA=",
	} {
		if !strings.Contains(text, expected) {
			t.Fatalf("trace does not contain %q: %q", expected, text)
		}
	}
	if strings.Count(text, "DATA_HORA=") != 6 {
		t.Fatalf("timestamp count = %d, trace=%q", strings.Count(text, "DATA_HORA="), text)
	}

	second := NewTracer()
	if active, err := second.SetLogDestination(path); err != nil || !active {
		t.Fatalf("second SetLogDestination active=%t err=%v", active, err)
	}
	second.RecordOpen("COM7", 19200)
	if err := second.Close(); err != nil {
		t.Fatal(err)
	}
	content, err = os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	if strings.Count(string(content), "open(COM7,19200,8,N,1)=>OK") != 2 {
		t.Fatalf("trace was not appended: %q", content)
	}
}

func TestTracerCreatesNonEmptyFileWhenEnabled(t *testing.T) {
	path := filepath.Join(t.TempDir(), "trace.log")
	tracer := NewTracer()
	if active, err := tracer.SetLogDestination(path); err != nil || !active {
		t.Fatalf("SetLogDestination active=%t err=%v", active, err)
	}
	if err := tracer.Close(); err != nil {
		t.Fatal(err)
	}

	content, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	text := string(content)
	if strings.TrimSpace(text) == "" {
		t.Fatalf("trace file is empty after enabling the destination: %q", text)
	}
	if !strings.Contains(text, "TRACE destination=") {
		t.Fatalf("trace does not contain activation marker: %q", text)
	}
}

func TestTracerMakesPersistenceFailuresObservable(t *testing.T) {
	path := filepath.Join(t.TempDir(), "trace.log")
	tracer := NewTracer()
	if active, err := tracer.SetLogDestination(path); err != nil || !active {
		t.Fatalf("SetLogDestination active=%t err=%v", active, err)
	}

	tracer.mu.Lock()
	destination := tracer.destination
	tracer.mu.Unlock()
	if err := destination.Close(); err != nil {
		t.Fatal(err)
	}
	if err := tracer.RecordSPE(command.CommandGIX, []byte{0x16}); err == nil {
		t.Fatal("expected the closed destination write to fail")
	}
	if tracer.Err() == nil {
		t.Fatal("expected the persistence failure to remain observable")
	}
	if err := tracer.Close(); err == nil {
		t.Fatal("expected final sync/close failure to be returned")
	}
}

func TestTracerClassifiesEveryTypedCommandAndKeepsControlBytesVisible(t *testing.T) {
	path := filepath.Join(t.TempDir(), "trace.log")
	tracer := NewTracer()
	if active, err := tracer.SetLogDestination(path); err != nil || !active {
		t.Fatalf("SetLogDestination active=%t err=%v", active, err)
	}
	if err := tracer.RecordOpen("COM7", 19200); err != nil {
		t.Fatal(err)
	}

	tests := []struct {
		kind      command.Type
		sensitive bool
	}{
		{command.CommandCAN, false}, {command.CommandOPN, false}, {command.CommandCLO, false},
		{command.CommandCLX, false}, {command.CommandGIX, false}, {command.CommandDSP, false},
		{command.CommandDEX, false}, {command.CommandMNU, false}, {command.CommandDSI, false},
		{command.CommandMLI, false}, {command.CommandMLR, true}, {command.CommandMLE, false},
		{command.CommandTLI, false}, {command.CommandTLR, true}, {command.CommandTLE, false},
		{command.CommandGKY, false}, {command.CommandGCX, true}, {command.CommandGTK, true},
		{command.CommandGOX, true}, {command.CommandFCX, true}, {command.CommandGPN, true},
	}
	for _, test := range tests {
		if err := tracer.RecordSPE(test.kind, []byte{0x16, 0x31, 0x17}); err != nil {
			t.Fatalf("RecordSPE %s: %v", test.kind, err)
		}
		if err := tracer.RecordPP(test.kind, []byte{0x06}); err != nil {
			t.Fatalf("RecordPP %s: %v", test.kind, err)
		}
	}
	if err := tracer.Close(); err != nil {
		t.Fatal(err)
	}

	content, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	text := string(content)
	for _, test := range tests {
		marker := "CMD=" + string(test.kind)
		if strings.Count(text, marker) != 1 {
			t.Fatalf("command %s was not recorded exactly once: %q", test.kind, text)
		}
		lineMarker := "SPE 16 31 17 " + marker
		if test.sensitive {
			lineMarker = "SPE **REDACTED(3 bytes)** " + marker
		}
		if !strings.Contains(text, lineMarker) {
			t.Fatalf("command %s classification not found as %q: %q", test.kind, lineMarker, text)
		}
	}
	if strings.Count(text, "PP  06") != len(tests) {
		t.Fatalf("isolated ACK must remain visible for every command: %q", text)
	}
}

func TestTracerRedactsSensitivePayloadsAndPreservesDestinationOnFailure(t *testing.T) {
	directory := t.TempDir()
	path := filepath.Join(directory, "trace.log")
	tracer := NewTracer()
	if active, err := tracer.SetLogDestination(path); err != nil || !active {
		t.Fatalf("SetLogDestination active=%t err=%v", active, err)
	}
	if active, err := tracer.SetLogDestination(filepath.Join(directory, "missing", "trace.log")); err == nil || active {
		t.Fatalf("failed replacement active=%t err=%v", active, err)
	}
	tracer.RecordOpen("COM8", 9600)
	tracer.RecordSPE(command.CommandGPN, []byte{0x12, 0x34, 0x56})
	tracer.RecordPP(command.CommandGPN, []byte{0x65, 0x43, 0x21})
	if active, err := tracer.SetLogDestination(""); err != nil || !active {
		t.Fatalf("disable active=%t err=%v", active, err)
	}
	tracer.RecordClose(nil)

	content, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	text := string(content)
	if strings.Contains(text, "12 34 56") || strings.Contains(text, "65 43 21") {
		t.Fatalf("sensitive bytes leaked: %q", text)
	}
	if count := strings.Count(text, "**REDACTED(3 bytes)**"); count != 2 {
		t.Fatalf("redaction count = %d, trace=%q", count, text)
	}
}

func TestTracerRecordsGTKClearTracksAsOneEscapedLine(t *testing.T) {
	path := filepath.Join(t.TempDir(), "trace.log")
	tracer := NewTracer()
	if active, err := tracer.SetLogDestination(path); err != nil || !active {
		t.Fatalf("SetLogDestination active=%t err=%v", active, err)
	}
	if err := tracer.RecordGTKClearTracks(
		[]byte("TRACK-ONE\nFORGED"),
		[]byte{0x54, 0x28, 0x20, 0x60, 0x97, 0x98, 0x40, 0x97, 0xD1, 0x12, 0x23, 0x36, 0x65, 0x5F},
		nil,
	); err != nil {
		t.Fatal(err)
	}
	if err := tracer.Close(); err != nil {
		t.Fatal(err)
	}

	content, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	text := string(content)
	want := `GTK_CLEAR TRACK1="TRACK-ONE\nFORGED" TRACK2="5428206097984097=1122336655" TRACK3=""`
	if !strings.Contains(text, want) {
		t.Fatalf("GTK clear trace does not contain %q: %q", want, text)
	}
	if strings.Contains(text, "TRACK-ONE\nFORGED") {
		t.Fatalf("GTK track created an injected line: %q", text)
	}
}

func TestDecodeGTKClearNumericTrack(t *testing.T) {
	tests := []struct {
		name    string
		encoded []byte
		want    string
		invalid bool
	}{
		{name: "vazio"},
		{
			name:    "vetor físico com separador e filler",
			encoded: []byte{0x54, 0x28, 0x20, 0x60, 0x97, 0x98, 0x40, 0x97, 0xD2, 0x11, 0x12, 0x01, 0x38, 0x29, 0x95, 0x58, 0x46, 0x37, 0x0F},
			want:    "5428206097984097=21112013829955846370",
		},
		{name: "mais de um filler final", encoded: []byte{0x12, 0xFF}, want: "12"},
		{name: "nibble reservado", encoded: []byte{0x1A}, invalid: true},
		{name: "dígito depois do filler", encoded: []byte{0xF1}, invalid: true},
		{name: "separador depois do filler", encoded: []byte{0xFD}, invalid: true},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			got, err := decodeGTKClearNumericTrack(test.encoded)
			if (err != nil) != test.invalid || got != test.want {
				t.Fatalf("decodeGTKClearNumericTrack(%X)=%q,%v; want %q invalid=%t", test.encoded, got, err, test.want, test.invalid)
			}
		})
	}
}

func TestTracerSerializesConcurrentLinesAndKeepsInstancesIsolated(t *testing.T) {
	directory := t.TempDir()
	firstPath := filepath.Join(directory, "first.log")
	secondPath := filepath.Join(directory, "second.log")
	first := NewTracer()
	second := NewTracer()
	for _, test := range []struct {
		name   string
		tracer *Tracer
		path   string
	}{
		{name: "first", tracer: first, path: firstPath},
		{name: "second", tracer: second, path: secondPath},
	} {
		t.Run(test.name, func(t *testing.T) {
			if active, err := test.tracer.SetLogDestination(test.path); err != nil || !active {
				t.Fatalf("SetLogDestination active=%t err=%v", active, err)
			}
			test.tracer.RecordOpen(test.name, 19200)
		})
	}

	var wait sync.WaitGroup
	for index := 0; index < 20; index++ {
		wait.Add(1)
		go func(value byte) {
			defer wait.Done()
			first.RecordSPE(command.CommandGIX, []byte{value})
		}(byte(index))
	}
	wait.Wait()
	if err := first.Close(); err != nil {
		t.Fatal(err)
	}
	if err := second.Close(); err != nil {
		t.Fatal(err)
	}

	firstContent, err := os.ReadFile(firstPath)
	if err != nil {
		t.Fatal(err)
	}
	secondContent, err := os.ReadFile(secondPath)
	if err != nil {
		t.Fatal(err)
	}
	if lines := strings.Count(string(firstContent), "\n"); lines != 22 {
		t.Fatalf("first line count = %d, trace=%q", lines, firstContent)
	}
	if strings.Contains(string(secondContent), "SPE") || !strings.Contains(string(secondContent), "[second#001] open(second,19200,8,N,1)=>OK") {
		t.Fatalf("instances mixed: %q", secondContent)
	}
}
