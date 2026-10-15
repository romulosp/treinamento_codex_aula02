package parser

import (
	"bytes"
	"fmt"
	"strings"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
)

// PaymentPAN extrai somente PAN numérico de fontes GCX não mascaradas e coerentes.
// O buffer retornado pertence ao core; deve ser zerado ao consumir ou invalidar a captura.
func PaymentPAN(card *model.GCXResponse) ([]byte, error) {
	if card == nil {
		return nil, fmt.Errorf("missing payment card")
	}
	pan := []byte{}
	accept := func(candidate string) error {
		if len(candidate) < 2 || len(candidate) > 19 || !numeric(candidate) {
			return fmt.Errorf("payment PAN unavailable or invalid")
		}
		if len(pan) != 0 && !bytes.Equal(pan, []byte(candidate)) {
			return fmt.Errorf("conflicting payment PAN sources")
		}
		if len(pan) == 0 {
			pan = append(pan, candidate...)
		}
		return nil
	}
	if card.PAN != "" {
		if err := accept(card.PAN); err != nil {
			return nil, err
		}
	}
	for index, track := range []string{card.Track1, card.Track2, card.Track3} {
		if track == "" {
			continue
		}
		var candidate string
		valid := false
		switch index {
		case 0:
			parts := strings.Split(track, "^")
			if len(track) <= 76 && len(parts) == 3 && strings.HasPrefix(parts[0], "B") {
				candidate, valid = parts[0][1:], len(parts[2]) == 7 && numeric(parts[2])
			}
		case 1:
			before, after, found := strings.Cut(track, "=")
			candidate, valid = before, len(track) <= 37 && found && len(after) == 7 && numeric(after)
		case 2:
			if len(track) >= 2 && len(track) <= 104 && (track[:2] == "01" || track[:2] == "02") {
				before, _, found := strings.Cut(track[2:], "=")
				candidate, valid = before, found
			}
		}
		if !valid {
			clear(pan)
			return nil, fmt.Errorf("invalid incomplete payment track")
		}
		if err := accept(candidate); err != nil {
			clear(pan)
			return nil, err
		}
	}
	if len(pan) == 0 {
		return nil, fmt.Errorf("payment PAN unavailable")
	}
	return pan, nil
}
