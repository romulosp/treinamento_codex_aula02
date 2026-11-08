package logging

import (
	"fmt"
	"strings"
)

// hexDump formata bytes em linhas de hex + ASCII com offset hexadecimal.
// bytesPerLine <= 0 assume 16.
func hexDump(data []byte, bytesPerLine int) string {
	if bytesPerLine <= 0 {
		bytesPerLine = 16
	}
	const hexDigits = "0123456789ABCDEF"
	var builder strings.Builder
	for offset := 0; offset < len(data); offset += bytesPerLine {
		end := offset + bytesPerLine
		if end > len(data) {
			end = len(data)
		}
		fmt.Fprintf(&builder, "%04X  ", offset)
		for i := offset; i < end; i++ {
			b := data[i]
			builder.WriteByte(hexDigits[b>>4])
			builder.WriteByte(hexDigits[b&0x0F])
			builder.WriteByte(' ')
		}
		builder.WriteString(" |")
		for i := offset; i < end; i++ {
			builder.WriteByte(printableByte(data[i]))
		}
		builder.WriteByte('|')
		if end < len(data) {
			builder.WriteByte('\n')
		}
	}
	return builder.String()
}

// printableByte devolve o byte ASCII visível ou '.' para não imprimíveis.
func printableByte(b byte) byte {
	if b >= 0x20 && b <= 0x7E {
		return b
	}
	return '.'
}
