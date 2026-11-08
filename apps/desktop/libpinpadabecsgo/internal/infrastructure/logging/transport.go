package logging

import (
	"context"
	"errors"
	"sync"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/port"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

// DetailedTransport acrescenta diagnóstico de pacotes validados ao transporte
// opaco do Bridge. Nunca altera os bytes, executa comandos ou trata retries.
// O transporte subjacente continua responsável pelo rastro bruto redigido.
type DetailedTransport struct {
	port.Transport
	mu             sync.Mutex
	tracer         *Tracer
	sent, received diagnosticStream
	active         command.Type
	secure         bool
}

// NewDetailedTransport envolve um transporte opaco para diagnóstico DETAILED.
// O chamador deve configurar o tracer e manter a redação do transporte original.
func NewDetailedTransport(transport port.Transport, tracer *Tracer) *DetailedTransport {
	return &DetailedTransport{Transport: transport, tracer: tracer}
}

// Open apaga o contexto observacional antes de iniciar uma sessão física.
func (t *DetailedTransport) Open() error {
	t.reset()
	return t.Transport.Open()
}

// Close fecha o transporte e apaga cópias privadas e correlação da sessão.
func (t *DetailedTransport) Close() error {
	err := t.Transport.Close()
	t.reset()
	return err
}

// Write observa somente bytes cuja escrita foi confirmada pelo transporte.
func (t *DetailedTransport) Write(data []byte) error {
	if err := t.Transport.Write(data); err != nil {
		t.mu.Lock()
		t.sent.reset()
		t.received.reset()
		t.active, t.secure = "", true
		t.mu.Unlock()
		return err
	}
	return t.observe(data, false)
}

// Read devolve o mesmo chunk do transporte; o buffer diagnóstico é separado.
func (t *DetailedTransport) Read(ctx context.Context) ([]byte, error) {
	data, err := t.Transport.Read(ctx)
	traceErr := t.observe(data, true)
	return data, errors.Join(err, traceErr)
}

func (t *DetailedTransport) reset() {
	t.mu.Lock()
	defer t.mu.Unlock()
	t.sent.reset()
	t.received.reset()
	t.active, t.secure = "", false
}

// observe mantém buffers por direção. Nenhum erro de conteúdo vira erro de
// transporte: apenas falhas de persistência do rastro são propagadas.
func (t *DetailedTransport) observe(data []byte, response bool) error {
	t.mu.Lock()
	defer t.mu.Unlock()
	stream := &t.sent
	if response {
		stream = &t.received
	}
	for _, value := range data {
		if t.secure {
			break
		}
		if len(stream.data) == 0 && value == protocol.PP_DC2 {
			t.secure, t.active = true, ""
			t.sent.reset()
			t.received.reset()
			return t.tracer.recordOpaquePacket("", "", len(data), response, true)
		}
		// Controles de cancelamento/fechamento invalidam a correlação, mas ACK
		// e NAK não criam nem repetem uma transação no observador.
		if len(stream.data) == 0 && (value == protocol.PP_CAN || value == protocol.PP_EOT) {
			t.active = ""
		}
		packet := stream.push(value)
		if packet == nil {
			continue
		}
		err := t.recordPacket(packet, response)
		clear(packet)
		if err != nil {
			return err
		}
	}
	return nil
}

func (t *DetailedTransport) recordPacket(packet []byte, response bool) error {
	if !response {
		t.active = ""
	}
	body, err := protocol.ValidatePacket(packet)
	defer clear(body)
	if err != nil || len(body) < 3 || len(body) > protocol.PacketDataMaxSize {
		return t.tracer.recordOpaquePacket("", "", len(packet), response, false)
	}
	if body[0] == protocol.PP_DC2 {
		t.secure, t.active = true, ""
		t.sent.reset()
		t.received.reset()
		return t.tracer.recordOpaquePacket("", "", len(packet), response, true)
	}
	kind := command.Type(body[:3])
	// OPN com parâmetros negocia comunicação segura. Não expor RSA/CRKSEC
	// nem qualquer pacote subsequente dessa sessão.
	if kind == command.CommandOPN && ((!response && len(body) != 3) || (response && len(body) > 6)) {
		t.secure, t.active = true, ""
		t.sent.reset()
		t.received.reset()
		return t.tracer.recordOpaquePacket(kind, "", len(packet), response, true)
	}
	if response && kind != t.active {
		return t.tracer.recordOpaquePacket("", "", len(packet), response, false)
	}
	if _, known := commandCatalog[kind]; !known {
		return t.tracer.recordOpaquePacket("", "", len(packet), response, false)
	}
	if !response {
		t.active = kind
	}
	if !safeDiagnosticFrame(kind, packet, response) {
		status := ""
		if response && len(body) >= 6 && diagnosticDigits(body[3:6]) {
			status = string(body[3:6])
		}
		return t.tracer.recordOpaquePacket(kind, status, len(packet), response, false)
	}
	if !response {
		t.active = kind
		return t.tracer.RecordSPEFromPolicy(kind, packet, "logging.DetailedTransport.Write", false)
	}
	return t.tracer.RecordPPFromPolicy(kind, packet, "logging.DetailedTransport.Read", false)
}

// recordOpaquePacket recebe apenas envelope validado; nunca recebe payload.
func (t *Tracer) recordOpaquePacket(kind command.Type, status string, size int, response, encrypted bool) error {
	if t == nil {
		return nil
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	direction, class, policy := "SPE", "B", "REDACTED"
	if response {
		direction = "PP"
	}
	if encrypted {
		class, policy = "C", "OPACO/CRIPTOGRAFADO DC2_OU_SESSAO_SEGURA"
	}
	return t.writeLineLocked("logging.DetailedTransport.observe", "[%s] %s ENVELOPE CMD=%s STATUS=%s CLASSE=%s PAYLOAD=%s(%d bytes)", t.sessionIDLocked(""), direction, kind, status, class, policy, size)
}

// diagnosticStream recompõe apenas chunks de um pacote. Após entrada fora de
// enquadramento ou limite excedido, fica opaco até a próxima abertura; nunca
// procura SYN dentro de conteúdo arbitrário para tentar recuperá-lo.
type diagnosticStream struct {
	data         []byte
	crcRemaining int
	blocked      bool
}

func (s *diagnosticStream) reset() {
	clear(s.data)
	s.data = nil
	s.crcRemaining, s.blocked = 0, false
}

func (s *diagnosticStream) push(value byte) []byte {
	if s.blocked {
		return nil
	}
	if len(s.data) == 0 {
		if isVisibleControl([]byte{value}) {
			return nil
		}
		if value != protocol.PP_SYN {
			s.blocked = true
			return nil
		}
		// Uma única alocação limitada evita realocações com cópias abandonadas
		// de chunks privados durante o crescimento do pacote.
		s.data = make([]byte, 0, 2*protocol.PacketDataMaxSize+4)
	}
	if len(s.data) >= 2*protocol.PacketDataMaxSize+4 {
		s.reset()
		s.blocked = true
		return nil
	}
	s.data = append(s.data, value)
	if s.crcRemaining > 0 {
		s.crcRemaining--
		if s.crcRemaining == 0 {
			packet := s.data
			s.data = nil
			return packet
		}
	} else if value == protocol.PP_ETB {
		s.crcRemaining = 2
	}
	return nil
}
