package parser

import (
	"fmt"
	"strconv"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

// MNUResponse contém a opção selecionada e o status do comando MNU.
type MNUResponse struct {
	SelectedIndex int
	Status        string
}

// ParseMNUResponse interpreta a resposta do comando MNU.
// O valor deve estar no TLV 0x804D como N2.
func ParseMNUResponse(data []byte) (MNUResponse, error) {
	if len(data) < 9 {
		return MNUResponse{}, fmt.Errorf("truncated MNU response")
	}
	status := string(data[:3])
	if data[3] != 0x80 || data[4] != 0x4D {
		return MNUResponse{}, fmt.Errorf("missing MNU PP_VALUE")
	}
	length := int(data[5])<<8 | int(data[6])
	if length != 2 || len(data) != 7+length {
		return MNUResponse{}, fmt.Errorf("invalid MNU selection")
	}
	value := string(data[7:9])
	for i := range value {
		if value[i] < '0' || value[i] > '9' {
			return MNUResponse{}, fmt.Errorf("invalid MNU selection")
		}
	}
	selected, err := strconv.Atoi(value)
	if err != nil {
		return MNUResponse{}, err
	}
	if selected < 1 || selected > 20 {
		return MNUResponse{}, fmt.Errorf("MNU selection outside 01..20")
	}
	return MNUResponse{SelectedIndex: selected, Status: status}, nil
}

// IsDisplayResponseSuccess informa se o status ABECS equivale a sucesso.
func IsDisplayResponseSuccess(status string) bool { return status == "000" }

// ParseGKYStatus interpreta a tecla codificada no RSP_STAT do GKY.
func ParseGKYStatus(status string) (byte, error) {
	switch status {
	case "000":
		return command.GKYKeyOK, nil
	case "004":
		return command.GKYKeyF1, nil
	case "005":
		return command.GKYKeyF2, nil
	case "006":
		return command.GKYKeyF3, nil
	case "007":
		return command.GKYKeyF4, nil
	case "008":
		return command.GKYKeyClear, nil
	case "013":
		return command.GKYKeyCancel, nil
	}
	return 0, fmt.Errorf("unknown GKY status %q", status)
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

// ValidateGCXResponse verifica PP_CARDTYPE e os campos condicionais definidos
// pela seção 3.7.1 do ABECS 2.12.
func ValidateGCXResponse(response *model.Response) (*model.GCXResponse, error) {
	if response == nil {
		return nil, fmt.Errorf("missing GCX response")
	}
	result := GCXResponseFromResponse(response)
	switch result.CardType {
	case command.GCXCardMagnetic:
		if len(result.ICCStatus) != 1 || (result.ICCStatus[0] != '0' && result.ICCStatus[0] != '1' && result.ICCStatus[0] != '2') {
			return nil, fmt.Errorf("missing or invalid PP_ICCSTAT")
		}
	case command.GCXCardICC, command.GCXCardContactlessEMV:
		if result.AidTableInfo == "" || len(result.PAN) < 2 || len(result.PAN) > 19 || !numeric(result.PAN) ||
			len(result.PANSequence) != 2 || !numeric(result.PANSequence) || result.Label == "" || len(result.Label) > 16 {
			return nil, fmt.Errorf("missing mandatory GCX ICC/CTLS field")
		}
	case command.GCXCardContactlessSimulated:
		if result.AidTableInfo == "" || result.Label == "" || len(result.Label) > 16 {
			return nil, fmt.Errorf("missing mandatory GCX contactless field")
		}
	default:
		return nil, fmt.Errorf("missing or invalid PP_CARDTYPE")
	}
	if result.AidTableInfo != "" && (len(result.AidTableInfo) > 120 || len(result.AidTableInfo)%6 != 0) {
		return nil, fmt.Errorf("invalid PP_AIDTABINFO")
	}
	if result.DeviceType != "" && (len(result.DeviceType) != 2 || !numeric(result.DeviceType)) {
		return nil, fmt.Errorf("invalid PP_DEVTYPE")
	}
	if result.IssuerCountry != "" && (len(result.IssuerCountry) != 3 || !numeric(result.IssuerCountry)) {
		return nil, fmt.Errorf("invalid PP_ISSCNTRY")
	}
	if result.ExpirationDate != "" {
		if len(result.ExpirationDate) != 6 || !numeric(result.ExpirationDate) {
			return nil, fmt.Errorf("invalid PP_CARDEXP")
		}
		if _, err := time.Parse("060102", result.ExpirationDate); err != nil {
			return nil, fmt.Errorf("invalid PP_CARDEXP: %w", err)
		}
	}
	if len(result.EMVData) > 0 {
		if _, err := ParseBerTLV(result.EMVData); err != nil {
			return nil, fmt.Errorf("invalid PP_EMVDATA: %w", err)
		}
	}
	return result, nil
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

// ValidateGTKResponse verifica os campos condicionais gerados pelo método de
// criptografia solicitado, sem exigir trilhas que o cartão não disponibilizou.
func ValidateGTKResponse(response *model.Response, request command.GTKRequest) (*model.GTKResponse, error) {
	if response == nil {
		return nil, fmt.Errorf("missing GTK response")
	}
	result := GTKResponseFromResponse(response)
	if len(result.EncryptedPAN) > 16 || len(result.Track1) > 88 || len(result.Track2) > 28 || len(result.Track3) > 60 {
		return nil, fmt.Errorf("GTK card data exceeds ABECS limit")
	}
	dukpt := request.DataMethod == "30" || request.DataMethod == "40" || request.DataMethod == "50" || request.DataMethod == "51"
	if dukpt {
		for _, field := range []struct {
			value []byte
			ksn   []byte
			name  string
		}{
			{result.EncryptedPAN, result.EncryptedPANKey, "PAN"},
			{result.Track1, result.Track1KSN, "track 1"},
			{result.Track2, result.Track2KSN, "track 2"},
			{result.Track3, result.Track3KSN, "track 3"},
		} {
			if len(field.value) > 0 && len(field.ksn) != 10 {
				return nil, fmt.Errorf("missing or invalid GTK KSN for %s", field.name)
			}
		}
	}
	if request.DataMethod == "90" || request.DataMethod == "91" {
		if len(result.EncryptedRandom) != 256 {
			return nil, fmt.Errorf("missing or invalid GTK encrypted random key")
		}
	}
	if !dukpt && (len(result.EncryptedPANKey) != 0 || len(result.Track1KSN) != 0 || len(result.Track2KSN) != 0 || len(result.Track3KSN) != 0) {
		return nil, fmt.Errorf("unexpected GTK KSN for non-DUKPT method")
	}
	return result, nil
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

// ValidateGOXResponse verifica campos mandatórios e condicionais da resposta.
func ValidateGOXResponse(response *model.Response, request command.GOXRequest) (*model.GOXResponse, error) {
	if response == nil {
		return nil, fmt.Errorf("missing GOX response")
	}
	result := GOXResponseFromResponse(response)
	if len(result.Result) != 6 ||
		(result.Result[0] != '0' && result.Result[0] != '1' && result.Result[0] != '2') ||
		(result.Result[1] != '0' && result.Result[1] != '1') ||
		(result.Result[2] != '0' && result.Result[2] != '1' && result.Result[2] != '2') ||
		(result.Result[3] != '0' && result.Result[3] != '1') || string(result.Result[4:]) != "00" {
		return nil, fmt.Errorf("missing or invalid PP_GOXRES")
	}
	if result.Result[2] == '2' {
		if len(result.PINBlock) != 8 {
			return nil, fmt.Errorf("missing GOX PIN block")
		}
		if (request.PinMethod == "2" || request.PinMethod == "3") && len(result.KSN) != 10 {
			return nil, fmt.Errorf("missing GOX KSN")
		}
	}
	if len(request.TagList) > 0 {
		if _, ok := response.RawTags[uint16(protocol.TagEMVData)]; !ok {
			return nil, fmt.Errorf("missing GOX EMV data")
		}
	}
	if len(result.EMVData) > 0 {
		if _, err := ParseBerTLV(result.EMVData); err != nil {
			return nil, fmt.Errorf("invalid GOX EMV data: %w", err)
		}
	}
	return result, nil
}

// ValidateFCXResponse verifica PP_FCXRES e o retorno EMV solicitado.
func ValidateFCXResponse(response *model.Response, request command.FCXRequest) (*model.FCXResponse, error) {
	if response == nil {
		return nil, fmt.Errorf("missing FCX response")
	}
	result := FCXResponseFromResponse(response)
	if len(result.Result) != 3 || (result.Result[0] != '0' && result.Result[0] != '1') || string(result.Result[1:]) != "00" {
		return nil, fmt.Errorf("missing or invalid PP_FCXRES")
	}
	if len(request.TagList) > 0 {
		if _, ok := response.RawTags[uint16(protocol.TagEMVData)]; !ok {
			return nil, fmt.Errorf("missing FCX EMV data")
		}
	}
	if len(result.EMVData) > 0 {
		if _, err := ParseBerTLV(result.EMVData); err != nil {
			return nil, fmt.Errorf("invalid FCX EMV data: %w", err)
		}
	}
	if len(result.IssuerScripts) > 50 || len(result.IssuerScripts)%5 != 0 {
		return nil, fmt.Errorf("invalid PP_ISRESULTS")
	}
	return result, nil
}

func numeric(value string) bool {
	for index := range value {
		if value[index] < '0' || value[index] > '9' {
			return false
		}
	}
	return true
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
