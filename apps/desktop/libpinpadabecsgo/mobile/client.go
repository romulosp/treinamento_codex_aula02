// Package mobile expõe uma fachada pequena e estável para gomobile.
// Tipos internos do core e context.Context permanecem privados ao Go.
package mobile

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"sync"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/service"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/transport/emulator"
)

// ErrBinding identifica uma falha recuperada na fronteira Go/Java/Kotlin.
var ErrBinding = errors.New("binding error")

var (
	buildVersion  = "dev"
	buildRevision = "unknown"
	buildDirty    = "unknown"
)

// Client é a fachada mínima usada pelo aplicativo Android futuro.
type Client struct {
	mu            sync.Mutex
	operationGate chan struct{}
	closing       bool
	transport     *emulator.Transport
	service       *service.Service
	operations    map[string]context.CancelFunc
	timeout       time.Duration
}

// Version devolve a versão lógica e a proveniência do artefato bindado.
// O valor pode ser preenchido no build por -ldflags; nenhum dado operacional
// do pinpad é incluído nessa resposta.
func Version() string {
	return fmt.Sprintf("version=%s revision=%s dirty=%s", buildVersion, buildRevision, buildDirty)
}

// NewClient cria uma fachada com host, porta e timeout simples para binding.
func NewClient(host string, port int, timeoutMillis int64) (client *Client, err error) {
	defer recoverBinding(&err)
	if timeoutMillis <= 0 || timeoutMillis > 120000 {
		return nil, fmt.Errorf("timeout must be positive")
	}
	transport, err := emulator.New(emulator.Config{Host: host, Port: port, Timeout: time.Duration(timeoutMillis) * time.Millisecond})
	if err != nil {
		return nil, err
	}
	client = &Client{transport: transport, operations: make(map[string]context.CancelFunc), timeout: time.Duration(timeoutMillis) * time.Millisecond}
	cfg := model.DefaultConfig()
	cfg.Timeout = client.timeout
	client.service = service.New(cfg, transport)
	client.service.SetQRCodeGenerator(mobileQRCodeGenerator{})
	return client, nil
}

// Open abre o transporte e a sessão ABECS associada ao operationID.
func (c *Client) Open(operationID string) (err error) {
	defer recoverBinding(&err)
	return c.run(operationID, func(ctx context.Context) error {
		if !c.transport.IsOpen() {
			if c.service.GetState() != model.StateClosed {
				_ = c.service.Close(ctx)
			}
			if err := c.transport.Ping(ctx, operationID); err != nil {
				return publicFailure("preflight", err)
			}
		}
		if err := c.service.Open(ctx); err != nil {
			return publicFailure("open", err)
		}
		return nil
	})
}

// Close cancela operações, fecha o serviço e libera a sessão do Bridge.
func (c *Client) Close(operationID string) (err error) {
	defer recoverBinding(&err)
	if c == nil {
		return publicFailure("command", ErrBinding)
	}
	ctx, cancel := context.WithTimeout(context.Background(), c.operationTimeout())
	defer cancel()
	c.mu.Lock()
	if c.closing {
		c.mu.Unlock()
		return publicFailure("command", emulator.ErrBusy)
	}
	c.closing = true
	gate := c.gateLocked()
	for _, stop := range c.operations {
		stop()
	}
	c.mu.Unlock()
	defer func() { c.mu.Lock(); c.closing = false; c.mu.Unlock() }()
	select {
	case gate <- struct{}{}:
		defer func() { <-gate }()
	case <-ctx.Done():
		return publicFailure("command", errors.Join(ctx.Err(), c.transport.Close()))
	}
	var serviceErr error
	if c.service != nil {
		serviceErr = c.service.Close(ctx)
	}
	return publicFailure("command", errors.Join(serviceErr, c.transport.Close()))
}

// Ping verifica a sessão com o Bridge sem enviar um comando ABECS.
func (c *Client) Ping(operationID string) (err error) {
	defer recoverBinding(&err)
	return c.run(operationID, func(ctx context.Context) error {
		return c.transport.Ping(ctx, operationID)
	})
}

// GetInfoJSON consulta GIX e devolve somente campos tipados e não sensíveis.
// O JSON é uma representação de saída do resultado Go; ele não é protocolo de
// transporte nem entrada para execução de comandos.
func (c *Client) GetInfoJSON(operationID string) (result string, err error) {
	defer recoverBinding(&err)
	result = "{}"
	err = c.run(operationID, func(ctx context.Context) error {
		info, callErr := c.service.GetInfo(ctx)
		if callErr != nil {
			return callErr
		}
		if info == nil {
			return errors.New("device info is nil")
		}
		encoded, marshalErr := json.Marshal(struct {
			SchemaVersion           string `json:"schemaVersion"`
			SerialNumber            string `json:"serialNumber,omitempty"`
			PartNumber              string `json:"partNumber,omitempty"`
			Model                   string `json:"model,omitempty"`
			Manufacturer            string `json:"manufacturer,omitempty"`
			Capabilities            string `json:"capabilities,omitempty"`
			OSVersion               string `json:"osVersion,omitempty"`
			Specification           string `json:"specification,omitempty"`
			ManufacturerVersion     string `json:"manufacturerVersion,omitempty"`
			AbecsVersion            string `json:"abecsVersion,omitempty"`
			ExtendedAbecsVersion    string `json:"extendedAbecsVersion,omitempty"`
			ContactlessCapabilities string `json:"contactlessCapabilities,omitempty"`
			KernelVersion           string `json:"kernelVersion,omitempty"`
			ContactlessVersion      string `json:"contactlessVersion,omitempty"`
			TextRows                int    `json:"textRows"`
			TextCols                int    `json:"textCols"`
			GraphicWidth            int    `json:"graphicWidth"`
			GraphicHeight           int    `json:"graphicHeight"`
			SupportedFormats        string `json:"supportedFormats,omitempty"`
		}{
			SchemaVersion:           "1",
			SerialNumber:            info.SerialNumber,
			PartNumber:              info.PartNumber,
			Model:                   info.Model,
			Manufacturer:            info.Manufacturer,
			Capabilities:            info.Capabilities,
			OSVersion:               info.OSVersion,
			Specification:           info.Specification,
			ManufacturerVersion:     info.ManufacturerVersion,
			AbecsVersion:            info.AbecsVersion,
			ExtendedAbecsVersion:    info.ExtendedAbecsVersion,
			ContactlessCapabilities: info.ContactlessCapabilities,
			KernelVersion:           info.KernelVersion,
			ContactlessVersion:      info.ContactlessVersion,
			TextRows:                info.TextRows,
			TextCols:                info.TextCols,
			GraphicWidth:            info.GraphicWidth,
			GraphicHeight:           info.GraphicHeight,
			SupportedFormats:        info.SupportedFormats,
		})
		if marshalErr != nil {
			return fmt.Errorf("marshal device info: %w", marshalErr)
		}
		result = string(encoded)
		return nil
	})
	return result, err
}

// GetState devolve somente o estado textual suportado pelo binding.
func (c *Client) GetState() (state string) {
	defer recoverState(&state)
	if c == nil || c.service == nil {
		return string(model.StateClosed)
	}
	if !c.transport.IsOpen() {
		return string(model.StateClosed)
	}
	return string(c.service.GetState())
}

// Cancel cancela a operação identificada, sem expor context.Context.
func (c *Client) Cancel(operationID string) (err error) {
	defer recoverBinding(&err)
	c.mu.Lock()
	cancel := c.operations[operationID]
	c.mu.Unlock()
	if cancel == nil {
		return fmt.Errorf("operation not found")
	}
	cancel()
	return nil
}

func (c *Client) run(operationID string, operation func(context.Context) error) error {
	if c == nil || c.service == nil {
		return fmt.Errorf("client is nil")
	}
	if operationID == "" {
		return fmt.Errorf("operation id is required")
	}
	ctx, cancel := context.WithTimeout(context.Background(), c.operationTimeout())
	defer cancel()
	c.mu.Lock()
	if c.closing {
		c.mu.Unlock()
		return publicFailure("command", emulator.ErrBusy)
	}
	if _, exists := c.operations[operationID]; exists {
		c.mu.Unlock()
		return publicFailure("command", emulator.ErrBusy)
	}
	c.operations[operationID] = cancel
	gate := c.gateLocked()
	c.mu.Unlock()
	defer func() { c.mu.Lock(); delete(c.operations, operationID); c.mu.Unlock() }()
	// O orçamento inclui a fila. Cancel/Close alcançam também chamadas pendentes.
	select {
	case gate <- struct{}{}:
		defer func() { <-gate }()
	case <-ctx.Done():
		return publicFailure("command", ctx.Err())
	}
	if err := ctx.Err(); err != nil {
		return publicFailure("command", err)
	}
	c.transport.SetCorrelationID(operationID)
	c.transport.SetOperationContext(ctx)
	defer func() {
		cancel()
		c.transport.SetCorrelationID("")
		c.transport.SetOperationContext(nil)
	}()
	return publicFailure("command", operation(ctx))
}

// gateLocked inicializa a admissão serializada sob mu; não bloqueia o cancelamento.
func (c *Client) gateLocked() chan struct{} {
	if c.operationGate == nil {
		c.operationGate = make(chan struct{}, 1)
	}
	return c.operationGate
}

func (c *Client) operationTimeout() time.Duration {
	if c.timeout > 0 {
		return c.timeout
	}
	return 30 * time.Second
}

func recoverBinding(target *error) {
	if recovered := recover(); recovered != nil {
		*target = publicFailure("binding", ErrBinding)
	}
}

func recoverState(target *string) {
	if recovered := recover(); recovered != nil {
		*target = "BINDING_ERROR"
	}
}
