package command

import "fmt"

func BuildPayload(command Type, parameters string) ([]byte, error) {
	if command == "" {
		return nil, fmt.Errorf("command type is required")
	}
	return append([]byte(string(command)), []byte(parameters)...), nil
}

// AdvancedContract identifies commands reserved for later Changes. It intentionally
// carries no command-specific business rules in this Change.
type AdvancedContract struct {
	Type    Type
	Payload []byte
}
