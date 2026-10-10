package command

import (
	"bytes"
	"fmt"
	"strings"
	"testing"
)

func TestFilteredGCXEncodesWithdrawalAndCandidates(t *testing.T) {
	request := FilteredGCXRequest{Amount: "000000015750", Date: "261010", Clock: "151000", TransactionType: 1, AcquirerReference: "01", ApplicationTypes: "0102", TimeoutSeconds: 60}
	payload, err := BuildFilteredGCXCommand(request)
	if err != nil {
		t.Fatal(err)
	}
	for _, want := range [][]byte{{0, 0x21, 0, 1, 1}, {0, 0x10, 0, 2, '0', '1'}, {0, 0x11, 0, 4, '0', '1', '0', '2'}, {0, 0x0c, 0, 1, 60}} {
		if !bytes.Contains(payload, want) {
			t.Fatalf("parameter absent: %x", want)
		}
	}
	request.AcquirerReference = ""
	request.ApplicationTypes = ""
	request.AIDReferences = "010101AZ"
	payload, err = BuildFilteredGCXCommand(request)
	if err != nil || !bytes.Contains(payload, append([]byte{0, 0x12, 0, 8}, []byte("010101AZ")...)) {
		t.Fatalf("references: %x %v", payload, err)
	}
}

func TestFilteredGCXRejectsInvalidFiltersBeforeIO(t *testing.T) {
	valid := FilteredGCXRequest{Amount: "000000015750", Date: "261010", Clock: "151000", TransactionType: 1, AcquirerReference: "01", ApplicationTypes: "01", TimeoutSeconds: 60}
	cases := []struct {
		name   string
		change func(*FilteredGCXRequest)
	}{
		{"empty types", func(r *FilteredGCXRequest) { r.ApplicationTypes = "" }},
		{"duplicate types", func(r *FilteredGCXRequest) { r.ApplicationTypes = "0101" }},
		{"reserved type", func(r *FilteredGCXRequest) { r.ApplicationTypes = "99" }},
		{"odd types", func(r *FilteredGCXRequest) { r.ApplicationTypes = "001" }},
		{"mixed strategies", func(r *FilteredGCXRequest) { r.AIDReferences = "0101" }},
		{"bad reference", func(r *FilteredGCXRequest) {
			r.AcquirerReference = ""
			r.ApplicationTypes = ""
			r.AIDReferences = "0100"
		}},
		{"duplicate reference", func(r *FilteredGCXRequest) {
			r.AcquirerReference = ""
			r.ApplicationTypes = ""
			r.AIDReferences = "01010101"
		}},
		{"invalid acquire", func(r *FilteredGCXRequest) { r.AcquirerReference = "00" }},
		{"overflow byte", func(r *FilteredGCXRequest) { r.TransactionType = 256 }},
		{"negative byte", func(r *FilteredGCXRequest) { r.TransactionType = -1 }},
		{"timeout zero", func(r *FilteredGCXRequest) { r.TimeoutSeconds = 0 }},
		{"bad date", func(r *FilteredGCXRequest) { r.Date = "260231" }},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			r := valid
			tc.change(&r)
			if _, err := BuildFilteredGCXCommand(r); err == nil {
				t.Fatal("invalid request accepted")
			}
		})
	}
}

func TestFormattedTLRPreservesInclusiveLength(t *testing.T) {
	records := []string{"01110101ABC", "01220101ABCD"}
	if err := ValidateFormattedEMVRecords(records, "01"); err != nil {
		t.Fatal(err)
	}
	actual, err := BuildFormattedTLRCommand(records)
	if err != nil {
		t.Fatal(err)
	}
	if want := []byte("TLR02502" + strings.Join(records, "")); !bytes.Equal(actual, want) {
		t.Fatalf("TLR=%q want%q", actual, want)
	}
	latin := []string{"00910101é"}
	actual, err = BuildFormattedTLRCommand(latin)
	if err != nil || actual[len(actual)-1] != 0xe9 {
		t.Fatalf("Latin1=%x err%v", actual, err)
	}
}

func TestFormattedRecordsRejectMalformedTables(t *testing.T) {
	cases := []struct {
		name    string
		records []string
		acq     string
	}{
		{"empty", nil, "01"}, {"duplicate", []string{"00810101", "00810101"}, "01"},
		{"length mismatch", []string{"00910101"}, "01"}, {"kind", []string{"00830101"}, "01"},
		{"acquirer mismatch", []string{"00810201"}, "01"}, {"index", []string{"00810100"}, "01"},
		{"unicode", []string{"00910101€"}, "01"}, {"short", []string{"001"}, "01"},
		{"bad prefix", []string{"ABC10101"}, "01"}, {"invalid acquirer", []string{"00810001"}, "00"},
		{"too large", []string{"99810101" + strings.Repeat("A", 990)}, "01"},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			if err := ValidateFormattedEMVRecords(tc.records, tc.acq); err == nil {
				t.Fatal("invalid table accepted")
			}
		})
	}
	for _, tc := range []struct {
		name    string
		records []string
	}{{"empty", nil}, {"count", make([]string, 100)}, {"badprefix", []string{"xxx10101"}}, {"short", []string{"003"}}, {"unicode", []string{"00910101€"}}, {"overflow", []string{"60010101" + strings.Repeat("A", 592), "60020101" + strings.Repeat("B", 592)}}} {
		t.Run(tc.name, func(t *testing.T) {
			if _, err := BuildFormattedTLRCommand(tc.records); err == nil {
				t.Fatal("invalid block accepted")
			}
		})
	}
	maximum := fmt.Sprintf("99710101%s", strings.Repeat("A", 989))
	if _, err := BuildFormattedTLRCommand([]string{maximum}); err != nil {
		t.Fatal(err)
	}
}
