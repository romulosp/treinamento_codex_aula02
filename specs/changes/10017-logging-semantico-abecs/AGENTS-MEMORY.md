# AGENTS-MEMORY — 10017-logging-semantico-abecs

> Memória operacional de continuidade. Leia antes de retomar esta Change.

## Estado atual (2026-10-15)

- **`IMPLEMENTADA`** — SPEC v2.0.0 aprovada e implementada; testes/vet/build aprovados.
- Próximos gates: revisão da implementação (`IMP-REV-*`) → validação (`VAL-*`) → aprovação → arquivamento.
- `DESIGN.md`, `tasks.md`, `proposal.md`, `spec.md`, `reviews/` e esta memória existem.

## O que foi implementado (em `apps/desktop/libpinpadabecsgo/internal/infrastructure/logging/`)

| Arquivo | Responsabilidade |
|---|---|
| `catalog.go` | Metadados por campo (`SPE_xxx`, `RSP_DATID`) e por comando, do PDF ABECS 2.12. |
| `classify.go` | `FrameClass` A/B/C (visível/sensível/criptografado). |
| `hexdump.go` | HEX + ASCII + offset (16 bytes/linha). |
| `decode.go` | Decodificação semântica (TLV ABECS + clássico DSP/DEX + resposta) + CRC diagnóstico. |
| `detailed.go` | `Tracer.SetDetailed`/`SetBytesPerLine` + bloco multi-linha (Classe A). |
| `logging.go` | `Tracer` ganhou `detailed`/`bytesPerLine`; roteamento SPE/PP para o modo detalhado. |
| `cmd/libpinpadabecsgo/main.go` | Lê `PINPAD_LOG_FORMAT=LEGACY|DETAILED` em `configureTracer`. |

## Decisões técnicas firmadas (não reabrir sem novo ADR)

1. **Logger decorativo**: consome `kind`/`status`/`redact`/sessão segura; não re-deriva.
2. **Classificação A/B/C** antes de formatar; falha → B. Comandos sensíveis: `MLR, TLR, GCX, GTK, GOX, FCX, GPN`.
3. **Catálogo = extensão** do existente, com nomes do PDF. `SPE_WKENC`/`SPE_EMVDATA` e campos `PP_PINBLK/KSN/EMVDATA/TRK*/PAN*` marcados sensíveis.
4. **DC3 (substituição)** e **DC2 (sessão segura)** sinalizados; payload criptografado nunca decodificado.
5. **DSP/DEX são clássicos (schema fixo)**; demais comandos ABECS são TLV. OPN/GPN têm resposta posicional.

## Teste manual (com pinpad físico)

1. Compilar: `go build -o bin/libpinpadabecsgo.exe ./cmd/libpinpadabecsgo` (em `apps/desktop/libpinpadabecsgo`).
2. Configurar ambiente:
   ```powershell
   $env:PORTA_PINPAD = "COM7"
   $env:PINPAD_LOG_FILE = "<abs>\logs\LogPinpadAbecs.txt"
   $env:PINPAD_LOG_FORMAT = "DETAILED"
   ```
3. Executar o menu e navegar; conferir o log: comandos não sensíveis (OPN, GIX, DSP, MNU, DSI…) saem em bloco `HEX` + `COMANDO` + `CRC`; GCX/GOX/GTK/GPN permanecem `**REDACTED(n bytes)**`.

## Discrepâncias conhecidas (reconciliar no interpretador)

- `0024h/0025h`: código `PinBlockMode/PinBlockExponent` vs PDF `SPE_PBKMOD/PBKEXP`.
- `8032h/8033h`: código `MerchantDescription/MerchantData` vs PDF `PP_MKTDESP/PP_MKTDESD`.
- 22 comandos documentados ainda não implementados na lib (ver memória do protocolo).

## Limites de dados sensíveis (inalteráveis)

- Nunca registrar PIN, chaves, KSN, trilhas, PAN, dados EMV sensíveis, blocos criptográficos.
- Extração integral do PDF mantida no projeto: [abecs212.txt](../../../docs/Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212/abecs212.txt). Internalizada pela Change 10018; o PDF permanece canônico.

## Referências

- PDF: `docs/Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212/Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212.pdf`
- Memória do protocolo: `docs/Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212/abecs-212-referencia-protocolo.md`
- Código: `apps/desktop/libpinpadabecsgo/`
- SPEC 066: `specs/archive/2026-09-09-066-lib-pinpad-abecs-go/`

## Marco mais recente — revisão 3 (2026-10-15)

Usuário solicitou completar o formato no caminho real BAT/Bridge. RF-016..020,
ADR-001 e revisão 3 SPEC_APROVADA registrados antes de código. Plano desta revisão
criado antes da implementação; não retroage ao plano ausente do trabalho anterior.
Bridge mantém serial opaca, com observador adicional limitado e validação antes
de detalhar. BAT default DETAILED, override LEGACY preservado. Tracer registra
TRACE FORMAT. Campos/offsets/encoding/status/controles/DC3/CRC agora explícitos.
Go all/integration/vet/build PASS; logging 82,3%; smoke do exe compilado PASS
com 8 blocos (OPN/GIX/DSP/CLO SPE+PP), amostra sintética em evidence/.
SHA256 exe BEFF95771D8071984469A922788825274BC87ED9C422E3B9362C5D92FC286411.
Novo Bridge físico iniciado oculto PID4828 em 127.0.0.1:39100, COM7/19200/30s,
DETAILED e log habitual em append; emulator-5554 reverse confirmado. Inicialização
sem OPN/compra. PIDs históricos: verificar listener antes de qualquer ação.
Próximo: teste humano no aplicativo. Não repetir compra automaticamente,
não iniciar outro Bridge ativo, não expor criptogramas/PAN/trilhas. Sem rebuild
Android (wire protocol/Go mobile não alterados). Manter IMPLEMENTADA;
sem revisão formal, validação formal, aprovação, commit ou archive nesta fase.