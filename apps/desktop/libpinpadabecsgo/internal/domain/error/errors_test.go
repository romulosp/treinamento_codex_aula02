package domainerror

import "testing"

func TestStatusErrorDoesNotExposePayload(t *testing.T) {
	err := (&StatusError{Code: "012"}).Error()
	if err != "pinpad returned status 012" {
		t.Fatalf("status error = %q", err)
	}
}
