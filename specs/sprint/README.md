# Sprint Planner

O Sprint Planner organiza quando e em qual ordem as Changes serão conduzidas. Ele não substitui a Change, a SPEC ou o workflow Spec Driven 01-06.

## Localização e nomenclatura

- Use `specs/sprint/SPRINT-<numero>-<nome>.md` para uma Sprint concreta.
- Crie o arquivo a partir de `templates/template-sprint.md`.
- Use os documentos em `prompts/` para planejar, acompanhar, preparar a implementação e finalizar a Sprint.
- O backlog da Sprint é formado somente por diretórios em `specs/changes/`; `specs/archive/` é histórico e não entra no planejamento.

## Governança da Change na Sprint

| Status da Change na Sprint | Gate ou situação da Change | Evidência mínima |
| --- | --- | --- |
| `PLANNED` | Contrato em elaboração ou revisão | `proposal.md`, `spec.md`, `DESIGN.md` e `tasks.md` existentes e estado atual registrado |
| `READY` | Contrato aprovado e dependências resolvidas | `proposal.md` e `spec.md` em `SPEC_APROVADA`, `implementation-plan.md` e riscos registrados |
| `IN_PROGRESS` | Implementação, revisão, validação ou correção em andamento | `tasks.md` atualizado e evidências da fase corrente |
| `BLOCKED` | Gate reprovado, dependência pendente, risco sem tratamento ou decisão externa necessária | causa, impacto, evidência e condição objetiva para desbloqueio |
| `DONE` | Workflow integral encerrado | aprovação final, atualização de `system/`, arquivamento, commit e evidências exigidas |
| `REMOVED` | Item retirado do planejamento | decisão e motivo registrados |

Uma Change pertence a, no máximo, uma Sprint `ACTIVE`. O Sprint Planner não muda automaticamente os status da Change nem substitui as evidências registradas nos seus documentos.

## Prioridade e transparência

Ao ordenar a Sprint, priorize nesta sequência: segurança, risco de negócio, testes unitários e testes de integração. Para cada item, registre o risco conhecido, a dependência, o gate atual e a evidência que comprova o avanço. O Sprint Goal precisa indicar o resultado esperado e como ele será demonstrado na Sprint Review.

## Ausência de Sonar ou cobertura configurada

Quando o módulo não possuir Sonar ou ferramenta de cobertura configurados, a Change não é liberada apenas pela ausência da ferramenta. Execute uma **Auditoria de Qualidade Assistida por LLM**: rode os comandos de build, tipo, lint e testes disponíveis; relacione cada artefato de produção alterado aos testes unitários e de integração aplicáveis; e revise defeitos, tratamento de erro, duplicação, código morto, complexidade desnecessária e documentação. Registre escopo, comandos, resultados, achados e correções em `validation.md`.

Essa auditoria substitui o gate operacional ausente, mas não declara percentual de cobertura nem afirma que o Sonar foi executado. Quando Sonar ou cobertura estiverem configurados, seus resultados continuam obrigatórios.

## Aplicabilidade da auditoria de segurança

A Skill `security-audit` é obrigatória quando a Change alterar código, API, autenticação, autorização, configuração, dependência, segredo, integração, frontend ou backend. Para Change exclusivamente documental, registre em `validation.md` os artefatos inspecionados e a não aplicabilidade; não gere nem reutilize PDF histórico de segurança como evidência.
