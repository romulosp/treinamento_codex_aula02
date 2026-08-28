# Sistema vigente

## Estado atual

- O workspace mantém localmente o módulo executável `apps/backend/` para a API `gerenciar-tarefas`; seus arquivos de código, configuração, scripts e produtos de build continuam ignorados pela política documental do repositório.
- A política no `.gitignore` permite versionar somente arquivos `.md` e `.txt`, com a exceção técnica do próprio `.gitignore`.
- A API de tarefas utiliza persistência relacional com PostgreSQL via Hibernate ORM Panache (Repository pattern) e H2 em memória para testes automatizados, expondo os contratos REST documentados na mudança 019.
- A geração local aceita `bancoDados` com DB2 como padrão, PostgreSQL ou MySQL como bancos relacionais e `SEM_BANCO` para APIs sem persistência, renderizando dependências e propriedades de forma exclusiva ou omitindo totalmente blocos de datasource.
- A inicialização local prioriza o Nexus corporativo e utiliza Maven Central quando o Nexus não está alcançável, sem alterar a configuração Maven global.
- A estratégia compartilhada de testes define inventário, cobertura e isolamento de testes unitários Java; mudanças Java usam a Skill `java-unit-test` quando aplicável.
- O documento `NotasProjeto.md` na raiz consolida a visão do projeto e o procedimento para reproduzir um módulo localmente.
- A pasta `apps/frontend/` permanece reservada para uma futura aplicação frontend e contém somente sua documentação.

## Estado histórico reproduzível

- As mudanças arquivadas registram a base Java 17 com Maven e Quarkus 3.2.10.Final, organizada nas camadas `api`, `application`, `domain` e `infrastructure`.
- As APIs geradas (`gerenciar-categorias` e `gerenciar-tarefas`), seus contratos, massa de teste, configurações parametrizadas, estratégia de teste com H2 e evidências estão preservados em `specs/archive/` e resumidos em `NotasProjeto.md`.
- As mudanças 013 a 019 registram a regeneração local, opções de banco (PostgreSQL, DB2, MySQL, SEM_BANCO), fallback Maven e a API de tarefas com persistência em PostgreSQL.
- Uma nova mudança aprovada deve regerar localmente o módulo, executar testes e registrar as evidências, sem versionar os arquivos produzidos.

## Processo de mudança

- A fonte canônica de fluxo, estados, gates e evidências é `specs/shared/process/workflow.md`; as Skills são o mecanismo operacional das fases.
- Toda implementação começa em `specs/changes/`, passa por revisão de SPEC, implementação, revisão, validação e aprovação, e só então é arquivada com atualização desta especificação vigente.
- Em reprovação, falha ou bloqueio, o fluxo é interrompido com indicação da evidência e da primeira fase de retorno.
