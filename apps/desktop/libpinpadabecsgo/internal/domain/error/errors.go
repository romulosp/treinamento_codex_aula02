package domainerror

import (
	"errors"
	"fmt"
)

// Erros sentinela classificam falhas de configuração, transporte, estado e contrato.
var (
	ErrPortNotConfigured            = errors.New("pinpad port not configured")
	ErrPortUnavailable              = errors.New("pinpad port unavailable")
	ErrPinpadClosed                 = errors.New("pinpad is closed")
	ErrPinpadBusy                   = errors.New("pinpad is busy")
	ErrPinpadDesynchronized         = errors.New("pinpad communication requires reset")
	ErrTimeout                      = errors.New("pinpad operation timed out")
	ErrChecksumInvalid              = errors.New("invalid checksum")
	ErrInvalidResponse              = errors.New("invalid pinpad response")
	ErrInvalidNotification          = errors.New("invalid pinpad notification")
	ErrNakReceived                  = errors.New("negative acknowledgement received")
	ErrSessionAlreadyOwned          = errors.New("pinpad session already owned")
	ErrNotSessionOwner              = errors.New("session is not pinpad owner")
	ErrQueueFull                    = errors.New("pinpad command queue is full")
	ErrQRCodeGeneratorNotConfigured = errors.New("QR code generator is not configured")
	ErrUnsupportedMedia             = errors.New("pinpad incompatible with PNG display")
	ErrNotImplemented               = errors.New("operation is not implemented")
	ErrInvalidCommandSequence       = errors.New("invalid pinpad command sequence")
)

// StatusError representa um RSP_STAT diferente de sucesso devolvido pelo pinpad.
// Code preserva os três dígitos ABECS para decisões específicas do consumidor.
type StatusError struct{ Code string }

// Error fornece uma descrição sanitizada, sem incluir payload ou dados do cartão.
func (e *StatusError) Error() string { return fmt.Sprintf("pinpad returned status %s", e.Code) }
