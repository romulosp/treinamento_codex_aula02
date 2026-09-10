# Proposta: 063-dashboard-atendimento-servico

## Status

`SPEC_APROVADA`

## Responsável e data

- Responsável: equipe do projeto
- Data: 2026-09-07

## Referências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- `specs/shared/architecture/backend-java.md`
- `specs/shared/api/rest-conventions.md`
- `specs/shared/database/migration-rules.md`
- `specs/shared/testing/testing-strategy.md`
- Change arquivada `026-chaves-aplicacao`
- Solicitação da Change 063 fornecida em 2026-09-07

## Problema e objetivo

Construir uma aplicação local para gerenciar compras e apresentá-las em um dashboard, com backend integralmente em Quarkus, frontend moderno e autenticação real por Keycloak/OIDC.

O objetivo adicional é isolar a integração com o Keycloak em um módulo reutilizável chamado `autenticacao-keycloak`, responsável por login, renovação, validação, consulta segura de informações do token e logout. Após o login, o usuário deve acessar `pagina_principal.html`; sem sessão válida, deve ser redirecionado a `login.html`.

## Escopo

- Backend Quarkus 3, Maven e Java 17 para CRUD e consultas agregadas de compras.
- PostgreSQL com Hibernate ORM Panache no padrão Active Record isolado na infraestrutura e contratos REST baseados em DTOs.
- Componente interno `autenticacao-keycloak`, desacoplado do domínio de compras dentro do único backend Quarkus.
- Frontend React/Vite com login, proteção reutilizável de rota, dashboard e cadastro de compra.
- Sessão protegida no backend, sem persistir access token ou refresh token em `localStorage` ou `sessionStorage`.
- Scripts Windows para gerar configuração local a partir de `D:\desenvolvimento\chave_des\chave_des.properties` e iniciar os componentes.
- Testes unitários, testes de integração, JavaDoc e auditoria de segurança com relatório PDF.
- Health checks, métricas técnicas e limpeza automática de sessões expiradas.

## Fora de escopo

- Spring, Spring Boot, Spring Data, `RestTemplate` ou qualquer componente Spring.
- Administração de usuários, grupos, realms ou clientes do Keycloak.
- Realms e clientes mobile/intranet não usados pelo dashboard.
- Pagamentos, pedidos, estoque, fornecedores como cadastros próprios ou integrações financeiras.
- Deploy em produção, alta disponibilidade e persistência distribuída de sessão.
- Versionamento de credenciais, tokens ou do BAT local preenchido.
- Flyway ou alteração da política global de migrações.
- Qute, PDFBox, carga automática de dados reais e dependências sem uso comprovado.

## Impactos e riscos

- O login por usuário e senha depende de Direct Access Grants habilitado no cliente Keycloak; a ausência dessa configuração bloqueia a execução e deve ser evidenciada na validação.
- Sessões em memória são adequadas ao uso local pedido, mas são perdidas ao reiniciar o backend e não atendem múltiplas instâncias.
- O arquivo externo contém valores sensíveis; geradores, logs, testes, documentos e commits não podem revelar seu conteúdo.
- O nome da Change permanece “dashboard de atendimento de serviço”, enquanto compras constituem o domínio funcional desta entrega, conforme consolidado pelo material v4.0.

## Critérios para aprovação da SPEC

- O domínio funcional de compras e a nomenclatura da Change estão explícitos.
- Direct Access Grants está definido como pré-condição verificável do cliente web no realm internet.
- `autenticacao-keycloak` está definido como componente interno do backend, não como serviço implantável independente.
- Campos, limites e estados da compra estão definidos de forma verificável.
- Todos os achados bloqueantes ou importantes da revisão da SPEC estão resolvidos.
