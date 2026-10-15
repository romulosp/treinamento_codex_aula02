package service

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"strings"
	"sync"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

func TestPaymentAuthorizationRetainsCompleteOperation(t *testing.T) {
	for _, tc := range []struct{ name, mode, decision string }{
		{"magnetic", "00", ""}, {"simulated contactless", "05", ""},
		{"chip offline", "03", "001000"}, {"contactless online", "06", "202000"},
		{"contactless denied", "06", "100000"},
	} {
		t.Run(tc.name, func(t *testing.T) {
			block, ksn := []byte{0, 17, 34, 51, 68, 85, 102, 119}, []byte{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}
			emv := []byte{0x9f, 0x27, 1, 0x80}
			final := response("GPN000036001122334455667700010203040506070809")
			if tc.decision != "" {
				values := map[protocol.ResponseTag][]byte{protocol.TagGOXResult: []byte(tc.decision), protocol.TagEMVData: emv}
				if tc.decision[2] == '2' {
					values[protocol.TagPINBlock], values[protocol.TagKSN] = block, ksn
				}
				final = paymentResponse("GOX", values)
			}
			s, p, logs := paymentTestSession(t, tc.mode, "000", final)
			values := map[protocol.ResponseTag][]byte{
				protocol.TagTrack1: bytes.Repeat([]byte{0x61}, 16), protocol.TagTrack1KSN: bytes.Repeat([]byte{0x71}, 10),
				protocol.TagTrack2: bytes.Repeat([]byte{0x62}, 8), protocol.TagTrack2KSN: bytes.Repeat([]byte{0x72}, 10),
				protocol.TagTrack3: bytes.Repeat([]byte{0x63}, 24), protocol.TagTrack3KSN: bytes.Repeat([]byte{0x73}, 10),
			}
			p.reads[1] = paymentResponse("GTK", values)
			ctx := WithOperationID(t.Context(), "owner")
			pin := command.PaymentPINConfig{Method: "3", KeyIndex: 17}
			if _, err := s.CapturePaymentCard(ctx, "owner", paymentRequest(), pin); err != nil {
				t.Fatal(err)
			}
			if _, err := s.PaymentAuthorizationSnapshot(ctx, "owner"); err == nil {
				t.Fatal("premature snapshot")
			}
			key := 7
			if err := s.PaymentTracks(ctx, "owner", command.GTKRequest{DataMethod: "30", KeyIndex: &key, Tracks: "0111"}); err != nil {
				t.Fatal(err)
			}
			if _, err := s.PaymentAuthorizationSnapshot(ctx, "owner"); err == nil {
				t.Fatal("snapshot before PIN/GOX")
			}
			if tc.decision == "" {
				// Wrapper legado limpa o resultado próprio; objeto da operação deve sobreviver.
				if err := s.CapturePaymentPIN(ctx, "owner", pin, "PIN"); err != nil {
					t.Fatal(err)
				}
			} else {
				r, err := s.ContinuePaymentEMV(ctx, "owner", paymentGOXRequest())
				if err != nil {
					t.Fatal(err)
				}
				clear(r.EMVData)
				clear(r.Result)
				clear(r.PINBlock)
				clear(r.KSN)
			}
			if _, err := s.PaymentAuthorizationSnapshot(ctx, "foreign"); err == nil {
				t.Fatal("foreign snapshot")
			}
			data, err := s.PaymentAuthorizationSnapshot(ctx, "owner")
			if err != nil {
				t.Fatal(err)
			}
			defer data.Clear()
			if data.DATAMethod() != "30" || data.DATAKeyIndex() != 7 {
				t.Fatal("DATA snapshot metadata lost")
			}
			tracks := data.Tracks()
			defer clearPaymentTracks(tracks)
			for _, pair := range []struct{ got, want []byte }{
				{tracks.Track1, values[protocol.TagTrack1]}, {tracks.Track2, values[protocol.TagTrack2]}, {tracks.Track3, values[protocol.TagTrack3]},
				{tracks.Track1KSN, values[protocol.TagTrack1KSN]}, {tracks.Track2KSN, values[protocol.TagTrack2KSN]}, {tracks.Track3KSN, values[protocol.TagTrack3KSN]},
			} {
				if !bytes.Equal(pair.got, pair.want) {
					t.Fatal("GTK payload lost")
				}
			}
			if tc.decision == "" || tc.decision[2] == '2' {
				if !bytes.Equal(data.PINBlock(), block) || !bytes.Equal(data.KSN(), ksn) {
					t.Fatal("PIN/KSN lost")
				}
			} else if len(data.PINBlock()) != 0 || len(data.KSN()) != 0 {
				t.Fatal("offline fabricated PIN")
			}
			if tc.decision != "" && (!bytes.Equal(data.EMVData(), emv) || string(data.EMVResult()) != tc.decision) {
				t.Fatal("GOX data lost")
			}
			encoded, err := json.Marshal(data)
			if err != nil || string(encoded) != "{}" || fmt.Sprint(data) != "PaymentAuthorizationData[private]" || fmt.Sprintf("%#v", data) != "PaymentAuthorizationData[private]" {
				t.Fatal("automatic representation exposes data")
			}
			tracks.Track2[0] = 0
			again := data.Tracks()
			if again.Track2[0] != 0x62 {
				t.Fatal("getter aliases snapshot")
			}
			clearPaymentTracks(again)
			data.Clear()
			data.Clear()
			if data.Tracks() != nil || len(data.PINBlock()) != 0 {
				t.Fatal("Clear ineffective")
			}
			// Clear do snapshot não deve apagar o objeto da operação.
			second, err := s.PaymentAuthorizationSnapshot(ctx, "owner")
			if err != nil {
				t.Fatal(err)
			}
			secondTracks := second.Tracks()
			if secondTracks.Track2[0] != 0x62 {
				t.Fatal("snapshot aliases operation")
			}
			second.Clear()
			clearPaymentTracks(secondTracks)
			controlled := [][]byte{s.payment.tracks.Track2, s.payment.pinBlock, s.payment.ksn, s.payment.emvData, s.payment.emvResult}
			if err := s.Close(ctx); err != nil {
				t.Fatal(err)
			}
			for _, b := range controlled {
				if !bytes.Equal(b, make([]byte, len(b))) {
					t.Fatal("close did not overwrite retained data")
				}
			}
			if _, err := s.PaymentAuthorizationSnapshot(ctx, "owner"); err == nil {
				t.Fatal("snapshot after close")
			}
			if strings.Contains(logs.String(), "1234567890123456") || strings.Contains(logs.String(), "0011223344556677") || strings.Contains(logs.String(), "62626262") {
				t.Fatal("private data logged")
			}
		})
	}
}

func TestPaymentAuthorizationFailureNeverPublishes(t *testing.T) {
	for _, status := range []string{"011", "042"} {
		t.Run(status, func(t *testing.T) {
			s, _, _ := paymentTestSession(t, "03", status, response("CLO000"))
			if _, err := s.CapturePaymentCard(t.Context(), "owner", paymentRequest(), command.PaymentPINConfig{Method: "3", KeyIndex: 17}); err != nil {
				t.Fatal(err)
			}
			key := 7
			if err := s.PaymentTracks(t.Context(), "owner", command.GTKRequest{DataMethod: "30", KeyIndex: &key}); err == nil {
				t.Fatal("GTK failure accepted")
			}
			if data, err := s.PaymentAuthorizationSnapshot(t.Context(), "owner"); err == nil || data != nil {
				t.Fatal("failed GTK published data")
			}
		})
	}
}

func TestPaymentAuthorizationLateTracksAreOverwritten(t *testing.T) {
	s, _, _ := paymentTestSession(t, "03", "000", response("CLO000"))
	if _, err := s.CapturePaymentCard(t.Context(), "owner", paymentRequest(), command.PaymentPINConfig{Method: "3", KeyIndex: 17}); err != nil {
		t.Fatal(err)
	}
	epoch := s.paymentEpoch
	s.InvalidatePaymentContext()
	late := &model.GTKResponse{Track2: []byte{1, 2, 3}, Track2KSN: bytes.Repeat([]byte{7}, 10)}
	if err := s.retainPaymentTracks(t.Context(), epoch, late, command.GTKRequest{}); err == nil {
		t.Fatal("late GTK accepted")
	}
	if !bytes.Equal(late.Track2, make([]byte, 3)) || !bytes.Equal(late.Track2KSN, make([]byte, 10)) {
		t.Fatal("late buffers not cleared")
	}
	if _, err := s.PaymentAuthorizationSnapshot(t.Context(), "owner"); err == nil {
		t.Fatal("late response restored operation")
	}
	canceled, cancel := context.WithCancel(t.Context())
	cancel()
	if _, err := s.PaymentAuthorizationSnapshot(canceled, "owner"); err == nil {
		t.Fatal("canceled query accepted")
	}
}

func TestPaymentAuthorizationSnapshotOwnership(t *testing.T) {
	d := &PaymentAuthorizationData{tracks: &model.GTKResponse{
		EncryptedPAN: []byte{1}, EncryptedPANKey: []byte{2}, EncryptedRandom: []byte{3},
		Track1: []byte{4}, Track2: []byte{5}, Track3: []byte{6},
		Track1KSN: []byte{7}, Track2KSN: []byte{8}, Track3KSN: []byte{9}},
		pinBlock: []byte{10}, ksn: []byte{11}, emvData: []byte{12}, emvResult: []byte{13}}
	owned := d.tracks
	all := [][]byte{owned.EncryptedPAN, owned.EncryptedPANKey, owned.EncryptedRandom, owned.Track1, owned.Track2, owned.Track3,
		owned.Track1KSN, owned.Track2KSN, owned.Track3KSN, d.pinBlock, d.ksn, d.emvData, d.emvResult}
	var workers sync.WaitGroup
	for range 8 {
		workers.Go(func() {
			for range 10 {
				tracks := d.Tracks()
				clearPaymentTracks(tracks)
				clear(d.PINBlock())
				clear(d.KSN())
				clear(d.EMVData())
				clear(d.EMVResult())
				_ = d.DATAMethod()
				_ = d.DATAKeyIndex()
				d.Clear()
			}
		})
	}
	workers.Wait()
	for _, b := range all {
		if b[0] != 0 {
			t.Fatal("owned buffer not overwritten")
		}
	}
	var empty *PaymentAuthorizationData
	empty.Clear()
	if empty.Tracks() != nil || empty.PINBlock() != nil || empty.KSN() != nil || empty.EMVData() != nil || empty.EMVResult() != nil || empty.DATAMethod() != "" || empty.DATAKeyIndex() != -1 {
		t.Fatal("nil receiver unsafe")
	}
}

func TestPaymentAuthorizationFinalizationFailureClearsOperation(t *testing.T) {
	for _, name := range []string{"invalid request", "device error"} {
		t.Run(name, func(t *testing.T) {
			gox := paymentResponse("GOX", map[protocol.ResponseTag][]byte{
				protocol.TagGOXResult: []byte("202000"), protocol.TagPINBlock: make([]byte, 8), protocol.TagKSN: make([]byte, 10),
			})
			s, p, _ := paymentTestSession(t, "03", "000", gox)
			ctx := WithOperationID(t.Context(), "owner")
			if _, err := s.CapturePaymentCard(ctx, "owner", paymentRequest(), command.PaymentPINConfig{Method: "3", KeyIndex: 17}); err != nil {
				t.Fatal(err)
			}
			key := 7
			if err := s.PaymentTracks(ctx, "owner", command.GTKRequest{DataMethod: "30", KeyIndex: &key}); err != nil {
				t.Fatal(err)
			}
			if _, err := s.ContinuePaymentEMV(ctx, "owner", paymentGOXRequest()); err != nil {
				t.Fatal(err)
			}
			controlled := s.payment.tracks.Track2
			request := command.FCXRequest{Options: "0000", Authorization: "00"}
			if name == "invalid request" {
				request.Options = "invalid"
			} else {
				p.reads = append([][]byte{response("FCX011")}, p.reads...)
			}
			if _, err := s.FinalizeEMV(ctx, request); err == nil {
				t.Fatal("FCX error accepted")
			}
			if _, err := s.PaymentAuthorizationSnapshot(ctx, "owner"); err == nil {
				t.Fatal("failed finalization retained operation")
			}
			if !bytes.Equal(controlled, make([]byte, len(controlled))) {
				t.Fatal("failed finalization did not overwrite GTK")
			}
		})
	}
}
