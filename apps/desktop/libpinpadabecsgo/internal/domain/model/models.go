package model

import "time"

// PinpadConfig define conexão serial e opções operacionais da fachada.
type PinpadConfig struct {
	Port              string
	BaudRate          int
	Timeout           time.Duration
	AutoLoadEMVTables bool
	AcquirerIndex     string
	TableVersion      string
}

// DefaultConfig devolve a configuração aprovada para uso local inicial.
func DefaultConfig() PinpadConfig {
	return PinpadConfig{Port: "COM7", BaudRate: 19200, Timeout: 30 * time.Second, AcquirerIndex: "00", TableVersion: "TABVER0001"}
}

// PinpadState representa o estado observável da comunicação com o pinpad.
type PinpadState string

// Estados possíveis do ciclo de vida da conexão com o pinpad.
const (
	StateClosed PinpadState = "CLOSED"
	StateOpen   PinpadState = "OPEN"
	StateBusy   PinpadState = "BUSY"
)

// DeviceInfo agrupa as capacidades e versões retornadas por GIX.
type DeviceInfo struct {
	SerialNumber, PartNumber, Model, Manufacturer                                                   string
	Capabilities, OSVersion, Specification, ManufacturerVersion, AbecsVersion, ExtendedAbecsVersion string
	ContactlessCapabilities, KernelVersion, ContactlessVersion                                      string
	MasterCardPaypass, VisaPaypass, Aexp, DiscoverContactless, QPContactless                        string
	TextRows, TextCols                                                                              int
	GraphicWidth, GraphicHeight                                                                     int
	SupportedFormats                                                                                string
	RawData                                                                                         []byte
}

// DisplayCapabilities descreve recursos de texto, gráfico e mídia do display.
type DisplayCapabilities struct {
	TextLines, TextCols, GraphicWidth, GraphicHeight            int
	HasGraphic, HasColor, SupportsPNG, SupportsJPG, SupportsGIF bool
	SupportsCTLS, HasICC, HasMagStripe                          bool
	Model, Manufacturer                                         string
}

// Response preserva o reconhecimento, status e tags recebidos do pinpad.
type Response struct {
	AckType, StatusCode string
	RawData             []byte
	Data                []byte
	Tags                map[string]string
	RawTags             map[uint16][]byte
}

// GCXResponse contém somente dados resultantes da captura GCX.
type GCXResponse struct {
	CardType, ICCStatus, AidTableInfo, PAN, PANSequence                                      string
	Track1, Track2, Track3, CardholderName, Label, IssuerCountry, ExpirationDate, DeviceType string
	EMVData                                                                                  []byte
	ParsedEMVData                                                                            map[uint32][]byte
}

// GTKResponse contém exclusivamente os dados devolvidos por GTK. Seus campos
// podem ser sensíveis e não devem ser reutilizados em GCXResponse. O utilitário
// local pode registrar as trilhas quando o operador escolher o modo em claro.
type GTKResponse struct {
	EncryptedPAN    []byte
	Track1          []byte
	Track2          []byte
	Track3          []byte
	Track1KSN       []byte
	Track2KSN       []byte
	Track3KSN       []byte
	EncryptedPANKey []byte
	EncryptedRandom []byte
}

// GOXResponse representa a continuação EMV e mantém PIN/KSN separados da
// resposta GCX para preservar fronteiras de dados sensíveis.
type GOXResponse struct {
	Result        []byte
	EMVData       []byte
	ParsedEMVData map[uint32][]byte
	PINBlock      []byte
	KSN           []byte
}

// FCXResponse representa a finalização EMV e os resultados de Issuer Scripts
// sem atribuí-los a respostas GCX ou GOX.
type FCXResponse struct {
	Result        []byte
	EMVData       []byte
	ParsedEMVData map[uint32][]byte
	IssuerScripts []byte
}
