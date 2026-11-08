package logging

import (
	"testing"

	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

func TestSpeCatalogNames(t *testing.T) {
	tests := []struct {
		name string
		id   command.SPEParameter
		want string
	}{
		{name: "amount", id: command.SPEAmount, want: "SPE_AMOUNT"},
		{name: "cashback", id: command.SPECashback, want: "SPE_CASHBACK"},
		{name: "dspmsg", id: command.SPEDisplayMessage, want: "SPE_DSPMSG"},
		{name: "working key", id: command.SPEEncryptedWorkingKey, want: "SPE_WKENC"},
		{name: "unknown", id: command.SPEParameter(0xFFFF), want: "DESCONHECIDO"},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := speFieldFor(tt.id).name; got != tt.want {
				t.Fatalf("speFieldFor(0x%04X).name = %q, want %q", uint16(tt.id), got, tt.want)
			}
		})
	}
}

func TestSpeCatalogSensitivity(t *testing.T) {
	if !speFieldFor(command.SPEEncryptedWorkingKey).sensitive {
		t.Fatal("SPE_WKENC deve ser sensível")
	}
	if !speFieldFor(command.SPEEMVData).sensitive {
		t.Fatal("SPE_EMVDATA deve ser sensível")
	}
	if speFieldFor(command.SPEAmount).sensitive {
		t.Fatal("SPE_AMOUNT não deve ser sensível")
	}
}

func TestRspCatalogSensitivity(t *testing.T) {
	for _, tag := range []protocol.ResponseTag{
		protocol.TagPINBlock,
		protocol.TagKSN,
		protocol.TagEMVData,
		protocol.TagTrack1,
		protocol.TagTrack2Included,
		protocol.TagPAN,
		protocol.TagEncryptedPAN,
	} {
		if !rspFieldFor(tag).sensitive {
			t.Errorf("tag 0x%04X deve ser sensível", uint16(tag))
		}
	}
	if rspFieldFor(protocol.TagSerialNumber).sensitive {
		t.Fatal("PP_SERNUM não deve ser sensível")
	}
}

func TestCommandCatalogClassic(t *testing.T) {
	if !commandMetaFor(command.CommandDSP).classic {
		t.Fatal("DSP deve ser clássico (schema fixo)")
	}
	if !commandMetaFor(command.CommandDEX).classic {
		t.Fatal("DEX deve ser clássico")
	}
	if commandMetaFor(command.CommandGIX).classic {
		t.Fatal("GIX não deve ser clássico")
	}
}
