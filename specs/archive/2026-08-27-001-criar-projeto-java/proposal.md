# Proposta: 001-criar-projeto-java

## Status
`SPEC_APROVADA`

## Problema e objetivo

O laboratório não possui uma aplicação executável que materialize as especificações. Criar o backend base para a API Java gerada, com Maven, Quarkus, persistência DB2, documentação OpenAPI, health checks e base de testes.

## Escopo

- Criar o módulo em `apps/backend/<artifactId-sem-hifens>/`, mantendo `apps/backend/` como contêiner local de projetos.
- Configurar Maven, Quarkus 3.2.10.Final, Java 17 e as dependências fornecidas.
- Configurar perfis de produção e teste.
- Criar teste de inicialização Quarkus.

## Fora de escopo

- Endpoints de negócio, autenticação, migrations, entidades e integração real com DB2.

## Critérios para aprovação da SPEC

- Documentos `spec.md`, `DESIGN.md` e `tasks.md` completos.
- Conflito entre Java 11 e Quarkus 3 resolvido por ADR.
