package service

import (
	"context"
	"slices"
	"sync"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
)

// PaymentAuthorizationData é um snapshot sensível de uma operação concluída no
// pinpad, destinado à futura integração interna Go com o autorizador. Não é DTO
// público ou persistente. Getters transferem cópias; consumidor limpa cada cópia
// e chama Clear antes de liberar o snapshot. Não copiar este objeto por valor.
type PaymentAuthorizationData struct {
	mu                                sync.Mutex
	tracks                            *model.GTKResponse
	pinBlock, ksn, emvData, emvResult []byte
	dataMethod                        string
	dataKeyIndex                      int
}

// String impede representação textual incidental dos buffers privados.
func (d *PaymentAuthorizationData) String() string { return "PaymentAuthorizationData[private]" }

// GoString protege também a representação %#v de diagnósticos incidentais.
func (d *PaymentAuthorizationData) GoString() string { return d.String() }

// Tracks devolve cópia independente dos dados GTK disponíveis, incluindo KSNs.
// O consumidor é responsável por limpar os buffers dessa cópia depois do uso.
func (d *PaymentAuthorizationData) Tracks() *model.GTKResponse {
	if d == nil {
		return nil
	}
	d.mu.Lock()
	defer d.mu.Unlock()
	return clonePaymentTracks(d.tracks)
}

// PINBlock devolve cópia do PIN block online; vazio quando não foi devolvido.
func (d *PaymentAuthorizationData) PINBlock() []byte {
	if d == nil {
		return nil
	}
	d.mu.Lock()
	defer d.mu.Unlock()
	return slices.Clone(d.pinBlock)
}

// KSN devolve cópia do KSN PIN, separado dos KSNs DATA de Tracks.
func (d *PaymentAuthorizationData) KSN() []byte {
	if d == nil {
		return nil
	}
	d.mu.Lock()
	defer d.mu.Unlock()
	return slices.Clone(d.ksn)
}

// EMVData devolve os bytes efetivamente retornados por GOX; vazio para GPN.
func (d *PaymentAuthorizationData) EMVData() []byte {
	if d == nil {
		return nil
	}
	d.mu.Lock()
	defer d.mu.Unlock()
	return slices.Clone(d.emvData)
}

// EMVResult devolve os seis bytes de decisão GOX; vazio para GPN.
func (d *PaymentAuthorizationData) EMVResult() []byte {
	if d == nil {
		return nil
	}
	d.mu.Lock()
	defer d.mu.Unlock()
	return slices.Clone(d.emvResult)
}

// DATAMethod identifica os bytes GTK: vazio significa em claro;30 é DUKPT TDES.
func (d *PaymentAuthorizationData) DATAMethod() string {
	if d == nil {
		return ""
	}
	d.mu.Lock()
	defer d.mu.Unlock()
	return d.dataMethod
}

// DATAKeyIndex retorna índice efetivamente enviado no GTK ou -1 quando ausente.
// Não representa a seleção PIN nem o índice reservado no perfil para uso futuro.
func (d *PaymentAuthorizationData) DATAKeyIndex() int {
	if d == nil {
		return -1
	}
	d.mu.Lock()
	defer d.mu.Unlock()
	if d.tracks == nil {
		return -1
	}
	return d.dataKeyIndex
}

// Clear sobrescreve buffers próprios e é idempotente. Não alcança cópias já
// transferidas por getters ou snapshots, que pertencem ao respectivo consumidor.
func (d *PaymentAuthorizationData) Clear() {
	if d == nil {
		return
	}
	d.mu.Lock()
	defer d.mu.Unlock()
	clearPaymentTracks(d.tracks)
	d.tracks = nil
	clear(d.pinBlock)
	d.pinBlock = nil
	clear(d.ksn)
	d.ksn = nil
	clear(d.emvData)
	d.emvData = nil
	clear(d.emvResult)
	d.emvResult = nil
	d.dataMethod = ""
	d.dataKeyIndex = -1
}

// PaymentAuthorizationSnapshot copia os dados privados somente após GPN/GOX e
// para a operação da sessão aberta. Não envia I/O nem autoriza a transação.
// A futura integração solicita antes de FCX/close e limpa snapshot e cópias.
func (s *Service) PaymentAuthorizationSnapshot(ctx context.Context, operationID string) (*PaymentAuthorizationData, error) {
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	p := s.payment
	if p == nil || p.operationID != operationID || p.phase != "done" || s.state != model.StateOpen || p.tracks == nil {
		return nil, domainerror.ErrInvalidCommandSequence
	}
	return &PaymentAuthorizationData{tracks: clonePaymentTracks(p.tracks),
		pinBlock: slices.Clone(p.pinBlock), ksn: slices.Clone(p.ksn),
		emvData: slices.Clone(p.emvData), emvResult: slices.Clone(p.emvResult),
		dataMethod: p.dataMethod, dataKeyIndex: p.dataKeyIndex}, nil
}

// retainPaymentTracks transfere ownership somente à geração ativa; erro limpa
// a resposta tardia e nunca repõe um contexto cancelado ou desconectado.
func (s *Service) retainPaymentTracks(ctx context.Context, epoch uint64, tracks *model.GTKResponse, request command.GTKRequest) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	if err := s.finishPaymentStepLocked(ctx, epoch, "tracks"); err != nil {
		clearPaymentTracks(tracks)
		return err
	}
	s.payment.tracks = tracks
	s.payment.dataMethod = request.DataMethod
	s.payment.dataKeyIndex = -1
	if request.KeyIndex != nil {
		s.payment.dataKeyIndex = *request.KeyIndex
	}
	return nil
}

// retainPaymentResult copia resultado e fase juntos sob s.mu antes da entrega
// ao chamador, cuja limpeza não deve destruir dados da operação.
func (s *Service) retainPaymentResult(ctx context.Context, epoch uint64, pinBlock, ksn, emvData, result []byte) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	if err := s.finishPaymentStepLocked(ctx, epoch, "done"); err != nil {
		return err
	}
	p := s.payment
	p.pinBlock, p.ksn = slices.Clone(pinBlock), slices.Clone(ksn)
	p.emvData, p.emvResult = slices.Clone(emvData), slices.Clone(result)
	return nil
}

// clonePaymentTracks não compartilha nenhum backing array com a origem.
func clonePaymentTracks(t *model.GTKResponse) *model.GTKResponse {
	if t == nil {
		return nil
	}
	return &model.GTKResponse{EncryptedPAN: slices.Clone(t.EncryptedPAN),
		Track1: slices.Clone(t.Track1), Track2: slices.Clone(t.Track2), Track3: slices.Clone(t.Track3),
		Track1KSN: slices.Clone(t.Track1KSN), Track2KSN: slices.Clone(t.Track2KSN), Track3KSN: slices.Clone(t.Track3KSN),
		EncryptedPANKey: slices.Clone(t.EncryptedPANKey), EncryptedRandom: slices.Clone(t.EncryptedRandom)}
}

// clearPaymentTracks sobrescreve todos os buffers GTK controlados, inclusive
// KSN do PAN e chave aleatória; suporta resposta ausente ou campos vazios.
func clearPaymentTracks(t *model.GTKResponse) {
	if t == nil {
		return
	}
	clear(t.EncryptedPAN)
	clear(t.Track1)
	clear(t.Track2)
	clear(t.Track3)
	clear(t.Track1KSN)
	clear(t.Track2KSN)
	clear(t.Track3KSN)
	clear(t.EncryptedPANKey)
	clear(t.EncryptedRandom)
}
