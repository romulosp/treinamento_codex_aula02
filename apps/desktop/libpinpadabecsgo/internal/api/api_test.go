package api_test

import (
	"bytes"
	"context"
	"crypto/rsa"
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/api"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/api/dto"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/application/service"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	domainerror "br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/error"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/parser"
)

type mockService struct {
	openFunc                   func(context.Context) error
	closeFunc                  func(context.Context) error
	resetFunc                  func(context.Context) error
	getInfoFunc                func(context.Context) (*model.DeviceInfo, error)
	getDisplayCapabilitiesFunc func(context.Context) (*model.DisplayCapabilities, error)
	displayDSPFunc             func(context.Context, string, string) (*model.Response, error)
	displayDEXFunc             func(context.Context, string) (*model.Response, error)
	displayMNUFunc             func(context.Context, int, string, []string) (parser.MNUResponse, error)
	closeVisualFunc            func(context.Context, command.CLXRequest) (*model.Response, error)
	displayImageFunc           func(context.Context, string) (*model.Response, error)
	loadQRCodeFunc             func(context.Context, string, string, int, service.ProgressFunc) error
	listMultimediaFunc         func(context.Context) ([]string, error)
	deleteMultimediaFunc       func(context.Context, []string) (*model.Response, error)
	tableLoadInitiateFunc      func(context.Context, string, string) (*model.Response, error)
	tableLoadRecordFunc        func(context.Context, []string) (*model.Response, error)
	tableLoadEndFunc           func(context.Context, string) (*model.Response, error)
	purchaseGCXFunc            func(context.Context, string, string, string, bool, bool) (*model.GCXResponse, error)
	getTracksFunc              func(context.Context, command.GTKRequest) (*model.GTKResponse, error)
	continueEMVFunc            func(context.Context, command.GOXRequest) (*model.GOXResponse, error)
	finalizeEMVFunc            func(context.Context, command.FCXRequest) (*model.FCXResponse, error)
	waitKeyPressFunc           func(context.Context, int) (byte, error)
	sendGPNMKFunc              func(context.Context, int, []byte, string, string) ([]byte, []byte, error)
	sendGPNDUKPTFunc           func(context.Context, int, string, string) ([]byte, []byte, error)
	openSecureFunc             func(context.Context, *rsa.PrivateKey) error
	sendCommandFunc            func(context.Context, command.Command) (*model.Response, error)
}

func (m *mockService) Open(ctx context.Context) error {
	if m.openFunc != nil {
		return m.openFunc(ctx)
	}
	return nil
}

func (m *mockService) Close(ctx context.Context) error {
	if m.closeFunc != nil {
		return m.closeFunc(ctx)
	}
	return nil
}

func (m *mockService) Reset(ctx context.Context) error {
	if m.resetFunc != nil {
		return m.resetFunc(ctx)
	}
	return nil
}

func (m *mockService) SendCommand(ctx context.Context, cmd command.Command) (*model.Response, error) {
	if m.sendCommandFunc != nil {
		return m.sendCommandFunc(ctx, cmd)
	}
	return &model.Response{StatusCode: "000"}, nil
}

func (m *mockService) GetInfo(ctx context.Context) (*model.DeviceInfo, error) {
	if m.getInfoFunc != nil {
		return m.getInfoFunc(ctx)
	}
	return &model.DeviceInfo{SerialNumber: "123456", Model: "PPC930", Manufacturer: "GERTEC"}, nil
}

func (m *mockService) GetInfoRaw(ctx context.Context) (*model.Response, error) {
	return &model.Response{StatusCode: "000"}, nil
}

func (m *mockService) GetDisplayCapabilities(ctx context.Context) (*model.DisplayCapabilities, error) {
	if m.getDisplayCapabilitiesFunc != nil {
		return m.getDisplayCapabilitiesFunc(ctx)
	}
	return &model.DisplayCapabilities{GraphicWidth: 128, GraphicHeight: 64, SupportsPNG: true, SupportsJPG: true, SupportsGIF: true}, nil
}

func (m *mockService) CloseVisual(ctx context.Context, req command.CLXRequest) (*model.Response, error) {
	if m.closeVisualFunc != nil {
		return m.closeVisualFunc(ctx, req)
	}
	return &model.Response{StatusCode: "000"}, nil
}

func (m *mockService) OpenSecure(ctx context.Context, key *rsa.PrivateKey) error {
	if m.openSecureFunc != nil {
		return m.openSecureFunc(ctx, key)
	}
	return nil
}

func (m *mockService) GetTracks(ctx context.Context, req command.GTKRequest) (*model.GTKResponse, error) {
	if m.getTracksFunc != nil {
		return m.getTracksFunc(ctx, req)
	}
	return &model.GTKResponse{Track1: []byte("TRACK1"), Track2: []byte("TRACK2")}, nil
}

func (m *mockService) ContinueEMV(ctx context.Context, req command.GOXRequest) (*model.GOXResponse, error) {
	if m.continueEMVFunc != nil {
		return m.continueEMVFunc(ctx, req)
	}
	return &model.GOXResponse{Result: []byte{0x00}}, nil
}

func (m *mockService) FinalizeEMV(ctx context.Context, req command.FCXRequest) (*model.FCXResponse, error) {
	if m.finalizeEMVFunc != nil {
		return m.finalizeEMVFunc(ctx, req)
	}
	return &model.FCXResponse{Result: []byte{0x00}}, nil
}

func (m *mockService) GetState() model.PinpadState {
	return model.StateOpen
}

func (m *mockService) SendMultimediaFile(ctx context.Context, name string, data []byte, progress service.ProgressFunc) error {
	return nil
}

func (m *mockService) LoadQRCodeMultimedia(ctx context.Context, name, text string, size int, progress service.ProgressFunc) error {
	if m.loadQRCodeFunc != nil {
		return m.loadQRCodeFunc(ctx, name, text, size, progress)
	}
	return nil
}

func (m *mockService) DisplayImage(ctx context.Context, name string) (*model.Response, error) {
	if m.displayImageFunc != nil {
		return m.displayImageFunc(ctx, name)
	}
	return &model.Response{StatusCode: "000"}, nil
}

func (m *mockService) ListMultimediaFiles(ctx context.Context) ([]string, error) {
	if m.listMultimediaFunc != nil {
		return m.listMultimediaFunc(ctx)
	}
	return []string{"IMG1", "IMG2"}, nil
}

func (m *mockService) DeleteMultimediaFiles(ctx context.Context, names []string) (*model.Response, error) {
	if m.deleteMultimediaFunc != nil {
		return m.deleteMultimediaFunc(ctx, names)
	}
	return &model.Response{StatusCode: "000"}, nil
}

func (m *mockService) LoadCompleteEMVTable(ctx context.Context, acquirer, version string, records []string, progress service.ProgressFunc) error {
	return nil
}

func (m *mockService) PurchaseGCX(ctx context.Context, amount, date, clock string, ctls, hide bool) (*model.GCXResponse, error) {
	if m.purchaseGCXFunc != nil {
		return m.purchaseGCXFunc(ctx, amount, date, clock, ctls, hide)
	}
	return &model.GCXResponse{CardType: "CHIP", PAN: "123456******1234"}, nil
}

func (m *mockService) DisplayQRCode(ctx context.Context, data string, size, margin, x, y int) (service.QRCodeResult, error) {
	return service.QRCodeResult{PNG: []byte("png"), PositionSupported: true}, nil
}

func (m *mockService) TransactionGCX(ctx context.Context, req service.TransactionGCXRequest) (*model.GCXResponse, error) {
	return nil, domainerror.ErrNotImplemented
}

func (m *mockService) DisplayDSP(ctx context.Context, l1, l2 string) (*model.Response, error) {
	if m.displayDSPFunc != nil {
		return m.displayDSPFunc(ctx, l1, l2)
	}
	return &model.Response{StatusCode: "000"}, nil
}

func (m *mockService) DisplayDEX(ctx context.Context, msg string) (*model.Response, error) {
	if m.displayDEXFunc != nil {
		return m.displayDEXFunc(ctx, msg)
	}
	return &model.Response{StatusCode: "000"}, nil
}

func (m *mockService) DisplayMNU(ctx context.Context, timeout int, title string, options []string) (parser.MNUResponse, error) {
	if m.displayMNUFunc != nil {
		return m.displayMNUFunc(ctx, timeout, title, options)
	}
	return parser.MNUResponse{SelectedIndex: 1, Status: "000"}, nil
}

func (m *mockService) WaitForKeyPress(ctx context.Context, timeout int) (byte, error) {
	if m.waitKeyPressFunc != nil {
		return m.waitKeyPressFunc(ctx, timeout)
	}
	return 0x0D, nil
}

func (m *mockService) TableLoadInitiate(ctx context.Context, acq, ver string) (*model.Response, error) {
	if m.tableLoadInitiateFunc != nil {
		return m.tableLoadInitiateFunc(ctx, acq, ver)
	}
	return &model.Response{StatusCode: "000"}, nil
}

func (m *mockService) TableLoadRecord(ctx context.Context, recs []string) (*model.Response, error) {
	if m.tableLoadRecordFunc != nil {
		return m.tableLoadRecordFunc(ctx, recs)
	}
	return &model.Response{StatusCode: "000"}, nil
}

func (m *mockService) TableLoadEnd(ctx context.Context, ver string) (*model.Response, error) {
	if m.tableLoadEndFunc != nil {
		return m.tableLoadEndFunc(ctx, ver)
	}
	return &model.Response{StatusCode: "000"}, nil
}

func (m *mockService) SendGPNCommandMK(ctx context.Context, keyIdx int, wk []byte, pan, msg string) ([]byte, []byte, error) {
	if m.sendGPNMKFunc != nil {
		return m.sendGPNMKFunc(ctx, keyIdx, wk, pan, msg)
	}
	return []byte{0x12, 0x34}, []byte{0x56, 0x78}, nil
}

func (m *mockService) SendGPNCommandDUKPT(ctx context.Context, keyIdx int, pan, msg string) ([]byte, []byte, error) {
	if m.sendGPNDUKPTFunc != nil {
		return m.sendGPNDUKPTFunc(ctx, keyIdx, pan, msg)
	}
	return []byte{0x12, 0x34}, []byte{0x56, 0x78}, nil
}

func TestAPICanonicalRoutes(t *testing.T) {
	mock := &mockService{}
	h := api.NewHandler(mock, nil, 5*time.Second)
	srv := httptest.NewServer(h.Routes())
	defer srv.Close()

	tests := []struct {
		name       string
		method     string
		path       string
		body       string
		wantStatus int
	}{
		{"OPN normal", http.MethodPost, "/api/v1/connections", `{"secure":false}`, http.StatusOK},
		{"OPN secure", http.MethodPost, "/api/v1/connections", `{"secure":true}`, http.StatusOK},
		{"CLO", http.MethodDelete, "/api/v1/connections/current", `{}`, http.StatusOK},
		{"CAN", http.MethodPost, "/api/v1/connections/current/cancellations", `{}`, http.StatusOK},
		{"RST", http.MethodPost, "/api/v1/connections/current/resets", `{}`, http.StatusOK},
		{"GIX", http.MethodGet, "/api/v1/pinpad", ``, http.StatusOK},
		{"DSP message", http.MethodPut, "/api/v1/display/message", `{"message":"LINHA 1\nLINHA 2"}`, http.StatusOK},
		{"DSP lines", http.MethodPut, "/api/v1/display/message", `{"lines":["L1","L2"]}`, http.StatusOK},
		{"DEX", http.MethodPost, "/api/v1/display/exchanges", `{"data":"test"}`, http.StatusOK},
		{"MNU", http.MethodPost, "/api/v1/display/menus", `{"title":"Menu","items":["A","B"]}`, http.StatusOK},
		{"CLX", http.MethodPost, "/api/v1/display/clear", `{"message":"Obrigado"}`, http.StatusOK},
		{"DSI", http.MethodPost, "/api/v1/display/images", `{"name":"IMG1"}`, http.StatusOK},
		{"QRCODE", http.MethodPost, "/api/v1/media/qrcodes", `{"name":"QR1","text":"https://ex.com","size":100}`, http.StatusOK},
		{"LMF", http.MethodGet, "/api/v1/media", ``, http.StatusOK},
		{"DMF", http.MethodDelete, "/api/v1/media", `{"names":["IMG1"]}`, http.StatusOK},
		{"MLI", http.MethodPost, "/api/v1/media-loads", `{"name":"IMG1","size":100,"crc":1234,"format":"PNG"}`, http.StatusOK},
		{"MLR", http.MethodPost, "/api/v1/media-loads/current/blocks", `{"data":"1234"}`, http.StatusOK},
		{"MLE", http.MethodPost, "/api/v1/media-loads/current/commit", `{}`, http.StatusOK},
		{"TLI", http.MethodPost, "/api/v1/table-loads", `{"acquirer":"00","version":"01"}`, http.StatusOK},
		{"TLR", http.MethodPost, "/api/v1/table-loads/current/records", `{"records":["REC1"]}`, http.StatusOK},
		{"TLE", http.MethodPost, "/api/v1/table-loads/current/commit", `{"version":"01"}`, http.StatusOK},
		{"GCX", http.MethodPost, "/api/v1/card-captures", `{"amount":"000000010000","date":"260915","time":"120000","options":"10000"}`, http.StatusOK},
		{"GTK clear", http.MethodPost, "/api/v1/card-tracks", `{"dataMethod":"clear"}`, http.StatusOK},
		{"GOX", http.MethodPost, "/api/v1/emv/continuations", `{"acquirerReference":"00","pinMethod":"online","keyIndex":1,"amount":"100"}`, http.StatusOK},
		{"FCX", http.MethodPost, "/api/v1/emv/finalizations", `{"options":"00"}`, http.StatusOK},
		{"GKY", http.MethodPost, "/api/v1/key-captures", `{"timeoutSeconds":10}`, http.StatusOK},
		{"GPN DUKPT", http.MethodPost, "/api/v1/pin-captures", `{"method":"DUKPT","keyIndex":1,"pan":"123456789012"}`, http.StatusOK},
		{"GPN MK", http.MethodPost, "/api/v1/pin-captures", `{"method":"MK","keyIndex":1,"pan":"123456789012"}`, http.StatusOK},
	}

	client := srv.Client()
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			var bodyReader io.Reader
			if tt.body != "" {
				bodyReader = bytes.NewBufferString(tt.body)
			}
			req, err := http.NewRequest(tt.method, srv.URL+tt.path, bodyReader)
			if err != nil {
				t.Fatalf("NewRequest error: %v", err)
			}
			req.Header.Set("Content-Type", "application/json")
			req.Header.Set("X-Correlation-ID", "corr-test-123")

			resp, err := client.Do(req)
			if err != nil {
				t.Fatalf("client.Do error: %v", err)
			}
			defer resp.Body.Close()

			if resp.StatusCode != tt.wantStatus {
				body, _ := io.ReadAll(resp.Body)
				t.Fatalf("expected status %d, got %d. Body: %s", tt.wantStatus, resp.StatusCode, string(body))
			}
			if cid := resp.Header.Get("X-Correlation-ID"); cid != "corr-test-123" {
				t.Fatalf("expected X-Correlation-ID corr-test-123, got %s", cid)
			}
		})
	}
}

func TestAPIConvenienceRoutes(t *testing.T) {
	mock := &mockService{}
	h := api.NewHandler(mock, nil, 5*time.Second)
	srv := httptest.NewServer(h.Routes())
	defer srv.Close()

	client := srv.Client()
	endpoints := []string{
		"/api/opn", "/api/can", "/api/clo", "/api/clx", "/api/gix",
		"/api/dsp", "/api/dex", "/api/mnu", "/api/mli", "/api/mlr",
		"/api/mle", "/api/lmf", "/api/dmf", "/api/dsi", "/api/qrcode",
		"/api/gcx", "/api/gtk", "/api/gox", "/api/fcx", "/api/gky",
		"/api/gpn", "/api/tli", "/api/tlr", "/api/tle", "/api/rst",
	}

	for _, ep := range endpoints {
		t.Run(ep, func(t *testing.T) {
			method := http.MethodPost
			if ep == "/api/gix" || ep == "/api/lmf" {
				method = http.MethodGet
			}
			var body io.Reader
			if ep == "/api/dsi" {
				body = bytes.NewBufferString(`{"name":"IMG1"}`)
			} else if ep == "/api/qrcode" {
				body = bytes.NewBufferString(`{"text":"https://example.com"}`)
			} else if ep == "/api/gcx" {
				body = bytes.NewBufferString(`{"amount":"100"}`)
			} else {
				body = bytes.NewBufferString(`{}`)
			}

			req, _ := http.NewRequest(method, srv.URL+ep, body)
			req.Header.Set("Content-Type", "application/json")
			resp, err := client.Do(req)
			if err != nil {
				t.Fatalf("client.Do error: %v", err)
			}
			defer resp.Body.Close()
			if resp.StatusCode != http.StatusOK {
				data, _ := io.ReadAll(resp.Body)
				t.Fatalf("expected 200 for %s, got %d. Body: %s", ep, resp.StatusCode, string(data))
			}
		})
	}
}

func TestMethodNotAllowedWithAllowHeader(t *testing.T) {
	mock := &mockService{}
	h := api.NewHandler(mock, nil, 5*time.Second)
	srv := httptest.NewServer(h.Routes())
	defer srv.Close()

	client := srv.Client()
	req, _ := http.NewRequest(http.MethodPost, srv.URL+"/api/v1/pinpad", nil)
	resp, err := client.Do(req)
	if err != nil {
		t.Fatalf("Do error: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusMethodNotAllowed {
		t.Fatalf("expected 405 Method Not Allowed, got %d", resp.StatusCode)
	}
	allow := resp.Header.Get("Allow")
	if allow != "GET" {
		t.Fatalf("expected Allow header 'GET', got '%s'", allow)
	}

	var errResp dto.ErrorResponse
	if err := json.NewDecoder(resp.Body).Decode(&errResp); err != nil {
		t.Fatalf("failed to decode ErrorResponse: %v", err)
	}
	if errResp.Error.Code != "ERR_METHOD_NOT_ALLOWED" {
		t.Fatalf("expected code ERR_METHOD_NOT_ALLOWED, got %s", errResp.Error.Code)
	}
}

func TestNotFound(t *testing.T) {
	mock := &mockService{}
	h := api.NewHandler(mock, nil, 5*time.Second)
	srv := httptest.NewServer(h.Routes())
	defer srv.Close()

	client := srv.Client()
	req, _ := http.NewRequest(http.MethodGet, srv.URL+"/api/v1/unknown-route", nil)
	resp, err := client.Do(req)
	if err != nil {
		t.Fatalf("Do error: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusNotFound {
		t.Fatalf("expected 404 Not Found, got %d", resp.StatusCode)
	}
	var errResp dto.ErrorResponse
	if err := json.NewDecoder(resp.Body).Decode(&errResp); err != nil {
		t.Fatalf("failed to decode ErrorResponse: %v", err)
	}
	if errResp.Error.Code != "ERR_NOT_FOUND" {
		t.Fatalf("expected code ERR_NOT_FOUND, got %s", errResp.Error.Code)
	}
}

func TestInvalidJSONPayload(t *testing.T) {
	mock := &mockService{}
	h := api.NewHandler(mock, nil, 5*time.Second)
	srv := httptest.NewServer(h.Routes())
	defer srv.Close()

	client := srv.Client()
	req, _ := http.NewRequest(http.MethodPost, srv.URL+"/api/v1/connections", bytes.NewBufferString("invalid json {"))
	resp, err := client.Do(req)
	if err != nil {
		t.Fatalf("Do error: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusBadRequest {
		t.Fatalf("expected 400 Bad Request, got %d", resp.StatusCode)
	}
	var errResp dto.ErrorResponse
	if err := json.NewDecoder(resp.Body).Decode(&errResp); err != nil {
		t.Fatalf("failed to decode ErrorResponse: %v", err)
	}
	if errResp.Error.Code != "ERR_INVALID_PAYLOAD" {
		t.Fatalf("expected ERR_INVALID_PAYLOAD, got %s", errResp.Error.Code)
	}
}

func TestDomainErrorMapping(t *testing.T) {
	tests := []struct {
		name       string
		svcErr     error
		wantStatus int
		wantCode   string
	}{
		{"closed", domainerror.ErrPinpadClosed, http.StatusConflict, "ERR_PINPAD_CLOSED"},
		{"busy", domainerror.ErrPinpadBusy, http.StatusConflict, "ERR_PINPAD_BUSY"},
		{"queue full", domainerror.ErrQueueFull, http.StatusServiceUnavailable, "ERR_QUEUE_FULL"},
		{"port unavail", domainerror.ErrPortUnavailable, http.StatusServiceUnavailable, "ERR_PORT_UNAVAILABLE"},
		{"not implemented", domainerror.ErrNotImplemented, http.StatusNotImplemented, "ERR_NOT_IMPLEMENTED"},
		{"qr unavail", domainerror.ErrQRCodeGeneratorNotConfigured, http.StatusNotImplemented, "ERR_QRCODE_NOT_CONFIGURED"},
		{"unsupported media", domainerror.ErrUnsupportedMedia, http.StatusBadRequest, "ERR_UNSUPPORTED_MEDIA"},
		{"timeout", domainerror.ErrTimeout, http.StatusGatewayTimeout, "ERR_PINPAD_TIMEOUT"},
		{"context timeout", context.DeadlineExceeded, http.StatusGatewayTimeout, "ERR_PINPAD_TIMEOUT"},
		{"generic error", errors.New("something went wrong"), http.StatusInternalServerError, "ERR_INTERNAL"},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			mock := &mockService{
				getInfoFunc: func(ctx context.Context) (*model.DeviceInfo, error) {
					return nil, tt.svcErr
				},
			}
			h := api.NewHandler(mock, nil, 5*time.Second)
			srv := httptest.NewServer(h.Routes())
			defer srv.Close()

			resp, err := srv.Client().Get(srv.URL + "/api/v1/pinpad")
			if err != nil {
				t.Fatalf("client.Get error: %v", err)
			}
			defer resp.Body.Close()

			if resp.StatusCode != tt.wantStatus {
				t.Fatalf("expected status %d, got %d", tt.wantStatus, resp.StatusCode)
			}
			var errResp dto.ErrorResponse
			if err := json.NewDecoder(resp.Body).Decode(&errResp); err != nil {
				t.Fatalf("failed to decode ErrorResponse: %v", err)
			}
			if errResp.Error.Code != tt.wantCode {
				t.Fatalf("expected code %s, got %s", tt.wantCode, errResp.Error.Code)
			}
		})
	}
}

func TestValidationErrorsAndFailures(t *testing.T) {
	mock := &mockService{
		openFunc: func(ctx context.Context) error { return errors.New("open error") },
		closeFunc: func(ctx context.Context) error { return errors.New("close error") },
		resetFunc: func(ctx context.Context) error { return errors.New("reset error") },
		displayDSPFunc: func(ctx context.Context, l1, l2 string) (*model.Response, error) { return nil, errors.New("dsp error") },
		displayDEXFunc: func(ctx context.Context, s string) (*model.Response, error) { return nil, errors.New("dex error") },
		displayMNUFunc: func(ctx context.Context, i int, s string, s2 []string) (parser.MNUResponse, error) { return parser.MNUResponse{}, errors.New("mnu error") },
		closeVisualFunc: func(ctx context.Context, request command.CLXRequest) (*model.Response, error) { return nil, errors.New("clx error") },
		displayImageFunc: func(ctx context.Context, s string) (*model.Response, error) { return nil, errors.New("dsi error") },
		loadQRCodeFunc: func(ctx context.Context, s, s2 string, i int, progress service.ProgressFunc) error { return errors.New("qr error") },
		listMultimediaFunc: func(ctx context.Context) ([]string, error) { return nil, errors.New("lmf error") },
		deleteMultimediaFunc: func(ctx context.Context, s []string) (*model.Response, error) { return nil, errors.New("dmf error") },
		tableLoadInitiateFunc: func(ctx context.Context, s, s2 string) (*model.Response, error) { return nil, errors.New("tli error") },
		tableLoadRecordFunc: func(ctx context.Context, s []string) (*model.Response, error) { return nil, errors.New("tlr error") },
		tableLoadEndFunc: func(ctx context.Context, s string) (*model.Response, error) { return nil, errors.New("tle error") },
		purchaseGCXFunc: func(ctx context.Context, s, s2, s3 string, b, b2 bool) (*model.GCXResponse, error) { return nil, errors.New("gcx error") },
		getTracksFunc: func(ctx context.Context, request command.GTKRequest) (*model.GTKResponse, error) { return nil, errors.New("gtk error") },
		continueEMVFunc: func(ctx context.Context, request command.GOXRequest) (*model.GOXResponse, error) { return nil, errors.New("gox error") },
		finalizeEMVFunc: func(ctx context.Context, request command.FCXRequest) (*model.FCXResponse, error) { return nil, errors.New("fcx error") },
		waitKeyPressFunc: func(ctx context.Context, i int) (byte, error) { return 0, errors.New("gky error") },
		sendGPNMKFunc: func(ctx context.Context, i int, bytes []byte, s, s2 string) ([]byte, []byte, error) { return nil, nil, errors.New("gpn error") },
	}

	h := api.NewHandler(mock, nil, 5*time.Second)
	srv := httptest.NewServer(h.Routes())
	defer srv.Close()
	client := srv.Client()

	errorTests := []struct {
		name       string
		method     string
		path       string
		body       string
		wantStatus int
	}{
		{"DSI missing name", http.MethodPost, "/api/v1/display/images", `{"name":""}`, http.StatusBadRequest},
		{"QRCODE missing text", http.MethodPost, "/api/v1/media/qrcodes", `{"text":""}`, http.StatusBadRequest},
		{"GCX missing amount", http.MethodPost, "/api/v1/card-captures", `{"amount":""}`, http.StatusBadRequest},
		{"DSP invalid payload", http.MethodPut, "/api/v1/display/message", `invalid`, http.StatusBadRequest},
		{"DEX invalid payload", http.MethodPost, "/api/v1/display/exchanges", `invalid`, http.StatusBadRequest},
		{"MNU invalid payload", http.MethodPost, "/api/v1/display/menus", `invalid`, http.StatusBadRequest},
		{"DSI invalid payload", http.MethodPost, "/api/v1/display/images", `invalid`, http.StatusBadRequest},
		{"QRCODE invalid payload", http.MethodPost, "/api/v1/media/qrcodes", `invalid`, http.StatusBadRequest},
		{"DMF invalid payload", http.MethodDelete, "/api/v1/media", `invalid`, http.StatusBadRequest},
		{"MLI invalid payload", http.MethodPost, "/api/v1/media-loads", `invalid`, http.StatusBadRequest},
		{"TLI invalid payload", http.MethodPost, "/api/v1/table-loads", `invalid`, http.StatusBadRequest},
		{"TLR invalid payload", http.MethodPost, "/api/v1/table-loads/current/records", `invalid`, http.StatusBadRequest},
		{"TLE invalid payload", http.MethodPost, "/api/v1/table-loads/current/commit", `invalid`, http.StatusBadRequest},
		{"GCX invalid payload", http.MethodPost, "/api/v1/card-captures", `invalid`, http.StatusBadRequest},
		{"GTK invalid payload", http.MethodPost, "/api/v1/card-tracks", `invalid`, http.StatusBadRequest},
		{"GOX invalid payload", http.MethodPost, "/api/v1/emv/continuations", `invalid`, http.StatusBadRequest},
		{"FCX invalid payload", http.MethodPost, "/api/v1/emv/finalizations", `invalid`, http.StatusBadRequest},
		{"GPN invalid payload", http.MethodPost, "/api/v1/pin-captures", `invalid`, http.StatusBadRequest},
		{"OPN failure", http.MethodPost, "/api/v1/connections", `{}`, http.StatusInternalServerError},
		{"CLO failure", http.MethodDelete, "/api/v1/connections/current", `{}`, http.StatusInternalServerError},
		{"CAN failure", http.MethodPost, "/api/v1/connections/current/cancellations", `{}`, http.StatusInternalServerError},
		{"RST failure", http.MethodPost, "/api/v1/connections/current/resets", `{}`, http.StatusInternalServerError},
		{"DSP failure", http.MethodPut, "/api/v1/display/message", `{"message":"test"}`, http.StatusInternalServerError},
		{"DEX failure", http.MethodPost, "/api/v1/display/exchanges", `{"data":"test"}`, http.StatusInternalServerError},
		{"MNU failure", http.MethodPost, "/api/v1/display/menus", `{"items":["A"]}`, http.StatusInternalServerError},
		{"CLX failure", http.MethodPost, "/api/v1/display/clear", `{"message":"test"}`, http.StatusInternalServerError},
		{"DSI failure", http.MethodPost, "/api/v1/display/images", `{"name":"IMG1"}`, http.StatusInternalServerError},
		{"QRCODE failure", http.MethodPost, "/api/v1/media/qrcodes", `{"text":"test"}`, http.StatusInternalServerError},
		{"LMF failure", http.MethodGet, "/api/v1/media", ``, http.StatusInternalServerError},
		{"DMF failure", http.MethodDelete, "/api/v1/media", `{"names":["IMG1"]}`, http.StatusInternalServerError},
		{"TLI failure", http.MethodPost, "/api/v1/table-loads", `{"acquirer":"00","version":"01"}`, http.StatusInternalServerError},
		{"TLR failure", http.MethodPost, "/api/v1/table-loads/current/records", `{"records":["REC1"]}`, http.StatusInternalServerError},
		{"TLE failure", http.MethodPost, "/api/v1/table-loads/current/commit", `{"version":"01"}`, http.StatusInternalServerError},
		{"GCX failure", http.MethodPost, "/api/v1/card-captures", `{"amount":"100"}`, http.StatusInternalServerError},
		{"GTK failure", http.MethodPost, "/api/v1/card-tracks", `{"tracks":[]}`, http.StatusInternalServerError},
		{"GOX failure", http.MethodPost, "/api/v1/emv/continuations", `{"acquirerReference":"00","amount":"100"}`, http.StatusInternalServerError},
		{"FCX failure", http.MethodPost, "/api/v1/emv/finalizations", `{"options":"00"}`, http.StatusInternalServerError},
		{"GKY failure", http.MethodPost, "/api/v1/key-captures", `{"timeoutSeconds":1}`, http.StatusInternalServerError},
		{"GPN failure", http.MethodPost, "/api/v1/pin-captures", `{"method":"MK","pan":"123"}`, http.StatusInternalServerError},
	}

	for _, tt := range errorTests {
		t.Run(tt.name, func(t *testing.T) {
			var bodyReader io.Reader
			if tt.body != "" {
				bodyReader = bytes.NewBufferString(tt.body)
			}
			req, _ := http.NewRequest(tt.method, srv.URL+tt.path, bodyReader)
			req.Header.Set("Content-Type", "application/json")
			resp, err := client.Do(req)
			if err != nil {
				t.Fatalf("client.Do error: %v", err)
			}
			defer resp.Body.Close()
			if resp.StatusCode != tt.wantStatus {
				t.Fatalf("expected status %d, got %d", tt.wantStatus, resp.StatusCode)
			}
		})
	}
}

func TestWebSocketRejection(t *testing.T) {
	mock := &mockService{}
	h := api.NewHandler(mock, nil, 5*time.Second)
	srv := httptest.NewServer(h.Routes())
	defer srv.Close()

	req, _ := http.NewRequest(http.MethodGet, srv.URL+"/api/v1/pinpad", nil)
	req.Header.Set("Upgrade", "websocket")

	resp, err := srv.Client().Do(req)
	if err != nil {
		t.Fatalf("Do error: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusBadRequest {
		t.Fatalf("expected 400 for WebSocket attempt, got %d", resp.StatusCode)
	}
	var errResp dto.ErrorResponse
	if err := json.NewDecoder(resp.Body).Decode(&errResp); err != nil {
		t.Fatalf("failed to decode ErrorResponse: %v", err)
	}
	if errResp.Error.Code != "ERR_WEBSOCKET_UNSUPPORTED" {
		t.Fatalf("expected ERR_WEBSOCKET_UNSUPPORTED, got %s", errResp.Error.Code)
	}
}
