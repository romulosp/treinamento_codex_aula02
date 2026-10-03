//go:build integration

package mobile

import (
	"context"
	"net"
	"testing"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridge"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/ownership"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/serial"
)

func TestClientCompletesDiagnosticFlowThroughBridgeTranscript(t *testing.T) {
	port := freeTCPPort(t)
	server, err := bridge.New(bridge.Config{
		Bind:       "127.0.0.1",
		Port:       port,
		SerialName: "SCRIPTED",
		Transport:  serial.NewDiagnosticScriptedTransport(),
		Ownership:  ownership.New(),
	})
	if err != nil {
		t.Fatal(err)
	}
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()
	serverDone := make(chan error, 1)
	go func() { serverDone <- server.Serve(ctx) }()

	client, err := NewClient("localhost", port, 2000)
	if err != nil {
		t.Fatal(err)
	}
	for attempt := 0; attempt < 20; attempt++ {
		if err = client.Ping("ping"); err == nil {
			break
		}
		time.Sleep(10 * time.Millisecond)
	}
	if err != nil {
		t.Fatal(err)
	}
	if err := client.Open("open"); err != nil {
		t.Fatal(err)
	}
	info, err := client.GetInfoJSON("get-info")
	if err != nil {
		t.Fatal(err)
	}
	if info == "" || info[0] != '{' {
		t.Fatalf("unexpected info result: %q", info)
	}
	if err := client.Close("close"); err != nil {
		t.Fatal(err)
	}
	cancel()
	if err := <-serverDone; err != nil {
		t.Fatal(err)
	}
}

func freeTCPPort(t *testing.T) int {
	t.Helper()
	listener, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		t.Fatal(err)
	}
	defer listener.Close()
	return listener.Addr().(*net.TCPAddr).Port
}
