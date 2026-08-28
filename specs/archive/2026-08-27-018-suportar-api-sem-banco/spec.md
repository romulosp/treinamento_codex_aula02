# SPEC: 018-suportar-api-sem-banco

## Status

`SPEC_APROVADA`

## Referências e dependências

- `specs/changes/018-suportar-api-sem-banco/proposal.md`
- `specs/archive/2026-08-27-016-renderizar-configuracao-banco-selecionado/spec.md`
- `specs/archive/2026-08-27-013-gerenciar-categorias/spec.md`
- `specs/shared/database/migration-rules.md`

## Requisitos funcionais

1. O campo `bancoDados` deve aceitar `SEM_BANCO`, além de `DB2`, `POSTGRESQL` e `MYSQL`; valor ausente ou nulo continua normalizado para `DB2`.
2. Para `bancoDados=SEM_BANCO`, o `pom.xml` gerado não pode conter `quarkus-jdbc-db2`, `quarkus-jdbc-postgresql`, `quarkus-jdbc-mysql`, `quarkus-jdbc-h2`, `quarkus-hibernate-orm-panache` ou outra dependência de persistência.
3. Para `bancoDados=SEM_BANCO`, o `application.properties` gerado não pode conter `quarkus.datasource`, `quarkus.hibernate-orm`, URL, usuário, senha ou referência a variáveis de banco.
4. Para `SEM_BANCO`, os scripts de inicialização permanecem independentes de dados de banco e não solicitam, exportam ou validam variáveis de datasource.
5. A API de categorias deve ser gerada com `SEM_BANCO`, preservar o armazenamento em memória e continuar expondo os contratos HTTP aprovados na mudança 013.
6. Os valores `DB2`, `POSTGRESQL` e `MYSQL` preservam a renderização exclusiva existente; valor inválido continua falhando antes de sobrescrever artefatos.

## Requisitos não funcionais

1. Não pode haver segredo versionado.
2. A decisão de banco continua externa à API: não há seleção por endpoint, DTO, cabeçalho ou corpo HTTP.
3. A documentação e as mensagens permanecem em português do Brasil.

## Regras de negócio

1. `SEM_BANCO` é uma escolha explícita de infraestrutura para APIs que não persistem dados localmente.
2. A ausência de `bancoDados` não significa `SEM_BANCO`; significa `DB2`.
3. Para trocar uma API de `SEM_BANCO` para um banco produtivo, é necessário regenerar os artefatos com a nova escolha.

## Cenários e critérios de aceite

- [ ] Gerar com `SEM_BANCO` não inclui drivers JDBC, H2, Hibernate ORM/Panache ou dependências de persistência no `pom.xml`.
- [ ] Gerar com `SEM_BANCO` não inclui propriedades ou variáveis de datasource e Hibernate no `application.properties`.
- [ ] Gerar sem `bancoDados` continua produzindo DB2.
- [ ] DB2, PostgreSQL e MySQL continuam com um único driver e suas variáveis exclusivas.
- [ ] Valor inválido não cria saída parcial e informa os quatro valores aceitos.
- [ ] A suíte Maven da API de categorias executa sem banco externo e sem dependências de persistência.
