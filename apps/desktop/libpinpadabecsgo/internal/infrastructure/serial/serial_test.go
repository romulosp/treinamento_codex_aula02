package serial

import (
	"errors"
	"testing"
)

func TestFakePortLifecycleAndIO(t *testing.T) {
	f := NewFakePort([]byte{0x06})
	if err := f.Open(); err != nil || !f.IsOpen() {
		t.Fatal("fake port did not open")
	}
	if err := f.Write([]byte("OPN000")); err != nil {
		t.Fatal(err)
	}
	got, err := f.Read()
	if err != nil || len(got) != 1 || got[0] != 0x06 {
		t.Fatalf("read = %v %v", got, err)
	}
	if err := f.Close(); err != nil || f.IsOpen() {
		t.Fatal("fake port did not close")
	}
}

func TestFakePortErrors(t *testing.T) {
	f := NewFakePort()
	f.ReadError = errors.New("read")
	if _, err := f.Read(); err == nil {
		t.Fatal("expected read error")
	}
	f.WriteError = errors.New("write")
	if err := f.Write(nil); err == nil {
		t.Fatal("expected write error")
	}
}
