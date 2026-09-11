package main

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/service"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"bufio"
	"bytes"
	"io"
	"log/slog"
	"os"
	"path/filepath"
	"strings"
	"testing"
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

func TestConfigureTracerUsesExplicitEnvironmentDestination(t *testing.T) {
	tracer := logging.NewTracer()
	defer func() {
		if err := tracer.Close(); err != nil {
			t.Fatal(err)
		}
	}()
	t.Setenv("PINPAD_LOG_FILE", "")
	if destination, err := configureTracer(tracer); err != nil || destination != "" {
		t.Fatalf("empty destination=%q err=%v", destination, err)
	}

	path := filepath.Join(t.TempDir(), "LogPinpadAbecs.txt")
	t.Setenv("PINPAD_LOG_FILE", path)
	if destination, err := configureTracer(tracer); err != nil || destination != path {
		t.Fatalf("configured destination=%q err=%v", destination, err)
	}
	tracer.RecordOpen("COM7", 19200)
	if _, err := os.Stat(path); err != nil {
		t.Fatalf("trace file was not created: %v", err)
	}
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
		runMenu(bufio.NewReader(bytes.NewBufferString("0\n")), service, model.DefaultConfig(), slog.New(slog.NewTextHandler(io.Discard, nil)))
	})
	if !strings.Contains(output, "Encerrando...") {
		t.Fatalf("runMenu output = %q", output)
	}
}
