package tlv

import "testing"

func TestBerTLVStoresValueAndChildren(t *testing.T) {
	value := BerTLV{Tag: 0xE1, Length: 1, Value: []byte{1}, Children: []BerTLV{{Tag: 0x80, Length: 1, Value: []byte{2}}}}
	if value.Tag != 0xE1 || value.Children[0].Tag != 0x80 {
		t.Fatalf("TLV = %#v", value)
	}
}
