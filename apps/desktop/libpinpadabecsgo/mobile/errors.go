package mobile

import (
	"context"
	"errors"
	"io"
	"net"
	"strings"

	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridgeprotocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/transport/emulator"
)

// operationFailure atravessa gomobile como CODE:phase, preservando causa apenas no Go.
type operationFailure struct {
	code, phase string
	cause       error
}

func (e *operationFailure) Error() string { return e.code + ":" + e.phase }
func (e *operationFailure) Unwrap() error { return e.cause }

func publicFailure(phase string, err error) error {
	if err == nil {
		return nil
	}
	var existing *operationFailure
	if errors.As(err, &existing) {
		return err
	}
	code := "BINDING_ERROR"
	var remote *emulator.RemoteError
	var network net.Error
	var status *domainerror.StatusError
	switch {
	case errors.Is(err, context.Canceled):
		code = "CANCELED"
	case errors.Is(err, context.DeadlineExceeded), errors.Is(err, domainerror.ErrTimeout):
		code = "TIMEOUT"
	case errors.As(err, &network) && network.Timeout():
		code = "TIMEOUT"
	case errors.As(err, &remote):
		code, phase = remote.Code, remote.Phase
	case errors.Is(err, emulator.ErrUnreachable):
		code = "BRIDGE_UNREACHABLE"
	case errors.Is(err, emulator.ErrBusy), errors.Is(err, domainerror.ErrPinpadBusy):
		code = "BUSY"
	case errors.Is(err, emulator.ErrDisconnected), errors.Is(err, io.EOF), errors.Is(err, io.ErrUnexpectedEOF), errors.Is(err, net.ErrClosed):
		code = "DISCONNECTED"
	case errors.Is(err, bridgeprotocol.ErrInvalidFrame), errors.Is(err, domainerror.ErrInvalidResponse), errors.Is(err, domainerror.ErrChecksumInvalid):
		code = "PROTOCOL_ERROR"
	case errors.Is(err, domainerror.ErrPinpadClosed):
		code = "PINPAD_CLOSED"
	case errors.As(err, &status):
		code = "PINPAD_ERROR"
	}
	if code == "INVALID_FRAME" {
		code = "PROTOCOL_ERROR"
	}
	return &operationFailure{code: code, phase: phase, cause: err}
}

// ErrorCode extrai categoria do binding; mensagens desconhecidas não são ecoadas.
func ErrorCode(message string) string {
	code, _, _ := strings.Cut(message, ":")
	switch code {
	case "BRIDGE_UNREACHABLE", "TIMEOUT", "PROTOCOL_ERROR", "BUSY", "OWNERSHIP_ERROR", "SERIAL_UNAVAILABLE", "CANCELED", "DISCONNECTED", "PINPAD_ERROR", "PINPAD_CLOSED":
		return code
	}
	return "BINDING_ERROR"
}

// ErrorPhase devolve fase reconhecida sem incluir parâmetros ou texto livre.
func ErrorPhase(message string) string {
	_, phase, _ := strings.Cut(message, ":")
	switch phase {
	case "preflight", "open", "hello", "acquire", "ownership", "serial_open", "serial_read", "serial_write", "session", "ping", "receive", "forward", "command", "bridge", "binding":
		return phase
	}
	return "binding"
}
