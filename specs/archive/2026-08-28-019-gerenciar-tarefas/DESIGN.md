# Design: 019-gerenciar-tarefas

## Contexto

A API `gerenciar-tarefas` é um serviço REST independente para gestão do ciclo de vida de tarefas, utilizando persistência relacional com PostgreSQL em produção e banco H2 em memória durante os testes automatizados.

## Referências

- `spec.md`
- `proposal.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/database/migration-rules.md`
- `specs/shared/testing/testing-strategy.md`

## Decisões

1. **Camadas Arquiteturais e Responsabilidades:**
   - `br.com.romulopenha.gerenciartarefas.api`:
     - `TarefaResource`: Endpoints REST (`GET`, `POST`, `PUT`, `DELETE`).
     - DTOs: `TarefaRequest`, `TarefaResponse`, `ListaTarefasResponse`, `ResultadoExclusaoResponse`, `MensagemResponse`.
     - Exception Mappers: Mapeamento de exceções de domínio para respostas HTTP 400 e 404 padronizadas.
   - `br.com.romulopenha.gerenciartarefas.application`:
     - `TarefaService`: Casos de uso de negócio (criar, listar, buscar por id, atualizar, excluir), validação de regras e conversão entre entidade/domínio e DTOs.
   - `br.com.romulopenha.gerenciartarefas.domain`:
     - `StatusTarefa` (Enum: `PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDA`).
     - Exceções: `TarefaNaoEncontradaException`, `TarefaInvalidaException`.
   - `br.com.romulopenha.gerenciartarefas.infrastructure`:
     - `TarefaEntity`: Entidade JPA anotada com `@Entity`, `@Table(name = "tarefas")`.
     - `TarefaRepository`: Repositório Panache implementando `PanacheRepositoryBase<TarefaEntity, Long>`.

2. **Isolamento de Persistência:**
   - DTOs na fronteira REST impedem vazamento de estruturas JPA.
   - Operações transacionais marcadas com `@Transactional` na camada de aplicação/serviço.

3. **Estratégia de Persistência e Datasource:**
   - Geração com `bancoDados=POSTGRESQL`.
   - Produção: Driver `quarkus-jdbc-postgresql`, dialeto `org.hibernate.dialect.PostgreSQLDialect`, variáveis `POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME`, `POSTGRESQL_PASSWORD`.
   - Testes: Driver `quarkus-jdbc-h2` (escopo test) com `%test.quarkus.datasource.db-kind=h2` e `%test.quarkus.hibernate-orm.database.generation=drop-and-create`.

4. **Estratégia de Testes:**
   - Testes unitários com JUnit 5 e Mockito para `TarefaService` e Exception Mappers.
   - Testes de integração de ponta a ponta com `@QuarkusTest` e Rest Assured para `TarefaResource`.

## Arquitetura e componentes

```text
HTTP Request
     │
     ▼
[ TarefaResource ] (api) ──────► [ DTOs & Exception Mappers ]
     │
     ▼
[ TarefaService ] (application) ──► [ StatusTarefa / Exceções ] (domain)
     │
     ▼
[ TarefaRepository ] (infrastructure)
     │
     ▼
[ TarefaEntity (JPA / Panache) ]
     │
     ▼
[ PostgreSQL (Prod) / H2 (Test) ]
```

## Alternativas e consequências

- **Panache Active Record vs Repository:** Foi escolhido o padrão *Repository* (`PanacheRepositoryBase`) para manter a separação clara entre a entidade JPA e as operações de acesso a dados.
- **Injeção de datasource único:** Mantém consistência com as regras de governança e matriz de geração do projeto.
