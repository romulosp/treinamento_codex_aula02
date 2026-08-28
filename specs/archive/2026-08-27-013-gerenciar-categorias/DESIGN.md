# Design: 013-gerenciar-categorias

## Contexto

Esta mudança recompõe localmente o módulo removido na reinicialização documentada. Ela reproduz somente a capacidade da mudança 003: gerenciamento de categorias em memória, sem persistência nem segurança OIDC.

## Referências

- `specs/changes/013-gerenciar-categorias/spec.md`
- `specs/archive/2026-08-26-003-gerenciar-categorias/DESIGN.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/api/rest-conventions.md`
- `specs/shared/testing/testing-strategy.md`

## Decisões

1. O módulo será criado em `apps/backend/`, com Quarkus 3.2.10.Final, Maven e Java 17.
2. `CategoriaResource` será a fronteira HTTP; ela validará o contrato, mapeará DTOs e delegará à aplicação.
3. `CategoriaService` concentrará as operações de listar, detalhar, criar, atualizar e excluir.
4. `ArmazenamentoCategoriasEmMemoria` pertencerá à infraestrutura e será o único componente que mantém estado, com reinicialização da massa fixa no ciclo de vida da aplicação.
5. DTOs `record` separarão contratos HTTP do modelo de domínio `Categoria`.
6. Exceções de negócio específicas e seus mapeadores JAX-RS produzirão corpos JSON com `mensagem` para os status `400` e `404`.
7. A documentação OpenAPI será declarada nas operações do recurso; testes de integração usarão `@QuarkusTest` e Rest Assured. Testes unitários chamarão diretamente serviço, armazenamento e modelo, sem banco nem container.

## Arquitetura e componentes

- `api`: `CategoriaResource`, DTOs de entrada e saída e mapeadores de erro.
- `application`: `CategoriaService`.
- `domain`: `Categoria`, exceções e validações de negócio.
- `infrastructure`: `ArmazenamentoCategoriasEmMemoria`.
- `test`: testes unitários JUnit 5 e testes de integração Quarkus com Rest Assured.

## Alternativas e consequências

- Banco de dados e Panache foram descartados para preservar o escopo. Como consequência, alterações não sobrevivem a reinicializações.
- A proteção OIDC histórica foi descartada porque não é requisito da SPEC de categorias desta mudança; como consequência, os contratos são acessíveis sem token.
- As rotas `add` e `deletar` foram preservadas por compatibilidade com o contrato aprovado, apesar de não serem a forma REST mais convencional.
