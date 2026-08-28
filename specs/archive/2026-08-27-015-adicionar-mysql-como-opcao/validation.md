# Validação: 015-adicionar-mysql-como-opcao

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

- `VAL-001` — `bancoDados=MYSQL` produz somente `quarkus-jdbc-mysql`, `db-kind=mysql` e variáveis `MYSQL_*`.
- `VAL-002` — A saída MySQL não contém drivers, propriedades ou variáveis produtivas DB2 e PostgreSQL.
- `VAL-003` — O valor inválido é rejeitado antes de qualquer sobrescrita parcial.

## Evidências

- A saída foi `Teste de geração de configuração de banco aprovado.`.
- MySQL é selecionado pela geração, sem parâmetro em endpoint, DTO, caso de uso ou domínio.
- Não há segredos nos artefatos validados.

## Veredito

`VALIDADA`
