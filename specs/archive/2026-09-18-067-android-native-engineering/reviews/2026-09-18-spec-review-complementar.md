# Revisão complementar da SPEC — Android Native Engineering

**Data:** 2026-09-18  
**Escopo:** artefatos da Change e implementação em `.agents/skills/android-native-engineering/`.  
**Resultado:** `REPROVADA`

## Pontos verificados

- Objetivo e escopo coerentes com o primeiro incremento: skill orquestradora, referências e validador estrutural, sem aplicativo Android.
- Arquitetura e perfis coerentes com o menor nível de complexidade suficiente.
- Requisitos de fontes temporais, evidências, segurança e recuperação controlada estão expressos.
- A implementação observada contém `SKILL.md`, `agents/openai.yaml`, quatro referências e `scripts/validate_android_project.py`, alinhados ao `DESIGN.md`.

## Achados

### REV-001 — Registro obrigatório de candidatas incompleto

- **Severidade:** alta
- **Evidência:** `RF-011` exige, para cada candidata, classificação, origem, mantenedor, versão/commit, licença, problema, sobreposição, aderência, impacto, riscos, evidências e decisão. `research.md` contém apenas `DEC-001` a `DEC-003` e a afirmação de que nenhuma skill foi importada; não existe uma seção `SKILL-CANDIDATE-XXX` preenchendo esses campos para as skills oficiais consultadas.
- **Impacto:** não é possível auditar se `REUSE/EXTEND/CREATE/REJECT` foi aplicado a cada candidata. O `CA-007` permanece não demonstrado.
- **Recomendação:** adicionar em `research.md` um registro por candidata relevante, inclusive para a conclusão `REJECT` ou para a decisão de mantê-la apenas como fonte, preenchendo os campos exigidos por `sources-and-decisions.md`.

### REV-002 — Evidência de consulta ao catálogo oficial não é reproduzível

- **Severidade:** média
- **Evidência:** `research.md` afirma que o catálogo e o repositório oficial foram consultados, mas não registra data/hora, itens/caminhos analisados nem o resultado da triagem. `validation.md` registra apenas a ausência do Android CLI.
- **Impacto:** a decisão de compor antes de duplicar não pode ser reproduzida nem revisada temporalmente.
- **Recomendação:** registrar URLs oficiais, data da consulta, versão/commit quando disponível, candidatas identificadas e motivo da decisão para cada uma.

### REV-003 — Critérios de aceite do validador não estão totalmente evidenciados

- **Severidade:** média
- **Evidência:** `CA-004` exige aceitar um fixture mínimo válido e rejeitar fixture sem Gradle Wrapper. `validation.md` descreve os resultados, mas não informa caminhos dos fixtures, comando literal completo, versão do Python ou diagnósticos do caso inválido.
- **Impacto:** o aceite não é integralmente reproduzível.
- **Recomendação:** registrar caminhos/cenários, comandos completos, versões, saída essencial e códigos de saída; manter os fixtures ou descrever sua criação de modo reproduzível.

### REV-004 — Status dos artefatos está adiantado em relação à revisão atual

- **Severidade:** média
- **Evidência:** `proposal.md`/`spec.md` declaram `SPEC_APROVADA`, enquanto `tasks.md` declara `IMPLEMENTADA` e `validation.md` declara `VALIDADA`, mas esta revisão encontra pendências materiais em `RF-011`/`CA-007`.
- **Impacto:** os status sugerem gates concluídos apesar da revisão atual não poder aprovar a SPEC.
- **Recomendação:** corrigir os achados, executar nova revisão formal e sincronizar os status conforme o workflow.

## Decisão

`REPROVADA`

A mudança não deve avançar para implementação adicional ou encerramento enquanto `REV-001` não for resolvido. `REV-002` e `REV-003` também devem ser resolvidos para tornar os critérios auditáveis. Nenhum código foi alterado nesta revisão.

## Situação posterior

Os quatro achados foram corrigidos em 2026-09-18. A decisão vigente está registrada em `2026-09-18-spec-review.md`; esta revisão complementar permanece como histórico do gate reprovado.
