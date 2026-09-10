package crc

import "testing"

func TestCRCEmpty(t *testing.T) {
	if CRC16CCITT(nil) != 0 {
		t.Fatal("empty CRC must be zero")
	}
}
func TestCRCKnown(t *testing.T) {
	if CRC16CCITT([]byte("123456789")) != 0x31C3 {
		t.Fatalf("unexpected CRC: %04X", CRC16CCITT([]byte("123456789")))
	}
}
