package parser

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"bytes"
	"testing"
)

func TestGCXResponseFromResponseCopiesSensitiveAndEMVData(t *testing.T) {
	emv := []byte{0x9F, 0x36, 0x02, 0x00, 0x01}
	response := &model.Response{RawTags: map[uint16][]byte{
		uint16(protocol.TagCardType):       []byte("03"),
		uint16(protocol.TagPAN):            []byte("masked"),
		uint16(protocol.TagCardholderName): []byte("holder"),
		uint16(protocol.TagEMVData):        emv,
	}}
	got := GCXResponseFromResponse(response)
	if got.CardType != "03" || got.PAN != "masked" || got.CardholderName != "holder" || len(got.ParsedEMVData) != 1 {
		t.Fatalf("GCX response = %#v", got)
	}
	emv[0] = 0
	if bytes.Equal(got.EMVData, emv) {
		t.Fatal("EMV data must be copied defensively")
	}
}

func TestGCXResponseFromResponseHandlesNilAndInvalidEMV(t *testing.T) {
	if got := GCXResponseFromResponse(nil); got == nil || got.ParsedEMVData != nil {
		t.Fatalf("nil response = %#v", got)
	}
	got := GCXResponseFromResponse(&model.Response{RawTags: map[uint16][]byte{uint16(protocol.TagEMVData): {0x9F}}})
	if got.ParsedEMVData == nil || len(got.ParsedEMVData) != 0 {
		t.Fatalf("invalid EMV response = %#v", got)
	}
}
