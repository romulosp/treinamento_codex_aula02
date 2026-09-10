package parser

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/utilitario/tlv"
	"fmt"
)

func ParseBerTLV(data []byte) ([]tlv.BerTLV, error) {
	out := make([]tlv.BerTLV, 0)
	for len(data) > 0 {
		item, n, err := parseOne(data)
		if err != nil {
			return nil, err
		}
		out = append(out, item)
		data = data[n:]
	}
	return out, nil
}
func parseOne(data []byte) (tlv.BerTLV, int, error) {
	if len(data) < 2 {
		return tlv.BerTLV{}, 0, fmt.Errorf("truncated ber tag")
	}
	pos := 0
	tag := uint32(data[pos])
	pos++
	if tag&0x1f == 0x1f {
		tag = 0
		for {
			if pos >= len(data) {
				return tlv.BerTLV{}, 0, fmt.Errorf("truncated long ber tag")
			}
			b := data[pos]
			pos++
			tag = (tag << 7) | uint32(b&0x7f)
			if b&0x80 == 0 {
				break
			}
		}
	}
	if pos >= len(data) {
		return tlv.BerTLV{}, 0, fmt.Errorf("missing ber length")
	}
	first := data[pos]
	pos++
	length := 0
	if first&0x80 == 0 {
		length = int(first)
	} else {
		count := int(first & 0x7f)
		if count == 0 || count > 4 || pos+count > len(data) {
			return tlv.BerTLV{}, 0, fmt.Errorf("invalid ber length")
		}
		for _, b := range data[pos : pos+count] {
			length = length<<8 | int(b)
		}
		pos += count
	}
	if length < 0 || pos+length > len(data) {
		return tlv.BerTLV{}, 0, fmt.Errorf("truncated ber value")
	}
	value := append([]byte(nil), data[pos:pos+length]...)
	total := pos + length
	item := tlv.BerTLV{Tag: tag, Length: length, Value: value}
	if data[0]&0x20 != 0 {
		children, err := ParseBerTLV(value)
		if err != nil {
			return tlv.BerTLV{}, 0, err
		}
		item.Children = children
	}
	return item, total, nil
}
