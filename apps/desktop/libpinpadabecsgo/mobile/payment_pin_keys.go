package mobile

import (
	"context"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
)

// ResolvePaymentPINConfiguration descobre índices por GIX e sorteia somente
// posições PIN DUKPTTDES presentes. A escolha é congelada para a sessão e usada
// pela integração em GCX/GOX/GPN; configuredIndex é legado, não preferência.
// Método/WK são da fonte. Não retorna chave secreta ou mapa raw do dispositivo.
func (c *Client) ResolvePaymentPINConfiguration(operationID, method string, configuredIndex int, workingKey string) (result *PaymentPINConfiguration, err error) {
	defer recoverBinding(&err)
	source := command.PaymentPINConfig{Method: method, KeyIndex: configuredIndex, WorkingKey: workingKey}
	err = c.run(operationID, func(ctx context.Context) error {
		selected, callErr := c.service.ResolvePaymentPINConfiguration(ctx, source)
		if callErr != nil {
			return publicFailure("preflight", callErr)
		}
		result = &PaymentPINConfiguration{value: selected}
		return nil
	})
	if err != nil {
		return nil, err
	}
	return result, nil
}
