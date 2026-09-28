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
	mu          sync.Mutex
	operationMu sync.Mutex
	transport   *emulator.Transport
	service     *service.Service
	operations  map[string]context.CancelFunc
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
	if timeoutMillis <= 0 {
		return nil, fmt.Errorf("timeout must be positive")
	}
	transport, err := emulator.New(emulator.Config{Host: host, Port: port, Timeout: time.Duration(timeoutMillis) * time.Millisecond})
	if err != nil {
		return nil, err
	}
	client = &Client{transport: transport, operations: make(map[string]context.CancelFunc)}
	client.service = service.New(model.DefaultConfig(), transport)
	client.service.SetQRCodeGenerator(mobileQRCodeGenerator{})
	return client, nil
}

// Open abre o transporte e a sessão ABECS associada ao operationID.
func (c *Client) Open(operationID string) (err error) {
	defer recoverBinding(&err)
	return c.run(operationID, func(ctx context.Context) error { return c.service.Open(ctx) })
}

// Close cancela operações, fecha o serviço e libera a sessão do Bridge.
func (c *Client) Close(operationID string) (err error) {
	defer recoverBinding(&err)
	if c == nil {
		return fmt.Errorf("client is nil")
	}
	c.cancelAll()
	c.operationMu.Lock()
	defer c.operationMu.Unlock()
	if c.service != nil {
		if err = c.service.Close(context.Background()); err != nil {
			return err
		}
	}
	return c.transport.Close()
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
	c.operationMu.Lock()
	defer c.operationMu.Unlock()
	ctx, cancel := context.WithCancel(context.Background())
	c.mu.Lock()
	if _, exists := c.operations[operationID]; exists {
		c.mu.Unlock()
		cancel()
		return fmt.Errorf("operation already exists")
	}
	c.operations[operationID] = cancel
	c.mu.Unlock()
	c.transport.SetCorrelationID(operationID)
	defer func() {
		cancel()
		c.transport.SetCorrelationID("")
		c.mu.Lock()
		delete(c.operations, operationID)
		c.mu.Unlock()
	}()
	return operation(ctx)
}

func (c *Client) cancelAll() {
	c.mu.Lock()
	cancels := make([]context.CancelFunc, 0, len(c.operations))
	for _, cancel := range c.operations {
		cancels = append(cancels, cancel)
	}
	c.mu.Unlock()
	for _, cancel := range cancels {
		cancel()
	}
}

func recoverBinding(target *error) {
	if recovered := recover(); recovered != nil {
		*target = fmt.Errorf("%w: %v", ErrBinding, recovered)
	}
}

func recoverState(target *string) {
	if recovered := recover(); recovered != nil {
		*target = "BINDING_ERROR"
	}
}
