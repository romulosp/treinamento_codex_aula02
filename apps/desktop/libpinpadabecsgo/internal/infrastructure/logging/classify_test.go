package logging

import (
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

func TestClassify(t *testing.T) {
	tests := []struct {
		name   string
		kind   command.Type
		data   []byte
		redact bool
		want   FrameClass
	}{
		{name: "dsp visível", kind: command.CommandDSP, data: []byte{0x16, 0x44}, redact: false, want: ClassVisible},
		{name: "gox sensível", kind: command.CommandGOX, data: []byte{0x16, 0x44}, redact: false, want: ClassSensitive},
		{name: "gtk sensível", kind: command.CommandGTK, data: []byte{0x16, 0x44}, redact: false, want: ClassSensitive},
		{name: "redação forçada", kind: command.CommandDSP, data: []byte{0x16, 0x44}, redact: true, want: ClassSensitive},
		{name: "criptografado DC2", kind: command.CommandDSP, data: []byte{protocol.PP_DC2, 0x00}, redact: false, want: ClassEncrypted},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := classify(tt.kind, tt.data, tt.redact); got != tt.want {
				t.Fatalf("classify() = %v, want %v", got, tt.want)
			}
		})
	}
}
