package command

import (
	"fmt"
	"strings"
	"testing"
)

func TestPaymentPINProperties(t *testing.T) {
	t.Parallel()
	for _, tc := range []struct {
		name, raw string
		valid     bool
	}{
		{name: "empty WK", raw: "TIPO_CHAVE=3\nINDICE_CHAVE=17\nWORKING_KEY=", valid: true},
		{name: "BOM comments and separators", raw: "\uFEFF!test\r\n # synthetic\n TIPO_CHAVE : 3 \n INDICE_CHAVE=17\n WORKING_KEY = \"\"\r\n", valid: true},
		{name: "missing WK", raw: "TIPO_CHAVE=3\nINDICE_CHAVE=17"},
		{name: "duplicate", raw: "TIPO_CHAVE=3\nTIPO_CHAVE=3\nINDICE_CHAVE=17\nWORKING_KEY="},
		{name: "unknown", raw: "TIPO_CHAVE=3\nINDICE_CHAVE=17\nWORKING_KEY=\nSECRET_SENTINEL=synthetic"},
		{name: "wrong case", raw: "tipo_chave=3\nINDICE_CHAVE=17\nWORKING_KEY="},
		{name: "wrong method", raw: "TIPO_CHAVE=1\nINDICE_CHAVE=17\nWORKING_KEY="},
		{name: "working key rejected", raw: "TIPO_CHAVE=3\nINDICE_CHAVE=17\nWORKING_KEY=SECRET_SENTINEL"},
		{name: "index not N2", raw: "TIPO_CHAVE=3\nINDICE_CHAVE=7\nWORKING_KEY="},
		{name: "index overflow", raw: "TIPO_CHAVE=3\nINDICE_CHAVE=100\nWORKING_KEY="},
		{name: "syntax", raw: "TIPO_CHAVE=3\nINDICE_CHAVE=17\nWORKING_KEY=\nSECRET_SENTINEL"},
		{name: "oversize", raw: strings.Repeat("#", 65537)},
		{name: "invalid UTF8", raw: "#\xff\nTIPO_CHAVE=3\nINDICE_CHAVE=17\nWORKING_KEY="},
	} {
		t.Run(tc.name, func(t *testing.T) {
			config, err := ParsePaymentPINProperties(tc.raw)
			if (err == nil) != tc.valid {
				t.Fatalf("valid=%v, error=%v", tc.valid, err)
			}
			if tc.valid && (config.Method != "3" || config.KeyIndex != 17 || config.WorkingKey != "") {
				t.Fatal("snapshot mismatch")
			}
			if !tc.valid && config != (PaymentPINConfig{}) {
				t.Fatal("invalid source returned configuration")
			}
			if err != nil && strings.Contains(err.Error(), "SECRET_SENTINEL") {
				t.Fatal("input exposed")
			}
		})
	}
}

func syntheticPaymentAID(index, app, aid string) string {
	return "284101" + index + fmt.Sprintf("%02d", len(aid)/2) + aid + strings.Repeat("0", 32-len(aid)) + app + strings.Repeat("0", 240)
}

func TestPaymentCandidatesPreserveBaseAndExtendedAIDs(t *testing.T) {
	t.Parallel()
	records := []string{
		syntheticPaymentAID("01", "01", "A0000000990101"), syntheticPaymentAID("02", "01", "A000000099010101"),
		syntheticPaymentAID("03", "02", "A0000000990201"), syntheticPaymentAID("04", "02", "A000000099020101"),
	}
	for _, app := range []string{"01", "02", "0102"} {
		t.Run(app, func(t *testing.T) {
			if err := ValidatePaymentCandidates(records, "01", app); err != nil {
				t.Fatal(err)
			}
			for _, record := range records {
				payload, err := BuildFormattedTLRCommand([]string{record})
				if err != nil {
					t.Fatal(err)
				}
				if !strings.Contains(string(payload), record) {
					t.Fatal("record identity/length lost")
				}
			}
		})
	}
	for _, tc := range []struct {
		name, app string
		records   []string
	}{
		{name: "no candidate", app: "03", records: records},
		{name: "duplicate filter", app: "0101", records: records},
		{name: "reserved filter", app: "99", records: records},
		{name: "short record", app: "01", records: []string{"00810101"}},
		{name: "invalid AID", app: "01", records: []string{syntheticPaymentAID("01", "01", "Z0000000990101")}},
	} {
		t.Run(tc.name, func(t *testing.T) {
			if ValidatePaymentCandidates(tc.records, "01", tc.app) == nil {
				t.Fatal("invalid candidates accepted")
			}
		})
	}
}
