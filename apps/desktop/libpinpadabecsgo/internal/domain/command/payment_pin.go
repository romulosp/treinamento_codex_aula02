package command

import (
	"fmt"
	"strconv"
	"strings"
	"unicode/utf8"
)

// PaymentPINConfig representa exclusivamente o perfil PIN DUKPT TDES de homologação.
// Não contém PAN nem material de chave; WK não vazia é recusada neste perfil.
type PaymentPINConfig struct {
	Method     string
	KeyIndex   int
	WorkingKey string
}

// Validate impede conversões automáticas de método ou utilização de WK com DUKPT.
func (c PaymentPINConfig) Validate() error {
	if c.Method != "3" || c.KeyIndex < 0 || c.KeyIndex > 99 || c.WorkingKey != "" {
		return fmt.Errorf("invalid payment PIN configuration")
	}
	return nil
}

// ParsePaymentPINProperties lê os três campos obrigatórios, sem interpretar expressões.
// Erros não reproduzem valores, linhas ou nomes desconhecidos da fonte externa.
func ParsePaymentPINProperties(raw string) (PaymentPINConfig, error) {
	if len(raw) == 0 || len(raw) > 65536 || !utf8.ValidString(raw) {
		return PaymentPINConfig{}, fmt.Errorf("invalid PIN properties size or encoding")
	}
	fields := make(map[string]string, 3)
	for _, line := range strings.Split(strings.TrimPrefix(raw, "\uFEFF"), "\n") {
		line = strings.TrimSpace(line)
		if line == "" || strings.HasPrefix(line, "#") || strings.HasPrefix(line, "!") {
			continue
		}
		separator := strings.IndexAny(line, "=:")
		if separator < 1 {
			return PaymentPINConfig{}, fmt.Errorf("invalid PIN properties syntax")
		}
		key, value := strings.TrimSpace(line[:separator]), strings.TrimSpace(line[separator+1:])
		switch key {
		case "TIPO_CHAVE", "INDICE_CHAVE", "WORKING_KEY":
		default:
			return PaymentPINConfig{}, fmt.Errorf("unknown PIN property")
		}
		if _, exists := fields[key]; exists {
			return PaymentPINConfig{}, fmt.Errorf("duplicate PIN property")
		}
		fields[key] = value
	}
	if len(fields) != 3 || !isFixedNumeric(fields["INDICE_CHAVE"], 2) {
		return PaymentPINConfig{}, fmt.Errorf("missing or invalid PIN property")
	}
	index, err := strconv.Atoi(fields["INDICE_CHAVE"])
	if err != nil {
		return PaymentPINConfig{}, fmt.Errorf("invalid PIN index")
	}
	wk := fields["WORKING_KEY"]
	if wk == `""` {
		wk = ""
	}
	config := PaymentPINConfig{Method: fields["TIPO_CHAVE"], KeyIndex: index, WorkingKey: wk}
	if err := config.Validate(); err != nil {
		return PaymentPINConfig{}, err
	}
	return config, nil
}

// ValidatePaymentCandidates verifica AID completo e tipo de aplicação sem alterar registros.
// A seleção das aplicações no cartão continua a cargo do kernel do pinpad.
func ValidatePaymentCandidates(records []string, acquirer, applicationTypes string) error {
	if err := ValidateFormattedEMVRecords(records, acquirer); err != nil {
		return err
	}
	if len(applicationTypes) == 0 || len(applicationTypes)%2 != 0 || len(applicationTypes) > 20 {
		return fmt.Errorf("invalid payment application filter")
	}
	wanted := make(map[string]bool)
	for offset := 0; offset < len(applicationTypes); offset += 2 {
		app := applicationTypes[offset : offset+2]
		if !numericIndex(app, 98) {
			return fmt.Errorf("invalid payment application filter")
		}
		if _, exists := wanted[app]; exists {
			return fmt.Errorf("duplicate payment application filter")
		}
		wanted[app] = false
	}
	for _, record := range records {
		if record[3] != '1' {
			continue
		}
		encoded, err := latin1(record, TLRMaxBlockSize-2, "payment AID record")
		if err != nil || len(encoded) < 284 {
			return fmt.Errorf("invalid payment AID record")
		}
		aidLength, err := strconv.Atoi(string(encoded[8:10]))
		if err != nil || aidLength < 5 || aidLength > 16 {
			return fmt.Errorf("invalid payment AID length")
		}
		for position, char := range encoded[10:42] {
			if position >= aidLength*2 && char == ' ' {
				continue
			}
			if !(char >= '0' && char <= '9' || char >= 'A' && char <= 'F' || char >= 'a' && char <= 'f') {
				return fmt.Errorf("invalid payment AID encoding")
			}
		}
		app := string(encoded[42:44])
		if !numericIndex(app, 98) {
			return fmt.Errorf("invalid payment application type")
		}
		if _, exists := wanted[app]; exists {
			wanted[app] = true
		}
	}
	for _, found := range wanted {
		if !found {
			return fmt.Errorf("payment application has no eligible candidates")
		}
	}
	return nil
}
