package command

import (
	"fmt"
	"hash/crc32"
	"strconv"
)

const (
	MLRMaxBlockSize = 1024
	TLRMaxRecords   = 99
)

func fixed(value string, size int) string {
	b := []byte(value)
	if len(b) > size {
		b = b[:size]
	}
	for len(b) < size {
		b = append(b, ' ')
	}
	return string(b)
}
func BuildDSPCommand(line1, line2 string) []byte {
	return BuildPacketPayload(CommandDSP, fixed(line1, 16)+fixed(line2, 16))
}

// BuildDEXCommand monta o comando DEX conforme ABECS 3.3.4: DEX_MSGLEN (N3)
// seguido de DEX_MSG (S..160), sem campos de alinhamento ou tipo de mensagem.
func BuildDEXCommand(message string) ([]byte, error) {
	if len(message) > 160 {
		return nil, fmt.Errorf("DEX message exceeds 160 characters")
	}
	return BuildPacketPayload(CommandDEX, fmt.Sprintf("%03d%s", len(message), message)), nil
}
func BuildMNUCommand(timeout int, title string, options []string) ([]byte, error) {
	if timeout < 0 || timeout > 999 {
		return nil, fmt.Errorf("invalid menu timeout")
	}
	return BuildPacketPayload(CommandMNU, fmt.Sprintf("%03d%s%s", timeout, fixed(title, 16), joinOptions(options))), nil
}
func joinOptions(options []string) string {
	result := ""
	for i, option := range options {
		if i > 0 {
			result += "|"
		}
		result += option
	}
	return result
}
func BuildGKYCommand(mode byte, timeout int) ([]byte, error) {
	if mode > GKYModeGetKey || timeout < 0 || timeout > 999 {
		return nil, fmt.Errorf("invalid GKY parameters")
	}
	return BuildPacketPayload(CommandGKY, string([]byte{'0' + mode})+strconv.Itoa(timeout)), nil
}

// BuildPacketPayload monta o payload logico ABECS (CMD_ID + LEN(3) + parametros)
// para o comando informado. O enquadramento SYN/ETB/CRC e feito posteriormente
// pela camada de transporte (service.exchange), nunca aqui.
func BuildPacketPayload(kind Type, payload string) []byte {
	data := []byte(payload)
	length := len(data)
	out := make([]byte, 0, len(kind)+3+length)
	out = append(out, []byte(string(kind))...)
	out = append(out, byte('0'+(length/100)%10), byte('0'+(length/10)%10), byte('0'+length%10))
	out = append(out, data...)
	return out
}
func BuildDSICommand(name string) []byte { return BuildPacketPayload(CommandDSI, name) }
func BuildMLICommand(name string, size int) []byte {
	return BuildPacketPayload(CommandMLI, fmt.Sprintf("%s%08d", name, size))
}
func BuildMLRCommand(block []byte) ([]byte, error) {
	if len(block) > MLRMaxBlockSize {
		return nil, fmt.Errorf("MLR block exceeds limit")
	}
	return BuildPacketPayload(CommandMLR, string(block)), nil
}
func BuildMLECommand(name string) []byte { return BuildPacketPayload(CommandMLE, name) }
func BuildTLICommand(acquirer, version string) ([]byte, error) {
	if len(acquirer) != 2 || version == "" {
		return nil, fmt.Errorf("invalid table parameters")
	}
	return BuildPacketPayload(CommandTLI, acquirer+version), nil
}
func BuildTLRCommand(records []string) ([]byte, error) {
	if len(records) == 0 || len(records) > TLRMaxRecords {
		return nil, fmt.Errorf("invalid table records")
	}
	return BuildPacketPayload(CommandTLR, joinOptions(records)), nil
}
func BuildTLECommand(version string) []byte { return BuildPacketPayload(CommandTLE, version) }

// BuildGCXCommand cria somente o subconjunto GCX especificado pela Change 066.
func BuildGCXCommand(amount, date, clock string, options byte) ([]byte, error) {
	if len(date) != 6 || len(clock) != 6 || amount == "" {
		return nil, fmt.Errorf("invalid GCX parameters")
	}
	return BuildPacketPayload(CommandGCX, fmt.Sprintf("%02X%s%s%s", options, date, clock, amount)), nil
}

func BuildRSTCommand() []byte { return BuildPacketPayload(CommandRST, "000") }

func BuildGPNCommandMK(keyIndex int, pan, message string) ([]byte, error) {
	if keyIndex < 0 || keyIndex > 99 || pan == "" || len(message) > 32 {
		return nil, fmt.Errorf("invalid MK PIN parameters")
	}
	return BuildPacketPayload(CommandGPN, fmt.Sprintf("MK%02d%s%s", keyIndex, pan, fixed(message, 32))), nil
}

func BuildGPNCommandDUKPT(ksn, pan, message string) ([]byte, error) {
	if len(ksn) != 20 || pan == "" || len(message) > 32 {
		return nil, fmt.Errorf("invalid DUKPT PIN parameters")
	}
	return BuildPacketPayload(CommandGPN, fmt.Sprintf("DU%s%s%s", ksn, pan, fixed(message, 32))), nil
}

func ParseGPNResponse(data []byte) (pinBlock, ksn []byte, err error) {
	if len(data) < 16 {
		return nil, nil, fmt.Errorf("invalid GPN response")
	}
	pinBlock = append([]byte(nil), data[:16]...)
	if len(data) >= 36 {
		ksn = append([]byte(nil), data[16:36]...)
	}
	return pinBlock, ksn, nil
}

func CalculateFileCRC(data []byte) uint32 { return crc32.ChecksumIEEE(data) }
