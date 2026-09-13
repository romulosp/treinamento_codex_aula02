// Package logging fornece o logger estruturado e o rastro serial da biblioteca
// ABECS. O tracer é opt-in, redige frames sensíveis e oferece uma operação
// explícita para o diagnóstico GTK em claro do utilitário local.
package logging

import (
	"errors"
	"fmt"
	"log/slog"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"sync"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
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
	lastErr     error
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
		syncErr := previous.Sync()
		closeErr := previous.Close()
		if syncErr != nil || closeErr != nil {
			err := errors.Join(wrapTraceError("sync trace destination", syncErr), wrapTraceError("close trace destination", closeErr))
			t.rememberError(err)
			return false, err
		}
		return true, nil
	}
	absolute, err := filepath.Abs(filename)
	if err != nil {
		return false, fmt.Errorf("normalize trace destination: %w", err)
	}
	filename = filepath.Clean(absolute)

	candidate, err := os.OpenFile(filename, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0o600)
	if err != nil {
		return false, fmt.Errorf("open trace destination: %w", err)
	}
	if err := writeLine(candidate, "logging.Tracer.SetLogDestination", "TRACE destination=%s enabled", safeText(filename)); err != nil {
		closeErr := candidate.Close()
		return false, errors.Join(fmt.Errorf("activate trace destination: %w", err), wrapTraceError("close failed trace destination", closeErr))
	}

	t.mu.Lock()
	previous := t.destination
	t.destination = candidate
	t.lastErr = nil
	t.mu.Unlock()
	if previous != nil {
		syncErr := previous.Sync()
		closeErr := previous.Close()
		if syncErr != nil || closeErr != nil {
			err := errors.Join(wrapTraceError("sync previous trace destination", syncErr), wrapTraceError("close previous trace destination", closeErr))
			t.rememberError(err)
			return true, err
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

// Err retorna a última falha de persistência observada pelo tracer. O erro é
// limpo somente quando um novo destino é ativado com sucesso.
func (t *Tracer) Err() error {
	if t == nil {
		return nil
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	return t.lastErr
}

// RecordOpen inicia uma nova sessão lógica após a abertura física da porta e
// registra seus parâmetros 8N1. A numeração só avança quando a abertura teve
// êxito, preservando a correlação até RecordClose.
func (t *Tracer) RecordOpen(port string, baud int) error {
	if t == nil {
		return nil
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	port = safeText(port)
	if port == "" {
		port = "PORT"
	}
	t.nextSession++
	t.session = fmt.Sprintf("%s#%03d", port, t.nextSession)
	return t.writeLineLocked("serial.Adapter.Open", "[%s] open(%s,%d,8,N,1)=>OK", t.session, port, baud)
}

// RecordOpenFailure registra uma tentativa de abertura que não criou uma
// sessão. O marcador OPEN distingue essa falha de uma conexão bem-sucedida.
func (t *Tracer) RecordOpenFailure(port string, baud int, err error) error {
	if t == nil {
		return nil
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	port = safeText(port)
	if port == "" {
		port = "PORT"
	}
	return t.writeLineLocked("serial.Adapter.Open", "[%s#OPEN] open(%s,%d,8,N,1)=>ERRO: %s", port, port, baud, safeError(err))
}

// RecordClose registra o fechamento da sessão atual. Quando a porta devolve
// erro, a linha preserva a sessão e descreve a falha sem expor dados de tráfego.
func (t *Tracer) RecordClose(err error) error {
	if t == nil {
		return nil
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	id := t.sessionIDLocked("")
	var traceErr error
	if err != nil {
		traceErr = t.writeLineLocked("serial.Adapter.Close", "[%s] close()=>ERRO: %s", id, safeError(err))
	} else {
		traceErr = t.writeLineLocked("serial.Adapter.Close", "[%s] close()", id)
	}
	t.session = ""
	return traceErr
}

// RecordSPE registra bytes enviados após a confirmação de escrita pelo
// adaptador. kind vazio representa byte de controle sem comando tipado.
func (t *Tracer) RecordSPE(kind command.Type, data []byte) error {
	return t.RecordSPEFrom(kind, data, "logging.Tracer.RecordSPE")
}

// RecordSPEFrom registra bytes enviados depois que a função informada confirma
// a escrita física. O nome da função permite diferenciar o ponto de envio sem
// depender de inspeção de pilha em tempo de execução.
func (t *Tracer) RecordSPEFrom(kind command.Type, data []byte, function string) error {
	return t.RecordSPEFromPolicy(kind, data, function, false)
}

// RecordSPEFromPolicy registra bytes enviados e permite que o ponto de
// composição imponha redação total para comunicação segura ou conteúdo que o
// consumidor classificou como sensível.
func (t *Tracer) RecordSPEFromPolicy(kind command.Type, data []byte, function string, redact bool) error {
	if t == nil {
		return nil
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	if t.destination == nil {
		return nil
	}
	payload := tracePayload(kind, data, redact)
	if kind == "" {
		return t.writeLineLocked(function, "[%s] SPE %s", t.sessionIDLocked(""), payload)
	}
	return t.writeLineLocked(function, "[%s] SPE %s CMD=%s", t.sessionIDLocked(""), payload, safeText(string(kind)))
}

// RecordPP registra bytes retornados pelo pinpad a cada leitura do adaptador.
// O tipo de comando ativo permite aplicar redação integral antes da escrita.
func (t *Tracer) RecordPP(kind command.Type, data []byte) error {
	return t.RecordPPFrom(kind, data, "logging.Tracer.RecordPP")
}

// RecordPPFrom registra bytes recebidos no ponto de leitura indicado. A linha
// é emitida para cada retorno não vazio do driver, incluindo ACK, NAK e EOT.
func (t *Tracer) RecordPPFrom(kind command.Type, data []byte, function string) error {
	return t.RecordPPFromPolicy(kind, data, function, false)
}

// RecordPPFromPolicy registra os bytes de uma leitura física, aplicando a
// política explícita de redação antes de qualquer formatação hexadecimal.
func (t *Tracer) RecordPPFromPolicy(kind command.Type, data []byte, function string, redact bool) error {
	if t == nil || len(data) == 0 {
		return nil
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	if t.destination == nil {
		return nil
	}
	return t.writeLineLocked(function, "[%s] PP  %s", t.sessionIDLocked(""), tracePayload(kind, data, redact))
}

// RecordResponse correlaciona uma resposta ABECS já interpretada com o comando
// que a originou. Não repete os bytes registrados em SPE ou PP.
func (t *Tracer) RecordResponse(kind command.Type, status string) error {
	if t == nil || kind == "" || status == "" {
		return nil
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	return t.writeLineLocked("service.exchangeCommand", "[%s] RSP CMD=%s STATUS=%s", t.sessionIDLocked(""), safeText(string(kind)), safeText(status))
}

// RecordGTKClearTracks registra as três trilhas já interpretadas de uma
// resposta GTK em claro. O chamador deve usar esta exceção somente quando o
// operador selecionar explicitamente o modo em claro no utilitário local.
// QuoteToASCII mantém cada valor delimitado em uma única linha do rastro.
func (t *Tracer) RecordGTKClearTracks(track1, track2, track3 []byte) error {
	if t == nil {
		return nil
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	if t.destination == nil {
		return nil
	}
	decodedTrack2, err := decodeGTKClearNumericTrack(track2)
	if err != nil {
		return fmt.Errorf("decode GTK clear track 2: %w", err)
	}
	decodedTrack3, err := decodeGTKClearNumericTrack(track3)
	if err != nil {
		return fmt.Errorf("decode GTK clear track 3: %w", err)
	}
	return t.writeLineLocked(
		"cmd.libpinpadabecsgo.GetTracks",
		"[%s] GTK_CLEAR TRACK1=%s TRACK2=%s TRACK3=%s",
		t.sessionIDLocked(""),
		strconv.QuoteToASCII(string(track1)),
		strconv.QuoteToASCII(decodedTrack2),
		strconv.QuoteToASCII(decodedTrack3),
	)
}

func decodeGTKClearNumericTrack(encoded []byte) (string, error) {
	var decoded strings.Builder
	decoded.Grow(len(encoded) * 2)
	filler := false
	for _, value := range encoded {
		for _, nibble := range []byte{value >> 4, value & 0x0F} {
			switch {
			case nibble <= 9 && !filler:
				decoded.WriteByte('0' + nibble)
			case nibble == 0x0D && !filler:
				decoded.WriteByte('=')
			case nibble == 0x0F:
				filler = true
			default:
				return "", fmt.Errorf("invalid nibble %X", nibble)
			}
		}
	}
	return decoded.String(), nil
}

// RecordError registra uma falha de I/O do adaptador com o identificador da
// sessão ativa ou, se ainda não houver sessão, com a porta que originou a falha.
func (t *Tracer) RecordError(port, operation string, err error) error {
	if t == nil || err == nil {
		return nil
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	function := "serial.Adapter." + safeText(operation)
	return t.writeLineLocked(function, "[%s] %s()=>ERRO: %s", t.sessionIDLocked(port), safeText(operation), safeError(err))
}

// RedactPayload devolve um marcador auditável para comandos que podem conter
// PAN, trilhas, PIN, KSN, chaves ou dados EMV sensíveis.
func RedactPayload(kind command.Type, data []byte) string {
	switch kind {
	case command.CommandMLR, command.CommandTLR, command.CommandGCX, command.CommandGTK, command.CommandGOX, command.CommandFCX, command.CommandGPN:
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

func (t *Tracer) writeLineLocked(function, format string, args ...any) error {
	if t.destination == nil {
		return nil
	}
	err := writeLine(t.destination, function, format, args...)
	if err != nil {
		t.lastErr = err
	}
	return err
}

func writeLine(destination *os.File, function, format string, args ...any) error {
	line := fmt.Sprintf(format, args...)
	timestamp := time.Now().Format(time.RFC3339Nano)
	if _, err := fmt.Fprintf(destination, "%s FUNC=%s DATA_HORA=%s\n", line, safeText(function), timestamp); err != nil {
		return fmt.Errorf("write trace line: %w", err)
	}
	if err := destination.Sync(); err != nil {
		return fmt.Errorf("sync trace line: %w", err)
	}
	return nil
}

func tracePayload(kind command.Type, data []byte, forceRedaction bool) string {
	if forceRedaction && !isVisibleControl(data) {
		return fmt.Sprintf("**REDACTED(%d bytes)**", len(data))
	}
	if isSensitive(kind) && !isVisibleControl(data) {
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
	case command.CommandMLR, command.CommandTLR, command.CommandGCX, command.CommandGTK, command.CommandGOX, command.CommandFCX, command.CommandGPN:
		return true
	default:
		return false
	}
}

func isVisibleControl(data []byte) bool {
	if len(data) != 1 {
		return false
	}
	switch data[0] {
	case protocol.PP_ACK, protocol.PP_NAK, protocol.PP_EOT, protocol.PP_CAN:
		return true
	default:
		return false
	}
}

func (t *Tracer) rememberError(err error) {
	if t == nil || err == nil {
		return
	}
	t.mu.Lock()
	t.lastErr = err
	t.mu.Unlock()
}

func wrapTraceError(operation string, err error) error {
	if err == nil {
		return nil
	}
	return fmt.Errorf("%s: %w", operation, err)
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
