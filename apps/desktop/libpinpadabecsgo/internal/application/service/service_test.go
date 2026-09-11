package service

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"context"
	"crypto/rand"
	"crypto/rsa"
	"encoding/hex"
	"errors"
	"fmt"
	"io"
	"log/slog"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"
)

type fakePort struct {
	open     bool
	writes   [][]byte
	reads    [][]byte
	readErr  error
	writeErr error
	traceCmd command.Type
}

func TestServiceTracerPropagatesTypedCommandAndRecordsRSP(t *testing.T) {
	path := filepath.Join(t.TempDir(), "trace.log")
	tracer := logging.NewTracer()
	if active, err := tracer.SetLogDestination(path); err != nil || !active {
		t.Fatalf("SetLogDestination active=%t err=%v", active, err)
	}
	defer func() {
		if err := tracer.Close(); err != nil {
			t.Fatal(err)
		}
	}()
	tracer.RecordOpen("COM7", 19200)
	p := &fakePort{reads: [][]byte{response("GIX000")}}
	s := New(model.DefaultConfig(), p)
	s.SetTracer(tracer)
	if _, err := s.exchangeCommand(context.Background(), command.CommandGIX, protocol.BuildPacket([]byte("GIX000"))); err != nil {
		t.Fatal(err)
	}

	content, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	text := string(content)
	if p.traceCmd != command.CommandGIX || !strings.Contains(text, "RSP CMD=GIX STATUS=000") {
		t.Fatalf("typed trace missing command or RSP: command=%s trace=%q", p.traceCmd, text)
	}
}

func (p *fakePort) Open() error  { p.open = true; return nil }
func (p *fakePort) Close() error { p.open = false; return nil }
func (p *fakePort) IsOpen() bool { return p.open }
func (p *fakePort) Write(b []byte) error {
	if p.writeErr != nil {
		return p.writeErr
	}
	p.writes = append(p.writes, append([]byte(nil), b...))
	return nil
}
func (p *fakePort) Read(context.Context) ([]byte, error) {
	if p.readErr != nil {
		return nil, p.readErr
	}
	if len(p.reads) == 0 {
		return nil, nil
	}
	x := p.reads[0]
	p.reads = p.reads[1:]
	return x, nil
}

func (p *fakePort) SetTraceCommand(kind command.Type) { p.traceCmd = kind }

func response(payload string) []byte {
	return append([]byte{protocol.PP_ACK}, protocol.BuildPacket([]byte(payload))...)
}

func TestServiceOpenAndReset(t *testing.T) {
	p := &fakePort{reads: [][]byte{response("OPN000"), {protocol.PP_EOT}, response("CLO000")}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	if err := s.Reset(context.Background()); err != nil {
		t.Fatal(err)
	}
	if err := s.Close(context.Background()); err != nil {
		t.Fatal(err)
	}
}

func TestServiceBasicGIXAndReset(t *testing.T) {
	p := &fakePort{reads: [][]byte{response("OPN000"), append(response("GIX000"), protocol.PP_EOT), response("CLO000")}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	if _, err := s.GetInfo(context.Background()); err != nil {
		t.Fatal(err)
	}
	if err := s.Reset(context.Background()); err != nil {
		t.Fatal(err)
	}
	if err := s.Close(context.Background()); err != nil {
		t.Fatal(err)
	}
	if len(p.writes) != 4 {
		t.Fatalf("expected OPN, GIX, CAN and CLO exchanges, got %d writes", len(p.writes))
	}
}

func TestLoadCompleteEMVTableStopsOnTableVersionDifferent(t *testing.T) {
	p := &fakePort{reads: [][]byte{response("OPN000"), response("TLI020"), response("CLO000")}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	if err := s.LoadCompleteEMVTable(context.Background(), "00", "TABVER0001", []string{"record"}, nil); err != nil {
		t.Fatal(err)
	}
	if len(p.writes) != 2 {
		t.Fatalf("writes = %d, want OPN and TLI", len(p.writes))
	}
}

func TestDisplayQRCodeReportsUnsupportedPosition(t *testing.T) {
	s := New(model.DefaultConfig(), &fakePort{})
	s.SetQRCodeGenerator(qrGeneratorFunc(func(string, int) ([]byte, error) { return []byte("png"), nil }))
	result, err := s.DisplayQRCode(context.Background(), "value", 100, 0, 10, 20)
	if err != nil {
		t.Fatal(err)
	}
	if result.PositionSupported || result.PositionWarning == "" {
		t.Fatalf("unexpected QR result: %#v", result)
	}
}

func TestGetDisplayCapabilitiesUsesGIXTags(t *testing.T) {
	tags := []byte{0x80, 0x03, 0x00, 0x01, 'M', 0x80, 0x04, 0x00, 0x01, 'F', 0x80, 0x05, 0x00, 0x02, '1', '2', 0x80, 0x20, 0x00, 0x04, '0', '2', '1', '6'}
	payload := append([]byte("GIX000"), []byte("024")...)
	payload = append(payload, tags...)
	p := &fakePort{reads: [][]byte{response("OPN000"), append([]byte{protocol.PP_ACK}, protocol.BuildPacket(payload)...)}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	capabilities, err := s.GetDisplayCapabilities(context.Background())
	if err != nil {
		t.Fatal(err)
	}
	if capabilities.Model != "M" || capabilities.Manufacturer != "F" || !capabilities.HasColor || !capabilities.SupportsCTLS || capabilities.TextLines != 2 || capabilities.TextCols != 16 {
		t.Fatalf("capabilities = %#v", capabilities)
	}
}

func TestGetInfoRejectsResponseForAnotherCommand(t *testing.T) {
	p := &fakePort{reads: [][]byte{response("OPN000"), response("DSP000")}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	if _, err := s.GetInfoRaw(context.Background()); err != domainerror.ErrInvalidResponse {
		t.Fatalf("GetInfoRaw error = %v", err)
	}
}

func TestGetInfoReturnsTypedABECSErrorStatus(t *testing.T) {
	p := &fakePort{reads: [][]byte{response("OPN000"), response("GIX043")}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	_, err := s.GetInfoRaw(context.Background())
	var statusError *domainerror.StatusError
	if !errors.As(err, &statusError) || statusError.Code != "043" {
		t.Fatalf("GetInfoRaw error = %v", err)
	}
}

func TestServiceLifecycleRejectsInvalidStates(t *testing.T) {
	s := New(model.DefaultConfig(), &fakePort{})
	defer s.Shutdown()
	if err := s.SetConfig(model.PinpadConfig{}); err == nil {
		t.Fatal("expected invalid configuration error")
	}
	if _, err := s.GetInfoRaw(context.Background()); err != domainerror.ErrPinpadClosed {
		t.Fatalf("closed GetInfoRaw error = %v", err)
	}
	if err := s.Open(context.Background()); err == nil {
		t.Fatal("expected missing OPN response error")
	}
	if s.GetState() != model.StateClosed {
		t.Fatalf("state after failed open = %s", s.GetState())
	}
	if err := s.Close(context.Background()); err != nil {
		t.Fatal(err)
	}
}

func TestQRCodeValidationAndTransactionStub(t *testing.T) {
	s := New(model.DefaultConfig(), &fakePort{})
	defer s.Shutdown()
	if _, err := s.DisplayQRCode(context.Background(), "x", 49, 0, 0, 0); err == nil {
		t.Fatal("expected QR size validation")
	}
	if _, err := s.DisplayQRCode(context.Background(), "x", 50, 11, 0, 0); err == nil {
		t.Fatal("expected QR margin validation")
	}
	if _, err := s.DisplayQRCode(context.Background(), "x", 50, 0, 0, 0); err != domainerror.ErrQRCodeGeneratorNotConfigured {
		t.Fatalf("missing generator error = %v", err)
	}
	s.SetQRCodeGenerator(qrGeneratorFunc(func(string, int) ([]byte, error) { return nil, fmt.Errorf("generator failed") }))
	if _, err := s.DisplayQRCode(context.Background(), "x", 50, 0, 0, 0); err == nil {
		t.Fatal("expected QR generator error")
	}
	if _, err := s.TransactionGCX(context.Background(), TransactionGCXRequest{}); err != domainerror.ErrNotImplemented {
		t.Fatalf("TransactionGCX error = %v", err)
	}
}

func TestMenuAndKeyUseShortResponseData(t *testing.T) {
	gkyPayload := append([]byte("GKY000001"), byte(command.GKYKeyF2))
	p := &fakePort{reads: [][]byte{response("OPN000"), response("MNU0000012"), append([]byte{protocol.PP_ACK}, protocol.BuildPacket(gkyPayload)...)}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	menu, err := s.DisplayMNU(context.Background(), 10, "titulo", []string{"a", "b"})
	if err != nil || menu.Status != "000" || menu.SelectedIndex != 2 {
		t.Fatalf("MNU = %#v, %v", menu, err)
	}
	key, err := s.WaitForKeyPress(context.Background(), 10)
	if err != nil || key != command.GKYKeyF2 {
		t.Fatalf("GKY = %x, %v", key, err)
	}
}

func TestGPNParsesOnlyResponseData(t *testing.T) {
	data := make([]byte, 36)
	responsePayload := append([]byte("GPN000036"), data...)
	p := &fakePort{reads: [][]byte{response("OPN000"), append([]byte{protocol.PP_ACK}, protocol.BuildPacket(responsePayload)...)}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	pinBlock, ksn, err := s.SendGPNCommandDUKPT(context.Background(), "12345678901234567890", "1234", "mensagem")
	if err != nil || len(pinBlock) != 16 || len(ksn) != 20 {
		t.Fatalf("GPN sizes=%d/%d err=%v", len(pinBlock), len(ksn), err)
	}
}

func TestDisplayMultimediaTableAndResetFlows(t *testing.T) {
	reads := [][]byte{response("OPN000"), response("DSP000"), response("DEX000"), response("DSI000"), response("MLI000"), response("MLR000"), response("MLE000"), response("TLI000"), response("TLR000"), response("TLE000"), response("RST000")}
	s := New(model.DefaultConfig(), &fakePort{reads: reads})
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	if _, err := s.DisplayDSP(context.Background(), "a", "b"); err != nil {
		t.Fatal(err)
	}
	if _, err := s.DisplayDEX(context.Background(), "mensagem"); err != nil {
		t.Fatal(err)
	}
	if _, err := s.DisplayImage(context.Background(), "imagem"); err != nil {
		t.Fatal(err)
	}
	if err := s.SendMultimediaFile(context.Background(), "arquivo", []byte{1, 2}, nil); err != nil {
		t.Fatal(err)
	}
	if err := s.LoadCompleteEMVTable(context.Background(), "00", "versao", []string{"registro"}, nil); err != nil {
		t.Fatal(err)
	}
	if err := s.ResetPinpad(context.Background()); err != nil {
		t.Fatal(err)
	}
}

func TestAdvancedFlowsRespectSequenceAndKeepModelsSeparate(t *testing.T) {
	gcxData := append([]byte("GCX000006"), []byte{0x80, 0x4F, 0x00, 0x02, '0', '3'}...)
	gtkData := append([]byte("GTK000005"), []byte{0x80, 0x44, 0x00, 0x01, 0x01}...)
	goxData := append([]byte("GOX000010"), []byte{0x80, 0x56, 0x00, 0x06, '2', '0', '0', '0', '0', '0'}...)
	fcxData := append([]byte("FCX000007"), []byte{0x80, 0x58, 0x00, 0x03, '0', '0', '0'}...)
	p := &fakePort{reads: [][]byte{response("OPN000"), append([]byte{protocol.PP_ACK}, protocol.BuildPacket(gcxData)...), append([]byte{protocol.PP_ACK}, protocol.BuildPacket(gtkData)...), append([]byte{protocol.PP_ACK}, protocol.BuildPacket(goxData)...), append([]byte{protocol.PP_ACK}, protocol.BuildPacket(fcxData)...), response("CLX000")}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if _, err := s.GetTracks(context.Background(), command.GTKRequest{}); err != domainerror.ErrInvalidCommandSequence {
		t.Fatalf("GTK before GCX error = %v", err)
	}
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	gcx, err := s.SendGCXCommand(context.Background(), "000000000100", "090926", "121314", 0)
	if err != nil || gcx.CardType != command.GCXCardICC {
		t.Fatalf("GCX = %#v, %v", gcx, err)
	}
	keyIndex := 1
	gtk, err := s.GetTracks(context.Background(), command.GTKRequest{DataMethod: "50", KeyIndex: &keyIndex})
	if err != nil || len(gtk.Track1) != 1 || gcx.Track1 != "" {
		t.Fatalf("GTK = %#v, GCX = %#v, err = %v", gtk, gcx, err)
	}
	gox, err := s.ContinueEMV(context.Background(), command.GOXRequest{AcquirerReference: "01", PinMethod: "3", KeyIndex: 1})
	if err != nil || string(gox.Result) != "200000" {
		t.Fatalf("GOX = %#v, err = %v", gox, err)
	}
	fcx, err := s.FinalizeEMV(context.Background(), command.FCXRequest{Options: "0000", Authorization: "00"})
	if err != nil || string(fcx.Result) != "000" {
		t.Fatalf("FCX = %#v, err = %v", fcx, err)
	}
	if _, err := s.CloseVisual(context.Background(), command.CLXRequest{}); err != nil {
		t.Fatal(err)
	}
}

func TestServiceAdditionalFacadeFlows(t *testing.T) {
	pinData := make([]byte, 16)
	gpnPayload := append([]byte("GPN000016"), pinData...)
	gcxPayload := append([]byte("GCX000006"), []byte{0x80, 0x4F, 0x00, 0x02, '0', '3'}...)
	reads := [][]byte{
		response("OPN000"), response("DSI000"), response("MLI000"), response("MLR000"), response("MLE000"),
		append([]byte{protocol.PP_ACK}, protocol.BuildPacket(gpnPayload)...),
		append([]byte{protocol.PP_ACK}, protocol.BuildPacket(gcxPayload)...), append([]byte{protocol.PP_ACK}, protocol.BuildPacket(gcxPayload)...),
		response("MLI000"), response("MLR000"), response("MLE000"), {protocol.PP_ACK},
	}
	p := &fakePort{reads: reads}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	config := s.GetConfig()
	config.Timeout = time.Second
	if err := s.SetConfig(config); err != nil || s.GetConfig().Timeout != time.Second {
		t.Fatalf("SetConfig = %v, %#v", err, s.GetConfig())
	}
	s.SetLogger(slog.New(slog.NewTextHandler(io.Discard, nil)))
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	if err := s.SetConfig(config); !errors.Is(err, domainerror.ErrPinpadBusy) {
		t.Fatalf("SetConfig while open = %v", err)
	}
	if _, err := s.DisplayDSI(context.Background(), "imagem"); err != nil {
		t.Fatal(err)
	}
	if err := s.LoadMultimediaFile(context.Background(), "arquivo", []byte{1}, nil); err != nil {
		t.Fatal(err)
	}
	if pinBlock, ksn, err := s.SendGPNCommandMK(context.Background(), 1, "1234567890123456", "ok"); err != nil || len(pinBlock) != 16 || len(ksn) != 0 {
		t.Fatalf("GPN MK sizes=%d/%d err=%v", len(pinBlock), len(ksn), err)
	}
	if _, err := s.SendGCXInitialization(context.Background()); err != nil {
		t.Fatal(err)
	}
	if _, err := s.PurchaseGCX(context.Background(), "000000000100", "090926", "121314", true, true); err != nil {
		t.Fatal(err)
	}
	path := t.TempDir() + "\\imagem.bin"
	if err := os.WriteFile(path, []byte{2}, 0o600); err != nil {
		t.Fatal(err)
	}
	if err := s.LoadMultimediaPath(context.Background(), path, "imagem", nil); err != nil {
		t.Fatal(err)
	}
	if err := s.exchangeAckOnly(context.Background(), []byte("ACK")); err != nil {
		t.Fatal(err)
	}
}

func TestServiceTransferAndAckErrors(t *testing.T) {
	s := New(model.DefaultConfig(), &fakePort{})
	defer s.Shutdown()
	if err := s.SendMultimediaFile(context.Background(), "arquivo", nil, nil); err == nil {
		t.Fatal("expected empty multimedia error")
	}
	ctx, cancel := context.WithCancel(context.Background())
	cancel()
	if err := s.SendMultimediaFile(ctx, "arquivo", []byte{1}, nil); !errors.Is(err, context.Canceled) {
		t.Fatalf("cancelled multimedia = %v", err)
	}
	for _, acknowledgement := range []byte{protocol.PP_NAK, protocol.PP_EOT} {
		p := &fakePort{reads: [][]byte{{acknowledgement}}}
		service := New(model.DefaultConfig(), p)
		if err := service.exchangeAckOnly(context.Background(), []byte("x")); err == nil {
			t.Fatalf("acknowledgement %x must fail", acknowledgement)
		}
		service.Shutdown()
	}
	if err := s.LoadMultimediaPath(context.Background(), "missing-file", "x", nil); err == nil {
		t.Fatal("expected missing multimedia file error")
	}
}

func TestOpenSecurePreservesAndInstallsEphemeralKSEC(t *testing.T) {
	privateKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		t.Fatal(err)
	}
	ksec := make([]byte, 16)
	if _, err := rand.Read(ksec); err != nil {
		t.Fatal(err)
	}
	ciphertext, err := rsa.EncryptPKCS1v15(rand.Reader, &privateKey.PublicKey, ksec)
	if err != nil {
		t.Fatal(err)
	}
	secureData := "256" + hex.EncodeToString(ciphertext)
	securePayload := fmt.Sprintf("OPN000%03d%s", len(secureData), secureData)
	p := &fakePort{reads: [][]byte{response("OPN000"), response(securePayload), response("CLO000")}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.OpenSecure(context.Background(), privateKey); err != nil {
		t.Fatal(err)
	}
	if s.secureSession == nil {
		t.Fatal("secure session was not installed")
	}
	if err := s.Close(context.Background()); err != nil {
		t.Fatal(err)
	}
	if s.secureSession != nil {
		t.Fatal("secure session was not cleared by CLO")
	}
	if err := s.OpenSecure(context.Background(), nil); err == nil {
		t.Fatal("expected missing private key error")
	}
}

func TestServiceRejectsCanceledAndUnavailableOperations(t *testing.T) {
	canceled, cancel := context.WithCancel(context.Background())
	cancel()
	s := New(model.DefaultConfig(), nil)
	defer s.Shutdown()
	if err := s.Open(canceled); !errors.Is(err, context.Canceled) {
		t.Fatalf("canceled Open = %v", err)
	}
	if err := s.Open(context.Background()); !errors.Is(err, domainerror.ErrPortUnavailable) {
		t.Fatalf("Open without port = %v", err)
	}
	if _, err := s.DisplayQRCode(canceled, "x", 50, 0, 0, 0); !errors.Is(err, context.Canceled) {
		t.Fatalf("canceled QR code = %v", err)
	}
	s.config.Timeout = 0
	if _, err := s.submit(context.Background(), command.Command{}); !errors.Is(err, domainerror.ErrTimeout) {
		t.Fatalf("submit with invalid timeout = %v", err)
	}
}

func TestServiceStreamAndExchangeReportTransportFailures(t *testing.T) {
	tests := []struct {
		name string
		port *fakePort
		want error
	}{
		{name: "write", port: &fakePort{writeErr: io.ErrClosedPipe}, want: io.ErrClosedPipe},
		{name: "read", port: &fakePort{readErr: io.ErrUnexpectedEOF}, want: io.ErrUnexpectedEOF},
		{name: "empty read", port: &fakePort{}, want: domainerror.ErrTimeout},
		{name: "unexpected acknowledgement", port: &fakePort{reads: [][]byte{{0x01}}}, want: domainerror.ErrInvalidResponse},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			s := New(model.DefaultConfig(), test.port)
			defer s.Shutdown()
			_, err := s.exchange(context.Background(), []byte("command"))
			if !errors.Is(err, test.want) {
				t.Fatalf("exchange error = %v, want %v", err, test.want)
			}
		})
	}
}

func TestServiceResponseValidationAndByteStreamBoundaries(t *testing.T) {
	if _, err := validateResponseCommand(command.CommandGIX, &model.Response{RawData: []byte("DSP000")}, nil); !errors.Is(err, domainerror.ErrInvalidResponse) {
		t.Fatalf("wrong command = %v", err)
	}
	response := &model.Response{AckType: "EOT"}
	if got, err := validateResponseCommand(command.CommandGIX, response, nil); got != response || err != nil {
		t.Fatalf("EOT validation = %#v, %v", got, err)
	}
	if got, err := validateResponseCommand(command.CommandGIX, nil, io.ErrUnexpectedEOF); got != nil || !errors.Is(err, io.ErrUnexpectedEOF) {
		t.Fatalf("transport validation = %#v, %v", got, err)
	}
	stream := newByteStream(nil)
	if err := stream.fill(context.Background()); !errors.Is(err, domainerror.ErrPortUnavailable) {
		t.Fatalf("nil stream port = %v", err)
	}
	canceled, cancel := context.WithCancel(context.Background())
	cancel()
	if _, err := stream.NextByte(canceled); !errors.Is(err, context.Canceled) {
		t.Fatalf("canceled stream = %v", err)
	}
}

type qrGeneratorFunc func(string, int) ([]byte, error)

func (f qrGeneratorFunc) Generate(data string, size int) ([]byte, error) { return f(data, size) }
