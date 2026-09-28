// Command libpinpadabecsgo-bridge inicia o Transport Bridge local.
package main

import (
	"context"
	"errors"
	"fmt"
	"log"
	"os"
	"os/signal"
	"path/filepath"
	"regexp"
	"runtime"
	"strconv"
	"strings"
	"syscall"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/port"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridge"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/config"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/ownership"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/serial"
)

func main() {
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()
	if err := run(ctx); err != nil {
		log.Printf("bridge encerrado com erro: %v", err)
		os.Exit(1)
	}
}

// run prepara o log antes da configuração e encerra recursos antes de devolver
// erro ao main. Os defers não são interrompidos por log.Fatal.
func run(ctx context.Context) (result error) {
	tracer := logging.NewTracer()
	destination, err := configureTracer(tracer)
	if err != nil {
		return err
	}
	defer func() { result = errors.Join(result, tracer.Close()) }()
	event := logging.BridgeEvent{Event: "bridge_starting", Phase: "startup", Code: "OK", PID: os.Getpid()}
	defer func() {
		if result != nil {
			event.Event, event.Code = "bridge_start_failed", "STARTUP_ERROR"
		} else {
			event.Event, event.Code = "bridge_stopped", "OK"
		}
		result = errors.Join(result, tracer.RecordBridgeEvent(event))
	}()
	if err := tracer.RecordBridgeEvent(event); err != nil {
		return err
	}
	bridgePortEnv := os.Getenv("PINPAD_BRIDGE_PORT")
	portNumber := 39100
	if bridgePortEnv != "" {
		value, err := strconv.Atoi(bridgePortEnv)
		if err != nil {
			return fmt.Errorf("invalid PINPAD_BRIDGE_PORT")
		}
		portNumber = value
	}
	serialConfig, err := config.Load()
	if err != nil {
		return err
	}
	mode := strings.TrimSpace(os.Getenv("PINPAD_BRIDGE_TRANSPORT"))
	if mode == "" {
		mode = "physical"
	}
	if mode != "physical" && mode != "scripted" {
		return fmt.Errorf("invalid PINPAD_BRIDGE_TRANSPORT")
	}
	if mode == "physical" && runtime.GOOS == "windows" && !regexp.MustCompile(`(?i)^COM[1-9][0-9]{0,3}$`).MatchString(serialConfig.Port) {
		return fmt.Errorf("invalid PORTA_PINPAD")
	}
	event.Port, event.Mode = serialConfig.Port, mode
	event.Endpoint = fmt.Sprintf("127.0.0.1:%d", portNumber)
	event.BaudRate, event.TimeoutMillis = serialConfig.BaudRate, serialConfig.Timeout.Milliseconds()
	event.Source = "PORTA_PINPAD=" + source("PORTA_PINPAD") + ";PINPAD_BRIDGE_PORT=" + source("PINPAD_BRIDGE_PORT")
	if err := tracer.RecordBridgeEvent(event); err != nil {
		return err
	}
	log.Printf("configuracao: COM=%s origem=%s baud=%d origem=%s timeout=%s origem=%s TCP=%d origem=%s modo=%s log=%s origem=%s", serialConfig.Port, source("PORTA_PINPAD"), serialConfig.BaudRate, source("PINPAD_BAUDRATE"), serialConfig.Timeout, source("PINPAD_TIMEOUT"), portNumber, source("PINPAD_BRIDGE_PORT"), mode, destination, source("PINPAD_LOG_FILE"))

	var serialPort port.Transport
	if mode == "scripted" {
		scripted := serial.NewDiagnosticScriptedTransportForPort(serialConfig.Port)
		scripted.SetTracer(tracer)
		scripted.SetOpaqueTrace(true)
		serialPort = scripted
	} else {
		physical := serial.New(serialConfig.Port, serialConfig.BaudRate, serialConfig.Timeout)
		physical.SetTracer(tracer)
		physical.SetOpaqueTrace(true)
		serialPort = physical
	}
	server, err := bridge.New(bridge.Config{
		Port:       portNumber,
		SerialName: serialConfig.Port,
		Transport:  serialPort,
		Ownership:  ownership.New(),
		Tracer:     tracer,
		Mode:       mode,
		OnListening: func(address string) {
			log.Printf("bridge_listening endpoint=%s pid=%d modo=%s COM=%s log=%s", address, os.Getpid(), mode, serialConfig.Port, destination)
		},
	})
	if err != nil {
		return err
	}
	return server.Serve(ctx)
}

func source(key string) string {
	if value, ok := os.LookupEnv(key); ok && strings.TrimSpace(value) != "" {
		return "environment"
	}
	return "default"
}

const bridgeModulePath = "br.com.romulopenha/lib-pinpad-abecs-go"

func configureTracer(tracer *logging.Tracer) (string, error) {
	return configureTracerFor(tracer, strings.TrimSpace(os.Getenv("PINPAD_LOG_FILE")))
}

func configureTracerFor(tracer *logging.Tracer, configured string) (string, error) {
	destination, err := resolveLogDestination(configured)
	if err != nil {
		return "", err
	}
	if err := os.MkdirAll(filepath.Dir(destination), 0o755); err != nil {
		return "", fmt.Errorf("criar diretorio do rastro serial: %w", err)
	}
	active, err := tracer.SetLogDestination(destination)
	if err != nil {
		return "", fmt.Errorf("configurar PINPAD_LOG_FILE: %w", err)
	}
	if !active {
		return "", fmt.Errorf("PINPAD_LOG_FILE nao ativou o rastro")
	}
	return destination, nil
}

func resolveLogDestination(configured string) (string, error) {
	if configured != "" && filepath.IsAbs(configured) {
		return filepath.Clean(configured), nil
	}
	starts := make([]string, 0, 2)
	if cwd, err := os.Getwd(); err == nil {
		starts = append(starts, cwd)
	}
	if executable, err := os.Executable(); err == nil {
		starts = append(starts, filepath.Dir(executable))
	}
	root, err := findModuleRoot(starts...)
	if err != nil {
		return "", fmt.Errorf("determinar raiz do modulo; defina PINPAD_LOG_FILE absoluto: %w", err)
	}
	if configured == "" {
		configured = filepath.FromSlash("logs/LogPinpadAbecs.txt")
	}
	return filepath.Abs(filepath.Join(root, configured))
}

func findModuleRoot(starts ...string) (string, error) {
	for _, start := range starts {
		current, err := filepath.Abs(start)
		if err != nil {
			continue
		}
		for {
			data, readErr := os.ReadFile(filepath.Join(current, "go.mod"))
			if readErr == nil && strings.Contains(string(data), "module "+bridgeModulePath) {
				return current, nil
			}
			parent := filepath.Dir(current)
			if parent == current {
				break
			}
			current = parent
		}
	}
	return "", fmt.Errorf("modulo %s nao encontrado", bridgeModulePath)
}
