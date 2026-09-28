package model

import (
	"testing"
	"time"
)

func TestDefaultConfig(t *testing.T) {
	config := DefaultConfig()
	if config.Port != "COM7" || config.BaudRate != 19200 || config.Timeout != 30*time.Second {
		t.Fatalf("unexpected serial defaults: %#v", config)
	}
	if config.AutoLoadEMVTables || config.AcquirerIndex != "00" || config.TableVersion != "TABVER0001" {
		t.Fatalf("unexpected application defaults: %#v", config)
	}
}
