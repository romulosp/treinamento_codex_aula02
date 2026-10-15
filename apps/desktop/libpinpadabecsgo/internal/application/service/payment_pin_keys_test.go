package service

import (
	"bytes"
	"context"
	"errors"
	"io"
	"strings"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

func TestChoosePaymentPINKeyDeterministicEntropy(t *testing.T) {
	indexes := []int{0, 31, 64, 99}
	for position, want := range indexes {
		t.Run(string(rune('A'+position)), func(t *testing.T) {
			got, err := choosePaymentPINKey(indexes, bytes.NewReader([]byte{byte(position)}))
			if err != nil || got != want {
				t.Fatal("candidate unreachable with supplied entropy")
			}
		})
	}
	if got, err := choosePaymentPINKey([]int{63}, nil); err != nil || got != 63 {
		t.Fatal("singleton read entropy")
	}
	if _, err := choosePaymentPINKey(nil, nil); !errors.Is(err, domainerror.ErrPaymentPINKeyMissing) {
		t.Fatal("empty set accepted")
	}
	if _, err := choosePaymentPINKey(indexes, bytes.NewReader(nil)); !errors.Is(err, io.EOF) {
		t.Fatal("entropy failure hidden")
	}
}

func TestPaymentPINDiscoveryFreezesSelectionAndGuardsCapture(t *testing.T) {
	s, p, logs := paymentTestSession(t, "03", "000", paymentResponse("GOX", map[protocol.ResponseTag][]byte{protocol.TagGOXResult: []byte("000000")}))
	flags := bytes.Repeat([]byte{'2'}, 100)
	flags[13] = '1'
	flags[63] = '1'
	flags[99] = '1'
	p.reads = append([][]byte{paymentResponse("GIX", map[protocol.ResponseTag][]byte{protocol.TagDukptTDESDescription: flags})}, p.reads...)
	source := command.PaymentPINConfig{Method: "3", KeyIndex: 8}
	resolved, err := s.ResolvePaymentPINConfiguration(t.Context(), source)
	if err != nil {
		t.Fatal(err)
	}
	if resolved.KeyIndex != 13 && resolved.KeyIndex != 63 && resolved.KeyIndex != 99 {
		t.Fatal("unloaded index selected")
	}
	if resolved.Method != source.Method || resolved.WorkingKey != source.WorkingKey {
		t.Fatal("source method/WK replaced")
	}
	before := len(p.writes)
	again, err := s.ResolvePaymentPINConfiguration(t.Context(), source)
	if err != nil || again != resolved || len(p.writes) != before {
		t.Fatal("repeat rediscovered or rerandomized")
	}
	if _, err := s.ResolvePaymentPINConfiguration(t.Context(), command.PaymentPINConfig{Method: "3", KeyIndex: 9}); err == nil {
		t.Fatal("changed source accepted")
	}
	if _, err := s.CapturePaymentCard(t.Context(), "owner", paymentRequest(), source); err == nil || len(p.writes) != before {
		t.Fatal("GCX replaced discovered selection")
	}
	if _, err := s.CapturePaymentCard(t.Context(), "owner", paymentRequest(), resolved); err != nil {
		t.Fatal(err)
	}
	if _, err := s.ResolvePaymentPINConfiguration(t.Context(), source); err == nil {
		t.Fatal("rediscovery after capture")
	}
	key := 7
	if err := s.PaymentTracks(t.Context(), "owner", command.GTKRequest{DataMethod: "30", KeyIndex: &key}); err != nil {
		t.Fatal(err)
	}
	request := paymentGOXRequest()
	request.KeyIndex = resolved.KeyIndex
	if _, err := s.ContinuePaymentEMV(t.Context(), "owner", request); err != nil {
		t.Fatal(err)
	}
	if !strings.Contains(logs.String(), `"msg":"pin_key_selected"`) {
		t.Fatal("missing safe selection log")
	}
	if err := s.Close(t.Context()); err != nil {
		t.Fatal(err)
	}
	if s.paymentPINSelection != nil {
		t.Fatal("selection survived close")
	}
}

func TestPaymentPINDiscoveryBlocksMissingMalformedAndCancelled(t *testing.T) {
	for _, tc := range []struct {
		name  string
		flags []byte
		want  error
	}{
		{"none", bytes.Repeat([]byte{'0'}, 100), domainerror.ErrPaymentPINKeyMissing},
		{"malformed", []byte("bad"), domainerror.ErrInvalidResponse},
		{"unsupported", bytes.Repeat([]byte{'2'}, 100), domainerror.ErrPaymentPINKeyMissing},
	} {
		t.Run(tc.name, func(t *testing.T) {
			s, p, _ := paymentTestSession(t, "03", "000", response("CLO000"))
			p.reads = append([][]byte{paymentResponse("GIX", map[protocol.ResponseTag][]byte{protocol.TagDukptTDESDescription: tc.flags})}, p.reads...)
			selected, err := s.ResolvePaymentPINConfiguration(t.Context(), command.PaymentPINConfig{Method: "3", KeyIndex: 8})
			if !errors.Is(err, tc.want) || selected.Method != "" || s.paymentPINSelection != nil {
				t.Fatal("discovery failed open")
			}
		})
	}
	s, p, _ := paymentTestSession(t, "03", "000", response("CLO000"))
	before := len(p.writes)
	ctx, cancel := context.WithCancel(t.Context())
	cancel()
	if _, err := s.ResolvePaymentPINConfiguration(ctx, command.PaymentPINConfig{Method: "3", KeyIndex: 8}); !errors.Is(err, context.Canceled) || len(p.writes) != before {
		t.Fatal("cancelled discovery sent GIX")
	}
	if _, err := s.ResolvePaymentPINConfiguration(t.Context(), command.PaymentPINConfig{Method: "1", KeyIndex: 8}); err == nil || len(p.writes) != before {
		t.Fatal("invalid source sent GIX")
	}
}

func TestPaymentPINDiscoveryCancelledDuringGIX(t *testing.T) {
	s, p, _ := paymentTestSession(t, "03", "000", response("CLO000"))
	flags := bytes.Repeat([]byte{'0'}, 100)
	flags[63] = '1'
	p.reads = append([][]byte{paymentResponse("GIX", map[protocol.ResponseTag][]byte{protocol.TagDukptTDESDescription: flags})}, p.reads...)
	ctx, cancel := context.WithCancel(t.Context())
	defer cancel()
	p.cancelAfterRead = cancel
	selected, err := s.ResolvePaymentPINConfiguration(ctx, command.PaymentPINConfig{Method: "3", KeyIndex: 8})
	if err == nil || selected.Method != "" || s.paymentPINSelection != nil {
		t.Fatal("cancelled GIX published selection")
	}
}

func TestPaymentPINDiscoveryQueriesNewSession(t *testing.T) {
	s, p, _ := paymentTestSession(t, "03", "000", response("CLO000"))
	map1, map2 := bytes.Repeat([]byte{'0'}, 100), bytes.Repeat([]byte{'0'}, 100)
	map1[17] = '1'
	map2[63] = '1'
	p.reads = [][]byte{paymentResponse("GIX", map[protocol.ResponseTag][]byte{protocol.TagDukptTDESDescription: map1}), response("CLO000"), response("OPN000"),
		paymentResponse("GIX", map[protocol.ResponseTag][]byte{protocol.TagDukptTDESDescription: map2}), response("CLO000")}
	source := command.PaymentPINConfig{Method: "3", KeyIndex: 8}
	first, err := s.ResolvePaymentPINConfiguration(t.Context(), source)
	if err != nil || first.KeyIndex != 17 {
		t.Fatal("first session selection")
	}
	if err := s.Close(t.Context()); err != nil {
		t.Fatal(err)
	}
	if err := s.Open(t.Context()); err != nil {
		t.Fatal(err)
	}
	second, err := s.ResolvePaymentPINConfiguration(t.Context(), source)
	if err != nil || second.KeyIndex != 63 {
		t.Fatal("new session reused stale key")
	}
	if err := s.Close(t.Context()); err != nil {
		t.Fatal(err)
	}
}

func TestPaymentPINDiscoveryCloseDuringGIXDoesNotPublish(t *testing.T) {
	s, p, _ := paymentTestSession(t, "03", "000", response("CLO000"))
	flags := bytes.Repeat([]byte{'0'}, 100)
	flags[63] = '1'
	p.reads = append([][]byte{paymentResponse("GIX", map[protocol.ResponseTag][]byte{protocol.TagDukptTDESDescription: flags})}, p.reads...)
	// Close invalida a geração antes de recusar encerramento com comando em voo.
	p.cancelAfterRead = func() { _ = s.Close(t.Context()) }
	selected, err := s.ResolvePaymentPINConfiguration(t.Context(), command.PaymentPINConfig{Method: "3", KeyIndex: 8})
	if err == nil || selected.Method != "" || s.paymentPINSelection != nil {
		t.Fatal("close during GIX published stale selection")
	}
}
