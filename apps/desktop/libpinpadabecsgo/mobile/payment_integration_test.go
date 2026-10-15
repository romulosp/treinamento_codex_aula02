//go:build integration

package mobile

import (
	"bytes"
	"context"
	"encoding/binary"
	"encoding/json"
	"fmt"
	"strings"
	"testing"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridge"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/ownership"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/serial"
)

func paymentWireResponse(kind string, values map[protocol.ResponseTag][]byte) []byte {
	data := []byte{}
	for tag, value := range values {
		data = binary.BigEndian.AppendUint16(data, uint16(tag))
		data = binary.BigEndian.AppendUint16(data, uint16(len(value)))
		data = append(data, value...)
	}
	return append([]byte(fmt.Sprintf("%s000%03d", kind, len(data))), data...)
}

func TestPaymentBindingThroughOpaqueBridge(t *testing.T) { exercisePaymentBinding(t, false, "30") }
func TestPaymentPrivateBindingThroughOpaqueBridge(t *testing.T) {
	exercisePaymentBinding(t, true, "30")
}
func TestPaymentClearGTKBindingThroughOpaqueBridge(t *testing.T) {
	exercisePaymentBinding(t, false, "")
}
func TestPaymentClearGTKPrivateBindingThroughOpaqueBridge(t *testing.T) {
	exercisePaymentBinding(t, true, "")
}

func TestPaymentDiscoveredPINPrivateBindingThroughOpaqueBridge(t *testing.T) {
	exercisePaymentBinding(t, true, "", true)
}
func TestPaymentDiscoveredPINLegacyBindingThroughOpaqueBridge(t *testing.T) {
	exercisePaymentBinding(t, false, "30", true)
}

func exercisePaymentBinding(t *testing.T, privateResult bool, dataMethod string, discovery ...bool) {
	discover := len(discovery) > 0 && discovery[0]
	for _, tc := range []struct{ name, mode, gox, gtkStatus string }{
		{name: "magnetic", mode: "00", gtkStatus: "000"},
		{name: "CTLS simulated", mode: "05", gtkStatus: "000"},
		{name: "chip offline", mode: "03", gox: "000000", gtkStatus: "000"},
		{name: "CTLS denied", mode: "06", gox: "100000", gtkStatus: "000"},
		{name: "CTLS online PIN inside GOX", mode: "06", gox: "202000", gtkStatus: "000"},
		{name: "GTK fails before GOX", mode: "03", gtkStatus: "042"},
		{name: "GTK invalid parameter before GOX", mode: "03", gtkStatus: "011"},
	} {
		t.Run(tc.name, func(t *testing.T) {
			steps := []serial.ScriptStep{{ExpectedWrite: []byte{protocol.PP_CAN}, Reads: [][]byte{{protocol.PP_EOT}}}}
			add := func(payload, reply []byte) {
				steps = append(steps, serial.ScriptStep{ExpectedWrite: protocol.BuildPacket(payload), Reads: [][]byte{{protocol.PP_ACK}, protocol.BuildPacket(reply)}})
			}
			add([]byte("OPN"), []byte("OPN000"))
			if discover {
				flags := bytes.Repeat([]byte{'0'}, 100)
				flags[17] = '1'
				add([]byte("GIX000"), paymentWireResponse("GIX", map[protocol.ResponseTag][]byte{protocol.TagDukptTDESDescription: flags,
					protocol.TagDukptTDESData: bytes.Repeat([]byte{'1'}, 100)}))
			}
			record := "2841010107A0000000990101" + strings.Repeat("0", 18) + "01" + strings.Repeat("0", 240)
			tli, err := command.BuildTLICommand("01", "TEST000001")
			if err != nil {
				t.Fatal(err)
			}
			tlr, err := command.BuildFormattedTLRCommand([]string{record})
			if err != nil {
				t.Fatal(err)
			}
			add(tli, []byte("TLI000"))
			add(tlr, []byte("TLR000"))
			add(command.BuildTLECommand(), []byte("TLE000"))
			request := command.FilteredGCXRequest{Amount: "000000001000", Date: "261012", Clock: "210000", Options: 0x10, TransactionType: 0, AcquirerReference: "01", ApplicationTypes: "01", TimeoutSeconds: 30}
			gcx, err := command.BuildFilteredGCXCommand(request)
			if err != nil {
				t.Fatal(err)
			}
			card := map[protocol.ResponseTag][]byte{protocol.TagCardType: []byte(tc.mode)}
			if tc.mode == "00" {
				card[protocol.TagICCStatus] = []byte("0")
			} else {
				card[protocol.TagAIDTableInfo] = []byte("010101")
				card[protocol.TagLabel] = []byte("TEST")
			}
			if tc.mode == "03" || tc.mode == "06" {
				card[protocol.TagPAN] = []byte("1234567890123456")
				card[protocol.TagPANSequenceNumber] = []byte("00")
			} else {
				card[protocol.TagTrack2Included] = []byte("1234567890123456=2912201")
			}
			add(gcx, paymentWireResponse("GCX", card))
			key := 7
			gtk, err := command.BuildGTKCommand(command.GTKRequest{DataMethod: "30", Tracks: "0111", KeyIndex: &key})
			if err != nil {
				t.Fatal(err)
			}
			if dataMethod == "" {
				// Vetor literal: somente SPE_TRACKS; sem método, índice ou WK.
				gtk = []byte{'G', 'T', 'K', '0', '0', '8', 0, 7, 0, 4, '0', '1', '1', '1'}
			}
			gtkResponse := []byte("GTK" + tc.gtkStatus)
			if tc.gtkStatus == "000" {
				values := map[protocol.ResponseTag][]byte{protocol.TagTrack2: bytes.Repeat([]byte{0x62}, 8)}
				if dataMethod == "30" {
					values[protocol.TagTrack2KSN] = bytes.Repeat([]byte{0x72}, 10)
				}
				gtkResponse = paymentWireResponse("GTK", values)
			}
			add(gtk, gtkResponse)
			if tc.gtkStatus == "000" {
				if tc.mode == "00" || tc.mode == "05" {
					gpn, err := command.BuildGPNCommandDUKPT(17, "1234567890123456", "PIN")
					if err != nil {
						t.Fatal(err)
					}
					add(gpn, []byte("GPN000036001122334455667700010203040506070809"))
				} else {
					timeout := byte(30)
					gox, err := command.BuildGOXCommand(command.GOXRequest{AcquirerReference: "01", PinMethod: "3", KeyIndex: 17, TransactionType: []byte{0}, Amount: request.Amount, Options: "01000", Timeout: &timeout})
					if err != nil {
						t.Fatal(err)
					}
					result := map[protocol.ResponseTag][]byte{protocol.TagGOXResult: []byte(tc.gox)}
					if tc.gox == "202000" {
						result[protocol.TagPINBlock] = []byte{1, 2, 3, 4, 5, 6, 7, 8}
						result[protocol.TagKSN] = make([]byte, 10)
					}
					add(gox, paymentWireResponse("GOX", result))
					if tc.gox[0] == '2' {
						fcx, err := command.BuildFCXCommand(command.FCXRequest{Options: "0000", Authorization: "00", Timeout: &timeout})
						if err != nil {
							t.Fatal(err)
						}
						add(fcx, paymentWireResponse("FCX", map[protocol.ResponseTag][]byte{protocol.TagFCXResult: []byte("000")}))
					}
				}
			}
			add(command.BuildPacketPayload(command.CommandCLO, strings.Repeat(" ", 32)), []byte("CLO000"))
			transport := serial.NewScriptedTransport(steps)
			ready := make(chan struct{})
			port := freeTCPPort(t)
			server, err := bridge.New(bridge.Config{Bind: "127.0.0.1", Port: port, SerialName: "SYNTHETIC-10011", Mode: "scripted", Transport: transport, Ownership: ownership.New(), OnListening: func(string) { close(ready) }})
			if err != nil {
				t.Fatal(err)
			}
			ctx, cancel := context.WithCancel(t.Context())
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
				t.Fatal("bridge startup timeout")
			}
			client, err := NewClient("127.0.0.1", port, 5000)
			if err != nil {
				t.Fatal(err)
			}
			defer client.Close("cleanup")
			if err = client.Open("open"); err != nil {
				t.Fatal(err)
			}
			if discover {
				selected, callErr := client.ResolvePaymentPINConfiguration("resolve", "3", 8, "")
				if callErr != nil || selected.KeyIndex() != 17 || selected.Method() != "3" || selected.WorkingKey() != "" {
					t.Fatal("GIX did not resolve source8 to PIN17")
				}
			}
			table, err := json.Marshal([]string{record})
			if err != nil {
				t.Fatal(err)
			}
			if err = ValidatePaymentCandidatesJSON("01", "01", string(table)); err != nil {
				t.Fatal(err)
			}
			if err = client.LoadFormattedEMVTableJSON("table", "01", "TEST000001", string(table)); err != nil {
				t.Fatal(err)
			}
			if discover {
				selected, callErr := client.ResolvePaymentPINConfiguration("cached", "3", 8, "")
				if callErr != nil || selected.KeyIndex() != 17 {
					t.Fatal("table load rerandomized selection")
				}
			}
			cardSummary, err := client.CapturePaymentCardSummaryJSON("payment", request.Amount, request.Date, request.Clock, "01", "01", "3", 17, "", 30)
			if err != nil {
				t.Fatal(err)
			}
			for _, marker := range []string{"1234567890123456", "track2", "pan"} {
				if strings.Contains(cardSummary, marker) {
					t.Fatal("card summary exposed sensitive field")
				}
			}
			tracksSummary, err := client.GetPaymentTracksSummaryJSON("payment", dataMethod, 7)
			if err == nil && tracksSummary != `{"schemaVersion":"1"}` {
				t.Fatal("GTK summary exposed card data")
			}
			if tc.gtkStatus != "000" {
				if err == nil || err.Error() != "PINPAD_ERROR:command" {
					t.Fatalf("GTK failure category=%v", err)
				}
			} else {
				if err != nil {
					t.Fatal(err)
				}
				if tc.mode == "00" || tc.mode == "05" {
					if privateResult {
						result, callErr := client.CapturePaymentPINResult("payment", "3", 17, "", "PIN")
						if callErr != nil {
							t.Fatal(callErr)
						}
						if fmt.Sprintf("%x", result.PINBlock()) != "0011223344556677" || fmt.Sprintf("%x", result.KSN()) != "00010203040506070809" {
							t.Fatal("PIN pair changed")
						}
						result.Clear()
					} else if err = client.CapturePaymentPIN("payment", "3", 17, "", "PIN"); err != nil {
						t.Fatal(err)
					}
					assertPaymentAuthorizationThroughBinding(t, client, dataMethod)
				} else {
					if privateResult {
						result, callErr := client.ContinuePaymentEMVResult("payment", "01", request.Amount, "3", 17, "", 30)
						if callErr != nil {
							t.Fatal(callErr)
						}
						if result.Result() != tc.gox {
							t.Fatal("decision changed")
						}
						if tc.gox == "202000" {
							if fmt.Sprintf("%x", result.PINBlock()) != "0102030405060708" || len(result.KSN()) != 10 {
								t.Fatal("online PIN pair")
							}
						} else if len(result.PINBlock()) != 0 || len(result.KSN()) != 0 {
							t.Fatal("offline invented PIN")
						}
						result.Clear()
					} else {
						summary, err := client.ContinuePaymentEMVSummaryJSON("payment", "01", request.Amount, "3", 17, "", 30)
						if err != nil {
							t.Fatal(err)
						}
						if summary != fmt.Sprintf(`{"schemaVersion":"1","result":"%s"}`, tc.gox) {
							t.Fatal("summary changed/exposed")
						}
					}
					assertPaymentAuthorizationThroughBinding(t, client, dataMethod)
					if tc.gox[0] == '2' {
						if _, err = client.FinalizeEMVSummaryJSON("payment", "0000", "00", "", "", 30); err != nil {
							t.Fatal(err)
						}
					}
				}
			}
			if err = client.Close("close"); err != nil {
				t.Fatal(err)
			}
			if !transport.Completed() {
				t.Fatal("sequence did not complete")
			}
		})
	}
}

func assertPaymentAuthorizationThroughBinding(t *testing.T, client *Client, dataMethod string) {
	t.Helper()
	data, err := client.service.PaymentAuthorizationSnapshot(t.Context(), "payment")
	if err != nil {
		t.Fatal(err)
	}
	defer data.Clear()
	tracks := data.Tracks()
	defer clear(tracks.Track2)
	defer clear(tracks.Track2KSN)
	expectedKey, expectedKSN := -1, []byte(nil)
	if dataMethod == "30" {
		expectedKey, expectedKSN = 7, bytes.Repeat([]byte{0x72}, 10)
	}
	if !bytes.Equal(tracks.Track2, bytes.Repeat([]byte{0x62}, 8)) || !bytes.Equal(tracks.Track2KSN, expectedKSN) || data.DATAMethod() != dataMethod || data.DATAKeyIndex() != expectedKey {
		t.Fatal("binding discarded GTK before future authorization")
	}
}

func TestPaymentPINDiscoveryFailureThroughOpaqueBridge(t *testing.T) {
	for _, tc := range []struct {
		name  string
		flags []byte
		code  string
	}{
		{"no keys", bytes.Repeat([]byte{'0'}, 100), "PIN_KEY_MISSING:preflight"},
		{"bad map", []byte("bad"), "PROTOCOL_ERROR:preflight"},
	} {
		t.Run(tc.name, func(t *testing.T) {
			steps := []serial.ScriptStep{{ExpectedWrite: []byte{protocol.PP_CAN}, Reads: [][]byte{{protocol.PP_EOT}}}}
			for _, pair := range []struct{ request, reply []byte }{
				{[]byte("OPN"), []byte("OPN000")},
				{[]byte("GIX000"), paymentWireResponse("GIX", map[protocol.ResponseTag][]byte{protocol.TagDukptTDESDescription: tc.flags})},
				{command.BuildPacketPayload(command.CommandCLO, strings.Repeat(" ", 32)), []byte("CLO000")},
			} {
				steps = append(steps, serial.ScriptStep{ExpectedWrite: protocol.BuildPacket(pair.request), Reads: [][]byte{{protocol.PP_ACK}, protocol.BuildPacket(pair.reply)}})
			}
			transport := serial.NewScriptedTransport(steps)
			ready := make(chan struct{})
			port := freeTCPPort(t)
			server, err := bridge.New(bridge.Config{Bind: "127.0.0.1", Port: port, SerialName: "SYNTHETIC-10016", Mode: "scripted", Transport: transport, Ownership: ownership.New(), OnListening: func(string) { close(ready) }})
			if err != nil {
				t.Fatal(err)
			}
			ctx, cancel := context.WithCancel(t.Context())
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
				t.Fatal("bridge timeout")
			}
			client, err := NewClient("127.0.0.1", port, 5000)
			if err != nil {
				t.Fatal(err)
			}
			defer client.Close("cleanup")
			if err = client.Open("open"); err != nil {
				t.Fatal(err)
			}
			selected, err := client.ResolvePaymentPINConfiguration("resolve", "3", 8, "")
			if err == nil || err.Error() != tc.code || selected != nil {
				t.Fatalf("wrong discovery failure: %v", err)
			}
			if err = client.Close("close"); err != nil {
				t.Fatal(err)
			}
			if !transport.Completed() {
				t.Fatal("discovery failure sent extra commands or failed cleanup")
			}
		})
	}
}
