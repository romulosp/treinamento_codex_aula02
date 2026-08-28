# SPEC: 004-parametrizar-nome-projeto-gerado

## Status
`SPEC_APROVADA`

## Referências

- `specs/changes/004-parametrizar-nome-projeto-gerado/proposal.md`
- `specs/shared/process/workflow.md`

## Regra de nomenclatura

1. O nome do projeto é o sufixo do diretório da mudança após o prefixo numérico e o primeiro hífen. Para `003-gerenciar-categorias`, o nome é `gerenciar-categorias`.
2. O nome público e Maven usa letras minúsculas, números e hífens: `gerenciar-categorias`.
3. O nome do schema de teste é derivado do nome público, convertido para letras maiúsculas e com hífens substituídos por sublinhados: `GERENCIAR_CATEGORIAS`.
4. O diretório do projeto é `apps/backend/gerenciarcategorias/`, derivado do `artifactId` com os hífens removidos; `apps/backend/` não é o diretório final da aplicação.

## Requisitos funcionais

1. No backend da mudança 003, o `artifactId` Maven deve ser `gerenciar-categorias`.
2. No backend da mudança 003, `quarkus.application.name` deve ser `gerenciar-categorias`.
3. No backend da mudança 003, o caminho OpenAPI deve ser `/swagger_gerenciar-categorias.json`.
4. No backend da mudança 003, a URL H2 de teste e o schema padrão devem usar `GERENCIAR_CATEGORIAS`.
5. O prompt de execução deve instruir que projetos futuros substituam os placeholders de nome pelo nome derivado da mudança e usem a forma normalizada para schemas SQL.
7. A fase de implementação descrita no prompt de execução deve exigir a criação de `apps/backend/gerenciarcategorias/start_aplicacao.bat` nos projetos Java Quarkus gerados.
8. O arquivo `start_aplicacao.bat` deve configurar somente para a sessão do script os valores `JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11`, `MAVEN_HOME=C:\Desenvolvimento\apache-maven-3.8.8` e `PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%`, exibir a versão de Java, executar `mvn quarkus:dev`, pausar ao término e encerrar o escopo local.
9. O prompt de execução deve identificar a primeira fase pendente e executar automaticamente, na mesma conversa, as fases subsequentes quando o gate anterior for aprovado: revisão da SPEC, implementação, revisão da implementação, validação, aprovação e encerramento.
10. O prompt deve interromper o fluxo quando um gate for reprovado, falhar ou estiver bloqueado, informar a evidência e a primeira fase de retorno, sem executar as fases posteriores.

## Requisitos não funcionais

1. A alteração não pode modificar package Java, dependências Maven, versão Java, portas HTTP ou comportamento dos endpoints.
2. A regra deve permanecer documentada em Markdown versionado; não pode depender de arquivos gerados.
3. A suíte Maven deve continuar executando com Java 17 e Maven 3.8.8.
4. O script deve usar `setlocal` e `endlocal`, sem alterar variáveis permanentes do sistema operacional.
5. A orquestração automática deve preservar os gates, as responsabilidades e as restrições de cada Skill; não pode alterar código durante revisão, validação ou aprovação.

## Critérios de aceite

- [ ] O `pom.xml` apresenta `artifactId` igual a `gerenciar-categorias`.
- [ ] O `application.properties` não contém `nome_da_api_gerada`, `nome_api_projeto` ou `NOME_SCHEMA`.
- [ ] O `application.properties` apresenta o nome público e OpenAPI esperados e o schema `GERENCIAR_CATEGORIAS` nos dois locais aplicáveis.
- [ ] O prompt de execução documenta a regra para futuros projetos gerados.
- [ ] O prompt de execução exige a geração de `apps/backend/start_aplicacao.bat` na fase de implementação.
- [ ] `apps/backend/start_aplicacao.bat` contém a configuração temporária de Java 17.0.11 e Maven 3.8.8, a exibição de `java -version`, o comando `mvn quarkus:dev`, `pause` e `endlocal`.
- [ ] O prompt executa automaticamente as fases subsequentes após cada gate aprovado, sem exigir solicitação manual do usuário.
- [ ] Diante de um gate reprovado, o prompt interrompe o fluxo e informa a evidência e a primeira fase de retorno.
- [ ] `mvn test` executa com sucesso utilizando Java 17 e Maven 3.8.8.
