package service

import (
	"context"
	"fmt"
	"log/slog"
	"strings"
	"unicode/utf8"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/parser"
)

// paymentContext pertence a uma captura, sessão e correlação. Nunca é serializado.
type paymentContext struct {
	operationID  string
	cardType     string
	phase        string
	pan          []byte
	pin          command.PaymentPINConfig
	amount       string
	acquirer     string
	attempt      int
	tracks       *model.GTKResponse
	pinBlock     []byte
	ksn          []byte
	emvData      []byte
	emvResult    []byte
	dataMethod   string
	dataKeyIndex int
}

// InvalidatePaymentContext descarta dados privados inclusive durante cancelamento/transporte perdido.
// A captura permanece consumida até uma nova abertura explícita da sessão.
func (s *Service) InvalidatePaymentContext() {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.clearPaymentLocked()
}

// clearPaymentLocked requer s.mu e invalida respostas tardias por geração.
func (s *Service) clearPaymentLocked() {
	if s.payment != nil {
		clear(s.payment.pan)
		clearPaymentTracks(s.payment.tracks)
		clear(s.payment.pinBlock)
		clear(s.payment.ksn)
		clear(s.payment.emvData)
		clear(s.payment.emvResult)
		s.payment = nil
	}
	s.paymentEpoch++
}

// CapturePaymentCard inicia uma única compra filtrada por sessão com snapshot PIN validado.
// Ramo sem EMV só avança se GCX fornecer PAN utilizável, mantido exclusivamente no core.
func (s *Service) CapturePaymentCard(
	ctx context.Context, operationID string, request command.FilteredGCXRequest, pin command.PaymentPINConfig,
) (*model.GCXResponse, error) {
	s.paymentMu.Lock()
	defer s.paymentMu.Unlock()
	if err := pin.Validate(); err != nil {
		s.InvalidatePaymentContext()
		return nil, err
	}
	if operationID == "" || len(operationID) > 128 || !utf8.ValidString(operationID) ||
		request.TransactionType != int(command.GCXTransactionPurchase) || request.AIDReferences != "" {
		s.InvalidatePaymentContext()
		return nil, fmt.Errorf("invalid payment capture")
	}
	if _, err := command.BuildFilteredGCXCommand(request); err != nil {
		s.InvalidatePaymentContext()
		return nil, err
	}
	s.mu.Lock()
	if s.paymentPINSelection != nil && s.paymentPINSelection.resolved != pin {
		s.clearPaymentLocked()
		s.mu.Unlock()
		return nil, domainerror.ErrInvalidCommandSequence
	}
	if s.paymentStarted || s.state != model.StateOpen {
		s.clearPaymentLocked()
		s.mu.Unlock()
		return nil, domainerror.ErrInvalidCommandSequence
	}
	if err := command.ValidatePaymentCandidates(s.paymentRecords, request.AcquirerReference, request.ApplicationTypes); err != nil {
		s.clearPaymentLocked()
		s.mu.Unlock()
		return nil, err
	}
	s.clearPaymentLocked()
	epoch := s.paymentEpoch
	s.paymentStarted = true
	s.payment = &paymentContext{operationID: operationID, phase: "capture", pin: pin}
	s.mu.Unlock()
	card, err := s.purchaseGCX(ctx, true, false, func(options byte) (*model.GCXResponse, error) {
		s.mu.Lock()
		if s.payment == nil || s.paymentEpoch != epoch {
			s.mu.Unlock()
			return nil, domainerror.ErrInvalidCommandSequence
		}
		s.payment.attempt++
		s.mu.Unlock()
		request.Options = options
		payload, buildErr := command.BuildFilteredGCXCommand(request)
		if buildErr != nil {
			return nil, buildErr
		}
		return s.readGCXPayload(ctx, payload)
	})
	if err != nil {
		s.InvalidatePaymentContext()
		return nil, err
	}
	if card.CardType != command.GCXCardMagnetic {
		s.mu.RLock()
		selectionErr := validatePaymentSelection(card.AidTableInfo, request, s.paymentRecords)
		s.mu.RUnlock()
		if selectionErr != nil {
			s.InvalidatePaymentContext()
			return nil, selectionErr
		}
	}
	pan := []byte{}
	if card.CardType == command.GCXCardMagnetic || card.CardType == command.GCXCardContactlessSimulated {
		pan, err = parser.PaymentPAN(card)
		if err != nil {
			s.InvalidatePaymentContext()
			return nil, err
		}
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	if ctx.Err() != nil || s.paymentEpoch != epoch || s.payment == nil {
		clear(pan)
		s.clearPaymentLocked()
		if ctx.Err() != nil {
			return nil, ctx.Err()
		}
		return nil, domainerror.ErrInvalidCommandSequence
	}
	s.payment.cardType = card.CardType
	s.payment.phase = "card"
	s.payment.pan = pan
	s.payment.amount = request.Amount
	s.payment.acquirer = request.AcquirerReference
	return card, nil
}

// validatePaymentSelection impede resposta incoerente de contornar modalidade/candidatos.
func validatePaymentSelection(info string, request command.FilteredGCXRequest, records []string) error {
	eligible := make(map[string]bool)
	for _, record := range records {
		if len(record) < 44 || record[3] != '1' {
			continue
		}
		for offset := 0; offset < len(request.ApplicationTypes); offset += 2 {
			if record[42:44] == request.ApplicationTypes[offset:offset+2] {
				eligible[record[4:8]+record[42:44]] = true
			}
		}
	}
	if info == "" || len(info)%6 != 0 {
		return domainerror.ErrInvalidResponse
	}
	for offset := 0; offset < len(info); offset += 6 {
		entry := info[offset : offset+6]
		if !strings.HasPrefix(entry, request.AcquirerReference) || !eligible[entry] {
			return domainerror.ErrInvalidResponse
		}
	}
	return nil
}

// PaymentTracks consome GTK uma vez e mantém sua resposta privada na operação.
// PAN de GPN permanece separado; GTK em claro ou DATA30 nunca integra texto/log.
func (s *Service) PaymentTracks(ctx context.Context, operationID string, request command.GTKRequest) error {
	s.paymentMu.Lock()
	defer s.paymentMu.Unlock()
	clearMode := request.DataMethod == "" && request.KeyIndex == nil
	dukptMode := request.DataMethod == "30" && request.KeyIndex != nil
	if !clearMode && !dukptMode {
		s.InvalidatePaymentContext()
		return fmt.Errorf("invalid payment DATA configuration")
	}
	epoch, err := s.beginPaymentStep(operationID, "card", "gtk")
	if err != nil {
		return err
	}
	tracks, err := s.getTracks(ctx, request)
	if err != nil {
		clearPaymentTracks(tracks)
		s.InvalidatePaymentContext()
		return err
	}
	return s.retainPaymentTracks(ctx, epoch, tracks, request)
}

// CapturePaymentPIN conserva a API anterior, descartando o par criptografado.
func (s *Service) CapturePaymentPIN(
	ctx context.Context, operationID string, pin command.PaymentPINConfig, message string,
) error {
	block, ksn, err := s.CapturePaymentPINResult(ctx, operationID, pin, message)
	clear(block)
	clear(ksn)
	return err
}

// CapturePaymentPINResult consome o contexto não EMV após GTK e transfere PIN block/KSN.
// O chamador é proprietário dos buffers retornados e deve sobrescrevê-los após consumo.
// Método/índice/WK vêm do snapshot validado da fonte; nunca recebe PAN do adapter.
// Uma cópia privada permanece na operação para o futuro autorizador até invalidação/close.
func (s *Service) CapturePaymentPINResult(
	ctx context.Context, operationID string, pin command.PaymentPINConfig, message string,
) ([]byte, []byte, error) {
	s.paymentMu.Lock()
	defer s.paymentMu.Unlock()
	if err := pin.Validate(); err != nil {
		s.InvalidatePaymentContext()
		return nil, nil, err
	}
	epoch, err := s.beginPaymentStep(operationID, "tracks", "gpn")
	if err != nil {
		return nil, nil, err
	}
	s.mu.Lock()
	p := s.payment
	if p == nil || p.pin != pin || (p.cardType != command.GCXCardMagnetic && p.cardType != command.GCXCardContactlessSimulated) {
		s.clearPaymentLocked()
		s.mu.Unlock()
		return nil, nil, domainerror.ErrInvalidCommandSequence
	}
	pan := append([]byte{}, p.pan...)
	clear(p.pan)
	p.pan = nil
	s.mu.Unlock()
	defer clear(pan)
	payload, err := command.BuildGPNCommandDUKPT(pin.KeyIndex, string(pan), message)
	if err != nil {
		s.InvalidatePaymentContext()
		return nil, nil, err
	}
	defer clear(payload)
	response, err := s.sendBlockingPayload(ctx, command.CommandGPN, payload)
	if err != nil {
		s.InvalidatePaymentContext()
		return nil, nil, err
	}
	pinBlock, ksn, err := command.ParseGPNResponse(response.Data)
	clear(response.Data)
	clear(response.RawData)
	if err == nil && (len(pinBlock) != 8 || len(ksn) != 10) {
		err = domainerror.ErrInvalidResponse
	}
	if err != nil {
		clear(pinBlock)
		clear(ksn)
		s.InvalidatePaymentContext()
		return nil, nil, err
	}
	if err = s.retainPaymentResult(ctx, epoch, pinBlock, ksn, nil, nil); err != nil {
		clear(pinBlock)
		clear(ksn)
		return nil, nil, err
	}
	return pinBlock, ksn, nil
}

// ContinuePaymentEMV deixa PIN sob controle do kernel somente para ICC/CTLS EMV.
// Compra, valor, adquirente e configuração PIN devem coincidir com o snapshot de GCX.
func (s *Service) ContinuePaymentEMV(
	ctx context.Context, operationID string, request command.GOXRequest,
) (*model.GOXResponse, error) {
	s.paymentMu.Lock()
	defer s.paymentMu.Unlock()
	epoch, err := s.beginPaymentStep(operationID, "tracks", "gox")
	if err != nil {
		return nil, err
	}
	s.mu.Lock()
	p := s.payment
	valid := p != nil && (p.cardType == command.GCXCardICC || p.cardType == command.GCXCardContactlessEMV) &&
		request.PinMethod == p.pin.Method && request.KeyIndex == p.pin.KeyIndex && len(request.WorkingKey) == 0 &&
		request.Amount == p.amount && request.AcquirerReference == p.acquirer &&
		len(request.TransactionType) == 1 && request.TransactionType[0] == command.GCXTransactionPurchase
	if !valid {
		s.clearPaymentLocked()
	}
	s.mu.Unlock()
	if !valid {
		return nil, domainerror.ErrInvalidCommandSequence
	}
	result, err := s.continueEMV(ctx, request)
	if err != nil {
		s.InvalidatePaymentContext()
		return nil, err
	}
	block, ksn := result.PINBlock, result.KSN
	if result.Result[2] != '2' {
		block, ksn = nil, nil
	}
	if err = s.retainPaymentResult(ctx, epoch, block, ksn, result.EMVData, result.Result); err != nil {
		clear(result.PINBlock)
		clear(result.KSN)
		return nil, err
	}
	s.mu.Lock()
	s.fcxEligible = result.Result[0] == '2'
	s.mu.Unlock()
	return result, nil
}

// beginPaymentStep reserva a transição antes do I/O; uso repetido/correlação alheia bloqueia envio.
func (s *Service) beginPaymentStep(operationID, expected, next string) (uint64, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.payment == nil || s.payment.operationID != operationID || s.payment.phase != expected || s.state != model.StateOpen {
		s.clearPaymentLocked()
		return 0, domainerror.ErrInvalidCommandSequence
	}
	s.payment.phase = next
	return s.paymentEpoch, nil
}

// finishPaymentStepLocked requer s.mu; publica dados/fase atomicamente e rejeita
// resultado tardio após cancelamento ou perda da sessão.
func (s *Service) finishPaymentStepLocked(ctx context.Context, epoch uint64, phase string) error {
	if ctx.Err() != nil || s.payment == nil || s.paymentEpoch != epoch {
		s.clearPaymentLocked()
		if ctx.Err() != nil {
			return ctx.Err()
		}
		return domainerror.ErrInvalidCommandSequence
	}
	s.payment.phase = phase
	return nil
}

// paymentMetadata fornece somente correlação e etapa permitidas para diagnóstico privado.
func (s *Service) paymentMetadata() []slog.Attr {
	s.mu.RLock()
	defer s.mu.RUnlock()
	if s.payment == nil {
		return []slog.Attr{slog.String("fase", "command"), slog.Int("tentativa", 1)}
	}
	return []slog.Attr{slog.String("correlationId", s.payment.operationID), slog.String("fase", s.payment.phase), slog.Int("tentativa", s.payment.attempt)}
}
