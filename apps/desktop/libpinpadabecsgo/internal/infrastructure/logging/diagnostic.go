package logging

import (
	"strconv"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

// safeDiagnosticFrame valida o envelope e os metadados dos campos antes de
// permitir qualquer representação HEX/ASCII, inclusive do pacote inteiro.
func safeDiagnosticFrame(kind command.Type, packet []byte, response bool) bool {
	if _, known := commandCatalog[kind]; !known || isSensitive(kind) {
		return false
	}
	body, err := protocol.ValidatePacket(packet)
	defer clear(body)
	if err != nil || len(body) < 3 || len(body) > protocol.PacketDataMaxSize || string(body[:3]) != string(kind) {
		return false
	}
	if response {
		if len(body) < 6 || !diagnosticDigits(body[3:6]) {
			return false
		}
		if kind == command.CommandOPN && len(body) != 6 {
			return false
		}
		if len(body) > 6 && !(kind == command.CommandGKY && len(body) == 8) && !diagnosticTLVSafe(body, 6, true) {
			return false
		}
	} else {
		switch kind {
		case command.CommandOPN:
			if len(body) != 3 {
				return false
			}
		case command.CommandGKY, command.CommandLMF, command.CommandMLE, command.CommandTLE:
			if len(body) != 3 {
				return false
			}
		case command.CommandDSP, command.CommandDEX, command.CommandCLO, command.CommandTLI:
			// Esquemas posicionais validados pelo decoder abaixo.
		default:
			if !diagnosticTLVSafe(body, 3, false) {
				return false
			}
		}
	}
	frame, err := decodeFrame(kind, packet, response)
	if err != nil || !frame.crc.valid {
		return false
	}
	for _, field := range frame.fields {
		if field.sensitive || field.name == "DESCONHECIDO" {
			return false
		}
	}
	return true
}

// diagnosticTLVSafe inspeciona apenas IDs e comprimentos, sem interpretar nem
// formatar valores de campos sensíveis/desconhecidos.
func diagnosticTLVSafe(body []byte, pos int, response bool) bool {
	for pos < len(body) {
		if pos+3 > len(body) || !diagnosticDigits(body[pos:pos+3]) {
			return false
		}
		size, _ := strconv.Atoi(string(body[pos : pos+3]))
		pos += 3
		end := pos + size
		if end > len(body) {
			return false
		}
		for pos < end {
			if pos+4 > end {
				return false
			}
			id := uint16(body[pos])<<8 | uint16(body[pos+1])
			size := int(body[pos+2])<<8 | int(body[pos+3])
			if response {
				meta, known := rspCatalog[protocol.ResponseTag(id)]
				if !known || meta.sensitive {
					return false
				}
			} else {
				meta, known := speCatalog[command.SPEParameter(id)]
				if !known || meta.sensitive {
					return false
				}
			}
			pos += 4 + size
			if pos > end {
				return false
			}
		}
	}
	return true
}

func diagnosticDigits(data []byte) bool {
	for _, value := range data {
		if value < '0' || value > '9' {
			return false
		}
	}
	return len(data) > 0
}
