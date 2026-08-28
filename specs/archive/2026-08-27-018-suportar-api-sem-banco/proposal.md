# Proposta: 018-suportar-api-sem-banco

## Status

`SPEC_APROVADA`

## Responsável e data

- Responsável: equipe do projeto.
- Data: 2026-08-27.

## Referências

- `AGENTS.md`
- `specs/shared/database/migration-rules.md`
- `specs/archive/2026-08-27-016-renderizar-configuracao-banco-selecionado/`
- `specs/archive/2026-08-27-013-gerenciar-categorias/`

## Problema e objetivo

Algumas APIs apenas orquestram chamadas para serviços externos ou mantêm estado transitório e não precisam de datasource. Hoje o gerador sempre renderiza um banco produtivo e a API de categorias, embora use armazenamento em memória, ainda carrega dependências e propriedades DB2.

Adicionar a opção explícita `SEM_BANCO` à geração e regenerar a API de categorias com essa opção, sem configuração ou dependência de banco.

## Escopo

- Aceitar `SEM_BANCO` como valor de `bancoDados`, preservando DB2 como padrão quando o campo estiver ausente ou nulo.
- Para `SEM_BANCO`, gerar `pom.xml` sem drivers JDBC produtivos, H2 de teste, Hibernate ORM/Panache ou outra dependência de persistência.
- Para `SEM_BANCO`, gerar `application.properties` sem propriedades `quarkus.datasource.*`, `quarkus.hibernate-orm.*`, URLs, usuários, senhas ou referências a variáveis de banco.
- Garantir que scripts de inicialização não recebam ou usem dados de banco para a API sem banco.
- Regenerar `apps/backend/` de categorias com `SEM_BANCO` e validar os contratos HTTP existentes.

## Fora de escopo

- Alterar os contratos REST ou as regras de categorias em memória.
- Criar mocks de APIs externas, mensageria, cache, migrations ou persistência.
- Alterar o padrão DB2 dos projetos cuja entrada `bancoDados` estiver ausente ou nula.

## Impactos e riscos

- Uma API sem banco não poderá usar JPA, Panache ou datasource até que uma mudança futura escolha um banco e regenere seus artefatos.
- A ausência de dependências de persistência reduz o tempo de bootstrap e elimina a necessidade de variáveis de banco em desenvolvimento e testes.

## Critérios para aprovação da SPEC

- `SEM_BANCO` tem comportamento verificável no gerador e não altera o padrão DB2.
- A saída sem banco não contém dependências, propriedades ou variáveis de banco.
- A API de categorias continua executando seus contratos sem infraestrutura externa.
