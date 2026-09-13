package service

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"bytes"
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
	open             bool
	writes           [][]byte
	reads            [][]byte
	readErr          error
	readErrs         []error
	writeErr         error
	traceCmd         command.Type
	traceRedact      bool
	lastReadDeadline time.Time
	disableAutoEOT   bool
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

func TestServicePropagatesConsumerTraceRedactionPolicy(t *testing.T) {
	p := &fakePort{}
	s := New(model.DefaultConfig(), p)
	s.SetTraceSensitive(command.CommandDSP, true)
	s.setTraceCommand(command.CommandDSP, false)
	if p.traceCmd != command.CommandDSP || !p.traceRedact {
		t.Fatalf("trace policy command=%s redact=%t", p.traceCmd, p.traceRedact)
	}
	s.SetTraceSensitive(command.CommandDSP, false)
	s.setTraceCommand(command.CommandDSP, false)
	if p.traceRedact {
		t.Fatal("trace redaction policy was not removed")
	}
	s.setTraceCommand(command.CommandOPN, true)
	if p.traceCmd != command.CommandOPN || !p.traceRedact {
		t.Fatalf("forced secure OPN policy command=%s redact=%t", p.traceCmd, p.traceRedact)
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
	if !p.disableAutoEOT && len(b) == 1 && b[0] == protocol.PP_CAN &&
		(len(p.reads) == 0 || len(p.reads[0]) != 1 || p.reads[0][0] != protocol.PP_EOT) {
		p.reads = append([][]byte{{protocol.PP_EOT}}, p.reads...)
	}
	return nil
}

func TestCancelHandshakeIgnoresUnrelatedBytesUntilEOT(t *testing.T) {
	port := &fakePort{
		disableAutoEOT: true,
		reads:          [][]byte{{0x01, protocol.PP_ACK}, {protocol.PP_EOT}},
	}
	svc := New(model.DefaultConfig(), port)
	defer svc.Shutdown()

	if err := svc.cancelHandshake(context.Background()); err != nil {
		t.Fatal(err)
	}
	if len(port.writes) != 1 || !bytes.Equal(port.writes[0], []byte{protocol.PP_CAN}) {
		t.Fatalf("escritas = % X, want um CAN", port.writes)
	}
}
func (p *fakePort) Read(ctx context.Context) ([]byte, error) {
	p.lastReadDeadline, _ = ctx.Deadline()
	if p.readErr != nil {
		return nil, p.readErr
	}
	if len(p.readErrs) > 0 {
		err := p.readErrs[0]
		p.readErrs = p.readErrs[1:]
		if err != nil {
			return nil, err
		}
	}
	if len(p.reads) == 0 {
		return nil, nil
	}
	x := p.reads[0]
	p.reads = p.reads[1:]
	return x, nil
}

func (p *fakePort) SetTraceCommand(kind command.Type) { p.traceCmd = kind }
func (p *fakePort) SetTracePolicy(kind command.Type, redact bool) {
	p.traceCmd = kind
	p.traceRedact = redact
}

func response(payload string) []byte {
	return append([]byte{protocol.PP_ACK}, protocol.BuildPacket([]byte(payload))...)
}

func validICCResponsePayload() []byte {
	data := make([]byte, 0, 53)
	appendTag := func(tag uint16, value string) {
		data = append(data, byte(tag>>8), byte(tag), byte(len(value)>>8), byte(len(value)))
		data = append(data, value...)
	}
	appendTag(uint16(protocol.TagCardType), command.GCXCardICC)
	appendTag(uint16(protocol.TagAIDTableInfo), "080301")
	appendTag(uint16(protocol.TagPAN), "4444333322221111")
	appendTag(uint16(protocol.TagPANSequenceNumber), "01")
	appendTag(uint16(protocol.TagLabel), "CREDITO")
	return append([]byte(fmt.Sprintf("GCX000%03d", len(data))), data...)
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
	if len(p.writes) != 5 {
		t.Fatalf("expected initial CAN, OPN, GIX, CAN and CLO exchanges, got %d writes", len(p.writes))
	}
}

func TestLoadCompleteEMVTableContinuesOnTableVersionDifferent(t *testing.T) {
	p := &fakePort{reads: [][]byte{response("OPN000"), response("TLI020"), response("TLR000"), response("TLE000")}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	if err := s.LoadCompleteEMVTable(context.Background(), "00", "TABVER0001", []string{"record"}, nil); err != nil {
		t.Fatal(err)
	}
	if len(p.writes) != 5 {
		t.Fatalf("writes = %d, want CAN, OPN, TLI, TLR and TLE", len(p.writes))
	}
}

func TestTableLoadRecordSendsExactTLRPayload(t *testing.T) {
	p := &fakePort{reads: [][]byte{response("OPN000"), response("TLR000")}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	if _, err := s.TableLoadRecord(context.Background(), []string{"ABC", "DE"}); err != nil {
		t.Fatal(err)
	}
	payload, err := protocol.ValidatePacket(p.writes[2])
	if err != nil {
		t.Fatal(err)
	}
	if got, want := string(payload), "TLR01302003ABC002DE"; got != want {
		t.Fatalf("TLR = %q, want %q", got, want)
	}
}

func TestLoadCompleteEMVTableSplitsTLRByProtocolBodyLimit(t *testing.T) {
	p := &fakePort{reads: [][]byte{
		response("OPN000"), response("TLI000"), response("TLR000"), response("TLR000"), response("TLE000"),
	}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	records := []string{strings.Repeat("A", 400), strings.Repeat("B", 400), strings.Repeat("C", 400)}
	var progress []int64
	if err := s.LoadCompleteEMVTable(context.Background(), "00", "TABVER0001", records, func(_ context.Context, current, _ int64) error {
		progress = append(progress, current)
		return nil
	}); err != nil {
		t.Fatal(err)
	}
	if len(p.writes) != 6 {
		t.Fatalf("writes = %d, want CAN, OPN, TLI, two TLR and TLE", len(p.writes))
	}
	for index, wantNREC := range []string{"02", "01"} {
		payload, err := protocol.ValidatePacket(p.writes[index+3])
		if err != nil {
			t.Fatal(err)
		}
		if got := string(payload[6:8]); got != wantNREC {
			t.Fatalf("TLR %d NREC = %q, want %q", index+1, got, wantNREC)
		}
		if len(payload)-6 > command.TLRMaxBlockSize {
			t.Fatalf("TLR %d body = %d, max %d", index+1, len(payload)-6, command.TLRMaxBlockSize)
		}
	}
	if fmt.Sprint(progress) != "[2 3]" {
		t.Fatalf("progress = %v, want [2 3]", progress)
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
	p := &fakePort{reads: [][]byte{response("OPN000"), response("MNU000006\x80\x4D\x00\x02\x30\x32"), response("GKY005")}}
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

func TestDisplayMNUParsesRealTLVSelection(t *testing.T) {
	menuData := append([]byte("MNU000006"), 0x80, 0x4D, 0x00, 0x02, '0', '3')
	p := &fakePort{reads: [][]byte{
		response("OPN000"),
		append([]byte{protocol.PP_ACK}, protocol.BuildPacket(menuData)...),
	}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}

	menu, err := s.DisplayMNU(context.Background(), 30, "TESTE", []string{"OPCAO1", "OPCAO2", "OPCAO3"})
	if err != nil || menu.Status != "000" || menu.SelectedIndex != 3 {
		t.Fatalf("MNU real TLV = %#v, %v", menu, err)
	}
}

func TestGPNParsesOnlyResponseData(t *testing.T) {
	data := []byte(strings.Repeat("0", 36))
	responsePayload := append([]byte("GPN000036"), data...)
	p := &fakePort{reads: [][]byte{response("OPN000"), append([]byte{protocol.PP_ACK}, protocol.BuildPacket(responsePayload)...)}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	pinBlock, ksn, err := s.SendGPNCommandDUKPT(context.Background(), 1, "1234", "mensagem")
	if err != nil || len(pinBlock) != 8 || len(ksn) != 10 {
		t.Fatalf("GPN sizes=%d/%d err=%v", len(pinBlock), len(ksn), err)
	}
}

func TestDisplayMultimediaAndTableFlows(t *testing.T) {
	reads := [][]byte{response("OPN000"), response("DSP000"), response("DEX000"), response("DSI000"), response("MLI000"), response("MLR000"), response("MLE000"), response("TLI000"), response("TLR000"), response("TLE000")}
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
	if _, err := s.DisplayImage(context.Background(), "IMG00001"); err != nil {
		t.Fatal(err)
	}
	if err := s.SendMultimediaFile(context.Background(), "IMG00001", []byte("\x89PNG\r\n\x1a\n"), nil); err != nil {
		t.Fatal(err)
	}
	if err := s.LoadCompleteEMVTable(context.Background(), "00", "TABVER0001", []string{"registro"}, nil); err != nil {
		t.Fatal(err)
	}
}

func TestAdvancedFlowsRespectSequenceAndKeepModelsSeparate(t *testing.T) {
	gcxData := validICCResponsePayload()
	gtkData := append([]byte("GTK000019"), []byte{0x80, 0x44, 0x00, 0x01, 0x01}...)
	gtkData = append(gtkData, []byte{0x80, 0x47, 0x00, 0x0A, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}...)
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
	pinData := []byte(strings.Repeat("0", 36))
	gpnPayload := append([]byte("GPN000036"), pinData...)
	gcxPayload := validICCResponsePayload()
	reads := [][]byte{
		response("OPN000"), response("DSI000"), response("MLI000"), response("MLR000"), response("MLE000"),
		append([]byte{protocol.PP_ACK}, protocol.BuildPacket(gpnPayload)...),
		append([]byte{protocol.PP_ACK}, protocol.BuildPacket(gcxPayload)...),
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
	if _, err := s.DisplayDSI(context.Background(), "IMG00001"); err != nil {
		t.Fatal(err)
	}
	if err := s.LoadMultimediaFile(context.Background(), "IMG00001", []byte("\x89PNG\r\n\x1a\n"), nil); err != nil {
		t.Fatal(err)
	}
	if pinBlock, ksn, err := s.SendGPNCommandMK(context.Background(), 1, make([]byte, 16), "1234567890123456", "ok"); err != nil || len(pinBlock) != 8 || len(ksn) != 10 {
		t.Fatalf("GPN MK sizes=%d/%d err=%v", len(pinBlock), len(ksn), err)
	}
	writesBeforePurchase := len(p.writes)
	if _, err := s.PurchaseGCX(context.Background(), "000000000100", "260909", "121314", true, false); err != nil {
		t.Fatal(err)
	}
	if got := len(p.writes) - writesBeforePurchase; got != 1 {
		t.Fatalf("PurchaseGCX writes = %d, want 1", got)
	}
	path := t.TempDir() + "\\imagem.png"
	if err := os.WriteFile(path, []byte("\x89PNG\r\n\x1a\n"), 0o600); err != nil {
		t.Fatal(err)
	}
	if err := s.LoadMultimediaPath(context.Background(), path, "IMG00002", nil); err != nil {
		t.Fatal(err)
	}
	if err := s.exchangeAckOnly(context.Background(), []byte("ACK")); err != nil {
		t.Fatal(err)
	}
}

func TestPurchaseGCXConsumesNotificationsAndPreservesCallerDeadline(t *testing.T) {
	gcxPayload := validICCResponsePayload()
	stream := append([]byte{protocol.PP_ACK}, protocol.BuildPacket([]byte("NTM000032"+strings.Repeat("A", 32)))...)
	stream = append(stream, protocol.BuildPacket([]byte("NTM000032"+strings.Repeat("B", 32)))...)
	stream = append(stream, protocol.BuildPacket(gcxPayload)...)
	p := &fakePort{reads: [][]byte{response("OPN000"), stream}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	writesBefore := len(p.writes)
	ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
	defer cancel()
	result, err := s.PurchaseGCX(ctx, "000000000100", "260912", "225000", true, true)
	if err != nil || result.CardType != command.GCXCardICC {
		t.Fatalf("PurchaseGCX result=%#v err=%v", result, err)
	}
	if got := len(p.writes) - writesBefore; got != 1 {
		t.Fatalf("writes after NTM = %d, want 1", got)
	}
	payload, err := protocol.ValidatePacket(p.writes[len(p.writes)-1])
	if err != nil || string(payload[len(payload)-5:]) != "11000" {
		t.Fatalf("GCX options payload=% X err=%v", payload, err)
	}
	if remaining := time.Until(p.lastReadDeadline); remaining < time.Second || remaining > 3*time.Second {
		t.Fatalf("GCX ACK deadline remaining=%s, want about 2s", remaining)
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
	p := &fakePort{reads: [][]byte{response(securePayload), response("CLO000")}}
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

func TestExchangeRetransmitsAfterNAK(t *testing.T) {
	port := &fakePort{reads: [][]byte{{protocol.PP_NAK}, response("GIX000")}}
	svc := New(model.DefaultConfig(), port)
	defer svc.Shutdown()

	if _, err := svc.exchangeCommand(context.Background(), command.CommandGIX, protocol.BuildPacket([]byte("GIX000"))); err != nil {
		t.Fatal(err)
	}
	if len(port.writes) != 2 || !bytes.Equal(port.writes[0], port.writes[1]) {
		t.Fatalf("retransmissões = %d; pacotes=% X", len(port.writes), port.writes)
	}
}

func TestExchangeStopsAfterThreeNAKs(t *testing.T) {
	port := &fakePort{reads: [][]byte{{protocol.PP_NAK}, {protocol.PP_NAK}, {protocol.PP_NAK}}}
	svc := New(model.DefaultConfig(), port)
	defer svc.Shutdown()

	if _, err := svc.exchangeCommand(context.Background(), command.CommandGIX, protocol.BuildPacket([]byte("GIX000"))); !errors.Is(err, domainerror.ErrNakReceived) {
		t.Fatalf("erro = %v", err)
	}
	if len(port.writes) != protocol.MaxAttempts {
		t.Fatalf("tentativas = %d, want %d", len(port.writes), protocol.MaxAttempts)
	}
}

func TestExchangeStopsAfterThreeMissingAcknowledgements(t *testing.T) {
	port := &fakePort{}
	svc := New(model.DefaultConfig(), port)
	defer svc.Shutdown()

	if _, err := svc.exchangeCommand(context.Background(), command.CommandGIX, protocol.BuildPacket([]byte("GIX000"))); !errors.Is(err, domainerror.ErrTimeout) {
		t.Fatalf("erro = %v, want timeout", err)
	}
	if len(port.writes) != protocol.MaxAttempts {
		t.Fatalf("tentativas = %d, want %d", len(port.writes), protocol.MaxAttempts)
	}
}

func TestExchangeDoesNotAcknowledgeValidPinpadResponse(t *testing.T) {
	port := &fakePort{reads: [][]byte{response("GIX000")}}
	svc := New(model.DefaultConfig(), port)
	defer svc.Shutdown()

	commandPacket := protocol.BuildPacket([]byte("GIX000"))
	if _, err := svc.exchangeCommand(context.Background(), command.CommandGIX, commandPacket); err != nil {
		t.Fatal(err)
	}
	if len(port.writes) != 1 || !bytes.Equal(port.writes[0], commandPacket) {
		t.Fatalf("escritas = % X, want somente o comando", port.writes)
	}
}

func TestExchangeRequestsRetransmissionForInvalidCRC(t *testing.T) {
	corrupted := protocol.BuildPacket([]byte("GIX000"))
	corrupted[len(corrupted)-1] ^= 0xff
	firstRead := append([]byte{protocol.PP_ACK}, corrupted...)
	port := &fakePort{reads: [][]byte{firstRead, protocol.BuildPacket([]byte("GIX000"))}}
	svc := New(model.DefaultConfig(), port)
	defer svc.Shutdown()

	if _, err := svc.exchangeCommand(context.Background(), command.CommandGIX, protocol.BuildPacket([]byte("GIX000"))); err != nil {
		t.Fatal(err)
	}
	if len(port.writes) != 2 || len(port.writes[1]) != 1 || port.writes[1][0] != protocol.PP_NAK {
		t.Fatalf("escritas = % X, want comando seguido de NAK", port.writes)
	}
}

func TestOpenStartsWithCANAndUsesClassicOPN(t *testing.T) {
	port := &fakePort{reads: [][]byte{response("OPN000")}}
	svc := New(model.DefaultConfig(), port)
	defer svc.Shutdown()

	if err := svc.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	if len(port.writes) < 2 || !bytes.Equal(port.writes[0], []byte{protocol.PP_CAN}) {
		t.Fatalf("primeira escrita = % X, want CAN", port.writes)
	}
	payload, err := protocol.ValidatePacket(port.writes[1])
	if err != nil || string(payload) != "OPN" {
		t.Fatalf("OPN = %q, %v", payload, err)
	}
}

func TestCancelHandshakeRetriesCANAndUsesTwoSecondDeadline(t *testing.T) {
	port := &fakePort{
		disableAutoEOT: true,
		readErrs:       []error{context.DeadlineExceeded, nil},
		reads:          [][]byte{{protocol.PP_EOT}},
	}
	svc := New(model.DefaultConfig(), port)
	defer svc.Shutdown()

	started := time.Now()
	if err := svc.cancelHandshake(context.Background()); err != nil {
		t.Fatal(err)
	}
	if len(port.writes) != 2 || !bytes.Equal(port.writes[0], []byte{protocol.PP_CAN}) || !bytes.Equal(port.writes[1], []byte{protocol.PP_CAN}) {
		t.Fatalf("escritas = % X, want dois CAN", port.writes)
	}
	remaining := port.lastReadDeadline.Sub(started)
	if remaining < 1900*time.Millisecond || remaining > 2100*time.Millisecond {
		t.Fatalf("prazo EOT = %s, want aproximadamente 2s", remaining)
	}
}

func TestPurchaseGCXAppliesContactlessFallback(t *testing.T) {
	card := validICCResponsePayload()
	port := &fakePort{reads: [][]byte{
		response("OPN000"),
		response("GCX081"),
		response("GCX081"),
		append([]byte{protocol.PP_ACK}, protocol.BuildPacket(card)...),
	}}
	svc := New(model.DefaultConfig(), port)
	defer svc.Shutdown()
	if err := svc.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	if _, err := svc.PurchaseGCX(context.Background(), "000000000100", "260913", "090000", true, true); err != nil {
		t.Fatal(err)
	}
	if len(port.writes) != 5 {
		t.Fatalf("escritas = %d, want CAN, OPN e três GCX", len(port.writes))
	}
	for index, want := range []string{"11000", "11000", "01000"} {
		payload, err := protocol.ValidatePacket(port.writes[index+2])
		if err != nil || string(payload[len(payload)-5:]) != want {
			t.Fatalf("GCX %d = %q, %v; want %s", index, payload, err, want)
		}
	}
}

type qrGeneratorFunc func(string, int) ([]byte, error)

func (f qrGeneratorFunc) Generate(data string, size int) ([]byte, error) { return f(data, size) }
