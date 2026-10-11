package mobile

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
)

// PurchaseGCXFilteredSummaryJSON captura com filtros do perfil sem expor dados de cartão.
// Os inteiros são validados antes de conversão para campos binários ABECS.
func (c *Client) PurchaseGCXFilteredSummaryJSON(operationID, amount, date, clock string, enableCTLS, hideAmount bool, transactionType int, acquirerReference, applicationTypes, aidReferences string, timeoutSeconds int) (result string, err error) {
	request := command.FilteredGCXRequest{Amount: amount, Date: date, Clock: clock, TransactionType: transactionType, AcquirerReference: acquirerReference, ApplicationTypes: applicationTypes, AIDReferences: aidReferences, TimeoutSeconds: timeoutSeconds}
	return c.runJSON(operationID, func(ctx context.Context) (any, error) {
		response, callErr := c.service.PurchaseGCXFiltered(ctx, request, enableCTLS, hideAmount)
		if callErr != nil {
			return nil, callErr
		}
		return struct {
			SchemaVersion string `json:"schemaVersion"`
			CardType      string `json:"cardType"`
			ICCStatus     string `json:"iccStatus,omitempty"`
			AidTableInfo  string `json:"aidTableInfo,omitempty"`
			DeviceType    string `json:"deviceType,omitempty"`
			Label         string `json:"label,omitempty"`
			IssuerCountry string `json:"issuerCountry,omitempty"`
		}{"1", response.CardType, response.ICCStatus, response.AidTableInfo, response.DeviceType, response.Label, response.IssuerCountry}, nil
	})
}

// LoadFormattedEMVTableJSON importa array externo e envia TLI/TLR/TLE pelo core.
// Comentários são permitidos somente em linhas inteiras; erros não ecoam dados.
func (c *Client) LoadFormattedEMVTableJSON(operationID, acquirer, version, recordsJSON string) (err error) {
	defer recoverBinding(&err)
	records, parseErr := parseFormattedEMVJSON(recordsJSON)
	if parseErr != nil {
		return parseErr
	}
	return c.run(operationID, func(ctx context.Context) error {
		return c.service.LoadFormattedEMVTable(ctx, acquirer, version, records)
	})
}

// ValidateFormattedEMVTableJSON valida a carga localmente, sem cliente ou I/O.
// Reutiliza os contratos do core; erros não incluem registros ou parâmetros.
func ValidateFormattedEMVTableJSON(acquirer, version, recordsJSON string) (err error) {
	defer recoverBinding(&err)
	records, err := parseFormattedEMVJSON(recordsJSON)
	if err != nil {
		return err
	}
	if _, err = command.BuildTLICommand(acquirer, version); err != nil {
		return err
	}
	if err = command.ValidateFormattedEMVRecords(records, acquirer); err != nil {
		return err
	}
	for _, record := range records {
		if _, err = command.BuildFormattedTLRCommand([]string{record}); err != nil {
			return err
		}
	}
	return nil
}

func parseFormattedEMVJSON(value string) ([]string, error) {
	if len(value) > 1024*1024 {
		return nil, fmt.Errorf("EMV table too large")
	}
	lines := strings.Split(strings.TrimPrefix(value, "\uFEFF"), "\n")
	kept := make([]string, 0, len(lines))
	for _, line := range lines {
		if !strings.HasPrefix(strings.TrimSpace(line), "//") {
			kept = append(kept, line)
		}
	}
	var records []string
	if err := json.Unmarshal([]byte(strings.Join(kept, "\n")), &records); err != nil || len(records) == 0 {
		return nil, fmt.Errorf("invalid EMV table JSON")
	}
	return records, nil
}
