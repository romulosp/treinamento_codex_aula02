# Plano técnico: 033-consolidar-governanca-sprint-planner

## Fatos observados

- O workflow canônico possui seis gates de Change e encerramento por commit.
- O Sprint Planner já possui template e quatro prompts, mas não possui guia de descoberta ou mapeamento formal entre estados e evidências.
- Os módulos atuais não possuem Sonar configurado de forma uniforme e a cobertura não está disponível em todos os módulos.
- O gerador de relatório de segurança existente possui conteúdo histórico; ele não é prova suficiente de auditoria para uma Change sem artefatos de aplicação no escopo.

## Impactos prováveis

- `specs/sprint/README.md`, template e prompts de Sprint.
- `specs/shared/process/workflow.md`, `specs/README.md` e orquestrador `.github`.
- Documentos de Change desta mudança e, no encerramento, `specs/system/README.md` e `STATUS.md`.

## Estratégia de implementação

1. Consolidar guia, nomenclatura, mapeamento de gates e prioridade de risco no Sprint Planner.
2. Registrar o plano técnico como artefato preparatório sem criar uma fase adicional.
3. Definir auditoria de qualidade assistida por LLM como fallback verificável para Sonar/cobertura ausentes.
4. Exigir segurança apenas quando houver artefato de frontend/backend aplicável e registrar não aplicabilidade para esta Change documental.
5. Validar o conteúdo por verificações estáticas e por `git diff --check`.

## Testes, cobertura e qualidade

- Não há código de produção alterado; testes unitários, integração e cobertura são não aplicáveis.
- A validação verificará a presença dos artefatos, requisitos textuais, ausência de alterações em `apps/` e espaços em branco inválidos.
- A Auditoria de Qualidade Assistida por LLM analisará consistência entre a SPEC, workflow, template, prompts e orquestrador.

## Auditoria de segurança

- Não há artefato de frontend/backend, contrato de API, autenticação, configuração, dependência ou segredo no escopo desta Change.
- A validação registrará a não aplicabilidade da Skill `security-audit`; não será gerado PDF para evitar reutilização de conteúdo histórico.

## Riscos, dúvidas e decisões necessárias

- Risco: tornar Sonar ou cobertura inexistentes um gate impossível. Mitigação: fallback assistido com evidência, sem métrica inventada.
- Risco: reutilizar PDF histórico como auditoria atual. Mitigação: PDF exigido somente quando houver auditoria aplicável e resultado atual representável.
