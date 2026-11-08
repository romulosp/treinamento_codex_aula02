package logging

import (
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/command"
	"br.com.romulopenha/lib-pinpad-abecs-go/internal/domain/protocol"
)

// Encoding identifica a codificação de um campo para a interpretação textual.
// O PDF ABECS 2.12 não declara explicitamente o encoding dos campos "S", por
// isso ISO-8859-1 é assumido provisoriamente e confirmado por campo no catálogo.
type Encoding string

const (
	EncASCII    Encoding = "ASCII"
	EncISO88591 Encoding = "ISO-8859-1"
	EncBinary   Encoding = "BINARIO"
	EncBCD      Encoding = "BCD"
	EncNumeric  Encoding = "NUMERICO"
	EncUnknown  Encoding = "DESCONHECIDO"
)

// speField descreve um parâmetro de comando (SPE_xxx) para o interpretador.
// Nomes, formatos e descrições vêm do PDF ABECS 2.12 (seção 3.1.3.1).
type speField struct {
	name        string
	format      string
	description string
	encoding    Encoding
	sensitive   bool
	pdf         string
}

// rspField descreve um dado de resposta (RSP_DATID / PP_xxx). Seção 3.1.3.2.
type rspField struct {
	name        string
	format      string
	description string
	encoding    Encoding
	sensitive   bool
	pdf         string
}

// commandMeta descreve um comando para o interpretador (Capítulo 3 do PDF).
type commandMeta struct {
	name        string
	description string
	pdf         string
	classic     bool // schema posicional fixo (DSP/DEX), não TLV
}

// speCatalog associa cada SPE_xxx ao seu metadado documental.
var speCatalog = map[command.SPEParameter]speField{
	command.SPEIDList:              {"SPE_IDLIST", "B..128", "Lista de até 64 identificadores de retorno", EncBinary, false, "3.1.3.1"},
	command.SPEPinMethod:           {"SPE_MTHDPIN", "N1", "Método de criptografia de PIN", EncNumeric, false, "3.1.3.1"},
	command.SPEDataMethod:          {"SPE_MTHDDAT", "N2", "Método de criptografia de dados", EncNumeric, false, "3.1.3.1"},
	command.SPETagList:             {"SPE_TAGLIST", "B..128", "Tags EMV requeridos", EncBinary, false, "3.1.3.1"},
	command.SPEEMVData:             {"SPE_EMVDATA", "B..512", "Objetos EMV enviados (TLV)", EncBinary, true, "3.1.3.1"},
	command.SPECEXOption:           {"SPE_CEXOPT", "A6", "Opções do comando CEX", EncASCII, false, "3.1.3.1"},
	command.SPETracks:              {"SPE_TRACKS", "N4", "Trilhas a devolver no GTK", EncNumeric, false, "3.1.3.1"},
	command.SPEOpenDigits:          {"SPE_OPNDIG", "N1", "Dígitos em claro nas trilhas", EncNumeric, false, "3.1.3.1"},
	command.SPEKeyIndex:            {"SPE_KEYIDX", "N2", "Índice da chave MK/DUKPT", EncNumeric, false, "3.1.3.1"},
	command.SPEEncryptedWorkingKey: {"SPE_WKENC", "B8/B16", "Working Key criptografada", EncBinary, true, "3.1.3.1"},
	command.SPEMessageIndex:        {"SPE_MSGIDX", "X2", "Índice da mensagem", EncBinary, false, "3.1.3.1"},
	command.SPETimeout:             {"SPE_TIMEOUT", "X1", "Tempo de inatividade do portador (s)", EncBinary, false, "3.1.3.1"},
	command.SPEMinimumDigits:       {"SPE_MINDIG", "X1", "Mínimo de dígitos (0-32)", EncBinary, false, "3.1.3.1"},
	command.SPEMaximumDigits:       {"SPE_MAXDIG", "X1", "Máximo de dígitos (1-32)", EncBinary, false, "3.1.3.1"},
	command.SPEDataIn:              {"SPE_DATAIN", "B..995", "Dados genéricos", EncBinary, false, "3.1.3.1"},
	command.SPEAcquirerReference:   {"SPE_ACQREF", "N2", "Rede Credenciadora (01-99)", EncNumeric, false, "3.1.3.1"},
	command.SPEApplicationType:     {"SPE_APPTYPE", "N..20", "Tipos de aplicação (01-98)", EncNumeric, false, "3.1.3.1"},
	command.SPEAIDList:             {"SPE_AIDLIST", "A..512", "Registros AID (AARR)", EncASCII, false, "3.1.3.1"},
	command.SPEAmount:              {"SPE_AMOUNT", "N12", "Valor da transação em centavos", EncNumeric, false, "3.1.3.1"},
	command.SPECashback:            {"SPE_CASHBACK", "N12", "Valor de saque/troco em centavos", EncNumeric, false, "3.1.3.1"},
	command.SPETransactionDate:     {"SPE_TRNDATE", "N6", "Data da transação (AAMMDD)", EncNumeric, false, "3.1.3.1"},
	command.SPETransactionTime:     {"SPE_TRNTIME", "N6", "Hora da transação (HHMMSS)", EncNumeric, false, "3.1.3.1"},
	command.SPEGCXOption:           {"SPE_GCXOPT", "N5", "Opções do comando GCX", EncNumeric, false, "3.1.3.1"},
	command.SPEGOXOption:           {"SPE_GOXOPT", "N5", "Opções do comando GOX", EncNumeric, false, "3.1.3.1"},
	command.SPEFCXOption:           {"SPE_FCXOPT", "N4", "Opções do comando FCX", EncNumeric, false, "3.1.3.1"},
	command.SPETerminalParameters:  {"SPE_TRMPAR", "B10", "Terminal Risk Management", EncBinary, false, "3.1.3.1"},
	command.SPEDisplayMessage:      {"SPE_DSPMSG", "S..128", "Mensagem no display (quebras 0Dh)", EncISO88591, false, "3.1.3.1"},
	command.SPEARC:                 {"SPE_ARC", "A2", "Authorization Response Code", EncASCII, false, "3.1.3.1"},
	command.SPEIVCBC:               {"SPE_IVCBC", "B8", "Initialization Vector (CBC)", EncBinary, false, "3.1.3.1"},
	command.SPEMultimediaFileName:  {"SPE_MFNAME", "A8", "Nome do arquivo multimídia", EncASCII, false, "3.1.3.1"},
	command.SPEMultimediaFileInfo:  {"SPE_MFINFO", "B10", "Informações do arquivo multimídia", EncBinary, false, "3.1.3.1"},
	command.SPEMenuOption:          {"SPE_MNUOPT", "S..24", "Texto de opção de menu", EncISO88591, false, "3.1.3.1"},
	command.SPETransactionType:     {"SPE_TRNTYPE", "B1", "Tipo de transação", EncBinary, false, "3.1.3.1"},
	command.SPETransactionCurrency: {"SPE_TRNCURR", "N3", "Código da moeda", EncNumeric, false, "3.1.3.1"},
	command.SPEPANMask:             {"SPE_PANMASK", "N4", "Mascaramento do PAN (eedd)", EncNumeric, false, "3.1.3.1"},
	command.SPEPinBlockMode:        {"SPE_PBKMOD", "B256", "Módulo de chave pública RSA 2048", EncBinary, false, "3.1.3.1"},
	command.SPEPinBlockExponent:    {"SPE_PBKEXP", "B..3", "Expoente de chave pública RSA", EncBinary, false, "3.1.3.1"},
	command.SPEGCDOption:           {"SPE_GCDOPT", "?", "Opções do comando GCD (ver §3.3.8)", EncUnknown, false, "3.3.8"},
}

// rspCatalog associa cada RSP_DATID ao seu metadado documental.
var rspCatalog = map[protocol.ResponseTag]rspField{
	protocol.TagSerialNumber:               {"PP_SERNUM", "A..32", "Número de série", EncASCII, false, "3.1.3.2"},
	protocol.TagPartNumber:                 {"PP_PARTNBR", "A..32", "Part Number", EncASCII, false, "3.1.3.2"},
	protocol.TagModel:                      {"PP_MODEL", "A..20", "Modelo/versão de hardware", EncASCII, false, "3.1.3.2"},
	protocol.TagManufacturer:               {"PP_MNNAME", "A..20", "Fabricante", EncASCII, false, "3.1.3.2"},
	protocol.TagCapabilities:               {"PP_CAPAB", "A10", "Capacidades (CTLS/display)", EncASCII, false, "3.1.3.2"},
	protocol.TagOSVersion:                  {"PP_SOVER", "A..20", "Versão do software/OS", EncASCII, false, "3.1.3.2"},
	protocol.TagSpecification:              {"PP_SPECVER", "A4", "Versão da especificação (2.12)", EncASCII, false, "3.1.3.2"},
	protocol.TagManufacturerVersion:        {"PP_MANVERS", "A16", "Versão app Gerenciadora", EncASCII, false, "3.1.3.2"},
	protocol.TagApplicationVersion:         {"PP_APPVERS", "A16", "Versão app Abecs", EncASCII, false, "3.1.3.2"},
	protocol.TagGeneralVersion:             {"PP_GENVERS", "A16", "Versão app Extensão", EncASCII, false, "3.1.3.2"},
	protocol.TagContactlessCapabilities:    {"ContactlessCapabilities", "A..", "Capacidades contactless (GIX)", EncASCII, false, "6.4.3"},
	protocol.TagKernelVersion:              {"PP_KRNLVER", "A..20", "Versão Kernel EMV ICC", EncASCII, false, "3.1.3.2"},
	protocol.TagContactlessVersion:         {"PP_CTLSVER", "A..20", "Versão Kernel CTLS", EncASCII, false, "3.1.3.2"},
	protocol.TagMagneticContactlessVersion: {"PP_MCTLSVER", "A..20", "Kernel MasterCard PayPass", EncASCII, false, "3.1.3.2"},
	protocol.TagVisaContactlessVersion:     {"PP_VCTLSVER", "A..20", "Kernel VISA PayWave", EncASCII, false, "3.1.3.2"},
	protocol.TagAmexContactlessVersion:     {"PP_AECTLSVER", "A..20", "Kernel American Express", EncASCII, false, "3.1.3.2"},
	protocol.TagDiscoverContactlessVersion: {"PP_DPCTLSVER", "A..20", "Kernel Discover", EncASCII, false, "3.1.3.2"},
	protocol.TagPureVersion:                {"PP_PUREVER", "A..20", "Kernel Pure", EncASCII, false, "3.1.3.2"},
	protocol.TagQPContactlessVersion:       {"QPContactless", "A..20", "Versão QP contactless (GIX)", EncASCII, false, "6.4.3"},
	protocol.TagDisplayTextSize:            {"PP_DSPTXTSZ", "N4", "Display texto (LLCC)", EncNumeric, false, "3.1.3.2"},
	protocol.TagDisplayGraphicSize:         {"PP_DSPGRSZ", "N8", "Display gráfico (LLLLCCCC)", EncNumeric, false, "3.1.3.2"},
	protocol.TagMultimediaSupport:          {"PP_MFSUP", "A..20", "Formatos multimídia suportados", EncASCII, false, "3.1.3.2"},
	protocol.TagMerchantDescription:        {"PP_MKTDESP", "A100", "Mapa de chaves MK:TDES:PIN", EncASCII, false, "3.1.3.2"},
	protocol.TagMerchantData:               {"PP_MKTDESD", "A100", "Mapa de chaves MK:TDES:DAT", EncASCII, false, "3.1.3.2"},
	protocol.TagDukptTDESDescription:       {"PP_DKPTTDESP", "A100", "Mapa de chaves DUKPT:TDES:PIN", EncASCII, false, "3.1.3.2"},
	protocol.TagDukptTDESData:              {"PP_DKPTTDESD", "A100", "Mapa de chaves DUKPT:TDES:DAT", EncASCII, false, "3.1.3.2"},
	protocol.TagEvent:                      {"PP_EVENT", "A2", "Evento detectado no CEX", EncASCII, false, "3.1.3.2"},
	protocol.TagTrack1Included:             {"PP_TRK1INC", "A..60", "Trilha 1 incompleta", EncASCII, true, "3.1.3.2"},
	protocol.TagTrack2Included:             {"PP_TRK2INC", "A..30", "Trilha 2 incompleta", EncASCII, true, "3.1.3.2"},
	protocol.TagTrack3Included:             {"PP_TRK3INC", "A..30", "Trilha 3 incompleta", EncASCII, true, "3.1.3.2"},
	protocol.TagTrack1:                     {"PP_TRACK1", "A..76", "Trilha 1", EncASCII, true, "3.1.3.2"},
	protocol.TagTrack2:                     {"PP_TRACK2", "A..37", "Trilha 2", EncASCII, true, "3.1.3.2"},
	protocol.TagTrack3:                     {"PP_TRACK3", "A..104", "Trilha 3", EncASCII, true, "3.1.3.2"},
	protocol.TagTrack1KSN:                  {"PP_TRK1KSN", "B10", "KSN da trilha 1", EncBinary, true, "3.1.3.2"},
	protocol.TagTrack2KSN:                  {"PP_TRK2KSN", "B10", "KSN da trilha 2", EncBinary, true, "3.1.3.2"},
	protocol.TagTrack3KSN:                  {"PP_TRK3KSN", "B10", "KSN da trilha 3", EncBinary, true, "3.1.3.2"},
	protocol.TagEncryptedPAN:               {"PP_PAN", "B..", "PAN criptografado", EncBinary, true, "3.1.3.2"},
	protocol.TagEncryptedPANKSN:            {"PP_PANKSN", "B10", "KSN do PAN criptografado", EncBinary, true, "3.1.3.2"},
	protocol.TagKSN:                        {"PP_KSN", "B10", "Key Serial Number", EncBinary, true, "3.1.3.2"},
	protocol.TagValue:                      {"PP_VALUE", "B..", "Valor", EncBinary, false, "3.1.3.2"},
	protocol.TagDataOut:                    {"PP_DATAOUT", "B..", "Dados de saída", EncBinary, false, "3.1.3.2"},
	protocol.TagCardType:                   {"PP_CARDTYPE", "A2", "Tipo de cartão", EncASCII, false, "3.1.3.2"},
	protocol.TagICCStatus:                  {"PP_ICCSTAT", "A1", "Status ICC", EncASCII, false, "3.1.3.2"},
	protocol.TagAIDTableInfo:               {"PP_AIDTBLINF", "B..", "Info da tabela AID", EncBinary, false, "3.1.3.2"},
	protocol.TagPAN:                        {"PP_PAN", "A..19", "Primary Account Number", EncASCII, true, "3.1.3.2"},
	protocol.TagPANSequenceNumber:          {"PP_PANSEQ", "N2", "Sequência do PAN", EncNumeric, true, "3.1.3.2"},
	protocol.TagEMVData:                    {"PP_EMVDATA", "B..", "Dados EMV (TLV)", EncBinary, true, "3.1.3.2"},
	protocol.TagCardholderName:             {"PP_CARDHNAME", "A..26", "Nome do portador", EncASCII, true, "3.1.3.2"},
	protocol.TagGOXResult:                  {"PP_GOXRES", "A6", "Resultado do GOX", EncASCII, false, "3.1.3.2"},
	protocol.TagPINBlock:                   {"PP_PINBLK", "B..", "Bloco de PIN criptografado", EncBinary, true, "3.1.3.2"},
	protocol.TagFCXResult:                  {"PP_FCXRES", "A..", "Resultado do FCX", EncASCII, false, "3.1.3.2"},
	protocol.TagISResults:                  {"PP_ISRES", "B..", "Issuer Script Results", EncBinary, true, "3.1.3.2"},
	protocol.TagBigRandom:                  {"PP_BIGRAND", "B..", "Grande aleatório", EncBinary, false, "3.1.3.2"},
	protocol.TagLabel:                      {"PP_LABEL", "A..", "Rótulo", EncASCII, false, "3.1.3.2"},
	protocol.TagIssuerCountry:              {"PP_ISSCTRY", "A3", "País do emissor", EncASCII, false, "3.1.3.2"},
	protocol.TagCardExpiration:             {"PP_CARDEXP", "N4", "Expiração do cartão", EncNumeric, true, "3.1.3.2"},
	protocol.TagMultimediaFileName:         {"PP_MFNAME", "A8", "Nome do arquivo multimídia", EncASCII, false, "3.1.3.2"},
	protocol.TagDeviceType:                 {"PP_DEVTYPE", "A..", "Tipo de dispositivo", EncASCII, false, "3.1.3.2"},
	protocol.TagTLRMemory:                  {"PP_TLRMEM", "B..", "Memória TLR", EncBinary, false, "3.1.3.2"},
	protocol.TagEncryptedRandom:            {"PP_ENCRAND", "B..", "Aleatório criptografado", EncBinary, true, "3.1.3.2"},
	protocol.TagBatteryInfo:                {"PP_BATTINF", "B..", "Informação de bateria", EncBinary, false, "3.1.3.2"},
	protocol.TagCommunicationInfo:          {"PP_COMMINF", "B..", "Informação de comunicação", EncBinary, false, "3.1.3.2"},
}

// commandCatalog descreve os comandos implementados pela biblioteca.
var commandCatalog = map[command.Type]commandMeta{
	command.CommandOPN: {"OPN", "Aloca recursos de hardware/software", "3.2.1/3.2.2", false},
	command.CommandCLO: {"CLO", "Libera recursos do pinpad", "3.2.6", false},
	command.CommandCLX: {"CLX", "Equivalente ao CLO, com mensagem", "3.2.7", false},
	command.CommandGIX: {"GIX", "Informações gerais do dispositivo", "3.2.4", false},
	command.CommandDSP: {"DSP", "Mensagem no display (2x16)", "3.3.5", true},
	command.CommandDEX: {"DEX", "Mensagem no display, formato livre", "3.3.4", true},
	command.CommandMNU: {"MNU", "Menu de até 20 opções", "3.3.13", false},
	command.CommandGKY: {"GKY", "Aguarda pressionamento de tecla", "3.3.10", false},
	command.CommandDSI: {"DSI", "Apresenta imagem multimídia", "3.4.6", false},
	command.CommandMLI: {"MLI", "Inicia carga de arquivo multimídia", "3.4.1", false},
	command.CommandMLR: {"MLR", "Carrega bloco de arquivo multimídia", "3.4.2", false},
	command.CommandMLE: {"MLE", "Finaliza carga de arquivo multimídia", "3.4.3", false},
	command.CommandLMF: {"LMF", "Lista nomes de arquivos multimídia", "3.4.4", false},
	command.CommandDMF: {"DMF", "Exclui arquivos multimídia", "3.4.5", false},
	command.CommandTLI: {"TLI", "Inicia carga de tabelas EMV", "3.5.2", false},
	command.CommandTLR: {"TLR", "Carrega registros das Tabelas EMV", "3.5.3", false},
	command.CommandTLE: {"TLE", "Finaliza carga de tabelas", "3.5.4", false},
	command.CommandGCX: {"GCX", "Inicia transação com cartão (ABECS)", "3.7.1", false},
	command.CommandGTK: {"GTK", "Obtém trilhas completas", "3.3.12", false},
	command.CommandGOX: {"GOX", "Continua processamento de chip (ABECS)", "3.7.3", false},
	command.CommandFCX: {"FCX", "Finaliza processamento de chip (ABECS)", "3.7.4", false},
	command.CommandGPN: {"GPN", "Captura de PIN (bloco)", "3.3.11", false},
}

// speFieldFor devolve o metadado de um parâmetro SPE ou um fallback neutro.
func speFieldFor(id command.SPEParameter) speField {
	if field, ok := speCatalog[id]; ok {
		return field
	}
	return speField{name: "DESCONHECIDO", format: "?", encoding: EncUnknown, pdf: ""}
}

// rspFieldFor devolve o metadado de um dado de resposta ou um fallback neutro.
func rspFieldFor(id protocol.ResponseTag) rspField {
	if field, ok := rspCatalog[id]; ok {
		return field
	}
	return rspField{name: "DESCONHECIDO", format: "?", encoding: EncUnknown, pdf: ""}
}

// commandMetaFor devolve o metadado de um comando ou um fallback neutro.
func commandMetaFor(kind command.Type) commandMeta {
	if meta, ok := commandCatalog[kind]; ok {
		return meta
	}
	return commandMeta{name: string(kind), description: "Comando não catalogado", pdf: ""}
}
