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

func TestPublishedABECSLimits(t *testing.T) {
	if PacketDataMaxSize != 2049 || CommandDataMaxSize != 2044 || MLRMaxBlockSize != 995 || TLRMaxRecords != 99 {
		t.Fatalf("limites divergentes: PKT=%d CMD=%d MLR=%d TLR=%d", PacketDataMaxSize, CommandDataMaxSize, MLRMaxBlockSize, TLRMaxRecords)
	}
}
