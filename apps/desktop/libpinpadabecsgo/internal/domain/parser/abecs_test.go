package parser

import (
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"testing"
)

func TestParseAbecsAck(t *testing.T) {
	r, err := ParseAbecsResponse([]byte{protocol.PP_ACK})
	if err != nil || r.AckType != "ACK" {
		t.Fatalf("%#v %v", r, err)
	}
}

func TestParseAbecsResponseRejectsNAKAndTruncatedTLV(t *testing.T) {
	if _, err := ParseAbecsResponse([]byte{protocol.PP_NAK}); err != domainerror.ErrNakReceived {
		t.Fatalf("NAK error = %v", err)
	}
	if _, err := ParseAbecsResponse([]byte("GIX000004\x80\x01\x00\x02A")); err != domainerror.ErrInvalidResponse {
		t.Fatalf("truncated error = %v", err)
	}
}

func TestParseAbecsResponsePreservesRawTags(t *testing.T) {
	data := []byte{'G', 'C', 'X', '0', '0', '0', '0', '0', '5', 0x80, 0x4f, 0x00, 0x01, '0'}
	response, err := ParseAbecsResponse(data)
	if err != nil {
		t.Fatal(err)
	}
	if got := string(response.RawTags[0x804f]); got != "0" {
		t.Fatalf("card type = %q", got)
	}
}

func TestParseAbecsResponsePreservesShortCommandData(t *testing.T) {
	response, err := ParseAbecsResponse([]byte("MNU0000012"))
	if err != nil || string(response.Data) != "2" || len(response.RawTags) != 0 {
		t.Fatalf("short response = %#v, %v", response, err)
	}
}
