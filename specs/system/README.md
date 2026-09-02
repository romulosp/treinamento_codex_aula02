# Sistema vigente

## Estado atual

- O workspace mantém localmente projetos executáveis independentes sob `apps/backend/<artifactId-sem-hifens>/`; por exemplo, `gerenciar-categorias` fica em `apps/backend/gerenciarcategorias/` e `gerenciar-tarefas` em `apps/backend/gerenciartarefas/`. O contêiner `apps/backend/` não é um módulo Maven.
- A política no `.gitignore` permite versionar somente arquivos `.md` e `.txt`, com a exceção técnica do próprio `.gitignore`.
- A API de tarefas utiliza persistência relacional com PostgreSQL via Hibernate ORM Panache (Repository pattern) e H2 em memória para testes automatizados, expondo os contratos REST documentados na mudança 019.
- A geração local aceita `bancoDados` com DB2 como padrão, PostgreSQL ou MySQL como bancos relacionais e `SEM_BANCO` para APIs sem persistência, renderizando dependências e propriedades de forma exclusiva ou omitindo totalmente blocos de datasource.
- A inicialização local prioriza o Nexus corporativo e utiliza Maven Central quando o Nexus não está alcançável, sem alterar a configuração Maven global.
- Projetos Java gerados preservam o `artifactId` com hífens no Maven, mas derivam o pacote-base removendo esses hífens; por exemplo, `gerenciar-tarefas` usa `br.com.romulopenha.gerenciartarefas`.
- O diretório de cada projeto também remove os hífens do `artifactId`, sem alterar o `artifactId` declarado no `pom.xml`.
- O `quarkus-maven-plugin` dos projetos gerados usa `extensions=true` e uma execução com o goal `build`, permitindo que `mvn quarkus:dev` inicie a aplicação.
- A estratégia compartilhada de testes define inventário, cobertura e isolamento de testes unitários Java; mudanças Java usam a Skill `java-unit-test` quando aplicável.
- O acervo local em `.agents/skills/` contém 54 skills com `SKILL.md`: 11 skills de processo/backend já existentes, 42 skills importadas do diretório de estudo e a Skill local `java-javadoc`, catalogadas em `.agents/skills/README.md`. A Skill `java-javadoc` orienta a criação e a atualização de JavaDoc em português do Brasil com base apenas em contratos comprovados.
- A configuração de fila de mensagens usa seleção exclusiva em build time (`filaMq`), com RabbitMQ como padrão e opções Kafka, IBM MQ e Redis; dependência Maven e propriedades são renderizadas apenas para o provedor selecionado. Redis é reservado à fila e não ao cache.
- O documento `NotasProjeto.md` na raiz consolida a visão do projeto e o procedimento para reproduzir um módulo localmente.
- As aplicações frontend são organizadas por plataforma em `apps/frontend/web/`, `apps/frontend/smartphone/` e `apps/frontend/desktop/`. A vitrine React demonstrativa Terra & Torra está em `apps/frontend/web/exemplo-site-web-001/`; smartphone e desktop estão documentados e reservados para aplicações futuras.
- O agente `implementador-para-teste`, em `.github/agents/`, executa mudanças até `IMPLEMENTADA` e para para avaliação humana; o prompt integral permanece responsável pelas fases de encerramento e commit.

## Estado histórico reproduzível

- As mudanças arquivadas registram a base Java 17 com Maven e Quarkus 3.2.10.Final, organizada nas camadas `api`, `application`, `domain` e `infrastructure`.
- As APIs geradas (`gerenciar-categorias` e `gerenciar-tarefas`), seus contratos, massa de teste, configurações parametrizadas, estratégia de teste com H2 e evidências estão preservados em `specs/archive/` e resumidos em `NotasProjeto.md`.
- As mudanças 013 a 019 registram a regeneração local, opções de banco (PostgreSQL, DB2, MySQL, SEM_BANCO), fallback Maven e a API de tarefas com persistência em PostgreSQL.
- Uma nova mudança aprovada deve criar a pasta específica derivada do `artifactId`, regerar localmente o módulo dentro dela, executar testes e registrar as evidências, sem versionar os arquivos produzidos.

## Processo de mudança

- A fonte canônica de fluxo, estados, gates e evidências é `specs/shared/process/workflow.md`; as Skills são o mecanismo operacional das fases.
- Toda implementação começa em `specs/changes/`, passa por revisão de SPEC, implementação, revisão, validação e aprovação, e só então é arquivada com atualização desta especificação vigente.
- Em reprovação, falha ou bloqueio, o fluxo é interrompido com indicação da evidência e da primeira fase de retorno.
