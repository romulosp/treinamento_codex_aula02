//go:build integration

package main

import (
	"context"
	"fmt"
	"net"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/mobile"
)

// TestBridgeLogFormatsOverTCP usa a mesma composição do main e o cliente mobile
// real com serial scripted. Não acessa COM nem executa pagamento.
func TestBridgeLogFormatsOverTCP(t *testing.T) {
	for _, format := range []string{"DETAILED", "LEGACY"} {
		t.Run(format, func(t *testing.T) {
			probe, err := net.Listen("tcp", "127.0.0.1:0")
			if err != nil {
				t.Fatal(err)
			}
			port := probe.Addr().(*net.TCPAddr).Port
			_ = probe.Close()
			logPath := filepath.Join(t.TempDir(), "trace.txt")
			t.Setenv("PORTA_PINPAD", "COM99")
			t.Setenv("PINPAD_BRIDGE_TRANSPORT", "scripted")
			t.Setenv("PINPAD_BRIDGE_PORT", fmt.Sprint(port))
			t.Setenv("PINPAD_LOG_FILE", logPath)
			t.Setenv("PINPAD_LOG_FORMAT", format)
			ctx, cancel := context.WithCancel(context.Background())
			done := make(chan error, 1)
			go func() { done <- run(ctx) }()
			t.Cleanup(func() {
				cancel()
				select {
				case err := <-done:
					if err != nil {
						t.Error(err)
					}
				case <-time.After(5 * time.Second):
					t.Error("bridge did not stop")
				}
			})
			// Readiness usa o listener real; somente eventos, sem sessão serial.
			deadline := time.Now().Add(5 * time.Second)
			for {
				conn, err := net.DialTimeout("tcp", fmt.Sprintf("127.0.0.1:%d", port), 100*time.Millisecond)
				if err == nil {
					_ = conn.Close()
					break
				}
				if time.Now().After(deadline) {
					t.Fatal("bridge not listening")
				}
				time.Sleep(10 * time.Millisecond)
			}
			client, err := mobile.NewClient("127.0.0.1", port, 5000)
			if err != nil {
				t.Fatal(err)
			}
			defer client.Close("smoke-cleanup")
			if err := client.Open("10017-open"); err != nil {
				t.Fatal(err)
			}
			if _, err := client.GetInfoJSON("10017-gix"); err != nil {
				t.Fatal(err)
			}
			if err := client.DisplayDSP("10017-dsp", "TESTE ANDROID", "HOST COM99"); err != nil {
				t.Fatal(err)
			}
			if err := client.Close("10017-close"); err != nil {
				t.Fatal(err)
			}
			cancel()
			// run fecha o destino antes da leitura; Cleanup também precisa do resultado.
			select {
			case err := <-done:
				done <- err
			case <-time.After(5 * time.Second):
				t.Fatal("bridge shutdown timeout")
			}
			content, err := os.ReadFile(logPath)
			if err != nil {
				t.Fatal(err)
			}
			logText := string(content)
			if !strings.Contains(logText, "TRACE FORMAT="+format) || !strings.Contains(logText, "REDACTED") {
				t.Fatal("missing format or raw redaction")
			}
			if format == "DETAILED" {
				for _, want := range []string{"SPE CMD=OPN", "PP CMD=OPN", "SPE CMD=GIX", "PP CMD=GIX", "SPE CMD=DSP", "PP CMD=DSP", "SPE CMD=CLO", "PP CMD=CLO", "HEX:", "DSP_MSG", "STATUS=VALIDO"} {
					if !strings.Contains(logText, want) {
						t.Errorf("missing %s", want)
					}
				}
			} else if strings.Contains(logText, "HEX:") || strings.Contains(logText, "TESTE ANDROID") {
				t.Fatal("LEGACY changed")
			}
		})
	}
}
