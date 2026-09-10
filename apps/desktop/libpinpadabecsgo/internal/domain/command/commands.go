package command

import "context"
import "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"

type Type string

const (
	CommandCAN Type = "CAN"
	CommandOPN Type = "OPN"
	CommandCLO Type = "CLO"
	CommandGIX Type = "GIX"
	CommandDSP Type = "DSP"
	CommandDEX Type = "DEX"
	CommandMNU Type = "MNU"
	CommandDSI Type = "DSI"
	CommandMLI Type = "MLI"
	CommandMLR Type = "MLR"
	CommandMLE Type = "MLE"
	CommandTLI Type = "TLI"
	CommandTLR Type = "TLR"
	CommandTLE Type = "TLE"
	CommandGCX Type = "GCX"
	CommandGOX Type = "GOX"
	CommandFCX Type = "FCX"
	CommandGKY Type = "GKY"
	CommandGPN Type = "GPN"
	CommandRST Type = "RST"
)

type Command struct {
	ID      string
	Type    Type
	Execute func(context.Context) (*model.Response, error)
	// Result, when provided, receives the asynchronous result of Enqueue.
	Result chan<- Result
}

type Result struct {
	Response *model.Response
	Err      error
}
