# Revisão final da SPEC — Android Native Engineering

**Data:** 2026-09-18  
**Escopo:** `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, `research.md`, `implementation-plan.md`, `validation.md`, revisões anteriores e estrutura implementada em `.agents/skills/android-native-engineering/`.  
**Resultado:** `SPEC_APROVADA`

## Verificação do contrato

- Objetivo, escopo e fora de escopo estão delimitados e não incluem a criação de um aplicativo Android.
- Requisitos funcionais e não funcionais cobrem governança, fontes temporais, composição, perfis, arquitetura, qualidade, segurança, recuperação, evidências e validação estrutural.
- Dependências, riscos e critérios de aceite possuem meios de verificação explícitos.
- `DESIGN.md` mantém uma skill orquestradora pequena e cinco referências por progressive disclosure.
- Não foi identificada decisão arquitetural adicional que exija ADR neste incremento.

## Reavaliação dos achados bloqueadores

### REV-001 — Registro obrigatório de candidatas

- **Severidade original:** alta
- **Evidência da correção:** `research.md` registra `SKILL-CANDIDATE-001` a `SKILL-CANDIDATE-005` com classificação, origem, mantenedor, commit, licença, problema, sobreposição, aderência, impacto, riscos, evidências e decisão.
- **Impacto após correção:** `RF-011` e `CA-007` tornaram-se auditáveis; nenhuma skill externa foi importada.
- **Recomendação:** atendida. Repetir a classificação quando uma nova candidata for efetivamente analisada.
- **Situação:** resolvido.

### REV-002 — Evidência temporal do catálogo oficial

- **Severidade original:** média
- **Evidência da correção:** `research.md` registra consulta em 2026-09-18, URLs oficiais, licença Apache-2.0 e commit `b1f707d90904129b5972b3cc6436b568583effe5`; `validation.md` registra os comandos e resultados da consulta.
- **Impacto após correção:** a triagem das skills oficiais pode ser reproduzida e revisada no tempo.
- **Recomendação:** atendida. Changes consumidoras devem repetir a consulta porque o catálogo é temporal.
- **Situação:** resolvido.

### REV-003 — Evidência dos fixtures do validador

- **Severidade original:** média
- **Evidência da correção:** `validation.md` contém ambiente, bloco de criação reproduzível, caminhos da execução, comandos literais, saídas e códigos. O fixture válido retornou `0`; o fixture equivalente sem Wrapper retornou `1` e somente o diagnóstico esperado.
- **Impacto após correção:** `CA-004` é integralmente reproduzível.
- **Recomendação:** atendida.
- **Situação:** resolvido.

### REV-004 — Sincronização dos status

- **Severidade original:** média
- **Evidência da correção:** os bloqueios da revisão complementar foram resolvidos, `tasks.md` registra as ações concluídas e esta revisão restabelece formalmente `SPEC_APROVADA`.
- **Impacto após correção:** `proposal.md`, `spec.md`, tarefas, validação e decisão vigente voltam a representar os gates executados.
- **Recomendação:** atendida. A revisão complementar permanece como histórico da reprovação anterior.
- **Situação:** resolvido.

## Decisão

Não há achado material aberto. A SPEC está completa, coerente, verificável e limitada ao primeiro incremento aprovado.

## Extensão revisada — KDoc obrigatório

- **Requisito:** `RF-012` exige KDoc em português do Brasil para declarações Kotlin públicas/protegidas e para contratos internos/privados não óbvios.
- **Verificabilidade:** `CA-008` exige referência operacional, inventário das declarações alteradas, documentação das aplicáveis e justificativa das exclusões.
- **Coerência:** `RNF-004` separa a obrigatoriedade do KDoc da instalação contextual de Dokka, evitando dependência não aprovada.
- **Fonte:** documentação e convenções oficiais do Kotlin consultadas em 2026-09-18 e registradas em `research.md`.
- **Achados:** nenhum achado material; não há necessidade de ADR.
- **Decisão:** extensão aprovada para implementação.

`SPEC_APROVADA`
