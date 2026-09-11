package config

import (
	"os"
	"testing"
)

func TestLoadUsesDefaultAndEnvironmentPrecedence(t *testing.T) {
	previous, existed := os.LookupEnv("PORTA_PINPAD")
	t.Cleanup(func() {
		if existed {
			_ = os.Setenv("PORTA_PINPAD", previous)
			return
		}
		_ = os.Unsetenv("PORTA_PINPAD")
	})
	_ = os.Unsetenv("PORTA_PINPAD")
	cfg, err := Load()
	if err != nil || cfg.Port != "COM7" {
		t.Fatalf("default config = %#v, %v", cfg, err)
	}
	t.Setenv("PORTA_PINPAD", "")
	if _, err := Load(); err == nil {
		t.Fatal("expected empty PORTA_PINPAD error")
	}
	t.Setenv("PORTA_PINPAD", "COM9")
	cfg, err = Load()
	if err != nil || cfg.Port != "COM9" {
		t.Fatalf("environment config = %#v, %v", cfg, err)
	}
}

func TestLoadRejectsInvalidNumericValues(t *testing.T) {
	t.Setenv("PORTA_PINPAD", "COM8")
	t.Setenv("PINPAD_BAUDRATE", "invalid")
	if _, err := Load(); err == nil {
		t.Fatal("expected invalid baud rate")
	}
	t.Setenv("PINPAD_BAUDRATE", "9600")
	t.Setenv("PINPAD_TIMEOUT", "0")
	if _, err := Load(); err == nil {
		t.Fatal("expected invalid timeout")
	}
}
