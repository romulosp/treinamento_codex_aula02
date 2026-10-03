# Revisão preparatória de consolidação — 074 Pinpad Android Bridge Stack

## Status

REVISÃO PREPARATÓRIA — PENDENTE DE APROVAÇÃO HUMANA

Este relatório registra a análise solicitada pelo autor. Não é aprovação formal
da SPEC e não autoriza implementação.

## Escopo revisado

Foram confrontados:

- proposal, spec, DESIGN, contratos transversais, tarefas e evidências das
  Changes 066, 067, 070 e 072;
- código atual Go, Bridge, transportes, fachada mobile, scripts e laboratório
  Android;
- workflow em specs/shared/process/workflow.md e regras do AGENTS.md.

## Achados

### REV-074-001 — ID solicitado indisponível

- Severidade: importante.
- Evidência: existe specs/archive/2026-09-28-073-remover-acoes-duplicadas-catalogo.
- Impacto: reutilizar 073 quebraria unicidade e rastreabilidade.
- Recomendação: manter 074 e registrar a decisão na proposal, DESIGN e
  validation.
- Estado: tratado documentalmente.

### REV-074-002 — Configuração atual ainda possui defaults fixos de COM

- Severidade: bloqueante para implementação/validação.
- Evidência: `apps/desktop/libpinpadabecsgo/start_aplication.bat:7` e
  `apps/desktop/libpinpadabecsgo/internal/domain/model/models.go:17`.
- Impacto: contradiz a regra consolidada de obter a porta exclusivamente de
  `PORTA_PINPAD` e pode produzir comunicação contra dispositivo incorreto.
- Recomendação: remover os defaults fixos e exigir `PORTA_PINPAD` em todos os
  entrypoints na implementação futura aprovada.
- Estado: não tratado por escopo.

### REV-074-003 — RST permanece na superfície REST

- Severidade: importante.
- Evidência: internal/api/handler.go e internal/api/dto/dto.go.
- Impacto: pode sugerir que RST ainda é comando ABECS, contradizendo a
  conformidade v2.12 de 066.
- Recomendação: decidir na aprovação se a rota fica como alias explícito de
  CAN/EOT ou se é removida; nunca serializar RST.
- Estado: decisão proposta em analysis.md e spec.md; aprovação humana pendente.

### REV-074-004 — Falha atual no teste de integração do EmulatorTransport

- Severidade: bloqueante para validação.
- Evidência: go test -tags=integration ./... falha em
  TestCloseAckVariants/legacy_eof com read/write on closed pipe.
- Impacto: o estado atual não tem validação integral de fechamento/reabertura.
- Recomendação: diagnosticar e corrigir em uma implementação futura aprovada,
  com novo teste determinístico.
- Estado: não tratado por escopo.

### REV-074-005 — Race detector indisponível

- Severidade: importante.
- Evidência: go test -race ./... informa incompatibilidade com windows/386.
- Impacto: ausência de evidência de race no ambiente observado.
- Recomendação: executar em toolchain/arquitetura suportada antes da validação.
- Estado: limitação registrada.

### REV-074-006 — Lacuna de escopo Android/UI e TransactionGCX

- Severidade: importante.
- Evidência: 067 exclui a UI do laboratório; 066 mantém TransactionGCX completo
  como ErrNotImplemented; 072 valida o laboratório como consumidor.
- Impacto: sem decisão explícita, “subsistema completo” poderia ser interpretado
  como incluir um produto de pagamento Android ou inventar o contrato GCX.
- Recomendação: aprovar a leitura de que 074 fecha o caminho AAR/Bridge e o
  laboratório consumidor, mas preserva UI como consumidora e TransactionGCX
  completo como lacuna formal.
- Estado: proposta em L-001 e L-004; aprovação humana pendente.

## Veredito preparatório

A consolidação é tecnicamente coerente e está documentada como baseline
autocontida, mas permanece em EM_REVISAO_SPEC. Não há SPEC_APROVADA,
IMPLEMENTADA, VALIDADA ou APROVADA nesta Change.
