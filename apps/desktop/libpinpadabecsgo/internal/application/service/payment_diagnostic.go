package service

import (
	"context"
	"errors"
	"log/slog"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
)

type operationIDKey struct{}

// WithOperationID associa a correlação de uma chamada sem registrar parâmetros de cartão.
func WithOperationID(ctx context.Context, operationID string) context.Context {
	return context.WithValue(ctx, operationIDKey{}, operationID)
}

// logFallback registra somente motivo normativo, próxima tentativa e interface.
func (s *Service) logFallback(ctx context.Context, reason string, attempt int, mode string) {
	s.mu.RLock()
	logger := s.logger
	s.mu.RUnlock()
	if logger == nil {
		return
	}
	attrs := []slog.Attr{slog.String("motivo", reason), slog.Int("tentativa", attempt), slog.String("interface", mode)}
	if id, ok := ctx.Value(operationIDKey{}).(string); ok {
		attrs = append(attrs, slog.String("correlationId", id))
	}
	logger.LogAttrs(ctx, slog.LevelInfo, "fallback GCX", attrs...)
}

// recordCommand usa apenas comando em whitelist e status N3 tipado; não inspeciona mensagens livres.
func (s *Service) recordCommand(
	ctx context.Context, logger *slog.Logger, kind command.Type, response *model.Response, err error, duration time.Duration,
) {
	if logger == nil {
		return
	}
	name := "OTHER"
	switch kind {
	case command.CommandOPN, command.CommandCLO, command.CommandCAN, command.CommandGCX,
		command.CommandGTK, command.CommandGOX, command.CommandFCX, command.CommandGPN,
		command.CommandGIX, command.CommandGKY, command.CommandDEX, command.CommandDSP,
		command.CommandTLI, command.CommandTLR, command.CommandTLE:
		name = string(kind)
	}
	status := ""
	if response != nil {
		status = response.StatusCode
	}
	var statusErr *domainerror.StatusError
	if errors.As(err, &statusErr) {
		status = statusErr.Code
	}
	if len(status) != 3 || status[0] < '0' || status[0] > '9' ||
		status[1] < '0' || status[1] > '9' || status[2] < '0' || status[2] > '9' {
		status = ""
	}
	attrs := s.paymentMetadata()
	if kind == command.CommandCLO {
		attrs = []slog.Attr{slog.String("fase", "close"), slog.Int("tentativa", 1)}
	}
	if id, ok := ctx.Value(operationIDKey{}).(string); ok && attrs[0].Key != "correlationId" {
		attrs = append(attrs, slog.String("correlationId", id))
	}
	attrs = append(attrs, slog.String("comando", name), slog.String("status", status),
		slog.Bool("sucesso", err == nil), slog.Duration("duracao", duration))
	logger.LogAttrs(ctx, slog.LevelInfo, "comando ABECS concluido", attrs...)
}
