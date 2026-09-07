# SPEC: 033-consolidar-governanca-sprint-planner

## Status
`SPEC_APROVADA`

## Referências e dependências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `specs/shared/testing/testing-strategy.md`
- `.agents/skills/security-audit/SKILL.md`

## Requisitos funcionais

1. Deve existir `specs/sprint/README.md` com localização, nomenclatura, origem do backlog e mapeamento entre status da Change na Sprint, gates e evidências.
2. Sprints concretas devem usar `specs/sprint/SPRINT-<numero>-<nome>.md`; somente diretórios em `specs/changes/` podem entrar no backlog.
3. O workflow deve exigir `implementation-plan.md` depois de `SPEC_APROVADA` e antes da implementação, sem criar uma nova fase ou alterar os gates 01-06.
4. O template e os prompts de Sprint devem registrar gate, evidência, dependência, risco, critério de sucesso do Sprint Goal e a condição objetiva para avançar ou desbloquear uma Change.
5. A ordem de planejamento deve priorizar segurança, risco de negócio, testes unitários e testes de integração.
6. Mudanças sem Sonar ou ferramenta de cobertura configurados devem executar Auditoria de Qualidade Assistida por LLM, registrada em `validation.md` com módulo, escopo, arquivos, comandos, resultados, mapeamento de testes, achados e correções. A auditoria não pode declarar percentual de cobertura nem aprovação do Sonar.
7. Se Sonar ou cobertura estiverem configurados, seus resultados permanecem obrigatórios; a auditoria assistida não os substitui.
8. A auditoria de segurança deve analisar as categorias aplicáveis aos artefatos de frontend e backend modificados. Para Change somente documental, `validation.md` deve registrar que não houve artefato aplicável; não é exigido PDF de segurança.
9. Achado de segurança confirmado e corrigível dentro da SPEC deve retornar autonomamente à implementação, revisão, validação e nova auditoria. Caso exija alteração de SPEC, ação externa ou decisão fora do escopo, a Change fica `BLOQUEADA`.

## Requisitos não funcionais

1. Documentos em português do Brasil, Markdown simples e rastreável.
2. Sem criação ou alteração de código de aplicação, testes de aplicação, configurações de build, infraestrutura ou Sprint concreta.
3. Sem segredos reais em evidências ou relatórios.

## Regras de negócio

1. `READY` exige contrato `SPEC_APROVADA`, plano técnico, dependências resolvidas, riscos registrados e ausência de bloqueio conhecido.
2. `DONE` exige todas as evidências do workflow, qualidade aplicável, segurança aplicável, aprovação final, atualização de `system/`, arquivamento e commit posterior à aprovação.
3. `BLOCKED` exige causa, impacto, evidência e condição objetiva para resolução.
4. A Sprint não altera automaticamente o status administrativo da Change.

## Cenários e critérios de aceite

- [ ] CA-001: `specs/sprint/README.md` documenta nomenclatura, backlog e mapeamento de todos os estados da Change na Sprint.
- [ ] CA-002: `workflow.md` e o orquestrador mencionam `implementation-plan.md` como preparação sem nova fase.
- [ ] CA-003: template e prompts exigem evidência por gate, risco, testes unitários e de integração, Review e retrospectiva.
- [ ] CA-004: quando não houver Sonar/cobertura, o fallback LLM é exigido e não declara métrica ou ferramenta inexistente.
- [ ] CA-005: a auditoria de segurança exige PDF atual apenas quando houver artefato aplicável e registra não aplicabilidade em Change somente documental.
- [ ] CA-006: nenhuma alteração ocorre em `apps/`, `docs/security-audit/`, POMs, `package.json` ou infraestrutura.
- [ ] CA-007: `git diff --check` termina com código de saída `0`.
