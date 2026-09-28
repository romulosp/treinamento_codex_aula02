package parser

import (
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"errors"
	"reflect"
	"testing"
)

func TestMultimediaFileNamesMatchPublishedLMFVector(t *testing.T) {
	data := []byte{
		0x80, 0x5e, 0x00, 0x08, 'S', 'I', 'G', 'N', 'A', 'L', 'S', ' ',
		0x80, 0x5e, 0x00, 0x08, 'P', 'R', 'E', 'S', 'T', 'O', ' ', ' ',
		0x80, 0x5e, 0x00, 0x08, 'Q', 'R', 'C', 'O', 'D', 'E', '0', '1',
		0x80, 0x5e, 0x00, 0x08, 'F', 'E', 'E', 'D', 'B', 'A', 'C', 'K',
		0x80, 0x5e, 0x00, 0x08, 'M', 'O', 'V', 'N', 'P', 'I', 'C', 'T',
	}
	payload := append([]byte("LMF000060"), data...)
	response, err := ParseAbecsResponse(payload)
	if err != nil {
		t.Fatal(err)
	}
	names, err := MultimediaFileNames(response)
	if err != nil {
		t.Fatal(err)
	}
	want := []string{"SIGNALS", "PRESTO", "QRCODE01", "FEEDBACK", "MOVNPICT"}
	if !reflect.DeepEqual(names, want) {
		t.Fatalf("LMF names = %#v, want %#v", names, want)
	}
	tag := uint16(protocol.TagMultimediaFileName)
	if got := len(response.RawTagValues[tag]); got != len(want) {
		t.Fatalf("repeated PP_MFNAME count = %d, want %d", got, len(want))
	}
	response.RawTags[tag][0] = 'X'
	if response.RawTagValues[tag][0][0] != 'S' {
		t.Fatal("RawTags and RawTagValues share mutable storage")
	}
	lastValue := append([]byte(nil), response.RawTags[tag]...)
	response.RawTagValues[tag][1][0] = 'X'
	if !reflect.DeepEqual(response.RawTags[tag], lastValue) {
		t.Fatal("mutating repeated value unexpectedly changed last RawTags value")
	}
}

func TestMultimediaFileNamesAcceptEmptyListAndRejectMalformedNames(t *testing.T) {
	empty, err := ParseAbecsResponse([]byte("LMF000000"))
	if err != nil {
		t.Fatal(err)
	}
	if names, err := MultimediaFileNames(empty); err != nil || len(names) != 0 {
		t.Fatalf("empty LMF list = %#v, err=%v", names, err)
	}
	if _, err := MultimediaFileNames(nil); !errors.Is(err, domainerror.ErrInvalidResponse) {
		t.Fatalf("nil response error = %v", err)
	}

	for _, test := range []struct {
		name  string
		value []byte
	}{
		{name: "short A8", value: []byte("SHORT")},
		{name: "internal space", value: []byte("AB CDEFG")},
		{name: "only padding", value: []byte("        ")},
		{name: "non ASCII", value: []byte{'A', 'B', 'C', 'D', 'E', 'F', 'G', 0xff}},
	} {
		t.Run(test.name, func(t *testing.T) {
			response := &model.Response{RawTagValues: map[uint16][][]byte{
				uint16(protocol.TagMultimediaFileName): {test.value},
			}}
			if _, err := MultimediaFileNames(response); !errors.Is(err, domainerror.ErrInvalidResponse) {
				t.Fatalf("malformed name error = %v", err)
			}
		})
	}
}
