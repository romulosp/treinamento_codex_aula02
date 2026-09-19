// Package dto define os objetos de transferência de dados (Data Transfer
// Objects) utilizados pela API RESTful para comunicação com o pinpad ABECS.
package dto

// ErrorBody padroniza falhas devolvidas pela API RESTful.
type ErrorBody struct {
	Code          string `json:"code"`
	Module        string `json:"module"`
	Message       string `json:"message"`
	Details       string `json:"details,omitempty"`
	CorrelationID string `json:"correlationId"`
}

// ErrorResponse representa a resposta HTTP padronizada para falhas.
type ErrorResponse struct {
	Error ErrorBody `json:"error"`
}

// CanInputDTO define a entrada para o comando CAN (cancelamento).
type CanInputDTO struct{}

// CanOutputDTO define a saída do comando CAN (cancelamento).
type CanOutputDTO struct {
	Status string `json:"status"`
}

// CloInputDTO define a entrada para o comando CLO (fechamento de sessão).
type CloInputDTO struct {
	Message string `json:"message,omitempty"`
	S32     string `json:"s32,omitempty"`
}

// CloOutputDTO define a saída do comando CLO (fechamento de sessão).
type CloOutputDTO struct {
	Status string `json:"status"`
}

// ClxInputDTO define a entrada para o comando CLX (encerramento visual).
type ClxInputDTO struct {
	Message   string `json:"message"`
	MediaName string `json:"mediaName,omitempty"`
}

// ClxOutputDTO define a saída do comando CLX (encerramento visual).
type ClxOutputDTO struct {
	Status string `json:"status"`
}

// DexInputDTO define a entrada para o comando DEX (display estendido).
type DexInputDTO struct {
	Data string `json:"data"`
}

// DexOutputDTO define a saída do comando DEX (display estendido).
type DexOutputDTO struct {
	Data   string `json:"data,omitempty"`
	Status string `json:"status"`
}

// DmfInputDTO define a entrada para o comando DMF (remoção de mídias).
type DmfInputDTO struct {
	Names []string `json:"names"`
}

// DmfOutputDTO define a saída do comando DMF (remoção de mídias).
type DmfOutputDTO struct {
	Status  string   `json:"status"`
	Deleted []string `json:"deleted,omitempty"`
}

// DsiInputDTO define a entrada para o comando DSI (exibição de imagem).
type DsiInputDTO struct {
	Name string `json:"name"`
}

// DsiOutputDTO define a saída do comando DSI (exibição de imagem).
type DsiOutputDTO struct {
	Status string `json:"status"`
	Name   string `json:"name"`
}

// DspInputDTO define a entrada para o comando DSP (display de duas linhas).
type DspInputDTO struct {
	Message string   `json:"message,omitempty"`
	Lines   []string `json:"lines,omitempty"`
	Timeout int      `json:"timeout,omitempty"`
}

// DspOutputDTO define a saída do comando DSP (display de duas linhas).
type DspOutputDTO struct {
	Status string `json:"status"`
}

// FcxInputDTO define a entrada para o comando FCX (finalização EMV).
type FcxInputDTO struct {
	Options       string `json:"options"`
	Authorization string `json:"authorization,omitempty"`
	EMVData       string `json:"emvData,omitempty"`
	TagList       string `json:"tagList,omitempty"`
	Timeout       *int   `json:"timeout,omitempty"`
}

// FcxOutputDTO define a saída do comando FCX (finalização EMV).
type FcxOutputDTO struct {
	Result        string `json:"result,omitempty"`
	EMVData       string `json:"emvData,omitempty"`
	IssuerScripts string `json:"issuerScripts,omitempty"`
}

// GcxInputDTO define a entrada para o comando GCX (captura de cartão).
type GcxInputDTO struct {
	Amount  string `json:"amount"`
	Date    string `json:"date"`
	Time    string `json:"time"`
	Options string `json:"options"`
}

// GcxOutputDTO define a saída do comando GCX (captura de cartão).
type GcxOutputDTO struct {
	CardType     string `json:"cardType,omitempty"`
	ICCStatus    string `json:"iccStatus,omitempty"`
	AidTableInfo string `json:"aidTableInfo,omitempty"`
	PAN          string `json:"pan,omitempty"`
	Track1       string `json:"track1,omitempty"`
	Track2       string `json:"track2,omitempty"`
	EMVData      string `json:"emvData,omitempty"`
}

// GixInputDTO define a entrada para o comando GIX (informações do dispositivo).
type GixInputDTO struct{}

// GixOutputDTO define a saída do comando GIX (informações do dispositivo).
type GixOutputDTO struct {
	SerialNumber     string `json:"serialNumber,omitempty"`
	Model            string `json:"model,omitempty"`
	Manufacturer     string `json:"manufacturer,omitempty"`
	GraphicWidth     int    `json:"graphicWidth"`
	GraphicHeight    int    `json:"graphicHeight"`
	SupportedFormats string `json:"supportedFormats,omitempty"`
}

// GkyInputDTO define a entrada para o comando GKY (aguardo de tecla).
type GkyInputDTO struct {
	TimeoutSeconds int `json:"timeoutSeconds,omitempty"`
}

// GkyOutputDTO define a saída do comando GKY (aguardo de tecla).
type GkyOutputDTO struct {
	Key    string `json:"key"`
	Status string `json:"status"`
}

// GoxInputDTO define a entrada para o comando GOX (continuação EMV).
type GoxInputDTO struct {
	AcquirerReference string `json:"acquirerReference"`
	PinMethod         string `json:"pinMethod"`
	KeyIndex          int    `json:"keyIndex"`
	Amount            string `json:"amount"`
	WorkingKey        string `json:"workingKey,omitempty"`
}

// GoxOutputDTO define a saída do comando GOX (continuação EMV).
type GoxOutputDTO struct {
	Result   string `json:"result,omitempty"`
	EMVData  string `json:"emvData,omitempty"`
	PINBlock string `json:"pinBlock,omitempty"`
	KSN      string `json:"ksn,omitempty"`
}

// GpnInputDTO define a entrada para o comando GPN (captura de PIN).
type GpnInputDTO struct {
	Method     string `json:"method"`
	KeyIndex   int    `json:"keyIndex"`
	WorkingKey string `json:"workingKey,omitempty"`
	PAN        string `json:"pan,omitempty"`
	Message    string `json:"message,omitempty"`
}

// GpnOutputDTO define a saída do comando GPN (captura de PIN).
type GpnOutputDTO struct {
	PINBlock string `json:"pinBlock,omitempty"`
	KSN      string `json:"ksn,omitempty"`
}

// GtkInputDTO define a entrada para o comando GTK (obtenção de trilhas).
type GtkInputDTO struct {
	Tracks     []string `json:"tracks,omitempty"`
	DataMethod string   `json:"dataMethod,omitempty"`
	KeyIndex   *int     `json:"keyIndex,omitempty"`
}

// GtkOutputDTO define a saída do comando GTK (obtenção de trilhas).
type GtkOutputDTO struct {
	Track1       string `json:"track1,omitempty"`
	Track2       string `json:"track2,omitempty"`
	Track3       string `json:"track3,omitempty"`
	EncryptedPAN string `json:"encryptedPAN,omitempty"`
}

// LmfInputDTO define a entrada para o comando LMF (listagem de mídias).
type LmfInputDTO struct{}

// LmfOutputDTO define a saída do comando LMF (listagem de mídias).
type LmfOutputDTO struct {
	Names []string `json:"names"`
}

// MleInputDTO define a entrada para o comando MLE (finalização de carga de mídia).
type MleInputDTO struct {
	Name string `json:"name,omitempty"`
}

// MleOutputDTO define a saída do comando MLE (finalização de carga de mídia).
type MleOutputDTO struct {
	Status string `json:"status"`
}

// MliInputDTO define a entrada para o comando MLI (início de carga de mídia).
type MliInputDTO struct {
	Name   string `json:"name"`
	Size   int64  `json:"size"`
	CRC    uint16 `json:"crc"`
	Format string `json:"format"`
}

// MliOutputDTO define a saída do comando MLI (início de carga de mídia).
type MliOutputDTO struct {
	Status string `json:"status"`
}

// MlrInputDTO define a entrada para o comando MLR (envio de bloco de mídia).
type MlrInputDTO struct {
	Data string `json:"data"`
}

// MlrOutputDTO define a saída do comando MLR (envio de bloco de mídia).
type MlrOutputDTO struct {
	Status string `json:"status"`
}

// MnuInputDTO define a entrada para o comando MNU (exibição de menu).
type MnuInputDTO struct {
	Title   string   `json:"title,omitempty"`
	Items   []string `json:"items"`
	Timeout int      `json:"timeout,omitempty"`
}

// MnuOutputDTO define a saída do comando MNU (exibição de menu).
type MnuOutputDTO struct {
	Selection string `json:"selection,omitempty"`
	Status    string `json:"status"`
}

// OpnInputDTO define a entrada para o comando OPN (abertura de comunicação).
type OpnInputDTO struct {
	Secure bool `json:"secure,omitempty"`
}

// OpnOutputDTO define a saída do comando OPN (abertura de comunicação).
type OpnOutputDTO struct {
	Status string `json:"status"`
	State  string `json:"state"`
}

// QrcodeInputDTO define a entrada para o comando QRCODE (exibição de QR Code).
type QrcodeInputDTO struct {
	Name string `json:"name"`
	Text string `json:"text"`
	Size int    `json:"size,omitempty"`
}

// QrcodeOutputDTO define a saída do comando QRCODE (exibição de QR Code).
type QrcodeOutputDTO struct {
	Name   string `json:"name"`
	Size   int    `json:"size"`
	Status string `json:"status"`
}

// RstInputDTO define a entrada para o comando RST (reinicialização/reset).
type RstInputDTO struct{}

// RstOutputDTO define a saída do comando RST (reinicialização/reset).
type RstOutputDTO struct {
	Status string `json:"status"`
	State  string `json:"state"`
}

// TleInputDTO define a entrada para o comando TLE (finalização de carga de tabela EMV).
type TleInputDTO struct {
	Version string `json:"version"`
}

// TleOutputDTO define a saída do comando TLE (finalização de carga de tabela EMV).
type TleOutputDTO struct {
	Status string `json:"status"`
}

// TliInputDTO define a entrada para o comando TLI (início de carga de tabela EMV).
type TliInputDTO struct {
	Acquirer string `json:"acquirer"`
	Version  string `json:"version"`
}

// TliOutputDTO define a saída do comando TLI (início de carga de tabela EMV).
type TliOutputDTO struct {
	Status string `json:"status"`
}

// TlrInputDTO define a entrada para o comando TLR (envio de registros de tabela EMV).
type TlrInputDTO struct {
	Records []string `json:"records"`
}

// TlrOutputDTO define a saída do comando TLR (envio de registros de tabela EMV).
type TlrOutputDTO struct {
	Status string `json:"status"`
}
