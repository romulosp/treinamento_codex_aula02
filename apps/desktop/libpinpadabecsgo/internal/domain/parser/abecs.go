package parser

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"encoding/hex"
	"fmt"
	"strconv"
	"strings"
)

// tagNames mapeia os identificadores de resposta (RSP_DATID) do comando GIX
// (secao 6.4.3 da especificacao ABECS) para chaves internas usadas em
// model.Response.Tags. Todos os identificadores abaixo correspondem as
// constantes ja definidas em protocol.Tag* e foram confirmados contra
// hardware real (PERTO PPP100).
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

func ParseAbecsResponse(data []byte) (*model.Response, error) {
	response := &model.Response{AckType: protocol.AckType(data), RawData: append([]byte(nil), data...), Tags: map[string]string{}}
	if len(data) == 1 {
		if data[0] == protocol.PP_NAK {
			return response, fmt.Errorf("nak response")
		}
		return response, nil
	}
	// O envelope de resposta ABECS espelha o de comando: CMD_ID (3 bytes,
	// ecoado) + STATUS (3 digitos ASCII) + LEN (3 digitos ASCII com o
	// tamanho decimal dos dados TLV que seguem), confirmado empiricamente
	// contra hardware real (resposta ao comando GIX). Quando o envelope
	// completo nao estiver presente (respostas curtas), cai de volta para o
	// layout minimo (3 primeiros bytes como status) para nao quebrar.
	tagsStart := 3
	if len(data) >= 9 {
		response.StatusCode = string(data[3:6])
		tagsStart = 9
	} else if len(data) >= 3 {
		response.StatusCode = string(data[:3])
	}
	for pos := tagsStart; pos+4 <= len(data); {
		tag := uint16(data[pos])<<8 | uint16(data[pos+1])
		length := int(data[pos+2])<<8 | int(data[pos+3])
		pos += 4
		if pos+length > len(data) {
			break
		}
		name, ok := tagNames[tag]
		if ok {
			response.Tags[name] = string(data[pos : pos+length])
		}
		pos += length
	}
	return response, nil
}
func DeviceInfoFromResponse(response *model.Response) model.DeviceInfo {
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
	// GraphicData (RSP_DATID 0x8021) traz 4 digitos de largura seguidos de 4
	// digitos de altura do display grafico, ex.: "03200240" = 320x240.
	if graphicData := get("GraphicData"); len(graphicData) == 8 {
		info.GraphicWidth, _ = strconv.Atoi(graphicData[:4])
		info.GraphicHeight, _ = strconv.Atoi(graphicData[4:])
	}
	return info
}
func TagHex(value []byte) string { return strings.ToUpper(hex.EncodeToString(value)) }

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
