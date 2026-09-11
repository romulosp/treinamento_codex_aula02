// Package logging fornece o logger estruturado e o rastro serial redigido da
// biblioteca ABECS. O tracer é opt-in e nunca deve receber dados sensíveis em
// claro.
package logging

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"fmt"
	"log/slog"
	"os"
	"strings"
	"sync"
	"time"
)

// New cria o logger estruturado padrão da biblioteca.
func New() *slog.Logger {
	return slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{Level: slog.LevelInfo}))
}

// Tracer grava o rastro textual SPE/PP/RSP de uma conexão serial. Seu valor
// recém-criado não possui arquivo de destino e, portanto, não produz I/O até
// SetLogDestination ser chamado. Os métodos são seguros para uso concorrente.
type Tracer struct {
	mu          sync.Mutex
	destination *os.File
	session     string
	nextSession uint64
}

// NewTracer cria um Tracer desabilitado. A composição do executável pode
// configurá-lo e compartilhá-lo entre Service e Adapter antes de abrir a porta.
func NewTracer() *Tracer {
	return &Tracer{}
}

// SetLogDestination configura o arquivo de rastro em modo append. Um caminho
// vazio desabilita o tracer e fecha o arquivo anterior. Em erro de abertura, o
// destino anterior é preservado; o valor booleano informa se a reconfiguração
// foi concluída com sucesso.
func (t *Tracer) SetLogDestination(filename string) (bool, error) {
	if t == nil {
		return false, fmt.Errorf("tracer is nil")
	}
	filename = strings.TrimSpace(filename)
	if filename == "" {
		t.mu.Lock()
		previous := t.destination
		t.destination = nil
		t.mu.Unlock()
		if previous == nil {
			return true, nil
		}
		if err := previous.Close(); err != nil {
			return false, fmt.Errorf("close trace destination: %w", err)
		}
		return true, nil
	}

	candidate, err := os.OpenFile(filename, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0o600)
	if err != nil {
		return false, fmt.Errorf("open trace destination: %w", err)
	}

	t.mu.Lock()
	previous := t.destination
	t.destination = candidate
	t.mu.Unlock()
	if previous != nil {
		if err := previous.Close(); err != nil {
			return true, fmt.Errorf("close previous trace destination: %w", err)
		}
	}
	return true, nil
}

// Close desabilita o tracer e libera o arquivo de destino atual. O encerramento
// da sessão serial deve ser registrado antes desta chamada por RecordClose.
func (t *Tracer) Close() error {
	if t == nil {
		return nil
	}
	_, err := t.SetLogDestination("")
	return err
}

// RecordOpen inicia uma nova sessão lógica após a abertura física da porta e
// registra seus parâmetros 8N1. A numeração só avança quando a abertura teve
// êxito, preservando a correlação até RecordClose.
func (t *Tracer) RecordOpen(port string, baud int) {
	if t == nil {
		return
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	port = safeText(port)
	if port == "" {
		port = "PORT"
	}
	t.nextSession++
	t.session = fmt.Sprintf("%s#%03d", port, t.nextSession)
	t.writeLineLocked("serial.Adapter.Open", "[%s] open(%s,%d,8,N,1)=>OK", t.session, port, baud)
}

// RecordOpenFailure registra uma tentativa de abertura que não criou uma
// sessão. O marcador OPEN distingue essa falha de uma conexão bem-sucedida.
func (t *Tracer) RecordOpenFailure(port string, baud int, err error) {
	if t == nil {
		return
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	port = safeText(port)
	if port == "" {
		port = "PORT"
	}
	t.writeLineLocked("serial.Adapter.Open", "[%s#OPEN] open(%s,%d,8,N,1)=>ERRO: %s", port, port, baud, safeError(err))
}

// RecordClose registra o fechamento da sessão atual. Quando a porta devolve
// erro, a linha preserva a sessão e descreve a falha sem expor dados de tráfego.
func (t *Tracer) RecordClose(err error) {
	if t == nil {
		return
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	id := t.sessionIDLocked("")
	if err != nil {
		t.writeLineLocked("serial.Adapter.Close", "[%s] close()=>ERRO: %s", id, safeError(err))
	} else {
		t.writeLineLocked("serial.Adapter.Close", "[%s] close()", id)
	}
	t.session = ""
}

// RecordSPE registra bytes enviados após a confirmação de escrita pelo
// adaptador. kind vazio representa byte de controle sem comando tipado.
func (t *Tracer) RecordSPE(kind command.Type, data []byte) {
	t.RecordSPEFrom(kind, data, "logging.Tracer.RecordSPE")
}

// RecordSPEFrom registra bytes enviados depois que a função informada confirma
// a escrita física. O nome da função permite diferenciar o ponto de envio sem
// depender de inspeção de pilha em tempo de execução.
func (t *Tracer) RecordSPEFrom(kind command.Type, data []byte, function string) {
	if t == nil {
		return
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	payload := tracePayload(kind, data)
	if kind == "" {
		t.writeLineLocked(function, "[%s] SPE %s", t.sessionIDLocked(""), payload)
		return
	}
	t.writeLineLocked(function, "[%s] SPE %s CMD=%s", t.sessionIDLocked(""), payload, safeText(string(kind)))
}

// RecordPP registra bytes retornados pelo pinpad a cada leitura do adaptador.
// O tipo de comando ativo permite aplicar redação integral antes da escrita.
func (t *Tracer) RecordPP(kind command.Type, data []byte) {
	t.RecordPPFrom(kind, data, "logging.Tracer.RecordPP")
}

// RecordPPFrom registra bytes recebidos no ponto de leitura indicado. A linha
// é emitida para cada retorno não vazio do driver, incluindo ACK, NAK e EOT.
func (t *Tracer) RecordPPFrom(kind command.Type, data []byte, function string) {
	if t == nil || len(data) == 0 {
		return
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	t.writeLineLocked(function, "[%s] PP  %s", t.sessionIDLocked(""), tracePayload(kind, data))
}

// RecordResponse correlaciona uma resposta ABECS já interpretada com o comando
// que a originou. Não repete os bytes registrados em SPE ou PP.
func (t *Tracer) RecordResponse(kind command.Type, status string) {
	if t == nil || kind == "" || status == "" {
		return
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	t.writeLineLocked("service.exchangeCommand", "[%s] RSP CMD=%s STATUS=%s", t.sessionIDLocked(""), safeText(string(kind)), safeText(status))
}

// RecordError registra uma falha de I/O do adaptador com o identificador da
// sessão ativa ou, se ainda não houver sessão, com a porta que originou a falha.
func (t *Tracer) RecordError(port, operation string, err error) {
	if t == nil || err == nil {
		return
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	function := "serial.Adapter." + safeText(operation)
	t.writeLineLocked(function, "[%s] %s()=>ERRO: %s", t.sessionIDLocked(port), safeText(operation), safeError(err))
}

// RedactPayload devolve um marcador auditável para comandos que podem conter
// PAN, trilhas, PIN, KSN, chaves ou dados EMV sensíveis.
func RedactPayload(kind command.Type, data []byte) string {
	switch kind {
	case command.CommandGCX, command.CommandGTK, command.CommandGOX, command.CommandFCX, command.CommandGPN:
		return fmt.Sprintf("**REDACTED(%d bytes)**", len(data))
	default:
		return fmt.Sprintf("%X", data)
	}
}

func (t *Tracer) sessionIDLocked(port string) string {
	if t.session != "" {
		return t.session
	}
	port = safeText(port)
	if port == "" {
		port = "PORT"
	}
	return port + "#000"
}

func (t *Tracer) writeLineLocked(function, format string, args ...any) {
	if t.destination == nil {
		return
	}
	line := fmt.Sprintf(format, args...)
	timestamp := time.Now().Format(time.RFC3339Nano)
	_, _ = fmt.Fprintf(t.destination, "%s FUNC=%s DATA_HORA=%s\n", line, safeText(function), timestamp)
}

func tracePayload(kind command.Type, data []byte) string {
	if isSensitive(kind) {
		return RedactPayload(kind, data)
	}
	if len(data) == 0 {
		return ""
	}
	const hexadecimal = "0123456789ABCDEF"
	var builder strings.Builder
	builder.Grow(len(data)*3 - 1)
	for index, value := range data {
		if index > 0 {
			builder.WriteByte(' ')
		}
		builder.WriteByte(hexadecimal[value>>4])
		builder.WriteByte(hexadecimal[value&0x0F])
	}
	return builder.String()
}

func isSensitive(kind command.Type) bool {
	switch kind {
	case command.CommandGCX, command.CommandGTK, command.CommandGOX, command.CommandFCX, command.CommandGPN:
		return true
	default:
		return false
	}
}

func safeError(err error) string {
	if err == nil {
		return ""
	}
	return safeText(err.Error())
}

func safeText(value string) string {
	return strings.Map(func(character rune) rune {
		if character == '\r' || character == '\n' || character == '\t' {
			return ' '
		}
		return character
	}, value)
}
