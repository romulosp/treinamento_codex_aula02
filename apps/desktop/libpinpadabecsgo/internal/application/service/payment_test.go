package service

import (
	"bytes"
	"context"
	"encoding/binary"
	"fmt"
	"io"
	"log/slog"
	"strings"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

func paymentResponse(kind string, values map[protocol.ResponseTag][]byte) []byte {
	data := []byte{}
	for tag, value := range values {
		data = binary.BigEndian.AppendUint16(data, uint16(tag))
		data = binary.BigEndian.AppendUint16(data, uint16(len(value)))
		data = append(data, value...)
	}
	return append([]byte{protocol.PP_ACK}, protocol.BuildPacket(append([]byte(fmt.Sprintf("%s000%03d", kind, len(data))), data...))...)
}

func paymentCardResponse(mode string) []byte {
	values := map[protocol.ResponseTag][]byte{protocol.TagCardType: []byte(mode)}
	if mode == "00" {
		values[protocol.TagICCStatus] = []byte("1")
	} else {
		values[protocol.TagAIDTableInfo] = []byte("010101")
		values[protocol.TagLabel] = []byte("TEST")
	}
	if mode == "03" || mode == "06" {
		values[protocol.TagPAN] = []byte("1234567890123456")
		values[protocol.TagPANSequenceNumber] = []byte("00")
	} else {
		values[protocol.TagTrack2Included] = []byte("1234567890123456=2912201")
	}
	return paymentResponse("GCX", values)
}

func paymentGTKResponse() []byte {
	return paymentResponse("GTK", map[protocol.ResponseTag][]byte{
		protocol.TagTrack2: bytes.Repeat([]byte{0x12}, 8), protocol.TagTrack2KSN: make([]byte, 10),
	})
}

func paymentTestSession(t *testing.T, mode, gtkStatus string, final []byte) (*Service, *fakePort, *bytes.Buffer) {
	t.Helper()
	gtk := paymentGTKResponse()
	if gtkStatus != "000" {
		gtk = response("GTK" + gtkStatus)
	}
	p := &fakePort{reads: [][]byte{response("OPN000"), response("TLI000"), response("TLR000"), response("TLE000"), paymentCardResponse(mode), gtk, final, response("CLO000")}}
	s := New(model.DefaultConfig(), p)
	t.Cleanup(s.Shutdown)
	logs := new(bytes.Buffer)
	s.SetLogger(slog.New(slog.NewJSONHandler(logs, nil)))
	if err := s.Open(t.Context()); err != nil {
		t.Fatal(err)
	}
	record := "2841010107A0000000990101" + strings.Repeat("0", 18) + "01" + strings.Repeat("0", 240)
	if err := s.LoadFormattedEMVTable(t.Context(), "01", "TEST000001", []string{record}); err != nil {
		t.Fatal(err)
	}
	return s, p, logs
}

func paymentRequest() command.FilteredGCXRequest {
	return command.FilteredGCXRequest{Amount: "000000001000", Date: "261012", Clock: "210000", TransactionType: 0, AcquirerReference: "01", ApplicationTypes: "01", TimeoutSeconds: 30}
}

func paymentGOXRequest() command.GOXRequest {
	return command.GOXRequest{AcquirerReference: "01", PinMethod: "3", KeyIndex: 17, Amount: "000000001000", TransactionType: []byte{0}, Options: "01000"}
}

func TestPaymentSequenceByModeAndSingleUse(t *testing.T) {
	for _, mode := range []string{"00", "03", "05", "06"} {
		t.Run(mode, func(t *testing.T) {
			final := response("GPN000036001122334455667700010203040506070809")
			if mode == "03" || mode == "06" {
				final = paymentResponse("GOX", map[protocol.ResponseTag][]byte{protocol.TagGOXResult: []byte("000000")})
			}
			s, p, logs := paymentTestSession(t, mode, "000", final)
			ctx := WithOperationID(t.Context(), "payment-test")
			pin := command.PaymentPINConfig{Method: "3", KeyIndex: 17}
			card, err := s.CapturePaymentCard(ctx, "payment-test", paymentRequest(), pin)
			if err != nil || card.CardType != mode {
				t.Fatalf("mode=%s err=%v", mode, err)
			}
			key := 7
			if err = s.PaymentTracks(ctx, "payment-test", command.GTKRequest{DataMethod: "30", KeyIndex: &key, Tracks: "0111"}); err != nil {
				t.Fatal(err)
			}
			if mode == "03" || mode == "06" {
				if _, err = s.ContinuePaymentEMV(ctx, "payment-test", paymentGOXRequest()); err != nil {
					t.Fatal(err)
				}
			} else {
				if err = s.CapturePaymentPIN(ctx, "payment-test", pin, "PIN"); err != nil {
					t.Fatal(err)
				}
				payload, err := protocol.ValidatePacket(p.writes[len(p.writes)-1])
				if err != nil {
					t.Fatal(err)
				}
				if !strings.Contains(string(payload), "161234567890123456") || !strings.HasPrefix(string(payload), "GPN093317") {
					t.Fatal("explicit private PAN/configuration not used")
				}
			}
			before := len(p.writes)
			if err = s.CapturePaymentPIN(ctx, "payment-test", pin, "PIN"); err == nil {
				t.Fatal("repeat GPN accepted")
			}
			if _, err = s.CapturePaymentCard(ctx, "payment-test", paymentRequest(), pin); err == nil {
				t.Fatal("repeat capture accepted")
			}
			if len(p.writes) != before {
				t.Fatal("invalid transition sent I/O")
			}
			if strings.Contains(logs.String(), "1234567890123456") || strings.Contains(logs.String(), "0011223344556677") {
				t.Fatal("sensitive data logged")
			}
			if !strings.Contains(logs.String(), `"correlationId":"payment-test"`) {
				t.Fatal("missing correlation")
			}
			if err = s.Close(ctx); err != nil {
				t.Fatal(err)
			}
		})
	}
}

func TestPaymentGTKFailureAndContextIsolation(t *testing.T) {
	for _, tc := range []struct{ name, mode, status string }{
		{name: "chip GTK key error", mode: "03", status: "042"},
		{name: "magnetic GTK key error", mode: "00", status: "042"},
		{name: "foreign correlation", mode: "05", status: "000"},
		{name: "explicit invalidation", mode: "00", status: "000"},
	} {
		t.Run(tc.name, func(t *testing.T) {
			s, p, logs := paymentTestSession(t, tc.mode, tc.status, response("CLO000"))
			pin := command.PaymentPINConfig{Method: "3", KeyIndex: 17}
			ctx := WithOperationID(t.Context(), "owner")
			if _, err := s.CapturePaymentCard(ctx, "owner", paymentRequest(), pin); err != nil {
				t.Fatal(err)
			}
			key := 7
			id := "owner"
			if tc.name == "foreign correlation" {
				id = "foreign"
			}
			if tc.name == "explicit invalidation" {
				s.InvalidatePaymentContext()
			}
			err := s.PaymentTracks(ctx, id, command.GTKRequest{DataMethod: "30", KeyIndex: &key})
			if err == nil {
				t.Fatal("invalid/failed GTK accepted")
			}
			before := len(p.writes)
			if err = s.CapturePaymentPIN(ctx, "owner", pin, "PIN"); err == nil {
				t.Fatal("invalid context accepted")
			}
			if len(p.writes) != before {
				t.Fatal("GPN after error sent")
			}
			if tc.status == "042" && (!strings.Contains(logs.String(), `"comando":"GTK"`) || !strings.Contains(logs.String(), `"status":"042"`)) {
				t.Fatal("missing sanitized GTK diagnosis")
			}
		})
	}
}

func TestPaymentCancelledContextDoesNotSendNextCommand(t *testing.T) {
	s, p, _ := paymentTestSession(t, "00", "000", response("CLO000"))
	ctx, cancel := context.WithCancel(t.Context())
	pin := command.PaymentPINConfig{Method: "3", KeyIndex: 17}
	if _, err := s.CapturePaymentCard(ctx, "owner", paymentRequest(), pin); err != nil {
		t.Fatal(err)
	}
	cancel()
	before := len(p.writes)
	key := 7
	if err := s.PaymentTracks(ctx, "owner", command.GTKRequest{DataMethod: "30", KeyIndex: &key}); err == nil {
		t.Fatal("cancelled GTK accepted")
	}
	if len(p.writes) != before {
		t.Fatal("cancelled command reached transport")
	}
}

func TestPaymentFallbackPreservesPurchaseAndModality(t *testing.T) {
	for _, tc := range []struct {
		name     string
		statuses []string
		options  string
	}{
		{name: "communication twice", statuses: []string{"081", "081"}, options: "00000"},
		{name: "multiple", statuses: []string{"080"}, options: "10000"},
		{name: "external CVM", statuses: []string{"086"}, options: "10000"},
		{name: "invalidated", statuses: []string{"082"}, options: "00000"},
		{name: "problems", statuses: []string{"083"}, options: "00000"},
		{name: "application unavailable", statuses: []string{"084"}, options: "00000"},
		{name: "application unauthorized", statuses: []string{"085"}, options: "00000"},
		{name: "interface change", statuses: []string{"087"}, options: "00000"},
	} {
		t.Run(tc.name, func(t *testing.T) {
			s, p, logs := paymentTestSession(t, "06", "000", response("CLO000"))
			prefix := [][]byte{}
			for _, status := range tc.statuses {
				prefix = append(prefix, response("GCX"+status))
				if status == "086" {
					prefix = append(prefix, response("DEX000"))
				}
			}
			p.reads = append(prefix, p.reads...)
			before := len(p.writes)
			ctx := WithOperationID(t.Context(), "fallback-test")
			if _, err := s.CapturePaymentCard(ctx, "fallback-test", paymentRequest(), command.PaymentPINConfig{Method: "3", KeyIndex: 17}); err != nil {
				t.Fatal(err)
			}
			gcx := [][]byte{}
			for _, frame := range p.writes[before:] {
				payload, err := protocol.ValidatePacket(frame)
				if err != nil {
					t.Fatal(err)
				}
				if strings.HasPrefix(string(payload), "GCX") {
					gcx = append(gcx, payload)
				}
			}
			if len(gcx) != len(tc.statuses)+1 {
				t.Fatal("unexpected retries")
			}
			for _, payload := range gcx {
				for _, field := range [][]byte{{0, 0x21, 0, 1, 0}, {0, 0x11, 0, 2, '0', '1'}, {0, 0x10, 0, 2, '0', '1'}} {
					if !bytes.Contains(payload, field) {
						t.Fatal("purchase or filters lost")
					}
				}
				if !strings.Contains(string(payload), "000000001000") || !strings.Contains(string(payload), "261012") || !strings.Contains(string(payload), "210000") {
					t.Fatal("snapshot lost")
				}
			}
			if !strings.Contains(string(gcx[len(gcx)-1]), tc.options) {
				t.Fatal("wrong fallback options")
			}
			if !strings.Contains(logs.String(), `"msg":"fallback GCX"`) {
				t.Fatal("missing fallback metadata")
			}
			if tc.name == "external CVM" && !strings.Contains(logs.String(), `"comando":"DEX"`) {
				t.Fatal("missing external CVM instruction")
			}
		})
	}
}

func TestPaymentRejectsChangedSnapshotAndWrongModes(t *testing.T) {
	for _, tc := range []struct {
		name, mode string
		pin        command.PaymentPINConfig
		mutate     func(*command.GOXRequest)
		gpn        bool
	}{
		{name: "GOX for magnetic", mode: "00"},
		{name: "GOX for simulated CTLS", mode: "05"},
		{name: "GPN for chip", mode: "03", gpn: true, pin: command.PaymentPINConfig{Method: "3", KeyIndex: 17}},
		{name: "GPN changed index", mode: "00", gpn: true, pin: command.PaymentPINConfig{Method: "3", KeyIndex: 18}},
		{name: "GPN invalid profile", mode: "00", gpn: true, pin: command.PaymentPINConfig{Method: "1", KeyIndex: 17}},
		{name: "GOX changed amount", mode: "03", mutate: func(r *command.GOXRequest) { r.Amount = "000000002000" }},
		{name: "GOX changed index", mode: "06", mutate: func(r *command.GOXRequest) { r.KeyIndex = 18 }},
		{name: "GOX withdrawal", mode: "03", mutate: func(r *command.GOXRequest) { r.TransactionType = []byte{1} }},
	} {
		t.Run(tc.name, func(t *testing.T) {
			s, p, _ := paymentTestSession(t, tc.mode, "000", response("CLO000"))
			ctx := WithOperationID(t.Context(), "owner")
			if _, err := s.CapturePaymentCard(ctx, "owner", paymentRequest(), command.PaymentPINConfig{Method: "3", KeyIndex: 17}); err != nil {
				t.Fatal(err)
			}
			key := 7
			if err := s.PaymentTracks(ctx, "owner", command.GTKRequest{DataMethod: "30", KeyIndex: &key}); err != nil {
				t.Fatal(err)
			}
			before := len(p.writes)
			if tc.gpn {
				if err := s.CapturePaymentPIN(ctx, "owner", tc.pin, "PIN"); err == nil {
					t.Fatal("invalid GPN accepted")
				}
			} else {
				request := paymentGOXRequest()
				if tc.mutate != nil {
					tc.mutate(&request)
				}
				if _, err := s.ContinuePaymentEMV(ctx, "owner", request); err == nil {
					t.Fatal("invalid GOX accepted")
				}
			}
			if len(p.writes) != before {
				t.Fatal("invalid continuation sent command")
			}
		})
	}
}

func TestPaymentCaptureGuardsAndIncoherentCards(t *testing.T) {
	for _, tc := range []struct {
		name, mode, id string
		mutate         func(*command.FilteredGCXRequest)
		pin            command.PaymentPINConfig
		reply          []byte
		noIO           bool
	}{
		{name: "invalid PIN", mode: "03", id: "owner", pin: command.PaymentPINConfig{Method: "1", KeyIndex: 17}, noIO: true},
		{name: "empty correlation", mode: "03", pin: command.PaymentPINConfig{Method: "3", KeyIndex: 17}, noIO: true},
		{name: "withdrawal", mode: "03", id: "owner", pin: command.PaymentPINConfig{Method: "3", KeyIndex: 17}, mutate: func(r *command.FilteredGCXRequest) { r.TransactionType = 1 }, noIO: true},
		{name: "invalid timeout", mode: "03", id: "owner", pin: command.PaymentPINConfig{Method: "3", KeyIndex: 17}, mutate: func(r *command.FilteredGCXRequest) { r.TimeoutSeconds = 0 }, noIO: true},
		{name: "no candidate", mode: "03", id: "owner", pin: command.PaymentPINConfig{Method: "3", KeyIndex: 17}, mutate: func(r *command.FilteredGCXRequest) { r.ApplicationTypes = "02" }, noIO: true},
		{name: "PAN absent", mode: "00", id: "owner", pin: command.PaymentPINConfig{Method: "3", KeyIndex: 17}, reply: paymentResponse("GCX", map[protocol.ResponseTag][]byte{protocol.TagCardType: []byte("00"), protocol.TagICCStatus: []byte("0")})},
		{name: "unexpected selected application", mode: "03", id: "owner", pin: command.PaymentPINConfig{Method: "3", KeyIndex: 17}, reply: paymentResponse("GCX", map[protocol.ResponseTag][]byte{protocol.TagCardType: []byte("03"), protocol.TagAIDTableInfo: []byte("010102"), protocol.TagLabel: []byte("TEST"), protocol.TagPAN: []byte("1234567890123456"), protocol.TagPANSequenceNumber: []byte("00")})},
	} {
		t.Run(tc.name, func(t *testing.T) {
			s, p, _ := paymentTestSession(t, tc.mode, "000", response("CLO000"))
			if tc.reply != nil {
				p.reads[0] = tc.reply
			}
			request := paymentRequest()
			if tc.mutate != nil {
				tc.mutate(&request)
			}
			before := len(p.writes)
			if _, err := s.CapturePaymentCard(t.Context(), tc.id, request, tc.pin); err == nil {
				t.Fatal("invalid capture accepted")
			}
			if tc.noIO && len(p.writes) != before {
				t.Fatal("invalid configuration started capture")
			}
			before = len(p.writes)
			key := 7
			if err := s.PaymentTracks(t.Context(), tc.id, command.GTKRequest{DataMethod: "30", KeyIndex: &key}); err == nil {
				t.Fatal("failed capture advanced to GTK")
			}
			if len(p.writes) != before {
				t.Fatal("failed capture sent GTK")
			}
		})
	}
}

func TestPaymentInvalidGPNMessageCannotReuseContext(t *testing.T) {
	s, p, _ := paymentTestSession(t, "00", "000", response("CLO000"))
	ctx := WithOperationID(t.Context(), "owner")
	pin := command.PaymentPINConfig{Method: "3", KeyIndex: 17}
	if _, err := s.CapturePaymentCard(ctx, "owner", paymentRequest(), pin); err != nil {
		t.Fatal(err)
	}
	key := 7
	if err := s.PaymentTracks(ctx, "owner", command.GTKRequest{DataMethod: "30", KeyIndex: &key}); err != nil {
		t.Fatal(err)
	}
	before := len(p.writes)
	if err := s.CapturePaymentPIN(ctx, "owner", pin, strings.Repeat("x", 33)); err == nil {
		t.Fatal("invalid message accepted")
	}
	if err := s.CapturePaymentPIN(ctx, "owner", pin, "PIN"); err == nil {
		t.Fatal("failed context reused")
	}
	if len(p.writes) != before {
		t.Fatal("invalid request sent GPN")
	}
}

func TestPaymentResetInvalidatesPrivateCapture(t *testing.T) {
	s, p, _ := paymentTestSession(t, "00", "000", response("CLO000"))
	ctx := WithOperationID(t.Context(), "owner")
	if _, err := s.CapturePaymentCard(ctx, "owner", paymentRequest(), command.PaymentPINConfig{Method: "3", KeyIndex: 17}); err != nil {
		t.Fatal(err)
	}
	if err := s.Reset(ctx); err != nil {
		t.Fatal(err)
	}
	before := len(p.writes)
	key := 7
	if err := s.PaymentTracks(ctx, "owner", command.GTKRequest{DataMethod: "30", KeyIndex: &key}); err == nil {
		t.Fatal("reset capture reused")
	}
	if len(p.writes) != before {
		t.Fatal("reset capture sent GTK")
	}
}

func TestPaymentTransportLossDoesNotConfirmClose(t *testing.T) {
	s, p, _ := paymentTestSession(t, "03", "000", response("CLO000"))
	ctx := WithOperationID(t.Context(), "owner")
	if _, err := s.CapturePaymentCard(ctx, "owner", paymentRequest(), command.PaymentPINConfig{Method: "3", KeyIndex: 17}); err != nil {
		t.Fatal(err)
	}
	// Injeta falha de reconexão com transporte fechado, mesma condição observável de desconexão.
	s.failProtocolReconnect(io.EOF, true)
	before := len(p.writes)
	if err := s.Close(ctx); err == nil {
		t.Fatal("socket release falsely confirmed device close")
	}
	if err := s.Close(ctx); err == nil {
		t.Fatal("repeat close hid unconfirmed state")
	}
	if len(p.writes) != before {
		t.Fatal("unavailable transport received close")
	}
}
