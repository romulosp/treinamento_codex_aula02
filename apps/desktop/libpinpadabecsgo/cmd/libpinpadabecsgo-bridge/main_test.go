package main

import (
	"os"
	"path/filepath"
	"strings"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
)

func TestResolveLogDestinationUsesModuleDefault(t *testing.T) {
	destination, err := resolveLogDestination("")
	if err != nil {
		t.Fatal(err)
	}
	if !strings.HasSuffix(filepath.ToSlash(destination), "/logs/LogPinpadAbecs.txt") {
		t.Fatalf("destination = %q", destination)
	}
}

func TestConfigureTracerCreatesConfiguredFile(t *testing.T) {
	destination := filepath.Join(t.TempDir(), "nested", "LogPinpadAbecs.txt")
	tracer := logging.NewTracer()
	resolved, err := configureTracerFor(tracer, destination)
	if err != nil {
		t.Fatal(err)
	}
	if resolved != destination {
		t.Fatalf("resolved = %q, want %q", resolved, destination)
	}
	if err := tracer.Close(); err != nil {
		t.Fatal(err)
	}
	content, err := os.ReadFile(destination)
	if err != nil {
		t.Fatal(err)
	}
	if !strings.Contains(string(content), "TRACE destination=") {
		t.Fatalf("trace file does not contain activation record: %q", content)
	}
}
