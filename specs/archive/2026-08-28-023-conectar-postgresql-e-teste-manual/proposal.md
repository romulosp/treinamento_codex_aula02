# Proposta: 023-conectar-postgresql-e-teste-manual

## Status

`SPEC_APROVADA`

## Objetivo

Complementar o projeto `019-gerenciar-tarefas` com a conexão ao PostgreSQL 16 executado por contêiner e com a configuração necessária no `start_aplicacao.bat`, que será o ponto de execução e teste manual da API.

## Escopo

- Configurar datasource PostgreSQL usando `POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME` e `POSTGRESQL_PASSWORD`.
- Atualizar o `.bat` de execução do projeto para entrar em `D:\desenvolvimento\banco_dados\postgresql`, executar o Docker Compose existente e só depois iniciar `mvn quarkus:dev`.
- Usar as credenciais locais de desenvolvimento fornecidas pelo usuário (`root`/`root`) no fluxo manual, sem alterar ou versionar o Compose externo.
- Aplicar a atualização automática do schema `tarefas` pelo Hibernate, preservando dados existentes conforme a capacidade do ORM.
- Manter H2 apenas no perfil de testes automatizados.

## Fora de escopo

- Alterar os endpoints ou regras de negócio de tarefas.
- Versionar senhas, arquivos `.env` ou dados reais do banco.
- Renomear ou apagar colunas automaticamente sem decisão explícita.
- Flyway/Liquibase nesta mudança.

## Dependências e riscos

O teste manual depende de Docker Desktop, do Compose existente, do contêiner saudável e de Java 17/Maven configurados no `.bat`. A atualização automática de schema é controlada pelo Hibernate e não substitui backup; alterações não inferíveis do mapeamento podem exigir intervenção no banco.
