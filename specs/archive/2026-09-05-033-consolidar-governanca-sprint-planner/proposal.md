# Proposta: 033-consolidar-governanca-sprint-planner

## Status
`SPEC_APROVADA`

## Responsável e data

- Responsável: Codex
- Data: 2026-09-05

## Referências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `specs/shared/testing/testing-strategy.md`
- `.agents/skills/security-audit/SKILL.md`

## Problema e objetivo

O Sprint Planner possui template e prompts, mas ainda não define de forma única a localização das Sprints, a relação entre o status da Sprint e os gates da Change, a evidência documental de cada fase e a contingência para módulos sem Sonar ou cobertura configurados. Isso permite avanço sem rastreabilidade e pode bloquear Changes documentais por controles não aplicáveis.

O objetivo é consolidar a governança do Sprint Planner para que cada Change planejada tenha gate, evidência, risco e condição de avanço visíveis, preservando o workflow 01-06.

## Escopo

- Documentar a localização, nomenclatura, estados e evidências do Sprint Planner.
- Registrar `implementation-plan.md` como artefato técnico preparatório após `SPEC_APROVADA`, sem criar uma nova fase.
- Atualizar template e prompts de Sprint para exigir documentação por gate, prioridade de segurança e risco de negócio, testes unitários e de integração, qualidade e encerramento rastreável.
- Definir Auditoria de Qualidade Assistida por LLM como fallback quando o módulo não possuir Sonar ou cobertura configurados, sem alegar resultados inexistentes.
- Delimitar a auditoria de segurança aos artefatos aplicáveis no escopo e registrar não aplicabilidade de Change somente documental.
- Alinhar o orquestrador de Change e a documentação de especificações ao processo consolidado.

## Fora de escopo

- Alterar código de aplicação, testes de aplicação, POMs, `package.json`, Sonar, cobertura ou infraestrutura Docker/Swarm.
- Tornar o gerador de PDF de segurança orientado a dados.
- Criar Sprint concreta, Change de produto, branch ou infraestrutura externa.

## Impactos e riscos

- As novas Changes deverão criar `implementation-plan.md` antes da implementação.
- Módulos sem Sonar ou cobertura usarão a auditoria assistida até que uma Change específica configure as ferramentas.
- A auditoria de segurança só poderá liberar uma Change quando suas categorias aplicáveis forem verificadas; o PDF atual não poderá ser usado como evidência de auditoria nova se tiver conteúdo histórico.

## Critérios para aprovação da SPEC

- O contrato define artefatos, estados, evidências e regras de aplicabilidade sem contradizer o workflow 01-06.
- A contingência de qualidade não declara Sonar ou cobertura sem ferramenta reproduzível.
- Os critérios de aceite são verificáveis por inspeção documental e comandos de validação seguros.
