package main

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"net"
	"net/http"
	"os"
	"os/signal"
	"path/filepath"
	"strings"
	"syscall"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/api"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/service"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/config"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/serial"
	qrcode "github.com/skip2/go-qrcode"
)

type qrCodeGenerator struct{}

func (qrCodeGenerator) Generate(data string, size int) ([]byte, error) {
	return qrcode.Encode(data, qrcode.Medium, size)
}

const (
	modulePath          = "br.com.romulopenha/lib-pinpad-abecs-go"
	defaultTraceRelPath = "logs/LogPinpadAbecs.txt"
	defaultBindAddress  = "127.0.0.1:8080"
)

func main() {
	os.Exit(run())
}

func run() int {
	logger := logging.New()
	cfg, err := config.Load()
	if err != nil {
		logger.Error("configuracao invalida", slog.Any("error", err))
		fmt.Fprintln(os.Stderr, err)
		return 1
	}

	tracer := logging.NewTracer()
	destination, err := configureTracer(tracer)
	if err != nil {
		logger.Error("falha ao configurar rastro serial", slog.Any("error", err))
		fmt.Fprintln(os.Stderr, err)
		return 1
	}
	logger.Info("rastro serial configurado", slog.String("arquivo", destination))

	port := serial.New(cfg.Port, cfg.BaudRate, cfg.Timeout)
	port.SetTracer(tracer)
	svc := service.New(cfg, port)
	svc.SetQRCodeGenerator(qrCodeGenerator{})
	svc.SetTracer(tracer)

	bindAddr := getBindAddress()
	handler := api.NewHandler(svc, logger, cfg.Timeout)
	server := &http.Server{
		Addr:              bindAddr,
		Handler:           handler.Routes(),
		ReadHeaderTimeout: 10 * time.Second,
		IdleTimeout:       60 * time.Second,
	}

	serverErr := make(chan error, 1)
	go func() {
		logger.Info("servidor REST iniciado", slog.String("endereco", bindAddr))
		if err := server.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			serverErr <- err
		}
	}()

	stop := make(chan os.Signal, 1)
	signal.Notify(stop, os.Interrupt, syscall.SIGTERM)

	select {
	case err := <-serverErr:
		logger.Error("falha ao iniciar servidor HTTP", slog.Any("error", err))
		fmt.Fprintln(os.Stderr, err)
		svc.Shutdown()
		_ = tracer.Close()
		return 1
	case sig := <-stop:
		logger.Info("sinal de encerramento recebido", slog.String("signal", sig.String()))
	}

	shutdownCtx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := server.Shutdown(shutdownCtx); err != nil {
		logger.Error("falha no shutdown do servidor HTTP", slog.Any("error", err))
	}
	svc.Shutdown()
	traceErr := tracer.Err()
	closeErr := tracer.Close()
	if traceErr != nil || closeErr != nil {
		logger.Error("falha ao encerrar rastro serial", slog.Any("error", errors.Join(traceErr, closeErr)))
	}
	logger.Info("servidor HTTP encerrado com sucesso")
	return 0
}

func getBindAddress() string {
	host := os.Getenv("PINPAD_HTTP_HOST")
	if host == "" {
		host = "127.0.0.1"
	}
	port := os.Getenv("PINPAD_HTTP_PORT")
	if port == "" {
		port = os.Getenv("PORT")
	}
	if port == "" {
		port = "8080"
	}
	return net.JoinHostPort(host, port)
}

func configureTracer(tracer *logging.Tracer) (string, error) {
	destination, err := resolveLogDestination(strings.TrimSpace(os.Getenv("PINPAD_LOG_FILE")))
	if err != nil {
		return "", err
	}
	if err := os.MkdirAll(filepath.Dir(destination), 0o755); err != nil {
		return "", fmt.Errorf("criar diretorio do rastro serial: %w", err)
	}
	active, err := tracer.SetLogDestination(destination)
	if err != nil {
		return "", fmt.Errorf("configurar destino do rastro serial: %w", err)
	}
	if !active {
		return "", fmt.Errorf("PINPAD_LOG_FILE nao ativou o rastro")
	}
	return destination, nil
}

func resolveLogDestination(explicit string) (string, error) {
	if explicit != "" {
		if filepath.IsAbs(explicit) {
			return filepath.Clean(explicit), nil
		}
		abs, err := filepath.Abs(explicit)
		if err != nil {
			return "", fmt.Errorf("obter caminho absoluto de PINPAD_LOG_FILE: %w", err)
		}
		return filepath.Clean(abs), nil
	}
	root, err := locateModuleRoot()
	if err != nil {
		return "", err
	}
	return filepath.Clean(filepath.Join(root, defaultTraceRelPath)), nil
}

func locateModuleRoot() (string, error) {
	wd, err := os.Getwd()
	if err == nil {
		if root, ok := findModuleRoot(wd); ok {
			return root, nil
		}
	}
	exe, err := os.Executable()
	if err == nil {
		if root, ok := findModuleRoot(filepath.Dir(exe)); ok {
			return root, nil
		}
	}
	return "", fmt.Errorf("nao foi possivel determinar a raiz canonica do modulo %s; defina PINPAD_LOG_FILE", modulePath)
}

func findModuleRoot(start string) (string, bool) {
	current := filepath.Clean(start)
	for {
		goMod := filepath.Join(current, "go.mod")
		if data, err := os.ReadFile(goMod); err == nil {
			if strings.Contains(string(data), "module "+modulePath) {
				return current, true
			}
		}
		parent := filepath.Dir(current)
		if parent == current {
			break
		}
		current = parent
	}
	return "", false
}
