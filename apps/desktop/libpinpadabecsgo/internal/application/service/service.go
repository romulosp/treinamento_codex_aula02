package service

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/parser"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/worker"
	"bytes"
	"context"
	"fmt"
	"os"
	"sync"
)

type SerialPort interface {
	Open() error
	Close() error
	Read() ([]byte, error)
	Write([]byte) error
	IsOpen() bool
}
type PinpadService interface {
	Open(context.Context) error
	Close(context.Context) error
	Reset(context.Context) error
	SendCommand(context.Context, command.Command) (*model.Response, error)
	GetInfo(context.Context) (*model.DeviceInfo, error)
	GetDisplayCapabilities(context.Context) (*model.DisplayCapabilities, error)
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

type QRCodeResult struct {
	PNG               []byte
	X, Y              int
	PositionSupported bool
}

type ProgressFunc func(context.Context, int64, int64) error

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

type Service struct {
	mu              sync.RWMutex
	config          model.PinpadConfig
	port            SerialPort
	state           model.PinpadState
	queue           *worker.Queue
	qrCodeGenerator QRCodeGenerator
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
	return QRCodeResult{PNG: append([]byte(nil), png...), X: xPos, Y: yPos, PositionSupported: false}, nil
}

func (s *Service) TransactionGCX(context.Context, TransactionGCXRequest) (*model.GCXResponse, error) {
	return nil, domainerror.ErrNotImplemented
}

func New(cfg model.PinpadConfig, port SerialPort) *Service {
	return &Service{config: cfg, port: port, state: model.StateClosed, queue: worker.New(100)}
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

// Open abre a porta serial do pinpad. O protocolo ABECS nao define um comando
// de abertura de sessao: a abertura e puramente uma operacao de porta serial,
// exatamente como na referencia C++ (openPinpad apenas configura e abre o
// handle, sem trocar nenhum comando com o dispositivo).
func (s *Service) Open(ctx context.Context) error {
	if err := ctx.Err(); err != nil {
		return err
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.state != model.StateClosed {
		return nil
	}
	if s.port == nil {
		return domainerror.ErrPortUnavailable
	}
	if err := s.port.Open(); err != nil {
		return fmt.Errorf("open pinpad: %w", err)
	}
	s.state = model.StateOpen
	return nil
}

// Close fecha a porta serial do pinpad. Assim como o Open, nao ha comando
// ABECS de encerramento de sessao: a referencia C++ (closePinpad) apenas
// fecha o handle da porta.
func (s *Service) Close(ctx context.Context) error {
	if err := ctx.Err(); err != nil {
		return err
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.state == model.StateBusy {
		return domainerror.ErrPinpadBusy
	}
	if s.port != nil {
		if err := s.port.Close(); err != nil {
			return fmt.Errorf("close pinpad: %w", err)
		}
	}
	s.state = model.StateClosed
	return nil
}
func (s *Service) Shutdown() { s.queue.Stop() }
func (s *Service) GetState() model.PinpadState {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.state
}
func (s *Service) Reset(ctx context.Context) error {
	_, err := s.SendCommand(ctx, command.Command{Type: command.CommandCAN, Execute: func(operationContext context.Context) (*model.Response, error) {
		return s.exchange(operationContext, []byte{protocol.PP_CAN})
	}})
	return err
}
func (s *Service) GetInfo(ctx context.Context) (*model.DeviceInfo, error) {
	r, err := s.SendCommand(ctx, command.Command{Type: command.CommandGIX, Execute: func(operationContext context.Context) (*model.Response, error) {
		return s.exchange(operationContext, protocol.BuildCommand("GIX", nil))
	}})
	if err != nil {
		return nil, err
	}
	info := parser.DeviceInfoFromResponse(r)
	return &info, nil
}
func (s *Service) GetDisplayCapabilities(context.Context) (*model.DisplayCapabilities, error) {
	return &model.DisplayCapabilities{}, nil
}

// sendPayload envia um payload logico ABECS (CMD_ID + LEN + parametros,
// ainda sem enquadramento) apos aplicar o enquadramento SYN/ETB/CRC exigido
// pelo protocolo (equivalente ao buildPacket() da referencia C++).
func (s *Service) sendPayload(ctx context.Context, kind command.Type, payload []byte) (*model.Response, error) {
	return s.SendCommand(ctx, command.Command{Type: kind, Execute: func(op context.Context) (*model.Response, error) {
		return s.exchange(op, protocol.BuildPacket(payload))
	}})
}

func (s *Service) DisplayDSP(ctx context.Context, line1, line2 string) (*model.Response, error) {
	return s.sendPayload(ctx, command.CommandDSP, command.BuildDSPCommand(line1, line2))
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
	return parser.ParseMNUResponse(r.RawData)
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
	return parser.ParseGKYKey(r.RawData)
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
	return parser.GCXResponseFromResponse(r), nil
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
	if _, err := s.TableLoadInitiate(ctx, acquirer, version); err != nil {
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
	return command.ParseGPNResponse(r.RawData)
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
	return command.ParseGPNResponse(r.RawData)
}
func (s *Service) SendCommand(ctx context.Context, cmd command.Command) (*model.Response, error) {
	s.mu.Lock()
	state := s.state
	if state == model.StateClosed {
		s.mu.Unlock()
		return nil, domainerror.ErrPinpadClosed
	}
	if state == model.StateBusy {
		s.mu.Unlock()
		return nil, domainerror.ErrPinpadBusy
	}
	s.state = model.StateBusy
	s.mu.Unlock()
	defer func() {
		s.mu.Lock()
		if s.state == model.StateBusy {
			s.state = model.StateOpen
		}
		s.mu.Unlock()
	}()
	return s.queue.Submit(ctx, cmd)
}
// byteStream acumula bytes lidos da porta serial sem descartar dados extras:
// uma unica leitura pode devolver o ACK/NAK/EOT junto com parte (ou todo) o
// pacote de resposta que o segue, e nenhum desses bytes pode ser perdido.
type byteStream struct {
	ctx  context.Context
	port SerialPort
	buf  []byte
}

func newByteStream(ctx context.Context, port SerialPort) *byteStream {
	return &byteStream{ctx: ctx, port: port}
}

func (b *byteStream) fill() error {
	if err := b.ctx.Err(); err != nil {
		return err
	}
	chunk, err := b.port.Read()
	if err != nil {
		return err
	}
	if len(chunk) == 0 {
		return domainerror.ErrTimeout
	}
	b.buf = append(b.buf, chunk...)
	return nil
}

func (b *byteStream) readByte() (byte, error) {
	for len(b.buf) == 0 {
		if err := b.fill(); err != nil {
			return 0, err
		}
	}
	v := b.buf[0]
	b.buf = b.buf[1:]
	return v, nil
}

// Read implementa protocol.Reader devolvendo os bytes ja bufferizados (ou
// buscando mais da porta serial quando o buffer estiver vazio).
func (b *byteStream) Read() ([]byte, error) {
	if len(b.buf) == 0 {
		if err := b.fill(); err != nil {
			return nil, err
		}
	}
	out := b.buf
	b.buf = nil
	return out, nil
}

func (s *Service) exchange(ctx context.Context, data []byte) (*model.Response, error) {
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	if err := s.port.Write(data); err != nil {
		return nil, fmt.Errorf("write command: %w", err)
	}
	stream := newByteStream(ctx, s.port)
	first, err := stream.readByte()
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
	return parser.ParseAbecsResponse(payload)
}

func (s *Service) exchangeAckOnly(ctx context.Context, data []byte) error {
	if err := ctx.Err(); err != nil {
		return err
	}
	if err := s.port.Write(data); err != nil {
		return fmt.Errorf("write command: %w", err)
	}
	response, err := s.port.Read()
	if err != nil {
		return fmt.Errorf("read acknowledgement: %w", err)
	}
	if len(response) != 1 || response[0] != protocol.PP_ACK {
		if len(response) == 1 && response[0] == protocol.PP_NAK {
			return domainerror.ErrNakReceived
		}
		return domainerror.ErrInvalidResponse
	}
	return nil
}
