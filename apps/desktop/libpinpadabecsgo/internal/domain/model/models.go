package model

import "time"

type PinpadConfig struct {
	Port                 string
	BaudRate             int
	Timeout              time.Duration
	AutoLoadEMVTables    bool
	UseGCXInitialization bool
	GCXInitTimeout       time.Duration
	AcquirerIndex        string
	TableVersion         string
}

func DefaultConfig() PinpadConfig {
	return PinpadConfig{Port: "COM3", BaudRate: 19200, Timeout: 30 * time.Second, UseGCXInitialization: true, GCXInitTimeout: 5 * time.Second, AcquirerIndex: "00", TableVersion: "TABVER0001"}
}

type PinpadState string

const (
	StateClosed PinpadState = "CLOSED"
	StateOpen   PinpadState = "OPEN"
	StateBusy   PinpadState = "BUSY"
)

type DeviceInfo struct {
	SerialNumber, PartNumber, Model, Manufacturer                                               string
	Capabilities, OSVersion, Specification, ManufacturerVersion, AbecsVersion, ExtendedAbecsVersion string
	ContactlessCapabilities, KernelVersion, ContactlessVersion                                     string
	MasterCardPaypass, VisaPaypass, Aexp, DiscoverContactless, QPContactless                       string
	TextRows, TextCols                                                                              int
	GraphicWidth, GraphicHeight                                                                     int
	SupportedFormats                                                                                string
	RawData                                                                                         []byte
}

type DisplayCapabilities struct {
	TextLines, TextCols, GraphicWidth, GraphicHeight            int
	HasGraphic, HasColor, SupportsPNG, SupportsJPG, SupportsGIF bool
	SupportsCTLS, HasICC, HasMagStripe                          bool
	Model, Manufacturer                                         string
}

type Response struct {
	AckType, StatusCode string
	RawData             []byte
	Tags                map[string]string
}

type GCXResponse struct {
	CardType, ICCStatus, AidTableInfo, PAN, PANSequence                                      string
	Track1, Track2, Track3, CardholderName, Label, IssuerCountry, ExpirationDate, DeviceType string
	EMVData                                                                                  []byte
	ParsedEMVData                                                                            map[uint32][]byte
}
