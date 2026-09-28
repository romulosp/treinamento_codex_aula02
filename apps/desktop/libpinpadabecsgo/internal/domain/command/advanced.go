package command

import (
	"encoding/binary"
	"encoding/hex"
	"fmt"
	"strconv"
	"strings"
	"time"
	"unicode/utf8"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/utilitario/crc"
)

// Limites dos blocos multimídia e de tabelas EMV definidos pelo ABECS 2.12.
const (
	MLRMaxBlockSize = 995
	TLRMaxRecords   = 99
	TLRMaxBlockSize = 999
)

// GPNRequest representa o bloco clássico do comando GPN.
type GPNRequest struct {
	Method              byte
	KeyIndex            int
	EncryptedWorkingKey []byte
	PAN                 string
	MinDigits           int
	MaxDigits           int
	Message             string
}

func latin1(value string, max int, field string) ([]byte, error) {
	if !utf8.ValidString(value) {
		return nil, fmt.Errorf("%s is not valid UTF-8", field)
	}
	result := make([]byte, 0, len(value))
	for _, current := range value {
		if current > 0xff {
			return nil, fmt.Errorf("%s contains a character outside Latin-1", field)
		}
		result = append(result, byte(current))
	}
	if len(result) > max {
		return nil, fmt.Errorf("%s exceeds %d bytes", field, max)
	}
	return result, nil
}

func fixedLatin1(value string, size int, field string) ([]byte, error) {
	result, err := latin1(value, size, field)
	if err != nil {
		return nil, err
	}
	for len(result) < size {
		result = append(result, ' ')
	}
	return result, nil
}

// BuildDSPCommand monta DSP032 com duas linhas S16.
func BuildDSPCommand(line1, line2 string) ([]byte, error) {
	first, err := fixedLatin1(line1, 16, "DSP line 1")
	if err != nil {
		return nil, err
	}
	second, err := fixedLatin1(line2, 16, "DSP line 2")
	if err != nil {
		return nil, err
	}
	return BuildPacketPayload(CommandDSP, string(append(first, second...))), nil
}

// BuildDEXCommand monta DEX com DEX_MSGLEN N3 e mensagem S1..160.
func BuildDEXCommand(message string) ([]byte, error) {
	encoded, err := latin1(message, 160, "DEX message")
	if err != nil {
		return nil, err
	}
	if len(encoded) == 0 {
		return nil, fmt.Errorf("DEX message is required")
	}
	return BuildPacketPayload(CommandDEX, fmt.Sprintf("%03d%s", len(encoded), encoded)), nil
}

// BuildMNUCommand monta o menu ABECS com os limites definidos na seção 3.3.13.
func BuildMNUCommand(timeout int, title string, options []string) ([]byte, error) {
	if timeout < 0 || timeout > 255 {
		return nil, fmt.Errorf("invalid menu timeout")
	}
	if len(options) < 1 || len(options) > 20 {
		return nil, fmt.Errorf("menu requires between 1 and 20 options")
	}
	parameters := make([]Parameter, 0, 2+len(options))
	if timeout > 0 {
		parameters = append(parameters, Parameter{ID: SPETimeout, Value: []byte{byte(timeout)}})
	}
	for _, option := range options {
		encoded, err := latin1(option, 24, "menu option")
		if err != nil {
			return nil, err
		}
		if len(encoded) == 0 {
			return nil, fmt.Errorf("menu option cannot be empty")
		}
		parameters = append(parameters, Parameter{ID: SPEMenuOption, Value: encoded})
	}
	if title != "" {
		encoded, err := latin1(title, 128, "menu title")
		if err != nil {
			return nil, err
		}
		parameters = append(parameters, Parameter{ID: SPEDisplayMessage, Value: encoded})
	}
	return BuildABECSPayload(CommandMNU, parameters)
}

// BuildGKYCommand produz o comando clássico bloqueante GKY.
func BuildGKYCommand() []byte { return []byte(CommandGKY) }

// BuildPacketPayload monta CMD_ID + LEN(N3) + dados.
func BuildPacketPayload(kind Type, payload string) []byte {
	data := []byte(payload)
	out := make([]byte, 0, len(kind)+3+len(data))
	out = append(out, []byte(string(kind))...)
	out = append(out, fmt.Sprintf("%03d", len(data))...)
	out = append(out, data...)
	return out
}

func validMediaName(name string) bool {
	if len(name) != 8 {
		return false
	}
	for i := range name {
		if !((name[i] >= 'A' && name[i] <= 'Z') || (name[i] >= 'a' && name[i] <= 'z') || (name[i] >= '0' && name[i] <= '9')) {
			return false
		}
	}
	return true
}

// DetectMediaType identifica PNG, JPEG ou GIF pela assinatura binária.
func DetectMediaType(data []byte) (byte, error) {
	switch {
	case len(data) >= 8 && string(data[:8]) == "\x89PNG\r\n\x1a\n":
		return 1, nil
	case len(data) >= 3 && data[0] == 0xff && data[1] == 0xd8 && data[2] == 0xff:
		return 2, nil
	case len(data) >= 6 && (string(data[:6]) == "GIF87a" || string(data[:6]) == "GIF89a"):
		return 3, nil
	default:
		return 0, fmt.Errorf("unsupported multimedia format")
	}
}

// BuildDSICommand monta DSI com SPE_MFNAME A8.
func BuildDSICommand(name string) ([]byte, error) {
	if !validMediaName(name) {
		return nil, fmt.Errorf("multimedia name must be 8 alphanumeric characters")
	}
	return BuildABECSPayload(CommandDSI, []Parameter{{ID: SPEMultimediaFileName, Value: []byte(name)}})
}

// BuildLMFCommand produz o comando clássico que lista nomes de mídia.
func BuildLMFCommand() []byte { return []byte(CommandLMF) }

// BuildDMFCommand monta um pedido para excluir um ou mais arquivos A8.
func BuildDMFCommand(names []string) ([]byte, error) {
	if len(names) == 0 {
		return nil, fmt.Errorf("DMF requires at least one multimedia name")
	}
	parameters := make([]Parameter, 0, len(names))
	for _, name := range names {
		if !validMediaName(name) {
			return nil, fmt.Errorf("DMF multimedia names must be 8 alphanumeric characters")
		}
		parameters = append(parameters, Parameter{ID: SPEMultimediaFileName, Value: []byte(name)})
	}
	return BuildABECSPayload(CommandDMF, parameters)
}

// BuildMLICommand monta SPE_MFNAME e SPE_MFINFO conforme a seção 3.4.1.
func BuildMLICommand(name string, data []byte) ([]byte, error) {
	if !validMediaName(name) {
		return nil, fmt.Errorf("multimedia name must be 8 alphanumeric characters")
	}
	if len(data) == 0 || uint64(len(data)) > uint64(^uint32(0)) {
		return nil, fmt.Errorf("invalid multimedia size")
	}
	mediaType, err := DetectMediaType(data)
	if err != nil {
		// A seção 6.6.1 do ABECS 2.12 posterga a validação de formato até DSI;
		// assinaturas desconhecidas usam o tipo reservado RUF durante MLI.
		mediaType = 0
	}
	info := make([]byte, 10)
	binary.BigEndian.PutUint32(info[:4], uint32(len(data)))
	binary.BigEndian.PutUint16(info[4:6], crc.CRC16CCITT(data))
	info[6] = mediaType
	return BuildABECSPayload(CommandMLI, []Parameter{
		{ID: SPEMultimediaFileName, Value: []byte(name)},
		{ID: SPEMultimediaFileInfo, Value: info},
	})
}

// BuildMLRCommand monta um SPE_DATAIN de até 995 bytes.
func BuildMLRCommand(block []byte) ([]byte, error) {
	if len(block) == 0 || len(block) > MLRMaxBlockSize {
		return nil, fmt.Errorf("MLR block must contain between 1 and %d bytes", MLRMaxBlockSize)
	}
	return BuildABECSPayload(CommandMLR, []Parameter{{ID: SPEDataIn, Value: append([]byte(nil), block...)}})
}

// BuildMLECommand produz o comando clássico MLE.
func BuildMLECommand() []byte { return []byte(CommandMLE) }

// BuildTLICommand monta o comando clássico TLI012.
func BuildTLICommand(acquirer, version string) ([]byte, error) {
	if !isFixedNumeric(acquirer, 2) || len(version) != 10 {
		return nil, fmt.Errorf("invalid table parameters")
	}
	if acquirer == "00" {
		// 00 seleciona todas as tabelas.
	} else if value, _ := strconv.Atoi(acquirer); value < 1 || value > 99 {
		return nil, fmt.Errorf("invalid table acquirer")
	}
	for i := range version {
		if !((version[i] >= 'A' && version[i] <= 'Z') || (version[i] >= 'a' && version[i] <= 'z') || (version[i] >= '0' && version[i] <= '9')) {
			return nil, fmt.Errorf("invalid table version")
		}
	}
	return []byte("TLI012" + acquirer + version), nil
}

// BuildTLRCommand concatena registros N3 sem delimitador.
func BuildTLRCommand(records []string) ([]byte, error) {
	if len(records) == 0 || len(records) > TLRMaxRecords {
		return nil, fmt.Errorf("invalid table records")
	}
	var body strings.Builder
	body.WriteString(fmt.Sprintf("%02d", len(records)))
	for _, record := range records {
		encoded, err := latin1(record, 994, "table record")
		if err != nil || len(encoded) == 0 {
			return nil, fmt.Errorf("invalid table record")
		}
		body.WriteString(fmt.Sprintf("%03d", len(encoded)))
		body.Write(encoded)
	}
	if body.Len() > TLRMaxBlockSize {
		return nil, fmt.Errorf("TLR block exceeds %d bytes", TLRMaxBlockSize)
	}
	return BuildPacketPayload(CommandTLR, body.String()), nil
}

// BuildTLECommand produz o comando clássico TLE.
func BuildTLECommand() []byte { return []byte(CommandTLE) }

// BuildGCXCommand cria o subconjunto GCX especificado pela Change 066.
func BuildGCXCommand(amount, date, clock string, options byte) ([]byte, error) {
	if !isFixedNumeric(amount, 12) || !isFixedNumeric(date, 6) || !isFixedNumeric(clock, 6) {
		return nil, fmt.Errorf("invalid GCX parameters")
	}
	if _, err := time.Parse("060102", date); err != nil {
		return nil, fmt.Errorf("invalid GCX date: %w", err)
	}
	if _, err := time.Parse("150405", clock); err != nil {
		return nil, fmt.Errorf("invalid GCX time: %w", err)
	}
	if options&^(GCXOptionWaitContactless|GCXOptionHideAmount) != 0 {
		return nil, fmt.Errorf("invalid GCX options")
	}
	return BuildABECSPayload(CommandGCX, []Parameter{
		{ID: SPEAmount, Value: []byte(amount)},
		{ID: SPETransactionDate, Value: []byte(date)},
		{ID: SPETransactionTime, Value: []byte(clock)},
		{ID: SPEGCXOption, Value: []byte(fmt.Sprintf("%05b", options))},
	})
}

func isFixedNumeric(value string, size int) bool {
	if len(value) != size {
		return false
	}
	for index := range value {
		if value[index] < '0' || value[index] > '9' {
			return false
		}
	}
	return true
}

// BuildGPNCommand monta o bloco clássico GPN.
func BuildGPNCommand(request GPNRequest) ([]byte, error) {
	if request.Method < '0' || request.Method > '3' || request.KeyIndex < 0 || request.KeyIndex > 99 {
		return nil, fmt.Errorf("invalid GPN method or key index")
	}
	if len(request.PAN) != 0 && (!isFixedNumeric(request.PAN, len(request.PAN)) || len(request.PAN) < 2 || len(request.PAN) > 19) {
		return nil, fmt.Errorf("invalid GPN PAN")
	}
	if request.MinDigits < 4 || request.MinDigits > 99 || request.MaxDigits < request.MinDigits || request.MaxDigits > 99 {
		return nil, fmt.Errorf("invalid GPN digit limits")
	}
	message, err := fixedLatin1(request.Message, 32, "GPN message")
	if err != nil {
		return nil, err
	}
	workingKey := request.EncryptedWorkingKey
	if request.Method == '0' || request.Method == '1' {
		if len(workingKey) != 16 {
			return nil, fmt.Errorf("GPN encrypted working key must have 16 bytes")
		}
	} else {
		if len(workingKey) != 0 {
			return nil, fmt.Errorf("GPN DUKPT does not accept a working key")
		}
		workingKey = make([]byte, 16)
	}
	pan := request.PAN + strings.Repeat(" ", 19-len(request.PAN))
	body := fmt.Sprintf("%c%02d%s%02d%s1%02d%02d%s",
		request.Method,
		request.KeyIndex,
		strings.ToUpper(hex.EncodeToString(workingKey)),
		len(request.PAN),
		pan,
		request.MinDigits,
		request.MaxDigits,
		message,
	)
	return BuildPacketPayload(CommandGPN, body), nil
}

// BuildGPNCommandMK monta GPN para captura de PIN com chave MK/WK TDES.
func BuildGPNCommandMK(keyIndex int, encryptedWorkingKey []byte, pan, message string) ([]byte, error) {
	return BuildGPNCommand(GPNRequest{Method: '1', KeyIndex: keyIndex, EncryptedWorkingKey: encryptedWorkingKey, PAN: pan, MinDigits: 4, MaxDigits: 12, Message: message})
}

// BuildGPNCommandDUKPT monta GPN para captura de PIN com derivação DUKPT TDES.
func BuildGPNCommandDUKPT(keyIndex int, pan, message string) ([]byte, error) {
	return BuildGPNCommand(GPNRequest{Method: '3', KeyIndex: keyIndex, PAN: pan, MinDigits: 4, MaxDigits: 12, Message: message})
}

// ParseGPNResponse decodifica PINBLK H16 e KSN H20.
func ParseGPNResponse(data []byte) (pinBlock, ksn []byte, err error) {
	if len(data) != 36 {
		return nil, nil, fmt.Errorf("invalid GPN response length")
	}
	pinBlock = make([]byte, 8)
	ksn = make([]byte, 10)
	if _, err = hex.Decode(pinBlock, data[:16]); err != nil {
		return nil, nil, fmt.Errorf("invalid GPN PIN block: %w", err)
	}
	if _, err = hex.Decode(ksn, data[16:]); err != nil {
		return nil, nil, fmt.Errorf("invalid GPN KSN: %w", err)
	}
	return pinBlock, ksn, nil
}
