package tlv

type BerTLV struct {
	Tag      uint32
	Length   int
	Value    []byte
	Children []BerTLV
}
