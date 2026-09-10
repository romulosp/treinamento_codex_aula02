# SPEC: 063-dashboard-atendimento-servico

## Status

`SPEC_APROVADA`

## Referências e dependências

- `proposal.md`
- `DESIGN.md`
- `tasks.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/api/rest-conventions.md`
- `specs/shared/database/migration-rules.md`
- `specs/shared/testing/testing-strategy.md`
- Keycloak acessível no ambiente local, com realm internet e cliente web configurados externamente.
- PostgreSQL acessível no ambiente local.

## Localização e plataforma

- Backend: `apps/backend/dashboardatendimentoservico/`.
- Componente de autenticação: namespace `autenticacaokeycloak` dentro de `apps/backend/dashboardatendimentoservico/`.
- Frontend: `apps/frontend/web/dashboardatendimentoservico/`.
- Backend exclusivamente em Quarkus 3.2.10.Final, Maven e Java 17.
- Pacote-base: `br.com.romulopenha.dashboardatendimentoservico`.
- São proibidos Spring, Spring Boot, Spring Data, `RestTemplate`, `@MockBean` e configurações `spring.*`.

## Sprint 001 — Backend Quarkus

### REQ-001 — Arquitetura

O backend deve ser organizado em `api`, `application`, `domain` e `infrastructure`. Recursos REST delegam casos de uso, validam a fronteira HTTP, usam DTOs e não expõem entidades JPA.

### REQ-002 — Compra

Uma compra deve possuir:

- `id`: `Long`, gerado automaticamente;
- `descricao`: texto obrigatório, de 1 a 255 caracteres;
- `valor`: decimal obrigatório, maior que zero, com duas casas decimais;
- `dataCompra`: data obrigatória;
- `fornecedor`: texto obrigatório, de 1 a 255 caracteres;
- `categoria`: texto obrigatório, de 1 a 100 caracteres;
- `status`: `PENDENTE`, `APROVADA` ou `CANCELADA`;
- `usuarioId`: subject (`sub`) do usuário autenticado que criou a compra;
- `observacao`: texto opcional de até 500 caracteres;
- `criadoEm` e `atualizadoEm`: data e hora mantidas pela aplicação.

### REQ-003 — API de compras

Todas as rotas abaixo exigem sessão autenticada:

- `GET /api/compras`: lista compras do usuário, com filtros opcionais `categoria`, `status`, `fornecedor`, `dataInicial` e `dataFinal`, ordenadas por `dataCompra DESC, id DESC`.
- `GET /api/compras/paginado?page=0&size=20&sort=dataCompra,desc`: retorna `content`, `page`, `size`, `totalElements` e `totalPages`; `page >= 0` e `1 <= size <= 100`.
- `GET /api/compras/{id}`: retorna uma compra do usuário ou HTTP 404.
- `POST /api/compras`: cria uma compra, ignora qualquer `id` ou `usuarioId` enviado e retorna HTTP 201 com cabeçalho `Location`.
- `PUT /api/compras/{id}`: substitui os campos editáveis e retorna HTTP 200 ou HTTP 404.
- `PATCH /api/compras/{id}/status`: altera somente o status e retorna HTTP 204 ou HTTP 404.
- `DELETE /api/compras/{id}`: exclui e retorna HTTP 204 ou HTTP 404.
- `GET /api/compras/dashboard`: retorna total de compras, valor total, quantidade por status, valor por categoria, valor mensal dos últimos 12 meses e as 10 compras mais recentes.

Uma compra pertencente a outro usuário deve ser tratada como inexistente, sem revelar sua existência. Entradas inválidas retornam HTTP 400; ausência ou expiração de sessão retorna HTTP 401. Erros usam JSON consistente e não expõem stack trace.

Usuários com papel `USER` podem consultar, criar e visualizar o dashboard. Usuários com papel `ADMIN` também podem atualizar, alterar status e excluir. Nesta Change, o papel `ADMIN` não remove o isolamento por proprietário.

### REQ-004 — Persistência

O backend deve usar Hibernate ORM Panache no padrão Active Record e driver PostgreSQL gerenciado pelo BOM do Quarkus. A entidade Panache/JPA e seus métodos estáticos ficam em `infrastructure`; o contrato de repositório fica em `domain`, e o adaptador encapsula as chamadas Active Record. Recursos REST e serviços de aplicação não podem chamar métodos estáticos Panache diretamente.

A URL, usuário e senha devem vir exclusivamente de `POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME` e `POSTGRESQL_PASSWORD`. Geração automática de esquema é permitida somente em desenvolvimento e teste. Produção usa validação/nenhuma geração. Flyway não faz parte desta Change.

### REQ-005 — Módulo `autenticacao-keycloak`

O componente deve expor à aplicação uma interface `GerenciadorAutenticacao` sem dependências de HTTP ou do domínio de compras. Um adaptador Quarkus em `infrastructure` integra os endpoints OIDC do Keycloak. Não deve ser usado Keycloak Admin Client para operações de token.

O login personalizado usa o grant `password`/Direct Access Grants no ambiente local. O cliente web do realm internet deve ter esse fluxo habilitado; configuração ausente ou incompatível deve causar falha clara sem expor credenciais. Authorization Code com PKCE não integra esta Change.

O módulo deve oferecer:

- autenticar usuário e criar sessão local;
- renovar tokens antes/depois da expiração do access token, enquanto o refresh token for válido;
- validar assinatura, emissor, audiência/cliente e expiração do access token;
- obter somente `sub`, nome de usuário, nome de exibição, papéis e expiração;
- encerrar a sessão local e solicitar logout/revogação ao Keycloak quando suportado.

Access token, refresh token, senha e client secret não podem ser registrados em log nem retornados ao frontend. O navegador recebe somente cookie de identificador de sessão opaco, com `HttpOnly`, `SameSite=Lax`, `Path=/` e `Secure` fora do ambiente local. A sessão do backend pode ser mantida em memória nesta aplicação local e deve ser removida no logout ou ao expirar.

### REQ-006 — API de autenticação

- `POST /api/auth/login`: recebe usuário e senha, autentica no Keycloak, cria sessão e retorna HTTP 200 com informações públicas do usuário; credenciais inválidas retornam HTTP 401.
- `POST /api/auth/refresh`: renova a sessão sem receber token no payload; retorna HTTP 204 ou HTTP 401.
- `GET /api/auth/session`: retorna HTTP 200 com `authenticated`, informações públicas e expiração; sem sessão válida retorna HTTP 401.
- `POST /api/auth/logout`: encerra a sessão, limpa o cookie e retorna HTTP 204, sendo idempotente.

Tentativas de login devem ter limitação configurável por origem e usuário. Mensagens de erro não devem revelar se o usuário existe. Um job Quarkus Scheduler deve remover sessões expiradas sem registrar tokens ou identificadores sensíveis. O armazenamento deve ser limitado por quantidade e tempo de vida para evitar crescimento sem controle.

### REQ-007 — Qualidade Java

Classes Java públicas criadas ou alteradas devem possuir JavaDoc em português do Brasil. Testes seguem integralmente `specs/shared/testing/testing-strategy.md`; a cobertura de linhas e branches aplicáveis deve ser medida de forma reproduzível, com meta entre 80% e 100%, sem declarar porcentagem não medida.

O backend deve expor liveness sem depender de PostgreSQL/Keycloak e readiness que verifique as dependências necessárias sem retornar mensagens internas. Métricas técnicas de requisições, erros, duração e sessões ativas/expiradas podem ser expostas sem incluir usuário, token, credencial ou outro rótulo de alta cardinalidade.

## Sprint 002 — Frontend

### REQ-008 — Tecnologia e páginas

O frontend deve usar React, Vite e TypeScript. Deve existir uma tela moderna e responsiva para `login.html`, uma rota protegida `pagina_principal.html` e uma tela/formulário de cadastro e edição de compra.

### REQ-009 — Proteção reutilizável de rota

Um componente reutilizável deve consultar `GET /api/auth/session` antes de renderizar páginas privadas. HTTP 401 redireciona a `login.html`. Após login bem-sucedido, o frontend redireciona a `pagina_principal.html`. O frontend não considera a simples presença de dados locais como prova de autenticação.

O frontend não pode armazenar tokens ou senha em `localStorage`, `sessionStorage`, IndexedDB, URL ou estado persistente. Requisições devem incluir cookies somente para a mesma origem/proxy configurado. O cookie HttpOnly contém exclusivamente o identificador opaco da sessão; access token e refresh token permanecem no backend.

### REQ-010 — Dashboard

A página principal deve exibir:

- cards de total de compras, valor total e quantidades por status;
- gráfico de barras para evolução mensal;
- gráfico de distribuição por categoria;
- tabela das 10 compras mais recentes;
- estados acessíveis de carregamento, vazio e erro;
- nome do usuário e ação de logout.

Os dados devem vir exclusivamente de `GET /api/compras/dashboard` e ser atualizados após criação, alteração ou exclusão.

### REQ-011 — Cadastro e edição

O formulário deve conter descrição, valor, data da compra, fornecedor, categoria, status e observação opcional, mostrar validações próximas aos campos e impedir envio duplicado. Sucesso deve informar o usuário e atualizar a listagem; falha deve preservar os dados editáveis. Alteração integral, alteração de status e exclusão devem aparecer somente a usuários `ADMIN`, sem substituir a autorização do backend.

### REQ-012 — Usabilidade e acessibilidade

A interface deve ser utilizável em desktop e dispositivos móveis, permitir navegação por teclado, ter foco visível, rótulos associados, contraste adequado e respeitar `prefers-reduced-motion`.

## Sprint 003 — Integração e execução local

### REQ-013 — Integração HTTP

O frontend deve centralizar chamadas em clientes de autenticação e compras, usar caminhos relativos `/api` e proxy do Vite no desenvolvimento. Respostas 401 em rotas privadas encerram o estado visual da sessão e redirecionam ao login.

No desenvolvimento, a origem autorizada é `http://localhost:3000`. Outras origens devem ser fornecidas por configuração externa explícita, sem default de produção e sem `*`. Como a autenticação usa cookie, operações mutáveis devem validar a origem e possuir proteção CSRF compatível; o frontend deve enviar o token CSRF fora do cookie.

### REQ-014 — Arquivo externo e mapeamento

A fonte de valores é `D:\desenvolvimento\chave_des\chave_des.properties`, nunca versionada. O gerador deve selecionar as seções de PostgreSQL do dashboard e SSO WEB/realm INTERNET, aceitar espaços ao redor do primeiro `=` e exigir, sem registrar valores:

- `HOSTNAME_DB_POSTGRESQL`;
- `PORTA_DB_POSTGRESQL`;
- `BANCO_DB_DASHBOARDCOMPRAS`;
- `USER_DB_POSTGRESQL`;
- `SENHA_DB_POSTGRESQL`;
- `OIDC_AUTH_SERVER_URL_INTERNET`;
- `OIDC_CLIENT_ID_INTERNET`;
- `OIDC_CLIENT_SECRET_INTERNET`.

O nome `BANCO_DB_DASHBOARDCOMPRAS` corrige a chave de fotogaleria repetida por engano no material original. O gerador mapeia as chaves externas para `POSTGRESQL_JDBC_URL`, `POSTGRESQL_USERNAME`, `POSTGRESQL_PASSWORD`, `OIDC_AUTH_SERVER_URL`, `OIDC_CLIENT_ID` e `OIDC_CLIENT_SECRET`.

### REQ-015 — Scripts

- Um gerador PowerShell e um template BAT sem valores devem ser versionados.
- `apps/backend/dashboardatendimentoservico/start_aplicacao.bat` deve ser gerado somente após validação completa, permanecer local e ser ignorado pelo Git.
- `testar_aplicacao.bat` deve validar pré-condições, gerar o BAT e iniciar PostgreSQL/Quarkus sem imprimir segredos.
- O frontend deve possuir `start_aplicacao_frontend.bat`, que instala dependências quando necessário e inicia o Vite.
- Falha de arquivo, seção ou chave deve ocorrer antes de substituir o BAT anterior e listar somente nomes ausentes.

### REQ-016 — Segurança e validação

Testes automatizados não devem acessar o arquivo real, PostgreSQL real ou Keycloak real; devem usar valores sintéticos, H2 quando aplicável, identidades simuladas e servidor OIDC simulado. A validação manual final deve cobrir PostgreSQL e Keycloak locais usando o arquivo externo.

Dados de exemplo devem ser criados pela tela/API autenticada. `import.sql` com proprietários artificiais não deve ser executado por padrão, pois poderia criar dados inacessíveis ou atribuídos ao usuário incorreto.

Após a implementação, deve ser executada auditoria de autenticação, autorização, IDOR, segredos, entradas, CORS/CSRF, dependências e logs. O PDF é uma evidência em `docs/security-audit/`; não deve existir gerador de relatório dentro do código de domínio da aplicação.

## Critérios de aceite

- CA-001: backend compila em Java 17 e contém somente tecnologias Quarkus/Jakarta, sem referências Spring.
- CA-002: CRUD, paginação e agregação do dashboard atendem ao contrato e isolam compras pelo `sub` autenticado.
- CA-003: módulo `autenticacao-keycloak` cobre login, refresh, validação, informações e logout sem expor tokens ao frontend.
- CA-004: `login.html` redireciona após sucesso e o guard envia acessos sem sessão a `login.html`.
- CA-005: nenhum token ou senha é mantido em armazenamento do navegador ou emitido em logs/respostas.
- CA-006: dashboard e formulário consomem dados reais da API e tratam carregamento, vazio, erro e validação.
- CA-007: PostgreSQL é configurado por variáveis e a geração de esquema fica restrita a desenvolvimento/teste.
- CA-008: gerador e scripts usam o arquivo externo, preservam valores fora do Git e falham com segurança.
- CA-009: testes unitários e de integração cobrem sucesso, erros, autenticação, autorização e isolamento; cobertura medida é registrada.
- CA-010: JavaDoc, OpenAPI, revisão da implementação, validação, auditoria de segurança e evidências estão completos.
- CA-011: teste manual do solicitante ocorre antes de `APROVADA`, arquivamento e commit final.
- CA-012: Panache Active Record permanece encapsulado em `infrastructure`; API/aplicação/domínio não dependem de entidade JPA.
- CA-013: papéis `USER` e `ADMIN`, filtros, alteração de status, health checks, métricas e limpeza de sessões obedecem aos contratos definidos.
