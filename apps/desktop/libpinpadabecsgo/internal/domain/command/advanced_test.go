package command

import (
	"bytes"
	"encoding/binary"
	"encoding/hex"
	"strings"
	"testing"
)

func TestBuildGCXCommandMatchesABECS212(t *testing.T) {
	got, err := BuildGCXCommand("000000010000", "260912", "173800", GCXOptionWaitMagCard)
	if err != nil {
		t.Fatal(err)
	}
	want := append([]byte("GCX045"),
		0x00, 0x13, 0x00, 0x0C, '0', '0', '0', '0', '0', '0', '0', '1', '0', '0', '0', '0',
		0x00, 0x15, 0x00, 0x06, '2', '6', '0', '9', '1', '2',
		0x00, 0x16, 0x00, 0x06, '1', '7', '3', '8', '0', '0',
		0x00, 0x17, 0x00, 0x05, '0', '0', '0', '0', '0',
	)
	if !bytes.Equal(got, want) {
		t.Fatalf("GCX payload = % X, want % X", got, want)
	}
}

func TestBuildGCXCommandValidatesAAMMDDAndOptions(t *testing.T) {
	tests := []struct {
		name, amount, date, clock string
		options                   byte
	}{
		{"data inexistente", "000000010000", "260231", "173800", 0},
		{"hora inválida", "000000010000", "260912", "246000", 0},
		{"valor curto", "100", "260912", "173800", 0},
		{"bit RUF", "000000010000", "260912", "173800", 1},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			if _, err := BuildGCXCommand(test.amount, test.date, test.clock, test.options); err == nil {
				t.Fatal("esperava erro")
			}
		})
	}
}

func TestDisplayAndMenuBuildersMatchABECS212(t *testing.T) {
	dsp, err := BuildDSPCommand("LINHA 1", "LINHA 2")
	if err != nil {
		t.Fatal(err)
	}
	if want := "DSP032LINHA 1         LINHA 2         "; string(dsp) != want {
		t.Fatalf("DSP = %q, want %q", dsp, want)
	}
	if string(BuildGKYCommand()) != "GKY" {
		t.Fatalf("GKY deve ser clássico")
	}
	mnu, err := BuildMNUCommand(255, "MENU", []string{"UM", "DOIS"})
	if err != nil {
		t.Fatal(err)
	}
	if string(mnu[:6]) != "MNU027" || !bytes.Contains(mnu, []byte{0x00, 0x0c, 0x00, 0x01, 0xff}) {
		t.Fatalf("MNU inesperado: % X", mnu)
	}
	for _, test := range []struct {
		name    string
		timeout int
		title   string
		options []string
	}{
		{"timeout", 256, "", []string{"A"}},
		{"sem opção", 0, "", nil},
		{"mais de vinte", 0, "", make([]string, 21)},
		{"opção longa", 0, "", []string{strings.Repeat("x", 25)}},
		{"texto fora de Latin-1", 0, "😀", []string{"A"}},
	} {
		t.Run(test.name, func(t *testing.T) {
			if _, err := BuildMNUCommand(test.timeout, test.title, test.options); err == nil {
				t.Fatal("esperava erro")
			}
		})
	}
}

func TestDisplayBuildersMatchPublishedABECS212Vectors(t *testing.T) {
	dsp, err := BuildDSPCommand("ERRO DE OPERAÇÃO", "CÓDIGO:  2112/76")
	if err != nil {
		t.Fatal(err)
	}
	wantDSP, err := hex.DecodeString("4453503033324552524F204445204F50455241C7C34F43D34449474F3A2020323131322F3736")
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(dsp, wantDSP) {
		t.Fatalf("DSP = %X, want %X", dsp, wantDSP)
	}

	dex, err := BuildDEXCommand("Feliz Natal\re um\rPróspero\rAno Novo!")
	if err != nil {
		t.Fatal(err)
	}
	wantDEX, err := hex.DecodeString("44455830333830333546656C697A204E6174616C0D6520756D0D5072F3737065726F0D416E6F204E6F766F21")
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(dex, wantDEX) {
		t.Fatalf("DEX = %X, want %X", dex, wantDEX)
	}
}

func TestDisplayBuildersRejectABECSLimitViolations(t *testing.T) {
	if _, err := BuildDSPCommand(strings.Repeat("A", 17), ""); err == nil {
		t.Fatal("DSP deve rejeitar linha acima de S16")
	}
	if _, err := BuildDSPCommand("😀", ""); err == nil {
		t.Fatal("DSP deve rejeitar caractere fora de Latin-1")
	}
	if _, err := BuildDEXCommand(""); err == nil {
		t.Fatal("DEX deve rejeitar mensagem vazia")
	}
	if _, err := BuildDEXCommand(strings.Repeat("A", 161)); err == nil {
		t.Fatal("DEX deve rejeitar mensagem acima de S160")
	}
}

func TestMNUBuilderMatchesPublishedABECS212Vector(t *testing.T) {
	got, err := BuildMNUCommand(30, "Selecione, por favor:", []string{
		"5.Chamado Técnico",
		"1.Consultas",
		"3.Ajuda",
		"Voltar!!",
	})
	if err != nil {
		t.Fatal(err)
	}
	want, err := hex.DecodeString(
		"4D4E55303839" +
			"000C00011E" +
			"00200011352E4368616D61646F2054E9636E69636F" +
			"0020000B312E436F6E73756C746173" +
			"00200007332E416A756461" +
			"00200008566F6C7461722121" +
			"001B001553656C6563696F6E652C20706F72206661766F723A",
	)
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(got, want) {
		t.Fatalf("MNU = %X, want %X", got, want)
	}
}

func TestMultimediaBuildersMatchABECS212(t *testing.T) {
	name := "IMG00001"
	data := []byte("\x89PNG\r\n\x1a\n")
	dsi, err := BuildDSICommand(name)
	if err != nil {
		t.Fatal(err)
	}
	wantDSI := append([]byte("DSI012"), 0x00, 0x1e, 0x00, 0x08)
	wantDSI = append(wantDSI, name...)
	if !bytes.Equal(dsi, wantDSI) {
		t.Fatalf("DSI = % X", dsi)
	}
	mli, err := BuildMLICommand(name, data)
	if err != nil {
		t.Fatal(err)
	}
	if string(mli[:6]) != "MLI026" {
		t.Fatalf("MLI envelope = %q", mli[:6])
	}
	info := mli[len(mli)-10:]
	if binary.BigEndian.Uint32(info[:4]) != uint32(len(data)) || info[6] != 1 || !bytes.Equal(info[7:], []byte{0, 0, 0}) {
		t.Fatalf("SPE_MFINFO = % X", info)
	}
	if got := binary.BigEndian.Uint16(info[4:6]); got != 0x61a5 {
		t.Fatalf("CRC16 PNG mínimo = %04X, want 61A5", got)
	}
	mlr, err := BuildMLRCommand([]byte{1, 2, 3})
	if err != nil {
		t.Fatal(err)
	}
	if want := []byte{'M', 'L', 'R', '0', '0', '7', 0x00, 0x0f, 0x00, 0x03, 1, 2, 3}; !bytes.Equal(mlr, want) {
		t.Fatalf("MLR = % X, want % X", mlr, want)
	}
	if string(BuildMLECommand()) != "MLE" {
		t.Fatal("MLE deve ser clássico")
	}
	if _, err := BuildMLICommand("curto", data); err == nil {
		t.Fatal("nome A8 deve ser obrigatório")
	}
	if _, err := BuildMLRCommand(make([]byte, MLRMaxBlockSize+1)); err == nil {
		t.Fatal("bloco acima de 995 deve falhar")
	}
}

func TestMultimediaBuildersMatchPublishedABECS212Vectors(t *testing.T) {
	dsi, err := BuildDSICommand("QRCODE01")
	if err != nil {
		t.Fatal(err)
	}
	wantDSI, err := hex.DecodeString("445349303132001E00085152434F44453031")
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(dsi, wantDSI) {
		t.Fatalf("DSI = %X, want %X", dsi, wantDSI)
	}
	file := make([]byte, 3334)
	copy(file, []byte("\x89PNG\r\n\x1a\n"))
	file[len(file)-2], file[len(file)-1] = 0x05, 0x47 // CRC16 0xF211 do exemplo publicado.
	mli, err := BuildMLICommand("QRCODE01", file)
	if err != nil {
		t.Fatal(err)
	}
	wantMLI, err := hex.DecodeString("4D4C49303236001E00085152434F44453031001F000A00000D06F21101000000")
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(mli, wantMLI) {
		t.Fatalf("MLI = %X, want %X", mli, wantMLI)
	}
	if got := string(BuildMLECommand()); got != "MLE" {
		t.Fatalf("MLE = %q, want MLE", got)
	}
}

func TestMultimediaManagementBuildersMatchPublishedABECS212Vectors(t *testing.T) {
	if got := BuildLMFCommand(); !bytes.Equal(got, []byte("LMF")) {
		t.Fatalf("LMF = % X, want literal LMF", got)
	}
	dmf, err := BuildDMFCommand([]string{"TESTECHO", "MOVNPICT"})
	if err != nil {
		t.Fatal(err)
	}
	want, err := hex.DecodeString("444D46303234001E0008544553544543484F001E00084D4F564E50494354")
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(dmf, want) {
		t.Fatalf("DMF = % X, want % X", dmf, want)
	}

	for _, names := range [][]string{nil, {}, {"QRCODE"}, {"BAD/NAME"}} {
		if _, err := BuildDMFCommand(names); err == nil {
			t.Fatalf("DMF names %#v should be rejected", names)
		}
	}
	if _, err := BuildDMFCommand([]string{"MISSING1"}); err != nil {
		t.Fatalf("unknown but valid A8 name should be sent to the pinpad: %v", err)
	}
}

func TestDetectMediaTypeAndNamesFollowABECS212(t *testing.T) {
	for name, test := range map[string]struct {
		data []byte
		want byte
	}{
		"PNG":  {[]byte("\x89PNG\r\n\x1a\n"), 1},
		"JPEG": {[]byte{0xff, 0xd8, 0xff, 0xe0}, 2},
		"GIF":  {[]byte("GIF89a"), 3},
	} {
		t.Run(name, func(t *testing.T) {
			if got, err := DetectMediaType(test.data); err != nil || got != test.want {
				t.Fatalf("tipo = %d, %v; want %d", got, err, test.want)
			}
		})
	}
	if _, err := DetectMediaType([]byte("BMP")); err == nil {
		t.Fatal("assinatura sem suporte deve falhar")
	}
	if _, err := BuildDSICommand("INVALID!"); err == nil {
		t.Fatal("DSI deve exigir nome A8 alfanumérico")
	}
	unknownTypeMLI, err := BuildMLICommand("IMG00001", []byte("BMP"))
	if err != nil {
		t.Fatalf("MLI deve transferir tipo desconhecido como RUF: %v", err)
	}
	if got := unknownTypeMLI[len(unknownTypeMLI)-4]; got != 0 {
		t.Fatalf("SPE_MFINFO.B1 para tipo desconhecido = %02X, want RUF=00", got)
	}
	if _, err := BuildMLRCommand(nil); err == nil {
		t.Fatal("MLR deve rejeitar SPE_DATAIN vazio")
	}
}

func TestTableBuildersMatchABECS212(t *testing.T) {
	tli, err := BuildTLICommand("00", "TABVER0008")
	if err != nil || string(tli) != "TLI01200TABVER0008" {
		t.Fatalf("TLI = %q, %v", tli, err)
	}
	tlr, err := BuildTLRCommand([]string{"ABC", "DE"})
	if err != nil || string(tlr) != "TLR01302003ABC002DE" {
		t.Fatalf("TLR = %q, %v", tlr, err)
	}
	if string(BuildTLECommand()) != "TLE" {
		t.Fatal("TLE deve ser clássico")
	}
	if _, err := BuildTLICommand("00", "CURTA"); err == nil {
		t.Fatal("versão A10 deve ser obrigatória")
	}
	if _, err := BuildTLRCommand([]string{strings.Repeat("a", 995)}); err == nil {
		t.Fatal("bloco TLR acima de 999 deve falhar")
	}
}

func TestGPNBuilderAndParserMatchABECS212(t *testing.T) {
	workingKey, _ := hex.DecodeString("00112233445566778899AABBCCDDEEFF")
	got, err := BuildGPNCommandMK(7, workingKey, "1234567890123456", "DIGITE O PIN")
	if err != nil {
		t.Fatal(err)
	}
	wantPrefix := "GPN09310700112233445566778899AABBCCDDEEFF161234567890123456   10412"
	if !strings.HasPrefix(string(got), wantPrefix) || len(got) != 99 {
		t.Fatalf("GPN = %q", got)
	}
	dukpt, err := BuildGPNCommandDUKPT(7, "1234567890123456", "PIN")
	if err != nil {
		t.Fatal(err)
	}
	if !strings.HasPrefix(string(dukpt), "GPN09330700000000000000000000000000000000") {
		t.Fatalf("GPN DUKPT = %q", dukpt)
	}
	pinBlock, ksn, err := ParseGPNResponse([]byte("0123456789ABCDEF00112233445566778899"))
	if err != nil {
		t.Fatal(err)
	}
	if hex.EncodeToString(pinBlock) != "0123456789abcdef" || hex.EncodeToString(ksn) != "00112233445566778899" {
		t.Fatalf("resposta GPN: %X %X", pinBlock, ksn)
	}
	for _, invalid := range [][]byte{nil, []byte(strings.Repeat("Z", 36)), []byte(strings.Repeat("0", 35))} {
		if _, _, err := ParseGPNResponse(invalid); err == nil {
			t.Fatalf("esperava erro para %q", invalid)
		}
	}
}

func TestGPNBuilderMatchesPublishedABECS212Vector(t *testing.T) {
	workingKey, _ := hex.DecodeString("4135EA58BA13E262F44C59ED7899AA3C")
	got, err := BuildGPNCommand(GPNRequest{
		Method:              '1',
		KeyIndex:            8,
		EncryptedWorkingKey: workingKey,
		PAN:                 "4444333322221111",
		MinDigits:           4,
		MaxDigits:           12,
		Message:             "R$        34,56DIGITE SUA SENHA",
	})
	if err != nil {
		t.Fatal(err)
	}
	want, err := hex.DecodeString(
		"47504E30393331303834313335454135384241313345323632463434433539454437383939414133433136" +
			"343434343333333332323232313131312020203130343132" +
			"5224202020202020202033342C3536444947495445205355412053454E484120",
	)
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(got, want) {
		t.Fatalf("GPN = %X, want %X", got, want)
	}
}

func TestBuildABECSPayloadLimitsCommandData(t *testing.T) {
	if _, err := BuildABECSPayload(CommandGCX, []Parameter{{ID: SPEDataIn, Value: make([]byte, 996)}}); err == nil {
		t.Fatal("parâmetro acima de 995 deve falhar")
	}
	if _, err := BuildABECSPayload(CommandGCX, []Parameter{
		{ID: SPEDataIn, Value: make([]byte, 995)},
		{ID: SPEDataIn, Value: make([]byte, 995)},
		{ID: SPEDataIn, Value: make([]byte, 100)},
	}); err == nil {
		t.Fatal("comando acima de 2044 deve falhar")
	}
}

func TestBuildABECSPayloadSplitsParametersIntoCompleteBlocks(t *testing.T) {
	got, err := BuildABECSPayload(CommandGCX, []Parameter{
		{ID: SPEDataIn, Value: make([]byte, 995)},
		{ID: SPEDataIn, Value: make([]byte, 995)},
	})
	if err != nil {
		t.Fatal(err)
	}
	if string(got[:6]) != "GCX999" || string(got[1005:1008]) != "999" || len(got) != 2007 {
		t.Fatalf("blocos ABECS = len %d, prefixos %q/%q", len(got), got[:6], got[1005:1008])
	}
}

func TestGTKContractMatchesConditionalABECSFields(t *testing.T) {
	if got, err := BuildGTKCommand(GTKRequest{}); err != nil || string(got) != "GTK000" {
		t.Fatalf("GTK para obter trilhas em claro = %q, %v", got, err)
	}
	index := 7
	if _, err := BuildGTKCommand(GTKRequest{DataMethod: "00", KeyIndex: &index, WorkingKey: make([]byte, 8)}); err != nil {
		t.Fatal(err)
	}
	if _, err := BuildGTKCommand(GTKRequest{DataMethod: "10", KeyIndex: &index, WorkingKey: make([]byte, 8)}); err == nil {
		t.Fatal("TDES exige WKENC de 16 bytes")
	}
	rsaPayload, err := BuildGTKCommand(GTKRequest{DataMethod: "90", PublicKeyMod: make([]byte, 256), PublicKeyExp: []byte{1, 0, 1}})
	if err != nil || string(rsaPayload[:3]) != "GTK" {
		t.Fatalf("GTK RSA = %q, %v", rsaPayload, err)
	}
	if _, err := BuildGTKCommand(GTKRequest{DataMethod: "91", PublicKeyMod: make([]byte, 255), PublicKeyExp: []byte{3}}); err == nil {
		t.Fatal("módulo RSA diferente de 256 bytes deve falhar")
	}
	if _, err := BuildGTKCommand(GTKRequest{Tracks: "0122"}); err == nil {
		t.Fatal("SPE_TRACKS aceita somente bits")
	}
	if _, err := BuildGTKCommand(GTKRequest{KeyIndex: &index}); err == nil {
		t.Fatal("índice sem método deve falhar")
	}
	if _, err := BuildGTKCommand(GTKRequest{DataMethod: "90", KeyIndex: &index, PublicKeyMod: make([]byte, 256), PublicKeyExp: []byte{3}}); err == nil {
		t.Fatal("índice não se aplica ao método RSA")
	}
}

func TestGTKBuilderMatchesPublishedABECS212Vector(t *testing.T) {
	index := 12
	got, err := BuildGTKCommand(GTKRequest{
		Tracks:     "0111",
		DataMethod: "40",
		OpenDigits: 6,
		KeyIndex:   &index,
	})
	if err != nil {
		t.Fatal(err)
	}
	want, err := hex.DecodeString("47544B30323500030002343000070004303131310008000136000900023132")
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(got, want) {
		t.Fatalf("GTK = %X, want %X", got, want)
	}
}

func TestCLXBuilderMatchesPublishedABECS212Vector(t *testing.T) {
	got, err := BuildCLXCommand(CLXRequest{Message: "PRESTO SHOP\rOBRIGADO E\rVOLTE SEMPRE!"})
	if err != nil {
		t.Fatal(err)
	}
	want, err := hex.DecodeString("434C58303430001B002450524553544F2053484F500D4F4252494741444F20450D564F4C54452053454D50524521")
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(got, want) {
		t.Fatalf("CLX = %X, want %X", got, want)
	}
}

func TestCLXBuilderRejectsInvalidABECSFields(t *testing.T) {
	if _, err := BuildCLXCommand(CLXRequest{Message: strings.Repeat("A", 129)}); err == nil {
		t.Fatal("CLX deve rejeitar mensagem acima de S128")
	}
	if _, err := BuildCLXCommand(CLXRequest{MediaName: "INVALID!"}); err == nil {
		t.Fatal("CLX deve rejeitar nome diferente de A8")
	}
}

func TestGOXAndFCXRejectInvalidConditionalFields(t *testing.T) {
	base := GOXRequest{AcquirerReference: "01", PinMethod: "3", KeyIndex: 1}
	tests := []GOXRequest{
		func() GOXRequest { r := base; r.Amount = "100"; return r }(),
		func() GOXRequest { r := base; r.Currency = []byte("BRL"); return r }(),
		func() GOXRequest { r := base; r.Options = "10001"; return r }(),
		func() GOXRequest { r := base; r.Options = "90000"; return r }(),
		func() GOXRequest { r := base; r.TerminalParams = make([]byte, 9); return r }(),
		func() GOXRequest { r := base; r.EMVData = make([]byte, 513); return r }(),
		func() GOXRequest { r := base; r.WorkingKey = make([]byte, 16); return r }(),
	}
	for index, request := range tests {
		if _, err := BuildGOXCommand(request); err == nil {
			t.Fatalf("GOX inválido %d foi aceito", index)
		}
	}
	for _, request := range []FCXRequest{
		{Options: "0001", Authorization: "00"},
		{Options: "0000"},
		{Options: "0000", Authorization: "é"},
		{Options: "2000", Authorization: "00"},
		{Options: "2000", EMVData: make([]byte, 513)},
		{Options: "2000", TagList: make([]byte, 129)},
	} {
		if _, err := BuildFCXCommand(request); err == nil {
			t.Fatalf("FCX inválido foi aceito: %#v", request)
		}
	}
}

func TestGOXBuilderMatchesPublishedABECS212Vector(t *testing.T) {
	got, err := BuildGOXCommand(GOXRequest{
		AcquirerReference: "08",
		PinMethod:         "3",
		KeyIndex:          7,
		Amount:            "000000023450",
		Cashback:          "000000010000",
		DisplayMessage:    "CRÉDITO\rR$ 234,50\rDIGITE SUA SENHA",
		TerminalParams:    []byte{0x00, 0x00, 0x27, 0x10, 0x14, 0x00, 0x00, 0x00, 0x19, 0x50},
		TagList:           []byte{0x95, 0x9f, 0x26, 0x9f, 0x27, 0x9f, 0x10, 0x9f, 0x34, 0x9f, 0x36},
	})
	if err != nil {
		t.Fatal(err)
	}
	want, err := hex.DecodeString(
		"474F58313136" +
			"0013000C303030303030303233343530" +
			"0014000C303030303030303130303030" +
			"0002000133" +
			"000900023037" +
			"001B00224352C94449544F0D5224203233342C35300D444947495445205355412053454E4841" +
			"001A000A00002710140000001950" +
			"0004000B959F269F279F109F349F36" +
			"001000023038",
	)
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(got, want) {
		t.Fatalf("GOX = %X, want %X", got, want)
	}
}

func TestFCXBuilderMatchesPublishedABECS212Vector(t *testing.T) {
	got, err := BuildFCXCommand(FCXRequest{
		Options:       "0000",
		Authorization: "Y3",
		EMVData: []byte{
			0x91, 0x08, 0xa1, 0x02, 0xdb, 0x6d, 0x41, 0xc6, 0x79, 0x63,
			0x72, 0x12, 0x9f, 0x18, 0x00, 0x86, 0x0d, 0x84, 0x24, 0x00,
			0x00, 0x08, 0xa0, 0x71, 0x54, 0x4a, 0x23, 0x76, 0x1a, 0xa1,
		},
		TagList: []byte{0x95, 0x9f, 0x26, 0x9f, 0x27, 0x9f, 0x10},
	})
	if err != nil {
		t.Fatal(err)
	}
	want, err := hex.DecodeString(
		"464358303539" +
			"0005001E9108A102DB6D41C6796372129F1800860D8424000008A071544A23761AA1" +
			"00040007959F269F279F10" +
			"001C00025933" +
			"0019000430303030",
	)
	if err != nil {
		t.Fatal(err)
	}
	if !bytes.Equal(got, want) {
		t.Fatalf("FCX = %X, want %X", got, want)
	}
}
