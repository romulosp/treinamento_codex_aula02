package parser

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"fmt"
)

type MNUResponse struct {
	SelectedIndex int
	Status        string
}

func ParseMNUResponse(data []byte) (MNUResponse, error) {
	if len(data) < 4 {
		return MNUResponse{}, fmt.Errorf("truncated MNU response")
	}
	return MNUResponse{SelectedIndex: int(data[3] - '0'), Status: string(data[:3])}, nil
}
func IsDisplayResponseSuccess(status string) bool { return status == "000" }
func ParseGKYKey(data []byte) (byte, error) {
	if len(data) == 0 {
		return command.GKYKeyNone, nil
	}
	if data[0] < command.GKYKeyOK || data[0] > command.GKYKeyF4 {
		return command.GKYKeyNone, fmt.Errorf("unknown GKY key")
	}
	return data[0], nil
}

func GCXResponseFromResponse(response *model.Response) *model.GCXResponse {
	result := &model.GCXResponse{}
	if response == nil {
		return result
	}
	result.EMVData = append([]byte(nil), response.RawData...)
	return result
}
