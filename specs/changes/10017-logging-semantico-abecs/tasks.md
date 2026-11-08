# Tarefas: 10017-logging-semantico-abecs

> **Estado: `IMPLEMENTADA`** — 2026-10-15.
> Evidência: `go test ./... -count=1` (27 pacotes), `go vet ./...` e
> `go build ./...` aprovados (exit 0).

## Fase 1 — Catálogo e classificação

- [x] **T1.1** `catalog.go`: `speField` (SPE_xxx 0x0001–0x0026), `rspField` (RSP_DATID), `commandMeta`; nomes/formatos/sensibilidade do PDF.
- [x] **T1.2** `catalog_test.go`: presença de IDs, nomes oficiais e sensibilidade.
- [x] **T1.3** `classify.go`: `FrameClass` A/B/C reutilizando `isSensitive` + `DC2` + `redact`.
- [x] **T1.4** `classify_test.go`: DSP→A, GOX/GTK→B, DC2→C, redact→B.

## Fase 2 — Formatador e decodificador

- [x] **T2.1** `hexdump.go`: HEX + ASCII + offset (16 bytes/linha).
- [x] **T2.2** `hexdump_test.go`: offsets, quebra de linha, não-imprimível → `.`.
- [x] **T2.3** `decode.go`: `decodeFrame` (TLV ABECS + clássico DSP/DEX + resposta) + CRC diagnóstico.
- [x] **T2.4** `decode_test.go`: DSP fixo, MNU TLV, GIX resposta, CRC inválido, pacote incompleto.

## Fase 3 — Integração e configuração

- [x] **T3.1** `detailed.go`: `SetDetailed`/`SetBytesPerLine` no `Tracer`; bloco multi-linha para Classe A.
- [x] **T3.2** `detailed_test.go`: liga/desliga; sensível não vaza; LEGACY padrão.
- [x] **T3.3** `cmd/libpinpadabecsgo/main.go`: `PINPAD_LOG_FORMAT=LEGACY|DETAILED` em `configureTracer`.

## Fase 4 — Qualidade e evidência

- [x] **T4.1** `go test ./... -count=1` + `go vet ./...` + `go build ./...` (exit 0).
- [x] **T4.2** Regressão: testes existentes de `logging`/`serial`/`service` inalterados e aprovados.
- [x] **T4.3** Segurança: `TestTracerDetailedSensitiveStaysRedacted` garante Classe B sem detalhamento.
- [x] Registrar comandos, ambiente e códigos de saída da implementação em `validation.md` (2026-10-15); todos passaram.
- [x] **T4.4** Matriz documental das 46 entradas em evidence/matriz-comandos.md; comandos sem catálogo redigidos. Validação formal posterior ao teste humano.


## Revisão 3 — caminho real

Estado da revisão 3: `IMPLEMENTADA` — testes e binário disponíveis em 2026-10-15.
- [x] Registrar contrato aprovado, ADR e plano antes de código.
- [x] Implementar observador, configuração e default no BAT.
- [x] Testar comportamento, integração e executar gates.
- [x] Preparar binário/retomada e registrar evidências e memória.
