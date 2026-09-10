package protocol

import (
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"bytes"
	"context"
	"fmt"
)

type Reader interface{ Read() ([]byte, error) }

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
		chunk, err := reader.Read()
		if err != nil {
			return nil, fmt.Errorf("read ABECS response: %w", err)
		}
		if len(chunk) == 0 {
			return nil, domainerror.ErrTimeout
		}
		packet = append(packet, chunk...)
		if idx := bytes.IndexByte(packet, PP_ETB); idx >= 0 && len(packet)-idx-1 >= 2 {
			return ValidatePacket(packet)
		}
	}
}

