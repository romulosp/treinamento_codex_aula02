package service

import (
	"bytes"
	"context"
	"fmt"
	"strings"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

func TestFilteredWithdrawalPreservesCandidatesAcrossFallback(t *testing.T) {
	p := &fakePort{reads: [][]byte{response("OPN000"), response("GCX081"), response("GCX081"),
		append([]byte{protocol.PP_ACK}, protocol.BuildPacket(validICCResponsePayload())...)}}
	svc := New(model.DefaultConfig(), p)
	defer svc.Shutdown()
	if err := svc.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	request := command.FilteredGCXRequest{Amount: "000000015750", Date: "261010", Clock: "150000",
		TransactionType: 1, AcquirerReference: "01", ApplicationTypes: "0102", TimeoutSeconds: 60}
	card, err := svc.PurchaseGCXFiltered(context.Background(), request, true, false)
	if err != nil || card.CardType != command.GCXCardICC {
		t.Fatalf("capture type/err = %v %v", card, err)
	}
	if len(p.writes) != 5 {
		t.Fatalf("writes=%d, want CAN/OPN/GCX/GCX/GCX", len(p.writes))
	}
	for index, packet := range p.writes[2:] {
		payload, err := protocol.ValidatePacket(packet)
		if err != nil {
			t.Fatal(err)
		}
		for _, expected := range [][]byte{{0, 0x21, 0, 1, 1}, {0, 0x10, 0, 2, '0', '1'}, {0, 0x11, 0, 4, '0', '1', '0', '2'}, {0, 0x0c, 0, 1, 60}} {
			if !bytes.Contains(payload, expected) {
				t.Fatalf("fallback %d lost a withdrawal parameter", index)
			}
		}
	}
}

func TestFormattedTableValidatesEverythingBeforeTLI(t *testing.T) {
	for _, tc := range []struct {
		name              string
		acquirer, version string
		records           []string
	}{
		{"invalid version", "01", "short", []string{"00810101"}},
		{"invalid record", "01", "TEST000001", []string{"00810101", "00920101"}},
		{"duplicate", "01", "TEST000001", []string{"00810101", "00810101"}},
	} {
		t.Run(tc.name, func(t *testing.T) {
			p := &fakePort{}
			svc := New(model.DefaultConfig(), p)
			defer svc.Shutdown()
			if err := svc.LoadFormattedEMVTable(context.Background(), tc.acquirer, tc.version, tc.records); err == nil {
				t.Fatal("invalid table accepted")
			}
			if len(p.writes) != 0 {
				t.Fatal("invalid table started physical I/O")
			}
		})
	}
	request := command.FilteredGCXRequest{Amount: "000000015750", Date: "261010", Clock: "150000", TransactionType: 1, AcquirerReference: "01", ApplicationTypes: "0101", TimeoutSeconds: 60}
	p := &fakePort{}
	svc := New(model.DefaultConfig(), p)
	defer svc.Shutdown()
	if _, err := svc.PurchaseGCXFiltered(context.Background(), request, true, false); err == nil {
		t.Fatal("duplicate filters accepted")
	}
	if len(p.writes) != 0 {
		t.Fatal("invalid filters started I/O")
	}
}

func TestFormattedTablePreservesRecordsPartitionsAndAbortsOnTLRFailure(t *testing.T) {
	for _, tc := range []struct {
		name, tlrStatus string
		wantWrites      int
		wantError       bool
	}{
		{"success after version change", "000", 6, false},
		{"failed TLR prevents TLE", "042", 4, true},
	} {
		t.Run(tc.name, func(t *testing.T) {
			p := &fakePort{reads: [][]byte{response("OPN000"), response("TLI020"), response("TLR" + tc.tlrStatus), response("TLR000"), response("TLE000")}}
			svc := New(model.DefaultConfig(), p)
			defer svc.Shutdown()
			if err := svc.Open(context.Background()); err != nil {
				t.Fatal(err)
			}
			records := []string{"60010101" + strings.Repeat("A", 592), "60020101" + strings.Repeat("B", 592)}
			err := svc.LoadFormattedEMVTable(context.Background(), "01", "TEST000001", records)
			if (err != nil) != tc.wantError {
				t.Fatalf("error=%v want failure=%v", err, tc.wantError)
			}
			if len(p.writes) != tc.wantWrites {
				t.Fatalf("writes=%d want%d", len(p.writes), tc.wantWrites)
			}
			blocks := 1
			if !tc.wantError {
				blocks = 2
			}
			for index := 0; index < blocks; index++ {
				payload, err := protocol.ValidatePacket(p.writes[3+index])
				if err != nil || string(payload) != "TLR60201"+records[index] {
					t.Fatalf("block%d changed record or length: %v", index, err)
				}
			}
			if !tc.wantError {
				payload, err := protocol.ValidatePacket(p.writes[5])
				if err != nil || !strings.HasPrefix(string(payload), "TLE") {
					t.Fatal(fmt.Errorf("missing TLE: %w", err))
				}
			}
		})
	}
}
