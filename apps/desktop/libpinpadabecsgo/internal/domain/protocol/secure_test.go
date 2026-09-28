package protocol

import (
	"bytes"
	"crypto/rand"
	"crypto/rsa"
	"encoding/hex"
	"fmt"
	"testing"
)

func TestSecureSessionMatchesPublishedABECS212Vector(t *testing.T) {
	key, err := hex.DecodeString("DB3B4D015432AB3223555A1F81759A94")
	if err != nil {
		t.Fatal(err)
	}
	clearData, err := hex.DecodeString("4749583031340001000A8001800480349101910E")
	if err != nil {
		t.Fatal(err)
	}
	want, err := hex.DecodeString("12EA229EDD36F84C2AA7E00275105C3A8A787FC9B2883540AEE827BA1C5A039496")
	if err != nil {
		t.Fatal(err)
	}
	session := &SecureSession{live: true}
	copy(session.key[:], key)
	got, err := session.Protect(clearData)
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(got, want) {
		t.Fatalf("PKTDATA seguro = %X, want %X", got, want)
	}
	decoded, err := session.Unprotect(want)
	if err != nil || !bytes.Equal(decoded, clearData) {
		t.Fatalf("Unprotect = %X, %v", decoded, err)
	}
}

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

func TestGenerateSecureOPNReturnsMatchingRSAKeyAndPayload(t *testing.T) {
	privateKey, payload, err := GenerateSecureOPN()
	if err != nil {
		t.Fatal(err)
	}
	want, err := BuildSecureOPN(&privateKey.PublicKey)
	if err != nil {
		t.Fatal(err)
	}
	if privateKey.N.BitLen() != 2048 || privateKey.E != 65537 || !bytes.Equal(payload, want) {
		t.Fatalf("OPN gerado não corresponde à chave RSA: bits=%d expoente=%d", privateKey.N.BitLen(), privateKey.E)
	}
}
