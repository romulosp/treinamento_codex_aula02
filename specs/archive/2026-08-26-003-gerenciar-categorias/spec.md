# SPEC: 003-gerenciar-categorias

## Status
`SPEC_APROVADA`

## Referências e dependências

- `specs/changes/003-gerenciar-categorias/proposal.md`
- `specs/shared/api/rest-conventions.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/testing/testing-strategy.md`

## Requisitos funcionais

1. A API deve disponibilizar `GET /categorias/` para recuperar a lista de categorias.
2. O `GET /categorias/` deve responder HTTP `200` com o atributo JSON `categorias`, contendo uma lista de categorias.
3. Cada categoria retornada na lista deve conter `id_categoria` numérico, `nome_categoria` textual e `quantidade_produtos` numérico.
4. Ao iniciar a aplicação, a lista deve conter as seguintes categorias: `1`/`CAMISAS`/`2`, `2`/`ACESSÓRIOS`/`1` e `3`/`VIDEO-GAMES`/`4`.
5. A API deve disponibilizar `POST /categorias/add` para adicionar uma categoria, recebendo um JSON com `nome_categoria` e `quantidade_produtos`.
6. Quando a inclusão for bem-sucedida, o `POST /categorias/add` deve responder HTTP `201` com `id_categoria`, `nome_categoria` e `quantidade_produtos`. Com a massa inicial, a primeira categoria incluída deve receber o identificador `4`.
7. A API deve disponibilizar `PUT /categorias/{id_categoria}` para atualizar a categoria identificada pelo parâmetro de rota, recebendo um JSON com `nome_categoria` e `quantidade_produtos`.
8. Quando a atualização for bem-sucedida, o `PUT /categorias/{id_categoria}` deve responder HTTP `200` com `id_categoria`, `nome_categoria` e `quantidade_produtos` atualizados.
9. A API deve disponibilizar `DELETE /categorias/deletar/{id_categoria}` para excluir a categoria identificada pelo parâmetro de rota, sem corpo de requisição.
10. Quando a exclusão for bem-sucedida, o `DELETE /categorias/deletar/{id_categoria}` deve responder HTTP `200` com o JSON `{"RESULTADO":"CATEGORIA EXCLUIDA COM SUCESSO"}`.
11. A API deve disponibilizar `GET /categorias/{id_categoria}` para detalhar uma categoria.
12. Quando a categoria solicitada existir, o `GET /categorias/{id_categoria}` deve responder HTTP `200` com `id_categoria`, `nome_categoria` e `quantidade_produtos`.

## Requisitos não funcionais

1. A mudança deve usar Quarkus, Maven e Java 17 já configurados no módulo.
2. A implementação não deve acessar banco de dados, entidades JPA ou repositórios Panache.
3. Os recursos REST devem delegar a operação para a camada de aplicação e não expor entidades de persistência.
4. Os contratos públicos devem ser documentados com OpenAPI.
5. Os endpoints aprovados devem possuir testes de integração Quarkus automatizados.

## Regras de negócio

1. As categorias devem existir somente em memória durante o ciclo de execução da aplicação.
2. Ao reiniciar a aplicação, a massa de categorias deve voltar ao estado inicial definido neste documento.
3. `quantidade_produtos` representa uma quantidade inteira de produtos da categoria.

## Pendências bloqueantes

Nenhuma.

## Contratos de erro

1. Para `POST /categorias/add` e `PUT /categorias/{id_categoria}`, `nome_categoria` é obrigatório e não pode ser vazio ou composto apenas por espaços; `quantidade_produtos` é obrigatória e deve ser um número inteiro maior ou igual a zero. Uma entrada inválida deve responder HTTP `400` com JSON contendo o atributo textual `mensagem`.
2. Para `GET /categorias/{id_categoria}`, `PUT /categorias/{id_categoria}` e `DELETE /categorias/deletar/{id_categoria}`, um identificador de categoria inexistente deve responder HTTP `404` com JSON contendo o atributo textual `mensagem`.

## Cenários e critérios de aceite

- [ ] `GET /categorias/` responde `200` e retorna a massa inicial no atributo `categorias`.
- [ ] `POST /categorias/add` com `{"nome_categoria":"RELÓGIO","quantidade_produtos":5}` responde `201` e retorna a categoria com `id_categoria` igual a `4`.
- [ ] `PUT /categorias/1` com `{"nome_categoria":"CAMISAS SOCIAIS","quantidade_produtos":3}` responde `200` e retorna a categoria `1` com os valores atualizados.
- [ ] `DELETE /categorias/deletar/1`, sem corpo de requisição, responde `200` com `{"RESULTADO":"CATEGORIA EXCLUIDA COM SUCESSO"}`.
- [ ] `GET /categorias/1` responde `200` e retorna `CAMISAS` com quantidade `2`.
- [ ] Uma inclusão ou atualização com entrada inválida responde `400` e contém o atributo `mensagem`.
- [ ] Uma consulta, atualização ou exclusão de categoria inexistente responde `404` e contém o atributo `mensagem`.
- [ ] Os cenários aprovados são executados por testes de integração sem banco de dados.