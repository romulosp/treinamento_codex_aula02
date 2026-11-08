package logging

import (
	"fmt"
	"strings"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/state"
)

// SetFormat seleciona LEGACY (inclusive vazio) ou DETAILED e registra a
// configuração efetiva. Valor inválido é rejeitado sem trocar o formato.
func (t *Tracer) SetFormat(value string) (string, error) {
	format := strings.ToUpper(strings.TrimSpace(value))
	if format == "" {
		format = "LEGACY"
	}
	if format != "LEGACY" && format != "DETAILED" {
		return "", fmt.Errorf("PINPAD_LOG_FORMAT deve ser LEGACY ou DETAILED")
	}
	if t == nil {
		return "", fmt.Errorf("tracer is nil")
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	t.detailed = format == "DETAILED"
	return format, t.writeLineLocked("logging.Tracer.SetFormat", "TRACE FORMAT=%s", format)
}

// SetDetailed ativa ou desativa o modo detalhado (HEX + ASCII + interpretação
// campo a campo) para os frames de Classe A. O modo LEGACY permanece o padrão.
func (t *Tracer) SetDetailed(enabled bool) {
	if t == nil {
		return
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	t.detailed = enabled
}

// SetBytesPerLine ajusta o agrupamento do hexdump (bytes por linha).
func (t *Tracer) SetBytesPerLine(n int) {
	if t == nil {
		return
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	if n <= 0 {
		n = 16
	}
	t.bytesPerLine = n
}

// shouldDetailLocked informa se o frame deve ser detalhado. O caller já detém o
// lock do tracer.
func (t *Tracer) shouldDetailLocked(kind command.Type, data []byte, redact, response bool) bool {
	return t.detailed &&
		kind != "" &&
		classify(kind, data, redact) == ClassVisible &&
		safeDiagnosticFrame(kind, data, response)
}

// writeDetailedLocked grava o bloco detalhado preservando sessão, direção, FUNC
// e DATA_HORA na linha-cabeçalho, seguido do corpo indentado.
func (t *Tracer) writeDetailedLocked(function, direction string, kind command.Type, data []byte, isResponse bool) error {
	if t.destination == nil {
		return nil
	}
	timestamp := time.Now().Format(time.RFC3339Nano)
	header := fmt.Sprintf("[%s] %s CMD=%s", t.sessionIDLocked(""), direction, safeText(string(kind)))
	if _, err := fmt.Fprintf(t.destination, "%s FUNC=%s DATA_HORA=%s\n", header, safeText(function), timestamp); err != nil {
		t.lastErr = err
		return fmt.Errorf("write trace header: %w", err)
	}
	for _, line := range strings.Split(formatDetailedBody(kind, data, isResponse, t.bytesPerLine), "\n") {
		if line == "" {
			continue
		}
		if _, err := fmt.Fprintf(t.destination, "  %s\n", line); err != nil {
			t.lastErr = err
			return fmt.Errorf("write trace body: %w", err)
		}
	}
	if err := t.destination.Sync(); err != nil {
		t.lastErr = err
		return fmt.Errorf("sync trace line: %w", err)
	}
	return nil
}

// formatDetailedBody produz o corpo multi-linha (sem a linha-cabeçalho).
func formatDetailedBody(kind command.Type, data []byte, isResponse bool, bytesPerLine int) string {
	var builder strings.Builder
	builder.WriteString("HEX:\n")
	for _, line := range strings.Split(hexDump(data, bytesPerLine), "\n") {
		builder.WriteString("  ")
		builder.WriteString(line)
		builder.WriteString("\n")
	}
	frame, err := decodeFrame(kind, data, isResponse)
	if err != nil {
		fmt.Fprintf(&builder, "DECODE: ERRO (%v)\n", err)
		return builder.String()
	}
	meta := commandMetaFor(kind)
	fmt.Fprintf(&builder, "COMANDO: %s - %s (PDF §%s)\n", meta.name, meta.description, meta.pdf)
	body, _ := protocol.ValidatePacket(data)
	defer clear(body)
	fmt.Fprintf(&builder, "CONTROLE: SYN(16) ... ETB(17); OFFSETS=CORPO_LOGICO; WIRE_BYTES=%d LOGICAL_BYTES=%d\n", len(data), len(body))
	escapes := 0
	for i := 1; i < len(data)-3; i++ {
		if data[i] == protocol.PP_DC3 {
			escapes++
			i++
		}
	}
	fmt.Fprintf(&builder, "DC3: SUBSTITUICOES=%d (13 33/36/37); HEX=BYTES_WIRE; CAMPOS=POS_REMOVE_SUBSTITUTION\n", escapes)
	if len(body) >= 3 {
		prefix := "CMD"
		if isResponse {
			prefix = "RSP"
		}
		fmt.Fprintf(&builder, "  %s_ID: A3 = %q\n", prefix, body[:3])
	}
	if !isResponse && len(body) >= 6 {
		fmt.Fprintf(&builder, "  CMD_LEN1: N3 = %q\n", body[3:6])
	}
	for _, field := range frame.fields {
		builder.WriteString("  ")
		builder.WriteString(field.name)
		encoding := field.encoding
		if encoding == "" {
			encoding = EncUnknown
		}
		ascii := ""
		if field.offset >= 0 && field.offset+field.length <= len(body) {
			ascii = asciiOnly(body[field.offset : field.offset+field.length])
		}
		fmt.Fprintf(&builder, " TIPO=%s OFFSET_INICIAL=%d OFFSET_FINAL=%d COMPRIMENTO_DECLARADO=%d COMPRIMENTO_EFETIVO=%d HEX=%s ASCII=%q CODIFICACAO=%s VALIDACAO=VALIDO", field.format, field.offset, field.offset+field.length-1, field.length, field.length, field.hex, ascii, encoding)
		if field.value != "" {
			fmt.Fprintf(&builder, " VALOR_INTERPRETADO = %q", field.value)
		}
		if field.name == "RSP_STAT" {
			fmt.Fprintf(&builder, " DESCRICAO=%q", state.GetStatusDescription(field.value))
		}
		builder.WriteString("\n")
	}
	fmt.Fprintf(&builder, "CRC: RECEBIDO=0x%04X CALCULADO=0x%04X STATUS=%s\n",
		frame.crc.received, frame.crc.calculated, crcStatus(frame.crc.valid))
	return builder.String()
}

// controlName descreve apenas controles isolados; nunca procura controles
// dentro de conteúdo de payload ou de pacotes ainda incompletos.
func controlName(data []byte) string {
	if len(data) != 1 {
		return ""
	}
	switch data[0] {
	case protocol.PP_ACK:
		return "ACK"
	case protocol.PP_NAK:
		return "NAK"
	case protocol.PP_EOT:
		return "EOT"
	case protocol.PP_CAN:
		return "CAN"
	default:
		return ""
	}
}

// crcStatus devolve o rótulo de validação do CRC.
func crcStatus(valid bool) string {
	if valid {
		return "VALIDO"
	}
	return "INVALIDO"
}
