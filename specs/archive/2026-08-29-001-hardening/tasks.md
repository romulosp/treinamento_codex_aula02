# Tasks da mudança Hardening

- [x] Configurar OIDC no `application.properties`.
- [x] Implementar `SecurityConfig` com validação de configuração obrigatória para JWT/OIDC.
- [x] Criar `TenantFilter` para extrair `tenantId` da identidade autenticada.
- [x] Aplicar `@RolesAllowed("ADMIN")` nos métodos de escrita de `TarefaResource`.
- [x] Atualizar `TarefaService` e `TarefaRepository` para filtrar por `tenantId` nas queries.
- [x] Adicionar Bean Validation ao DTO `TarefaRequest`.
- [x] Escrever testes unitários e de integração cobrindo autenticação, autorização e isolamento de tenant.
- [x] Atualizar `README` com instruções de configuração de variáveis de ambiente.
