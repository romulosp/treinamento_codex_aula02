package bytesutil

import "testing"

func TestHexRoundTrip(t *testing.T) {
	got, err := FromHex("0x16 17")
	if err != nil || ToHex(got) != "1617" {
		t.Fatalf("got %x %v", got, err)
	}
}
