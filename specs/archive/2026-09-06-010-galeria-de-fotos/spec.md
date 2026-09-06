# SPEC: Desenvolvimento e Entrega de Galeria de Fotos

## Status

`SPEC_APROVADA`

## Localização obrigatória

- Backend: `apps/backend/fotogaleria/`.
- Frontend: `apps/frontend/web/fotogaleria/`.
- Script do backend: `apps/backend/fotogaleria/testar_aplicacao.bat`.
- Script do frontend: `apps/frontend/web/fotogaleria/start_aplicacao_frontend.bat`.
- É proibida a geração em `galeria/backend`, `galeria/frontend` ou em outra pasta raiz alternativa.

## Sprint 001 — Backend

### REQ-001 — Plataforma e arquitetura

O backend deve usar Quarkus 3.2, Maven, Java 17 e pacote-base `br.com.romulopenha.fotogaleria`, separado nas camadas `api`, `application`, `domain` e `infrastructure`. Recursos REST devem usar DTOs e nunca expor entidades JPA.

### REQ-002 — Modelo persistido

Uma foto deve possuir:

- `id`: `Long`, gerado automaticamente;
- `nome`: `String`, obrigatório, derivado do nome original do arquivo;
- `descricao`: `String`, opcional, com até 5.000 caracteres;
- `dataUpload`: `LocalDateTime`, preenchido automaticamente no upload;
- `imagemBase64`: `String`, persistida como `TEXT` no PostgreSQL.

O valor de `imagemBase64` deve ser um Data URL (`data:<tipo>;base64,<conteúdo>`) utilizável diretamente pelo frontend. São aceitos JPEG, PNG, GIF e WebP. Arquivos vazios, tipos não permitidos ou arquivos acima de 5 MiB devem ser rejeitados com HTTP 400.

### REQ-003 — API REST

- `POST /api/fotos/upload`: recebe `multipart/form-data` com campo obrigatório `arquivo` e campo opcional `descricao`, persiste a foto e retorna a foto completa com HTTP 201.
- `GET /api/fotos?page=0&size=12`: retorna página ordenada por `dataUpload` decrescente, contendo `content`, `page`, `size`, `totalElements` e `totalPages`. Cada item omite `imagemBase64`. `page` deve ser maior ou igual a zero; `size`, entre 1 e 50.
- `GET /api/fotos/{id}`: retorna a foto completa; retorna HTTP 404 quando inexistente.
- `PUT /api/fotos/{id}`: recebe JSON `{ "descricao": "..." }`, altera somente a descrição e retorna o resumo atualizado; retorna HTTP 404 quando inexistente.
- `DELETE /api/fotos/{id}`: remove a foto e retorna HTTP 204; retorna HTTP 404 quando inexistente.

Entradas inválidas devem produzir resposta JSON compreensível sem stack trace.

### REQ-004 — Persistência e execução

O datasource deve ser PostgreSQL, configurável por variáveis de ambiente com valores locais de desenvolvimento como fallback. A criação de esquema deve ocorrer em modo de desenvolvimento. O backend deve operar na porta 2000 quando iniciado pelo script da Sprint 004.

### REQ-005 — Qualidade Java

Todas as classes públicas devem conter JavaDoc em português do Brasil baseado no contrato implementado. Regras da camada de aplicação devem possuir testes unitários JUnit 5 e Mockito independentes de banco, `EntityManager`, Arc e container Quarkus.

## Sprint 002 — Frontend

### REQ-006 — Login demonstrativo

O frontend deve apresentar login estático com credenciais `admin/admin`, sem alegar segurança de servidor. Sessão local pode durar somente até o recarregamento da página.

### REQ-007 — Galeria responsiva

A galeria deve exibir fotos em grid de 3 colunas no desktop, 2 no tablet e 1 no mobile. Cada item deve exibir miniatura, nome, descrição e data. Estados de carregamento, vazio e erro devem ser compreensíveis.

### REQ-008 — Paginação e ações

A interface deve apresentar anterior, próximo e números de página. Cada foto deve permitir editar somente a descrição, excluir após confirmação e abrir um lightbox com imagem maior e detalhes.

### REQ-009 — Upload múltiplo

O botão “Adicionar fotos” deve aceitar múltiplos arquivos de imagem, apresentar pré-visualização antes do envio, permitir descrição individual e mostrar progresso por arquivo. Os arquivos devem ser enviados individualmente para o endpoint singular da API.

### REQ-010 — Apresentação

O frontend deve usar Material UI, ícones Material e transições suaves, com identidade visual editorial/fotográfica e foco visível para navegação por teclado.

### REQ-011 — Script do frontend

`start_aplicacao_frontend.bat` deve verificar `node_modules`, executar `npm install` quando ausente, iniciar `npm run start` na porta 3000, mostrar `http://localhost:3000` e terminar com `pause`.

## Sprint 003 — Integração

### REQ-012 — Cliente HTTP

`fotoService.js` deve usar Fetch com caminhos relativos e cobrir upload com `FormData`, listagem paginada, detalhe, atualização de descrição e exclusão. O cliente não deve fixar a URL absoluta do backend.

### REQ-013 — Proxy e atualização da tela

O Vite deve encaminhar `/api` para `http://localhost:2000`. A listagem deve ser recarregada após upload, edição e exclusão concluídos.

## Sprint 004 — Automação

### REQ-014 — PostgreSQL local

O backend deve conter `docker-compose.yml` para iniciar PostgreSQL com healthcheck, volume nomeado e credenciais locais sobrescrevíveis por variáveis de ambiente.

### REQ-015 — Script do backend

`testar_aplicacao.bat` deve:

1. validar a existência de `D:\desenvolvimento\chave_des\chave_des.properties`;
2. gerar `start_aplicacao.bat` por meio de um gerador versionado que selecione dinamicamente a seção `##DB POSTGRESQL <nome-do-projeto>`, usando `fotogaleria` como nome deste projeto;
3. dentro da seção selecionada, exigir `HOSTNAME_DB_POSTGRESQL`, `PORTA_DB_POSTGRESQL`, `BANCO_DB_TAREFA`, `USER_DB_POSTGRESQL` e `SENHA_DB_POSTGRESQL`, aceitando espaços ao redor do primeiro `=`;
4. mapear os valores para `POSTGRES_URL`, `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD`, sem imprimir valores ou gravá-los em artefatos versionados;
5. iniciar o PostgreSQL por Docker Compose e abrir o Quarkus em modo desenvolvimento com `mvn quarkus:dev -Dquarkus.http.port=2000`;
6. falhar antes de iniciar serviços quando a seção ou uma chave obrigatória estiver ausente ou vazia, informando apenas nomes de seção/chaves;
7. manter `start_aplicacao.bat` local e ignorado pelo Git;
8. mostrar mensagens e URLs claras;
9. manter os terminais abertos.

## Critérios de aceite

- CA-001: todos os artefatos existem exatamente nos quatro caminhos obrigatórios.
- CA-002: a API implementa os cinco endpoints e não expõe `imagemBase64` na listagem.
- CA-003: entidade JPA permanece na infraestrutura e DTOs definem o contrato HTTP.
- CA-004: testes unitários exercitam sucessos, entradas inválidas e fotos inexistentes.
- CA-005: login, grid responsivo, paginação, upload múltiplo com preview/progresso, edição, confirmação de exclusão e lightbox estão implementados.
- CA-006: toda comunicação usa `fotoService.js`, Fetch, URL relativa e proxy `/api`.
- CA-007: scripts e Docker Compose atendem aos requisitos das Sprints 002 e 004.
- CA-008: build/testes disponíveis são executados e registrados com comando, resultado e código de saída.
- CA-009: revisão da SPEC, revisão da implementação, validação, auditoria de segurança e aprovação são registradas.
- CA-010: a documentação declara que login sem proteção no backend é aceito exclusivamente para demonstração local.
