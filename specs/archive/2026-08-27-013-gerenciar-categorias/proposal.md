# Proposta: 013-gerenciar-categorias

## Status
`SPEC_APROVADA`

## Responsável e data

- Responsável: GitHub Copilot
- Data: 2026-08-27

## Regularização de status

O veredito da revisão registrada em `reviews/2026-08-27-spec-review.md` é `SPEC_APROVADA`. Este status foi alinhado ao veredito para remover a inconsistência documental que bloqueava indevidamente a implementação.

## Referências

- `AGENTS.md`
- `specs/archive/2026-08-26-003-gerenciar-categorias/`
- `specs/archive/2026-08-27-001-criar-projeto-java/`
- `specs/archive/2026-08-26-004-parametrizar-nome-projeto-gerado/`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/api/rest-conventions.md`
- `specs/shared/testing/testing-strategy.md`
- `specs/shared/process/workflow.md`

## Problema e objetivo

O workspace foi reinicializado intencionalmente e não possui o módulo executável `apps/backend/`. Regenerar localmente a API de categorias em memória, reproduzindo os contratos aprovados da mudança 003, para que o comportamento possa ser executado e testado novamente.

## Escopo

- Gerar o módulo Maven/Quarkus em `apps/backend/` para Java 17.
- Implementar a API REST de categorias, seus DTOs, a camada de aplicação, o domínio e o armazenamento em memória.
- Reproduzir a massa inicial, contratos HTTP, validações, respostas de erro e documentação OpenAPI da mudança 003.
- Criar testes unitários para a lógica aplicável e testes de integração Quarkus para todos os contratos aprovados.

## Fora de escopo

- Banco de dados, migrations, entidades JPA e repositórios Panache.
- OAuth 2.0/OIDC e autorização por cliente, que pertencem à mudança histórica 006 e não fazem parte da SPEC de categorias solicitada.
- Paginação, filtros, ordenação, versionamento de API e frontend.
- Alterações em documentos arquivados ou no estado vigente do sistema nesta fase de implementação.

## Impactos e riscos

- O módulo é local e ignorado pela política documental do repositório; não deve ser incluído em commit.
- Categorias são descartadas a cada reinicialização, conforme o contrato.
- A dependência das extensões Quarkus e da JDK 17 torna a execução condicionada à disponibilidade local dessas ferramentas.

## Critérios para aprovação da SPEC

- Contratos HTTP, massa inicial, validações e erros estão completos e verificáveis.
- A solução não requer banco de dados nem infraestrutura externa.
- As responsabilidades entre API, aplicação, domínio e infraestrutura estão explícitas.
- Todos os requisitos possuem estratégia de teste automatizável.
