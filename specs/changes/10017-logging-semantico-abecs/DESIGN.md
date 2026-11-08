# Design: 10017-logging-semantico-abecs

## 1. Objetivo

Decorar o `logging.Tracer` existente com interpretação semântica dos frames
SPE/PP (HEX + ASCII + detalhamento campo a campo), **sem** alterar protocolo,
comunicação, builders, parsers ou cálculo de CRC.

## 2. Princípios

1. **Decorativo:** consome `kind` (`command.Type`), `status`, flag de redação e
   estado de sessão segura já conhecidos; não re-deriva comando a partir de bytes.
2. **Classificação A/B/C antes de qualquer formatação** (falha → classe B).
3. **Catálogo = extensão** do existente, com nomes do PDF ABECS 2.12.
4. **Redação prevalece:** nenhum payload sensível/criptografado é decodificado.

## 3. Decisões de arquitetura

| Decisão | Justificativa |
|---|---|
| Código novo vive em `internal/infrastructure/logging/` (mesmo pacote) | É observabilidade; evita novo pacote e ciclo de import; já importa `command` e `protocol`. |
| Catálogo em `map` tipado (`SPEParameter`→`SpeField`, `ResponseTag`→`RspField`) | Reusa constantes existentes; extensível sem alterar o formatador. |
| Encoding por campo, default `DESCONHECIDO` → `NAO_INTERPRETADO` | O PDF não declara encoding de campos `S`; evitar interpretação errada. |
| Reusa `isSensitive`/`RedactPayload` e `protocol.ValidatePacket`/`RemoveSubstitution` | Não duplica regra funcional (Risco 2 da SPEC). |
| `SetDetailed(bool)` no `Tracer` (default `false` = LEGACY) | Mantém comportamento atual; `DETAILED` opt-in. |
| Bridge permanece opaco (`SetOpaqueTrace(true)` já redige tudo) | Bridge não conhece comando; detalhamento não deve vazar por essa via. |

## 4. Estrutura de arquivos (novos em `internal/infrastructure/logging/`)

```
internal/infrastructure/logging/
├── catalog.go       # metadados por campo (SPE_xxx, RSP_DATID) e por comando
├── classify.go      # FrameClass (Visible/Sensitive/Encrypted)
├── hexdump.go       # HEX + ASCII + offset (linhas de N bytes)
├── decode.go        # decodificação semântica (TLV ABECS + fixo clássico)
├── detailed.go      # integração ao Tracer (SetDetailed + emissão multi-linha)
└── *_test.go        # testes por módulo
```

## 5. Modelo de dados

```go
// catalog.go
type Encoding string
const (
    EncASCII     Encoding = "ASCII"
    EncISO88591  Encoding = "ISO-8859-1"
    EncBinary    Encoding = "BINARIO"
    EncBCD       Encoding = "BCD"
    EncNumeric   Encoding = "NUMERICO"
    EncUnknown   Encoding = "DESCONHECIDO"
)

type SpeField struct {
    Name, Format, Description, PDF string
    Encoding Encoding
    Sensitive bool
}
type RspField struct {
    Name, Format, Description, PDF string
    Encoding Encoding
    Sensitive bool
}
type CommandMeta struct {
    Name, Description, PDF string
    Clássico bool // schema fixo (DSP/DEX), não TLV
}

// classify.go
type FrameClass int
const (
    ClassVisible   FrameClass = iota // A
    ClassSensitive                   // B
    ClassEncrypted                   // C
)
func classify(kind command.Type, data []byte, redact, secureSession bool) FrameClass

// decode.go
type DecodedField struct {
    Name, Format string
    Offset, Length int
    Hex, ASCII, Value string
    Sensitive bool
}
func decodeCommand(kind command.Type, body []byte) ([]DecodedField, error)
func decodeResponse(kind command.Type, body []byte) ([]DecodedField, error)

// detailed.go
func (t *Tracer) SetDetailed(enabled bool)
```

## 6. Fluxo de decodificação (Classe A apenas)

1. `RecordSPE/RecordPP` → classifica (A/B/C). Se B/C → linha LEGACY (redigida/opaca).
2. Classe A e multi-byte → tenta enquadrar com `protocol.ValidatePacket` (modo
   diagnóstico, **não fatal**).
3. Extrai `body` (pós `RemoveSubstitution`) e cruza `body[:3]` com `kind`.
4. Comando TLV (ABECS): `CMD_ID(A3) + CMD_LEN(N3) + blocos{ PARID(X2) + PARLEN(X2) + PAR }`.
   Resposta TLV: `RSP_ID(A3) + RSP_STAT(N3) + RSP_LEN(N3) + blocos{ DATID(X2) + DATLEN(X2) + DAT }`.
5. Comando clássico (`DSP`/`DEX`): schema fixo.
6. Para cada campo, consulta o catálogo e emite `DecodedField` (valor interpretado
   somente se `Encoding != DESCONHECIDO` e `!Sensitive`).
7. CRC: recalcula com `crc.CRC16CCITT` (mesma regra de `ValidatePacket`) e informa
   `VALIDO/INVALIDO`.

**Não implementar** reconstrução de fragmentos nem máquina ACK/NAK no logger.

## 7. Formato DETAILED (exemplo)

```text
[COM7#001] SPE CMD=DSP FUNC=serial.Adapter.Write DATA_HORA=<RFC3339Nano>
  HEX:
    0000  16 44 53 50 30 33 32 20 20 20 20 4F 50 45 52 41  |..DSP032    OPERA|
    0010  C7 C3 4F 20 20 20 20 20 20 20 46 49 4E 41 4C 49  |..O       FINALI|
    0020  5A 41 44 41 20 20 20 17 52 13                    |ZADA   ..R.    |
  COMANDO: DSP (Exibição de mensagem no display — §3.3.5)
    CMD_LEN: 032 (fixo)
    DSP_MSG: S32 [ISO-8859-1] = "<32 chars, padding preservado>"
  CRC: RECEBIDO=0x5213 CALCULADO=0x5213 STATUS=VALIDO
```

- Cada bloco preserva sessão, direção e `FUNC`/`DATA_HORA` na linha-cabeçalho.
- Comandos Classe B/C continuam emitindo **uma linha LEGACY** (redigida/opaca),
  nunca o bloco detalhado.

## 8. Integração e configuração

- `configureTracer` em `cmd/libpinpadabecsgo/main.go` passa a ler
  `PINPAD_LOG_FORMAT=LEGACY|DETAILED` e chamar `tracer.SetDetailed(true)` quando
  `DETAILED`.
- `cmd/libpinpadabecsgo-bridge` não é alterado (permanece opaco).
- `internal/infrastructure/config/config.go`: opcional — a flag de formato fica na
  composição do executável (não no `model.PinpadConfig`), para não ampliar o contrato.

## 9. Segurança

- `classify` aplica `isSensitive` + `DC2 (12h)` + `redact`/`secureSession`.
- Catálogo marca campos sensíveis (`SPE_WKENC`, `SPE_PBKMOD/PBKEXP`, `PP_PINBLK`,
  `PP_KSN`, `PP_TRK1INC/2/3`, `PP_PAN*`, `PP_EMVDATA`); mesmo em Classe A, campo
  sensível é emitido como `[REDACTED]`.
- Nenhuma representação intermediária com dados protegidos é persistida.

## 10. Testes

- `catalog_test.go`: cobertura do catálogo (IDs presentes, nomes corretos, sensibilidade).
- `classify_test.go`: A/B/C (DSP visível; GOX sensível; DC2 criptografado; redact forçado).
- `hexdump_test.go`: offsets, 16 bytes/linha, ASCII com `.`.
- `decode_test.go`: DSP (fixo), GOX (TLV — só envelope), resposta GIX, CRC válido/inválido.
- `detailed_test.go`: `SetDetailed` liga/desliga; B/C não vazam; formato multi-linha.
- Regressão: testes existentes de `logging`/`serial`/`service` seguem passando.


## Revisão 3

ADR-001 substitui a exclusão da integração Bridge: decorador logging.DetailedTransport preserva port.Transport, buffers por direção e correlação por comando validado. Rastro bruto permanece opaco. Composição no main do Bridge e default DETAILED no BAT; sem alterações Android. Ver spec RF-016..020.
