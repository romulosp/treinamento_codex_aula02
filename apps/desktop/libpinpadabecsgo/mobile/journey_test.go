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
