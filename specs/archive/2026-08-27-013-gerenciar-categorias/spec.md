# SPEC: 013-gerenciar-categorias

## Status
`SPEC_APROVADA`

## Referências e dependências

- `specs/changes/013-gerenciar-categorias/proposal.md`
- `specs/archive/2026-08-26-003-gerenciar-categorias/spec.md`
- `specs/shared/api/rest-conventions.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/testing/testing-strategy.md`

## Requisitos funcionais

1. O módulo `apps/backend/` deve usar Maven, Quarkus 3.2.10.Final e Java 17, com `groupId` `br.com.romulopenha`, `artifactId` e nome público `gerenciar-categorias`.
2. A API deve disponibilizar `GET /categorias/`, respondendo HTTP `200` com o atributo JSON `categorias`, contendo a lista de categorias.
3. Cada categoria deve conter `id_categoria` numérico, `nome_categoria` textual e `quantidade_produtos` numérico.
4. A lista inicial deve conter `1`/`CAMISAS`/`2`, `2`/`ACESSÓRIOS`/`1` e `3`/`VIDEO-GAMES`/`4`.
5. A API deve disponibilizar `POST /categorias/add`, recebendo JSON com `nome_categoria` e `quantidade_produtos`, e responder HTTP `201` com a categoria criada. Com a massa inicial, a primeira inclusão deve receber identificador `4`.
6. A API deve disponibilizar `PUT /categorias/{id_categoria}`, recebendo JSON com `nome_categoria` e `quantidade_produtos`, e responder HTTP `200` com a categoria atualizada.
7. A API deve disponibilizar `DELETE /categorias/deletar/{id_categoria}`, sem corpo de requisição, e responder HTTP `200` com `{"RESULTADO":"CATEGORIA EXCLUIDA COM SUCESSO"}`.
8. A API deve disponibilizar `GET /categorias/{id_categoria}`, respondendo HTTP `200` com a categoria solicitada quando ela existir.

## Requisitos não funcionais

1. As categorias devem permanecer somente em memória; a implementação não pode acessar banco de dados, entidades JPA ou repositórios Panache.
2. Recursos REST devem delegar as operações à camada de aplicação e não expor entidades de persistência.
3. Todos os contratos públicos devem ter documentação OpenAPI.
4. Os endpoints devem possuir testes de integração Quarkus automatizados.
5. Classes de produção aplicáveis devem ter testes unitários JUnit 5 independentes de banco e do container Quarkus, conforme a estratégia compartilhada.

## Regras de negócio

1. A massa de categorias é recriada no início de cada ciclo da aplicação.
2. `quantidade_produtos` representa uma quantidade inteira maior ou igual a zero.
3. Em `POST /categorias/add` e `PUT /categorias/{id_categoria}`, `nome_categoria` é obrigatório e não pode ser vazio ou conter apenas espaços; `quantidade_produtos` é obrigatório e deve ser inteiro maior ou igual a zero. Entrada inválida responde HTTP `400` com o atributo textual `mensagem`.
4. Para detalhamento, atualização e exclusão, uma categoria inexistente responde HTTP `404` com o atributo textual `mensagem`.
5. O identificador na rota é autoritativo para atualização e exclusão.

## Cenários e critérios de aceite

- [ ] `GET /categorias/` responde `200` e retorna a massa inicial no atributo `categorias`.
- [ ] `POST /categorias/add` com `{"nome_categoria":"RELÓGIO","quantidade_produtos":5}` responde `201` e retorna identificador `4`.
- [ ] `PUT /categorias/1` com `{"nome_categoria":"CAMISAS SOCIAIS","quantidade_produtos":3}` responde `200` com os valores atualizados.
- [ ] `DELETE /categorias/deletar/1` responde `200` com o JSON de sucesso definido.
- [ ] `GET /categorias/1` responde `200` com `CAMISAS` e quantidade `2`.
- [ ] Inclusão ou atualização inválida responde `400` e contém `mensagem`.
- [ ] Consulta, atualização ou exclusão de categoria inexistente responde `404` e contém `mensagem`.
- [ ] A suíte de integração executa os cenários sem banco de dados, e a suíte unitária cobre a lógica aplicável sem container Quarkus.
