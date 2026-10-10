package service

import (
	"context"
	"fmt"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/state"
)

// PurchaseGCXFiltered mantém filtros e timeout nas mudanças normativas de interface.
// TransactionGCX completo continua reservado; esta operação somente captura.
func (s *Service) PurchaseGCXFiltered(ctx context.Context, request command.FilteredGCXRequest, enableCTLS, hideAmount bool) (*model.GCXResponse, error) {
	return s.purchaseGCX(ctx, enableCTLS, hideAmount, func(options byte) (*model.GCXResponse, error) {
		request.Options = options
		payload, err := command.BuildFilteredGCXCommand(request)
		if err != nil {
			return nil, err
		}
		return s.sendGCXPayload(ctx, payload)
	})
}

// LoadFormattedEMVTable valida toda a tabela antes de TLI e preserva TAB_LEN.
// Nenhum bloco parcialmente inválido inicia uma carga física.
func (s *Service) LoadFormattedEMVTable(ctx context.Context, acquirer, version string, records []string) error {
	if _, err := command.BuildTLICommand(acquirer, version); err != nil {
		return err
	}
	if err := command.ValidateFormattedEMVRecords(records, acquirer); err != nil {
		return err
	}
	batches := make([][]byte, 0)
	for start := 0; start < len(records); {
		var payload []byte
		end := start
		for end < len(records) && end-start < command.TLRMaxRecords {
			next, err := command.BuildFormattedTLRCommand(records[start : end+1])
			if err != nil {
				break
			}
			payload = next
			end++
		}
		if end == start {
			return fmt.Errorf("formatted EMV record exceeds TLR block")
		}
		batches = append(batches, payload)
		start = end
	}
	response, err := s.TableLoadInitiate(ctx, acquirer, version)
	if err != nil {
		return err
	}
	if response == nil || (response.StatusCode != state.StatusOK && response.StatusCode != state.StatusTableVersionDifferent) {
		return fmt.Errorf("invalid TLI status")
	}
	for _, payload := range batches {
		if _, err = s.sendPayload(ctx, command.CommandTLR, payload); err != nil {
			return err
		}
	}
	_, err = s.TableLoadEnd(ctx, version)
	return err
}
