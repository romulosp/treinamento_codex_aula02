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
	DisplayImage(context.Context, string) (*model.Response, error)
	LoadCompleteEMVTable(context.Context, string, string, []string, ProgressFunc) error
	SendGCXInitialization(context.Context) (*model.GCXResponse, error)
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
	mu              sync.RWMutex
	config          model.PinpadConfig
	port            SerialPort
	state           model.PinpadState
	queue           *worker.Queue
	stream          *byteStream
	stopping        bool
	opening         bool
	closing         bool
	inFlight        int
	logger          *slog.Logger
	tracer          *logging.Tracer
	qrCodeGenerator QRCodeGenerator
	tracksEligible  bool
	goxEligible     bool
	fcxEligible     bool
	secureSession   *protocol.SecureSession
}

func (s *Service) SetQRCodeGenerator(generator QRCodeGenerator) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.qrCodeGenerator = generator
}

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
	return QRCodeResult{PNG: append([]byte(nil), png...), X: xPos, Y: yPos, PositionSupported: false, PositionWarning: "xPos and yPos are not supported by ABECS"}, nil
}

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

// SetTracer associa o rastro serial usado para registrar SPE e RSP. A mesma
// instância deve ser configurada no adaptador serial antes de Open para que PP,
// abertura e fechamento pertençam à mesma sessão. Um valor nil desabilita o
// rastro da fachada sem alterar o fluxo do comando.
func (s *Service) SetTracer(tracer *logging.Tracer) {
	if tracer == nil {
		tracer = logging.NewTracer()
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	s.tracer = tracer
}

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

func (s *Service) GetConfig() model.PinpadConfig {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.config
}

var _ PinpadService = (*Service)(nil)

// Open abre a porta serial e inicializa o diálogo ABECS com OPN000. Chamadas
// repetidas após uma abertura bem-sucedida são idempotentes.
func (s *Service) Open(ctx context.Context) error {
	if err := ctx.Err(); err != nil {
		return err
	}
	s.mu.Lock()
	if s.state != model.StateClosed {
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
		return s.exchangePayload(operationContext, command.CommandOPN, []byte("OPN000"))
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
	s.mu.Unlock()
	return nil
}

// OpenSecure estabelece KSEC por OPN seguro após abrir a porta. A chave
// privada permanece sob controle exclusivo do consumidor.
func (s *Service) OpenSecure(ctx context.Context, privateKey *rsa.PrivateKey) error {
	if privateKey == nil {
		return fmt.Errorf("secure OPN private key is required")
	}
	if err := s.Open(ctx); err != nil {
		return err
	}
	payload, err := protocol.BuildSecureOPN(&privateKey.PublicKey)
	if err != nil {
		return err
	}
	response, err := s.sendPayload(ctx, command.CommandOPN, payload)
	if err != nil {
		return fmt.Errorf("secure OPN: %w", err)
	}
	session, err := protocol.EstablishSecureSession(privateKey, response.RawData)
	if err != nil {
		return fmt.Errorf("establish secure OPN: %w", err)
	}
	s.mu.Lock()
	if s.stopping || s.state == model.StateClosed {
		s.mu.Unlock()
		session.Close()
		return domainerror.ErrPinpadClosed
	}
	if s.secureSession != nil {
		s.secureSession.Close()
	}
	s.secureSession = session
	s.mu.Unlock()
	return nil
}

// Close encerra o diálogo ABECS por CLO000 antes de liberar a porta serial.
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
	s.closing = true
	s.state = model.StateBusy
	s.mu.Unlock()

	_, protocolErr := s.submit(ctx, command.Command{Type: command.CommandCLO, Execute: func(operationContext context.Context) (*model.Response, error) {
		return s.exchangePayload(operationContext, command.CommandCLO, []byte("CLO000"))
	}})
	s.mu.Lock()
	closeErr := error(nil)
	if s.port != nil {
		closeErr = s.port.Close()
	}
	s.state = model.StateClosed
	s.closing = false
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
	s.stream = newByteStream(s.port)
	if s.secureSession != nil {
		s.secureSession.Close()
		s.secureSession = nil
	}
	s.mu.Unlock()
}
func (s *Service) GetState() model.PinpadState {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.state
}
func (s *Service) Reset(ctx context.Context) error {
	_, err := s.SendCommand(ctx, command.Command{Type: command.CommandCAN, Execute: func(operationContext context.Context) (*model.Response, error) {
		return s.exchangeCommand(operationContext, command.CommandCAN, []byte{protocol.PP_CAN})
	}})
	return err
}
func (s *Service) GetInfo(ctx context.Context) (*model.DeviceInfo, error) {
	r, err := s.GetInfoRaw(ctx)
	if err != nil {
		return nil, err
	}
	info := parser.DeviceInfoFromResponse(r)
	return &info, nil
}
func (s *Service) GetInfoRaw(ctx context.Context) (*model.Response, error) {
	return s.SendCommand(ctx, command.Command{Type: command.CommandGIX, Execute: func(operationContext context.Context) (*model.Response, error) {
		return s.exchangePayload(operationContext, command.CommandGIX, []byte("GIX000"))
	}})
}
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
	return s.SendCommand(ctx, command.Command{Type: kind, Execute: func(op context.Context) (*model.Response, error) {
		return s.exchangePayload(op, kind, payload)
	}})
}

func (s *Service) exchangePayload(ctx context.Context, kind command.Type, payload []byte) (*model.Response, error) {
	s.mu.RLock()
	session := s.secureSession
	s.mu.RUnlock()
	if session != nil && kind != command.CommandOPN && kind != command.CommandCLO && kind != command.CommandCLX {
		protected, err := session.Protect(payload)
		if err != nil {
			return nil, err
		}
		response, err := s.exchangeCommand(ctx, kind, protocol.BuildPacket(protected))
		return validateResponseCommand(kind, response, err)
	}
	response, err := s.exchangeCommand(ctx, kind, protocol.BuildPacket(payload))
	return validateResponseCommand(kind, response, err)
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

func (s *Service) DisplayDSP(ctx context.Context, line1, line2 string) (*model.Response, error) {
	return s.sendPayload(ctx, command.CommandDSP, command.BuildDSPCommand(line1, line2))
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
	return parser.GTKResponseFromResponse(response), nil
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
	response, err := s.sendPayload(ctx, command.CommandGOX, payload)
	if err != nil {
		return nil, err
	}
	s.mu.Lock()
	s.goxEligible = false
	s.fcxEligible = true
	s.mu.Unlock()
	return parser.GOXResponseFromResponse(response), nil
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
	response, err := s.sendPayload(ctx, command.CommandFCX, payload)
	if err != nil {
		return nil, err
	}
	s.mu.Lock()
	s.fcxEligible = false
	s.mu.Unlock()
	return parser.FCXResponseFromResponse(response), nil
}
func (s *Service) DisplayDEX(ctx context.Context, message string) (*model.Response, error) {
	payload, err := command.BuildDEXCommand(message)
	if err != nil {
		return nil, err
	}
	return s.sendPayload(ctx, command.CommandDEX, payload)
}
func (s *Service) DisplayMNU(ctx context.Context, timeout int, title string, options []string) (parser.MNUResponse, error) {
	payload, err := command.BuildMNUCommand(timeout, title, options)
	if err != nil {
		return parser.MNUResponse{}, err
	}
	r, err := s.sendPayload(ctx, command.CommandMNU, payload)
	if err != nil {
		return parser.MNUResponse{}, err
	}
	return parser.ParseMNUResponse(append([]byte(r.StatusCode), r.Data...))
}
func (s *Service) WaitForKeyPress(ctx context.Context, timeout int) (byte, error) {
	payload, err := command.BuildGKYCommand(command.GKYModeWaitKey, timeout)
	if err != nil {
		return command.GKYKeyNone, err
	}
	r, err := s.sendPayload(ctx, command.CommandGKY, payload)
	if err != nil {
		return command.GKYKeyNone, err
	}
	return parser.ParseGKYKey(r.Data)
}
func (s *Service) ResetPinpad(ctx context.Context) error {
	_, err := s.sendPayload(ctx, command.CommandRST, command.BuildRSTCommand())
	return err
}
func (s *Service) SendGCXCommand(ctx context.Context, amount, date, clock string, options byte) (*model.GCXResponse, error) {
	payload, err := command.BuildGCXCommand(amount, date, clock, options)
	if err != nil {
		return nil, err
	}
	r, err := s.sendPayload(ctx, command.CommandGCX, payload)
	if err != nil {
		return nil, err
	}
	result := parser.GCXResponseFromResponse(r)
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
	if _, err := s.sendPayload(ctx, command.CommandMLI, command.BuildMLICommand(name, len(data))); err != nil {
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
		if progress != nil {
			if err = progress(ctx, int64(end), int64(len(data))); err != nil {
				return err
			}
		}
	}
	if _, err := s.sendPayload(ctx, command.CommandMLE, command.BuildMLECommand(name)); err != nil {
		return fmt.Errorf("MLE: %w", err)
	}
	return nil
}

func (s *Service) LoadMultimediaFile(ctx context.Context, name string, data []byte, progress ProgressFunc) error {
	return s.SendMultimediaFile(ctx, name, data, progress)
}
func (s *Service) DisplayImage(ctx context.Context, name string) (*model.Response, error) {
	return s.sendPayload(ctx, command.CommandDSI, command.BuildDSICommand(name))
}
func (s *Service) DisplayDSI(ctx context.Context, name string) (*model.Response, error) {
	return s.DisplayImage(ctx, name)
}

func (s *Service) TableLoadInitiate(ctx context.Context, acquirer, version string) (*model.Response, error) {
	p, err := command.BuildTLICommand(acquirer, version)
	if err != nil {
		return nil, err
	}
	return s.sendPayload(ctx, command.CommandTLI, p)
}
func (s *Service) TableLoadRecord(ctx context.Context, records []string) (*model.Response, error) {
	p, err := command.BuildTLRCommand(records)
	if err != nil {
		return nil, err
	}
	return s.sendPayload(ctx, command.CommandTLR, p)
}
func (s *Service) TableLoadEnd(ctx context.Context, version string) (*model.Response, error) {
	return s.sendPayload(ctx, command.CommandTLE, command.BuildTLECommand(version))
}
func (s *Service) LoadCompleteEMVTable(ctx context.Context, acquirer, version string, records []string, progress ProgressFunc) error {
	init, err := s.TableLoadInitiate(ctx, acquirer, version)
	if init != nil && init.StatusCode == state.StatusTableVersionDifferent {
		return nil
	}
	if err != nil {
		return fmt.Errorf("TLI: %w", err)
	}
	for i := 0; i < len(records); i += command.TLRMaxRecords {
		end := i + command.TLRMaxRecords
		if end > len(records) {
			end = len(records)
		}
		if _, err := s.TableLoadRecord(ctx, records[i:end]); err != nil {
			return fmt.Errorf("TLR: %w", err)
		}
		if progress != nil {
			if err := progress(ctx, int64(end), int64(len(records))); err != nil {
				return err
			}
		}
	}
	if _, err := s.TableLoadEnd(ctx, version); err != nil {
		return fmt.Errorf("TLE: %w", err)
	}
	return nil
}
func (s *Service) SendGCXInitialization(ctx context.Context) (*model.GCXResponse, error) {
	return s.SendGCXCommand(ctx, "000000000000", "000000", "000000", 0)
}
func (s *Service) PurchaseGCX(ctx context.Context, amount, date, clock string, enableCTLS, skipInit bool) (*model.GCXResponse, error) {
	if !skipInit && s.config.UseGCXInitialization {
		if _, err := s.SendGCXInitialization(ctx); err != nil {
			return nil, fmt.Errorf("GCX initialization: %w", err)
		}
	}
	var options byte
	if enableCTLS {
		options = 1
	}
	return s.SendGCXCommand(ctx, amount, date, clock, options)
}
func (s *Service) LoadMultimediaPath(ctx context.Context, path, name string, progress ProgressFunc) error {
	data, err := os.ReadFile(path)
	if err != nil {
		return fmt.Errorf("read multimedia file: %w", err)
	}
	return s.SendMultimediaFile(ctx, name, bytes.Clone(data), progress)
}
func (s *Service) SendGPNCommandMK(ctx context.Context, keyIndex int, pan, message string) ([]byte, []byte, error) {
	payload, err := command.BuildGPNCommandMK(keyIndex, pan, message)
	if err != nil {
		return nil, nil, err
	}
	r, err := s.sendPayload(ctx, command.CommandGPN, payload)
	if err != nil {
		return nil, nil, err
	}
	return command.ParseGPNResponse(r.Data)
}
func (s *Service) SendGPNCommandDUKPT(ctx context.Context, ksn, pan, message string) ([]byte, []byte, error) {
	payload, err := command.BuildGPNCommandDUKPT(ksn, pan, message)
	if err != nil {
		return nil, nil, err
	}
	r, err := s.sendPayload(ctx, command.CommandGPN, payload)
	if err != nil {
		return nil, nil, err
	}
	return command.ParseGPNResponse(r.Data)
}
func (s *Service) SendCommand(ctx context.Context, cmd command.Command) (*model.Response, error) {
	startedAt := time.Now()
	s.mu.Lock()
	state := s.state
	if s.stopping || state == model.StateClosed {
		s.mu.Unlock()
		return nil, domainerror.ErrPinpadClosed
	}
	if s.opening || s.closing {
		s.mu.Unlock()
		return nil, domainerror.ErrPinpadBusy
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
	response, err := s.submit(ctx, cmd)
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
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	s.setTraceCommand(kind)
	if err := s.port.Write(data); err != nil {
		return nil, fmt.Errorf("write command: %w", err)
	}
	stream := s.stream
	if stream == nil {
		stream = newByteStream(s.port)
		s.stream = stream
	}
	first, err := stream.NextByte(ctx)
	if err != nil {
		return nil, fmt.Errorf("read acknowledgement: %w", err)
	}
	if first == protocol.PP_NAK {
		return &model.Response{AckType: "NAK"}, domainerror.ErrNakReceived
	}
	if first == protocol.PP_EOT {
		return &model.Response{AckType: "EOT"}, nil
	}
	if first != protocol.PP_ACK {
		return nil, domainerror.ErrInvalidResponse
	}
	payload, err := protocol.ReadFullResponse(ctx, stream)
	if err != nil {
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
			return nil, err
		}
	}
	response, err := parser.ParseAbecsResponse(payload)
	if response != nil && response.StatusCode != "" {
		s.tracerFor().RecordResponse(kind, response.StatusCode)
	}
	if err != nil || response == nil || response.StatusCode == "" || response.StatusCode == state.StatusOK {
		return response, err
	}
	return response, &domainerror.StatusError{Code: response.StatusCode}
}

func (s *Service) exchangeAckOnly(ctx context.Context, data []byte) error {
	if err := ctx.Err(); err != nil {
		return err
	}
	s.setTraceCommand("")
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

func (s *Service) setTraceCommand(kind command.Type) {
	if port, ok := s.port.(traceCommandPort); ok {
		port.SetTraceCommand(kind)
	}
}

func (s *Service) tracerFor() *logging.Tracer {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.tracer
}
