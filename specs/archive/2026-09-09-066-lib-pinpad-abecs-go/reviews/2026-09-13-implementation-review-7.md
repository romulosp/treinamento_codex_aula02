# Revisão da implementação — decodificação GTK em claro

**Data:** 2026-09-13

**Estado de entrada:** `IMPLEMENTADA`

## Rastreabilidade

| Regra ABECS 2.12 | Implementação | Teste |
| --- | --- | --- |
| Trilha 1 em ASCII, seção 5.4.2.1 | Preservada como texto e escapada por `QuoteToASCII` | `TestTracerRecordsGTKClearTracksAsOneEscapedLine` |
| Trilhas 2/3 com um símbolo por nibble, seção 5.4.2.2 | `decodeGTKClearNumericTrack` expande cada byte | `TestDecodeGTKClearNumericTrack` |
| Separador | Nibble `D` produz `=` | Vetor físico e vetor legível solicitado |
| Filler | Um ou mais nibbles `F` são aceitos somente no final e removidos | Casos de filler simples/múltiplo e dado posterior inválido |
| Entrada inválida | Nibbles `A`, `B`, `C`, `E`, dígito ou `D` após filler retornam erro | Casos negativos orientados a tabela |
| Modo criptografado | Continua sem chamar o registro em claro | `TestRecordGTKResultLogsOnlyClearTracks` |

O vetor físico `54 28 20 60 97 98 40 97 D2 11 12 01 38 29 95 58 46 37 0F`
é convertido para `5428206097984097=21112013829955846370`. A implementação não
interpreta o restante da trilha além da transformação normativa dos símbolos.

Nenhuma divergência material foi encontrada.

## Decisão

`IMPLEMENTACAO_APROVADA`
