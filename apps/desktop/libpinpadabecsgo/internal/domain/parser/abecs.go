package parser

import (
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"strconv"
	"strings"
)

// tagNames mapeia os identificadores de resposta (RSP_DATID) do comando GIX
// (secao 6.4.3 do manual ABECS v2.12) para chaves internas usadas em
// model.Response.Tags. A interpretação no dispositivo permanece sujeita à
// validação física prevista pela Change.
var tagNames = map[uint16]string{
	0x8001: "SerialNumber",
	0x8002: "PartNumber",
	0x8003: "Model",
	0x8004: "Manufacturer",
	0x8005: "Capabilities",
	0x8006: "OSVersion",
	0x8007: "Specification",
	0x8008: "ManufacturerVersion",
	0x8009: "AbecsVersion",
	0x800A: "ExtendedAbecsVersion",
	0x800B: "ContactlessCapabilities",
	0x8010: "KernelVersion",
	0x8011: "ContactlessVersion",
	0x8012: "MasterCardPaypass",
	0x8013: "VisaPaypass",
	0x8014: "Aexp",
	0x8015: "DiscoverContactless",
	0x8016: "PureVersion",
	0x8018: "QPContactless",
	0x8020: "TextMode",
	0x8021: "GraphicData",
	0x8022: "SupportedFormats",
}

// ParseNotification valida uma mensagem intermediária NTM de comando blocante
// e devolve seu texto sem tratá-lo como um bloco de parâmetros TLV.
func ParseNotification(data []byte) (string, error) {
	if len(data) < 9 || string(data[:3]) != "NTM" || string(data[3:6]) != "000" {
		return "", domainerror.ErrInvalidNotification
	}
	length, err := strconv.Atoi(string(data[6:9]))
	if err != nil || length < 0 || length > 32 || len(data) != 9+length {
		return "", domainerror.ErrInvalidNotification
	}
	return string(data[9:]), nil
}

// ParseAbecsResponse interpreta status, blocos N3 e parâmetros TLV de uma resposta ABECS.
func ParseAbecsResponse(data []byte) (*model.Response, error) {
	response := &model.Response{AckType: protocol.AckType(data), RawData: append([]byte(nil), data...), Tags: map[string]string{}, RawTags: map[uint16][]byte{}}
	if len(data) == 1 {
		if data[0] == protocol.PP_NAK {
			return response, domainerror.ErrNakReceived
		}
		return response, nil
	}
	if len(data) < 6 {
		return nil, domainerror.ErrInvalidResponse
	}
	response.StatusCode = string(data[3:6])
	if len(data) == 6 {
		return response, nil
	}
	if len(data) < 9 {
		return nil, domainerror.ErrInvalidResponse
	}

	commandID := string(data[:3])
	pos := 6
	for pos < len(data) {
		if pos+3 > len(data) {
			return nil, domainerror.ErrInvalidResponse
		}
		blockLength, err := strconv.Atoi(string(data[pos : pos+3]))
		if err != nil || blockLength < 0 || blockLength > 999 || pos+3+blockLength > len(data) {
			return nil, domainerror.ErrInvalidResponse
		}
		pos += 3
		block := data[pos : pos+blockLength]
		response.Data = append(response.Data, block...)
		pos += blockLength

		// OPN seguro e GPN possuem dados posicionais em vez de RSP_DATID TLV.
		if commandID == "OPN" || commandID == "GPN" {
			continue
		}
		blockPos := 0
		for blockPos < len(block) {
			if blockPos+4 > len(block) {
				return nil, domainerror.ErrInvalidResponse
			}
			tag := uint16(block[blockPos])<<8 | uint16(block[blockPos+1])
			length := int(block[blockPos+2])<<8 | int(block[blockPos+3])
			blockPos += 4
			if blockPos+length > len(block) {
				return nil, domainerror.ErrInvalidResponse
			}
			value := append([]byte(nil), block[blockPos:blockPos+length]...)
			response.RawTags[tag] = value
			if name, ok := tagNames[tag]; ok {
				response.Tags[name] = string(value)
			}
			blockPos += length
		}
	}
	return response, nil
}

// DeviceInfoFromResponse converte as tags GIX conhecidas em informações tipadas.
func DeviceInfoFromResponse(response *model.Response) model.DeviceInfo {
	if response == nil {
		return model.DeviceInfo{}
	}
	get := func(key string) string { return response.Tags[key] }
	info := model.DeviceInfo{
		SerialNumber:            get("SerialNumber"),
		PartNumber:              get("PartNumber"),
		Model:                   get("Model"),
		Manufacturer:            get("Manufacturer"),
		Capabilities:            get("Capabilities"),
		OSVersion:               get("OSVersion"),
		Specification:           get("Specification"),
		ManufacturerVersion:     get("ManufacturerVersion"),
		AbecsVersion:            get("AbecsVersion"),
		ExtendedAbecsVersion:    get("ExtendedAbecsVersion"),
		ContactlessCapabilities: get("ContactlessCapabilities"),
		KernelVersion:           get("KernelVersion"),
		ContactlessVersion:      get("ContactlessVersion"),
		MasterCardPaypass:       get("MasterCardPaypass"),
		VisaPaypass:             get("VisaPaypass"),
		Aexp:                    get("Aexp"),
		DiscoverContactless:     get("DiscoverContactless"),
		QPContactless:           get("QPContactless"),
		SupportedFormats:        get("SupportedFormats"),
		RawData:                 append([]byte(nil), response.RawData...),
	}
	// Capabilities (RSP_DATID 0x8005 / PP_CAPAB) e uma string N10 cujo
	// significado dos digitos e definido na secao 3.1.3.2 da especificacao
	// ABECS:
	//   digito 1: "0" = nao suporta CTLS; "1" = suporta CTLS.
	//   digito 2: "0" = sem display grafico; "1" = display grafico
	//             monocromatico; "2" = display grafico colorido.
	//   digitos 3-10: reservados para uso futuro (RUF).
	if info.Capabilities != "" {
		info.Capabilities = decodeCapabilities(info.Capabilities)
	}
	// TextMode (RSP_DATID 0x8020) traz 2 digitos de linhas seguidos de 2
	// digitos de colunas do display, ex.: "1608" = 16 linhas x 8 colunas.
	if textMode := get("TextMode"); len(textMode) == 4 {
		info.TextRows, _ = strconv.Atoi(textMode[:2])
		info.TextCols, _ = strconv.Atoi(textMode[2:])
	}
	// GraphicData (RSP_DATID 0x8021) usa LLLLCCCC: linhas/altura e
	// colunas/largura.
	if graphicData := get("GraphicData"); len(graphicData) == 8 {
		info.GraphicHeight, _ = strconv.Atoi(graphicData[:4])
		info.GraphicWidth, _ = strconv.Atoi(graphicData[4:])
	}
	return info
}

// DisplayCapabilitiesFromResponse converte as tags GIX em capacidades sem
// depender de heurísticas específicas de fabricante.
func DisplayCapabilitiesFromResponse(response *model.Response) model.DisplayCapabilities {
	info := DeviceInfoFromResponse(response)
	capabilities := model.DisplayCapabilities{
		TextLines:     info.TextRows,
		TextCols:      info.TextCols,
		GraphicWidth:  info.GraphicWidth,
		GraphicHeight: info.GraphicHeight,
		Model:         info.Model,
		Manufacturer:  info.Manufacturer,
	}
	raw := ""
	if response != nil {
		raw = response.Tags["Capabilities"]
	}
	if len(raw) >= 2 {
		capabilities.SupportsCTLS = raw[0] == '1'
		capabilities.HasGraphic = raw[1] == '1' || raw[1] == '2'
		capabilities.HasColor = raw[1] == '2'
	}
	if response != nil {
		formats := response.Tags["SupportedFormats"]
		capabilities.SupportsPNG = len(formats) > 0 && formats[0] == '1'
		capabilities.SupportsJPG = len(formats) > 1 && formats[1] == '1'
		capabilities.SupportsGIF = len(formats) > 2 && formats[2] == '1'
	}
	return capabilities
}

// decodeCapabilities traduz o valor bruto de PP_CAPAB (tag 0x8005) para uma
// descricao legivel: o digito 1 indica suporte a CTLS ("1" = CTLS) e o
// digito 2 indica o tipo de display grafico ("1" = IMG_MONO, "2" =
// IMG_COLOR). Quando um digito nao corresponde a nenhuma capacidade
// conhecida, nada e adicionado para aquele digito.
func decodeCapabilities(raw string) string {
	if len(raw) < 2 {
		return raw
	}
	var parts []string
	if raw[0] == '1' {
		parts = append(parts, "CTLS")
	}
	switch raw[1] {
	case '1':
		parts = append(parts, "IMG_MONO")
	case '2':
		parts = append(parts, "IMG_COLOR")
	}
	if len(parts) == 0 {
		return raw
	}
	return strings.Join(parts, ",")
}
