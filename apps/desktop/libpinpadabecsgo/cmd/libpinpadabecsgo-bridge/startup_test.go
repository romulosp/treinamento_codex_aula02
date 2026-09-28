package main

import (
	"context"
	"net"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"testing"
)

func TestStartupFailureRecordsCauseWithoutReadiness(t *testing.T) {
	for _, tc := range []struct{ name, key, value string }{
		{"port", "PINPAD_BRIDGE_PORT", "invalid"}, {"portRange", "PINPAD_BRIDGE_PORT", "0"},
		{"baud", "PINPAD_BAUDRATE", "invalid"}, {"mode", "PINPAD_BRIDGE_TRANSPORT", "invalid"},
		{"emptyCOM", "PORTA_PINPAD", " "},
	} {
		t.Run(tc.name, func(t *testing.T) {
			path := filepath.Join(t.TempDir(), "logs", "LogPinpadAbecs.txt")
			t.Setenv("PINPAD_LOG_FILE", path)
			t.Setenv("PINPAD_BRIDGE_TRANSPORT", "scripted")
			t.Setenv("PORTA_PINPAD", "COM14")
			t.Setenv("PINPAD_BAUDRATE", "19200")
			t.Setenv("PINPAD_TIMEOUT", "30")
			t.Setenv("PINPAD_BRIDGE_PORT", "39100")
			t.Setenv(tc.key, tc.value)
			if err := run(context.Background()); err == nil {
				t.Fatal("startup succeeded")
			}
			contents, _ := os.ReadFile(path)
			if !strings.Contains(string(contents), "bridge_start_failed") || strings.Contains(string(contents), "bridge_listening") {
				t.Fatalf("incorrect readiness: %s", contents)
			}
		})
	}
}

func TestBindConflictAndRequestedShutdownAreObservable(t *testing.T) {
	probe, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		t.Fatal(err)
	}
	defer probe.Close()
	port := strconv.Itoa(probe.Addr().(*net.TCPAddr).Port)
	path := filepath.Join(t.TempDir(), "log.txt")
	t.Setenv("PINPAD_LOG_FILE", path)
	t.Setenv("PINPAD_BRIDGE_PORT", port)
	t.Setenv("PINPAD_BRIDGE_TRANSPORT", "scripted")
	t.Setenv("PORTA_PINPAD", "COM14")
	if err := run(context.Background()); err == nil {
		t.Fatal("bind conflict hidden")
	}
	contents, _ := os.ReadFile(path)
	if strings.Contains(string(contents), "bridge_listening") {
		t.Fatal("false readiness")
	}
	_ = probe.Close()
	ctx, cancel := context.WithCancel(context.Background())
	cancel()
	if err := run(ctx); err != nil {
		t.Fatal(err)
	}
	contents, _ = os.ReadFile(path)
	if !strings.Contains(string(contents), "bridge_listening") || !strings.Contains(string(contents), "bridge_stopped") {
		t.Fatal("shutdown not recorded")
	}
}

func TestInvalidLogDestinationFailsBeforeStartup(t *testing.T) {
	// Um diretório não é arquivo gravável; não promete log em destino impossível.
	t.Setenv("PINPAD_LOG_FILE", t.TempDir())
	if err := run(context.Background()); err == nil {
		t.Fatal("invalid log destination accepted")
	}
}
