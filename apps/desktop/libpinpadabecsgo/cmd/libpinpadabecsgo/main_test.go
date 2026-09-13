package main

import (
	"bufio"
	"bytes"
	"fmt"
	"io"
	"log/slog"
	"os"
	"path/filepath"
	"strings"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/service"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
)

func captureOutput(t *testing.T, action func()) string {
	t.Helper()
	previous := os.Stdout
	reader, writer, err := os.Pipe()
	if err != nil {
		t.Fatal(err)
	}
	os.Stdout = writer
	defer func() { os.Stdout = previous }()
	action()
	if err := writer.Close(); err != nil {
		t.Fatal(err)
	}
	output, err := io.ReadAll(reader)
	if err != nil {
		t.Fatal(err)
	}
	return string(output)
}

func TestResolveLogDestinationUsesTheSameModuleRootFromNestedDirectories(t *testing.T) {
	root := t.TempDir()
	if err := os.WriteFile(filepath.Join(root, "go.mod"), []byte("module "+modulePath+"\n\ngo 1.26\n"), 0o600); err != nil {
		t.Fatal(err)
	}
	nested := filepath.Join(root, "cmd", "libpinpadabecsgo")
	if err := os.MkdirAll(nested, 0o755); err != nil {
		t.Fatal(err)
	}
	want := filepath.Join(root, filepath.FromSlash(defaultTraceRelPath))
	for _, start := range []string{root, nested} {
		got, err := resolveLogDestinationFrom("", start)
		if err != nil || got != want {
			t.Fatalf("start=%q destination=%q want=%q err=%v", start, got, want, err)
		}
	}
	relative, err := resolveLogDestinationFrom(filepath.Join("custom", "trace.txt"), nested)
	if err != nil || relative != filepath.Join(root, "custom", "trace.txt") {
		t.Fatalf("relative destination=%q err=%v", relative, err)
	}
	if _, err := resolveLogDestinationFrom("", t.TempDir()); err == nil {
		t.Fatal("expected error when the module root cannot be found")
	}
	if _, err := os.Stat(filepath.Join(nested, "logs", "LogPinpadAbecs.txt")); !os.IsNotExist(err) {
		t.Fatalf("nested trace file must not be created: %v", err)
	}
}

func TestResolveLogDestinationHandlesAbsoluteAndRelativeEnvironmentValues(t *testing.T) {
	absolute := filepath.Join(t.TempDir(), "trace.txt")
	got, err := resolveLogDestination(absolute)
	if err != nil || got != absolute {
		t.Fatalf("absolute destination=%q want=%q err=%v", got, absolute, err)
	}
	got, err = resolveLogDestination(filepath.Join("custom", "trace.txt"))
	if err != nil {
		t.Fatal(err)
	}
	root, err := findModuleRoot(".")
	if err != nil {
		t.Fatal(err)
	}
	if want := filepath.Join(root, "custom", "trace.txt"); got != want {
		t.Fatalf("relative destination=%q want=%q", got, want)
	}
	got, err = resolveLogDestination("")
	if err != nil {
		t.Fatal(err)
	}
	if want := filepath.Join(root, filepath.FromSlash(defaultTraceRelPath)); got != want {
		t.Fatalf("default destination=%q want=%q", got, want)
	}
	if declaresModule([]byte("module example.invalid\n"), modulePath) || declaresModule([]byte("// empty\n"), modulePath) {
		t.Fatal("unexpected module declaration match")
	}
}

func TestConfigureTracerUsesAbsoluteEnvironmentDestination(t *testing.T) {
	tracer := logging.NewTracer()
	defer func() {
		if err := tracer.Close(); err != nil {
			t.Fatal(err)
		}
	}()

	path := filepath.Join(t.TempDir(), "LogPinpadAbecs.txt")
	t.Setenv("PINPAD_LOG_FILE", path)
	if destination, err := configureTracer(tracer); err != nil || destination != path {
		t.Fatalf("configured destination=%q err=%v", destination, err)
	}
	if err := tracer.RecordOpen("COM7", 19200); err != nil {
		t.Fatal(err)
	}
	if _, err := os.Stat(path); err != nil {
		t.Fatalf("trace file was not created: %v", err)
	}
	content, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	if !strings.Contains(string(content), "TRACE destination="+path+" enabled") {
		t.Fatalf("activation marker does not contain the absolute path: %q", content)
	}
}

func TestConfigureTracerRejectsNilTracer(t *testing.T) {
	t.Setenv("PINPAD_LOG_FILE", filepath.Join(t.TempDir(), "trace.txt"))
	if _, err := configureTracer(nil); err == nil {
		t.Fatal("expected nil tracer configuration to fail")
	}
}

func TestRunReturnsFailureForInvalidConfigurationAndTraceDirectory(t *testing.T) {
	t.Run("invalid configuration", func(t *testing.T) {
		t.Setenv("PINPAD_BAUDRATE", "invalid")
		if code := run(); code != 1 {
			t.Fatalf("run code=%d", code)
		}
	})
	t.Run("trace parent is a file", func(t *testing.T) {
		t.Setenv("PORTA_PINPAD", "COM7")
		t.Setenv("PINPAD_BAUDRATE", "19200")
		t.Setenv("PINPAD_TIMEOUT", "30")
		parent := filepath.Join(t.TempDir(), "not-a-directory")
		if err := os.WriteFile(parent, []byte("x"), 0o600); err != nil {
			t.Fatal(err)
		}
		t.Setenv("PINPAD_LOG_FILE", filepath.Join(parent, "trace.txt"))
		if code := run(); code != 1 {
			t.Fatalf("run code=%d", code)
		}
	})
}

func TestCLIHelpersParseAndDescribeValues(t *testing.T) {
	if got := splitOptions(" um, ,dois , tres "); strings.Join(got, ",") != "um,dois,tres" {
		t.Fatalf("splitOptions = %#v", got)
	}
	if got := splitRecords(" um; ;dois ; tres "); strings.Join(got, ",") != "um,dois,tres" {
		t.Fatalf("splitRecords = %#v", got)
	}
	reader := bufio.NewReader(strings.NewReader(" 42 \ninvalid\ntexto\n"))
	if value := readInt(reader, ""); value != 42 {
		t.Fatalf("readInt = %d", value)
	}
	if value := readInt(reader, ""); value != 0 {
		t.Fatalf("invalid readInt = %d", value)
	}
	if value := readLine(reader, ""); value != "texto" {
		t.Fatalf("readLine = %q", value)
	}
	if stateName(model.StateOpen) != "aberto" || stateName(model.StateBusy) != "ocupado" || stateName(model.StateClosed) != "fechado" {
		t.Fatal("state names do not match the local CLI contract")
	}
}

func TestReadGCXOptions(t *testing.T) {
	tests := []struct {
		name       string
		input      string
		enableCTLS bool
		hideAmount bool
		wantError  bool
	}{
		{name: "chip showing amount", input: "1\n1\n"},
		{name: "chip hiding amount", input: "1\n2\n", hideAmount: true},
		{name: "contactless showing amount", input: "2\n1\n", enableCTLS: true},
		{name: "contactless hiding amount", input: "2\n2\n", enableCTLS: true, hideAmount: true},
		{name: "invalid interface", input: "3\n", wantError: true},
		{name: "invalid visibility", input: "1\n3\n", wantError: true},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			var enableCTLS, hideAmount bool
			var err error
			output := captureOutput(t, func() {
				enableCTLS, hideAmount, err = readGCXOptions(bufio.NewReader(strings.NewReader(test.input)))
			})
			if (err != nil) != test.wantError || enableCTLS != test.enableCTLS || hideAmount != test.hideAmount {
				t.Fatalf("readGCXOptions()=%t,%t,%v", enableCTLS, hideAmount, err)
			}
			if !strings.Contains(output, "Modo de leitura do cartao") {
				t.Fatalf("GCX prompt missing: %q", output)
			}
		})
	}
}

func TestReadGTKRequest(t *testing.T) {
	tests := []struct {
		name      string
		input     string
		wantError bool
		validate  func(*testing.T, command.GTKRequest)
	}{
		{
			name:  "trilhas em claro",
			input: "1\n",
			validate: func(t *testing.T, request command.GTKRequest) {
				payload, err := command.BuildGTKCommand(request)
				if err != nil || string(payload) != "GTK000" || request.KeyIndex != nil {
					t.Fatalf("GTK claro = %q, %#v, %v", payload, request, err)
				}
			},
		},
		{
			name:  "trilhas DUKPT",
			input: "2\n02\n",
			validate: func(t *testing.T, request command.GTKRequest) {
				if request.DataMethod != "50" || request.Tracks != "1111" || request.KeyIndex == nil || *request.KeyIndex != 2 {
					t.Fatalf("GTK DUKPT = %#v", request)
				}
				payload, err := command.BuildGTKCommand(request)
				if err != nil {
					t.Fatal(err)
				}
				want := []byte{'G', 'T', 'K', '0', '2', '0', 0, 3, 0, 2, '5', '0', 0, 7, 0, 4, '1', '1', '1', '1', 0, 9, 0, 2, '0', '2'}
				if !bytes.Equal(payload, want) {
					t.Fatalf("GTK DUKPT = %X, want %X", payload, want)
				}
			},
		},
		{name: "modo inválido", input: "3\n", wantError: true},
		{name: "índice não numérico", input: "2\nabc\n", wantError: true},
		{name: "índice acima de N2", input: "2\n100\n", wantError: true},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			var request command.GTKRequest
			var err error
			output := captureOutput(t, func() {
				request, err = readGTKRequest(bufio.NewReader(strings.NewReader(test.input)))
			})
			if (err != nil) != test.wantError {
				t.Fatalf("readGTKRequest()=%#v, %v", request, err)
			}
			if test.validate != nil {
				test.validate(t, request)
			}
			if !strings.Contains(output, "Modo de retorno das trilhas") {
				t.Fatalf("prompt GTK ausente: %q", output)
			}
		})
	}
}

func TestRecordGTKResultLogsOnlyClearTracks(t *testing.T) {
	path := filepath.Join(t.TempDir(), "trace.log")
	tracer := logging.NewTracer()
	if active, err := tracer.SetLogDestination(path); err != nil || !active {
		t.Fatalf("SetLogDestination active=%t err=%v", active, err)
	}
	response := &model.GTKResponse{
		Track1: []byte("TRACK-ONE"),
		Track2: []byte{0x54, 0x28, 0x20, 0x60, 0x97, 0x98, 0x40, 0x97, 0xD1, 0x12, 0x23, 0x36, 0x65, 0x5F},
	}
	logged, err := recordGTKResult(tracer, command.GTKRequest{}, response)
	if err != nil || !logged {
		t.Fatalf("clear GTK logged=%t err=%v", logged, err)
	}
	logged, err = recordGTKResult(tracer, command.GTKRequest{DataMethod: "50"}, response)
	if err != nil || logged {
		t.Fatalf("encrypted GTK logged=%t err=%v", logged, err)
	}
	if _, err := recordGTKResult(tracer, command.GTKRequest{}, nil); err == nil {
		t.Fatal("missing clear GTK response was accepted")
	}
	if err := tracer.Close(); err != nil {
		t.Fatal(err)
	}
	content, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	text := string(content)
	if strings.Count(text, "GTK_CLEAR") != 1 || !strings.Contains(text, `TRACK1="TRACK-ONE" TRACK2="5428206097984097=1122336655" TRACK3=""`) {
		t.Fatalf("unexpected GTK clear trace: %q", text)
	}
}

func TestReadGOXRequestUsesGCXContextAndBuildsExactPayload(t *testing.T) {
	gcx := &model.GCXResponse{CardType: command.GCXCardICC, AidTableInfo: "080301"}
	var request command.GOXRequest
	var err error
	output := captureOutput(t, func() {
		request, err = readGOXRequest(bufio.NewReader(strings.NewReader("\n3\n07\n")), gcx, "000000010000")
	})
	if err != nil {
		t.Fatal(err)
	}
	if request.AcquirerReference != "08" || request.PinMethod != "3" || request.KeyIndex != 7 || request.Amount != "000000010000" {
		t.Fatalf("GOX request = %#v", request)
	}
	payload, err := command.BuildGOXCommand(request)
	if err != nil {
		t.Fatal(err)
	}
	want := []byte{
		'G', 'O', 'X', '0', '3', '3',
		0x00, 0x13, 0x00, 0x0C, '0', '0', '0', '0', '0', '0', '0', '1', '0', '0', '0', '0',
		0x00, 0x02, 0x00, 0x01, '3',
		0x00, 0x09, 0x00, 0x02, '0', '7',
		0x00, 0x10, 0x00, 0x02, '0', '8',
	}
	if !bytes.Equal(payload, want) {
		t.Fatalf("GOX payload = %X, want %X", payload, want)
	}
	if !strings.Contains(output, "Redes credenciadoras disponíveis: 08") {
		t.Fatalf("GOX prompt = %q", output)
	}
}

func TestReadGOXRequestMatchesSuccessfulPhysicalConfig(t *testing.T) {
	gcx := &model.GCXResponse{CardType: command.GCXCardICC, AidTableInfo: "040501"}
	var request command.GOXRequest
	var err error
	captureOutput(t, func() {
		request, err = readGOXRequest(
			bufio.NewReader(strings.NewReader("04\n3\n02\n")),
			gcx,
			"000000010000",
		)
	})
	if err != nil {
		t.Fatal(err)
	}
	payload, err := command.BuildGOXCommand(request)
	if err != nil {
		t.Fatal(err)
	}
	want := []byte{
		'G', 'O', 'X', '0', '3', '3',
		0x00, 0x13, 0x00, 0x0C, '0', '0', '0', '0', '0', '0', '0', '1', '0', '0', '0', '0',
		0x00, 0x02, 0x00, 0x01, '3',
		0x00, 0x09, 0x00, 0x02, '0', '2',
		0x00, 0x10, 0x00, 0x02, '0', '4',
	}
	if !bytes.Equal(payload, want) {
		t.Fatalf("GOX físico = %X, want %X", payload, want)
	}
}

func TestReadGOXRequestValidatesAcquirerPINMethodAndWorkingKey(t *testing.T) {
	gcx := &model.GCXResponse{CardType: command.GCXCardContactlessEMV, AidTableInfo: "080301020503080402"}
	request, err := readGOXRequest(
		bufio.NewReader(strings.NewReader("02\n1\n09\n00112233445566778899AABBCCDDEEFF\n")),
		gcx,
		"000000000100",
	)
	if err != nil {
		t.Fatal(err)
	}
	if request.AcquirerReference != "02" || request.PinMethod != "1" || request.KeyIndex != 9 || len(request.WorkingKey) != 16 {
		t.Fatalf("GOX MK/WK request = %#v", request)
	}

	tests := []struct {
		name   string
		gcx    *model.GCXResponse
		amount string
		input  string
	}{
		{name: "sem GCX", amount: "000000000100"},
		{name: "cartão magnético", gcx: &model.GCXResponse{CardType: command.GCXCardMagnetic, AidTableInfo: "080301"}, amount: "000000000100"},
		{name: "valor ausente", gcx: gcx},
		{name: "AID inválido", gcx: &model.GCXResponse{CardType: command.GCXCardICC, AidTableInfo: "08030"}, amount: "000000000100"},
		{name: "rede fora da lista", gcx: gcx, amount: "000000000100", input: "99\n"},
		{name: "método inválido", gcx: gcx, amount: "000000000100", input: "\n4\n"},
		{name: "índice inválido", gcx: gcx, amount: "000000000100", input: "\n3\n100\n"},
		{name: "WKENC curta", gcx: gcx, amount: "000000000100", input: "\n1\n07\n1234\n"},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			if _, err := readGOXRequest(bufio.NewReader(strings.NewReader(test.input)), test.gcx, test.amount); err == nil {
				t.Fatal("entrada GOX inválida foi aceita")
			}
		})
	}
}

func TestGOXAcquirerReferences(t *testing.T) {
	got, err := goxAcquirerReferences("080301020503080402")
	if err != nil || strings.Join(got, ",") != "08,02" {
		t.Fatalf("goxAcquirerReferences = %v, %v", got, err)
	}
	for _, invalid := range []string{"", "08030", "08A301"} {
		if _, err := goxAcquirerReferences(invalid); err == nil {
			t.Fatalf("PP_AIDTABINFO inválido aceito: %q", invalid)
		}
	}
}

func TestReadFCXRequestMapsNetworkResultAndBuildsExactPayload(t *testing.T) {
	gox := &model.GOXResponse{Result: []byte("201000")}
	tests := []struct {
		name     string
		input    string
		options  string
		arc      string
		wantHex  string
		validate func(*testing.T, command.FCXRequest)
	}{
		{
			name:    "aprovada com campos opcionais",
			input:   "1\nY3\n9108A102DB6D41C67963\n959F26\n30\n",
			options: "0000",
			arc:     "Y3",
			wantHex: "4643583034300005000A9108A102DB6D41C6796300040003959F26001C000259330019000430303030000C00011E",
			validate: func(t *testing.T, request command.FCXRequest) {
				t.Helper()
				if request.Timeout == nil || *request.Timeout != 30 {
					t.Fatalf("timeout FCX = %v", request.Timeout)
				}
			},
		},
		{
			name:    "negada",
			input:   "2\n05\n\n\n\n",
			options: "1000",
			arc:     "05",
			wantHex: "464358303134001C000230350019000431303030",
		},
		{
			name:    "falha de comunicação sem ARC",
			input:   "3\n\n\n\n",
			options: "2000",
			wantHex: "4643583030380019000432303030",
		},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			var request command.FCXRequest
			var err error
			output := captureOutput(t, func() {
				request, err = readFCXRequest(bufio.NewReader(strings.NewReader(test.input)), gox)
			})
			if err != nil {
				t.Fatal(err)
			}
			if request.Options != test.options || request.Authorization != test.arc {
				t.Fatalf("FCX request = %#v", request)
			}
			payload, err := command.BuildFCXCommand(request)
			if err != nil {
				t.Fatal(err)
			}
			if strings.ToUpper(fmt.Sprintf("%X", payload)) != test.wantHex {
				t.Fatalf("FCX payload = %X, want %s", payload, test.wantHex)
			}
			if bytes.Contains(payload, []byte{0x80, 0x56}) {
				t.Fatalf("PP_FCXRES foi serializado como entrada: %X", payload)
			}
			if !strings.Contains(output, "Resultado do GOX conservado: 201000") {
				t.Fatalf("contexto GOX não exibido: %q", output)
			}
			if test.validate != nil {
				test.validate(t, request)
			}
		})
	}
}

func TestReadFCXRequestRejectsInvalidContextAndInputs(t *testing.T) {
	validGOX := &model.GOXResponse{Result: []byte("201000")}
	tests := []struct {
		name  string
		gox   *model.GOXResponse
		input string
	}{
		{name: "sem GOX"},
		{name: "PP_GOXRES inválido", gox: &model.GOXResponse{Result: []byte("300000")}},
		{name: "escolha inválida", gox: validGOX, input: "4\n"},
		{name: "ARC curto", gox: validGOX, input: "1\n0\n"},
		{name: "ARC fora de A2", gox: validGOX, input: "1\né\n"},
		{name: "dados EMV hex inválidos", gox: validGOX, input: "3\n0\n"},
		{name: "tag list hex inválida", gox: validGOX, input: "3\n\n0\n"},
		{name: "timeout zero", gox: validGOX, input: "3\n\n\n0\n"},
		{name: "timeout acima de B1", gox: validGOX, input: "3\n\n\n256\n"},
		{name: "timeout não numérico", gox: validGOX, input: "3\n\n\nabc\n"},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			captureOutput(t, func() {
				if _, err := readFCXRequest(bufio.NewReader(strings.NewReader(test.input)), test.gox); err == nil {
					t.Fatal("entrada FCX inválida foi aceita")
				}
			})
		})
	}
}

func TestRunMenuRejectsFCXWithoutSuccessfulGOXBeforeService(t *testing.T) {
	svc := service.New(model.DefaultConfig(), nil)
	defer svc.Shutdown()
	output := captureOutput(t, func() {
		input := bytes.NewBufferString("21\n0\n")
		if err := runMenu(bufio.NewReader(input), svc, model.DefaultConfig(), slog.New(slog.NewTextHandler(io.Discard, nil)), logging.NewTracer()); err != nil {
			t.Fatal(err)
		}
	})
	if !strings.Contains(output, "[ERRO] Opcoes FCX: execute antes um GOX válido") {
		t.Fatalf("ausência de GOX não foi informada: %q", output)
	}
	if strings.Contains(output, "FinalizeEMV") {
		t.Fatalf("FCX chegou ao serviço sem GOX válido: %q", output)
	}
}

func TestCLIPrintsMenuDeviceInfoAndResults(t *testing.T) {
	logger := slog.New(slog.NewTextHandler(io.Discard, nil))
	output := captureOutput(t, func() {
		printMenu(model.DefaultConfig())
		printDeviceInfo(&model.DeviceInfo{SerialNumber: "serial", Model: "model"})
		if !handleErr(logger, "GIX", nil) || handleErr(logger, "GIX", io.ErrUnexpectedEOF) {
			t.Fatal("handleErr return values")
		}
	})
	for _, expected := range []string{
		"Menu de teste local", "numeroSerie:: serial", "modelo:: model", "[OK] GIX", "[ERRO] GIX",
		"Abrir sessao segura (OPN RSA/AES)", "(MLI/MLR/MLE)", "(TLI/TLR/TLE)", "(GTK)",
		"(GOX)", "(FCX)", "(GPN, redigido)", "QR Code", "GCX completa",
	} {
		if !strings.Contains(output, expected) {
			t.Fatalf("output does not contain %q: %s", expected, output)
		}
	}
}

func TestRunMenuExitsOnZero(t *testing.T) {
	service := service.New(model.DefaultConfig(), nil)
	defer service.Shutdown()
	output := captureOutput(t, func() {
		if err := runMenu(bufio.NewReader(bytes.NewBufferString("0\n")), service, model.DefaultConfig(), slog.New(slog.NewTextHandler(io.Discard, nil)), logging.NewTracer()); err != nil {
			t.Fatal(err)
		}
	})
	if !strings.Contains(output, "Encerrando...") {
		t.Fatalf("runMenu output = %q", output)
	}
}

func TestRunMenuHandlesInvalidChoiceAndProgress(t *testing.T) {
	service := service.New(model.DefaultConfig(), nil)
	defer service.Shutdown()
	output := captureOutput(t, func() {
		if err := runMenu(bufio.NewReader(bytes.NewBufferString("invalid\n0\n")), service, model.DefaultConfig(), slog.New(slog.NewTextHandler(io.Discard, nil)), logging.NewTracer()); err != nil {
			t.Fatal(err)
		}
		if err := printProgress(nil, 2, 4); err != nil {
			t.Fatal(err)
		}
	})
	if !strings.Contains(output, "Opcao invalida.") || !strings.Contains(output, "Progresso: 2/4") {
		t.Fatalf("unexpected CLI output: %q", output)
	}
}

func TestRunMenuRejectsInvalidGCXOptionsBeforePurchase(t *testing.T) {
	svc := service.New(model.DefaultConfig(), nil)
	defer svc.Shutdown()
	output := captureOutput(t, func() {
		input := bytes.NewBufferString("11\n3\n0\n")
		if err := runMenu(bufio.NewReader(input), svc, model.DefaultConfig(), slog.New(slog.NewTextHandler(io.Discard, nil)), logging.NewTracer()); err != nil {
			t.Fatal(err)
		}
	})
	if !strings.Contains(output, "[ERRO] Opcoes GCX: modo de leitura invalido") {
		t.Fatalf("invalid GCX choice was not reported: %q", output)
	}
	if strings.Contains(output, "PurchaseGCX") {
		t.Fatalf("GCX purchase started after invalid choice: %q", output)
	}
}

func TestRunMenuRejectsGOXWithoutEligibleGCXBeforeService(t *testing.T) {
	svc := service.New(model.DefaultConfig(), nil)
	defer svc.Shutdown()
	output := captureOutput(t, func() {
		input := bytes.NewBufferString("20\n0\n")
		if err := runMenu(bufio.NewReader(input), svc, model.DefaultConfig(), slog.New(slog.NewTextHandler(io.Discard, nil)), logging.NewTracer()); err != nil {
			t.Fatal(err)
		}
	})
	if !strings.Contains(output, "[ERRO] Opcoes GOX: execute antes um GCX") {
		t.Fatalf("ausência de GCX não foi informada: %q", output)
	}
	if strings.Contains(output, "ContinueEMV") {
		t.Fatalf("GOX chegou ao serviço sem GCX elegível: %q", output)
	}
}
