# Sistema vigente

## Backend

- Backend Java 17 baseado em Quarkus e Maven, organizado nas camadas `api`, `application`, `domain` e `infrastructure`.
- Os recursos REST usam DTOs como contratos públicos e não expõem modelos de persistência.

## Gerenciamento de categorias

- A API disponibiliza categorias em memória durante a execução da aplicação.
- A massa inicial contém: `1`/`CAMISAS`/`2`, `2`/`ACESSÓRIOS`/`1` e `3`/`VIDEO-GAMES`/`4`.
- Estão disponíveis os endpoints `GET /categorias/`, `GET /categorias/{id_categoria}`, `POST /categorias/add`, `PUT /categorias/{id_categoria}` e `DELETE /categorias/deletar/{id_categoria}`.
- Entradas inválidas retornam HTTP `400` com `mensagem`; categorias inexistentes retornam HTTP `404` com `mensagem`.
- Os contratos públicos são documentados em OpenAPI e possuem testes de integração Quarkus automatizados.

## Limitações atuais

- As categorias não são persistidas e retornam à massa inicial quando a aplicação é reiniciada.
- Não há autenticação, autorização, paginação, filtros, ordenação ou versionamento de API para categorias.
