# Validação: 016-renderizar-configuracao-banco-selecionado

## Ambiente

- Data/hora: 2026-08-27 21:27 -03:00.
- Sistema: Windows 10 amd64.
- PowerShell executado com `-NoProfile -ExecutionPolicy Bypass`; a política não foi alterada permanentemente.

## Comandos e códigos de saída

| Comando | Resultado | Código |
| --- | --- | --- |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\testar-geracao-configuracao-banco.ps1` | Testes de renderização aprovados | `0` |

## Testes unitários e cobertura

- Ferramenta: script de teste PowerShell local, sem framework de cobertura configurado para esse tipo de artefato.
- Escopo: normalização da entrada, matriz de banco, renderização de `pom.xml`, renderização de `application.properties` e proteção contra saída parcial.
- Cobertura percentual: não aferida, pois não há ferramenta de cobertura PowerShell configurada; os cenários exigidos pela SPEC foram executados explicitamente.

## Cenários executados

- `VAL-001` — `bancoDados` ausente gera DB2.
- `VAL-002` — DB2 explícito gera somente `quarkus-jdbc-db2` e referências `DB2_*`.
- `VAL-003` — PostgreSQL gera somente `quarkus-jdbc-postgresql` e referências `POSTGRESQL_*`.
- `VAL-004` — MySQL gera somente `quarkus-jdbc-mysql` e referências `MYSQL_*`.
- `VAL-005` — `ORACLE` falha com mensagem de valores aceitos e mantém hashes de `pom.xml` e `application.properties` inalterados.

## Evidências

- A saída do teste foi `Teste de geração de configuração de banco aprovado.`.
- Todos os cenários são executados em diretório temporário e removidos ao término; os artefatos DB2 do projeto permanecem como saída padrão.
- Nenhuma URL, usuário ou senha real foi escrita nos artefatos ou nas evidências.

## Veredito

`VALIDADA`
