# Proposta: 003-gerenciar-categorias

## Status
`IMPLEMENTADA`

## Responsável e data

- Responsável: a definir
- Data: 2026-08-26

## Referências

- `specs/shared/architecture/backend-java.md`
- `specs/shared/api/rest-conventions.md`
- `specs/shared/testing/testing-strategy.md`

## Problema e objetivo

O backend ainda não possui uma capacidade de negócio para demonstrar uma API REST completa. Disponibilizar operações de recuperação, inclusão, atualização, exclusão e detalhamento de categorias, sem integração com banco de dados nesta etapa.

## Escopo

- Disponibilizar os endpoints de categorias descritos na SPEC.
- Retornar e receber contratos JSON com os campos `id_categoria`, `nome_categoria` e `quantidade_produtos`, conforme aplicável.
- Manter as categorias em memória durante a execução da aplicação.
- Iniciar a aplicação com as três categorias fornecidas como massa inicial.
- Criar testes automatizados para os contratos aprovados.

## Fora de escopo

- Persistência em banco de dados, migrations, entidades JPA e repositórios Panache.
- Autenticação e autorização.
- Paginação, filtros e ordenação da lista de categorias.
- Versionamento de API.
- Definição de contratos de erro ainda não informados.

## Impactos e riscos

- Dados mantidos em memória são perdidos ao reiniciar a aplicação e não devem ser usados como persistência.
- Os contratos de atualização e exclusão usam exclusivamente o identificador presente na rota.

## Critérios para aprovação da SPEC

- A rota e a forma de identificar a categoria na atualização estão definidas.
- O contrato de exclusão define que não há corpo de requisição e que o identificador da rota é autoritativo.
- Todos os critérios de aceite devem poder ser testados sem banco de dados.