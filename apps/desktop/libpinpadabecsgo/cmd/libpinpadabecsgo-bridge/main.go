// Command libpinpadabecsgo-bridge inicia o Transport Bridge local.
package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"os/signal"
	"path/filepath"
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
	bridgePortEnv := os.Getenv("PINPAD_BRIDGE_PORT")
	portNumber := 39100
	if bridgePortEnv != "" {
		value, err := strconv.Atoi(bridgePortEnv)
		if err != nil {
			log.Fatal("invalid PINPAD_BRIDGE_PORT")
		}
		portNumber = value
	}
	serialConfig, err := config.Load()
	if err != nil {
		log.Fatal(err)
	}
	tracer := logging.NewTracer()
	destination, err := configureTracer(tracer)
	if err != nil {
		log.Fatal(err)
	}
	defer tracer.Close()
	log.Printf("rastro serial configurado: %s", destination)

	var serialPort port.Transport
	if os.Getenv("PINPAD_BRIDGE_TRANSPORT") == "scripted" {
		scripted := serial.NewDiagnosticScriptedTransport()
		scripted.SetTracer(tracer)
		serialPort = scripted
	} else {
		physical := serial.New(serialConfig.Port, serialConfig.BaudRate, serialConfig.Timeout)
		physical.SetTracer(tracer)
		serialPort = physical
	}
	server, err := bridge.New(bridge.Config{
		Port:       portNumber,
		SerialName: serialConfig.Port,
		Transport:  serialPort,
		Ownership:  ownership.New(),
	})
	if err != nil {
		log.Fatal(err)
	}
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()
	if err := server.Serve(ctx); err != nil {
		log.Fatal(err)
	}
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
