package command

import "context"
import "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"

// Type identifica um comando ABECS de três caracteres.
type Type string

// Identificadores dos comandos implementados ou formalmente reservados na Change.
const (
	CommandCAN Type = "CAN"
	CommandOPN Type = "OPN"
	CommandCLO Type = "CLO"
	CommandCLX Type = "CLX"
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
	CommandGTK Type = "GTK"
	CommandGOX Type = "GOX"
	CommandFCX Type = "FCX"
	CommandGKY Type = "GKY"
	CommandGPN Type = "GPN"
)

// GTKContract identifica o contrato tipado de obtenção de trilhas, separado
// de GCX para que PAN, trilhas e KSN não sejam misturados ao resultado de
// captura do cartão.
type GTKContract struct{ Type Type }

// CLXContract identifica o comando visual não bloqueante que mantém a porta
// serial aberta e encerra a sessão segura ativa no pinpad.
type CLXContract struct{ Type Type }

// GOXContract identifica a continuação do processamento EMV iniciada por GCX.
type GOXContract struct{ Type Type }

// FCXContract identifica a finalização EMV e seus Issuer Script Results.
type FCXContract struct{ Type Type }

// Command representa uma operação serializada e seu canal opcional de resultado.
type Command struct {
	ID      string
	Type    Type
	Execute func(context.Context) (*model.Response, error)
	// Result, when provided, receives the asynchronous result of Enqueue.
	Result chan<- Result
}

// Result entrega a resposta ou erro de uma operação enfileirada assincronamente.
type Result struct {
	Response *model.Response
	Err      error
}
