# Revisão da implementação: 013-gerenciar-categorias

## Data e escopo

- Data: 2026-08-27.
- Estado de entrada: `IMPLEMENTADA`.
- Itens revisados: módulo Maven/Quarkus, contratos REST, testes e configuração local.
- Referências: `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, `AGENTS.md`, estratégia de testes e o orquestrador Spec Driven.

## Matriz de aderência

| Requisito da SPEC | Evidência | Resultado |
| --- | --- | --- |
| Maven, Quarkus 3.2.10.Final e Java 17 | `pom.xml` declara o artefato `gerenciar-categorias`, a plataforma Quarkus e `maven.compiler.release` igual a `17`. | Aprovado |
| Contratos e massa inicial | `CategoriaResource`, `CategoriaService` e `ArmazenamentoCategoriasEmMemoria` implementam as cinco operações, a massa inicial e o próximo identificador `4`. | Aprovado |
| DTOs e respostas de erro | Os DTOs preservam os nomes JSON aprovados; os mapeadores retornam `mensagem` para `400` e `404`. | Aprovado |
| Ausência de persistência e separação arquitetural | A camada `api` delega à `application`; os dados estão em `infrastructure`, sem JPA, banco ou Panache. | Aprovado |
| OpenAPI e testes de integração | As operações possuem anotações OpenAPI e `CategoriaResourceTest` usa `@QuarkusTest` e Rest Assured para os critérios HTTP. | Aprovado |
| Testes unitários e cobertura por classe aplicável | O relatório JaCoCo indica somente 51% de instruções e 75% de branches em `CategoriaResource`; seus métodos `listar`, `detalhar` e `excluir` não são exercitados pela suíte unitária. | Reprovado |
| Inicialização local prevista pelo orquestrador | Não existe `apps/backend/start_aplicacao.bat` com a configuração temporária de Java e Maven exigida para módulo Quarkus gerado. | Reprovado |

## Achados

### IMP-REV-001 — Cobertura unitária insuficiente da fronteira HTTP

- Severidade: importante.
- Evidência: `CategoriaResourceUnitTest` cobre apenas inclusão e corpo ausente; o relatório JaCoCo gerado em `target/site/jacoco/` indica 51% de instruções e 75% de branches para `CategoriaResource`.
- Impacto: a classe de fronteira que contém mapeamento de DTOs e decisões de corpo nulo não atende à meta de 80% a 100% para lógica aplicável definida pela estratégia de testes.
- Ação necessária: ampliar os testes unitários de `CategoriaResource` para listar, detalhar, atualizar e excluir, cobrindo as ramificações de requisição nula e presente; executar novamente a aferição JaCoCo.

### IMP-REV-002 — Script obrigatório de inicialização local ausente

- Severidade: importante.
- Evidência: o módulo gerado não contém `apps/backend/start_aplicacao.bat`.
- Impacto: a entrega não cumpre o requisito explícito do orquestrador para projetos Java Quarkus gerados e não fornece a inicialização local temporária padronizada.
- Ação necessária: criar o script com `setlocal`, Java 17.0.11, Maven 3.8.8, exibição da versão Java, execução de `mvn quarkus:dev`, `pause` e `endlocal`.

## Conclusão

`REPROVADA`

A mudança deve retornar à fase de implementação para resolver `IMP-REV-001` e `IMP-REV-002`. Nenhuma validação, aprovação, arquivamento ou commit foi executado.