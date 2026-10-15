package parser

import (
	"fmt"

	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

// PaymentPINKeyIndexes lê exclusivamente PP_DKPTTDESP(8035), não DATA/MK/DES.
// A100 contém uma flag por posição00..99:0ausente,1carregada,2não suportada.
// Devolve índices independentes da resposta, sem valor secreto de chave/KSN.
func PaymentPINKeyIndexes(response *model.Response) ([]int, error) {
	if response == nil || response.StatusCode != "000" {
		return nil, fmt.Errorf("missing PIN key map: %w", domainerror.ErrInvalidResponse)
	}
	flags := response.RawTags[uint16(protocol.TagDukptTDESDescription)]
	if len(flags) != 100 {
		return nil, fmt.Errorf("invalid PIN key map size: %w", domainerror.ErrInvalidResponse)
	}
	indexes := make([]int, 0)
	for index, flag := range flags {
		switch flag {
		case '1':
			indexes = append(indexes, index)
		case '0', '2':
		default:
			return nil, fmt.Errorf("invalid PIN key map flag: %w", domainerror.ErrInvalidResponse)
		}
	}
	return indexes, nil
}
