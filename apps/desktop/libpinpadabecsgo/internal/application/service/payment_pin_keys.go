package service

import (
	"context"
	"crypto/rand"
	"fmt"
	"io"
	"log/slog"
	"math/big"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/parser"
)

// paymentPINSelection pertence à sessão; fonte e escolha são valores imutáveis.
// Não contém chave secreta: este perfil aceita somente WK vazia.
type paymentPINSelection struct{ source, resolved command.PaymentPINConfig }

// ResolvePaymentPINConfiguration consulta GIX uma vez por sessão e sorteia um
// índice PIN DUKPTTDES carregado. Método/WK permanecem da fonte validada.
// Repetição com mesma fonte antes de GCX reutiliza seleção sem I/O/sorteio;
// depois de GCX ou fonte alterada, rejeita. Chaves nunca deixam o dispositivo.
func (s *Service) ResolvePaymentPINConfiguration(ctx context.Context, source command.PaymentPINConfig) (command.PaymentPINConfig, error) {
	s.paymentMu.Lock()
	defer s.paymentMu.Unlock()
	if err := ctx.Err(); err != nil {
		return command.PaymentPINConfig{}, err
	}
	if err := source.Validate(); err != nil {
		return command.PaymentPINConfig{}, err
	}
	s.mu.RLock()
	if s.state != model.StateOpen || s.paymentStarted || s.closing || s.protocolDesynchronized {
		s.mu.RUnlock()
		return command.PaymentPINConfig{}, domainerror.ErrInvalidCommandSequence
	}
	if selected := s.paymentPINSelection; selected != nil {
		s.mu.RUnlock()
		if selected.source != source {
			return command.PaymentPINConfig{}, domainerror.ErrInvalidCommandSequence
		}
		return selected.resolved, nil
	}
	epoch := s.paymentEpoch
	s.mu.RUnlock()
	response, err := s.GetInfoRaw(ctx)
	if err != nil {
		return command.PaymentPINConfig{}, err
	}
	indexes, err := parser.PaymentPINKeyIndexes(response)
	if response != nil {
		clear(response.Data)
		clear(response.RawData)
		for _, value := range response.RawTags {
			clear(value)
		}
	}
	if err != nil {
		return command.PaymentPINConfig{}, err
	}
	index, err := choosePaymentPINKey(indexes, rand.Reader)
	if err != nil {
		return command.PaymentPINConfig{}, err
	}
	resolved := source
	resolved.KeyIndex = index
	s.mu.Lock()
	if ctx.Err() != nil || s.paymentEpoch != epoch || s.state != model.StateOpen || s.paymentStarted || s.closing {
		s.mu.Unlock()
		if ctx.Err() != nil {
			return command.PaymentPINConfig{}, ctx.Err()
		}
		return command.PaymentPINConfig{}, domainerror.ErrInvalidCommandSequence
	}
	s.paymentPINSelection = &paymentPINSelection{source: source, resolved: resolved}
	logger := s.logger
	s.mu.Unlock()
	if logger != nil {
		logger.LogAttrs(ctx, slog.LevelInfo, "pin_key_selected", slog.String("metodo", resolved.Method),
			slog.Int("indice", index), slog.Int("quantidade", len(indexes)))
	}
	return resolved, nil
}

// choosePaymentPINKey escolhe uniformemente sem módulo enviesado. Reader local
// permite testar seleção/falha determinísticas, sem trocar aleatoriedade global.
func choosePaymentPINKey(indexes []int, entropy io.Reader) (int, error) {
	if len(indexes) == 0 {
		return 0, domainerror.ErrPaymentPINKeyMissing
	}
	if len(indexes) == 1 {
		return indexes[0], nil
	}
	position, err := rand.Int(entropy, big.NewInt(int64(len(indexes))))
	if err != nil {
		return 0, fmt.Errorf("PIN key selection unavailable: %w", err)
	}
	return indexes[position.Int64()], nil
}
