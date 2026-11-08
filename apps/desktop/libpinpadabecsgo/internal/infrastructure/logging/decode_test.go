package logging

import (
	"fmt"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

func TestDecodeFrameDSPCommand(t *testing.T) {
	msg := []byte("0123456789abcdef0123456789abcdef") // 32 bytes
	frame, err := decodeFrame(command.CommandDSP, protocol.BuildCommand("DSP", msg), false)
	if err != nil {
		t.Fatalf("decodeFrame: %v", err)
	}
	if len(frame.fields) != 1 {
		t.Fatalf("fields = %d, want 1", len(frame.fields))
	}
	if frame.fields[0].name != "DSP_MSG" {
		t.Fatalf("field = %q, want DSP_MSG", frame.fields[0].name)
	}
	if frame.fields[0].value != string(msg) {
		t.Fatalf("value = %q, want %q", frame.fields[0].value, msg)
	}
	if !frame.crc.valid {
		t.Fatal("CRC deveria ser válido")
	}
}

func TestDecodeFrameTLVCommand(t *testing.T) {
	params := []command.Parameter{{ID: command.SPEMenuOption, Value: []byte("OPCAO 1")}}
	payload, err := command.BuildABECSPayload(command.CommandMNU, params)
	if err != nil {
		t.Fatalf("BuildABECSPayload: %v", err)
	}
	frame, err := decodeFrame(command.CommandMNU, protocol.BuildPacket(payload), false)
	if err != nil {
		t.Fatalf("decodeFrame: %v", err)
	}
	if len(frame.fields) != 1 {
		t.Fatalf("fields = %d, want 1", len(frame.fields))
	}
	if frame.fields[0].name != "SPE_MNUOPT" {
		t.Fatalf("field = %q, want SPE_MNUOPT", frame.fields[0].name)
	}
	if frame.fields[0].value != "OPCAO 1" {
		t.Fatalf("value = %q, want %q", frame.fields[0].value, "OPCAO 1")
	}
}

func TestDecodeFrameGIXResponse(t *testing.T) {
	body := responseBody("GIX", "000", tagValue{uint16(protocol.TagModel), []byte("P35")})
	frame, err := decodeFrame(command.CommandGIX, protocol.BuildPacket(body), true)
	if err != nil {
		t.Fatalf("decodeFrame: %v", err)
	}
	if len(frame.fields) != 2 {
		t.Fatalf("fields = %d, want 2 (RSP_STAT + PP_MODEL)", len(frame.fields))
	}
	if frame.fields[0].name != "RSP_STAT" || frame.fields[0].value != "000" {
		t.Fatalf("field[0] = %+v", frame.fields[0])
	}
	if frame.fields[1].name != "PP_MODEL" {
		t.Fatalf("field[1].name = %q, want PP_MODEL", frame.fields[1].name)
	}
}

func TestDecodeFrameCRCInvalid(t *testing.T) {
	packet := protocol.BuildCommand("DSP", []byte("0123456789abcdef0123456789abcdef"))
	packet[len(packet)-1] ^= 0xFF
	frame, err := decodeFrame(command.CommandDSP, packet, false)
	if err != nil {
		t.Fatalf("decodeFrame: %v", err)
	}
	if frame.crc.valid {
		t.Fatal("CRC deveria ser inválido após corrupção")
	}
}

func TestDecodeFrameIncomplete(t *testing.T) {
	if _, err := decodeFrame(command.CommandDSP, []byte{0x16, 0x44}, false); err == nil {
		t.Fatal("esperava erro para pacote incompleto")
	}
}

type tagValue struct {
	id    uint16
	value []byte
}

func responseBody(rspID, status string, tags ...tagValue) []byte {
	block := make([]byte, 0, 64)
	for _, tag := range tags {
		block = append(block, byte(tag.id>>8), byte(tag.id), byte(len(tag.value)>>8), byte(len(tag.value)))
		block = append(block, tag.value...)
	}
	body := []byte(rspID + status + fmt.Sprintf("%03d", len(block)))
	return append(body, block...)
}
