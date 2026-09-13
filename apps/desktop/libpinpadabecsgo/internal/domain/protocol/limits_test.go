package protocol

import "testing"

func TestBuildPacketCheckedEnforcesABECSLimit(t *testing.T) {
	if _, err := BuildPacketChecked(make([]byte, PacketDataMaxSize)); err != nil {
		t.Fatal(err)
	}
	if _, err := BuildPacketChecked(make([]byte, PacketDataMaxSize+1)); err == nil {
		t.Fatal("PKTDATA acima de 2049 bytes deve falhar")
	}
}
