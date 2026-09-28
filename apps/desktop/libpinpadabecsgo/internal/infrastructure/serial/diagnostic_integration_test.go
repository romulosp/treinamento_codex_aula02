//go:build integration

package serial_test

import (
	"context"
	"testing"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/service"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/serial"
)

func TestDiagnosticTranscriptCompletesCoreLifecycle(t *testing.T) {
	transport := serial.NewDiagnosticScriptedTransport()
	core := service.New(model.DefaultConfig(), transport)
	defer core.Shutdown()

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := core.Open(ctx); err != nil {
		t.Fatal(err)
	}
	if _, err := core.GetInfo(ctx); err != nil {
		t.Fatal(err)
	}
	if err := core.Close(ctx); err != nil {
		t.Fatal(err)
	}
	if !transport.Completed() {
		t.Fatal("core did not consume the complete diagnostic transcript")
	}
}
