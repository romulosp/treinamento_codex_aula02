package bridgeprotocol

import (
	"bytes"
	"context"
	"encoding/binary"
	"errors"
	"net"
	"testing"
)

func TestEncodeDecodePreservesCorrelationAndPayload(t *testing.T) {
	want := Frame{MessageType: Data, Flags: 2, CorrelationID: "operation-1", Payload: []byte{0x16, 0x17, 0x04}}
	encoded, err := Encode(want)
	if err != nil {
		t.Fatal(err)
	}
	got, err := Decode(encoded)
	if err != nil {
		t.Fatal(err)
	}
	if got.MessageType != want.MessageType || got.Flags != want.Flags || got.CorrelationID != want.CorrelationID || !bytes.Equal(got.Payload, want.Payload) {
		t.Fatalf("decoded frame = %#v, want %#v", got, want)
	}
}

func TestReadFrameSupportsFragmentedStream(t *testing.T) {
	client, server := net.Pipe()
	defer client.Close()
	defer server.Close()
	want := Frame{MessageType: Data, CorrelationID: "fragmented", Payload: []byte("payload")}
	encoded, err := Encode(want)
	if err != nil {
		t.Fatal(err)
	}
	go func() {
		for _, value := range encoded {
			_, _ = server.Write([]byte{value})
		}
	}()
	got, err := ReadFrame(context.Background(), client)
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(got.Payload, want.Payload) {
		t.Fatalf("payload = %q, want %q", got.Payload, want.Payload)
	}
}

func TestDecodeRejectsPayloadOverLimit(t *testing.T) {
	data := make([]byte, headerSize)
	copy(data, []byte(magic))
	data[4] = Version
	data[5] = byte(Data)
	binary.BigEndian.PutUint32(data[10:14], MaxPayload+1)
	if _, err := Decode(data); !errors.Is(err, ErrInvalidFrame) {
		t.Fatalf("error = %v, want ErrInvalidFrame", err)
	}
}
