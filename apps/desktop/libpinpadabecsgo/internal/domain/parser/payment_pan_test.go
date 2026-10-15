package parser

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"testing"
)

func TestPaymentPANFromValidatedGCXSources(t *testing.T) {
	t.Parallel()
	for _, tc := range []struct {
		name  string
		card  *model.GCXResponse
		valid bool
	}{
		{name: "PAN", card: &model.GCXResponse{PAN: "1234567890123456"}, valid: true},
		{name: "track 2", card: &model.GCXResponse{Track2: "1234567890123456=2912201"}, valid: true},
		{name: "track 1", card: &model.GCXResponse{Track1: "B1234567890123456^^2912201"}, valid: true},
		{name: "coherent", card: &model.GCXResponse{PAN: "1234567890123456", Track2: "1234567890123456=2912201"}, valid: true},
		{name: "nil"},
		{name: "empty", card: &model.GCXResponse{}},
		{name: "masked", card: &model.GCXResponse{Track2: "123456******3456=2912201"}},
		{name: "conflict", card: &model.GCXResponse{PAN: "1234567890123456", Track2: "1234567890123457=2912201"}},
		{name: "masked second source", card: &model.GCXResponse{PAN: "1234567890123456", Track1: "B123456******3456^^2912201"}},
		{name: "missing separator", card: &model.GCXResponse{Track2: "12345678901234562912201"}},
		{name: "invalid suffix", card: &model.GCXResponse{Track2: "1234567890123456=291220X"}},
		{name: "short PAN", card: &model.GCXResponse{PAN: "1"}},
	} {
		t.Run(tc.name, func(t *testing.T) {
			pan, err := PaymentPAN(tc.card)
			defer clear(pan)
			if (err == nil) != tc.valid {
				t.Fatalf("valid=%v err=%v", tc.valid, err)
			}
			if tc.valid && string(pan) != "1234567890123456" {
				t.Fatal("PAN extraction mismatch")
			}
		})
	}
}
