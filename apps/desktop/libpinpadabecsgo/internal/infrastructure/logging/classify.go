package logging

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

// FrameClass classifica um frame para decidir o que pode ser apresentado.
type FrameClass int

const (
	ClassVisible   FrameClass = iota // A: HEX + ASCII + interpretação por campo
	ClassSensitive                   // B: envelope + REDACTED
	ClassEncrypted                   // C: envelope + OPACO
)

// classify decide a classe de um frame. O sinal redact já agrega a política do
// serviço (forceRedaction || secureSession || traceSensitive[kind]); isSensitive
// e o prefixo DC2 são defesa em profundidade.
func classify(kind command.Type, data []byte, redact bool) FrameClass {
	if redact || isSensitive(kind) {
		return ClassSensitive
	}
	if len(data) > 0 && data[0] == protocol.PP_DC2 {
		return ClassEncrypted
	}
	return ClassVisible
}
