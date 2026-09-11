package command

import "fmt"

// BuildPayload concatena o identificador de comando e parâmetros já validados.
func BuildPayload(command Type, parameters string) ([]byte, error) {
	if command == "" {
		return nil, fmt.Errorf("command type is required")
	}
	return append([]byte(string(command)), []byte(parameters)...), nil
}

// AdvancedContract identifica comandos reservados sem regra transacional própria.
type AdvancedContract struct {
	Type    Type
	Payload []byte
}
