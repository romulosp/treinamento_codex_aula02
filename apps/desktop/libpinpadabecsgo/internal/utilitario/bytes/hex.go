package bytesutil

import (
	"encoding/hex"
	"fmt"
	"strings"
)

// FromHex converte texto hexadecimal em bytes, aceitando espaços e prefixo 0x.
func FromHex(value string) ([]byte, error) {
	clean := strings.ReplaceAll(strings.ReplaceAll(strings.TrimSpace(value), " ", ""), "0x", "")
	if len(clean)%2 != 0 {
		return nil, fmt.Errorf("hex string has odd length")
	}
	out, err := hex.DecodeString(clean)
	if err != nil {
		return nil, fmt.Errorf("decode hex: %w", err)
	}
	return out, nil
}

// ToHex converte bytes em texto hexadecimal maiúsculo.
func ToHex(value []byte) string { return strings.ToUpper(hex.EncodeToString(value)) }
