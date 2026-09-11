package logging

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"os"
	"path/filepath"
	"strings"
	"sync"
	"testing"
)

func TestRedactPayload(t *testing.T) {
	if got := RedactPayload(command.CommandGPN, []byte{1, 2, 3}); got != "**REDACTED(3 bytes)**" {
		t.Fatalf("sensitive payload = %q", got)
	}
	if got := RedactPayload(command.CommandGIX, []byte{0xAA}); got != "AA" {
		t.Fatalf("ordinary payload = %q", got)
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
	if strings.Count(text, "DATA_HORA=") != 5 {
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
	if lines := strings.Count(string(firstContent), "\n"); lines != 21 {
		t.Fatalf("first line count = %d, trace=%q", lines, firstContent)
	}
	if strings.Contains(string(secondContent), "SPE") || !strings.Contains(string(secondContent), "[second#001] open(second,19200,8,N,1)=>OK") {
		t.Fatalf("instances mixed: %q", secondContent)
	}
}
