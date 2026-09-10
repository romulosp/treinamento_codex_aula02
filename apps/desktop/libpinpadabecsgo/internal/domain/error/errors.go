package domainerror

import "errors"

var (
	ErrPortNotConfigured            = errors.New("pinpad port not configured")
	ErrPortUnavailable              = errors.New("pinpad port unavailable")
	ErrPinpadClosed                 = errors.New("pinpad is closed")
	ErrPinpadBusy                   = errors.New("pinpad is busy")
	ErrTimeout                      = errors.New("pinpad operation timed out")
	ErrChecksumInvalid              = errors.New("invalid checksum")
	ErrInvalidResponse              = errors.New("invalid pinpad response")
	ErrNakReceived                  = errors.New("negative acknowledgement received")
	ErrSessionAlreadyOwned          = errors.New("pinpad session already owned")
	ErrNotSessionOwner              = errors.New("session is not pinpad owner")
	ErrQueueFull                    = errors.New("pinpad command queue is full")
	ErrQRCodeGeneratorNotConfigured = errors.New("QR code generator is not configured")
	ErrNotImplemented               = errors.New("operation is not implemented")
)
