package protocol

import (
	"context"
	"fmt"
)

// Reader fornece bytes individuais da porta serial e preserva quaisquer bytes
// seguintes para a próxima leitura de protocolo.
type Reader interface {
	NextByte(context.Context) (byte, error)
}

// ReadFullResponse acumula bytes lidos de reader ate reconhecer um pacote
// ABECS completo (SYN + dados escapados + ETB + CRC de 2 bytes) e o valida.
// A acumulacao e necessaria porque uma unica chamada de leitura serial pode
// devolver qualquer quantidade de bytes (inclusive o pacote inteiro ou apenas
// parte dele); nenhum byte recebido pode ser descartado.
func ReadFullResponse(ctx context.Context, reader Reader) ([]byte, error) {
	if ctx == nil {
		ctx = context.Background()
	}
	packet := make([]byte, 0, 256)
	for {
		if err := ctx.Err(); err != nil {
			return nil, err
		}
		value, err := reader.NextByte(ctx)
		if err != nil {
			return nil, fmt.Errorf("read ABECS response: %w", err)
		}
		packet = append(packet, value)
		if value == PP_ETB {
			for range 2 {
				crc, err := reader.NextByte(ctx)
				if err != nil {
					return nil, fmt.Errorf("read ABECS CRC: %w", err)
				}
				packet = append(packet, crc)
			}
			return ValidatePacket(packet)
		}
	}
}
