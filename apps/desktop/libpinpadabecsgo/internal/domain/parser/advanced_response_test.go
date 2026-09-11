package parser

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"testing"
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

func TestDeviceInfoAndDisplayCapabilitiesFromGIX(t *testing.T) {
	response := &model.Response{Tags: map[string]string{
		"SerialNumber":     "serial",
		"Model":            "modelo",
		"Manufacturer":     "fabricante",
		"Capabilities":     "12",
		"TextMode":         "0216",
		"GraphicData":      "03200240",
		"SupportedFormats": "PNG,JPEG,GIF",
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
	menu, err := ParseMNUResponse([]byte("0003"))
	if err != nil || menu.SelectedIndex != 3 || !IsDisplayResponseSuccess(menu.Status) {
		t.Fatalf("MNU = %#v, %v", menu, err)
	}
	if _, err := ParseGKYKey([]byte{0}); err == nil {
		t.Fatal("expected invalid GKY key")
	}
	if key, err := ParseGKYKey(nil); err != nil || key != command.GKYKeyNone {
		t.Fatalf("empty GKY = %x, %v", key, err)
	}
}
