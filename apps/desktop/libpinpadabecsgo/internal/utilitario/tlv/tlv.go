package tlv

// BerTLV representa um objeto BER-TLV, inclusive seus filhos quando construído.
type BerTLV struct {
	Tag      uint32
	Length   int
	Value    []byte
	Children []BerTLV
}
