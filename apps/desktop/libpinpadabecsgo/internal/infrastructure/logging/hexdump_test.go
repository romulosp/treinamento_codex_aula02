package logging

import "testing"

func TestHexDump(t *testing.T) {
	tests := []struct {
		name string
		data []byte
		want string
	}{
		{name: "curto", data: []byte{0x16, 0x44, 0x53}, want: "0000  16 44 53  |.DS|"},
		{name: "vazio", data: nil, want: ""},
		{
			name: "quebra de linha",
			data: []byte{
				0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
				0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
				0x41,
			},
			want: "0000  00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F  |................|\n0010  41  |A|",
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := hexDump(tt.data, 16); got != tt.want {
				t.Fatalf("hexDump() = %q, want %q", got, tt.want)
			}
		})
	}
}
