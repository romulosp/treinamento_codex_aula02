package logging

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"path/filepath"
	"strings"
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

type diagnosticPort struct {
	reads, writes [][]byte
	writeErr      error
	open          bool
}

func (p *diagnosticPort) Open() error  { p.open = true; return nil }
func (p *diagnosticPort) Close() error { p.open = false; return nil }
func (p *diagnosticPort) IsOpen() bool { return p.open }
func (p *diagnosticPort) Write(data []byte) error {
	if p.writeErr != nil {
		return p.writeErr
	}
	p.writes = append(p.writes, append([]byte(nil), data...))
	return nil
}
func (p *diagnosticPort) Read(context.Context) ([]byte, error) {
	if len(p.reads) == 0 {
		return nil, nil
	}
	data := p.reads[0]
	p.reads = p.reads[1:]
	return data, nil
}

func diagnosticHarness(t *testing.T) (*DetailedTransport, *diagnosticPort, string) {
	t.Helper()
	tracer := NewTracer()
	tracer.SetDetailed(true)
	path := filepath.Join(t.TempDir(), "trace.txt")
	if _, err := tracer.SetLogDestination(path); err != nil {
		t.Fatal(err)
	}
	t.Cleanup(func() {
		if err := tracer.Close(); err != nil {
			t.Error(err)
		}
	})
	p := &diagnosticPort{}
	transport := NewDetailedTransport(p, tracer)
	if err := transport.Open(); err != nil {
		t.Fatal(err)
	}
	t.Cleanup(func() { _ = transport.Close() })
	return transport, p, path
}

func TestDetailedTransportPreservesChunksAndLogsPackets(t *testing.T) {
	t.Parallel()
	transport, raw, path := diagnosticHarness(t)
	packet := protocol.BuildCommand("GIX", nil)
	chunks := [][]byte{packet[:2], packet[2:5], packet[5:]}
	for _, chunk := range chunks {
		if err := transport.Write(chunk); err != nil {
			t.Fatal(err)
		}
	}
	if len(raw.writes) != len(chunks) {
		t.Fatal("write boundaries changed")
	}
	for i := range chunks {
		if !bytes.Equal(raw.writes[i], chunks[i]) {
			t.Fatal("write changed")
		}
	}
	body := responseBody("GIX", "000", tagValue{uint16(protocol.TagModel), []byte("P35")})
	response := protocol.BuildPacket(body)
	raw.reads = [][]byte{append([]byte{protocol.PP_ACK}, response[:1]...), response[1:4], response[4:]}
	for len(raw.reads) > 0 {
		want := append([]byte(nil), raw.reads[0]...)
		got, err := transport.Read(context.Background())
		if err != nil || !bytes.Equal(got, want) {
			t.Fatalf("read changed: %v", err)
		}
	}
	content := readTraceFile(t, path)
	for _, want := range []string{"SPE CMD=GIX", "PP CMD=GIX", "PP_MODEL", `= "P35"`, "STATUS=VALIDO"} {
		if !strings.Contains(content, want) {
			t.Errorf("missing %q", want)
		}
	}
	if strings.Count(content, "HEX:") != 2 {
		t.Fatal("expected two complete diagnostic packets")
	}
}

func TestDetailedTransportFailsClosed(t *testing.T) {
	secret := []byte("synthetic-private-sentinel")
	badCRC := protocol.BuildCommand("GIX", nil)
	badCRC[len(badCRC)-1] ^= 1
	sensitiveTag := protocol.BuildPacket(responseBody("GIX", "000", tagValue{uint16(protocol.TagPAN), secret}))
	unknownTag := protocol.BuildPacket(responseBody("GIX", "000", tagValue{0xFFFF, secret}))
	cases := []struct {
		name              string
		request, response []byte
	}{
		{"sensitive-command", protocol.BuildCommand("GOX", secret), protocol.BuildPacket(append([]byte("GOX000"), secret...))},
		{"unknown-command", protocol.BuildCommand("ZZZ", secret), nil},
		{"bad-crc", badCRC, protocol.BuildPacket([]byte("GIX000"))},
		{"sensitive-field", protocol.BuildCommand("GIX", nil), sensitiveTag},
		{"unknown-field", protocol.BuildCommand("GIX", nil), unknownTag},
		{"mismatched-response", protocol.BuildCommand("GIX", nil), protocol.BuildPacket([]byte("DSP000"))},
		{"orphan-response", nil, protocol.BuildPacket([]byte("GIX000"))},
		{"secure-prefix", append([]byte{protocol.PP_DC2}, protocol.BuildCommand("GIX", nil)...), protocol.BuildPacket([]byte("GIX000"))},
		{"secure-opn", protocol.BuildCommand("OPN", secret), protocol.BuildPacket(append([]byte("OPN000"), secret...))},
		{"short-response", protocol.BuildCommand("GIX", nil), protocol.BuildPacket([]byte("GI"))},
		{"incomplete", []byte{protocol.PP_SYN, 'G'}, nil},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			t.Parallel()
			transport, raw, path := diagnosticHarness(t)
			if len(tc.request) > 0 {
				if err := transport.Write(tc.request); err != nil {
					t.Fatal(err)
				}
			}
			raw.reads = [][]byte{tc.response}
			if _, err := transport.Read(context.Background()); err != nil {
				t.Fatal(err)
			}
			content := readTraceFile(t, path)
			for _, forbidden := range []string{string(secret), fmt.Sprintf("%X", secret), "PP CMD="} {
				if strings.Contains(content, forbidden) {
					t.Errorf("protected response exposed: %q", forbidden)
				}
			}
			if !bytes.Equal(tc.request, protocol.BuildCommand("GIX", nil)) && strings.Contains(content, "HEX:") {
				t.Fatal("invalid request detailed")
			}
		})
	}
}

func TestDetailedTransportSessionResetAndOversize(t *testing.T) {
	t.Parallel()
	transport, raw, path := diagnosticHarness(t)
	if err := transport.Write(append([]byte{protocol.PP_SYN}, bytes.Repeat([]byte{'X'}, 5000)...)); err != nil {
		t.Fatal(err)
	}
	if err := transport.Write(protocol.BuildCommand("GIX", nil)); err != nil {
		t.Fatal(err)
	}
	if strings.Contains(readTraceFile(t, path), "HEX:") {
		t.Fatal("resynchronized inside oversized packet")
	}
	if err := transport.Close(); err != nil {
		t.Fatal(err)
	}
	if err := transport.Open(); err != nil {
		t.Fatal(err)
	}
	// No correlation survives the previous session.
	raw.reads = [][]byte{protocol.BuildPacket([]byte("GIX000"))}
	if _, err := transport.Read(context.Background()); err != nil {
		t.Fatal(err)
	}
	if strings.Contains(readTraceFile(t, path), "HEX:") {
		t.Fatal("response crossed sessions")
	}
	if err := transport.Write(protocol.BuildCommand("GIX", nil)); err != nil {
		t.Fatal(err)
	}
	if strings.Count(readTraceFile(t, path), "HEX:") != 1 {
		t.Fatal("did not recover on new session")
	}
}

func TestDetailedTransportWriteFailureDoesNotLogSuccess(t *testing.T) {
	t.Parallel()
	transport, raw, path := diagnosticHarness(t)
	raw.writeErr = errors.New("synthetic write failure")
	if err := transport.Write(protocol.BuildCommand("GIX", nil)); !errors.Is(err, raw.writeErr) {
		t.Fatal("lost write error")
	}
	if strings.Contains(readTraceFile(t, path), "HEX:") {
		t.Fatal("failed write was logged as sent")
	}
}

func TestDetailedTracerRejectsSensitiveAndUnknownFieldsBeforeHex(t *testing.T) {
	for _, tag := range []uint16{uint16(protocol.TagPINBlock), uint16(protocol.TagKSN), 0xFFFF} {
		t.Run(fmt.Sprintf("tag-%04X", tag), func(t *testing.T) {
			t.Parallel()
			transport, _, path := diagnosticHarness(t)
			packet := protocol.BuildPacket(responseBody("GIX", "000", tagValue{tag, []byte("synthetic-secret")}))
			if err := transport.tracer.RecordPP(command.CommandGIX, packet); err != nil {
				t.Fatal(err)
			}
			content := readTraceFile(t, path)
			if strings.Contains(content, "HEX:") || !strings.Contains(content, "REDACTED") {
				t.Fatal("unsafe frame must remain redacted")
			}
		})
	}
}

func TestDetailedRealClassicCommands(t *testing.T) {
	dex, _ := command.BuildDEXCommand("VALOR TOTAL\rCONFIRMAR")
	tli, _ := command.BuildTLICommand("01", "0000000001")
	cases := []struct {
		name  string
		kind  command.Type
		body  []byte
		field string
	}{
		{"OPN", command.CommandOPN, []byte("OPN"), "COMANDO: OPN"},
		{"CLO", command.CommandCLO, command.BuildPacketPayload(command.CommandCLO, strings.Repeat(" ", 32)), "CLO_MSG"},
		{"DEX", command.CommandDEX, dex, `VALOR TOTAL\\rCONFIRMAR`},
		{"GKY", command.CommandGKY, command.BuildGKYCommand(), "COMANDO: GKY"},
		{"TLI", command.CommandTLI, tli, "TLI_VERSION"},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			t.Parallel()
			transport, _, path := diagnosticHarness(t)
			if err := transport.Write(protocol.BuildPacket(tc.body)); err != nil {
				t.Fatal(err)
			}
			if !strings.Contains(readTraceFile(t, path), tc.field) {
				t.Fatalf("missing %s", tc.field)
			}
		})
	}
}
