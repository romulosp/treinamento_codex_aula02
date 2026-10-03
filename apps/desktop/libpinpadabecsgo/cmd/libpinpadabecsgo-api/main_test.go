package main

import (
	"os"
	"path/filepath"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/infrastructure/logging"
)

func TestGetBindAddress(t *testing.T) {
	origHost := os.Getenv("PINPAD_HTTP_HOST")
	origPort := os.Getenv("PINPAD_HTTP_PORT")
	origP := os.Getenv("PORT")
	defer func() {
		os.Setenv("PINPAD_HTTP_HOST", origHost)
		os.Setenv("PINPAD_HTTP_PORT", origPort)
		os.Setenv("PORT", origP)
	}()

	os.Unsetenv("PINPAD_HTTP_HOST")
	os.Unsetenv("PINPAD_HTTP_PORT")
	os.Unsetenv("PORT")
	if addr := getBindAddress(); addr != "127.0.0.1:8080" {
		t.Fatalf("expected default 127.0.0.1:8080, got %s", addr)
	}

	os.Setenv("PINPAD_HTTP_HOST", "127.0.0.1")
	os.Setenv("PINPAD_HTTP_PORT", "9090")
	if addr := getBindAddress(); addr != "127.0.0.1:9090" {
		t.Fatalf("expected 127.0.0.1:9090, got %s", addr)
	}

	os.Unsetenv("PINPAD_HTTP_PORT")
	os.Setenv("PORT", "3000")
	if addr := getBindAddress(); addr != "127.0.0.1:3000" {
		t.Fatalf("expected 127.0.0.1:3000, got %s", addr)
	}
}

func TestLocateModuleRoot(t *testing.T) {
	root, err := locateModuleRoot()
	if err != nil {
		t.Fatalf("locateModuleRoot returned error: %v", err)
	}
	if _, err := os.Stat(filepath.Join(root, "go.mod")); err != nil {
		t.Fatalf("go.mod not found at located root %s: %v", root, err)
	}

	_, found := findModuleRoot(os.TempDir())
	if found {
		// might or might not find depending on tempdir location
	}
}

func TestResolveLogDestination(t *testing.T) {
	explicit := filepath.Join(os.TempDir(), "custom-log.txt")
	resolved, err := resolveLogDestination(explicit)
	if err != nil {
		t.Fatalf("resolveLogDestination error: %v", err)
	}
	if resolved != filepath.Clean(explicit) {
		t.Fatalf("expected %s, got %s", explicit, resolved)
	}

	relPath := "custom_sub_log.txt"
	resolvedRel, err := resolveLogDestination(relPath)
	if err != nil {
		t.Fatalf("resolveLogDestination relative error: %v", err)
	}
	if !filepath.IsAbs(resolvedRel) {
		t.Fatalf("expected absolute path for relative input, got %s", resolvedRel)
	}

	def, err := resolveLogDestination("")
	if err != nil {
		t.Fatalf("resolveLogDestination default error: %v", err)
	}
	if !filepath.IsAbs(def) {
		t.Fatalf("expected absolute path, got %s", def)
	}
}

func TestConfigureTracer(t *testing.T) {
	tempFile := filepath.Join(t.TempDir(), "test-log.txt")
	orig := os.Getenv("PINPAD_LOG_FILE")
	defer os.Setenv("PINPAD_LOG_FILE", orig)

	os.Setenv("PINPAD_LOG_FILE", tempFile)
	tracer := logging.NewTracer()
	dest, err := configureTracer(tracer)
	if err != nil {
		t.Fatalf("configureTracer error: %v", err)
	}
	if dest != filepath.Clean(tempFile) {
		t.Fatalf("expected dest %s, got %s", tempFile, dest)
	}
	_ = tracer.Close()
}

func TestQRCodeGenerator(t *testing.T) {
	var gen qrCodeGenerator
	png, err := gen.Generate("https://example.com", 128)
	if err != nil {
		t.Fatalf("Generate returned error: %v", err)
	}
	if len(png) == 0 {
		t.Fatal("expected non-empty PNG data")
	}
}
