// Package service orquestra os casos de uso ABECS sobre uma porta serial, sem
// acoplar o domínio a um driver físico ou ao formato de logging.
package service

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/parser"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/state"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/worker"
	"bytes"
	"context"
	"crypto/rsa"
	"errors"
	"fmt"
	"log/slog"
	"os"
	"sync"
	"time"
)

// SerialPort define o transporte serial que a fachada usa exclusivamente.
type SerialPort interface {
	Open() error
	Close() error
	Read(context.Context) ([]byte, error)
	Write([]byte) error
	IsOpen() bool
}

type traceCommandPort interface {
	SetTraceCommand(command.Type)
}

type tracePolicyPort interface {
	SetTracePolicy(command.Type, bool)
}

// PinpadService expõe operações tipadas, sem comandos brutos nem rede.
type PinpadService interface {
	Open(context.Context) error
	Close(context.Context) error
	Reset(context.Context) error
	SendCommand(context.Context, command.Command) (*model.Response, error)
	GetInfo(context.Context) (*model.DeviceInfo, error)
	GetInfoRaw(context.Context) (*model.Response, error)
	GetDisplayCapabilities(context.Context) (*model.DisplayCapabilities, error)
	CloseVisual(context.Context, command.CLXRequest) (*model.Response, error)
	OpenSecure(context.Context, *rsa.PrivateKey) error
	GetTracks(context.Context, command.GTKRequest) (*model.GTKResponse, error)
	ContinueEMV(context.Context, command.GOXRequest) (*model.GOXResponse, error)
	FinalizeEMV(context.Context, command.FCXRequest) (*model.FCXResponse, error)
	GetState() model.PinpadState
	SendMultimediaFile(context.Context, string, []byte, ProgressFunc) error
	LoadQRCodeMultimedia(context.Context, string, string, int, ProgressFunc) error
	DisplayImage(context.Context, string) (*model.Response, error)
	ListMultimediaFiles(context.Context) ([]string, error)
	DeleteMultimediaFiles(context.Context, []string) (*model.Response, error)
	LoadCompleteEMVTable(context.Context, string, string, []string, ProgressFunc) error
	PurchaseGCX(context.Context, string, string, string, bool, bool) (*model.GCXResponse, error)
	DisplayQRCode(context.Context, string, int, int, int, int) (QRCodeResult, error)
	TransactionGCX(context.Context, TransactionGCXRequest) (*model.GCXResponse, error)
}

// QRCodeGenerator produz uma imagem PNG sem acoplar a biblioteca a um gerador.
type QRCodeGenerator interface {
	Generate(data string, size int) ([]byte, error)
}

// QRCodeResult contém a imagem produzida e a limitação explícita de posição.
type QRCodeResult struct {
	PNG               []byte
	X, Y              int
	PositionSupported bool
	PositionWarning   string
}

// ProgressFunc recebe progresso e pode cancelar uma operação de transferência.
type ProgressFunc func(context.Context, int64, int64) error

// TransactionGCXRequest reserva os campos do fluxo GCX ainda não implementado.
type TransactionGCXRequest struct {
	TransactionType   string
	AcquirerReference string
	ApplicationType   string
	AIDs              []string
	Cashback          string
	Currency          string
	PANMask           string
	EMVData           []byte
	Tags              map[string]string
}

// Service serializa operações de hardware e mantém seu ciclo de vida.
type Service struct {
	mu                     sync.RWMutex
	config                 model.PinpadConfig
	port                   SerialPort
	state                  model.PinpadState
	queue                  *worker.Queue
	stream                 *byteStream
	stopping               bool
	protocolDesynchronized bool
	opening                bool
	closing                bool
	inFlight               int
	logger                 *slog.Logger
	tracer                 *logging.Tracer
	qrCodeGenerator        QRCodeGenerator
	tracksEligible         bool
	goxEligible            bool
	fcxEligible            bool
	secureSession          *protocol.SecureSession
	traceSensitive         map[command.Type]bool
}

// SetQRCodeGenerator instala o gerador usado por DisplayQRCode.
func (s *Service) SetQRCodeGenerator(generator QRCodeGenerator) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.qrCodeGenerator = generator
}

// DisplayQRCode gera, carrega e exibe um QR Code como mídia PNG no pinpad.
func (s *Service) DisplayQRCode(ctx context.Context, data string, size, margin, xPos, yPos int) (QRCodeResult, error) {
	if err := ctx.Err(); err != nil {
		return QRCodeResult{}, err
	}
	if size < 50 || size > 320 {
		return QRCodeResult{}, fmt.Errorf("QR code size must be between 50 and 320")
	}
	if margin < 0 || margin > 10 {
		return QRCodeResult{}, fmt.Errorf("QR code margin must be between 0 and 10")
	}
	s.mu.RLock()
	generator := s.qrCodeGenerator
	s.mu.RUnlock()
	if generator == nil {
		return QRCodeResult{}, domainerror.ErrQRCodeGeneratorNotConfigured
	}
	png, err := generator.Generate(data, size)
	if err != nil {
		return QRCodeResult{}, fmt.Errorf("generate QR code: %w", err)
	}
	result := QRCodeResult{PNG: append([]byte(nil), png...), X: xPos, Y: yPos}
	if xPos != 0 || yPos != 0 {
		result.PositionWarning = "xPos and yPos are not supported by ABECS"
	}
	return result, nil
}

// LoadQRCodeMultimedia consulta o display, escolhe uma dimensão compatível,
// gera PNG e executa MLI/MLR/MLE sem solicitar caminho de arquivo ao operador.
func (s *Service) LoadQRCodeMultimedia(ctx context.Context, name, data string, requestedSize int, progress ProgressFunc) error {
	capabilities, err := s.GetDisplayCapabilities(ctx)
	if err != nil {
		return fmt.Errorf("consultar capacidades do display: %w", err)
	}
	if !capabilities.HasGraphic || !capabilities.SupportsPNG {
		return domainerror.ErrUnsupportedMedia
	}
	size := requestedSize
	if size == 0 {
		size = capabilities.GraphicWidth
		if capabilities.GraphicHeight > 0 && capabilities.GraphicHeight < size {
			size = capabilities.GraphicHeight
		}
	}
	if size < 50 || size > 320 {
		return fmt.Errorf("resolucao do display incompatível com QR Code: %dx%d", capabilities.GraphicWidth, capabilities.GraphicHeight)
	}
	result, err := s.DisplayQRCode(ctx, data, size, 0, 0, 0)
	if err != nil {
		return err
	}
	return s.SendMultimediaFile(ctx, name, result.PNG, progress)
}

// TransactionGCX reserva a transação completa, ainda não implementada nesta Change.
func (s *Service) TransactionGCX(context.Context, TransactionGCXRequest) (*model.GCXResponse, error) {
	return nil, domainerror.ErrNotImplemented
}

// New cria uma fachada independente com fila FIFO e transporte informado.
func New(cfg model.PinpadConfig, port SerialPort) *Service {
	return &Service{config: cfg, port: port, state: model.StateClosed, queue: worker.New(100), stream: newByteStream(port), logger: logging.New(), tracer: logging.NewTracer()}
}

// SetLogger substitui o destino estruturado de telemetria. O logger recebe
// somente metadados e nunca payloads de comandos sensíveis.
func (s *Service) SetLogger(logger *slog.Logger) {
	if logger == nil {
		return
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	s.logger = logger
}

// SetTracer associa o rastro serial usado pelo serviço para registrar RSP. A
// mesma instância deve ser configurada no adaptador serial antes de Open para
// que SPE, PP, abertura e fechamento pertençam à mesma sessão. Um valor nil
// desabilita o rastro da fachada sem alterar o fluxo do comando.
func (s *Service) SetTracer(tracer *logging.Tracer) {
	if tracer == nil {
		tracer = logging.NewTracer()
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	s.tracer = tracer
}

// SetTraceSensitive permite que o consumidor imponha redação total a um tipo
// de comando cujo conteúdo de negócio seja sensível no seu contexto.
func (s *Service) SetTraceSensitive(kind command.Type, sensitive bool) {
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.traceSensitive == nil {
		s.traceSensitive = make(map[command.Type]bool)
	}
	if sensitive {
		s.traceSensitive[kind] = true
		return
	}
	delete(s.traceSensitive, kind)
}

// SetConfig valida e substitui a configuração enquanto o serviço está fechado.
func (s *Service) SetConfig(cfg model.PinpadConfig) error {
	if cfg.Port == "" || cfg.BaudRate <= 0 || cfg.Timeout <= 0 {
		return fmt.Errorf("invalid pinpad configuration")
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.state != model.StateClosed {
		return domainerror.ErrPinpadBusy
	}
	s.config = cfg
	return nil
}

// GetConfig devolve uma cópia da configuração atual.
func (s *Service) GetConfig() model.PinpadConfig {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.config
}

var _ PinpadService = (*Service)(nil)

// Open abre a porta serial, limpa comunicação residual com CAN/EOT e envia OPN.
// repetidas após uma abertura bem-sucedida são idempotentes.
func (s *Service) Open(ctx context.Context) error {
	if err := ctx.Err(); err != nil {
		return err
	}
	s.mu.Lock()
	if s.state != model.StateClosed {
		if s.protocolDesynchronized {
			s.mu.Unlock()
			return domainerror.ErrPinpadDesynchronized
		}
		s.mu.Unlock()
		return nil
	}
	if s.port == nil {
		s.mu.Unlock()
		return domainerror.ErrPortUnavailable
	}
	s.opening = true
	s.state = model.StateBusy
	if err := s.port.Open(); err != nil {
		s.opening = false
		s.state = model.StateClosed
		s.mu.Unlock()
		return fmt.Errorf("open pinpad: %w", err)
	}
	s.stopping = false
	s.mu.Unlock()

	_, err := s.submit(ctx, command.Command{Type: command.CommandOPN, Execute: func(operationContext context.Context) (*model.Response, error) {
		if err := s.cancelHandshake(operationContext); err != nil {
			return nil, fmt.Errorf("initial CAN: %w", err)
		}
		return s.exchangePayloadWithoutRecovery(operationContext, command.CommandOPN, []byte("OPN"), false)
	}})
	if err != nil {
		closeErr := s.port.Close()
		s.mu.Lock()
		s.opening = false
		s.state = model.StateClosed
		s.stream = newByteStream(s.port)
		s.mu.Unlock()
		if closeErr != nil {
			return fmt.Errorf("OPN: %w; close pinpad: %v", err, closeErr)
		}
		return fmt.Errorf("OPN: %w", err)
	}
	s.mu.Lock()
	s.opening = false
	s.state = model.StateOpen
	s.protocolDesynchronized = false
	s.mu.Unlock()
	return nil
}

// OpenSecure abre a porta e estabelece KSEC diretamente por OPN seguro. A chave
// privada permanece sob controle exclusivo do consumidor.
func (s *Service) OpenSecure(ctx context.Context, privateKey *rsa.PrivateKey) error {
	if privateKey == nil {
		return fmt.Errorf("secure OPN private key is required")
	}
	payload, err := protocol.BuildSecureOPN(&privateKey.PublicKey)
	if err != nil {
		return err
	}
	s.mu.Lock()
	if s.state != model.StateClosed {
		s.mu.Unlock()
		return domainerror.ErrPinpadBusy
	}
	if s.port == nil {
		s.mu.Unlock()
		return domainerror.ErrPortUnavailable
	}
	s.opening = true
	s.state = model.StateBusy
	if err = s.port.Open(); err != nil {
		s.opening = false
		s.state = model.StateClosed
		s.mu.Unlock()
		return fmt.Errorf("open pinpad: %w", err)
	}
	s.stopping = false
	s.mu.Unlock()
	response, err := s.submit(ctx, command.Command{Type: command.CommandOPN, Execute: func(operationContext context.Context) (*model.Response, error) {
		if err := s.cancelHandshake(operationContext); err != nil {
			return nil, fmt.Errorf("initial CAN: %w", err)
		}
		return s.exchangePayloadWithoutRecovery(operationContext, command.CommandOPN, payload, true)
	}})
	if err != nil {
		_ = s.port.Close()
		s.mu.Lock()
		s.opening = false
		s.state = model.StateClosed
		s.stream = newByteStream(s.port)
		s.mu.Unlock()
		return fmt.Errorf("secure OPN: %w", err)
	}
	session, err := protocol.EstablishSecureSession(privateKey, response.RawData)
	if err != nil {
		return fmt.Errorf("establish secure OPN: %w", err)
	}
	s.mu.Lock()
	if s.stopping {
		s.mu.Unlock()
		session.Close()
		return domainerror.ErrPinpadClosed
	}
	if s.secureSession != nil {
		s.secureSession.Close()
	}
	s.secureSession = session
	s.opening = false
	s.state = model.StateOpen
	s.mu.Unlock()
	return nil
}

// Close encerra o diálogo ABECS por CLO032 + S32 antes de liberar a porta.
// Ele recusa uma operação normal enquanto comandos já aceitos estiverem ativos.
func (s *Service) Close(ctx context.Context) error {
	if err := ctx.Err(); err != nil {
		return err
	}
	s.mu.Lock()
	if s.state == model.StateClosed {
		s.mu.Unlock()
		return nil
	}
	if s.state == model.StateBusy || s.inFlight > 0 || s.opening || s.closing {
		s.mu.Unlock()
		return domainerror.ErrPinpadBusy
	}
	desynchronized := s.protocolDesynchronized
	s.closing = true
	s.state = model.StateBusy
	s.mu.Unlock()

	var protocolErr error
	if !desynchronized {
		_, protocolErr = s.submit(ctx, command.Command{Type: command.CommandCLO, Execute: func(operationContext context.Context) (*model.Response, error) {
			return s.exchangePayload(operationContext, command.CommandCLO, command.BuildPacketPayload(command.CommandCLO, fmt.Sprintf("%-32s", "")))
		}})
	}
	s.mu.Lock()
	closeErr := error(nil)
	if s.port != nil {
		closeErr = s.port.Close()
	}
	s.state = model.StateClosed
	s.closing = false
	s.protocolDesynchronized = false
	s.stream = newByteStream(s.port)
	if s.secureSession != nil {
		s.secureSession.Close()
		s.secureSession = nil
	}
	s.mu.Unlock()
	if protocolErr != nil && closeErr != nil {
		return fmt.Errorf("CLO: %w; close pinpad: %v", protocolErr, closeErr)
	}
	if protocolErr != nil {
		return fmt.Errorf("CLO: %w", protocolErr)
	}
	if closeErr != nil {
		return fmt.Errorf("close pinpad: %w", closeErr)
	}
	return nil
}

// Shutdown interrompe a fila, cancela a operação em andamento e fecha a porta.
// É idempotente e impede novas submissões antes de iniciar o encerramento.
func (s *Service) Shutdown() {
	s.mu.Lock()
	if s.stopping {
		s.mu.Unlock()
		return
	}
	s.stopping = true
	s.mu.Unlock()
	s.queue.Stop()
	s.mu.Lock()
	if s.port != nil {
		_ = s.port.Close()
	}
	s.state = model.StateClosed
	s.opening = false
	s.closing = false
	s.protocolDesynchronized = false
	s.stream = newByteStream(s.port)
	if s.secureSession != nil {
		s.secureSession.Close()
		s.secureSession = nil
	}
	s.mu.Unlock()
}

// GetState devolve o estado atual do ciclo de vida do pinpad.
func (s *Service) GetState() model.PinpadState {
	s.mu.RLock()
	defer s.mu.RUnlock()
	if s.protocolDesynchronized {
		return model.StateDesynchronized
	}
	return s.state
}

// Reset cancela a operação corrente por CAN/EOT e reconecta a porta quando o
// firmware não encerra o diálogo depois das três tentativas normativas.
func (s *Service) Reset(ctx context.Context) error {
	_, err := s.SendCommand(ctx, command.Command{Type: command.CommandCAN, Execute: func(operationContext context.Context) (*model.Response, error) {
		if handshakeErr := s.cancelHandshake(operationContext); handshakeErr != nil {
			if reconnectErr := s.reconnectProtocol(operationContext); reconnectErr != nil {
				return nil, errors.Join(
					fmt.Errorf("reset CAN/EOT: %w", handshakeErr),
					reconnectErr,
				)
			}
		}
		return &model.Response{AckType: "EOT"}, nil
	}})
	return err
}

// GetInfo executa GIX e converte suas tags em informações tipadas.
func (s *Service) GetInfo(ctx context.Context) (*model.DeviceInfo, error) {
	r, err := s.GetInfoRaw(ctx)
	if err != nil {
		return nil, err
	}
	info := parser.DeviceInfoFromResponse(r)
	return &info, nil
}

// GetInfoRaw executa GIX e preserva a resposta ABECS completa.
func (s *Service) GetInfoRaw(ctx context.Context) (*model.Response, error) {
	return s.SendCommand(ctx, command.Command{Type: command.CommandGIX, Execute: func(operationContext context.Context) (*model.Response, error) {
		return s.exchangePayload(operationContext, command.CommandGIX, []byte("GIX000"))
	}})
}

// GetDisplayCapabilities interpreta as capacidades de tela retornadas por GIX.
func (s *Service) GetDisplayCapabilities(ctx context.Context) (*model.DisplayCapabilities, error) {
	r, err := s.GetInfoRaw(ctx)
	if err != nil {
		return nil, err
	}
	capabilities := parser.DisplayCapabilitiesFromResponse(r)
	return &capabilities, nil
}

// sendPayload envia um payload logico ABECS (CMD_ID + LEN + parametros,
// ainda sem enquadramento) apos aplicar o enquadramento SYN/ETB/CRC exigido
// pelo protocolo (equivalente ao buildPacket() da referencia C++).
func (s *Service) sendPayload(ctx context.Context, kind command.Type, payload []byte) (*model.Response, error) {
	return s.sendPayloadWithTracePolicy(ctx, kind, payload, false)
}

func (s *Service) sendPayloadWithTracePolicy(ctx context.Context, kind command.Type, payload []byte, redact bool) (*model.Response, error) {
	return s.SendCommand(ctx, command.Command{Type: kind, Execute: func(op context.Context) (*model.Response, error) {
		return s.exchangePayloadWithTracePolicy(op, kind, payload, redact)
	}})
}

// sendBlockingPayload envia um comando blocante preservando o prazo definido
// pelo consumidor, sem reduzi-lo ao timeout genérico da configuração.
func (s *Service) sendBlockingPayload(ctx context.Context, kind command.Type, payload []byte) (*model.Response, error) {
	return s.sendCommand(ctx, command.Command{Type: kind, Execute: func(op context.Context) (*model.Response, error) {
		return s.exchangePayloadWithTracePolicy(op, kind, payload, false)
	}}, false)
}

func (s *Service) exchangePayload(ctx context.Context, kind command.Type, payload []byte) (*model.Response, error) {
	return s.exchangePayloadWithTracePolicy(ctx, kind, payload, false)
}

func (s *Service) exchangePayloadWithTracePolicy(ctx context.Context, kind command.Type, payload []byte, redact bool) (*model.Response, error) {
	return s.exchangePayloadWithRecovery(ctx, kind, payload, redact, true)
}

func (s *Service) exchangePayloadWithoutRecovery(ctx context.Context, kind command.Type, payload []byte, redact bool) (*model.Response, error) {
	return s.exchangePayloadWithRecovery(ctx, kind, payload, redact, false)
}

func (s *Service) exchangePayloadWithRecovery(ctx context.Context, kind command.Type, payload []byte, redact, recoverOnFailure bool) (*model.Response, error) {
	s.mu.RLock()
	session := s.secureSession
	s.mu.RUnlock()
	if session != nil && kind != command.CommandOPN {
		protected, err := session.Protect(payload)
		if err != nil {
			return nil, err
		}
		frame, err := protocol.BuildPacketChecked(protected)
		if err != nil {
			return nil, err
		}
		response, err := s.exchangeCommandWithRecovery(ctx, kind, frame, true, recoverOnFailure)
		return s.validateAndRecoverResponse(ctx, kind, response, err, recoverOnFailure)
	}
	frame, err := protocol.BuildPacketChecked(payload)
	if err != nil {
		return nil, err
	}
	response, err := s.exchangeCommandWithRecovery(ctx, kind, frame, redact, recoverOnFailure)
	return s.validateAndRecoverResponse(ctx, kind, response, err, recoverOnFailure)
}

func (s *Service) validateAndRecoverResponse(ctx context.Context, kind command.Type, response *model.Response, err error, recoverOnFailure bool) (*model.Response, error) {
	validated, validationErr := validateResponseCommand(kind, response, err)
	if recoverOnFailure && (errors.Is(validationErr, domainerror.ErrInvalidResponse) || errors.Is(validationErr, domainerror.ErrChecksumInvalid)) {
		return validated, s.recoverProtocol(ctx, validationErr)
	}
	return validated, validationErr
}

func validateResponseCommand(kind command.Type, response *model.Response, err error) (*model.Response, error) {
	if err != nil || response == nil || response.AckType == "EOT" {
		return response, err
	}
	if len(response.RawData) < 3 || string(response.RawData[:3]) != string(kind) {
		return nil, domainerror.ErrInvalidResponse
	}
	return response, nil
}

// DisplayDSP exibe duas linhas fixas de 16 bytes no display.
func (s *Service) DisplayDSP(ctx context.Context, line1, line2 string) (*model.Response, error) {
	payload, err := command.BuildDSPCommand(line1, line2)
	if err != nil {
		return nil, err
	}
	return s.sendPayload(ctx, command.CommandDSP, payload)
}

// CloseVisual envia CLX para limpar ou atualizar o display sem encerrar a
// porta serial nem alterar o ciclo de vida da comunicação ABECS.
func (s *Service) CloseVisual(ctx context.Context, request command.CLXRequest) (*model.Response, error) {
	payload, err := command.BuildCLXCommand(request)
	if err != nil {
		return nil, err
	}
	response, err := s.sendPayload(ctx, command.CommandCLX, payload)
	if err != nil {
		return nil, err
	}
	s.mu.Lock()
	if s.secureSession != nil {
		s.secureSession.Close()
		s.secureSession = nil
	}
	s.mu.Unlock()
	return response, nil
}

// GetTracks executa GTK uma única vez após GCX elegível. O retorno contém
// dados sensíveis e a fachada não o registra nem o adiciona a GCXResponse.
func (s *Service) GetTracks(ctx context.Context, request command.GTKRequest) (*model.GTKResponse, error) {
	s.mu.Lock()
	if !s.tracksEligible {
		s.mu.Unlock()
		return nil, domainerror.ErrInvalidCommandSequence
	}
	s.mu.Unlock()
	payload, err := command.BuildGTKCommand(request)
	if err != nil {
		return nil, err
	}
	response, err := s.sendPayload(ctx, command.CommandGTK, payload)
	if err != nil {
		return nil, err
	}
	s.mu.Lock()
	s.tracksEligible = false
	s.mu.Unlock()
	return parser.ValidateGTKResponse(response, request)
}

// ContinueEMV envia GOX somente depois de uma captura GCX ICC ou CTLS EMV.
func (s *Service) ContinueEMV(ctx context.Context, request command.GOXRequest) (*model.GOXResponse, error) {
	s.mu.Lock()
	if !s.goxEligible {
		s.mu.Unlock()
		return nil, domainerror.ErrInvalidCommandSequence
	}
	s.mu.Unlock()
	payload, err := command.BuildGOXCommand(request)
	if err != nil {
		return nil, err
	}
	response, err := s.sendBlockingPayload(ctx, command.CommandGOX, payload)
	if err != nil {
		return nil, err
	}
	result, err := parser.ValidateGOXResponse(response, request)
	if err != nil {
		return nil, err
	}
	s.mu.Lock()
	s.goxEligible = false
	s.fcxEligible = true
	s.mu.Unlock()
	return result, nil
}

// FinalizeEMV envia FCX depois de GOX e mantém Issuer Script Results no
// modelo próprio da finalização.
func (s *Service) FinalizeEMV(ctx context.Context, request command.FCXRequest) (*model.FCXResponse, error) {
	s.mu.Lock()
	if !s.fcxEligible {
		s.mu.Unlock()
		return nil, domainerror.ErrInvalidCommandSequence
	}
	s.mu.Unlock()
	payload, err := command.BuildFCXCommand(request)
	if err != nil {
		return nil, err
	}
	response, err := s.sendBlockingPayload(ctx, command.CommandFCX, payload)
	if err != nil {
		return nil, err
	}
	result, err := parser.ValidateFCXResponse(response, request)
	if err != nil {
		return nil, err
	}
	s.mu.Lock()
	s.fcxEligible = false
	s.mu.Unlock()
	return result, nil
}

// DisplayDEX exibe uma mensagem estendida de até 160 bytes.
func (s *Service) DisplayDEX(ctx context.Context, message string) (*model.Response, error) {
	payload, err := command.BuildDEXCommand(message)
	if err != nil {
		return nil, err
	}
	return s.sendPayload(ctx, command.CommandDEX, payload)
}

// DisplayMNU apresenta até 20 opções e devolve o índice escolhido pelo operador.
func (s *Service) DisplayMNU(ctx context.Context, timeout int, title string, options []string) (parser.MNUResponse, error) {
	payload, err := command.BuildMNUCommand(timeout, title, options)
	if err != nil {
		return parser.MNUResponse{}, err
	}
	r, err := s.sendBlockingPayload(ctx, command.CommandMNU, payload)
	if err != nil {
		return parser.MNUResponse{}, err
	}
	return parser.ParseMNUResponse(append([]byte(r.StatusCode), r.Data...))
}

// WaitForKeyPress executa GKY e converte RSP_STAT na tecla ABECS correspondente.
func (s *Service) WaitForKeyPress(ctx context.Context, timeout int) (byte, error) {
	if timeout < 0 {
		return 0, fmt.Errorf("invalid GKY timeout")
	}
	operationContext := ctx
	cancel := func() {}
	if timeout > 0 {
		operationContext, cancel = context.WithTimeout(ctx, time.Duration(timeout)*time.Second)
	}
	defer cancel()
	r, err := s.sendBlockingPayload(operationContext, command.CommandGKY, command.BuildGKYCommand())
	if err != nil {
		var statusErr *domainerror.StatusError
		if !errors.As(err, &statusErr) {
			return 0, err
		}
	}
	return parser.ParseGKYStatus(r.StatusCode)
}

// SendGCXCommand inicia a captura de cartão com os campos GCX informados.
func (s *Service) SendGCXCommand(ctx context.Context, amount, date, clock string, options byte) (*model.GCXResponse, error) {
	payload, err := command.BuildGCXCommand(amount, date, clock, options)
	if err != nil {
		return nil, err
	}
	r, err := s.sendBlockingPayload(ctx, command.CommandGCX, payload)
	if err != nil {
		return nil, err
	}
	result, err := parser.ValidateGCXResponse(r)
	if err != nil {
		return nil, fmt.Errorf("%w: %v", domainerror.ErrInvalidResponse, err)
	}
	s.mu.Lock()
	s.tracksEligible = true
	s.goxEligible = result.CardType == command.GCXCardICC || result.CardType == command.GCXCardContactlessEMV
	s.fcxEligible = false
	s.mu.Unlock()
	return result, nil
}

// SendMultimediaFile carrega um arquivo em blocos e somente o exibe após a confirmação.
func (s *Service) SendMultimediaFile(ctx context.Context, name string, data []byte, progress ProgressFunc) error {
	if len(data) == 0 {
		return fmt.Errorf("multimedia file is empty")
	}
	if err := ctx.Err(); err != nil {
		return err
	}
	mli, err := command.BuildMLICommand(name, data)
	if err != nil {
		return err
	}
	if _, err := s.sendPayload(ctx, command.CommandMLI, mli); err != nil {
		return fmt.Errorf("MLI: %w", err)
	}
	for offset := 0; offset < len(data); offset += command.MLRMaxBlockSize {
		end := offset + command.MLRMaxBlockSize
		if end > len(data) {
			end = len(data)
		}
		block, err := command.BuildMLRCommand(data[offset:end])
		if err != nil {
			return err
		}
		if _, err = s.sendPayload(ctx, command.CommandMLR, block); err != nil {
			return fmt.Errorf("MLR: %w", err)
		}
		if progress != nil && end < len(data) {
			if err = progress(ctx, int64(end), int64(len(data))); err != nil {
				return err
			}
		}
	}
	if _, err := s.sendPayload(ctx, command.CommandMLE, command.BuildMLECommand()); err != nil {
		return fmt.Errorf("MLE: %w", err)
	}
	if progress != nil {
		return progress(ctx, int64(len(data)), int64(len(data)))
	}
	return nil
}

// LoadMultimediaFile é um alias de SendMultimediaFile preservado para a fachada.
func (s *Service) LoadMultimediaFile(ctx context.Context, name string, data []byte, progress ProgressFunc) error {
	return s.SendMultimediaFile(ctx, name, data, progress)
}

// DisplayImage exibe pelo nome uma mídia previamente carregada.
func (s *Service) DisplayImage(ctx context.Context, name string) (*model.Response, error) {
	payload, err := command.BuildDSICommand(name)
	if err != nil {
		return nil, err
	}
	return s.sendPayload(ctx, command.CommandDSI, payload)
}

// DisplayDSI é um alias explícito da operação oferecida por DisplayImage.
func (s *Service) DisplayDSI(ctx context.Context, name string) (*model.Response, error) {
	return s.DisplayImage(ctx, name)
}

// ListMultimediaFiles devolve nomes das mídias carregadas em maiúsculas.
func (s *Service) ListMultimediaFiles(ctx context.Context) ([]string, error) {
	response, err := s.sendPayload(ctx, command.CommandLMF, command.BuildLMFCommand())
	if err != nil {
		return nil, err
	}
	names, err := parser.MultimediaFileNames(response)
	if errors.Is(err, domainerror.ErrInvalidResponse) {
		return nil, s.recoverProtocol(ctx, err)
	}
	return names, err
}

// DeleteMultimediaFiles solicita a exclusão de uma ou mais mídias pelo nome.
func (s *Service) DeleteMultimediaFiles(ctx context.Context, names []string) (*model.Response, error) {
	payload, err := command.BuildDMFCommand(names)
	if err != nil {
		return nil, err
	}
	return s.sendPayload(ctx, command.CommandDMF, payload)
}

// TableLoadInitiate inicia a carga EMV para o adquirente e a versão informados.
func (s *Service) TableLoadInitiate(ctx context.Context, acquirer, version string) (*model.Response, error) {
	p, err := command.BuildTLICommand(acquirer, version)
	if err != nil {
		return nil, err
	}
	return s.sendPayload(ctx, command.CommandTLI, p)
}

// TableLoadRecord envia um bloco TLR previamente limitado a 999 bytes.
func (s *Service) TableLoadRecord(ctx context.Context, records []string) (*model.Response, error) {
	p, err := command.BuildTLRCommand(records)
	if err != nil {
		return nil, err
	}
	return s.sendPayload(ctx, command.CommandTLR, p)
}

// TableLoadEnd confirma a carga temporária por meio do comando literal TLE.
func (s *Service) TableLoadEnd(ctx context.Context, _ string) (*model.Response, error) {
	return s.sendPayload(ctx, command.CommandTLE, command.BuildTLECommand())
}

// LoadCompleteEMVTable executa TLI, particiona registros em TLR válidos e finaliza com TLE.
func (s *Service) LoadCompleteEMVTable(ctx context.Context, acquirer, version string, records []string, progress ProgressFunc) error {
	batches, err := buildTLRBatches(records)
	if err != nil {
		return fmt.Errorf("TLR: %w", err)
	}
	init, err := s.TableLoadInitiate(ctx, acquirer, version)
	if err != nil {
		return fmt.Errorf("TLI: %w", err)
	}
	if init == nil || (init.StatusCode != state.StatusOK && init.StatusCode != state.StatusTableVersionDifferent) {
		return fmt.Errorf("TLI returned an invalid status")
	}
	processed := 0
	for _, batch := range batches {
		if _, err := s.sendPayload(ctx, command.CommandTLR, batch.payload); err != nil {
			return fmt.Errorf("TLR: %w", err)
		}
		processed += batch.records
		if progress != nil {
			if err := progress(ctx, int64(processed), int64(len(records))); err != nil {
				return err
			}
		}
	}
	if _, err := s.TableLoadEnd(ctx, version); err != nil {
		return fmt.Errorf("TLE: %w", err)
	}
	return nil
}

type tlrBatch struct {
	payload []byte
	records int
}

// buildTLRBatches divide registros respeitando simultaneamente NREC N2 e o
// limite CMD_LEN de 999 bytes definido para o corpo do TLR no ABECS 2.12.
func buildTLRBatches(records []string) ([]tlrBatch, error) {
	for _, record := range records {
		if _, err := command.BuildTLRCommand([]string{record}); err != nil {
			return nil, err
		}
	}

	batches := make([]tlrBatch, 0)
	for start := 0; start < len(records); {
		var payload []byte
		end := start
		for end < len(records) && end-start < command.TLRMaxRecords {
			candidate, err := command.BuildTLRCommand(records[start : end+1])
			if err != nil {
				break
			}
			payload = candidate
			end++
		}
		batches = append(batches, tlrBatch{payload: payload, records: end - start})
		start = end
	}
	return batches, nil
}

// PurchaseGCX executa GCX e aplica as retentativas e mudanças de interface CTLS normativas.
func (s *Service) PurchaseGCX(ctx context.Context, amount, date, clock string, enableCTLS, hideAmount bool) (*model.GCXResponse, error) {
	options := command.GCXOptionWaitMagCard
	if hideAmount {
		options |= command.GCXOptionHideAmount
	}
	if !enableCTLS {
		return s.SendGCXCommand(ctx, amount, date, clock, options)
	}
	ctlsOptions := options | command.GCXOptionWaitContactless
	sameInterfaceRetries := 0
	for {
		response, err := s.SendGCXCommand(ctx, amount, date, clock, ctlsOptions)
		if err == nil {
			return response, nil
		}
		var statusErr *domainerror.StatusError
		if !errors.As(err, &statusErr) {
			return nil, err
		}
		switch statusErr.Code {
		case state.StatusContactlessCommunicationError:
			if sameInterfaceRetries == 0 {
				sameInterfaceRetries++
				continue
			}
			return s.SendGCXCommand(ctx, amount, date, clock, options)
		case state.StatusContactlessMultipleCards, state.StatusContactlessExternalCVM:
			if sameInterfaceRetries == 0 {
				sameInterfaceRetries++
				continue
			}
			return nil, err
		case state.StatusContactlessInvalidated,
			state.StatusContactlessProblems,
			state.StatusContactlessApplicationNotAllowed,
			state.StatusContactlessApplicationNotAuthorized,
			state.StatusContactlessInterfaceChange:
			return s.SendGCXCommand(ctx, amount, date, clock, options)
		default:
			return nil, err
		}
	}
}

// LoadMultimediaPath lê uma mídia do disco e a transfere pelo fluxo MLI/MLR/MLE.
func (s *Service) LoadMultimediaPath(ctx context.Context, path, name string, progress ProgressFunc) error {
	data, err := os.ReadFile(path)
	if err != nil {
		return fmt.Errorf("read multimedia file: %w", err)
	}
	return s.SendMultimediaFile(ctx, name, data, progress)
}

// SendGPNCommandMK captura PIN com chave de trabalho MK/WK e devolve PIN block e KSN.
func (s *Service) SendGPNCommandMK(ctx context.Context, keyIndex int, encryptedWorkingKey []byte, pan, message string) ([]byte, []byte, error) {
	payload, err := command.BuildGPNCommandMK(keyIndex, encryptedWorkingKey, pan, message)
	if err != nil {
		return nil, nil, err
	}
	r, err := s.sendBlockingPayload(ctx, command.CommandGPN, payload)
	if err != nil {
		return nil, nil, err
	}
	return command.ParseGPNResponse(r.Data)
}

// SendGPNCommandDUKPT captura PIN e devolve PIN block e KSN como dados sensíveis.
func (s *Service) SendGPNCommandDUKPT(ctx context.Context, keyIndex int, pan, message string) ([]byte, []byte, error) {
	payload, err := command.BuildGPNCommandDUKPT(keyIndex, pan, message)
	if err != nil {
		return nil, nil, err
	}
	r, err := s.sendBlockingPayload(ctx, command.CommandGPN, payload)
	if err != nil {
		return nil, nil, err
	}
	return command.ParseGPNResponse(r.Data)
}

// SendCommand serializa uma operação tipada na fila única do dispositivo.
func (s *Service) SendCommand(ctx context.Context, cmd command.Command) (*model.Response, error) {
	return s.sendCommand(ctx, cmd, true)
}

func (s *Service) sendCommand(ctx context.Context, cmd command.Command, useConfigTimeout bool) (*model.Response, error) {
	startedAt := time.Now()
	s.mu.Lock()
	state := s.state
	if s.stopping {
		s.mu.Unlock()
		return nil, domainerror.ErrPinpadClosed
	}
	if s.protocolDesynchronized && cmd.Type != command.CommandCAN {
		s.mu.Unlock()
		return nil, domainerror.ErrPinpadDesynchronized
	}
	if state == model.StateClosed {
		s.mu.Unlock()
		return nil, domainerror.ErrPinpadClosed
	}
	if s.opening || s.closing {
		s.mu.Unlock()
		return nil, domainerror.ErrPinpadBusy
	}
	// Revalida dentro do worker para que comandos já enfileirados não alcancem a
	// porta serial se uma operação anterior perder a sincronização.
	execute := cmd.Execute
	cmd.Execute = func(operationContext context.Context) (*model.Response, error) {
		s.mu.RLock()
		desynchronized := s.protocolDesynchronized
		s.mu.RUnlock()
		if desynchronized && cmd.Type != command.CommandCAN {
			return nil, domainerror.ErrPinpadDesynchronized
		}
		return execute(operationContext)
	}
	s.state = model.StateBusy
	s.inFlight++
	s.mu.Unlock()
	defer func() {
		s.mu.Lock()
		s.inFlight--
		if s.inFlight == 0 && s.state == model.StateBusy && !s.stopping && !s.opening && !s.closing {
			s.state = model.StateOpen
		}
		s.mu.Unlock()
	}()
	var response *model.Response
	var err error
	if useConfigTimeout {
		response, err = s.submit(ctx, cmd)
	} else {
		if ctx == nil {
			ctx = context.Background()
		}
		response, err = s.queue.Submit(ctx, cmd)
	}
	if cmd.Type == command.CommandCAN && err == nil && response != nil && response.AckType == "EOT" {
		s.mu.Lock()
		s.protocolDesynchronized = false
		s.mu.Unlock()
	}
	s.mu.RLock()
	logger := s.logger
	s.mu.RUnlock()
	if logger != nil {
		status := ""
		if response != nil {
			status = response.StatusCode
		}
		logger.Info("comando ABECS concluido", slog.String("comando", string(cmd.Type)), slog.String("status", status), slog.Duration("duracao", time.Since(startedAt)), slog.Bool("sucesso", err == nil))
	}
	return response, err
}

// submit aplica o timeout configurado à operação sem estender um prazo menor
// já imposto pelo consumidor. Ele é usado também por OPN e CLO, quando a
// fachada está propositalmente no estado BUSY para bloquear comandos externos.
func (s *Service) submit(ctx context.Context, cmd command.Command) (*model.Response, error) {
	if ctx == nil {
		ctx = context.Background()
	}
	s.mu.RLock()
	timeout := s.config.Timeout
	s.mu.RUnlock()
	if timeout <= 0 {
		return nil, domainerror.ErrTimeout
	}
	operationContext, cancel := context.WithTimeout(ctx, timeout)
	defer cancel()
	return s.queue.Submit(operationContext, cmd)
}

// byteStream acumula bytes lidos da porta serial sem descartar dados extras:
// uma unica leitura pode devolver o ACK/NAK/EOT junto com parte (ou todo) o
// pacote de resposta que o segue, e nenhum desses bytes pode ser perdido.
type byteStream struct {
	port SerialPort
	buf  []byte
}

func newByteStream(port SerialPort) *byteStream {
	return &byteStream{port: port}
}

func (b *byteStream) fill(ctx context.Context) error {
	if err := ctx.Err(); err != nil {
		return err
	}
	if b.port == nil {
		return domainerror.ErrPortUnavailable
	}
	chunk, err := b.port.Read(ctx)
	if err != nil {
		return err
	}
	if len(chunk) == 0 {
		return domainerror.ErrTimeout
	}
	b.buf = append(b.buf, chunk...)
	return nil
}

func (b *byteStream) NextByte(ctx context.Context) (byte, error) {
	for len(b.buf) == 0 {
		if err := b.fill(ctx); err != nil {
			return 0, err
		}
	}
	v := b.buf[0]
	b.buf = b.buf[1:]
	return v, nil
}

// exchange preserva o caminho sem comando tipado para helpers internos e
// testes. Operações de fachada devem usar exchangeCommand para correlacionar o
// rastro com o comando ABECS efetivo.
func (s *Service) exchange(ctx context.Context, data []byte) (*model.Response, error) {
	return s.exchangeCommand(ctx, "", data)
}

// exchangeCommand envia um frame, registra SPE depois da escrita confirmada e
// registra RSP após o parser interpretar um status. PP e erros de I/O pertencem
// exclusivamente ao adaptador serial compartilhando o mesmo tracer.
func (s *Service) exchangeCommand(ctx context.Context, kind command.Type, data []byte) (*model.Response, error) {
	return s.exchangeCommandWithTracePolicy(ctx, kind, data, false)
}

func (s *Service) recoverExchangeFailure(ctx context.Context, cause error, recoverOnFailure bool) error {
	if !recoverOnFailure {
		return cause
	}
	return s.recoverProtocol(ctx, cause)
}

func (s *Service) recoverProtocol(ctx context.Context, cause error) error {
	s.mu.Lock()
	s.protocolDesynchronized = true
	s.mu.Unlock()
	cleanupContext, cancel := context.WithTimeout(
		contextWithoutCancellation(ctx),
		protocol.AcknowledgementTimeout*protocol.MaxAttempts,
	)
	defer cancel()
	if cleanupErr := s.cancelHandshake(cleanupContext); cleanupErr != nil {
		s.mu.Lock()
		s.protocolDesynchronized = true
		s.mu.Unlock()
		if reconnectErr := s.reconnectProtocol(ctx); reconnectErr != nil {
			return errors.Join(
				cause,
				fmt.Errorf("recover protocol with CAN/EOT: %w", cleanupErr),
				reconnectErr,
			)
		}
		return cause
	}
	s.mu.Lock()
	s.protocolDesynchronized = false
	s.mu.Unlock()
	return cause
}

func contextWithoutCancellation(ctx context.Context) context.Context {
	if ctx == nil {
		return context.Background()
	}
	return context.WithoutCancel(ctx)
}

func (s *Service) reconnectProtocol(parent context.Context) error {
	s.mu.RLock()
	timeout := s.config.Timeout
	s.mu.RUnlock()
	minimumTimeout := 2*protocol.AcknowledgementTimeout*protocol.MaxAttempts + protocol.ResponseTimeout
	if timeout < minimumTimeout {
		timeout = minimumTimeout
	}
	reconnectContext, cancel := context.WithTimeout(contextWithoutCancellation(parent), timeout)
	defer cancel()

	s.mu.Lock()
	port := s.port
	s.protocolDesynchronized = true
	s.opening = true
	s.state = model.StateBusy
	s.tracksEligible = false
	s.goxEligible = false
	s.fcxEligible = false
	if s.secureSession != nil {
		s.secureSession.Close()
		s.secureSession = nil
	}
	s.mu.Unlock()
	if port == nil {
		return s.failProtocolReconnect(domainerror.ErrPortUnavailable, false)
	}

	if err := port.Close(); err != nil {
		return s.failProtocolReconnect(fmt.Errorf("controlled reconnect close pinpad: %w", err), false)
	}
	s.replaceByteStream()
	if err := reconnectContext.Err(); err != nil {
		return s.failProtocolReconnect(fmt.Errorf("controlled reconnect before open: %w", err), false)
	}
	if err := port.Open(); err != nil {
		return s.failProtocolReconnect(fmt.Errorf("controlled reconnect open pinpad: %w", err), false)
	}
	s.replaceByteStream()
	if err := s.cancelHandshake(reconnectContext); err != nil {
		return s.failProtocolReconnect(fmt.Errorf("controlled reconnect initial CAN/EOT: %w", err), true)
	}
	if _, err := s.exchangePayloadWithoutRecovery(reconnectContext, command.CommandOPN, []byte("OPN"), false); err != nil {
		return s.failProtocolReconnect(fmt.Errorf("controlled reconnect OPN: %w", err), true)
	}

	s.mu.Lock()
	s.opening = false
	s.protocolDesynchronized = false
	s.state = model.StateBusy
	s.mu.Unlock()
	return nil
}

func (s *Service) replaceByteStream() {
	s.mu.Lock()
	s.stream = newByteStream(s.port)
	s.mu.Unlock()
}

func (s *Service) failProtocolReconnect(cause error, closePort bool) error {
	var closeErr error
	if closePort && s.port != nil {
		closeErr = s.port.Close()
	}
	portOpen := s.port != nil && s.port.IsOpen()
	s.mu.Lock()
	s.opening = false
	s.protocolDesynchronized = true
	s.stream = newByteStream(s.port)
	if portOpen {
		s.state = model.StateOpen
	} else {
		s.state = model.StateClosed
	}
	s.mu.Unlock()
	if closeErr != nil {
		return errors.Join(cause, fmt.Errorf("controlled reconnect cleanup close: %w", closeErr))
	}
	return cause
}

func isExchangeTimeout(err error) bool {
	return errors.Is(err, context.Canceled) || errors.Is(err, context.DeadlineExceeded) || errors.Is(err, domainerror.ErrTimeout)
}

func (s *Service) exchangeCommandWithTracePolicy(ctx context.Context, kind command.Type, data []byte, forceRedaction bool) (*model.Response, error) {
	return s.exchangeCommandWithRecovery(ctx, kind, data, forceRedaction, true)
}

func (s *Service) exchangeCommandWithRecovery(ctx context.Context, kind command.Type, data []byte, forceRedaction, recoverOnFailure bool) (*model.Response, error) {
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	s.setTraceCommand(kind, forceRedaction)
	stream := s.stream
	if stream == nil {
		stream = newByteStream(s.port)
		s.stream = stream
	}
	var acknowledgementErr error
	acknowledged := false
	for attempt := 0; attempt < protocol.MaxAttempts; attempt++ {
		if err := ctx.Err(); err != nil {
			acknowledgementErr = err
			break
		}
		if err := s.port.Write(data); err != nil {
			return nil, fmt.Errorf("write command: %w", err)
		}
		ackContext, cancel := context.WithTimeout(ctx, protocol.AcknowledgementTimeout)
		first, err := stream.NextByte(ackContext)
		cancel()
		if err != nil {
			acknowledgementErr = fmt.Errorf("read acknowledgement: %w", err)
			continue
		}
		switch first {
		case protocol.PP_ACK:
			acknowledged = true
		case protocol.PP_NAK:
			acknowledgementErr = domainerror.ErrNakReceived
		case protocol.PP_EOT:
			return &model.Response{AckType: "EOT"}, nil
		default:
			return nil, s.recoverExchangeFailure(ctx, domainerror.ErrInvalidResponse, recoverOnFailure)
		}
		if acknowledged {
			break
		}
	}
	if !acknowledged {
		return &model.Response{AckType: "NAK"}, s.recoverExchangeFailure(ctx, acknowledgementErr, recoverOnFailure)
	}

	responseContext := ctx
	responseCancel := func() {}
	if !isBlockingCommand(kind) {
		responseContext, responseCancel = context.WithTimeout(ctx, protocol.ResponseTimeout)
	}
	defer responseCancel()

	invalidFrames := 0
	for {
		payload, err := protocol.ReadFullResponse(responseContext, stream)
		if err != nil {
			if errors.Is(err, domainerror.ErrChecksumInvalid) || errors.Is(err, domainerror.ErrInvalidResponse) {
				invalidFrames++
				if invalidFrames >= protocol.MaxAttempts {
					return nil, s.recoverExchangeFailure(ctx, err, recoverOnFailure)
				}
				s.setTraceCommand("", false)
				if writeErr := s.port.Write([]byte{protocol.PP_NAK}); writeErr != nil {
					return nil, errors.Join(err, fmt.Errorf("write response NAK: %w", writeErr))
				}
				continue
			}
			if isExchangeTimeout(err) {
				return nil, s.recoverExchangeFailure(ctx, err, recoverOnFailure)
			}
			return nil, err
		}
		if len(payload) > 0 && payload[0] == protocol.PP_DC2 {
			s.mu.RLock()
			session := s.secureSession
			s.mu.RUnlock()
			if session == nil {
				return nil, domainerror.ErrInvalidResponse
			}
			payload, err = session.Unprotect(payload)
			if err != nil {
				s.clearSecureSession()
				return nil, err
			}
		}
		if bytes.HasPrefix(payload, []byte("NTM")) {
			if _, err := parser.ParseNotification(payload); err != nil {
				return nil, s.recoverExchangeFailure(ctx, err, recoverOnFailure)
			}
			continue
		}
		response, err := parser.ParseAbecsResponse(payload)
		var traceErr error
		if response != nil && response.StatusCode != "" {
			traceErr = s.tracerFor().RecordResponse(kind, response.StatusCode)
			if response.StatusCode == state.StatusPacketSecurityError {
				s.clearSecureSession()
			}
		}
		if err != nil || response == nil || response.StatusCode == "" || isSuccessfulCommandStatus(kind, response.StatusCode) {
			return response, errors.Join(err, wrapTracePersistenceError(traceErr))
		}
		return response, errors.Join(&domainerror.StatusError{Code: response.StatusCode}, wrapTracePersistenceError(traceErr))
	}
}

func (s *Service) clearSecureSession() {
	s.mu.Lock()
	if s.secureSession != nil {
		s.secureSession.Close()
		s.secureSession = nil
	}
	s.mu.Unlock()
}

func isBlockingCommand(kind command.Type) bool {
	switch kind {
	case command.CommandMNU, command.CommandGKY, command.CommandGPN,
		command.CommandGCX, command.CommandGOX, command.CommandFCX:
		return true
	default:
		return false
	}
}

func isSuccessfulCommandStatus(kind command.Type, status string) bool {
	if status == state.StatusOK {
		return true
	}
	if kind == command.CommandTLI && status == state.StatusTableVersionDifferent {
		return true
	}
	if kind == command.CommandGKY {
		switch status {
		case state.StatusF1, state.StatusF2, state.StatusF3, state.StatusF4,
			state.StatusBackspace, state.StatusCancel:
			return true
		}
	}
	return false
}

// cancelHandshake envia CAN isolado e aguarda EOT, ignorando outros bytes.
func (s *Service) cancelHandshake(ctx context.Context) error {
	stream := s.stream
	if stream == nil {
		stream = newByteStream(s.port)
		s.stream = stream
	}
	var lastErr error
	for attempt := 0; attempt < protocol.MaxAttempts; attempt++ {
		s.setTraceCommand("", false)
		if err := s.port.Write([]byte{protocol.PP_CAN}); err != nil {
			return fmt.Errorf("write CAN: %w", err)
		}
		attemptContext, cancel := context.WithTimeout(ctx, protocol.AcknowledgementTimeout)
		for {
			value, err := stream.NextByte(attemptContext)
			if err != nil {
				lastErr = err
				break
			}
			if value == protocol.PP_EOT {
				cancel()
				return nil
			}
		}
		cancel()
	}
	return fmt.Errorf("EOT not received after %d CAN attempts: %w", protocol.MaxAttempts, lastErr)
}

func wrapTracePersistenceError(err error) error {
	if err == nil {
		return nil
	}
	return fmt.Errorf("record response trace: %w", err)
}

func (s *Service) exchangeAckOnly(ctx context.Context, data []byte) error {
	if err := ctx.Err(); err != nil {
		return err
	}
	s.setTraceCommand("", false)
	if err := s.port.Write(data); err != nil {
		return fmt.Errorf("write command: %w", err)
	}
	stream := s.stream
	if stream == nil {
		stream = newByteStream(s.port)
		s.stream = stream
	}
	response, err := stream.NextByte(ctx)
	if err != nil {
		return fmt.Errorf("read acknowledgement: %w", err)
	}
	if response != protocol.PP_ACK {
		if response == protocol.PP_NAK {
			return domainerror.ErrNakReceived
		}
		return domainerror.ErrInvalidResponse
	}
	return nil
}

func (s *Service) setTraceCommand(kind command.Type, forceRedaction bool) {
	s.mu.RLock()
	redact := forceRedaction || s.secureSession != nil || s.traceSensitive[kind]
	s.mu.RUnlock()
	if port, ok := s.port.(tracePolicyPort); ok {
		port.SetTracePolicy(kind, redact)
		return
	}
	if port, ok := s.port.(traceCommandPort); ok {
		port.SetTraceCommand(kind)
	}
}

func (s *Service) tracerFor() *logging.Tracer {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.tracer
}
