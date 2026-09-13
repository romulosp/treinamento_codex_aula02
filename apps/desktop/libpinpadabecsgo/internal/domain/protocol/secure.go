package protocol

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/utilitario/crc"
	"bytes"
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"crypto/rsa"
	"encoding/binary"
	"encoding/hex"
	"fmt"
	"strconv"
	"strings"
	"sync"
)

// SecureSession mantém KSEC exclusivamente em memória durante a comunicação
// segura ABECS. A sessão não é segura para cópia e deve ser encerrada por Close.
type SecureSession struct {
	mu   sync.RWMutex
	key  [aes.BlockSize]byte
	live bool
}

// GenerateSecureOPN cria uma chave RSA de 2048 bits com expoente 65537 e
// devolve o payload OPN seguro em claro. A chave privada pertence ao chamador
// e nunca deve ser persistida ou registrada por esta biblioteca.
func GenerateSecureOPN() (*rsa.PrivateKey, []byte, error) {
	privateKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		return nil, nil, fmt.Errorf("generate OPN RSA key: %w", err)
	}
	payload, err := BuildSecureOPN(&privateKey.PublicKey)
	if err != nil {
		return nil, nil, err
	}
	return privateKey, payload, nil
}

// BuildSecureOPN monta o OPN seguro para uma chave RSA ABECS de 2048 bits e
// expoente público 65537. O resultado ainda não contém framing serial.
func BuildSecureOPN(publicKey *rsa.PublicKey) ([]byte, error) {
	if publicKey == nil || publicKey.N == nil || publicKey.N.BitLen() != 2048 || publicKey.E != 65537 {
		return nil, fmt.Errorf("secure OPN requires RSA-2048 with exponent 65537")
	}
	modulus := publicKey.N.FillBytes(make([]byte, 256))
	exponent := make([]byte, 4)
	binary.BigEndian.PutUint32(exponent, uint32(publicKey.E))
	exponent = bytes.TrimLeft(exponent, "\x00")
	data := "0" + "256" + strings.ToUpper(hex.EncodeToString(modulus)) + strconv.Itoa(len(exponent)) + strings.ToUpper(hex.EncodeToString(exponent))
	return []byte("OPN" + fmt.Sprintf("%03d", len(data)) + data), nil
}

// EstablishSecureSession valida a resposta OPN segura, abre CRKSEC com a
// chave RSA privada e instala KSEC temporariamente em uma nova sessão.
func EstablishSecureSession(privateKey *rsa.PrivateKey, response []byte) (*SecureSession, error) {
	if privateKey == nil || privateKey.N == nil || privateKey.N.BitLen() != 2048 || privateKey.PublicKey.E != 65537 {
		return nil, fmt.Errorf("secure OPN requires RSA-2048 with exponent 65537")
	}
	if len(response) < 12 || string(response[:3]) != "OPN" || string(response[3:6]) != "000" {
		return nil, fmt.Errorf("invalid secure OPN response")
	}
	length, err := strconv.Atoi(string(response[6:9]))
	if err != nil || length != len(response)-9 || length < 3 {
		return nil, fmt.Errorf("invalid secure OPN response length")
	}
	cipherLength, err := strconv.Atoi(string(response[9:12]))
	if err != nil || cipherLength != 256 || cipherLength*2 != len(response)-12 {
		return nil, fmt.Errorf("invalid secure OPN CRKSEC length")
	}
	ciphertext := make([]byte, cipherLength)
	if _, err := hex.Decode(ciphertext, response[12:]); err != nil {
		return nil, fmt.Errorf("decode secure OPN CRKSEC: %w", err)
	}
	key, err := rsa.DecryptPKCS1v15(rand.Reader, privateKey, ciphertext)
	if err != nil || len(key) != aes.BlockSize {
		return nil, fmt.Errorf("decrypt secure OPN CRKSEC")
	}
	session := &SecureSession{live: true}
	copy(session.key[:], key)
	for index := range key {
		key[index] = 0
	}
	return session, nil
}

// Protect encapsula CLRDATA em DATALEN/DATACRC, preenche com zeros e aplica
// AES-128-CBC com IV zero, prefixando o pacote protegido com DC2.
func (s *SecureSession) Protect(clearData []byte) ([]byte, error) {
	key, err := s.copyKey()
	if err != nil {
		return nil, err
	}
	defer zero(key)
	if len(clearData) > 2044 {
		return nil, fmt.Errorf("secure packet exceeds 2044 bytes")
	}
	plain := make([]byte, 4+len(clearData))
	binary.BigEndian.PutUint16(plain[:2], uint16(len(clearData)))
	binary.BigEndian.PutUint16(plain[2:4], crc.CRC16CCITT(clearData))
	copy(plain[4:], clearData)
	padding := aes.BlockSize - len(plain)%aes.BlockSize
	if padding != aes.BlockSize {
		plain = append(plain, make([]byte, padding)...)
	}
	block, err := aes.NewCipher(key)
	if err != nil {
		return nil, fmt.Errorf("create secure cipher: %w", err)
	}
	packetData := make([]byte, 1+len(plain))
	packetData[0] = PP_DC2
	cipher.NewCBCEncrypter(block, make([]byte, aes.BlockSize)).CryptBlocks(packetData[1:], plain)
	return packetData, nil
}

// Unprotect valida e decripta um PKTDATA iniciado por DC2, retornando apenas
// CLRDATA. Falhas de tamanho ou CRC são rejeitadas sem retornar dados parciais.
func (s *SecureSession) Unprotect(packetData []byte) ([]byte, error) {
	key, err := s.copyKey()
	if err != nil {
		return nil, err
	}
	defer zero(key)
	if len(packetData) < 1+aes.BlockSize || packetData[0] != PP_DC2 ||
		(len(packetData)-1)%aes.BlockSize != 0 {
		return nil, fmt.Errorf("invalid secure packet")
	}
	block, err := aes.NewCipher(key)
	if err != nil {
		return nil, fmt.Errorf("create secure cipher: %w", err)
	}
	plain := make([]byte, len(packetData)-1)
	cipher.NewCBCDecrypter(block, make([]byte, aes.BlockSize)).CryptBlocks(plain, packetData[1:])
	length := int(binary.BigEndian.Uint16(plain[:2]))
	if length > 2044 || length+4 > len(plain) {
		return nil, fmt.Errorf("invalid secure packet length")
	}
	clearData := append([]byte(nil), plain[4:4+length]...)
	if binary.BigEndian.Uint16(plain[2:4]) != crc.CRC16CCITT(clearData) {
		return nil, fmt.Errorf("invalid secure packet checksum")
	}
	return clearData, nil
}

// Close limpa KSEC da memória volátil da sessão. Ele é idempotente.
func (s *SecureSession) Close() {
	if s == nil {
		return
	}
	s.mu.Lock()
	zero(s.key[:])
	s.live = false
	s.mu.Unlock()
}

func (s *SecureSession) copyKey() ([]byte, error) {
	if s == nil {
		return nil, fmt.Errorf("secure session is not established")
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	if !s.live {
		return nil, fmt.Errorf("secure session is closed")
	}
	return append([]byte(nil), s.key[:]...), nil
}

func zero(data []byte) {
	for index := range data {
		data[index] = 0
	}
}
