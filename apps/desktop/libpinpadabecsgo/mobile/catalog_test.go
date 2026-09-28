package mobile

import (
	"strings"
	"testing"
)

func TestDecodeHexRejectsInvalidInputAndAcceptsEmpty(t *testing.T) {
	if value, err := decodeHex("", "key"); err != nil || value != nil {
		t.Fatalf("empty value = %v, %v; want nil, nil", value, err)
	}
	if _, err := decodeHex("not-hex", "key"); err == nil {
		t.Fatal("expected invalid hexadecimal error")
	}
	value, err := decodeHex("0Aff", "key")
	if err != nil || len(value) != 2 || value[0] != 0x0a || value[1] != 0xff {
		t.Fatalf("decoded value = %x, err = %v", value, err)
	}
}

func TestBuildGOXRequestRejectsTimeoutOutsideProtocolRange(t *testing.T) {
	if _, err := buildGOXRequest("", "", 0, "", "", "", "", "", "", "", "", "", "", 256); err == nil {
		t.Fatal("expected GOX timeout validation error")
	}
}

func TestBuildFCXRequestKeepsTypedBinaryInputsOutOfTextFields(t *testing.T) {
	request, err := buildFCXRequest("O", "A", "00ff", "0102", 30)
	if err != nil {
		t.Fatal(err)
	}
	if string(request.EMVData) != "\x00\xff" || string(request.TagList) != "\x01\x02" {
		t.Fatalf("binary fields were not decoded: emv=%x tags=%x", request.EMVData, request.TagList)
	}
}

func TestSensitiveNamesAreNotPartOfAllowedSummaryContract(t *testing.T) {
	allowed := "schemaVersion cardType iccStatus aidTableInfo deviceType label issuerCountry " +
		"encryptedPanBytes track1Bytes track2Bytes track3Bytes track1KsnBytes track2KsnBytes track3KsnBytes " +
		"result emvDataBytes issuerScriptsBytes generatedBytes x y positionWarning"
	for _, forbidden := range []string{"\"pan\":", "\"track1\":", "\"pinBlock\":", "\"ksn\":", "\"workingKey\":", "\"rawData\":"} {
		if strings.Contains(allowed, forbidden) {
			t.Fatalf("forbidden sensitive field %q leaked into summary allowlist", forbidden)
		}
	}
}
