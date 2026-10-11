package mobile

import (
	"errors"
	"strings"
	"testing"

	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/state"
)

func TestFormattedJSONAllowsOnlyWholeLineComments(t *testing.T) {
	records, err := parseFormattedEMVJSON("\uFEFF// ambiente\n[\"01110101ABC\", \"01220101ABCD\"]\n // fim")
	if err != nil || len(records) != 2 || records[0] != "01110101ABC" {
		t.Fatalf("parse=%v %v", records, err)
	}
	for _, tc := range []struct{ name, value string }{{"object", "{}"}, {"null", "null"}, {"empty", "[]"}, {"inline comment", "[\"00810101\"] // comment"}, {"numeric item", "[1]"}, {"oversize", strings.Repeat("x", 1024*1024+1)}} {
		t.Run(tc.name, func(t *testing.T) {
			if _, err := parseFormattedEMVJSON(tc.value); err == nil {
				t.Fatal("invalid JSON accepted")
			}
		})
	}
}

func TestValidateFormattedEMVTableWithoutClient(t *testing.T) {
	t.Parallel()
	cases := []struct {
		name, acquirer, version, table string
		valid                          bool
	}{
		{"valid with comments", "01", "TEST000001", "// DES\n[\"01110101ABC\",\"01220101ABCD\"]", true},
		{"empty", "01", "TEST000001", "[]", false},
		{"invalid JSON", "01", "TEST000001", "broken", false},
		{"wrong acquirer", "02", "TEST000001", "[\"01110101ABC\"]", false},
		{"wrong length", "01", "TEST000001", "[\"01010101ABC\"]", false},
		{"duplicate", "01", "TEST000001", "[\"01110101ABC\",\"01110101ABC\"]", false},
		{"invalid version", "01", "short", "[\"01110101ABC\"]", false},
		{"invalid index", "01", "TEST000001", "[\"01110100ABC\"]", false},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			err := ValidateFormattedEMVTableJSON(tc.acquirer, tc.version, tc.table)
			if (err == nil) != tc.valid {
				t.Fatalf("valid=%v, error=%v", tc.valid, err)
			}
			if err != nil && strings.Contains(err.Error(), "ABC") {
				t.Fatal("record exposed")
			}
		})
	}
}

func TestPhysicalCancelAndTimeoutUseSafeCategories(t *testing.T) {
	for _, tc := range []struct{ name, status, code string }{{"cancel", state.StatusCancel, "CANCELED"}, {"timeout", state.StatusTimeout, "TIMEOUT"}, {"other", state.StatusKeyError, "PINPAD_ERROR"}} {
		t.Run(tc.name, func(t *testing.T) {
			cause := &domainerror.StatusError{Code: tc.status}
			err := publicFailure("command", cause)
			if ErrorCode(err.Error()) != tc.code || !errors.Is(err, cause) {
				t.Fatalf("category=%v", err)
			}
		})
	}
}
