package mobile

import (
	"strings"
	"testing"
)

func TestPaymentPINPreflightAndSummaryDoNotExposeSource(t *testing.T) {
	t.Parallel()
	raw := "TIPO_CHAVE=3\nINDICE_CHAVE=17\nWORKING_KEY=\"\""
	if err := ValidatePaymentPINProperties(raw); err != nil {
		t.Fatal(err)
	}
	summary, err := PaymentPINConfigurationSummaryJSON(raw)
	if err != nil || summary != `{"method":"3","keyIndex":17}` {
		t.Fatalf("invalid summary: %v", err)
	}
	invalid := raw + "\nSECRET_SENTINEL=synthetic"
	if err = ValidatePaymentPINProperties(invalid); err == nil || strings.Contains(err.Error(), "SECRET_SENTINEL") {
		t.Fatal("invalid source accepted/exposed")
	}
	if summary, err = PaymentPINConfigurationSummaryJSON(invalid); err == nil || summary != "{}" {
		t.Fatal("invalid source returned summary")
	}
}

func TestPaymentAPIsRejectMissingSessionBeforeNetwork(t *testing.T) {
	client, err := NewClient("localhost", 39100, 1000)
	if err != nil {
		t.Fatal(err)
	}
	for _, tc := range []struct {
		name string
		call func() error
	}{
		{name: "capture invalid PIN", call: func() error {
			_, err := client.CapturePaymentCardSummaryJSON("test", "000000001000", "261012", "210000", "01", "01", "1", 17, "", 30)
			return err
		}},
		{name: "capture without session", call: func() error {
			_, err := client.CapturePaymentCardSummaryJSON("test", "000000001000", "261012", "210000", "01", "01", "3", 17, "", 30)
			return err
		}},
		{name: "GTK", call: func() error { _, err := client.GetPaymentTracksSummaryJSON("test", "30", 7); return err }},
		{name: "GOX", call: func() error {
			_, err := client.ContinuePaymentEMVSummaryJSON("test", "01", "000000001000", "3", 17, "", 30)
			return err
		}},
		{name: "GOX invalid PIN", call: func() error {
			_, err := client.ContinuePaymentEMVSummaryJSON("test", "01", "000000001000", "1", 17, "", 30)
			return err
		}},
		{name: "GPN", call: func() error { return client.CapturePaymentPIN("test", "3", 17, "", "PIN") }},
	} {
		t.Run(tc.name, func(t *testing.T) {
			err := tc.call()
			if err == nil || err.Error() != "BINDING_ERROR:command" {
				t.Fatalf("unexpected category: %v", err)
			}
			if client.GetState() != "CLOSED" {
				t.Fatal("invalid sequence opened session")
			}
		})
	}
}
