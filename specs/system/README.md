# Sistema vigente

## Estado atual

- O workspace está intencionalmente reinicializado: não há módulo executável em `apps/backend/`, arquivos de código, configurações, scripts, diagramas ou produtos de build.
- A política no `.gitignore` permite versionar somente arquivos `.md` e `.txt`, com a exceção técnica do próprio `.gitignore`.
- O documento `NotasProjeto.md` na raiz consolida a visão do projeto e o procedimento para reproduzir um módulo localmente.
- A pasta `apps/frontend/` permanece reservada para uma futura aplicação frontend e contém somente sua documentação.

## Estado histórico reproduzível

- As mudanças arquivadas registram a base Java 17 com Maven e Quarkus 3.2.10.Final, organizada nas camadas `api`, `application`, `domain` e `infrastructure`.
- A última API gerada era `gerenciar-categorias`, com categorias em memória, DTOs, OpenAPI, testes de integração, OAuth 2.0/OIDC e autorização de clientes pelo claim `azp`.
- Os contratos, a massa inicial, as configurações parametrizadas, a estratégia de teste com H2 e as evidências estão preservados em `specs/archive/` e resumidos em `NotasProjeto.md`.
- Uma nova mudança aprovada deve regerar localmente o módulo, executar testes e registrar as evidências, sem versionar os arquivos produzidos.

## Processo de mudança

- A fonte canônica de fluxo, estados, gates e evidências é `specs/shared/process/workflow.md`; as Skills são o mecanismo operacional das fases.
- Toda implementação começa em `specs/changes/`, passa por revisão de SPEC, implementação, revisão, validação e aprovação, e só então é arquivada com atualização desta especificação vigente.
- Em reprovação, falha ou bloqueio, o fluxo é interrompido com indicação da evidência e da primeira fase de retorno.
