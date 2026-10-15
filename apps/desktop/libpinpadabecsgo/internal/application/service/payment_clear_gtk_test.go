package service

import (
	"bytes"
	"strings"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

func TestPaymentClearGTKPreservesTracksAndEncryptedPIN(t *testing.T) {
	for _, mode := range []string{"00", "03", "05", "06"} {
		t.Run(mode, func(t *testing.T) {
			final := response("GPN000036001122334455667700010203040506070809")
			if mode == "03" || mode == "06" {
				final = paymentResponse("GOX", map[protocol.ResponseTag][]byte{
					protocol.TagGOXResult: []byte("202000"), protocol.TagPINBlock: []byte{0, 17, 34, 51, 68, 85, 102, 119}, protocol.TagKSN: []byte{0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
				})
			}
			s, p, logs := paymentTestSession(t, mode, "000", final)
			plain := []byte("1234567890123456=2912201")
			p.reads[1] = paymentResponse("GTK", map[protocol.ResponseTag][]byte{protocol.TagTrack2: plain})
			pin := command.PaymentPINConfig{Method: "3", KeyIndex: 17}
			if _, err := s.CapturePaymentCard(t.Context(), "owner", paymentRequest(), pin); err != nil {
				t.Fatal(err)
			}
			if err := s.PaymentTracks(t.Context(), "owner", command.GTKRequest{Tracks: "0111"}); err != nil {
				t.Fatal(err)
			}
			payload, err := protocol.ValidatePacket(p.writes[len(p.writes)-1])
			if err != nil {
				t.Fatal(err)
			}
			want := []byte{'G', 'T', 'K', '0', '0', '8', 0, 7, 0, 4, '0', '1', '1', '1'}
			if !bytes.Equal(payload, want) {
				t.Fatal("clear GTK sent encryption fields")
			}
			if mode == "03" || mode == "06" {
				result, err := s.ContinuePaymentEMV(t.Context(), "owner", paymentGOXRequest())
				if err != nil {
					t.Fatal(err)
				}
				clear(result.PINBlock)
				clear(result.KSN)
			} else if err := s.CapturePaymentPIN(t.Context(), "owner", pin, "PIN"); err != nil {
				t.Fatal(err)
			}
			data, err := s.PaymentAuthorizationSnapshot(t.Context(), "owner")
			if err != nil {
				t.Fatal(err)
			}
			defer data.Clear()
			tracks := data.Tracks()
			defer clearPaymentTracks(tracks)
			if !bytes.Equal(tracks.Track2, plain) || len(tracks.Track2KSN) != 0 || data.DATAMethod() != "" || data.DATAKeyIndex() != -1 {
				t.Fatal("clear GTK lost/altered tracks or invented DATA KSN")
			}
			if len(data.PINBlock()) != 8 || len(data.KSN()) != 10 {
				t.Fatal("clear GTK altered encrypted PIN contract")
			}
			if strings.Contains(logs.String(), string(plain)) || strings.Contains(logs.String(), "1234567890123456") {
				t.Fatal("clear card data logged")
			}
			if err := s.Close(t.Context()); err != nil {
				t.Fatal(err)
			}
		})
	}
}

func TestPaymentGTKRejectsImplicitEncryptionChoices(t *testing.T) {
	key := 7
	for _, tc := range []struct {
		name    string
		request command.GTKRequest
	}{
		{"clear with key", command.GTKRequest{KeyIndex: &key}},
		{"DUKPT without key", command.GTKRequest{DataMethod: "30"}},
		{"unsupported method", command.GTKRequest{DataMethod: "00", KeyIndex: &key}},
		{"clear with working key", command.GTKRequest{WorkingKey: []byte{1}}},
	} {
		t.Run(tc.name, func(t *testing.T) {
			s, p, _ := paymentTestSession(t, "03", "000", response("CLO000"))
			if _, err := s.CapturePaymentCard(t.Context(), "owner", paymentRequest(), command.PaymentPINConfig{Method: "3", KeyIndex: 17}); err != nil {
				t.Fatal(err)
			}
			before := len(p.writes)
			if err := s.PaymentTracks(t.Context(), "owner", tc.request); err == nil {
				t.Fatal("invalid GTK choice accepted")
			}
			if len(p.writes) != before {
				t.Fatal("invalid GTK reached transport")
			}
			if _, err := s.ContinuePaymentEMV(t.Context(), "owner", paymentGOXRequest()); err == nil {
				t.Fatal("invalid GTK downgraded or advanced")
			}
		})
	}
}
