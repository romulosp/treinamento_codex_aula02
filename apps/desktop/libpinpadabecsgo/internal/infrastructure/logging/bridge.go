package logging

import (
	"strconv"
	"strings"
)

// BridgeEvent contém somente metadados; payload e mensagem de driver são excluídos.
type BridgeEvent struct {
	Event, Phase, Code, CorrelationID, SessionID, Port, Mode string
	PID                                                      int
	CauseCode                                                string
	Endpoint, Source                                         string
	BaudRate                                                 int
	TimeoutMillis                                            int64
}

// RecordBridgeEvent persiste no mesmo rastro serial, delimitando metadados para
// impedir que controles ou valores longos injetem linhas no arquivo.
func (t *Tracer) RecordBridgeEvent(event BridgeEvent) error {
	if t == nil {
		return nil
	}
	quote := func(value string) string {
		value = strings.Map(func(r rune) rune {
			if r < 32 || r == 127 {
				return ' '
			}
			return r
		}, value)
		if len(value) > 128 {
			value = value[:128]
		}
		return strconv.QuoteToASCII(value)
	}
	t.mu.Lock()
	defer t.mu.Unlock()
	return t.writeLineLocked("bridge.event", "BRIDGE event=%s phase=%s code=%s correlationId=%s sessionId=%s port=%s mode=%s pid=%d cause=%s endpoint=%s baud=%d timeoutMs=%d source=%s",
		quote(event.Event), quote(event.Phase), quote(event.Code), quote(event.CorrelationID), quote(event.SessionID),
		quote(event.Port), quote(event.Mode), event.PID, quote(event.CauseCode), quote(event.Endpoint), event.BaudRate, event.TimeoutMillis, quote(event.Source))
}
