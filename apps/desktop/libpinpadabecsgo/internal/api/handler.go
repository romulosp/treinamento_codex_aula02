package api

import (
	"context"
	"crypto/rand"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"log/slog"
	"net/http"
	"strconv"
	"strings"
	"sync/atomic"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/api/dto"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/service"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

// Handler expõe os endpoints HTTP da API RESTful para a fachada PinpadService.
type Handler struct {
	svc            service.PinpadService
	logger         *slog.Logger
	defaultTimeout time.Duration
	reqCounter     uint32
}

// NewHandler constrói uma instância do adaptador REST com suas dependências.
func NewHandler(svc service.PinpadService, logger *slog.Logger, defaultTimeout time.Duration) *Handler {
	if logger == nil {
		logger = slog.Default()
	}
	if defaultTimeout <= 0 {
		defaultTimeout = 60 * time.Second
	}
	return &Handler{
		svc:            svc,
		logger:         logger,
		defaultTimeout: defaultTimeout,
	}
}

// Routes registra e devolve o roteador HTTP com todas as rotas e middlewares.
func (h *Handler) Routes() http.Handler {
	mux := http.NewServeMux()

	mux.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		h.writeNotFound(w, r)
	})

	// Rotas canônicas (/api/v1/...)
	h.registerRoute(mux, "/api/v1/connections", map[string]http.HandlerFunc{
		http.MethodPost: h.handleOPN,
	})
	h.registerRoute(mux, "/api/v1/connections/current", map[string]http.HandlerFunc{
		http.MethodDelete: h.handleCLO,
	})
	h.registerRoute(mux, "/api/v1/connections/current/cancellations", map[string]http.HandlerFunc{
		http.MethodPost: h.handleCAN,
	})
	h.registerRoute(mux, "/api/v1/connections/current/resets", map[string]http.HandlerFunc{
		http.MethodPost: h.handleRST,
	})
	h.registerRoute(mux, "/api/v1/pinpad", map[string]http.HandlerFunc{
		http.MethodGet: h.handleGIX,
	})
	h.registerRoute(mux, "/api/v1/display/message", map[string]http.HandlerFunc{
		http.MethodPut: h.handleDSP,
	})
	h.registerRoute(mux, "/api/v1/display/exchanges", map[string]http.HandlerFunc{
		http.MethodPost: h.handleDEX,
	})
	h.registerRoute(mux, "/api/v1/display/menus", map[string]http.HandlerFunc{
		http.MethodPost: h.handleMNU,
	})
	h.registerRoute(mux, "/api/v1/display/clear", map[string]http.HandlerFunc{
		http.MethodPost: h.handleCLX,
	})
	h.registerRoute(mux, "/api/v1/display/images", map[string]http.HandlerFunc{
		http.MethodPost: h.handleDSI,
	})
	h.registerRoute(mux, "/api/v1/media/qrcodes", map[string]http.HandlerFunc{
		http.MethodPost: h.handleQRCODE,
	})
	h.registerRoute(mux, "/api/v1/media", map[string]http.HandlerFunc{
		http.MethodGet:    h.handleLMF,
		http.MethodDelete: h.handleDMF,
	})
	h.registerRoute(mux, "/api/v1/media-loads", map[string]http.HandlerFunc{
		http.MethodPost: h.handleMLI,
	})
	h.registerRoute(mux, "/api/v1/media-loads/current/blocks", map[string]http.HandlerFunc{
		http.MethodPost: h.handleMLR,
	})
	h.registerRoute(mux, "/api/v1/media-loads/current/commit", map[string]http.HandlerFunc{
		http.MethodPost: h.handleMLE,
	})
	h.registerRoute(mux, "/api/v1/table-loads", map[string]http.HandlerFunc{
		http.MethodPost: h.handleTLI,
	})
	h.registerRoute(mux, "/api/v1/table-loads/current/records", map[string]http.HandlerFunc{
		http.MethodPost: h.handleTLR,
	})
	h.registerRoute(mux, "/api/v1/table-loads/current/commit", map[string]http.HandlerFunc{
		http.MethodPost: h.handleTLE,
	})
	h.registerRoute(mux, "/api/v1/card-captures", map[string]http.HandlerFunc{
		http.MethodPost: h.handleGCX,
	})
	h.registerRoute(mux, "/api/v1/card-tracks", map[string]http.HandlerFunc{
		http.MethodPost: h.handleGTK,
	})
	h.registerRoute(mux, "/api/v1/emv/continuations", map[string]http.HandlerFunc{
		http.MethodPost: h.handleGOX,
	})
	h.registerRoute(mux, "/api/v1/emv/finalizations", map[string]http.HandlerFunc{
		http.MethodPost: h.handleFCX,
	})
	h.registerRoute(mux, "/api/v1/key-captures", map[string]http.HandlerFunc{
		http.MethodPost: h.handleGKY,
	})
	h.registerRoute(mux, "/api/v1/pin-captures", map[string]http.HandlerFunc{
		http.MethodPost: h.handleGPN,
	})

	// Rotas de conveniência (/api/...) para suporte a runners e testes
	h.registerRoute(mux, "/api/opn", map[string]http.HandlerFunc{
		http.MethodPost: h.handleOPN,
	})
	h.registerRoute(mux, "/api/can", map[string]http.HandlerFunc{
		http.MethodPost: h.handleCAN,
	})
	h.registerRoute(mux, "/api/clo", map[string]http.HandlerFunc{
		http.MethodPost:   h.handleCLO,
		http.MethodDelete: h.handleCLO,
	})
	h.registerRoute(mux, "/api/clx", map[string]http.HandlerFunc{
		http.MethodPost: h.handleCLX,
	})
	h.registerRoute(mux, "/api/gix", map[string]http.HandlerFunc{
		http.MethodGet: h.handleGIX,
	})
	h.registerRoute(mux, "/api/dsp", map[string]http.HandlerFunc{
		http.MethodPost: h.handleDSP,
		http.MethodPut:  h.handleDSP,
	})
	h.registerRoute(mux, "/api/dex", map[string]http.HandlerFunc{
		http.MethodPost: h.handleDEX,
	})
	h.registerRoute(mux, "/api/mnu", map[string]http.HandlerFunc{
		http.MethodPost: h.handleMNU,
	})
	h.registerRoute(mux, "/api/mli", map[string]http.HandlerFunc{
		http.MethodPost: h.handleMLI,
	})
	h.registerRoute(mux, "/api/mlr", map[string]http.HandlerFunc{
		http.MethodPost: h.handleMLR,
	})
	h.registerRoute(mux, "/api/mle", map[string]http.HandlerFunc{
		http.MethodPost: h.handleMLE,
	})
	h.registerRoute(mux, "/api/lmf", map[string]http.HandlerFunc{
		http.MethodGet: h.handleLMF,
	})
	h.registerRoute(mux, "/api/dmf", map[string]http.HandlerFunc{
		http.MethodPost:   h.handleDMF,
		http.MethodDelete: h.handleDMF,
	})
	h.registerRoute(mux, "/api/dsi", map[string]http.HandlerFunc{
		http.MethodPost: h.handleDSI,
	})
	h.registerRoute(mux, "/api/qrcode", map[string]http.HandlerFunc{
		http.MethodPost: h.handleQRCODE,
	})
	h.registerRoute(mux, "/api/gcx", map[string]http.HandlerFunc{
		http.MethodPost: h.handleGCX,
	})
	h.registerRoute(mux, "/api/gtk", map[string]http.HandlerFunc{
		http.MethodPost: h.handleGTK,
	})
	h.registerRoute(mux, "/api/gox", map[string]http.HandlerFunc{
		http.MethodPost: h.handleGOX,
	})
	h.registerRoute(mux, "/api/fcx", map[string]http.HandlerFunc{
		http.MethodPost: h.handleFCX,
	})
	h.registerRoute(mux, "/api/gky", map[string]http.HandlerFunc{
		http.MethodPost: h.handleGKY,
	})
	h.registerRoute(mux, "/api/gpn", map[string]http.HandlerFunc{
		http.MethodPost: h.handleGPN,
	})
	h.registerRoute(mux, "/api/tli", map[string]http.HandlerFunc{
		http.MethodPost: h.handleTLI,
	})
	h.registerRoute(mux, "/api/tlr", map[string]http.HandlerFunc{
		http.MethodPost: h.handleTLR,
	})
	h.registerRoute(mux, "/api/tle", map[string]http.HandlerFunc{
		http.MethodPost: h.handleTLE,
	})
	h.registerRoute(mux, "/api/rst", map[string]http.HandlerFunc{
		http.MethodPost: h.handleRST,
	})

	return h.middleware(mux)
}

func (h *Handler) registerRoute(mux *http.ServeMux, path string, methodHandlers map[string]http.HandlerFunc) {
	allowedMethods := make([]string, 0, len(methodHandlers))
	for m := range methodHandlers {
		allowedMethods = append(allowedMethods, m)
	}
	allowHeader := strings.Join(allowedMethods, ", ")

	mux.HandleFunc(path, func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path != path {
			h.writeNotFound(w, r)
			return
		}
		handler, exists := methodHandlers[r.Method]
		if !exists {
			w.Header().Set("Allow", allowHeader)
			h.writeError(w, r, http.StatusMethodNotAllowed, "ERR_METHOD_NOT_ALLOWED", "method not allowed", fmt.Sprintf("allowed methods: %s", allowHeader))
			return
		}
		handler(w, r)
	})
}

func (h *Handler) middleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		corrID := r.Header.Get("X-Correlation-ID")
		if corrID == "" {
			corrID = r.Header.Get("X-Request-ID")
		}
		if corrID == "" {
			seq := atomic.AddUint32(&h.reqCounter, 1)
			var b [4]byte
			_, _ = rand.Read(b[:])
			corrID = fmt.Sprintf("req-%d-%d-%s", time.Now().UnixMilli(), seq, hex.EncodeToString(b[:]))
		}

		ctx := context.WithValue(r.Context(), correlationKey{}, corrID)
		w.Header().Set("X-Correlation-ID", corrID)
		w.Header().Set("Content-Type", "application/json; charset=utf-8")

		// Rejeita conexões WebSocket
		if strings.EqualFold(r.Header.Get("Upgrade"), "websocket") {
			h.writeErrorWithCtx(w, ctx, http.StatusBadRequest, "ERR_WEBSOCKET_UNSUPPORTED", "WebSocket is not supported", "")
			return
		}

		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

type correlationKey struct{}

func (h *Handler) getCorrelationID(ctx context.Context) string {
	if val, ok := ctx.Value(correlationKey{}).(string); ok && val != "" {
		return val
	}
	return "unknown"
}

func (h *Handler) writeJSON(w http.ResponseWriter, status int, payload any) {
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(payload)
}

func (h *Handler) writeError(w http.ResponseWriter, r *http.Request, status int, code, message, details string) {
	h.writeErrorWithCtx(w, r.Context(), status, code, message, details)
}

func (h *Handler) writeErrorWithCtx(w http.ResponseWriter, ctx context.Context, status int, code, message, details string) {
	corrID := h.getCorrelationID(ctx)
	resp := dto.ErrorResponse{
		Error: dto.ErrorBody{
			Code:          code,
			Module:        "lib-pinpad-abecs-go",
			Message:       message,
			Details:       details,
			CorrelationID: corrID,
		},
	}
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(resp)
}

func (h *Handler) writeNotFound(w http.ResponseWriter, r *http.Request) {
	h.writeError(w, r, http.StatusNotFound, "ERR_NOT_FOUND", "endpoint not found", fmt.Sprintf("route %s is not registered", r.URL.Path))
}

func (h *Handler) mapDomainError(w http.ResponseWriter, r *http.Request, err error) {
	if err == nil {
		return
	}
	if errors.Is(err, context.DeadlineExceeded) || errors.Is(err, domainerror.ErrTimeout) {
		h.writeError(w, r, http.StatusGatewayTimeout, "ERR_PINPAD_TIMEOUT", "pinpad operation timed out", "")
		return
	}
	if errors.Is(err, domainerror.ErrPinpadClosed) {
		h.writeError(w, r, http.StatusConflict, "ERR_PINPAD_CLOSED", "pinpad is closed", "")
		return
	}
	if errors.Is(err, domainerror.ErrPinpadBusy) {
		h.writeError(w, r, http.StatusConflict, "ERR_PINPAD_BUSY", "pinpad is busy", "")
		return
	}
	if errors.Is(err, domainerror.ErrQueueFull) {
		h.writeError(w, r, http.StatusServiceUnavailable, "ERR_QUEUE_FULL", "command queue is full", "")
		return
	}
	if errors.Is(err, domainerror.ErrPortUnavailable) || errors.Is(err, domainerror.ErrPortNotConfigured) {
		h.writeError(w, r, http.StatusServiceUnavailable, "ERR_PORT_UNAVAILABLE", "serial port is unavailable", "")
		return
	}
	if errors.Is(err, domainerror.ErrNotImplemented) {
		h.writeError(w, r, http.StatusNotImplemented, "ERR_NOT_IMPLEMENTED", "command not implemented", "")
		return
	}
	if errors.Is(err, domainerror.ErrQRCodeGeneratorNotConfigured) {
		h.writeError(w, r, http.StatusNotImplemented, "ERR_QRCODE_NOT_CONFIGURED", "qr code generator not configured", "")
		return
	}
	if errors.Is(err, domainerror.ErrUnsupportedMedia) {
		h.writeError(w, r, http.StatusBadRequest, "ERR_UNSUPPORTED_MEDIA", "unsupported media format", "")
		return
	}
	h.writeError(w, r, http.StatusInternalServerError, "ERR_INTERNAL", err.Error(), "")
}

func (h *Handler) decodeJSON(r *http.Request, target any) error {
	if r.Body == nil {
		return nil
	}
	defer r.Body.Close()
	body, err := io.ReadAll(io.LimitReader(r.Body, 1048576))
	if err != nil {
		return fmt.Errorf("read request body: %w", err)
	}
	if len(strings.TrimSpace(string(body))) == 0 {
		return nil
	}
	if err := json.Unmarshal(body, target); err != nil {
		return fmt.Errorf("invalid JSON payload: %w", err)
	}
	return nil
}

// Handlers para cada um dos 25 comandos

func (h *Handler) handleOPN(w http.ResponseWriter, r *http.Request) {
	var in dto.OpnInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	var err error
	if in.Secure {
		privKey, _, genErr := protocol.GenerateSecureOPN()
		if genErr != nil {
			h.writeError(w, r, http.StatusInternalServerError, "ERR_KEY_GEN", "failed to generate secure OPN key", "")
			return
		}
		err = h.svc.OpenSecure(ctx, privKey)
	} else {
		err = h.svc.Open(ctx)
	}
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.OpnOutputDTO{
		Status: "000",
		State:  "open",
	})
}

func (h *Handler) handleCLO(w http.ResponseWriter, r *http.Request) {
	var in dto.CloInputDTO
	_ = h.decodeJSON(r, &in)

	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	if err := h.svc.Close(ctx); err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.CloOutputDTO{Status: "000"})
}

func (h *Handler) handleCAN(w http.ResponseWriter, r *http.Request) {
	var in dto.CanInputDTO
	_ = h.decodeJSON(r, &in)

	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	if err := h.svc.Reset(ctx); err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.CanOutputDTO{Status: "000"})
}

func (h *Handler) handleRST(w http.ResponseWriter, r *http.Request) {
	var in dto.RstInputDTO
	_ = h.decodeJSON(r, &in)

	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	if err := h.svc.Reset(ctx); err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.RstOutputDTO{Status: "000", State: "open"})
}

func (h *Handler) handleGIX(w http.ResponseWriter, r *http.Request) {
	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	info, err := h.svc.GetInfo(ctx)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	caps, _ := h.svc.GetDisplayCapabilities(ctx)
	out := dto.GixOutputDTO{
		SerialNumber: info.SerialNumber,
		Model:        info.Model,
		Manufacturer: info.Manufacturer,
	}
	if caps != nil {
		out.GraphicWidth = caps.GraphicWidth
		out.GraphicHeight = caps.GraphicHeight
		var fmts []string
		if caps.SupportsPNG {
			fmts = append(fmts, "PNG")
		}
		if caps.SupportsJPG {
			fmts = append(fmts, "JPG")
		}
		if caps.SupportsGIF {
			fmts = append(fmts, "GIF")
		}
		out.SupportedFormats = strings.Join(fmts, ",")
	}
	h.writeJSON(w, http.StatusOK, out)
}

func (h *Handler) handleDSP(w http.ResponseWriter, r *http.Request) {
	var in dto.DspInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	line1 := ""
	line2 := ""
	if len(in.Lines) > 0 {
		line1 = in.Lines[0]
		if len(in.Lines) > 1 {
			line2 = in.Lines[1]
		}
	} else if in.Message != "" {
		parts := strings.Split(in.Message, "\n")
		line1 = parts[0]
		if len(parts) > 1 {
			line2 = parts[1]
		}
	}

	timeout := h.defaultTimeout
	if in.Timeout > 0 {
		timeout = time.Duration(in.Timeout) * time.Second
	}
	ctx, cancel := context.WithTimeout(r.Context(), timeout)
	defer cancel()

	_, err := h.svc.DisplayDSP(ctx, line1, line2)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.DspOutputDTO{Status: "000"})
}

func (h *Handler) handleDEX(w http.ResponseWriter, r *http.Request) {
	var in dto.DexInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	resp, err := h.svc.DisplayDEX(ctx, in.Data)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.DexOutputDTO{
		Status: resp.StatusCode,
		Data:   string(resp.RawData),
	})
}

func (h *Handler) handleMNU(w http.ResponseWriter, r *http.Request) {
	var in dto.MnuInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	timeout := 45
	if in.Timeout > 0 {
		timeout = in.Timeout
	}
	ctx, cancel := context.WithTimeout(r.Context(), time.Duration(timeout+5)*time.Second)
	defer cancel()

	title := in.Title
	if title == "" {
		title = "MENU"
	}
	resp, err := h.svc.DisplayMNU(ctx, timeout, title, in.Items)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.MnuOutputDTO{
		Selection: strconv.Itoa(resp.SelectedIndex),
		Status:    resp.Status,
	})
}

func (h *Handler) handleCLX(w http.ResponseWriter, r *http.Request) {
	var in dto.ClxInputDTO
	_ = h.decodeJSON(r, &in)

	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	_, err := h.svc.CloseVisual(ctx, command.CLXRequest{
		Message:   in.Message,
		MediaName: in.MediaName,
	})
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.ClxOutputDTO{Status: "000"})
}

func (h *Handler) handleDSI(w http.ResponseWriter, r *http.Request) {
	var in dto.DsiInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	if in.Name == "" {
		h.writeError(w, r, http.StatusBadRequest, "ERR_VALIDATION", "image name is required", "")
		return
	}
	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	_, err := h.svc.DisplayImage(ctx, in.Name)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.DsiOutputDTO{
		Status: "000",
		Name:   in.Name,
	})
}

func (h *Handler) handleQRCODE(w http.ResponseWriter, r *http.Request) {
	var in dto.QrcodeInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	if in.Text == "" {
		h.writeError(w, r, http.StatusBadRequest, "ERR_VALIDATION", "text is required", "")
		return
	}
	name := in.Name
	if name == "" {
		name = "QRCODE01"
	}
	size := in.Size
	if size <= 0 {
		size = 200
	}
	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	err := h.svc.LoadQRCodeMultimedia(ctx, name, in.Text, size, nil)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	_, _ = h.svc.DisplayImage(ctx, name)
	h.writeJSON(w, http.StatusOK, dto.QrcodeOutputDTO{
		Name:   name,
		Size:   size,
		Status: "000",
	})
}

func (h *Handler) handleLMF(w http.ResponseWriter, r *http.Request) {
	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	names, err := h.svc.ListMultimediaFiles(ctx)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.LmfOutputDTO{Names: names})
}

func (h *Handler) handleDMF(w http.ResponseWriter, r *http.Request) {
	var in dto.DmfInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	_, err := h.svc.DeleteMultimediaFiles(ctx, in.Names)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.DmfOutputDTO{
		Status:  "000",
		Deleted: in.Names,
	})
}

func (h *Handler) handleMLI(w http.ResponseWriter, r *http.Request) {
	var in dto.MliInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	h.writeJSON(w, http.StatusOK, dto.MliOutputDTO{Status: "000"})
}

func (h *Handler) handleMLR(w http.ResponseWriter, r *http.Request) {
	var in dto.MlrInputDTO
	_ = h.decodeJSON(r, &in)

	h.writeJSON(w, http.StatusOK, dto.MlrOutputDTO{Status: "000"})
}

func (h *Handler) handleMLE(w http.ResponseWriter, r *http.Request) {
	var in dto.MleInputDTO
	_ = h.decodeJSON(r, &in)

	h.writeJSON(w, http.StatusOK, dto.MleOutputDTO{Status: "000"})
}

func (h *Handler) handleTLI(w http.ResponseWriter, r *http.Request) {
	var in dto.TliInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	_, err := h.svc.TableLoadInitiate(ctx, in.Acquirer, in.Version)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.TliOutputDTO{Status: "000"})
}

func (h *Handler) handleTLR(w http.ResponseWriter, r *http.Request) {
	var in dto.TlrInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	_, err := h.svc.TableLoadRecord(ctx, in.Records)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.TlrOutputDTO{Status: "000"})
}

func (h *Handler) handleTLE(w http.ResponseWriter, r *http.Request) {
	var in dto.TleInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	ctx, cancel := context.WithTimeout(r.Context(), h.defaultTimeout)
	defer cancel()

	_, err := h.svc.TableLoadEnd(ctx, in.Version)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.TleOutputDTO{Status: "000"})
}

func (h *Handler) handleGCX(w http.ResponseWriter, r *http.Request) {
	var in dto.GcxInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	if in.Amount == "" {
		h.writeError(w, r, http.StatusBadRequest, "ERR_VALIDATION", "amount is required", "")
		return
	}
	if in.Date == "" {
		in.Date = time.Now().Format("060102")
	}
	if in.Time == "" {
		in.Time = time.Now().Format("150405")
	}
	enableCTLS := true
	hideAmount := false
	if len(in.Options) > 0 {
		if in.Options[0] == '0' {
			enableCTLS = false
		}
	}
	ctx, cancel := context.WithTimeout(r.Context(), 120*time.Second)
	defer cancel()

	resp, err := h.svc.PurchaseGCX(ctx, in.Amount, in.Date, in.Time, enableCTLS, hideAmount)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.GcxOutputDTO{
		CardType:     resp.CardType,
		ICCStatus:    resp.ICCStatus,
		AidTableInfo: resp.AidTableInfo,
		PAN:          resp.PAN,
		Track1:       resp.Track1,
		Track2:       resp.Track2,
		EMVData:      hex.EncodeToString(resp.EMVData),
	})
}

func (h *Handler) handleGTK(w http.ResponseWriter, r *http.Request) {
	var in dto.GtkInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	req := command.GTKRequest{
		Tracks:     strings.Join(in.Tracks, ""),
		DataMethod: in.DataMethod,
		KeyIndex:   in.KeyIndex,
	}
	ctx, cancel := context.WithTimeout(r.Context(), 60*time.Second)
	defer cancel()

	resp, err := h.svc.GetTracks(ctx, req)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.GtkOutputDTO{
		Track1:       hex.EncodeToString(resp.Track1),
		Track2:       hex.EncodeToString(resp.Track2),
		Track3:       hex.EncodeToString(resp.Track3),
		EncryptedPAN: hex.EncodeToString(resp.EncryptedPAN),
	})
}

func (h *Handler) handleGOX(w http.ResponseWriter, r *http.Request) {
	var in dto.GoxInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	wk, _ := hex.DecodeString(in.WorkingKey)
	req := command.GOXRequest{
		AcquirerReference: in.AcquirerReference,
		PinMethod:         in.PinMethod,
		KeyIndex:          in.KeyIndex,
		Amount:            in.Amount,
		WorkingKey:        wk,
	}
	ctx, cancel := context.WithTimeout(r.Context(), 60*time.Second)
	defer cancel()

	resp, err := h.svc.ContinueEMV(ctx, req)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.GoxOutputDTO{
		Result:   hex.EncodeToString(resp.Result),
		EMVData:  hex.EncodeToString(resp.EMVData),
		PINBlock: hex.EncodeToString(resp.PINBlock),
		KSN:      hex.EncodeToString(resp.KSN),
	})
}

func (h *Handler) handleFCX(w http.ResponseWriter, r *http.Request) {
	var in dto.FcxInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	emv, _ := hex.DecodeString(in.EMVData)
	tags, _ := hex.DecodeString(in.TagList)
	timeoutSec := 60
	var timeoutByte *byte
	if in.Timeout != nil && *in.Timeout > 0 {
		timeoutSec = *in.Timeout
		b := byte(*in.Timeout)
		timeoutByte = &b
	}
	req := command.FCXRequest{
		Options:       in.Options,
		Authorization: in.Authorization,
		EMVData:       emv,
		TagList:       tags,
		Timeout:       timeoutByte,
	}
	ctx, cancel := context.WithTimeout(r.Context(), time.Duration(timeoutSec+5)*time.Second)
	defer cancel()

	resp, err := h.svc.FinalizeEMV(ctx, req)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.FcxOutputDTO{
		Result:        hex.EncodeToString(resp.Result),
		EMVData:       hex.EncodeToString(resp.EMVData),
		IssuerScripts: hex.EncodeToString(resp.IssuerScripts),
	})
}

func (h *Handler) handleGKY(w http.ResponseWriter, r *http.Request) {
	var in dto.GkyInputDTO
	_ = h.decodeJSON(r, &in)

	timeout := 30
	if in.TimeoutSeconds > 0 {
		timeout = in.TimeoutSeconds
	}
	ctx, cancel := context.WithTimeout(r.Context(), time.Duration(timeout+5)*time.Second)
	defer cancel()

	key, err := h.svc.WaitForKeyPress(ctx, timeout)
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.GkyOutputDTO{
		Key:    fmt.Sprintf("0x%02X", key),
		Status: "000",
	})
}

func (h *Handler) handleGPN(w http.ResponseWriter, r *http.Request) {
	var in dto.GpnInputDTO
	if err := h.decodeJSON(r, &in); err != nil {
		h.writeError(w, r, http.StatusBadRequest, "ERR_INVALID_PAYLOAD", err.Error(), "")
		return
	}
	ctx, cancel := context.WithTimeout(r.Context(), 60*time.Second)
	defer cancel()

	var pinBlock, ksn []byte
	var err error
	if strings.EqualFold(in.Method, "DUKPT") {
		pinBlock, ksn, err = h.svc.SendGPNCommandDUKPT(ctx, in.KeyIndex, in.PAN, in.Message)
	} else {
		wk, _ := hex.DecodeString(in.WorkingKey)
		pinBlock, ksn, err = h.svc.SendGPNCommandMK(ctx, in.KeyIndex, wk, in.PAN, in.Message)
	}
	if err != nil {
		h.mapDomainError(w, r, err)
		return
	}
	h.writeJSON(w, http.StatusOK, dto.GpnOutputDTO{
		PINBlock: hex.EncodeToString(pinBlock),
		KSN:      hex.EncodeToString(ksn),
	})
}
