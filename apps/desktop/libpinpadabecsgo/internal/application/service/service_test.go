package service

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/model"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
	"context"
	"testing"
)

type fakePort struct {
	open   bool
	writes [][]byte
	reads  [][]byte
}

func (p *fakePort) Open() error  { p.open = true; return nil }
func (p *fakePort) Close() error { p.open = false; return nil }
func (p *fakePort) IsOpen() bool { return p.open }
func (p *fakePort) Write(b []byte) error {
	p.writes = append(p.writes, append([]byte(nil), b...))
	return nil
}
func (p *fakePort) Read() ([]byte, error) {
	x := p.reads[0]
	p.reads = p.reads[1:]
	return x, nil
}
func TestServiceOpenAndReset(t *testing.T) {
	p := &fakePort{reads: [][]byte{{0x04}}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	if err := s.Reset(context.Background()); err != nil {
		t.Fatal(err)
	}
	if err := s.Close(context.Background()); err != nil {
		t.Fatal(err)
	}
}

func TestServiceBasicGIXAndReset(t *testing.T) {
	p := &fakePort{reads: [][]byte{{protocol.PP_ACK}, protocol.BuildPacket([]byte("GIX000")), {protocol.PP_EOT}}}
	s := New(model.DefaultConfig(), p)
	defer s.Shutdown()
	if err := s.Open(context.Background()); err != nil {
		t.Fatal(err)
	}
	if _, err := s.GetInfo(context.Background()); err != nil {
		t.Fatal(err)
	}
	if err := s.Reset(context.Background()); err != nil {
		t.Fatal(err)
	}
	if err := s.Close(context.Background()); err != nil {
		t.Fatal(err)
	}
	if len(p.writes) != 2 {
		t.Fatalf("expected GIX and CAN exchanges, got %d writes", len(p.writes))
	}
}
