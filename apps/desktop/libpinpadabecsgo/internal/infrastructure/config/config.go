package config

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"fmt"
	"os"
	"strconv"
	"time"
)

func Load() (model.PinpadConfig, error) {
	cfg := model.DefaultConfig()
	if value, ok := os.LookupEnv("PORTA_PINPAD"); ok {
		cfg.Port = value
	}
	if cfg.Port == "" {
		return cfg, fmt.Errorf("PORTA_PINPAD is required")
	}
	if value := os.Getenv("PINPAD_BAUDRATE"); value != "" {
		n, err := strconv.Atoi(value)
		if err != nil || n <= 0 {
			return cfg, fmt.Errorf("invalid PINPAD_BAUDRATE")
		}
		cfg.BaudRate = n
	}
	if value := os.Getenv("PINPAD_TIMEOUT"); value != "" {
		seconds, err := strconv.Atoi(value)
		if err != nil || seconds <= 0 {
			return cfg, fmt.Errorf("invalid PINPAD_TIMEOUT")
		}
		cfg.Timeout = time.Duration(seconds) * time.Second
	}
	return cfg, nil
}
