package service

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"bytes"
	"context"
	"errors"
	"fmt"
	"testing"
	"time"
)

func TestMultimediaTransferPacketsAndFailures(t *testing.T) {
	for _, tc := range []struct {
		name   string
		size   int
		failAt int
		status string
	}{
		{"menor", 8, -1, ""}, {"exato", 995, -1, ""}, {"maior", 996, -1, ""},
		{"MLI par?metro", 996, 0, "011"}, {"MLI obrigat?rio", 996, 0, "019"},
		{"MLR sequ?ncia", 996, 1, "010"}, {"MLR mem?ria", 996, 2, "040"},
		{"MLE CRC", 996, 3, "102"}, {"MLE mem?ria", 996, 3, "040"},
	} {
		t.Run(tc.name, func(t *testing.T) {
			data := make([]byte, tc.size)
			copy(data, []byte("\x89PNG\r\n\x1a\n"))
			blocks := (len(data) + 994) / 995
			commands := []string{"MLI"}
			for range blocks {
				commands = append(commands, "MLR")
			}
			commands = append(commands, "MLE")
			p := &fakePort{reads: [][]byte{response("OPN000")}}
			for i, c := range commands {
				status := "000"
				if i == tc.failAt {
					status = tc.status
				}
				p.reads = append(p.reads, response(c+status))
			}
			s := New(model.DefaultConfig(), p)
			defer s.Shutdown()
			if err := s.Open(context.Background()); err != nil {
				t.Fatal(err)
			}
			before := len(p.writes)
			final := false
			err := s.SendMultimediaFile(context.Background(), "QRCODE01", data, func(_ context.Context, current, total int64) error {
				if current == total {
					final = true
					if len(p.writes)-before != len(commands) {
						t.Fatal("progresso final antes do MLE")
					}
				}
				return nil
			})
			if (err != nil) != (tc.failAt >= 0) {
				t.Fatalf("erro: %v", err)
			}
			expected := len(commands)
			if tc.failAt >= 0 {
				expected = tc.failAt + 1
			}
			if len(p.writes)-before != expected || final != (tc.failAt < 0) {
				t.Fatalf("escritas/progresso: %d/%t", len(p.writes)-before, final)
			}
			for i := 0; i < expected; i++ {
				var payload []byte
				switch commands[i] {
				case "MLI":
					// Vetor m?nimo: X4 tamanho, CRC16 PNG 61A5, tipo PNG e RUF.
					if tc.size != 8 {
						continue
					}
					payload = append([]byte("MLI026\x00\x1e\x00\x08QRCODE01\x00\x1f\x00\x0a"), []byte{0, 0, 0, 8, 0x61, 0xa5, 1, 0, 0, 0}...)
				case "MLR":
					start := (i - 1) * 995
					end := min(start+995, len(data))
					n := end - start
					payload = []byte(fmt.Sprintf("MLR%03d", n+4))
					payload = append(payload, 0, 15, byte(n>>8), byte(n))
					payload = append(payload, data[start:end]...)
				case "MLE":
					payload = []byte("MLE")
				}
				if !bytes.Equal(p.writes[before+i], protocol.BuildPacket(payload)) {
					t.Fatalf("pacote %s divergente", commands[i])
				}
			}
		})
	}
}

func TestMultimediaExpiredContextSendsNothing(t *testing.T) {
	p := &fakePort{}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	ctx, cancel := context.WithDeadline(context.Background(), time.Now().Add(-time.Second))
	defer cancel()
	err := s.SendMultimediaFile(ctx, "QRCODE01", []byte("\x89PNG\r\n\x1a\n"), nil)
	if !errors.Is(err, context.DeadlineExceeded) || len(p.writes) != 0 {
		t.Fatalf("erro=%v escritas=%d", err, len(p.writes))
	}
}

func TestMultimediaProgressCancellationStopsBeforeNextBlock(t *testing.T) {
	p := &fakePort{reads: [][]byte{response("OPN000"), response("MLI000"), response("MLR000")}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	before := len(p.writes)
	data := make([]byte, 996)
	copy(data, []byte("\x89PNG\r\n\x1a\n"))
	err := s.SendMultimediaFile(context.Background(), "QRCODE01", data, func(context.Context, int64, int64) error { return context.Canceled })
	if !errors.Is(err, context.Canceled) || len(p.writes)-before != 2 {
		t.Fatalf("erro=%v escritas=%d", err, len(p.writes)-before)
	}
}
