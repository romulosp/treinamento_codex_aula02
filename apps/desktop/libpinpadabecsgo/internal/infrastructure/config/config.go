package config

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"fmt"
	"os"
	"strconv"
	"time"
)

// Load aplica as variáveis de ambiente à configuração padrão do pinpad.
func Load() (model.PinpadConfig, error) {
	cfg := model.DefaultConfig()
	if value, ok := os.LookupEnv("PORTA_PINPAD"); ok {
		if value == "" {
			return cfg, fmt.Errorf("invalid PORTA_PINPAD")
		}
		cfg.Port = value
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
