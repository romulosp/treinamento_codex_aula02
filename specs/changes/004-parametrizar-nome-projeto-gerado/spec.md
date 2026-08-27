# SPEC: 004-parametrizar-nome-projeto-gerado

## Status
`RASCUNHO`

## Referências

- `specs/changes/004-parametrizar-nome-projeto-gerado/proposal.md`
- `specs/shared/process/workflow.md`

## Regra de nomenclatura

1. O nome do projeto é o sufixo do diretório da mudança após o prefixo numérico e o primeiro hífen. Para `003-gerenciar-categorias`, o nome é `gerenciar-categorias`.
2. O nome público e Maven usa letras minúsculas, números e hífens: `gerenciar-categorias`.
3. O nome do schema de teste é derivado do nome público, convertido para letras maiúsculas e com hífens substituídos por sublinhados: `GERENCIAR_CATEGORIAS`.

## Requisitos funcionais

1. No backend da mudança 003, o `artifactId` Maven deve ser `gerenciar-categorias`.
2. No backend da mudança 003, `quarkus.application.name` deve ser `gerenciar-categorias`.
3. No backend da mudança 003, o caminho OpenAPI deve ser `/swagger_gerenciar-categorias.json`.
4. No backend da mudança 003, a URL H2 de teste e o schema padrão devem usar `GERENCIAR_CATEGORIAS`.
5. O prompt de execução deve instruir que projetos futuros substituam os placeholders de nome pelo nome derivado da mudança e usem a forma normalizada para schemas SQL.

## Requisitos não funcionais

1. A alteração não pode modificar package Java, dependências Maven, versão Java, portas HTTP ou comportamento dos endpoints.
2. A regra deve permanecer documentada em Markdown versionado; não pode depender de arquivos gerados.
3. A suíte Maven deve continuar executando com Java 17 e Maven 3.8.8.

## Critérios de aceite

- [ ] O `pom.xml` apresenta `artifactId` igual a `gerenciar-categorias`.
- [ ] O `application.properties` não contém `nome_da_api_gerada`, `nome_api_projeto` ou `NOME_SCHEMA`.
- [ ] O `application.properties` apresenta o nome público e OpenAPI esperados e o schema `GERENCIAR_CATEGORIAS` nos dois locais aplicáveis.
- [ ] O prompt de execução documenta a regra para futuros projetos gerados.
- [ ] `mvn test` executa com sucesso utilizando Java 17 e Maven 3.8.8.