# Tarefas: 063-dashboard-atendimento-servico

## Pré-condições

- [x] Resolver os achados da revisão da SPEC.
- [x] Obter `SPEC_APROVADA` antes de alterar código.
- [x] Criar `implementation-plan.md` após a aprovação da SPEC.
- [ ] Confirmar Keycloak local, cliente web e PostgreSQL no teste manual; Java 17, Maven e Node.js confirmados.

## Sprint 001 — Backend

- [x] Criar projeto Maven Quarkus único e pacote-base definidos.
- [x] Implementar `autenticacao-keycloak` e testes unitários.
- [x] Implementar API/sessão de autenticação e testes de integração com identidade simulada.
- [x] Implementar domínio, persistência, CRUD, paginação e agregações de compras.
- [x] Encapsular Panache Active Record no adaptador de infraestrutura.
- [x] Aplicar isolamento por usuário e cobrir IDOR em testes.
- [x] Implementar papéis `USER`/`ADMIN`, filtros e alteração isolada de status.
- [x] Implementar liveness, readiness, métricas seguras e limpeza de sessões expiradas.
- [x] Configurar PostgreSQL por variáveis e H2/test doubles no perfil de teste.
- [x] Documentar contratos públicos com JavaDoc e OpenAPI.

## Sprint 002 — Frontend

- [x] Criar projeto React/Vite/TypeScript.
- [x] Implementar login e guard reutilizável de sessão.
- [x] Implementar dashboard responsivo, gráficos e compras recentes.
- [x] Implementar listagem, cadastro, edição e exclusão.
- [x] Condicionar ações administrativas à UX sem substituir autorização no backend.
- [x] Cobrir estados de carregamento, vazio, erro e acessibilidade.
- [x] Criar testes de componentes e clientes HTTP.

## Sprint 003 — Integração e execução

- [x] Criar template BAT sem valores e gerador PowerShell seguro.
- [x] Corrigir/mapear `BANCO_DB_DASHBOARDCOMPRAS` e chaves OIDC internet.
- [x] Criar scripts de inicialização de backend e frontend.
- [ ] Integrar frontend, backend, PostgreSQL e Keycloak locais no teste manual.
- [x] Verificar que tokens, senhas, segredos e BAT preenchido não são rastreados nem registrados.

## Revisão e validação

- [x] Revisar implementação contra a SPEC.
- [x] Executar testes e registrar ambiente, comandos, resultados e códigos de saída em `validation.md`.
- [x] Medir cobertura de linhas e branches aplicáveis.
- [x] Executar auditoria de segurança e verificar visualmente o PDF.
- [ ] Realizar teste manual pelo solicitante.
- [ ] Aprovar somente após validação formal e ausência de pendências materiais.
