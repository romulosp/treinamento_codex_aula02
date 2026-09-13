package command

import (
	"fmt"
	"strconv"
)

// CLXRequest define a apresentação visual opcional usada por CLX. MediaName
// tem precedência no equipamento quando os dois campos são enviados.
type CLXRequest struct {
	Message   string
	MediaName string
}

// GTKRequest define a recuperação de trilhas após uma leitura elegível. Os
// campos criptográficos são binários e nunca devem ser registrados pelo chamador.
type GTKRequest struct {
	Tracks       string
	DataMethod   string
	OpenDigits   int
	KeyIndex     *int
	WorkingKey   []byte
	IV           []byte
	PublicKeyMod []byte
	PublicKeyExp []byte
}

// GOXRequest define a continuação EMV de uma captura GCX ICC ou CTLS.
type GOXRequest struct {
	AcquirerReference string
	PinMethod         string
	KeyIndex          int
	WorkingKey        []byte
	TransactionType   []byte
	Amount            string
	Cashback          string
	Currency          []byte
	Options           string
	DisplayMessage    string
	TerminalParams    []byte
	EMVData           []byte
	TagList           []byte
	Timeout           *byte
}

// FCXRequest define a finalização EMV após GOX, incluindo autorização e
// objetos EMV opcionais solicitados pelo consumidor.
type FCXRequest struct {
	Options       string
	Authorization string
	EMVData       []byte
	TagList       []byte
	Timeout       *byte
}

// BuildCLXCommand monta CLX com display limpo, mensagem, mídia ou ambos.
func BuildCLXCommand(request CLXRequest) ([]byte, error) {
	parameters := make([]Parameter, 0, 2)
	if request.Message != "" {
		message, err := latin1(request.Message, 128, "CLX message")
		if err != nil {
			return nil, err
		}
		parameters = append(parameters, Parameter{ID: SPEDisplayMessage, Value: message})
	}
	if request.MediaName != "" {
		if !validMediaName(request.MediaName) {
			return nil, fmt.Errorf("CLX multimedia name must be A8")
		}
		parameters = append(parameters, Parameter{ID: SPEMultimediaFileName, Value: []byte(request.MediaName)})
	}
	return BuildABECSPayload(CommandCLX, parameters)
}

// BuildGTKCommand valida os parâmetros criptográficos conhecidos do GTK e
// monta o payload sem converter ou registrar dados sensíveis.
func BuildGTKCommand(request GTKRequest) ([]byte, error) {
	if request.Tracks != "" && (len(request.Tracks) != 4 || !decimalDigits(request.Tracks)) {
		return nil, fmt.Errorf("invalid GTK track selection")
	}
	if request.DataMethod != "" && !validDataMethod(request.DataMethod) {
		return nil, fmt.Errorf("invalid GTK data method")
	}
	if request.OpenDigits < 0 || request.OpenDigits > 8 || request.OpenDigits%2 != 0 {
		return nil, fmt.Errorf("invalid GTK open digits")
	}
	rsaMethod := request.DataMethod == "90" || request.DataMethod == "91"
	if request.DataMethod != "" && !rsaMethod && (request.KeyIndex == nil || *request.KeyIndex < 0 || *request.KeyIndex > 99) {
		return nil, fmt.Errorf("GTK key index is required")
	}
	desMKWK := request.DataMethod == "00" || request.DataMethod == "01"
	tdesMKWK := request.DataMethod == "10" || request.DataMethod == "11"
	mkwk := desMKWK || tdesMKWK
	if (desMKWK && len(request.WorkingKey) != 8) || (tdesMKWK && len(request.WorkingKey) != 16) {
		return nil, fmt.Errorf("GTK working key is required for MK/WK")
	}
	if !mkwk && len(request.WorkingKey) != 0 {
		return nil, fmt.Errorf("GTK working key is not allowed for this method")
	}
	cbc := request.DataMethod == "01" || request.DataMethod == "11" || request.DataMethod == "51" || request.DataMethod == "91"
	if len(request.IV) != 0 && (!cbc || len(request.IV) != 8) {
		return nil, fmt.Errorf("GTK CBC IV must have 8 bytes")
	}
	if rsaMethod && (len(request.PublicKeyMod) != 256 || len(request.PublicKeyExp) < 1 || len(request.PublicKeyExp) > 3) {
		return nil, fmt.Errorf("GTK RSA key must contain a 256-byte modulus and 1..3-byte exponent")
	}
	if !rsaMethod && (len(request.PublicKeyMod) != 0 || len(request.PublicKeyExp) != 0) {
		return nil, fmt.Errorf("GTK RSA key is only allowed for method 90/91")
	}
	parameters := make([]Parameter, 0, 8)
	if request.DataMethod != "" {
		parameters = append(parameters, Parameter{ID: SPEDataMethod, Value: []byte(request.DataMethod)})
	}
	if request.Tracks != "" {
		parameters = append(parameters, Parameter{ID: SPETracks, Value: []byte(request.Tracks)})
	}
	if len(request.IV) > 0 {
		parameters = append(parameters, Parameter{ID: SPEIVCBC, Value: append([]byte(nil), request.IV...)})
	}
	if request.OpenDigits > 0 {
		parameters = append(parameters, Parameter{ID: SPEOpenDigits, Value: []byte(strconv.Itoa(request.OpenDigits))})
	}
	if request.KeyIndex != nil && !rsaMethod {
		parameters = append(parameters, Parameter{ID: SPEKeyIndex, Value: []byte(fmt.Sprintf("%02d", *request.KeyIndex))})
	}
	if len(request.WorkingKey) > 0 {
		parameters = append(parameters, Parameter{ID: SPEEncryptedWorkingKey, Value: append([]byte(nil), request.WorkingKey...)})
	}
	if rsaMethod {
		parameters = append(parameters, Parameter{ID: SPEPinBlockMode, Value: append([]byte(nil), request.PublicKeyMod...)}, Parameter{ID: SPEPinBlockExponent, Value: append([]byte(nil), request.PublicKeyExp...)})
	}
	return BuildABECSPayload(CommandGTK, parameters)
}

// BuildGOXCommand valida os campos mandatórios de GOX e monta os parâmetros
// ABECS preservando blobs EMV e material de PIN como bytes.
func BuildGOXCommand(request GOXRequest) ([]byte, error) {
	if len(request.AcquirerReference) != 2 || !decimalDigits(request.AcquirerReference) || request.AcquirerReference == "00" {
		return nil, fmt.Errorf("invalid GOX acquirer reference")
	}
	if len(request.PinMethod) != 1 || request.PinMethod[0] < '0' || request.PinMethod[0] > '3' || request.KeyIndex < 0 || request.KeyIndex > 99 {
		return nil, fmt.Errorf("invalid GOX PIN method or key index")
	}
	if (request.PinMethod == "0" || request.PinMethod == "1") && (len(request.WorkingKey) != 8 && len(request.WorkingKey) != 16) {
		return nil, fmt.Errorf("GOX working key is required for MK/WK")
	}
	if request.TransactionType != nil && len(request.TransactionType) != 1 {
		return nil, fmt.Errorf("GOX transaction type must have one byte")
	}
	if request.Amount != "" && !isFixedNumeric(request.Amount, 12) {
		return nil, fmt.Errorf("invalid GOX amount")
	}
	if request.Cashback != "" && !isFixedNumeric(request.Cashback, 12) {
		return nil, fmt.Errorf("invalid GOX cashback")
	}
	if request.Currency != nil && (len(request.Currency) != 3 || !decimalDigits(string(request.Currency))) {
		return nil, fmt.Errorf("invalid GOX currency")
	}
	if request.Options != "" && (len(request.Options) != 5 ||
		(request.Options[0] != '0' && request.Options[0] != '1') ||
		(request.Options[1] != '0' && request.Options[1] != '1') ||
		(request.Options[2] != '0' && request.Options[2] != '1') || request.Options[3:] != "00") {
		return nil, fmt.Errorf("invalid GOX options")
	}
	displayMessage, err := latin1(request.DisplayMessage, 128, "GOX display message")
	if err != nil {
		return nil, err
	}
	if request.TerminalParams != nil && len(request.TerminalParams) != 10 {
		return nil, fmt.Errorf("GOX terminal parameters must have 10 bytes")
	}
	if len(request.EMVData) > 512 || len(request.TagList) > 128 {
		return nil, fmt.Errorf("GOX EMV data or tag list exceeds the limit")
	}
	parameters := make([]Parameter, 0, 13)
	parameters = appendOptional(parameters, SPETransactionType, request.TransactionType)
	parameters = appendOptionalString(parameters, SPEAmount, request.Amount)
	parameters = appendOptionalString(parameters, SPECashback, request.Cashback)
	parameters = appendOptional(parameters, SPETransactionCurrency, request.Currency)
	parameters = appendOptionalString(parameters, SPEGOXOption, request.Options)
	parameters = append(parameters,
		Parameter{ID: SPEPinMethod, Value: []byte(request.PinMethod)},
		Parameter{ID: SPEKeyIndex, Value: []byte(fmt.Sprintf("%02d", request.KeyIndex))},
	)
	if len(request.WorkingKey) > 0 {
		parameters = append(parameters, Parameter{ID: SPEEncryptedWorkingKey, Value: append([]byte(nil), request.WorkingKey...)})
	}
	parameters = appendOptional(parameters, SPEDisplayMessage, displayMessage)
	parameters = appendOptional(parameters, SPETerminalParameters, request.TerminalParams)
	parameters = appendOptional(parameters, SPEEMVData, request.EMVData)
	parameters = appendOptional(parameters, SPETagList, request.TagList)
	parameters = append(parameters, Parameter{ID: SPEAcquirerReference, Value: []byte(request.AcquirerReference)})
	if request.Timeout != nil {
		parameters = append(parameters, Parameter{ID: SPETimeout, Value: []byte{*request.Timeout}})
	}
	return BuildABECSPayload(CommandGOX, parameters)
}

// BuildFCXCommand valida o resultado de autorização e os campos condicionais
// de FCX antes de montar o comando ABECS.
func BuildFCXCommand(request FCXRequest) ([]byte, error) {
	if len(request.Options) != 4 || !decimalDigits(request.Options) || request.Options[0] < '0' || request.Options[0] > '2' || request.Options[1:] != "000" {
		return nil, fmt.Errorf("invalid FCX option")
	}
	if (request.Options[0] == '0' || request.Options[0] == '1') && len(request.Authorization) != 2 {
		return nil, fmt.Errorf("FCX authorization response code is required")
	}
	if request.Options[0] == '2' && request.Authorization != "" {
		return nil, fmt.Errorf("FCX authorization response code is not allowed for abort")
	}
	if len(request.EMVData) > 512 || len(request.TagList) > 128 {
		return nil, fmt.Errorf("FCX EMV data or tag list exceeds the limit")
	}
	parameters := make([]Parameter, 0, 5)
	parameters = appendOptional(parameters, SPEEMVData, request.EMVData)
	parameters = appendOptional(parameters, SPETagList, request.TagList)
	parameters = appendOptionalString(parameters, SPEARC, request.Authorization)
	parameters = append(parameters, Parameter{ID: SPEFCXOption, Value: []byte(request.Options)})
	if request.Timeout != nil {
		parameters = append(parameters, Parameter{ID: SPETimeout, Value: []byte{*request.Timeout}})
	}
	return BuildABECSPayload(CommandFCX, parameters)
}

func appendOptional(parameters []Parameter, id SPEParameter, value []byte) []Parameter {
	if len(value) != 0 {
		return append(parameters, Parameter{ID: id, Value: append([]byte(nil), value...)})
	}
	return parameters
}

func appendOptionalString(parameters []Parameter, id SPEParameter, value string) []Parameter {
	if value != "" {
		return append(parameters, Parameter{ID: id, Value: []byte(value)})
	}
	return parameters
}

func decimalDigits(value string) bool {
	for _, current := range []byte(value) {
		if current < '0' || current > '9' {
			return false
		}
	}
	return true
}

func validDataMethod(value string) bool {
	switch value {
	case "00", "01", "10", "11", "30", "40", "50", "51", "90", "91":
		return true
	}
	return false
}
