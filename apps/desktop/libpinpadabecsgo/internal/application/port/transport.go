// Package port define as portas de aplicação usadas pelos casos de uso ABECS.
package port

import "context"

// Transport representa um fluxo bidirecional de bytes e seu ciclo de vida.
// Implementações podem usar serial, TCP ou outro meio, sem expor a tecnologia
// ao serviço ABECS.
type Transport interface {
	Open() error
	Close() error
	Read(context.Context) ([]byte, error)
	Write([]byte) error
	IsOpen() bool
}
