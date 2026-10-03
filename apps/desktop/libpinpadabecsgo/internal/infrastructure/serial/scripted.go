package serial

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"strings"
	"sync"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
)

// ScriptStep descreve uma troca binária fechada entre o core e um transporte
// de teste. O adaptador não interpreta comandos; somente compara o transcript.
type ScriptStep struct {
	ExpectedWrite []byte
	Reads         [][]byte
	Optional      bool
}

// ScriptedTransport reproduz um transcript de transporte sem hardware.
// Ele é apropriado para testes de integração do Bridge e não para produção.
type ScriptedTransport struct {
	mu          sync.Mutex
	steps       []ScriptStep
	index       int
	reads       [][]byte
	open        bool
	openErr     error
	closeErr    error
	notify      chan struct{}
	tracer      *logging.Tracer
	traceOpen   bool
	opaqueTrace bool
}

// NewScriptedTransport cria um transporte que compara cada escrita com a
// próxima etapa e disponibiliza os bytes configurados para leitura.
func NewScriptedTransport(steps []ScriptStep) *ScriptedTransport {
	cloned := make([]ScriptStep, len(steps))
	for i, step := range steps {
		cloned[i] = ScriptStep{
			ExpectedWrite: append([]byte(nil), step.ExpectedWrite...),
			Reads:         cloneChunks(step.Reads),
			Optional:      step.Optional,
		}
	}
	return &ScriptedTransport{steps: cloned, notify: make(chan struct{}, 1)}
}

// NewDiagnosticScriptedTransport cria o transcript mínimo OPN/GIX/CLO usado
// pelos testes humanos do fluxo de diagnóstico.
func NewDiagnosticScriptedTransport() *ScriptedTransport {
	return NewDiagnosticScriptedTransportForPort("TEST")
}

// NewDiagnosticScriptedTransportForPort cria o transcript usando a porta
// recebida da configuração efetiva; nenhum nome de COM é fixado no teste.
func NewDiagnosticScriptedTransportForPort(port string) *ScriptedTransport {
	port = strings.TrimSpace(port)
	if port == "" {
		port = "AMBIENTE"
	}
	if len(port) > 10 {
		port = "CONFIG"
	}
	dsp, _ := command.BuildDSPCommand("TESTE ANDROID", "HOST "+port)
	return NewScriptedTransport([]ScriptStep{
		{ExpectedWrite: []byte{protocol.PP_CAN}, Reads: [][]byte{{protocol.PP_EOT}}},
		{ExpectedWrite: protocol.BuildPacket([]byte("OPN")), Reads: [][]byte{{protocol.PP_ACK}, protocol.BuildPacket([]byte("OPN000"))}},
		{ExpectedWrite: protocol.BuildCommand("GIX", nil), Reads: [][]byte{{protocol.PP_ACK}, protocol.BuildPacket([]byte("GIX000"))}, Optional: true},
		{ExpectedWrite: protocol.BuildPacket(dsp), Reads: [][]byte{{protocol.PP_ACK}, protocol.BuildPacket([]byte("DSP000"))}, Optional: true},
		{ExpectedWrite: protocol.BuildCommand("CLO", []byte("                                ")), Reads: [][]byte{{protocol.PP_ACK}, protocol.BuildPacket([]byte("CLO000"))}},
	})
}

// Open inicia a sessão roteirizada.
func (s *ScriptedTransport) Open() error {
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.openErr != nil {
		return s.openErr
	}
	s.open = true
	s.index = 0
	s.reads = nil
	if s.tracer != nil && !s.traceOpen {
		s.traceOpen = true
		if err := s.tracer.RecordOpen("SCRIPTED", 0); err != nil {
			s.open = false
			s.traceOpen = false
			return err
		}
	}
	return nil
}

// Close encerra a sessão roteirizada.
func (s *ScriptedTransport) Close() error {
	s.mu.Lock()
	s.open = false
	err := s.closeErr
	tracer := s.tracer
	traceOpen := s.traceOpen
	s.traceOpen = false
	s.mu.Unlock()
	if tracer != nil && traceOpen {
		err = errors.Join(err, tracer.RecordClose(err))
	}
	s.signal()
	return err
}

// Read devolve o próximo bloco do transcript ou timeout quando ele acabou.
func (s *ScriptedTransport) Read(ctx context.Context) ([]byte, error) {
	if ctx == nil {
		ctx = context.Background()
	}
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	for {
		s.mu.Lock()
		if !s.open {
			s.mu.Unlock()
			return nil, fmt.Errorf("scripted transport is closed")
		}
		if len(s.reads) > 0 {
			chunk := append([]byte(nil), s.reads[0]...)
			tracer := s.tracer
			redact := s.opaqueTrace
			s.reads = s.reads[1:]
			s.mu.Unlock()
			if tracer != nil {
				if err := tracer.RecordPPFromPolicy("", chunk, "serial.ScriptedTransport.Read", redact); err != nil {
					return nil, err
				}
			}
			return chunk, nil
		}
		s.mu.Unlock()
		select {
		case <-ctx.Done():
			return nil, ctx.Err()
		case <-s.notify:
		}
	}
}

// Write compara uma escrita com a etapa atual e enfileira suas respostas.
func (s *ScriptedTransport) Write(data []byte) error {
	s.mu.Lock()
	if !s.open {
		s.mu.Unlock()
		return fmt.Errorf("scripted transport is closed")
	}
	if s.index >= len(s.steps) {
		s.mu.Unlock()
		return fmt.Errorf("unexpected scripted write after transcript end")
	}
	for s.index < len(s.steps) && s.steps[s.index].Optional && !bytes.Equal(data, s.steps[s.index].ExpectedWrite) {
		s.index++
	}
	if s.index >= len(s.steps) {
		s.mu.Unlock()
		return fmt.Errorf("unexpected scripted write after optional steps")
	}
	step := s.steps[s.index]
	if !bytes.Equal(data, step.ExpectedWrite) {
		s.mu.Unlock()
		return fmt.Errorf("scripted write mismatch at step %d", s.index)
	}
	s.index++
	s.reads = append(s.reads, cloneChunks(step.Reads)...)
	tracer := s.tracer
	redact := s.opaqueTrace
	s.mu.Unlock()
	if tracer != nil {
		if err := tracer.RecordSPEFromPolicy("", data, "serial.ScriptedTransport.Write", redact); err != nil {
			return err
		}
	}
	s.signal()
	return nil
}

// SetTracer associa o mesmo rastro do Bridge ao transporte scripted.
func (s *ScriptedTransport) SetTracer(tracer *logging.Tracer) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.tracer = tracer
}

// SetOpaqueTrace redige cada chunk roteirizado quando usado pelo Bridge.
func (s *ScriptedTransport) SetOpaqueTrace(enabled bool) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.opaqueTrace = enabled
}

// IsOpen informa se o transcript está aberto.
func (s *ScriptedTransport) IsOpen() bool {
	s.mu.Lock()
	defer s.mu.Unlock()
	return s.open
}

// Completed informa se todas as escritas esperadas foram observadas.
func (s *ScriptedTransport) Completed() bool {
	s.mu.Lock()
	defer s.mu.Unlock()
	return s.index == len(s.steps)
}

func cloneChunks(chunks [][]byte) [][]byte {
	cloned := make([][]byte, len(chunks))
	for i, chunk := range chunks {
		cloned[i] = append([]byte(nil), chunk...)
	}
	return cloned
}

func (s *ScriptedTransport) signal() {
	select {
	case s.notify <- struct{}{}:
	default:
	}
}
