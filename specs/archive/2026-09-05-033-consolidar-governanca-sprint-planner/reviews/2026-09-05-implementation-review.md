# Revisão da implementação: 033-consolidar-governanca-sprint-planner

**Veredito:** `REPROVADA`

## Resumo executivo

A revisão identificou uma divergência no prompt de finalização: ele exigia impacto, mas não exigia o registro explícito de risco para pendências e bloqueios. Isso não atendia integralmente RF-004 e CA-003, que exigem risco visível em todas as fases da Sprint. A Change retornou à implementação documental para correção.

## Achados

| ID | Severidade | Localização | Desvio identificado | Impacto | Correção necessária |
| --- | --- | --- | --- | --- | --- |
| IMP-REV-001 | Importante | `specs/sprint/prompts/prompt-finalizar-sprint.md` | Pendências e bloqueios registravam impacto, mas não o risco de segurança ou negócio. | A Sprint poderia encerrar sem explicitar o risco residual de um item pendente. | Exigir o registro de risco, impacto e condição de resolução. |

## Conclusão

Retornar à implementação documental e submeter novamente à revisão.
