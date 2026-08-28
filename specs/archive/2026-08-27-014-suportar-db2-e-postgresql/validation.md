# Validação: 014-suportar-db2-e-postgresql

## Ambiente

- Data/hora: 2026-08-27.
- Sistema: Windows 10 amd64.
- PowerShell com `-NoProfile -ExecutionPolicy Bypass`, sem alteração permanente de política.
- Implementação consolidada na mudança 016.

## Comandos e códigos de saída

| Comando | Resultado | Código |
| --- | --- | --- |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\testar-geracao-configuracao-banco.ps1` | Matriz DB2/PostgreSQL/MySQL e proteção contra saída parcial aprovadas | `0` |

## Cenários executados

- `VAL-001` — Ausência de `bancoDados` produz DB2, com somente `quarkus-jdbc-db2` e `DB2_*`.
- `VAL-002` — `bancoDados=POSTGRESQL` produz somente `quarkus-jdbc-postgresql` e `POSTGRESQL_*`.
- `VAL-003` — As saídas DB2 e PostgreSQL não contêm driver ou variáveis produtivas uma da outra.
- `VAL-004` — Valor inválido não sobrescreve os artefatos de infraestrutura.

## Evidências

- A saída foi `Teste de geração de configuração de banco aprovado.`.
- A API não recebe opção de banco; a decisão ocorre no renderizador, antes da geração.
- H2 permanece limitado ao perfil de teste e não há segredos nos artefatos.

## Veredito

`VALIDADA`
