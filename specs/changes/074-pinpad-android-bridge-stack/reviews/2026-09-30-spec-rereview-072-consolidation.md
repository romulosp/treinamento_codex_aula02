# Re-revisão da SPEC — consolidação da Change 072 na 074

Revisor: Codex  
Data: 2026-09-30  
Skill aplicada: `spec-review`

## Escopo

Foram confrontados `proposal.md`, `spec.md`, `DESIGN.md`, `analysis.md`,
`traceability.md`, `tasks.md`, `test-matrix.md`, a fonte arquivada 072 e a
aprovação humana `reviews/2026-09-29-approval.md`.

## Achados

### REV-074-072-001 — Fallback do entrypoint direto

- Severidade: importante, resolvido na rastreabilidade.
- Evidência: RF-072-01 preservava fallback quando `PORTA_PINPAD` estivesse
  ausente no entrypoint direto. A 074 removeu o fallback de todos os processos
  físicos.
- Decisão posterior: a aprovação humana de 2026-09-29 aprovou explicitamente
  `PORTA_PINPAD` obrigatória e ausência de default/fallback.
- Correção: a 074 mantém a regra mais estrita, mas agora a identifica como
  supersessão aprovada da 072, sem atribuí-la incorretamente à fonte.

### REV-074-072-002 — Cobertura dos demais contratos da 072

- Severidade: importante, resolvido.
- Evidência: readiness após bind, helper/reverse, preflight Ping, log único,
  eventos, categorias de erro, estado Android, cleanup, mutex e reabertura estão
  presentes nas seções 4, 11, 13, 14 e 17 e nos CA-074-07 a CA-074-18.
- Impacto: a 074 é autocontida e não exige leitura operacional da 072.

### REV-074-072-003 — Implementação da configuração

- Severidade: informativa.
- Evidência: `config.Load()` exige `PORTA_PINPAD` e `DefaultConfig()` não possui
  COM fixa; testes técnicos de configuração foram registrados.
- Conclusão: implementação alinhada à decisão C-003 da 074; revisão da
  implementação e validação formal continuam pendentes.

### REV-074-072-004 — Ambiente Java/Maven

- Severidade: importante, resolvido documentalmente.
- Evidência: Java e Maven estão definidos no projeto.
- Correção: tarefas/validation registram Java em
  `C:\Desenvolvimento\jdk-17.0.11` e Maven em
  `C:\Desenvolvimento\apache-maven-3.8.8`; ausência de Java não pode continuar
  sendo usada como bloqueio atual.

## Veredito

`SPEC_APROVADA`

A 074 consolida integralmente a 072 e documenta a única supersessão material:
a remoção do fallback da COM, explicitamente aprovada pelo usuário. A Change
permanece `IMPLEMENTADA — TESTE HUMANO PENDENTE`, sem revisão/validação formal.
