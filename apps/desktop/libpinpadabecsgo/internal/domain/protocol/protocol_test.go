package protocol

import "testing"

func TestSubstitutionRoundTrip(t *testing.T) {
	for _, input := range [][]byte{{}, {0x13}, {0x16}, {0x17}, {0x10, 0x13, 0x16, 0x17}} {
		escaped := ApplySubstitution(input)
		got, err := RemoveSubstitution(escaped)
		if err != nil {
			t.Fatal(err)
		}
		if string(got) != string(input) {
			t.Fatalf("got %x want %x", got, input)
		}
	}
}
func TestBuildAndValidatePacket(t *testing.T) {
	input := []byte("GIX000")
	got, err := ValidatePacket(BuildPacket(input))
	if err != nil {
		t.Fatal(err)
	}
	if string(got) != string(input) {
		t.Fatalf("got %q", got)
	}
}
