//go:build integration

package mobile

import (
	"context"
	"net"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridge"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/ownership"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/serial"
)

func TestRealBridgeSharedLogAndMobileLifecycleRegression(t *testing.T) {
	probe, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		t.Fatal(err)
	}
	port := probe.Addr().(*net.TCPAddr).Port
	_ = probe.Close()
	tracer := logging.NewTracer()
	logPath := filepath.Join(t.TempDir(), "LogPinpadAbecs.txt")
	if _, err := tracer.SetLogDestination(logPath); err != nil {
		t.Fatal(err)
	}
	defer tracer.Close()
	transport := serial.NewDiagnosticScriptedTransport()
	transport.SetTracer(tracer)
	transport.SetOpaqueTrace(true)
	ready := make(chan struct{})
	server, err := bridge.New(bridge.Config{Port: port, SerialName: "SCRIPTED-072", Mode: "scripted", Transport: transport, Ownership: ownership.New(), Tracer: tracer, OnListening: func(string) { close(ready) }})
	if err != nil {
		t.Fatal(err)
	}
	ctx, cancel := context.WithCancel(context.Background())
	done := make(chan error, 1)
	go func() { done <- server.Serve(ctx) }()
	defer func() {
		cancel()
		if err := <-done; err != nil {
			t.Error(err)
		}
	}()
	select {
	case <-ready:
	case <-time.After(3 * time.Second):
		t.Fatal("server not ready")
	}
	client, err := NewClient("127.0.0.1", port, 5000)
	if err != nil {
		t.Fatal(err)
	}
	defer client.Close("cleanup")
	if err := client.Ping("072-integration-ping"); err != nil {
		t.Fatal(err)
	}
	if client.GetState() != "CLOSED" {
		t.Fatal("Ping opened pinpad")
	}
	if err := client.Open("072-integration-open"); err != nil {
		t.Fatal(err)
	}
	if client.GetState() != "OPEN" {
		t.Fatal("OPN did not open")
	}
	if _, err := client.GetInfoJSON("072-integration-gix"); err != nil {
		t.Fatal(err)
	}
	if err := client.DisplayDSP("072-integration-dsp", "TESTE ANDROID", "HOST TEST"); err != nil {
		t.Fatal(err)
	}
	if err := client.Close("072-integration-close"); err != nil {
		t.Fatal(err)
	}
	// A reabertura é imediata: o ACK de Close confirma liberação do ownership.
	if err := client.Open("072-integration-reopen"); err != nil {
		t.Fatal(err)
	}
	if err := client.Close("072-integration-reclose"); err != nil {
		t.Fatal(err)
	}
	waitReleased(t, logPath, 2)
	contents, _ := os.ReadFile(logPath)
	for _, event := range []string{"bridge_listening", "ping_ok", "ownership_acquired", "session_acquired", "session_released", "SPE", "PP"} {
		if !strings.Contains(string(contents), event) {
			t.Errorf("missing %s", event)
		}
	}
	if strings.Contains(string(contents), "TESTE ANDROID") {
		t.Fatal("opaque data escaped redaction")
	}
}

func waitReleased(t *testing.T, path string, count int) {
	t.Helper()
	deadline := time.NewTimer(3 * time.Second)
	defer deadline.Stop()
	ticker := time.NewTicker(time.Millisecond)
	defer ticker.Stop()
	for {
		contents, _ := os.ReadFile(path)
		if strings.Count(string(contents), `event="session_released"`) >= count {
			return
		}
		select {
		case <-deadline.C:
			t.Fatal("release not persisted")
		case <-ticker.C:
		}
	}
}
