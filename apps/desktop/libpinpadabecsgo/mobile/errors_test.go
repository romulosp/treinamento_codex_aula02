package mobile

import (
	"context"
	"errors"
	"io"
	"testing"

	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/bridgeprotocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/transport/emulator"
)

func TestPublicFailureCodesDoNotEchoCauses(t *testing.T) {
	for _, tc := range []struct {
		name, code string
		err        error
	}{
		{"cancel", "CANCELED", context.Canceled}, {"deadline", "TIMEOUT", context.DeadlineExceeded},
		{"unreachable", "BRIDGE_UNREACHABLE", emulator.ErrUnreachable}, {"busy", "BUSY", emulator.ErrBusy},
		{"disconnected", "DISCONNECTED", io.EOF}, {"protocol", "PROTOCOL_ERROR", bridgeprotocol.ErrInvalidFrame},
		{"closed", "PINPAD_CLOSED", domainerror.ErrPinpadClosed}, {"status", "PINPAD_ERROR", &domainerror.StatusError{Code: "001"}},
		{"remote", "SERIAL_UNAVAILABLE", &emulator.RemoteError{Code: "SERIAL_UNAVAILABLE", Phase: "serial_open"}},
		{"unknown", "BINDING_ERROR", errors.New("PRIVATE_SYNTHETIC")},
	} {
		t.Run(tc.name, func(t *testing.T) {
			err := publicFailure("command", tc.err)
			if ErrorCode(err.Error()) != tc.code || !errors.Is(err, tc.err) {
				t.Fatalf("%v", err)
			}
			if publicFailure("binding", err) != err {
				t.Fatal("double wrapping")
			}
		})
	}
	if ErrorCode("SECRET:whatever") != "BINDING_ERROR" || ErrorPhase("SECRET:whatever") != "binding" || ErrorPhase("TIMEOUT:preflight") != "preflight" {
		t.Fatal("unknown text accepted")
	}
	if publicFailure("command", nil) != nil {
		t.Fatal("nil became error")
	}
}

func TestUnknownRemoteErrorIsSanitizedAtPublicBoundary(t *testing.T) {
	cause := &emulator.RemoteError{Code: "PRIVATE_SYNTHETIC", Phase: "PRIVATE_PATH"}
	err := publicFailure("command", cause)
	if err.Error() != "BINDING_ERROR:binding" || !errors.Is(err, cause) {
		t.Fatalf("unsafe remote error: %v", err)
	}
}
