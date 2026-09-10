# Plano de implementação: 063-dashboard-atendimento-servico

## Status

`IMPLEMENTADA`

## Impactos previstos

- Novo backend Quarkus em `apps/backend/dashboardatendimentoservico/`.
- Novo frontend React/Vite/TypeScript em `apps/frontend/web/dashboardatendimentoservico/`.
- Novos gerador PowerShell e template BAT versionados em `scripts/`.
- Nenhuma alteração em aplicações existentes.

## Estratégia

1. Criar o backend em camadas `api`, `application`, `domain` e `infrastructure`.
2. Encapsular a entidade Active Record Panache em um adaptador do contrato de repositório.
3. Implementar sessão BFF em memória com cookie opaco, proteção CSRF e adaptador OIDC do Keycloak.
4. Implementar CRUD, paginação, filtros, papéis, isolamento por `sub` e agregações.
5. Criar frontend editorial responsivo com clientes HTTP centralizados e guard de sessão.
6. Criar scripts seguros para ler as seções PostgreSQL/SSO do arquivo externo sem registrar valores.

## Testes unitários

- JUnit 5 e Mockito sem container Quarkus para regras de compra, sessão, autenticação e validação de origem/CSRF.
- Vitest e Testing Library para clientes, guard, login, dashboard e formulário.
- JaCoCo para cobertura Java e V8 para cobertura TypeScript.

## Testes de integração

- `@QuarkusTest`, Rest Assured, H2 e identidades/sessões sintéticas.
- Servidor OIDC real e arquivo externo real ficam restritos à validação manual.
- Testes do gerador usam arquivo temporário com valores sintéticos.

## Qualidade e segurança

- JavaDoc em português do Brasil nos contratos públicos relevantes.
- OpenAPI para endpoints e respostas.
- Tokens e credenciais não aparecem em resposta, log, documentação gerada ou armazenamento do navegador.
- Repositórios filtram sempre por proprietário; recursos administrativos não anulam esse isolamento.
- Liveness não consulta dependências; readiness não expõe exceções.
- Auditoria de segurança e PDF atual serão produzidos somente após revisão da implementação.

## Riscos e mitigação

- Direct Access Grants indisponível: falha clara de autenticação e registro da pré-condição na validação.
- Sessão em memória: armazenamento limitado e limpeza periódica; reinício encerra sessões.
- CORS/CSRF: origem allowlist, cookie `SameSite=Lax` e token CSRF fora do cookie.
- Ferramentas locais indisponíveis: registrar evidência objetiva e aplicar fallback permitido pelo workflow.

## Decisões técnicas

- Backend imperativo; Hibernate ORM JDBC não será envolvido artificialmente em `Uni`.
- Keycloak será acessado via REST Client, sem Keycloak Admin Client.
- O frontend usa caminhos relativos `/api` e proxy do Vite.
- Dados de exemplo serão criados pela API autenticada; não haverá `import.sql` padrão.
