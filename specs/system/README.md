# Sistema vigente

## Backend

- Backend Java 17 baseado em Quarkus e Maven, organizado nas camadas `api`, `application`, `domain` e `infrastructure`.
- A base Maven usa Quarkus 3.2.10.Final e o espelho `NEXUS_INTERNO`; o datasource DB2 de produção é configurado exclusivamente por variáveis de ambiente.
- O perfil de teste usa H2 em memória com schema isolado, sem depender de uma instância DB2 externa.
- Os recursos REST usam DTOs como contratos públicos e não expõem modelos de persistência.
- O nome público da aplicação e o `artifactId` Maven são `gerenciar-categorias`; o OpenAPI é disponibilizado em `/swagger_gerenciar-categorias.json`.
- O perfil de teste usa o schema H2 `GERENCIAR_CATEGORIAS`.
- O script `apps/backend/start_aplicacao.bat` é o único script de inicialização local do backend. Ele configura Java 17.0.11, Maven 3.8.8 e as variáveis `AUTH-SERVER-URL`, `CLIENT-ID`, `SECRET`, `CLIENTS-AUTHORIZED`, `DB2_JDBC_URL`, `DB2_USERNAME` e `DB2_PASSWORD` exclusivamente para a sessão do script antes de iniciar o Quarkus.

## Gerenciamento de categorias

- A API disponibiliza categorias em memória durante a execução da aplicação.
- A massa inicial contém: `1`/`CAMISAS`/`2`, `2`/`ACESSÓRIOS`/`1` e `3`/`VIDEO-GAMES`/`4`.
- Estão disponíveis os endpoints `GET /categorias/`, `GET /categorias/{id_categoria}`, `POST /categorias/add`, `PUT /categorias/{id_categoria}` e `DELETE /categorias/deletar/{id_categoria}`.
- Entradas inválidas retornam HTTP `400` com `mensagem`; categorias inexistentes retornam HTTP `404` com `mensagem`.
- Os contratos públicos são documentados em OpenAPI e possuem testes de integração Quarkus automatizados.
- Todas as operações de categorias exigem token Bearer validado por OAuth 2.0/OIDC e retornam HTTP `401` quando não autenticadas.
- Clientes autenticados são autorizados pelo claim `azp`, comparado com a lista de `CLIENTS-AUTHORIZED`; clientes ausentes ou não autorizados recebem HTTP `403`.
- OIDC é configurado exclusivamente por `AUTH-SERVER-URL`, `CLIENT-ID` e `SECRET`; a lista de clientes autorizados é configurada por `CLIENTS-AUTHORIZED`.
- A infraestrutura inclui `FiservFiltroAutenticacao` para clientes REST externos, que adiciona os cabeçalhos `Content-Type: application/json` e `Capture-Network-Code` com base em `ambiente`.

## Limitações atuais

- As categorias não são persistidas e retornam à massa inicial quando a aplicação é reiniciada.
- Não há paginação, filtros, ordenação, versionamento de API ou autorização granular por papel para categorias.

## Processo de mudança

- A fonte canônica de fluxo, estados, gates e evidências é `specs/shared/process/workflow.md`; as Skills são o mecanismo operacional das fases.
- O prompt executável `.github/prompts/executar-mudanca-spec-driven.prompt.md` identifica a primeira fase pendente e conduz automaticamente as fases posteriores somente quando seus gates são aprovados.
- Em reprovação, falha ou bloqueio, o fluxo é interrompido com indicação da evidência e da primeira fase de retorno.

## Limpeza de artefatos

- `deletar-arquivos-gerados.bat` remove, após confirmação, somente diretórios `target`, diretórios `.quarkus` e arquivos `*.log` localizados abaixo de `apps/`.
- A limpeza preserva fontes, recursos, testes, scripts, configurações, documentos e metadados Git.
