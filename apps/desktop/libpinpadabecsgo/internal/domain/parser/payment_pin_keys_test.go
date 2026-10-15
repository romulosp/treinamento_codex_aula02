package parser

import (
	"bytes"
	"errors"
	"reflect"
	"testing"

	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

func TestPaymentPINKeyIndexes(t *testing.T) {
	for _, tc := range []struct {
		name    string
		size    int
		flag    byte
		present []int
		invalid bool
	}{
		{"boundaries", 100, '2', []int{0, 23, 99}, false}, {"none", 100, '0', nil, false},
		{"all unsupported", 100, '2', nil, false}, {"singleton", 100, '0', []int{63}, false},
		{"short", 99, '0', nil, true}, {"long", 101, '0', nil, true}, {"binary", 100, 0, nil, true},
		{"unknown flag", 100, '3', nil, true},
	} {
		t.Run(tc.name, func(t *testing.T) {
			flags := bytes.Repeat([]byte{tc.flag}, tc.size)
			for _, i := range tc.present {
				flags[i] = '1'
			}
			response := &model.Response{StatusCode: "000", RawTags: map[uint16][]byte{uint16(protocol.TagDukptTDESDescription): flags,
				uint16(protocol.TagDukptTDESData): bytes.Repeat([]byte{'1'}, 100)}}
			got, err := PaymentPINKeyIndexes(response)
			if tc.invalid {
				if !errors.Is(err, domainerror.ErrInvalidResponse) {
					t.Fatal("invalid map accepted")
				}
				return
			}
			if err != nil || len(got) != len(tc.present) {
				t.Fatal("incorrect candidates")
			}
			if len(got) > 0 && !reflect.DeepEqual(got, tc.present) {
				t.Fatal("DATA/unsupported entered candidates")
			}
			clear(flags)
			if len(got) > 0 && got[0] != tc.present[0] {
				t.Fatal("indexes alias raw response")
			}
		})
	}
	for _, response := range []*model.Response{nil, {}, {StatusCode: "042"}, {StatusCode: "000", RawTags: map[uint16][]byte{uint16(protocol.TagDukptTDESData): bytes.Repeat([]byte{'1'}, 100)}}} {
		if _, err := PaymentPINKeyIndexes(response); !errors.Is(err, domainerror.ErrInvalidResponse) {
			t.Fatal("missing PIN map accepted")
		}
	}
}
