package logging

import (
	"encoding/binary"
	"fmt"
	"strconv"
	"strings"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/utilitario/crc"
)

// decodedField é um campo interpretado de um comando ou resposta.
type decodedField struct {
	name      string
	format    string
	offset    int
	length    int
	hex       string
	value     string
	sensitive bool
	encoding  Encoding
}

// decodedFrame é o resultado da decodificação semântica de um pacote completo.
type decodedFrame struct {
	command     string
	description string
	fields      []decodedField
	crc         crcDiagnostic
}

type crcDiagnostic struct {
	received   uint16
	calculated uint16
	valid      bool
}

// isCompletePacket informa se o buffer é um pacote enquadrado íntegro
// (SYN ... ETB + CRC), sem validar o CRC.
func isCompletePacket(data []byte) bool {
	return len(data) >= 4 && data[0] == protocol.PP_SYN && data[len(data)-3] == protocol.PP_ETB
}

// decodeFrame interpreta um pacote SPE/PP completo. isResponse escolhe o
// esquema (resposta) em vez do esquema de comando.
func decodeFrame(kind command.Type, data []byte, isResponse bool) (*decodedFrame, error) {
	if !isCompletePacket(data) {
		return nil, fmt.Errorf("pacote incompleto")
	}
	body, err := protocol.RemoveSubstitution(data[1 : len(data)-3])
	if err != nil {
		return nil, fmt.Errorf("substituição inválida: %w", err)
	}
	frame := &decodedFrame{
		command:     string(kind),
		description: commandMetaFor(kind).description,
		fields:      []decodedField{},
	}
	frame.crc.received = binary.BigEndian.Uint16(data[len(data)-2:])
	crcData := append(append([]byte(nil), body...), protocol.PP_ETB)
	frame.crc.calculated = crc.CRC16CCITT(crcData)
	frame.crc.valid = frame.crc.received == frame.crc.calculated

	if isResponse {
		frame.fields, err = decodeResponseFields(kind, body)
	} else {
		frame.fields, err = decodeCommandFields(kind, body)
	}
	if err != nil {
		return frame, err
	}
	return frame, nil
}

// decodeCommandFields interpreta o payload lógico de um comando.
func decodeCommandFields(kind command.Type, body []byte) ([]decodedField, error) {
	if len(body) == 3 {
		switch kind {
		case command.CommandOPN, command.CommandGKY, command.CommandLMF, command.CommandMLE, command.CommandTLE:
			return nil, nil
		}
	}
	if len(body) < 6 {
		return nil, fmt.Errorf("comando curto")
	}
	if id := string(body[:3]); id != string(kind) {
		return nil, fmt.Errorf("CMD_ID %q != tipo %q", id, kind)
	}
	if commandMetaFor(kind).classic || kind == command.CommandCLO {
		return decodeClassicCommand(kind, body)
	}
	if kind == command.CommandTLI {
		if len(body) != 18 || string(body[3:6]) != "012" || !diagnosticDigits(body[6:8]) {
			return nil, fmt.Errorf("TLI inválido")
		}
		return []decodedField{
			{name: "TLI_ACQIDX", format: "N2", offset: 6, length: 2, hex: fmt.Sprintf("%X", body[6:8]), value: string(body[6:8])},
			{name: "TLI_VERSION", format: "A10", offset: 8, length: 10, hex: fmt.Sprintf("%X", body[8:]), value: asciiOnly(body[8:])},
		}, nil
	}
	return decodeTLVCommand(kind, body)
}

// decodeClassicCommand interpreta comandos de schema posicional fixo (DSP/DEX).
func decodeClassicCommand(kind command.Type, body []byte) ([]decodedField, error) {
	if !diagnosticDigits(body[3:6]) {
		return nil, fmt.Errorf("CMD_LEN inválido")
	}
	length, err := strconv.Atoi(string(body[3:6]))
	if err != nil {
		return nil, fmt.Errorf("CMD_LEN inválido")
	}
	msg := body[6:]
	if length != len(msg) {
		return nil, fmt.Errorf("CMD_LEN %d != payload %d", length, len(msg))
	}
	name, format := "DSP_MSG", "S32"
	if kind == command.CommandDEX {
		name, format = "DEX_MSG", "S.."
		if len(msg) < 3 || !diagnosticDigits(msg[:3]) {
			return nil, fmt.Errorf("DEX_MSGLEN inválido")
		}
		msgLength, _ := strconv.Atoi(string(msg[:3]))
		if msgLength != len(msg)-3 || msgLength < 1 || msgLength > 160 {
			return nil, fmt.Errorf("DEX_MSGLEN inválido")
		}
		msg = msg[3:]
	} else if len(msg) != 32 {
		return nil, fmt.Errorf("mensagem deve ter 32 bytes")
	}
	if kind == command.CommandCLO {
		name = "CLO_MSG"
	}
	offset := 6
	if kind == command.CommandDEX {
		offset = 9
	}
	return []decodedField{{
		name:     name,
		format:   format,
		offset:   offset,
		length:   len(msg),
		hex:      fmt.Sprintf("%X", msg),
		value:    interpretValue(msg, EncISO88591),
		encoding: EncISO88591,
	}}, nil
}

// decodeTLVCommand interpreta um comando ABECS: CMD_ID + blocos de parâmetros
// CMD_PARID(X2) + CMD_PARLEN(X2) + CMD_PAR.
func decodeTLVCommand(kind command.Type, body []byte) ([]decodedField, error) {
	fields := []decodedField{}
	pos := 3
	for pos < len(body) {
		if pos+3 > len(body) {
			return fields, fmt.Errorf("CMD_LEN incompleto")
		}
		blockLen, err := strconv.Atoi(string(body[pos : pos+3]))
		if err != nil || blockLen < 0 || pos+3+blockLen > len(body) {
			return fields, fmt.Errorf("CMD_LEN inválido")
		}
		pos += 3
		blockEnd := pos + blockLen
		for pos < blockEnd {
			if pos+4 > blockEnd {
				return fields, fmt.Errorf("parâmetro truncado")
			}
			id := command.SPEParameter(uint16(body[pos])<<8 | uint16(body[pos+1]))
			paramLen := int(body[pos+2])<<8 | int(body[pos+3])
			pos += 4
			if pos+paramLen > blockEnd {
				return fields, fmt.Errorf("valor do parâmetro 0x%04X truncado", uint16(id))
			}
			value := body[pos : pos+paramLen]
			meta := speFieldFor(id)
			fields = append(fields, decodedField{
				name:      meta.name,
				format:    meta.format,
				offset:    pos - 4,
				length:    paramLen,
				hex:       fmt.Sprintf("%X", value),
				value:     interpretField(value, meta.encoding, meta.sensitive),
				sensitive: meta.sensitive,
				encoding:  meta.encoding,
			})
			pos += paramLen
		}
	}
	return fields, nil
}

// decodeResponseFields interpreta o payload lógico de uma resposta.
func decodeResponseFields(kind command.Type, body []byte) ([]decodedField, error) {
	if len(body) < 6 {
		return nil, fmt.Errorf("resposta curta")
	}
	if id := string(body[:3]); id != string(kind) {
		return nil, fmt.Errorf("RSP_ID %q != tipo %q", id, kind)
	}
	fields := []decodedField{{
		name:     "RSP_STAT",
		format:   "N3",
		offset:   3,
		length:   3,
		hex:      fmt.Sprintf("%X", body[3:6]),
		value:    string(body[3:6]),
		encoding: EncNumeric,
	}}
	if len(body) == 6 {
		return fields, nil
	}
	// OPN e GPN possuem dados posicionais em vez de RSP_DATID TLV.
	if kind == command.CommandOPN || kind == command.CommandGPN {
		return nil, fmt.Errorf("resposta posicional protegida")
	}
	if kind == command.CommandGKY && len(body) == 8 && diagnosticDigits(body[6:]) {
		return append(fields, decodedField{name: "PP_VALUE", format: "N2", offset: 6, length: 2, hex: fmt.Sprintf("%X", body[6:]), value: string(body[6:])}), nil
	}
	pos := 6
	for pos < len(body) {
		if pos+3 > len(body) {
			return fields, fmt.Errorf("RSP_LEN incompleto")
		}
		blockLen, err := strconv.Atoi(string(body[pos : pos+3]))
		if err != nil || blockLen < 0 || pos+3+blockLen > len(body) {
			return fields, fmt.Errorf("RSP_LEN inválido")
		}
		pos += 3
		blockEnd := pos + blockLen
		for pos < blockEnd {
			if pos+4 > blockEnd {
				return fields, fmt.Errorf("dado truncado")
			}
			tag := protocol.ResponseTag(uint16(body[pos])<<8 | uint16(body[pos+1]))
			dataLen := int(body[pos+2])<<8 | int(body[pos+3])
			pos += 4
			if pos+dataLen > blockEnd {
				return fields, fmt.Errorf("valor do dado 0x%04X truncado", uint16(tag))
			}
			value := body[pos : pos+dataLen]
			meta := rspFieldFor(tag)
			fields = append(fields, decodedField{
				name:      meta.name,
				format:    meta.format,
				offset:    pos - 4,
				length:    dataLen,
				hex:       fmt.Sprintf("%X", value),
				value:     interpretField(value, meta.encoding, meta.sensitive),
				sensitive: meta.sensitive,
				encoding:  meta.encoding,
			})
			pos += dataLen
		}
	}
	return fields, nil
}

// interpretField devolve o valor textual de um campo, respeitando a redação.
func interpretField(data []byte, encoding Encoding, sensitive bool) string {
	if sensitive {
		return "[REDACTED]"
	}
	return interpretValue(data, encoding)
}

// interpretValue converte bytes em texto conforme a codificação do campo.
func interpretValue(data []byte, encoding Encoding) string {
	switch encoding {
	case EncASCII, EncNumeric:
		return asciiOnly(data)
	case EncISO88591:
		return latin1(data)
	case EncUnknown:
		return "NAO_INTERPRETADO"
	default:
		return ""
	}
}

// asciiOnly representa bytes como ASCII visível; controles tornam-se '.'.
func asciiOnly(data []byte) string {
	var builder strings.Builder
	for _, b := range data {
		switch {
		case b == 0x0D:
			builder.WriteString(`\r`)
		case b >= 0x20 && b <= 0x7E:
			builder.WriteByte(b)
		default:
			builder.WriteByte('.')
		}
	}
	return builder.String()
}

// latin1 representa bytes como ISO-8859-1; 0Dh é exibido como "\r".
func latin1(data []byte) string {
	var builder strings.Builder
	for _, b := range data {
		switch {
		case b == 0x0D:
			builder.WriteString(`\r`)
		case b >= 0x20 && b <= 0x7E:
			builder.WriteByte(b)
		case b >= 0xA0:
			builder.WriteRune(rune(b))
		default:
			builder.WriteByte('.')
		}
	}
	return builder.String()
}
