package command

import "fmt"

const maxCommandDataSize = 2044

// Parameter representa um parâmetro TLV simplificado de um comando ABECS.
// Value é copiado pelo builder e pode conter dados binários.
type Parameter struct {
	ID    SPEParameter
	Value []byte
}

// BuildABECSPayload monta um comando ABECS com um ou mais blocos de até 999
// bytes. O resultado ainda não contém framing serial, que é responsabilidade
// da camada de protocolo.
func BuildABECSPayload(kind Type, parameters []Parameter) ([]byte, error) {
	if kind == "" {
		return nil, fmt.Errorf("command type is required")
	}
	if len(parameters) == 0 {
		return []byte(string(kind) + "000"), nil
	}
	blocks := make([][]byte, 0, 1)
	block := make([]byte, 0, 999)
	for _, parameter := range parameters {
		if len(parameter.Value) > 995 {
			return nil, fmt.Errorf("parameter %04X exceeds 995 bytes", uint16(parameter.ID))
		}
		encoded := make([]byte, 4+len(parameter.Value))
		encoded[0] = byte(parameter.ID >> 8)
		encoded[1] = byte(parameter.ID)
		encoded[2] = byte(len(parameter.Value) >> 8)
		encoded[3] = byte(len(parameter.Value))
		copy(encoded[4:], parameter.Value)
		if len(block) > 0 && len(block)+len(encoded) > 999 {
			blocks = append(blocks, block)
			block = make([]byte, 0, 999)
		}
		block = append(block, encoded...)
	}
	if len(block) > 0 {
		blocks = append(blocks, block)
	}
	payload := make([]byte, 0, len(kind)+len(blocks)*3)
	payload = append(payload, string(kind)...)
	for _, current := range blocks {
		payload = append(payload, fmt.Sprintf("%03d", len(current))...)
		payload = append(payload, current...)
	}
	if len(payload) > maxCommandDataSize {
		return nil, fmt.Errorf("ABECS command data exceeds %d bytes", maxCommandDataSize)
	}
	return payload, nil
}
