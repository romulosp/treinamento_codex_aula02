package parser

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"fmt"
)

// MNUResponse contém a opção selecionada e o status do comando MNU.
type MNUResponse struct {
	SelectedIndex int
	Status        string
}

// ParseMNUResponse interpreta a resposta curta do comando MNU.
func ParseMNUResponse(data []byte) (MNUResponse, error) {
	if len(data) < 4 {
		return MNUResponse{}, fmt.Errorf("truncated MNU response")
	}
	return MNUResponse{SelectedIndex: int(data[3] - '0'), Status: string(data[:3])}, nil
}

// IsDisplayResponseSuccess informa se o status ABECS equivale a sucesso.
func IsDisplayResponseSuccess(status string) bool { return status == "000" }

// ParseGKYKey interpreta a tecla opcional retornada por GKY.
func ParseGKYKey(data []byte) (byte, error) {
	if len(data) == 0 {
		return command.GKYKeyNone, nil
	}
	if data[0] < command.GKYKeyOK || data[0] > command.GKYKeyF4 {
		return command.GKYKeyNone, fmt.Errorf("unknown GKY key")
	}
	return data[0], nil
}

// GCXResponseFromResponse converte tags GCX sem alterar respostas de outros comandos.
func GCXResponseFromResponse(response *model.Response) *model.GCXResponse {
	result := &model.GCXResponse{}
	if response == nil {
		return result
	}
	get := func(tag protocol.ResponseTag) string { return string(response.RawTags[uint16(tag)]) }
	result.CardType = get(protocol.TagCardType)
	result.ICCStatus = get(protocol.TagICCStatus)
	result.AidTableInfo = get(protocol.TagAIDTableInfo)
	result.PAN = get(protocol.TagPAN)
	result.PANSequence = get(protocol.TagPANSequenceNumber)
	result.CardholderName = get(protocol.TagCardholderName)
	result.Label = get(protocol.TagLabel)
	result.IssuerCountry = get(protocol.TagIssuerCountry)
	result.ExpirationDate = get(protocol.TagCardExpiration)
	result.DeviceType = get(protocol.TagDeviceType)
	result.Track1 = get(protocol.TagTrack1)
	result.Track2 = get(protocol.TagTrack2)
	result.Track3 = get(protocol.TagTrack3)
	result.EMVData = append([]byte(nil), response.RawTags[uint16(protocol.TagEMVData)]...)
	result.ParsedEMVData = map[uint32][]byte{}
	if len(result.EMVData) == 0 {
		return result
	}
	objects, err := ParseBerTLV(result.EMVData)
	if err != nil {
		return result
	}
	for _, object := range objects {
		result.ParsedEMVData[object.Tag] = append([]byte(nil), object.Value...)
	}
	return result
}

// GTKResponseFromResponse converte as tags de GTK sem compartilhar dados de
// trilha ou KSN com outros modelos de comando.
func GTKResponseFromResponse(response *model.Response) *model.GTKResponse {
	result := &model.GTKResponse{}
	if response == nil {
		return result
	}
	get := func(tag protocol.ResponseTag) []byte { return append([]byte(nil), response.RawTags[uint16(tag)]...) }
	result.EncryptedPAN = get(protocol.TagEncryptedPAN)
	result.Track1 = get(protocol.TagTrack1)
	result.Track2 = get(protocol.TagTrack2)
	result.Track3 = get(protocol.TagTrack3)
	result.Track1KSN = get(protocol.TagTrack1KSN)
	result.Track2KSN = get(protocol.TagTrack2KSN)
	result.Track3KSN = get(protocol.TagTrack3KSN)
	result.EncryptedPANKey = get(protocol.TagEncryptedPANKSN)
	result.EncryptedRandom = get(protocol.TagEncryptedRandom)
	return result
}

// GOXResponseFromResponse converte o resultado EMV e preserva dados de PIN e
// KSN como blobs binários separados.
func GOXResponseFromResponse(response *model.Response) *model.GOXResponse {
	result := &model.GOXResponse{ParsedEMVData: map[uint32][]byte{}}
	if response == nil {
		return result
	}
	result.Result = append([]byte(nil), response.RawTags[uint16(protocol.TagGOXResult)]...)
	result.EMVData = append([]byte(nil), response.RawTags[uint16(protocol.TagEMVData)]...)
	result.PINBlock = append([]byte(nil), response.RawTags[uint16(protocol.TagPINBlock)]...)
	result.KSN = append([]byte(nil), response.RawTags[uint16(protocol.TagKSN)]...)
	parseEMVData(result.EMVData, result.ParsedEMVData)
	return result
}

// FCXResponseFromResponse converte os dados exclusivos da finalização EMV,
// inclusive Issuer Script Results quando presentes.
func FCXResponseFromResponse(response *model.Response) *model.FCXResponse {
	result := &model.FCXResponse{ParsedEMVData: map[uint32][]byte{}}
	if response == nil {
		return result
	}
	result.Result = append([]byte(nil), response.RawTags[uint16(protocol.TagFCXResult)]...)
	result.EMVData = append([]byte(nil), response.RawTags[uint16(protocol.TagEMVData)]...)
	result.IssuerScripts = append([]byte(nil), response.RawTags[uint16(protocol.TagISResults)]...)
	parseEMVData(result.EMVData, result.ParsedEMVData)
	return result
}

func parseEMVData(data []byte, destination map[uint32][]byte) {
	if len(data) == 0 {
		return
	}
	objects, err := ParseBerTLV(data)
	if err != nil {
		return
	}
	for _, object := range objects {
		destination[object.Tag] = append([]byte(nil), object.Value...)
	}
}
