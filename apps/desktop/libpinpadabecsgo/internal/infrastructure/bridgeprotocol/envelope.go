// Package bridgeprotocol implementa o framing exclusivo do Transport Bridge.
// O payload DATA nunca é interpretado como protocolo ABECS.
package bridgeprotocol

import (
	"context"
	"encoding/binary"
	"errors"
	"fmt"
	"io"
	"net"
	"strings"
	"time"
	"unicode/utf8"
)

const (
	Version        byte   = 1
	MaxCorrelation uint16 = 128
	MaxPayload     uint32 = 1024 * 1024
	headerSize            = 14
	magic                 = "PBRG"
)

// MessageType identifica mensagens de controle e dados do Bridge.
type MessageType byte

const (
	Hello MessageType = iota + 1
	HelloOK
	Acquire
	AcquireOK
	Release
	Data
	Ping
	Pong
	Error
	Close
)

// Frame é uma mensagem completa do protocolo do Bridge.
type Frame struct {
	Version       byte
	MessageType   MessageType
	Flags         uint16
	CorrelationID string
	Payload       []byte
}

// ErrInvalidFrame identifica framing inválido antes de qualquer payload ser
// encaminhado ao transporte ABECS.
var ErrInvalidFrame = errors.New("invalid bridge frame")

func (f Frame) validate() error {
	if f.Version == 0 {
		f.Version = Version
	}
	if f.Version != Version {
		return fmt.Errorf("%w: unsupported version", ErrInvalidFrame)
	}
	if f.MessageType < Hello || f.MessageType > Close {
		return fmt.Errorf("%w: unsupported message type", ErrInvalidFrame)
	}
	if len(f.CorrelationID) > int(MaxCorrelation) || !utf8.ValidString(f.CorrelationID) {
		return fmt.Errorf("%w: invalid correlation id", ErrInvalidFrame)
	}
	if len(f.Payload) > int(MaxPayload) {
		return fmt.Errorf("%w: payload exceeds limit", ErrInvalidFrame)
	}
	return nil
}

// Encode serializa um frame com header fixo e payload limitado.
func Encode(f Frame) ([]byte, error) {
	if f.Version == 0 {
		f.Version = Version
	}
	if err := f.validate(); err != nil {
		return nil, err
	}
	correlation := []byte(f.CorrelationID)
	result := make([]byte, headerSize+len(correlation)+len(f.Payload))
	copy(result[:4], magic)
	result[4] = f.Version
	result[5] = byte(f.MessageType)
	binary.BigEndian.PutUint16(result[6:8], f.Flags)
	binary.BigEndian.PutUint16(result[8:10], uint16(len(correlation)))
	binary.BigEndian.PutUint32(result[10:14], uint32(len(f.Payload)))
	copy(result[14:], correlation)
	copy(result[14+len(correlation):], f.Payload)
	return result, nil
}

// Decode valida e decodifica um frame completo já recebido.
func Decode(data []byte) (Frame, error) {
	if len(data) < headerSize || string(data[:4]) != magic {
		return Frame{}, fmt.Errorf("%w: header", ErrInvalidFrame)
	}
	correlationLen := int(binary.BigEndian.Uint16(data[8:10]))
	payloadLen := int(binary.BigEndian.Uint32(data[10:14]))
	if correlationLen > int(MaxCorrelation) || payloadLen > int(MaxPayload) {
		return Frame{}, fmt.Errorf("%w: declared length exceeds limit", ErrInvalidFrame)
	}
	if len(data) != headerSize+correlationLen+payloadLen {
		return Frame{}, fmt.Errorf("%w: truncated or trailing bytes", ErrInvalidFrame)
	}
	f := Frame{
		Version:       data[4],
		MessageType:   MessageType(data[5]),
		Flags:         binary.BigEndian.Uint16(data[6:8]),
		CorrelationID: string(data[14 : 14+correlationLen]),
		Payload:       append([]byte(nil), data[14+correlationLen:]...),
	}
	if err := f.validate(); err != nil {
		return Frame{}, err
	}
	return f, nil
}

// WriteFrame escreve integralmente um frame respeitando o contexto.
func WriteFrame(ctx context.Context, conn net.Conn, frame Frame) error {
	if ctx == nil {
		ctx = context.Background()
	}
	data, err := Encode(frame)
	if err != nil {
		return err
	}
	return writeAll(ctx, conn, data)
}

// ReadFrame lê exatamente um frame, inclusive quando o socket fragmenta o
// header ou o payload em várias leituras.
func ReadFrame(ctx context.Context, conn net.Conn) (Frame, error) {
	if ctx == nil {
		ctx = context.Background()
	}
	header := make([]byte, headerSize)
	if err := readAll(ctx, conn, header); err != nil {
		return Frame{}, err
	}
	if string(header[:4]) != magic {
		return Frame{}, fmt.Errorf("%w: magic", ErrInvalidFrame)
	}
	correlationLen := int(binary.BigEndian.Uint16(header[8:10]))
	payloadLen := int(binary.BigEndian.Uint32(header[10:14]))
	if correlationLen > int(MaxCorrelation) || payloadLen > int(MaxPayload) {
		return Frame{}, fmt.Errorf("%w: declared length exceeds limit", ErrInvalidFrame)
	}
	body := make([]byte, correlationLen+payloadLen)
	if err := readAll(ctx, conn, body); err != nil {
		return Frame{}, err
	}
	data := append(header, body...)
	return Decode(data)
}

func setDeadline(ctx context.Context, conn net.Conn) error {
	deadline := time.Now().Add(100 * time.Millisecond)
	if ctxDeadline, ok := ctx.Deadline(); ok && ctxDeadline.Before(deadline) {
		deadline = ctxDeadline
	}
	return conn.SetDeadline(deadline)
}

func writeAll(ctx context.Context, conn net.Conn, data []byte) error {
	for len(data) > 0 {
		if err := ctx.Err(); err != nil {
			return err
		}
		if err := setDeadline(ctx, conn); err != nil {
			return fmt.Errorf("set bridge write deadline: %w", err)
		}
		n, err := conn.Write(data)
		if n > 0 {
			data = data[n:]
		}
		if err != nil {
			if netErr, ok := err.(net.Error); ok && netErr.Timeout() {
				continue
			}
			return fmt.Errorf("write bridge frame: %w", err)
		}
		if n == 0 {
			return io.ErrShortWrite
		}
	}
	return conn.SetDeadline(time.Time{})
}

func readAll(ctx context.Context, conn net.Conn, data []byte) error {
	for len(data) > 0 {
		if err := ctx.Err(); err != nil {
			return err
		}
		if err := setDeadline(ctx, conn); err != nil {
			return fmt.Errorf("set bridge read deadline: %w", err)
		}
		n, err := conn.Read(data)
		if n > 0 {
			data = data[n:]
		}
		if err != nil {
			if errors.Is(err, io.EOF) && len(data) > 0 {
				return io.ErrUnexpectedEOF
			}
			if netErr, ok := err.(net.Error); ok && netErr.Timeout() {
				continue
			}
			return fmt.Errorf("read bridge frame: %w", err)
		}
		if n == 0 {
			return io.ErrNoProgress
		}
	}
	return conn.SetDeadline(time.Time{})
}

// ErrorPayload serializa uma mensagem de erro de infraestrutura sem incluir
// payload ABECS.
func ErrorPayload(code, message string) []byte {
	return []byte(strings.TrimSpace(code) + ":" + strings.TrimSpace(message))
}
