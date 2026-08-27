# Convenções de evidência

## Relatórios

Os relatórios devem ser salvos em `reviews/` com o formato `AAAA-MM-DD-<fase>.md`.

| Fase | Identificador de achado | Conteúdo mínimo |
| --- | --- | --- |
| Revisão da SPEC | `REV-001` | severidade, evidência, impacto e recomendação |
| Revisão da implementação | `IMP-REV-001` | requisito avaliado, divergência e correção esperada |
| Validação | `VAL-001` | cenário, comando ou procedimento, resultado e evidência |
| Aprovação | `APR-001` | pré-condições verificadas e decisão |

## Severidade

- **Bloqueante**: impede a fase seguinte.
- **Importante**: deve ser resolvida antes da aprovação.
- **Melhoria**: não bloqueia, mas deve ser registrada para trabalho futuro.

## Evidência mínima de testes

Registre o ambiente, versões relevantes, comando executado, código de saída, resultado e caminhos dos artefatos gerados quando aplicável.