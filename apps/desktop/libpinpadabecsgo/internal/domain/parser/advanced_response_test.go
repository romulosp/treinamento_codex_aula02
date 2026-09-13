package parser

import (
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

func TestAdvancedResponsesKeepSensitiveFieldsSeparated(t *testing.T) {
	response := &model.Response{RawTags: map[uint16][]byte{
		uint16(protocol.TagEncryptedPAN): {0x01},
		uint16(protocol.TagTrack1):       {0x02},
		uint16(protocol.TagTrack1KSN):    {0x03},
		uint16(protocol.TagGOXResult):    []byte("200000"),
		uint16(protocol.TagPINBlock):     {0x04},
		uint16(protocol.TagKSN):          {0x05},
		uint16(protocol.TagFCXResult):    []byte("000"),
		uint16(protocol.TagISResults):    {0x06},
		uint16(protocol.TagEMVData):      {0x9F, 0x36, 0x02, 0x00, 0x01},
	}}
	gtk := GTKResponseFromResponse(response)
	if len(gtk.Track1) != 1 || len(gtk.Track1KSN) != 1 || len(gtk.EncryptedPAN) != 1 {
		t.Fatalf("GTK response = %#v", gtk)
	}
	gox := GOXResponseFromResponse(response)
	if string(gox.Result) != "200000" || len(gox.PINBlock) != 1 || len(gox.KSN) != 1 || len(gox.ParsedEMVData) != 1 {
		t.Fatalf("GOX response = %#v", gox)
	}
	fcx := FCXResponseFromResponse(response)
	if string(fcx.Result) != "000" || len(fcx.IssuerScripts) != 1 || len(fcx.ParsedEMVData) != 1 {
		t.Fatalf("FCX response = %#v", fcx)
	}
}

func TestAdvancedResponseParsersAcceptNil(t *testing.T) {
	if GTKResponseFromResponse(nil) == nil || GOXResponseFromResponse(nil) == nil || FCXResponseFromResponse(nil) == nil {
		t.Fatal("expected non-nil empty responses")
	}
}

func TestGCXResponseValidationMatchesConditionalABECSFields(t *testing.T) {
	validICC := &model.Response{RawTags: map[uint16][]byte{
		uint16(protocol.TagCardType):          []byte(command.GCXCardICC),
		uint16(protocol.TagAIDTableInfo):      []byte("080301"),
		uint16(protocol.TagPAN):               []byte("4444333322221111"),
		uint16(protocol.TagPANSequenceNumber): []byte("01"),
		uint16(protocol.TagLabel):             []byte("CREDITO"),
	}}
	if _, err := ValidateGCXResponse(validICC); err != nil {
		t.Fatal(err)
	}
	for name, response := range map[string]*model.Response{
		"sem card type": {RawTags: map[uint16][]byte{}},
		"ICC sem PAN": {RawTags: map[uint16][]byte{
			uint16(protocol.TagCardType):          []byte(command.GCXCardICC),
			uint16(protocol.TagAIDTableInfo):      []byte("080301"),
			uint16(protocol.TagPANSequenceNumber): []byte("01"),
			uint16(protocol.TagLabel):             []byte("CREDITO"),
		}},
		"magnetico sem ICC status": {RawTags: map[uint16][]byte{
			uint16(protocol.TagCardType): []byte(command.GCXCardMagnetic),
		}},
	} {
		t.Run(name, func(t *testing.T) {
			if _, err := ValidateGCXResponse(response); err == nil {
				t.Fatal("esperava erro")
			}
		})
	}
}

func TestGOXAndFCXResponseValidation(t *testing.T) {
	goxRequest := command.GOXRequest{PinMethod: "3", TagList: []byte{0x9f, 0x36}}
	if _, err := ValidateGOXResponse(&model.Response{RawTags: map[uint16][]byte{
		uint16(protocol.TagGOXResult): []byte("202000"),
	}}, goxRequest); err == nil {
		t.Fatal("GOX sem PIN block, KSN e EMV deve falhar")
	}
	validGOX := &model.Response{RawTags: map[uint16][]byte{
		uint16(protocol.TagGOXResult): []byte("202000"),
		uint16(protocol.TagPINBlock):  make([]byte, 8),
		uint16(protocol.TagKSN):       make([]byte, 10),
		uint16(protocol.TagEMVData):   {},
	}}
	if _, err := ValidateGOXResponse(validGOX, goxRequest); err != nil {
		t.Fatal(err)
	}
	if _, err := ValidateFCXResponse(&model.Response{RawTags: map[uint16][]byte{
		uint16(protocol.TagFCXResult): []byte("000"),
	}}, command.FCXRequest{TagList: []byte{0x9f}}); err == nil {
		t.Fatal("FCX sem PP_EMVDATA solicitado deve falhar")
	}
	validFCX := &model.Response{RawTags: map[uint16][]byte{
		uint16(protocol.TagFCXResult): []byte("100"),
		uint16(protocol.TagEMVData):   {},
	}}
	if _, err := ValidateFCXResponse(validFCX, command.FCXRequest{TagList: []byte{0x9f}}); err != nil {
		t.Fatal(err)
	}
}

func TestGTKResponseValidation(t *testing.T) {
	dukpt := command.GTKRequest{DataMethod: "40"}
	if _, err := ValidateGTKResponse(&model.Response{RawTags: map[uint16][]byte{
		uint16(protocol.TagTrack1): {1},
	}}, dukpt); err == nil {
		t.Fatal("GTK DUKPT sem KSN deve falhar")
	}
	if _, err := ValidateGTKResponse(&model.Response{RawTags: map[uint16][]byte{
		uint16(protocol.TagTrack1):    {1},
		uint16(protocol.TagTrack1KSN): make([]byte, 10),
	}}, dukpt); err != nil {
		t.Fatal(err)
	}
	if _, err := ValidateGTKResponse(&model.Response{RawTags: map[uint16][]byte{}}, command.GTKRequest{DataMethod: "90"}); err == nil {
		t.Fatal("GTK 90 sem PP_ENCKRAND deve falhar")
	}
}

func TestDeviceInfoAndDisplayCapabilitiesFromGIX(t *testing.T) {
	response := &model.Response{Tags: map[string]string{
		"SerialNumber":     "serial",
		"Model":            "modelo",
		"Manufacturer":     "fabricante",
		"Capabilities":     "12",
		"TextMode":         "0216",
		"GraphicData":      "02400320",
		"SupportedFormats": "111",
	}, RawData: []byte{1}}
	info := DeviceInfoFromResponse(response)
	if info.SerialNumber != "serial" || info.Capabilities != "CTLS,IMG_COLOR" || info.TextRows != 2 || info.TextCols != 16 || info.GraphicWidth != 320 || info.GraphicHeight != 240 {
		t.Fatalf("device info = %#v", info)
	}
	capabilities := DisplayCapabilitiesFromResponse(response)
	if !capabilities.SupportsCTLS || !capabilities.HasGraphic || !capabilities.HasColor || !capabilities.SupportsPNG || !capabilities.SupportsJPG || !capabilities.SupportsGIF {
		t.Fatalf("display capabilities = %#v", capabilities)
	}
	if info := DeviceInfoFromResponse(nil); info.Model != "" {
		t.Fatalf("nil device info = %#v", info)
	}
}

func TestShortResponseHelpersValidateData(t *testing.T) {
	if _, err := ParseMNUResponse([]byte("000")); err == nil {
		t.Fatal("expected truncated MNU error")
	}
	if _, err := ParseMNUResponse([]byte("0003")); err == nil {
		t.Fatal("MNU curto deve falhar")
	}
	var err error
	if _, err = ParseMNUResponse([]byte("00002")); err == nil {
		t.Fatal("MNU sem PP_VALUE deve falhar")
	}
	menu, err := ParseMNUResponse([]byte{0x30, 0x30, 0x30, 0x80, 0x4D, 0x00, 0x02, 0x30, 0x33})
	if err != nil || menu.SelectedIndex != 3 || !IsDisplayResponseSuccess(menu.Status) {
		t.Fatalf("MNU TLV = %#v, %v", menu, err)
	}
	if key, err := ParseGKYStatus("005"); err != nil || key != command.GKYKeyF2 {
		t.Fatalf("GKY F2 = %x, %v", key, err)
	}
	if _, err := ParseGKYStatus("012"); err == nil {
		t.Fatal("timeout não é tecla")
	}
}
