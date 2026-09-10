package parser

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"testing"
)

func TestParseAbecsAck(t *testing.T) {
	r, err := ParseAbecsResponse([]byte{protocol.PP_ACK})
	if err != nil || r.AckType != "ACK" {
		t.Fatalf("%#v %v", r, err)
	}
}
