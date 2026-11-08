# Revisão da SPEC — 10017-logging-semantico-abecs

**Data:** 2026-10-15
**Change:** `10017-logging-semantico-abecs`
**Artefato revisado:** `spec.md` (V.2.0.0) + `proposal.md` + `AGENTS-MEMORY.md`
**Skill:** `spec-review`
**Referências de processo:** `specs/shared/process/workflow.md`

## Escopo da revisão

Revisão formal da SPEC consolidada V.2.0.0, que sucede a V.0.0.0.1 (reprovada em
revisão anterior) e foi rebaseada no PDF ABECS 2.12 e no código existente em
`apps/desktop/libpinpadabecsgo/`.

## Verificação de conteúdo

Confirmados como **corretos e verificáveis**:

- Objetivo, escopo e fora de escopo estão delimitados e coerentes com o baseline.
- A direção arquitetural (logger decorativo, consumir `kind`/`status`/redação/sessão segura) é compatível com o código (`service.go:1585-1596`, `logging.go:397-404`).
- Tabela de bytes de controle corrigida (SYN/ETB/DC3/DC2, sem STX/ETX) — conforme PDF §2.2.
- Modelo de classificação A/B/C resolve a contradição de segurança da V.0.0.0.1.
- Catálogo como extensão do existente (não novo) — conforme `command/constants.go`, `protocol/constants.go`, `state/status.go`.
- Matriz de cobertura por grupo e sensibilidade presente.

## Achados

### REV-001 — MÉDIA — Contrato incompleto: faltam `DESIGN.md` e `tasks.md`

- **Evidência:** `specs/shared/process/workflow.md` exige `proposal.md, spec.md, DESIGN.md, tasks.md` como entrada da fase "Especificação" e "contrato completo" para entrar em `EM_REVISAO_SPEC`. A pasta `specs/changes/10017-logging-semantico-abecs/` contém apenas `proposal.md`, `spec.md` e `AGENTS-MEMORY.md`.
- **Impacto:** o gate "Revisão da SPEC" não pode ser concluído como `SPEC_APROVADA` sem o contrato completo.
- **Recomendação:** produzir `DESIGN.md` (estrutura dos módulos de decoração, integração com `logging.Tracer`, detalhamento do formato `DETAILED`, política de fila assíncrona) e `tasks.md` (decomposição das Fases 1–7), e reexecutar esta revisão.

### REV-002 — BAIXA — Encoding dos campos de texto não confirmado no PDF

- **Evidência:** o PDF não declara explicitamente o encoding dos campos `S` (ex.: `SPE_DSPMSG` em §3.1.3.1 e §3.3.5 usa "formato livre, com quebras `0Dh`"). A SPEC usa "ISO-8859-1" nos exemplos (DSP/GOX).
- **Impacto:** risco de interpretação textual incorreta para bytes ≥ 0x80.
- **Recomendação:** tratar o encoding como **item derivado do catálogo** (por campo) com fallback seguro `DESCONHECIDA → NAO_INTERPRETADO`; registrar a incerteza até confirmar com hardware/manual.

### REV-003 — BAIXA — Matriz de cobertura em nível de grupo, não per-comando

- **Evidência:** §8 da SPEC apresenta a matriz por grupo; o critério de aceite (§14) exige "matriz de cobertura com os 46 comandos". A tabela individual está na memória (`abecs-212-referencia-protocolo.md` §6), não na SPEC.
- **Impacto:** SPEC não é autocontida para o critério de aceite.
- **Recomendação:** tornar a tabela de 46 comandos um anexo normativo da SPEC ou referenciá-la explicitamente como fonte obrigatória do critério.

### REV-004 — BAIXA — Seção de dependências/riscos ausente

- **Evidência:** a V.0.0.0.1 possuía "Dependências e riscos"; a V.2.0.0 removeu. O workflow (§2 da skill) pede confirmação de dependências e riscos.
- **Impacto:** rastreabilidade de decisões reduzida.
- **Recomendação:** reintroduzir uma seção curta de riscos (segurança, desempenho, encoding, divergência de nomes) com mitigação.

### REV-005 — BAIXA — Formato `DETAILED` (multi-linha × trace de linha única) implícito

- **Evidência:** a SPEC define o que emitir, mas não como o bloco multi-linha convive com o formato atual de linha única (`[COM7#001] SPE … FUNC=… DATA_HORA=…`).
- **Impacto:** ambiguidade de implementação.
- **Recomendação:** definir, no `DESIGN.md`, que cada bloco `DETAILED` preserve sessão/direção/`FUNC`/`DATA_HORA` e o contrato de intercalação com o modo `LEGACY`.

## Pontos fortes (resolvidos em relação à V.0.0.0.1)

- Contradição "interpretar tudo" × redação: resolvida via A/B/C.
- Sessão segura (DC2) e substituição (DC3): agora requisitos explícitos.
- Catálogo: de "criar" para "estender o existente".
- Arquitetura: de "reconstruir/identificar no logger" para "decorar/consumir metadados".
- Tabela de controles corrigida.

## Parecer

**`REPROVADA`** (não por falha de conteúdo da `spec.md`, que está tecnicamente
sólida, mas porque o **contrato está incompleto**: faltam `DESIGN.md` e
`tasks.md`, exigidos pelo `workflow.md` antes do gate `EM_REVISAO_SPEC`, além de
quatro itens de baixa severidade a fechar).

**Caminho para aprovação:**
1. Criar `DESIGN.md` e `tasks.md` (fechar REV-001 e REV-005).
2. Ajustar `spec.md` para REV-002/003/004 (encoding por catálogo, matriz per-comando/autocontida, seção de riscos).
3. Reexecutar a revisão → expectativa de `SPEC_APROVADA`.
