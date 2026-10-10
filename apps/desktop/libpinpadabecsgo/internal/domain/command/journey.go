package command

import (
	"fmt"
	"strconv"
	"strings"
)

// FilteredGCXRequest define captura de saque com candidatos explícitos da tabela.
// Referências AARR são índices de registros, não AIDs hexadecimais.
type FilteredGCXRequest struct {
	Amount, Date, Clock                                string
	Options                                            byte
	TransactionType, TimeoutSeconds                    int
	AcquirerReference, ApplicationTypes, AIDReferences string
}

// BuildFilteredGCXCommand valida os filtros antes de construir o GCX ABECS.
// A estratégia por AIDReferences não pode ser misturada com ACQREF/APPTYPE.
func BuildFilteredGCXCommand(request FilteredGCXRequest) ([]byte, error) {
	if _, err := BuildGCXCommand(request.Amount, request.Date, request.Clock, request.Options); err != nil {
		return nil, err
	}
	if request.TransactionType < 0 || request.TransactionType > 255 || request.TimeoutSeconds < 1 || request.TimeoutSeconds > 255 {
		return nil, fmt.Errorf("invalid GCX transaction type or timeout")
	}
	parameters := []Parameter{
		{ID: SPEAmount, Value: []byte(request.Amount)},
		{ID: SPETransactionDate, Value: []byte(request.Date)},
		{ID: SPETransactionTime, Value: []byte(request.Clock)},
		{ID: SPEGCXOption, Value: []byte(fmt.Sprintf("%05b", request.Options))},
		{ID: SPETransactionType, Value: []byte{byte(request.TransactionType)}},
		{ID: SPETimeout, Value: []byte{byte(request.TimeoutSeconds)}},
	}
	if request.AIDReferences != "" {
		if request.AcquirerReference != "" || request.ApplicationTypes != "" || len(request.AIDReferences)%4 != 0 || len(request.AIDReferences) > 512 {
			return nil, fmt.Errorf("invalid GCX reference strategy")
		}
		seen := map[string]bool{}
		for offset := 0; offset < len(request.AIDReferences); offset += 4 {
			reference := request.AIDReferences[offset : offset+4]
			if !numericIndex(reference[:2], 99) || !validTableRecordIndex(reference[2:]) || seen[reference] {
				return nil, fmt.Errorf("invalid GCX table reference")
			}
			seen[reference] = true
		}
		parameters = append(parameters, Parameter{ID: SPEAIDList, Value: []byte(request.AIDReferences)})
	} else {
		if !numericIndex(request.AcquirerReference, 99) || len(request.ApplicationTypes) == 0 || len(request.ApplicationTypes) > 20 || len(request.ApplicationTypes)%2 != 0 {
			return nil, fmt.Errorf("invalid GCX application filter")
		}
		seen := map[string]bool{}
		for offset := 0; offset < len(request.ApplicationTypes); offset += 2 {
			value := request.ApplicationTypes[offset : offset+2]
			if !numericIndex(value, 98) || seen[value] {
				return nil, fmt.Errorf("invalid GCX application type")
			}
			seen[value] = true
		}
		parameters = append(parameters, Parameter{ID: SPEAcquirerReference, Value: []byte(request.AcquirerReference)}, Parameter{ID: SPEApplicationType, Value: []byte(request.ApplicationTypes)})
	}
	return BuildABECSPayload(CommandGCX, parameters)
}

// ValidateFormattedEMVRecords valida comprimentos completos e identidade de registros.
// Erros contêm somente categorias, nunca conteúdo da tabela externa.
func ValidateFormattedEMVRecords(records []string, acquirer string) error {
	if len(records) == 0 || !numericIndex(acquirer, 99) {
		return fmt.Errorf("invalid formatted EMV table")
	}
	seen := map[string]bool{}
	for _, record := range records {
		encoded, err := latin1(record, TLRMaxBlockSize-2, "table record")
		if err != nil || len(encoded) < 8 || !isFixedNumeric(string(encoded[:3]), 3) {
			return fmt.Errorf("invalid formatted EMV record")
		}
		length, _ := strconv.Atoi(string(encoded[:3]))
		if length != len(encoded) || (encoded[3] != '1' && encoded[3] != '2') || string(encoded[4:6]) != acquirer || !validTableRecordIndex(string(encoded[6:8])) {
			return fmt.Errorf("invalid formatted EMV record header")
		}
		key := string(encoded[3:8])
		if seen[key] {
			return fmt.Errorf("duplicate formatted EMV record")
		}
		seen[key] = true
	}
	return nil
}

// BuildFormattedTLRCommand preserva TAB_LEN e o registro Latin-1 completo.
// Somente NREC é adicionado; o corpo completo respeita CMD_LEN até 999 bytes.
func BuildFormattedTLRCommand(records []string) ([]byte, error) {
	if len(records) == 0 || len(records) > TLRMaxRecords {
		return nil, fmt.Errorf("invalid formatted TLR records")
	}
	body := strings.Builder{}
	body.WriteString(fmt.Sprintf("%02d", len(records)))
	for _, record := range records {
		encoded, err := latin1(record, TLRMaxBlockSize-2, "table record")
		if err != nil || len(encoded) < 8 || !isFixedNumeric(string(encoded[:3]), 3) {
			return nil, fmt.Errorf("invalid formatted TLR record")
		}
		length, _ := strconv.Atoi(string(encoded[:3]))
		if length != len(encoded) {
			return nil, fmt.Errorf("invalid formatted TLR length")
		}
		body.Write(encoded)
	}
	if body.Len() > TLRMaxBlockSize {
		return nil, fmt.Errorf("formatted TLR block too large")
	}
	return BuildPacketPayload(CommandTLR, body.String()), nil
}

func numericIndex(value string, maximum int) bool {
	if !isFixedNumeric(value, 2) {
		return false
	}
	number, _ := strconv.Atoi(value)
	return number >= 1 && number <= maximum
}

func validTableRecordIndex(value string) bool {
	if len(value) != 2 || value == "00" {
		return false
	}
	for index := range value {
		if !(value[index] >= '0' && value[index] <= '9') && !(value[index] >= 'A' && value[index] <= 'Z') {
			return false
		}
	}
	return true
}
