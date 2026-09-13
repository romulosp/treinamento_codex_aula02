package main

import (
	"bufio"
	"bytes"
	"io"
	"log/slog"
	"os"
	"path/filepath"
	"strings"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/service"
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
		"Abrir sessao segura (OPN RSA/AES)", "(MLI/MLR/MLE)", "(TLI/TLR/TLE)", "(GTK, redigido)",
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
