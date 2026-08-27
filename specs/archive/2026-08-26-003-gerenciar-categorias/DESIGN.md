# Design: 003-gerenciar-categorias

## Contexto

Esta mudança introduz o primeiro recurso de negócio do backend para gerenciar categorias. Nesta etapa não haverá banco de dados; os dados serão mantidos somente em memória, com massa inicial fixa em cada inicialização.

## Referências

- `specs/changes/003-gerenciar-categorias/spec.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/api/rest-conventions.md`
- `specs/shared/testing/testing-strategy.md`

## Decisões

1. O recurso REST será responsável somente por HTTP, validação de fronteira e transformação dos contratos JSON.
2. A camada de aplicação concentrará as operações de listar, detalhar, incluir, atualizar e excluir categorias.
3. Os dados serão mantidos por um componente de infraestrutura em memória, inicializado com a massa definida na SPEC.
4. Os contratos de entrada e saída serão DTOs; nenhum modelo de persistência será exposto.
5. A documentação OpenAPI e os testes de integração serão incluídos após a SPEC ser aprovada.
6. A atualização usará `PUT /categorias/{id_categoria}` e o identificador da rota será autoritativo.
7. A exclusão usará `DELETE /categorias/deletar/{id_categoria}` sem corpo de requisição; o identificador da rota será autoritativo.
8. A fronteira HTTP retornará JSON com o atributo `mensagem` para entradas inválidas (`400`) e categorias inexistentes (`404`).

## Arquitetura e componentes

- `api`: recurso de categorias, DTOs de entrada e DTOs de resposta.
- `application`: casos de uso para cada operação de categoria.
- `domain`: modelo de categoria e regras que não dependam de HTTP.
- `infrastructure`: armazenamento em memória e controle de geração de identificadores.
- `test`: testes de integração Quarkus com Rest Assured para os contratos HTTP aprovados.

## Alternativas e consequências

- Usar banco de dados foi descartado nesta mudança para manter o escopo no contrato REST. Como consequência, dados não sobrevivem ao reinício da aplicação.
- Manter `POST /categorias/add` foi preservado conforme solicitado, embora a rota seja mais específica que a convenção REST usual.
- Os contratos de atualização e exclusão usam rotas distintas do padrão de inclusão para preservar o contrato aprovado e eliminar ambiguidade sobre o identificador.