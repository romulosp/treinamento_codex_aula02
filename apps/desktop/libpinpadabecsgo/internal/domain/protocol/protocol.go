package protocol

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/utilitario/crc"
	"encoding/binary"
	"fmt"
)

const (
	PP_SYN byte = 0x16
	PP_ETB byte = 0x17
	PP_ACK byte = 0x06
	PP_NAK byte = 0x15
	PP_EOT byte = 0x04
	PP_CAN byte = 0x18
	PP_DC3 byte = 0x13
)

// ApplySubstitution escapa bytes de controle que ocorrem no corpo do pacote.
func ApplySubstitution(data []byte) []byte {
	out := make([]byte, 0, len(data))
	for _, b := range data {
		switch b {
		case PP_DC3:
			out = append(out, PP_DC3, 0x33)
		case PP_SYN:
			out = append(out, PP_DC3, 0x36)
		case PP_ETB:
			out = append(out, PP_DC3, 0x37)
		default:
			out = append(out, b)
		}
	}
	return out
}

// RemoveSubstitution reverte escapes ABECS e rejeita sequências incompletas.
func RemoveSubstitution(data []byte) ([]byte, error) {
	out := make([]byte, 0, len(data))
	for i := 0; i < len(data); i++ {
		if data[i] != PP_DC3 {
			out = append(out, data[i])
			continue
		}
		if i+1 >= len(data) {
			return nil, fmt.Errorf("%w: incomplete substitution", domainerror.ErrInvalidResponse)
		}
		switch data[i+1] {
		case 0x33:
			out = append(out, PP_DC3)
		case 0x36:
			out = append(out, PP_SYN)
		case 0x37:
			out = append(out, PP_ETB)
		default:
			return nil, fmt.Errorf("%w: unknown substitution 0x%02x", domainerror.ErrInvalidResponse, data[i+1])
		}
		i++
	}
	return out, nil
}

// BuildPacket enquadra o payload com SYN, ETB, substitution e CRC-16-CCITT.
func BuildPacket(payload []byte) []byte {
	escaped := ApplySubstitution(payload)
	packet := make([]byte, 0, 1+len(escaped)+3)
	packet = append(packet, PP_SYN)
	packet = append(packet, escaped...)
	packet = append(packet, PP_ETB)
	// O CRC e calculado sobre os dados originais (sem substituicao) seguidos do
	// byte ETB, exatamente como na referencia C++ (crcData = data + PP_ETB).
	crcData := make([]byte, 0, len(payload)+1)
	crcData = append(crcData, payload...)
	crcData = append(crcData, PP_ETB)
	sum := crc.CRC16CCITT(crcData)
	var check [2]byte
	binary.BigEndian.PutUint16(check[:], sum)
	return append(packet, check[:]...)
}

// BuildAbecsPayload monta o payload logico de um comando ABECS: identificador
// de 3 caracteres, seguido do tamanho dos parametros em 3 digitos ASCII e, por
// fim, os proprios parametros. Este payload ainda nao esta enquadrado (sem
// SYN/ETB/CRC); use BuildPacket para produzir o pacote final.
func BuildAbecsPayload(id string, parameters []byte) []byte {
	length := len(parameters)
	payload := make([]byte, 0, len(id)+3+length)
	payload = append(payload, []byte(id)...)
	payload = append(payload, byte('0'+(length/100)%10), byte('0'+(length/10)%10), byte('0'+length%10))
	payload = append(payload, parameters...)
	return payload
}

// BuildCommand monta e enquadra um comando ABECS de bloco único.
func BuildCommand(id string, parameters []byte) []byte {
	return BuildPacket(BuildAbecsPayload(id, parameters))
}

// ValidatePacket valida framing, substitution e CRC, retornando o payload puro.
func ValidatePacket(packet []byte) ([]byte, error) {
	if len(packet) < 4 || packet[0] != PP_SYN || packet[len(packet)-3] != PP_ETB {
		return nil, fmt.Errorf("%w: invalid frame", domainerror.ErrInvalidResponse)
	}
	body, err := RemoveSubstitution(packet[1 : len(packet)-3])
	if err != nil {
		return nil, err
	}
	got := binary.BigEndian.Uint16(packet[len(packet)-2:])
	// O CRC recebido cobre o corpo original (sem substituicao) seguido do
	// byte ETB, na mesma ordem usada em BuildPacket.
	crcData := make([]byte, 0, len(body)+1)
	crcData = append(crcData, body...)
	crcData = append(crcData, PP_ETB)
	if crc.CRC16CCITT(crcData) != got {
		return nil, domainerror.ErrChecksumInvalid
	}
	return body, nil
}

// AckType classifica um único byte de controle ou dados de resposta.
func AckType(data []byte) string {
	switch {
	case len(data) == 1 && data[0] == PP_ACK:
		return "ACK"
	case len(data) == 1 && data[0] == PP_NAK:
		return "NAK"
	case len(data) == 1 && data[0] == PP_EOT:
		return "EOT"
	default:
		return "DATA"
	}
}
