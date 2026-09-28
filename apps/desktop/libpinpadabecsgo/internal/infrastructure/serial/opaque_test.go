package serial

import (
	"context"
	"errors"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
)

func TestOpaqueTraceRedactsFragmentedDataAndDriverErrors(t *testing.T) {
	for _, failure := range []bool{false, true} {
		t.Run(map[bool]string{false: "chunks", true: "driver_failure"}[failure], func(t *testing.T) {
			path := filepath.Join(t.TempDir(), "trace.txt")
			tracer := logging.NewTracer()
			if _, err := tracer.SetLogDestination(path); err != nil {
				t.Fatal(err)
			}
			defer tracer.Close()
			secret := "SYNTHETIC_PRIVATE_DRIVER_TEXT"
			driverErr := errors.New(secret)
			p := &testPort{reads: [][]byte{[]byte(secret)}}
			if failure {
				p.readErr = driverErr
				p.writeErr = driverErr
				p.closeErr = driverErr
			}
			a := New("COM14", 19200, time.Second)
			a.SetTracer(tracer)
			a.SetOpaqueTrace(true)
			a.port = p
			if err := tracer.RecordOpen("COM14", 19200); err != nil {
				t.Fatal(err)
			}
			writeErr := a.Write([]byte(secret))
			_, readErr := a.Read(context.Background())
			closeErr := a.Close()
			if failure {
				for _, err := range []error{writeErr, readErr, closeErr} {
					if !errors.Is(err, driverErr) {
						t.Fatalf("cause lost: %v", err)
					}
				}
			} else if errors.Join(writeErr, readErr, closeErr) != nil {
				t.Fatal("unexpected I/O failure")
			}
			data, err := os.ReadFile(path)
			if err != nil {
				t.Fatal(err)
			}
			if strings.Contains(string(data), secret) {
				t.Fatal("opaque trace exposed driver text")
			}
			if !failure && strings.Count(string(data), "**REDACTED(") != 2 {
				t.Fatalf("missing redaction: %s", data)
			}
		})
	}
}
