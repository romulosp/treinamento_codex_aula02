# SPEC: 021-definir-diretorio-projeto-gerado

## Status
`SPEC_APROVADA`

## Referências e dependências

- `specs/archive/2026-08-27-001-criar-projeto-java/spec.md`
- `specs/archive/2026-08-26-004-parametrizar-nome-projeto-gerado/spec.md`
- `specs/archive/2026-08-28-020-corrigir-pacote-java-artifactid/spec.md`
- `specs/shared/process/workflow.md`

## Regra de identidade e diretório

1. O nome público e o `artifactId` Maven preservam letras minúsculas, números e hífens.
2. O nome do diretório do projeto é o `artifactId` convertido para identificador de diretório, removendo todos os hífens.
3. Cada projeto Java deve ser gerado em `apps/backend/<artifactId-sem-hifens>/`.
4. O diretório `apps/backend/` é somente o contêiner local de projetos e não recebe diretamente `pom.xml`, `src/`, scripts ou artefatos de uma aplicação.
5. O `artifactId` e o pacote Java têm derivações independentes: `gerenciar-tarefas` permanece o `artifactId`, enquanto o pacote-base é `br.com.romulopenha.gerenciartarefas`.

## Requisitos funcionais

1. Para `artifactId=gerenciar-categorias`, o projeto deve estar em `apps/backend/gerenciarcategorias/`.
2. Para `artifactId=gerenciar-tarefas`, o projeto deve estar em `apps/backend/gerenciartarefas/`.
3. O `pom.xml`, `src/main`, `src/test`, `application.properties` e `start_aplicacao.bat` de cada projeto devem ficar dentro de sua pasta própria.
4. Com dois projetos gerados, os arquivos de `gerenciar-categorias` não podem ocupar ou sobrescrever os arquivos de `gerenciar-tarefas`.
5. Os comandos Maven, scripts de inicialização e instruções de execução devem ser executados a partir da pasta específica do projeto.
6. As rotinas de limpeza devem localizar e operar nos artefatos sob cada pasta de projeto, preservando as fontes e os documentos dos demais projetos.
7. A documentação de geração deve derivar a pasta do `artifactId` informado, sem usar `nomedaapigerada`, `nome_da_api_gerada` ou uma pasta fixa como destino final.

## Requisitos não funcionais

1. A regra deve ser aplicável a qualquer novo `artifactId` válido, sem depender do nome de uma API específica.
2. A mudança não altera `groupId`, `artifactId`, versão Java, dependências, portas, contratos HTTP ou pacote-base.
3. Projetos continuam independentes e podem ser testados separadamente.
4. As pastas e arquivos gerados permanecem locais e ignorados pela política documental do repositório.

## Cenários e critérios de aceite

- [ ] `gerenciar-categorias` resolve para `apps/backend/gerenciarcategorias/`, e não para `apps/backend/`.
- [ ] `gerenciar-tarefas` resolve para `apps/backend/gerenciartarefas/`, e não para `apps/backend/`.
- [ ] Em cada pasta, `pom.xml` declara o `artifactId` com hífen e as fontes usam o pacote sem hífen.
- [ ] Um inventário das duas árvores confirma que não há arquivos compartilhados entre os projetos.
- [ ] Os comandos de teste e inicialização documentados apontam para a pasta específica do projeto.
- [ ] As instruções de limpeza não removem fontes ou documentos de outro projeto.
- [ ] Não há regra normativa nova que use `nomedaapigerada` como diretório, pacote ou nome de projeto.
