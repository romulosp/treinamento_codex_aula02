package protocol

import (
	"context"
	"errors"
	"testing"
)

type sliceReader struct {
	data []byte
	err  error
}

func (r *sliceReader) NextByte(context.Context) (byte, error) {
	if r.err != nil {
		return 0, r.err
	}
	if len(r.data) == 0 {
		return 0, errors.New("empty reader")
	}
	value := r.data[0]
	r.data = r.data[1:]
	return value, nil
}

func TestSubstitutionRoundTrip(t *testing.T) {
	for _, input := range [][]byte{{}, {0x13}, {0x16}, {0x17}, {0x10, 0x13, 0x16, 0x17}} {
		escaped := ApplySubstitution(input)
		got, err := RemoveSubstitution(escaped)
		if err != nil {
			t.Fatal(err)
		}
		if string(got) != string(input) {
			t.Fatalf("got %x want %x", got, input)
		}
	}
}
func TestBuildAndValidatePacket(t *testing.T) {
	input := []byte("GIX000")
	got, err := ValidatePacket(BuildPacket(input))
	if err != nil {
		t.Fatal(err)
	}
	if string(got) != string(input) {
		t.Fatalf("got %q", got)
	}
}

func TestPayloadBuildersAndDynamicTags(t *testing.T) {
	payload := BuildAbecsPayload("GIX", []byte("abc"))
	if string(payload) != "GIX003abc" {
		t.Fatalf("payload = %q", payload)
	}
	packet := BuildCommand("GIX", []byte("abc"))
	decoded, err := ValidatePacket(packet)
	if err != nil || string(decoded) != "GIX003abc" {
		t.Fatalf("command decoded = %q, %v", decoded, err)
	}
	if TagKSNTDESDescription(2) != TagKSNTDESDescriptionBase+2 || TagKSNTDESData(3) != TagKSNTDESDataBase+3 || TagTableVersion(4) != TagTableVersionBase+4 {
		t.Fatal("dynamic tags did not preserve their bases")
	}
}

func TestReadFullResponseAndInvalidFrames(t *testing.T) {
	packet := BuildPacket([]byte("GIX000"))
	reader := &sliceReader{data: append(packet, 0x99)}
	response, err := ReadFullResponse(context.Background(), reader)
	if err != nil || string(response) != "GIX000" || len(reader.data) != 1 {
		t.Fatalf("response=%q remainder=%x err=%v", response, reader.data, err)
	}
	if _, err := ReadFullResponse(context.Background(), &sliceReader{err: errors.New("read failed")}); err == nil {
		t.Fatal("expected reader error")
	}
	ctx, cancel := context.WithCancel(context.Background())
	cancel()
	if _, err := ReadFullResponse(ctx, &sliceReader{}); !errors.Is(err, context.Canceled) {
		t.Fatalf("canceled read error = %v", err)
	}
	if _, err := ValidatePacket([]byte{PP_SYN, PP_ETB, 0, 0}); err == nil {
		t.Fatal("expected invalid checksum")
	}
	if _, err := RemoveSubstitution([]byte{PP_DC3}); err == nil {
		t.Fatal("expected incomplete substitution")
	}
	if _, err := ValidatePacket([]byte{0}); err == nil {
		t.Fatal("expected invalid frame")
	}
	if AckType([]byte{PP_ACK}) != "ACK" || AckType([]byte{PP_NAK}) != "NAK" || AckType([]byte{PP_EOT}) != "EOT" || AckType([]byte{1, 2}) != "DATA" {
		t.Fatal("unexpected acknowledgement classification")
	}
}
