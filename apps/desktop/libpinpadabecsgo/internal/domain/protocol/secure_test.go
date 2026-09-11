package protocol

import (
	"crypto/rand"
	"crypto/rsa"
	"encoding/hex"
	"fmt"
	"testing"
)

func TestSecureSessionProtectsAndValidatesPackets(t *testing.T) {
	session := &SecureSession{live: true}
	copy(session.key[:], []byte("0123456789ABCDEF"))
	packet, err := session.Protect([]byte("GIX000"))
	if err != nil || packet[0] != PP_DC2 {
		t.Fatalf("protect: %x %v", packet, err)
	}
	plain, err := session.Unprotect(packet)
	if err != nil || string(plain) != "GIX000" {
		t.Fatalf("unprotect: %q %v", plain, err)
	}
	packet[len(packet)-1] ^= 1
	if _, err := session.Unprotect(packet); err == nil {
		t.Fatal("expected protected packet validation error")
	}
	session.Close()
	if _, err := session.Protect([]byte("GIX000")); err == nil {
		t.Fatal("expected closed session error")
	}
	if _, err := (*SecureSession)(nil).Unprotect([]byte{PP_DC2}); err == nil {
		t.Fatal("expected nil session error")
	}
}

func TestSecureOPNUsesRSA2048AndExtractsKSEC(t *testing.T) {
	privateKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		t.Fatal(err)
	}
	payload, err := BuildSecureOPN(&privateKey.PublicKey)
	if err != nil || string(payload[:3]) != "OPN" || len(payload) != 529 {
		t.Fatalf("secure OPN: len=%d err=%v", len(payload), err)
	}
	key := []byte("0123456789ABCDEF")
	ciphertext, err := rsa.EncryptPKCS1v15(rand.Reader, &privateKey.PublicKey, key)
	if err != nil {
		t.Fatal(err)
	}
	responseData := fmt.Sprintf("256%s", hex.EncodeToString(ciphertext))
	response := []byte(fmt.Sprintf("OPN000%03d%s", len(responseData), responseData))
	session, err := EstablishSecureSession(privateKey, response)
	if err != nil {
		t.Fatal(err)
	}
	if _, err := session.Protect([]byte("DSP000")); err != nil {
		t.Fatal(err)
	}
	if _, err := BuildSecureOPN(nil); err == nil {
		t.Fatal("expected invalid public key")
	}
	if _, err := EstablishSecureSession(privateKey, []byte("OPN000003bad")); err == nil {
		t.Fatal("expected malformed secure response")
	}
	if _, err := session.Protect(make([]byte, 2045)); err == nil {
		t.Fatal("expected secure payload length validation")
	}
}
