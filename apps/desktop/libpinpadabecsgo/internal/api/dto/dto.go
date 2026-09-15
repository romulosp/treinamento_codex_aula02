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

type CanInputDTO struct{}
type CanOutputDTO struct {
	Status string `json:"status"`
}
type CloInputDTO struct{}
type CloOutputDTO struct {
	Status string `json:"status"`
}
type ClxInputDTO struct {
	Message   string `json:"message"`
	MediaName string `json:"mediaName,omitempty"`
}
type ClxOutputDTO struct {
	Status string `json:"status"`
}
type DexInputDTO struct {
	Data []byte `json:"data"`
}
type DexOutputDTO struct {
	Data   []byte `json:"data"`
	Status string `json:"status"`
}
type DmfInputDTO struct {
	Names []string `json:"names"`
}
type DmfOutputDTO struct {
	Status  string   `json:"status"`
	Deleted []string `json:"deleted,omitempty"`
}
type DsiInputDTO struct {
	Name string `json:"name"`
}
type DsiOutputDTO struct {
	Status string `json:"status"`
	Name   string `json:"name"`
}
type DspInputDTO struct {
	Message string `json:"message"`
}
type DspOutputDTO struct {
	Status string `json:"status"`
}
type FcxInputDTO struct {
	Options       string `json:"options"`
	Authorization string `json:"authorization,omitempty"`
	EMVData       []byte `json:"emvData,omitempty"`
	TagList       []byte `json:"tagList,omitempty"`
	Timeout       *int   `json:"timeout,omitempty"`
}
type FcxOutputDTO struct {
	Result        []byte `json:"result,omitempty"`
	EMVData       []byte `json:"emvData,omitempty"`
	IssuerScripts []byte `json:"issuerScripts,omitempty"`
}
type GcxInputDTO struct {
	Amount  string `json:"amount"`
	Date    string `json:"date"`
	Time    string `json:"time"`
	Options string `json:"options"`
}
type GcxOutputDTO struct {
	CardType     string `json:"cardType,omitempty"`
	ICCStatus    string `json:"iccStatus,omitempty"`
	AidTableInfo string `json:"aidTableInfo,omitempty"`
	PAN          string `json:"pan,omitempty"`
	Track1       string `json:"track1,omitempty"`
	Track2       string `json:"track2,omitempty"`
	EMVData      []byte `json:"emvData,omitempty"`
}
type GixInputDTO struct{}
type GixOutputDTO struct {
	SerialNumber     string `json:"serialNumber,omitempty"`
	Model            string `json:"model,omitempty"`
	Manufacturer     string `json:"manufacturer,omitempty"`
	GraphicWidth     int    `json:"graphicWidth"`
	GraphicHeight    int    `json:"graphicHeight"`
	SupportedFormats string `json:"supportedFormats,omitempty"`
}
type GkyInputDTO struct {
	TimeoutSeconds int `json:"timeoutSeconds,omitempty"`
}
type GkyOutputDTO struct {
	Key    string `json:"key"`
	Status string `json:"status"`
}
type GoxInputDTO struct {
	AcquirerReference string `json:"acquirerReference"`
	PinMethod         string `json:"pinMethod"`
	KeyIndex          int    `json:"keyIndex"`
	Amount            string `json:"amount"`
	WorkingKey        []byte `json:"workingKey,omitempty"`
}
type GoxOutputDTO struct {
	Result   []byte `json:"result,omitempty"`
	EMVData  []byte `json:"emvData,omitempty"`
	PINBlock []byte `json:"pinBlock,omitempty"`
	KSN      []byte `json:"ksn,omitempty"`
}
type GpnInputDTO struct {
	Method     string `json:"method"`
	KeyIndex   int    `json:"keyIndex"`
	WorkingKey []byte `json:"workingKey,omitempty"`
	PAN        string `json:"pan,omitempty"`
	Message    string `json:"message,omitempty"`
}
type GpnOutputDTO struct {
	PINBlock []byte `json:"pinBlock,omitempty"`
	KSN      []byte `json:"ksn,omitempty"`
}
type GtkInputDTO struct {
	Tracks     string `json:"tracks"`
	DataMethod string `json:"dataMethod"`
	KeyIndex   *int   `json:"keyIndex,omitempty"`
}
type GtkOutputDTO struct {
	Track1       []byte `json:"track1,omitempty"`
	Track2       []byte `json:"track2,omitempty"`
	Track3       []byte `json:"track3,omitempty"`
	EncryptedPAN []byte `json:"encryptedPAN,omitempty"`
}
type LmfInputDTO struct{}
type LmfOutputDTO struct {
	Names []string `json:"names"`
}
type MleInputDTO struct{}
type MleOutputDTO struct {
	Status string `json:"status"`
}
type MliInputDTO struct {
	Name   string `json:"name"`
	Size   int64  `json:"size"`
	CRC    uint16 `json:"crc"`
	Format string `json:"format"`
}
type MliOutputDTO struct {
	Status string `json:"status"`
}
type MlrInputDTO struct {
	Data []byte `json:"data"`
}
type MlrOutputDTO struct {
	Status string `json:"status"`
}
type MnuInputDTO struct {
	Items []string `json:"items"`
}
type MnuOutputDTO struct {
	Selection string `json:"selection,omitempty"`
	Status    string `json:"status"`
}
type OpnInputDTO struct {
	Secure bool `json:"secure,omitempty"`
}
type OpnOutputDTO struct {
	Status string `json:"status"`
	State  string `json:"state"`
}
type QrcodeInputDTO struct {
	Name string `json:"name"`
	Text string `json:"text"`
	Size int    `json:"size,omitempty"`
}
type QrcodeOutputDTO struct {
	Name   string `json:"name"`
	Size   int    `json:"size"`
	Status string `json:"status"`
}
type RstInputDTO struct{}
type RstOutputDTO struct {
	Status string `json:"status"`
	State  string `json:"state"`
}
type TleInputDTO struct {
	Version string `json:"version"`
}
type TleOutputDTO struct {
	Status string `json:"status"`
}
type TliInputDTO struct {
	Acquirer string `json:"acquirer"`
	Version  string `json:"version"`
}
type TliOutputDTO struct {
	Status string `json:"status"`
}
type TlrInputDTO struct {
	Records []string `json:"records"`
}
type TlrOutputDTO struct {
	Status string `json:"status"`
}
