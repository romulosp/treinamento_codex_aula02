package parser

import (
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"errors"
	"strings"
	"testing"
)

func TestParseNotification(t *testing.T) {
	tests := []struct {
		name    string
		payload string
		want    string
		valid   bool
	}{
		{name: "32 byte message", payload: "NTM000032" + strings.Repeat("A", 32), want: strings.Repeat("A", 32), valid: true},
		{name: "empty message", payload: "NTM000000", valid: true},
		{name: "wrong status", payload: "NTM001000"},
		{name: "length mismatch", payload: "NTM000032short"},
		{name: "message too large", payload: "NTM000033" + strings.Repeat("A", 33)},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			got, err := ParseNotification([]byte(test.payload))
			if test.valid {
				if err != nil || got != test.want {
					t.Fatalf("ParseNotification()=%q, %v", got, err)
				}
				return
			}
			if !errors.Is(err, domainerror.ErrInvalidNotification) {
				t.Fatalf("ParseNotification error=%v", err)
			}
		})
	}
}

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
	if _, err := ParseAbecsResponse([]byte("GIX000005\x80\x01\x00\x00A")); err != domainerror.ErrInvalidResponse {
		t.Fatalf("trailing data error = %v", err)
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

func TestParseAbecsResponseSupportsMultipleBlocks(t *testing.T) {
	data := []byte{'G', 'I', 'X', '0', '0', '0', '0', '0', '5', 0x80, 0x01, 0x00, 0x01, 'A', '0', '0', '5', 0x80, 0x03, 0x00, 0x01, 'B'}
	response, err := ParseAbecsResponse(data)
	if err != nil {
		t.Fatal(err)
	}
	if string(response.RawTags[0x8001]) != "A" || string(response.RawTags[0x8003]) != "B" || len(response.Data) != 10 {
		t.Fatalf("multi-block response = %#v", response)
	}
}

func TestParseAbecsResponseRejectsNonTLVCommandData(t *testing.T) {
	if _, err := ParseAbecsResponse([]byte("MNU0000012")); err != domainerror.ErrInvalidResponse {
		t.Fatalf("short response error = %v", err)
	}
}
