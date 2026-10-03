package parser

import "testing"

func TestParseBerTLVNesting(t *testing.T) {
	got, err := ParseBerTLV([]byte{0xE1, 0x03, 0x5A, 0x01, 0x01})
	if err != nil {
		t.Fatal(err)
	}
	if len(got) != 1 || len(got[0].Children) != 1 {
		t.Fatalf("unexpected %#v", got)
	}
}
func TestParseBerTLVRejectsTruncated(t *testing.T) {
	if _, err := ParseBerTLV([]byte{0x5A, 0x02, 0x01}); err == nil {
		t.Fatal("expected error")
	}
}
