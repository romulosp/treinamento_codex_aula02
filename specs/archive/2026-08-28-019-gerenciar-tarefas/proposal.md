# Proposta: 019-gerenciar-tarefas

## Status

`SPEC_APROVADA`

## Responsável e data

- Responsável: equipe do projeto.
- Data: 2026-08-27.

## Referências

- `AGENTS.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/api/rest-conventions.md`
- `specs/shared/database/migration-rules.md`
- `specs/shared/testing/testing-strategy.md`
- `specs/shared/process/workflow.md`

## Problema e objetivo

Criar uma nova API independente de gerenciamento de tarefas (`gerenciar-tarefas`) utilizando Quarkus com Java 17 e persistência de dados em banco relacional **PostgreSQL** via Hibernate ORM com Panache (padrão Repository).

A API deve permitir o ciclo de vida completo de tarefas (criação, listagem, consulta por identificador, atualização de dados/status e exclusão), com validação rigorosa de entradas, tratamento de erros padronizado e cobertura de testes unitários e de integração.

## Escopo

- Definir o identificador da mudança como `019-gerenciar-tarefas`, com nome público `gerenciar-tarefas` e schema de testes associado.
- Configurar o gerador/módulo backend para utilizar `bancoDados=POSTGRESQL`, incluindo o driver `quarkus-jdbc-postgresql` e o `quarkus-hibernate-orm-panache`.
- Implementar as camadas arquiteturais conforme as diretrizes do projeto:
  - **API:** endpoints REST em `/tarefas`, DTOs de entrada e saída (`TarefaRequest`, `TarefaResponse`, `ListaTarefasResponse`, `ResultadoExclusaoResponse`, `MensagemResponse`), documentação OpenAPI e Exception Mappers para erros 400 e 404.
  - **Aplicação:** serviço `TarefaService` para orquestração de casos de uso e validação de regras de negócio.
  - **Domínio:** modelo `Tarefa`, enum `StatusTarefa` (`PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDA`) e exceções de domínio (`TarefaNaoEncontradaException`, `TarefaInvalidaException`).
  - **Infraestrutura:** entidade JPA `TarefaEntity` e repositório Panache `TarefaRepository` para persistência no PostgreSQL, utilizando datasource configurado via variáveis de ambiente (`POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME`, `POSTGRESQL_PASSWORD`).
- Configurar suíte de testes com H2 em memória no perfil de teste para execução determinística sem depender de servidor PostgreSQL externo.
- Implementar testes unitários (JUnit 5 + Mockito) e testes de integração REST Assured (`@QuarkusTest`).

## Fora de escopo

- Autenticação e autorização via OAuth2/OIDC (pode ser introduzida em mudança posterior).
- Subtarefas, anexos, notificações por e-mail ou agendamento de jobs.
- Migrations automáticas via Flyway/Liquibase (esquema gerenciado pelo Hibernate/ORM).

## Impactos e riscos

- A alteração do módulo `apps/backend/` para `gerenciar-tarefas` substitui o módulo anterior de categorias no ambiente local, seguindo a política documental do repositório.
- A persistência produtiva exige variáveis de ambiente do PostgreSQL em tempo de execução, mas os testes automatizados executam isoladamente com H2.

## Critérios para aprovação da SPEC

- Contratos HTTP, códigos de status e formatos JSON de sucesso e erro claramente descritos.
- Estrutura de banco e mapeamento de campos devidamente especificados.
- Casos de teste e critérios de aceite verificáveis definidos em `spec.md`.
