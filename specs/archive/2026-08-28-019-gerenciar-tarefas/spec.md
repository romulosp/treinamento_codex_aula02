# SPEC: 019-gerenciar-tarefas

## Status

`SPEC_APROVADA`

## Referências e dependências

- `specs/changes/019-gerenciar-tarefas/proposal.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/api/rest-conventions.md`
- `specs/shared/database/migration-rules.md`
- `specs/shared/testing/testing-strategy.md`

## Requisitos funcionais

1. **Modelo de Dados (PostgreSQL):**
   - Tabela `tarefas`:
     - `id`: Chave primária (numérico sequencial / `BIGSERIAL` / `Long`).
     - `titulo`: `VARCHAR(100)` não nulo.
     - `descricao`: `VARCHAR(255)` opcional.
     - `status`: `VARCHAR(20)` não nulo (`PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDA`).
     - `data_criacao`: Timestamp / data de criação não nula.
     - `data_conclusao`: Timestamp / data de conclusão (opcional, preenchida quando status for `CONCLUIDA`).

2. **Contratos da API REST (`/tarefas`):**
   - `GET /tarefas`: Retorna HTTP `200 OK` com a lista de tarefas no atributo `tarefas`.
   - `GET /tarefas/{id}`: Retorna HTTP `200 OK` com o DTO da tarefa solicitada. Se o ID não existir, retorna HTTP `404 Not Found` com `{"mensagem": "Tarefa não encontrada"}`.
   - `POST /tarefas`: Cria uma nova tarefa.
     - Payload: `{"titulo": "string", "descricao": "string", "status": "string"}` (o campo `status` é opcional e assume `PENDENTE` por padrão se não informado).
     - Retorna HTTP `201 Created` com o DTO da tarefa criada (`id`, `titulo`, `descricao`, `status`, `data_criacao`, `data_conclusao`).
     - Validação: Se `titulo` for nulo, vazio ou contiver apenas espaços, retorna HTTP `400 Bad Request` com `{"mensagem": "O título da tarefa é obrigatório"}`.
     - Se `status` for informado com valor diferente de `PENDENTE`, `EM_ANDAMENTO` ou `CONCLUIDA`, retorna HTTP `400 Bad Request` com `{"mensagem": "Status inválido"}`.
   - `PUT /tarefas/{id}`: Atualiza os dados de uma tarefa existente.
     - Payload: `{"titulo": "string", "descricao": "string", "status": "string"}`.
     - Retorna HTTP `200 OK` com o DTO da tarefa atualizada.
     - Se o ID não for encontrado, retorna HTTP `404 Not Found` com `{"mensagem": "Tarefa não encontrada"}`.
     - Se o título for inválido ou status for inválido, retorna HTTP `400 Bad Request`.
   - `DELETE /tarefas/{id}`: Exclui a tarefa.
     - Retorna HTTP `200 OK` com `{"resultado": "TAREFA EXCLUIDA COM SUCESSO"}`.
     - Se o ID não existir, retorna HTTP `404 Not Found` com `{"mensagem": "Tarefa não encontrada"}`.

3. **Isolamento de Camadas e DTOs:**
   - Recursos REST (`TarefaResource`) interagem exclusivamente com `TarefaService`.
   - Entidades JPA (`TarefaEntity`) nunca são retornadas diretamente em contratos HTTP, utilizando sempre os DTOs `TarefaResponse`, `ListaTarefasResponse` e `ResultadoExclusaoResponse`.
   - Erros tratados via Exception Mappers específicos (`TarefaNaoEncontradaExceptionMapper` -> 404, `TarefaInvalidaExceptionMapper` -> 400).

4. **Configuração e Banco de Dados:**
   - Configuração do gerador: `bancoDados=POSTGRESQL`.
   - Dependências produtivas: `io.quarkus:quarkus-jdbc-postgresql` e `io.quarkus:quarkus-hibernate-orm-panache`.
   - Em produção, credenciais via variáveis: `POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME`, `POSTGRESQL_PASSWORD`.
   - Em testes (`@QuarkusTest`), utilizar banco H2 em memória (`%test.quarkus.datasource.db-kind=h2`).

## Requisitos não funcionais

1. **Segurança de Configuração:** Nenhuma credencial ou URL de banco de produção deve ser versionada no código ou repositório.
2. **Documentação OpenAPI:** Especificação pública acessível no caminho `/swagger_gerenciar-tarefas.json` com UI em `/q/swagger-ui/`.
3. **Internacionalização:** Mensagens de validação e respostas de erro em português do Brasil.
4. **Compatibilidade Java:** Código compilado e executado sob Java 17 com Quarkus 3.2.10.Final.

## Regras de negócio

1. O status inicial de qualquer tarefa criada sem status explícito é `PENDENTE`.
2. Se o status for alterado para `CONCLUIDA`, a data de conclusão deve ser registrada automaticamente.
3. Se uma tarefa concluída tiver seu status alterado para `PENDENTE` ou `EM_ANDAMENTO`, a data de conclusão deve ser limpa (`null`).

## Cenários e critérios de aceite

- [ ] Listar tarefas cadastradas retorna HTTP 200 com lista no atributo `tarefas`.
- [ ] Obter tarefa por ID existente retorna HTTP 200 com os dados completos.
- [ ] Obter tarefa por ID inexistente retorna HTTP 404 com mensagem apropriada.
- [ ] Criar tarefa válida retorna HTTP 201 com dados da tarefa e status padrão `PENDENTE`.
- [ ] Criar tarefa sem título ou com título vazio retorna HTTP 400.
- [ ] Criar tarefa com status inválido retorna HTTP 400.
- [ ] Atualizar tarefa existente retorna HTTP 200 com dados atualizados.
- [ ] Atualizar status para `CONCLUIDA` preenche `data_conclusao`.
- [ ] Atualizar tarefa inexistente retorna HTTP 404.
- [ ] Excluir tarefa existente retorna HTTP 200 e confirmação de sucesso.
- [ ] Excluir tarefa inexistente retorna HTTP 404.
- [ ] Suíte de testes unitários e de integração executa com 100% de sucesso no perfil de teste H2.
