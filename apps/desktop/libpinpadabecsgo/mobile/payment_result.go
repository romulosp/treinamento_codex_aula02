package mobile

import (
	"context"
	"sync"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
)

// PaymentResult é um retorno proprietário para a integração privada, nunca JSON/estado UI.
// Getters copiam buffers; Clear sobrescreve buffers controlados, sem garantia sobre cópias do runtime.
type PaymentResult struct {
	mu            sync.Mutex
	decision      string
	pinBlock, ksn []byte
}

// Result devolve somente a decisão EMV N6, vazia em GPN.
func (r *PaymentResult) Result() string {
	if r == nil {
		return ""
	}
	r.mu.Lock()
	defer r.mu.Unlock()
	return r.decision
}

// PINBlock copia o bloco PIN criptografado de 8 bytes, ou vazio quando não houve PIN online.
func (r *PaymentResult) PINBlock() []byte {
	if r == nil {
		return nil
	}
	r.mu.Lock()
	defer r.mu.Unlock()
	return append([]byte(nil), r.pinBlock...)
}

// KSN copia os 10 bytes DUKPT, sem registrar seu valor.
func (r *PaymentResult) KSN() []byte {
	if r == nil {
		return nil
	}
	r.mu.Lock()
	defer r.mu.Unlock()
	return append([]byte(nil), r.ksn...)
}

// Clear é idempotente e sincronizado com os getters; a decisão não contém dados de PIN.
func (r *PaymentResult) Clear() {
	if r == nil {
		return
	}
	r.mu.Lock()
	defer r.mu.Unlock()
	clear(r.pinBlock)
	clear(r.ksn)
	r.pinBlock, r.ksn = nil, nil
}

// PaymentPINConfiguration é o snapshot imutável da configuração PIN validada.
// Parse importa a fonte inteira; Resolve usa índice GIX, preservando método/WK.
// A homologação DUKPT TDES aceita apenas WK logicamente vazia; nenhum valor é inventado.
type PaymentPINConfiguration struct{ value command.PaymentPINConfig }

// Method devolve TIPO_CHAVE da fonte, sem substituição de método.
func (p *PaymentPINConfiguration) Method() string { return p.value.Method }

// KeyIndex devolve índice N2 da fonte (Parse) ou sorteado entre presentes GIX (Resolve).
func (p *PaymentPINConfiguration) KeyIndex() int { return p.value.KeyIndex }

// WorkingKey devolve WORKING_KEY importada e normalizada pelo parser.
func (p *PaymentPINConfiguration) WorkingKey() string { return p.value.WorkingKey }

// ParsePaymentPINConfiguration valida os três campos e retorna o snapshot privado sem JSON.
func ParsePaymentPINConfiguration(raw string) (result *PaymentPINConfiguration, err error) {
	defer recoverBinding(&err)
	value, err := command.ParsePaymentPINProperties(raw)
	if err != nil {
		return nil, err
	}
	return &PaymentPINConfiguration{value: value}, nil
}

// CapturePaymentPINResult retorna o par DUKPT TDES somente após GTK e consumo válido.
// Mantém a API void anterior; este objeto deve ser limpo pela integração privada.
func (c *Client) CapturePaymentPINResult(operationID, pinMethod string, pinKeyIndex int, workingKey, message string) (result *PaymentResult, err error) {
	defer recoverBinding(&err)
	pin := command.PaymentPINConfig{Method: pinMethod, KeyIndex: pinKeyIndex, WorkingKey: workingKey}
	err = c.run(operationID, func(ctx context.Context) error {
		block, ksn, callErr := c.service.CapturePaymentPINResult(ctx, operationID, pin, message)
		if callErr != nil {
			return callErr
		}
		result = &PaymentResult{pinBlock: block, ksn: ksn}
		return nil
	})
	if err != nil {
		result.Clear()
		return nil, err
	}
	return result, nil
}

// ContinuePaymentEMVResult devolve decisão e par somente se o kernel capturou PIN online.
// Não força CVM, não executa GPN e não registra blobs em diagnóstico de comandos.
func (c *Client) ContinuePaymentEMVResult(operationID, acquirer, amount, pinMethod string, pinKeyIndex int, workingKey string, timeoutSeconds int) (result *PaymentResult, err error) {
	defer recoverBinding(&err)
	pin := command.PaymentPINConfig{Method: pinMethod, KeyIndex: pinKeyIndex, WorkingKey: workingKey}
	if err = pin.Validate(); err != nil {
		if c != nil && c.service != nil {
			c.service.InvalidatePaymentContext()
		}
		return nil, publicFailure("command", err)
	}
	request, buildErr := buildGOXRequest(acquirer, pinMethod, pinKeyIndex, workingKey,
		string([]byte{command.GCXTransactionPurchase}), amount, "", "", "01000", "", "", "", "", timeoutSeconds)
	if buildErr != nil {
		if c != nil && c.service != nil {
			c.service.InvalidatePaymentContext()
		}
		return nil, publicFailure("command", buildErr)
	}
	err = c.run(operationID, func(ctx context.Context) error {
		response, callErr := c.service.ContinuePaymentEMV(ctx, operationID, request)
		if callErr != nil {
			return callErr
		}
		if response.Result[2] != '2' {
			clear(response.PINBlock)
			clear(response.KSN)
			response.PINBlock, response.KSN = nil, nil
		}
		result = &PaymentResult{decision: string(response.Result), pinBlock: response.PINBlock, ksn: response.KSN}
		return nil
	})
	if err != nil {
		result.Clear()
		return nil, err
	}
	return result, nil
}
