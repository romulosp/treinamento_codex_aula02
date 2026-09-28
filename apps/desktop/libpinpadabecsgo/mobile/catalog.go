package mobile

import (
	"context"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"strings"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	qrcode "github.com/skip2/go-qrcode"
)

// mobileQRCodeGenerator fornece o gerador aprovado pelo core para a fachada.
// Os bytes PNG permanecem no Go e nunca cruzam a fronteira gomobile.
type mobileQRCodeGenerator struct{}

func (mobileQRCodeGenerator) Generate(data string, size int) ([]byte, error) {
	return qrcode.Encode(data, qrcode.Medium, size)
}

// Reset envia CAN/RST pelo caso de uso tipado do core.
func (c *Client) Reset(operationID string) (err error) {
	defer recoverBinding(&err)
	return c.run(operationID, func(ctx context.Context) error { return c.service.Reset(ctx) })
}

// GetInfoRawSummaryJSON devolve apenas status e tamanhos de GIX, sem bytes raw.
func (c *Client) GetInfoRawSummaryJSON(operationID string) (result string, err error) {
	return c.runJSON(operationID, func(ctx context.Context) (any, error) {
		response, callErr := c.service.GetInfoRaw(ctx)
		if callErr != nil {
			return nil, callErr
		}
		return struct {
			SchemaVersion string `json:"schemaVersion"`
			AckType       string `json:"ackType"`
			StatusCode    string `json:"statusCode"`
			DataBytes     int    `json:"dataBytes"`
			RawBytes      int    `json:"rawBytes"`
		}{"1", response.AckType, response.StatusCode, len(response.Data), len(response.RawData)}, nil
	})
}

// GetDisplayCapabilitiesJSON devolve capacidades tipadas e não sensíveis.
func (c *Client) GetDisplayCapabilitiesJSON(operationID string) (result string, err error) {
	return c.runJSON(operationID, func(ctx context.Context) (any, error) {
		capabilities, callErr := c.service.GetDisplayCapabilities(ctx)
		if callErr != nil {
			return nil, callErr
		}
		return capabilities, nil
	})
}

// DisplayDSP envia duas linhas de texto fixo ao display.
func (c *Client) DisplayDSP(operationID, line1, line2 string) (err error) {
	defer recoverBinding(&err)
	return c.run(operationID, func(ctx context.Context) error {
		_, callErr := c.service.DisplayDSP(ctx, line1, line2)
		return callErr
	})
}

// DisplayDEX envia uma mensagem estendida ao display.
func (c *Client) DisplayDEX(operationID, message string) (err error) {
	defer recoverBinding(&err)
	return c.run(operationID, func(ctx context.Context) error {
		_, callErr := c.service.DisplayDEX(ctx, message)
		return callErr
	})
}

// DisplayMNU apresenta opções e devolve somente o índice selecionado.
func (c *Client) DisplayMNU(operationID string, timeout int, title, optionsCSV string) (selected int, err error) {
	return c.runInt(operationID, func(ctx context.Context) (int, error) {
		response, callErr := c.service.DisplayMNU(ctx, timeout, title, splitCSV(optionsCSV))
		if callErr != nil {
			return -1, callErr
		}
		return response.SelectedIndex, nil
	})
}

// WaitForKeyPress aguarda GKY e devolve a tecla como inteiro sem bytes raw.
func (c *Client) WaitForKeyPress(operationID string, timeoutSeconds int) (key int, err error) {
	return c.runInt(operationID, func(ctx context.Context) (int, error) {
		value, callErr := c.service.WaitForKeyPress(ctx, timeoutSeconds)
		return int(value), callErr
	})
}

// PurchaseGCXSummaryJSON executa GCX e redige PAN, trilhas e EMV.
func (c *Client) PurchaseGCXSummaryJSON(operationID, amount, date, clock string, enableCTLS, hideAmount bool) (result string, err error) {
	return c.runJSON(operationID, func(ctx context.Context) (any, error) {
		response, callErr := c.service.PurchaseGCX(ctx, amount, date, clock, enableCTLS, hideAmount)
		if callErr != nil {
			return nil, callErr
		}
		return struct {
			SchemaVersion string `json:"schemaVersion"`
			CardType      string `json:"cardType,omitempty"`
			ICCStatus     string `json:"iccStatus,omitempty"`
			AidTableInfo  string `json:"aidTableInfo,omitempty"`
			DeviceType    string `json:"deviceType,omitempty"`
			Label         string `json:"label,omitempty"`
			IssuerCountry string `json:"issuerCountry,omitempty"`
		}{"1", response.CardType, response.ICCStatus, response.AidTableInfo, response.DeviceType, response.Label, response.IssuerCountry}, nil
	})
}

// OpenSecure abre a sessão OPN RSA/AES com chave efêmera gerada no Go.
func (c *Client) OpenSecure(operationID string) (err error) {
	defer recoverBinding(&err)
	return c.run(operationID, func(ctx context.Context) error {
		privateKey, _, generateErr := protocol.GenerateSecureOPN()
		if generateErr != nil {
			return generateErr
		}
		return c.service.OpenSecure(ctx, privateKey)
	})
}

// CloseVisual envia CLX sem encerrar a conexão serial.
func (c *Client) CloseVisual(operationID, message, mediaName string) (err error) {
	defer recoverBinding(&err)
	return c.run(operationID, func(ctx context.Context) error {
		_, callErr := c.service.CloseVisual(ctx, command.CLXRequest{Message: message, MediaName: mediaName})
		return callErr
	})
}

// LoadQRCodeMultimedia gera, carrega e finaliza uma mídia QR no pinpad.
func (c *Client) LoadQRCodeMultimedia(operationID, name, data string, requestedSize int) (err error) {
	defer recoverBinding(&err)
	return c.run(operationID, func(ctx context.Context) error {
		return c.service.LoadQRCodeMultimedia(ctx, name, data, requestedSize, nil)
	})
}

// DisplayImage exibe uma mídia previamente carregada por DSI.
func (c *Client) DisplayImage(operationID, name string) (err error) {
	defer recoverBinding(&err)
	return c.run(operationID, func(ctx context.Context) error {
		_, callErr := c.service.DisplayImage(ctx, name)
		return callErr
	})
}

// LoadCompleteEMVTable carrega uma tabela EMV pelos comandos TLI/TLR/TLE.
func (c *Client) LoadCompleteEMVTable(operationID, acquirer, version, recordsCSV string) (err error) {
	defer recoverBinding(&err)
	return c.run(operationID, func(ctx context.Context) error {
		return c.service.LoadCompleteEMVTable(ctx, acquirer, version, splitCSV(recordsCSV), nil)
	})
}

// GetTracksSummaryJSON executa GTK e retorna apenas tamanhos e presença de dados.
func (c *Client) GetTracksSummaryJSON(operationID, tracks, dataMethod string, openDigits, keyIndex int, workingKeyHex, ivHex, publicKeyModHex, publicKeyExpHex string) (result string, err error) {
	request, buildErr := buildGTKRequest(tracks, dataMethod, openDigits, keyIndex, workingKeyHex, ivHex, publicKeyModHex, publicKeyExpHex)
	if buildErr != nil {
		return "{}", buildErr
	}
	return c.runJSON(operationID, func(ctx context.Context) (any, error) {
		response, callErr := c.service.GetTracks(ctx, request)
		if callErr != nil {
			return nil, callErr
		}
		return struct {
			SchemaVersion  string `json:"schemaVersion"`
			EncryptedPAN   int    `json:"encryptedPanBytes"`
			Track1Bytes    int    `json:"track1Bytes"`
			Track2Bytes    int    `json:"track2Bytes"`
			Track3Bytes    int    `json:"track3Bytes"`
			Track1KSNBytes int    `json:"track1KsnBytes"`
			Track2KSNBytes int    `json:"track2KsnBytes"`
			Track3KSNBytes int    `json:"track3KsnBytes"`
		}{"1", len(response.EncryptedPAN), len(response.Track1), len(response.Track2), len(response.Track3), len(response.Track1KSN), len(response.Track2KSN), len(response.Track3KSN)}, nil
	})
}

// ContinueEMVSummaryJSON executa GOX e não expõe PIN block, KSN ou EMV bruto.
func (c *Client) ContinueEMVSummaryJSON(operationID, acquirerReference, pinMethod string, keyIndex int, workingKeyHex, transactionType, amount, cashback, currencyHex, options, displayMessage, terminalParamsHex, emvDataHex, tagListHex string, timeoutSeconds int) (result string, err error) {
	request, buildErr := buildGOXRequest(acquirerReference, pinMethod, keyIndex, workingKeyHex, transactionType, amount, cashback, currencyHex, options, displayMessage, terminalParamsHex, emvDataHex, tagListHex, timeoutSeconds)
	if buildErr != nil {
		return "{}", buildErr
	}
	return c.runJSON(operationID, func(ctx context.Context) (any, error) {
		response, callErr := c.service.ContinueEMV(ctx, request)
		if callErr != nil {
			return nil, callErr
		}
		return struct {
			SchemaVersion string `json:"schemaVersion"`
			Result        string `json:"result"`
			EMVDataBytes  int    `json:"emvDataBytes"`
		}{"1", string(response.Result), len(response.EMVData)}, nil
	})
}

// FinalizeEMVSummaryJSON executa FCX sem expor dados EMV brutos.
func (c *Client) FinalizeEMVSummaryJSON(operationID, options, authorization, emvDataHex, tagListHex string, timeoutSeconds int) (result string, err error) {
	request, buildErr := buildFCXRequest(options, authorization, emvDataHex, tagListHex, timeoutSeconds)
	if buildErr != nil {
		return "{}", buildErr
	}
	return c.runJSON(operationID, func(ctx context.Context) (any, error) {
		response, callErr := c.service.FinalizeEMV(ctx, request)
		if callErr != nil {
			return nil, callErr
		}
		return struct {
			SchemaVersion string `json:"schemaVersion"`
			Result        string `json:"result"`
			EMVDataBytes  int    `json:"emvDataBytes"`
			IssuerScripts int    `json:"issuerScriptsBytes"`
		}{"1", string(response.Result), len(response.EMVData), len(response.IssuerScripts)}, nil
	})
}

// CapturePINMK executa GPN MK/WK e descarta PIN block e KSN no Go.
func (c *Client) CapturePINMK(operationID string, keyIndex int, workingKeyHex, pan, message string) (err error) {
	workingKey, decodeErr := decodeHex(workingKeyHex, "working key")
	if decodeErr != nil {
		return decodeErr
	}
	defer recoverBinding(&err)
	return c.run(operationID, func(ctx context.Context) error {
		_, _, callErr := c.service.SendGPNCommandMK(ctx, keyIndex, workingKey, pan, message)
		return callErr
	})
}

// CapturePINDUKPT executa GPN DUKPT e descarta PIN block e KSN no Go.
func (c *Client) CapturePINDUKPT(operationID string, keyIndex int, pan, message string) (err error) {
	defer recoverBinding(&err)
	return c.run(operationID, func(ctx context.Context) error {
		_, _, callErr := c.service.SendGPNCommandDUKPT(ctx, keyIndex, pan, message)
		return callErr
	})
}

// DisplayQRCodeSummaryJSON gera PNG local e retorna apenas metadados da geração.
func (c *Client) DisplayQRCodeSummaryJSON(operationID, data string, size, margin, xPos, yPos int) (result string, err error) {
	return c.runJSON(operationID, func(ctx context.Context) (any, error) {
		response, callErr := c.service.DisplayQRCode(ctx, data, size, margin, xPos, yPos)
		if callErr != nil {
			return nil, callErr
		}
		return struct {
			SchemaVersion   string `json:"schemaVersion"`
			GeneratedBytes  int    `json:"generatedBytes"`
			X               int    `json:"x"`
			Y               int    `json:"y"`
			PositionWarning string `json:"positionWarning,omitempty"`
		}{"1", len(response.PNG), response.X, response.Y, response.PositionWarning}, nil
	})
}

// ListMultimediaFilesJSON devolve somente nomes A8 validados pelo parser Go.
func (c *Client) ListMultimediaFilesJSON(operationID string) (result string, err error) {
	return c.runJSON(operationID, func(ctx context.Context) (any, error) {
		return c.service.ListMultimediaFiles(ctx)
	})
}

// DeleteMultimediaFiles remove mídias pelos nomes validados pelo core.
func (c *Client) DeleteMultimediaFiles(operationID, namesCSV string) (status string, err error) {
	return c.runString(operationID, func(ctx context.Context) (string, error) {
		response, callErr := c.service.DeleteMultimediaFiles(ctx, splitCSV(namesCSV))
		if callErr != nil {
			return "", callErr
		}
		return response.StatusCode, nil
	})
}

func (c *Client) runJSON(operationID string, operation func(context.Context) (any, error)) (result string, err error) {
	defer recoverBinding(&err)
	result = "{}"
	err = c.run(operationID, func(ctx context.Context) error {
		value, callErr := operation(ctx)
		if callErr != nil {
			return callErr
		}
		encoded, marshalErr := json.Marshal(value)
		if marshalErr != nil {
			return marshalErr
		}
		result = string(encoded)
		return nil
	})
	return result, err
}

func (c *Client) runInt(operationID string, operation func(context.Context) (int, error)) (result int, err error) {
	defer recoverBinding(&err)
	err = c.run(operationID, func(ctx context.Context) error {
		value, callErr := operation(ctx)
		result = value
		return callErr
	})
	return result, err
}

func (c *Client) runString(operationID string, operation func(context.Context) (string, error)) (result string, err error) {
	defer recoverBinding(&err)
	err = c.run(operationID, func(ctx context.Context) error {
		value, callErr := operation(ctx)
		result = value
		return callErr
	})
	return result, err
}

func decodeHex(value, name string) ([]byte, error) {
	value = strings.TrimSpace(value)
	if value == "" {
		return nil, nil
	}
	decoded, err := hex.DecodeString(value)
	if err != nil {
		return nil, fmt.Errorf("%s must be hexadecimal: %w", name, err)
	}
	return decoded, nil
}

// splitCSV converte listas da ABI gomobile em valores tipados do caso de uso.
func splitCSV(value string) []string {
	parts := strings.FieldsFunc(value, func(r rune) bool { return r == ',' || r == ';' })
	result := make([]string, 0, len(parts))
	for _, part := range parts {
		if trimmed := strings.TrimSpace(part); trimmed != "" {
			result = append(result, trimmed)
		}
	}
	return result
}

func buildGTKRequest(tracks, dataMethod string, openDigits, keyIndex int, workingKeyHex, ivHex, publicKeyModHex, publicKeyExpHex string) (command.GTKRequest, error) {
	workingKey, err := decodeHex(workingKeyHex, "GTK working key")
	if err != nil {
		return command.GTKRequest{}, err
	}
	iv, err := decodeHex(ivHex, "GTK IV")
	if err != nil {
		return command.GTKRequest{}, err
	}
	modulus, err := decodeHex(publicKeyModHex, "GTK RSA modulus")
	if err != nil {
		return command.GTKRequest{}, err
	}
	exponent, err := decodeHex(publicKeyExpHex, "GTK RSA exponent")
	if err != nil {
		return command.GTKRequest{}, err
	}
	request := command.GTKRequest{Tracks: tracks, DataMethod: dataMethod, OpenDigits: openDigits, WorkingKey: workingKey, IV: iv, PublicKeyMod: modulus, PublicKeyExp: exponent}
	if keyIndex >= 0 {
		request.KeyIndex = &keyIndex
	}
	return request, nil
}

func buildGOXRequest(acquirerReference, pinMethod string, keyIndex int, workingKeyHex, transactionType, amount, cashback, currencyHex, options, displayMessage, terminalParamsHex, emvDataHex, tagListHex string, timeoutSeconds int) (command.GOXRequest, error) {
	workingKey, err := decodeHex(workingKeyHex, "GOX working key")
	if err != nil {
		return command.GOXRequest{}, err
	}
	currency, err := decodeHex(currencyHex, "GOX currency")
	if err != nil {
		return command.GOXRequest{}, err
	}
	terminalParams, err := decodeHex(terminalParamsHex, "GOX terminal parameters")
	if err != nil {
		return command.GOXRequest{}, err
	}
	emvData, err := decodeHex(emvDataHex, "GOX EMV data")
	if err != nil {
		return command.GOXRequest{}, err
	}
	tagList, err := decodeHex(tagListHex, "GOX tag list")
	if err != nil {
		return command.GOXRequest{}, err
	}
	request := command.GOXRequest{AcquirerReference: acquirerReference, PinMethod: pinMethod, KeyIndex: keyIndex, WorkingKey: workingKey, TransactionType: []byte(transactionType), Amount: amount, Cashback: cashback, Currency: currency, Options: options, DisplayMessage: displayMessage, TerminalParams: terminalParams, EMVData: emvData, TagList: tagList}
	if timeoutSeconds > 0 {
		if timeoutSeconds > 255 {
			return command.GOXRequest{}, fmt.Errorf("GOX timeout must be between 1 and 255 seconds")
		}
		value := byte(timeoutSeconds)
		request.Timeout = &value
	}
	return request, nil
}

func buildFCXRequest(options, authorization, emvDataHex, tagListHex string, timeoutSeconds int) (command.FCXRequest, error) {
	emvData, err := decodeHex(emvDataHex, "FCX EMV data")
	if err != nil {
		return command.FCXRequest{}, err
	}
	tagList, err := decodeHex(tagListHex, "FCX tag list")
	if err != nil {
		return command.FCXRequest{}, err
	}
	request := command.FCXRequest{Options: options, Authorization: authorization, EMVData: emvData, TagList: tagList}
	if timeoutSeconds > 0 {
		if timeoutSeconds > 255 {
			return command.FCXRequest{}, fmt.Errorf("FCX timeout must be between 1 and 255 seconds")
		}
		value := byte(timeoutSeconds)
		request.Timeout = &value
	}
	return request, nil
}
