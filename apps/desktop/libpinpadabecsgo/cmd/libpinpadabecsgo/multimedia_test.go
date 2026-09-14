package main

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/service"
	"bufio"
	"context"
	"errors"
	"io"
	"strings"
	"testing"
	"testing/synctest"
	"time"
)

type delayedMediaReader struct{ input io.Reader }

func (r delayedMediaReader) Read(p []byte) (int, error) {
	time.Sleep(61 * time.Second)
	return r.input.Read(p)
}

func TestMultimediaInputDeadlineStartsAfterTyping(t *testing.T) {
	synctest.Test(t, func(t *testing.T) {
		called := false
		reader := bufio.NewReader(delayedMediaReader{strings.NewReader("file.png\nQRCODE01\n")})
		err := loadMultimediaInput(reader, func(ctx context.Context, path, name string, progress service.ProgressFunc) error {
			called = true
			if path != "file.png" || name != "QRCODE01" {
				t.Fatal("entradas divergentes")
			}
			deadline, ok := ctx.Deadline()
			if !ok || time.Until(deadline) != menuTimeout || ctx.Err() != nil {
				t.Fatalf("prazo consumido pela digita??o: %v", ctx.Err())
			}
			return nil
		})
		if err != nil || !called {
			t.Fatalf("carga: %v, chamada=%t", err, called)
		}
	})
}

func TestMultimediaInputRejectsNameAndPropagatesLoadError(t *testing.T) {
	for _, name := range []string{"QRCODE", "TOOLONG99", "BAD/NAME"} {
		t.Run(name, func(t *testing.T) {
			err := loadMultimediaInput(bufio.NewReader(strings.NewReader("file.png\n"+name+"\n")), func(context.Context, string, string, service.ProgressFunc) error {
				t.Fatal("nome inv?lido chegou ? carga")
				return nil
			})
			if err == nil {
				t.Fatal("nome inv?lido aceito")
			}
		})
	}
	expected := errors.New("falha de carga")
	err := loadMultimediaInput(bufio.NewReader(strings.NewReader("file.png\nQRCODE01\n")), func(context.Context, string, string, service.ProgressFunc) error { return expected })
	if !errors.Is(err, expected) {
		t.Fatal(err)
	}
}
