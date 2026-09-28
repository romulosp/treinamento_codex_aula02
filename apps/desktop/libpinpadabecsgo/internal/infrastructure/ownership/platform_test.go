//go:build windows

package ownership

import (
	"context"
	"errors"
	"testing"
)

func TestPlatformRejectsInvalidMutexNameAndCanceledAcquisition(t *testing.T) {
	for _, name := range []string{"invalid\x00name", "invalid\\nested"} {
		if release, err := acquirePlatform(context.Background(), name); err == nil {
			if release != nil {
				_ = release()
			}
			t.Fatal("invalid mutex name accepted")
		}
	}
	ctx, cancel := context.WithCancel(context.Background())
	cancel()
	if _, err := acquirePlatform(ctx, "072-canceled-platform"); !errors.Is(err, context.Canceled) {
		t.Fatalf("cancel: %v", err)
	}
	// Não sobra handle/posse da aquisição cancelada.
	release, err := acquirePlatform(context.Background(), "072-canceled-platform")
	if err != nil {
		t.Fatal(err)
	}
	if err := release(); err != nil {
		t.Fatal(err)
	}
	if err := release(); err != nil {
		t.Fatal(err)
	}
}
