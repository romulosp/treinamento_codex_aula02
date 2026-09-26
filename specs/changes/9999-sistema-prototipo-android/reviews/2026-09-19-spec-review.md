# Revisão da SPEC: 9999-sistema-prototipo-android

## Data e escopo

- Data: 2026-09-19.
- Fase avaliada: revisão da SPEC.
- Artefatos revisados: `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, especificações complementares, `inventario-origem.md`, `migration-matrix.md`, `sources-and-decisions.md`, `implementation-plan.md`, `validation.md` e `source-material/README.md`.
- Regras verificadas: `AGENTS.md`, `specs/shared/process/workflow.md`, `specs/shared/process/evidence-conventions.md` e Skill `spec-review`.
- Limite da revisão: nenhuma decisão de produto foi presumida e nenhum código foi implementado.

## Matriz de verificabilidade

| Item | Evidência | Resultado |
| --- | --- | --- |
| Objetivo, identidade e diretório | `proposal.md` e RF-001 definem plataforma, caminho, grupo, `namespace` e `applicationId`. | Aprovado |
| Escopo e fora de escopo | `proposal.md` separa protótipo visual, menu demonstrativo e exclusões de negócio, integrações e publicação. | Aprovado |
| Arquitetura | `DESIGN.md` define Compose, fluxo unidirecional, módulo único e layout adaptativo. | Aprovado |
| Famílias e comportamentos | RF-003 e as especificações complementares descrevem componentes, estados e critérios específicos. | Aprovado com bloqueios de ativos descritos abaixo |
| Testabilidade | CA-001 a CA-010 e as especificações complementares preveem testes unitários, UI, screenshots, acessibilidade e emuladores. | Aprovado com bloqueio de `minSdk` |
| Dependências e decisões | A própria SPEC mantém decisões de plataforma, distribuição, direitos de uso e recursos ausentes como pendentes. | Reprovado |

## Achados

### REV-001 — Bloqueante — `minSdk` permanece sem decisão

- Evidência: `spec.md`, em **Premissas e decisões pendentes**, marca `minSdk = 26` como `NEEDS_DECISION`; `spec-fundacao-aplicativo.md` repete a pendência; `tasks.md` exige resolvê-la antes de `SPEC_APROVADA`.
- Impacto: a configuração Gradle, a faixa suportada e o nível mínimo de emulador não possuem contrato autoritativo. A implementação e a validação de RNF-003, RNF-006 e CA-004A podem produzir resultados incompatíveis com a decisão posterior.
- Recomendação: selecionar e registrar um único `minSdk`, com a justificativa de compatibilidade e a matriz mínima de validação correspondente.

### REV-002 — Bloqueante — Forma de distribuição permanece indefinida

- Evidência: `spec.md` marca a distribuição interna, privada ou Google Play como `NEEDS_DECISION`; `sources-and-decisions.md` condiciona a obrigatoriedade de `targetSdk = 36` à Google Play e classifica o mesmo valor apenas como recomendado para distribuição interna; `tasks.md` exige resolver a forma de distribuição antes da aprovação.
- Impacto: o fundamento de RNF-001 não está fechado e não é possível avaliar integralmente requisitos de empacotamento, política de plataforma e evidências necessárias para o canal escolhido.
- Recomendação: escolher o canal de distribuição deste incremento e consolidar no contrato as consequências verificáveis dessa escolha.

### REV-003 — Bloqueante — Direitos de uso dos ativos e fontes não foram comprovados nem substituídos

- Evidência: `proposal.md` registra risco de licença, marca e distribuição; `spec.md` marca a comprovação como `NEEDS_DECISION`; `sources-and-decisions.md` informa que a presença em `source-material/` não autoriza distribuição no APK; `spec-tema-assets-tipografia.md` proíbe copiar recursos sem aprovação; `tasks.md` mantém essa resolução como pré-condição aberta.
- Impacto: a implementação não pode determinar quais imagens e fontes entram em `app/src/main/res`, e os critérios visuais, de tipografia, rastreabilidade e screenshot não têm conjunto autorizado de referência executável.
- Recomendação: registrar licença ou autoridade de uso por ativo/família, ou aprovar explicitamente substitutos neutros e fallbacks para cada conjunto não autorizado.

### REV-004 — Bloqueante — Tratamento das 118 referências visuais ausentes não está fechado

- Evidência: `inventario-origem.md` identifica 118 caminhos declarados sem arquivo local; `spec.md` os marca como `NEEDS_EVIDENCE`; `migration-matrix.md` admite `ADIAR` ou `SUBSTITUIR`, mas permanece parcial e não escolhe a decisão por referência; `tasks.md` exige decidir o tratamento antes da aprovação.
- Impacto: RF-002, RF-003, RF-004, RF-007, CA-003, CA-005 e CA-009 não possuem escopo determinístico para variantes afetadas. A implementação teria de inventar exclusões ou substituições durante a codificação.
- Recomendação: completar a matriz das referências ausentes com decisão individual rastreável (`ADIAR` ou `SUBSTITUIR`), justificativa e evidência esperada; fornecer os arquivos quando a decisão for migrar a variante.

## Conclusão

`REPROVADA`

Os achados `REV-001` a `REV-004` são materiais e impedem `SPEC_APROVADA`. A Change deve retornar à fase de especificação, resolver as quatro decisões sem ampliar o escopo e ser submetida a uma nova revisão formal. Planejamento técnico, implementação, revisão da implementação, validação final, aprovação, arquivamento e commit permanecem proibidos.
