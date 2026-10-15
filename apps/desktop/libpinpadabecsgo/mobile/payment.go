package mobile

import (
	"context"
	"encoding/json"
	"fmt"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
)

// ValidatePaymentPINProperties valida a fonte inteira antes de criar/abrir cliente.
// Não publica valores ou linhas inválidas; perfil aceito é exclusivamente DUKPT TDES.
func ValidatePaymentPINProperties(raw string) (err error) {
	defer recoverBinding(&err)
	_, err = command.ParsePaymentPINProperties(raw)
	return err
}

// PaymentPINConfigurationSummaryJSON retorna método/índice após validar WK necessariamente vazia.
// O resultado é configuração privada do adapter, nunca estado da UI.
func PaymentPINConfigurationSummaryJSON(raw string) (result string, err error) {
	defer recoverBinding(&err)
	config, err := command.ParsePaymentPINProperties(raw)
	if err != nil {
		return "{}", err
	}
	data, err := json.Marshal(struct {
		Method   string `json:"method"`
		KeyIndex int    `json:"keyIndex"`
	}{Method: config.Method, KeyIndex: config.KeyIndex})
	return string(data), err
}

// ValidatePaymentCandidatesJSON verifica filtros contra a tabela completa sem hardware.
func ValidatePaymentCandidatesJSON(acquirer, applicationTypes, recordsJSON string) (err error) {
	defer recoverBinding(&err)
	records, err := parseFormattedEMVJSON(recordsJSON)
	if err != nil {
		return err
	}
	return command.ValidatePaymentCandidates(records, acquirer, applicationTypes)
}

// CapturePaymentCardSummaryJSON inicia compra com três interfaces e contexto privado single use.
// Argumentos primitivos mantêm compatibilidade gomobile; não recebe nem retorna PAN/trilhas.
func (c *Client) CapturePaymentCardSummaryJSON(
	operationID, amount, date, clock, acquirer, applicationTypes, pinMethod string,
	pinKeyIndex int, workingKey string, timeoutSeconds int,
) (result string, err error) {
	defer recoverBinding(&err)
	request := command.FilteredGCXRequest{
		Amount: amount, Date: date, Clock: clock, AcquirerReference: acquirer,
		ApplicationTypes: applicationTypes, TransactionType: int(command.GCXTransactionPurchase),
		TimeoutSeconds: timeoutSeconds,
	}
	pin := command.PaymentPINConfig{Method: pinMethod, KeyIndex: pinKeyIndex, WorkingKey: workingKey}
	return c.runJSON(operationID, func(ctx context.Context) (any, error) {
		card, err := c.service.CapturePaymentCard(ctx, operationID, request, pin)
		if err != nil {
			return nil, err
		}
		return struct {
			SchemaVersion string `json:"schemaVersion"`
			CardType      string `json:"cardType"`
			ICCStatus     string `json:"iccStatus,omitempty"`
			AidTableInfo  string `json:"aidTableInfo,omitempty"`
		}{SchemaVersion: "1", CardType: card.CardType, ICCStatus: card.ICCStatus, AidTableInfo: card.AidTableInfo}, nil
	})
}

// GetPaymentTracksSummaryJSON executa GTK uma vez sem retornar cartão ao adapter.
// Método vazio omite criptografia/índice;30 envia DUKPT DATA no índice da fonte.
// keyIndex deve estar em0..99 mesmo quando reservado pelo perfil para uso futuro.
func (c *Client) GetPaymentTracksSummaryJSON(
	operationID, dataMethod string, keyIndex int,
) (result string, err error) {
	defer recoverBinding(&err)
	return c.runJSON(operationID, func(ctx context.Context) (any, error) {
		if keyIndex < 0 || keyIndex > 99 {
			c.service.InvalidatePaymentContext()
			return nil, fmt.Errorf("invalid payment DATA key index")
		}
		request := command.GTKRequest{Tracks: "0111", DataMethod: dataMethod}
		if dataMethod != "" {
			request.KeyIndex = &keyIndex
		}
		if err := c.service.PaymentTracks(ctx, operationID, request); err != nil {
			return nil, err
		}
		return struct {
			SchemaVersion string `json:"schemaVersion"`
		}{SchemaVersion: "1"}, nil
	})
}

// ContinuePaymentEMVSummaryJSON processa chip/CTLS EMV e deixa CVM/PIN no kernel.
// Compra e parâmetros são confrontados com o snapshot capturado no core.
func (c *Client) ContinuePaymentEMVSummaryJSON(
	operationID, acquirer, amount, pinMethod string, pinKeyIndex int, workingKey string, timeoutSeconds int,
) (result string, err error) {
	defer recoverBinding(&err)
	pin := command.PaymentPINConfig{Method: pinMethod, KeyIndex: pinKeyIndex, WorkingKey: workingKey}
	if err := pin.Validate(); err != nil {
		c.service.InvalidatePaymentContext()
		return "{}", publicFailure("command", err)
	}
	request, err := buildGOXRequest(
		acquirer, pinMethod, pinKeyIndex, workingKey, string([]byte{command.GCXTransactionPurchase}),
		amount, "", "", "01000", "", "", "", "", timeoutSeconds,
	)
	if err != nil {
		c.service.InvalidatePaymentContext()
		return "{}", publicFailure("command", err)
	}
	return c.runJSON(operationID, func(ctx context.Context) (any, error) {
		response, err := c.service.ContinuePaymentEMV(ctx, operationID, request)
		if err != nil {
			return nil, err
		}
		defer clear(response.PINBlock)
		defer clear(response.KSN)
		return struct {
			SchemaVersion string `json:"schemaVersion"`
			Result        string `json:"result"`
		}{SchemaVersion: "1", Result: string(response.Result)}, nil
	})
}

// CapturePaymentPIN usa PAN exclusivamente do contexto Go após GTK no ramo sem EMV.
// Configuração tem de ser idêntica à de GCX; PIN block/KSN são descartados no core.
func (c *Client) CapturePaymentPIN(
	operationID, pinMethod string, pinKeyIndex int, workingKey, message string,
) (err error) {
	defer recoverBinding(&err)
	pin := command.PaymentPINConfig{Method: pinMethod, KeyIndex: pinKeyIndex, WorkingKey: workingKey}
	return c.run(operationID, func(ctx context.Context) error {
		return c.service.CapturePaymentPIN(ctx, operationID, pin, message)
	})
}
