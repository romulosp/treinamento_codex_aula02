# Tarefas: 019-gerenciar-tarefas

## Pré-condições

- [x] Revisar e aprovar proposta, SPEC, design e tarefas (`SPEC_APROVADA`).

## Implementação

- [x] Configurar `pom.xml` com `artifactId=gerenciar-tarefas`, driver PostgreSQL e Hibernate Panache.
- [x] Configurar `application.properties` com OpenAPI `/swagger_gerenciar-tarefas.json`, variáveis PostgreSQL e perfil de teste H2.
- [x] Implementar modelo de domínio, enum `StatusTarefa` e exceções de domínio em `domain/`.
- [x] Implementar entidade JPA `TarefaEntity` e repositório Panache `TarefaRepository` em `infrastructure/`.
- [x] Implementar serviço de aplicação `TarefaService` com regras de negócio e conversões em `application/`.
- [x] Implementar DTOs, Exception Mappers e recurso REST `TarefaResource` em `api/`.
- [x] Implementar testes unitários com JUnit 5 e Mockito.
- [x] Implementar testes de integração com `@QuarkusTest` e Rest Assured.

## Revisão e validação

- [x] Revisar a implementação contra a SPEC aprovada (`IMP-REV-*`).
- [x] Executar suíte de testes automatizados e registrar evidências em `validation.md`.
- [x] Obter aprovação formal antes do encerramento.
