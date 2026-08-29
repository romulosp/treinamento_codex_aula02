# Design da mudança Hardening

Esboço de arquitetura:
- `SecurityConfig` configurará OIDC.
- `TenantFilter` extrairá `userId`/`tenantId` do token e adicionará ao contexto.
- `TarefaResource` receberá `@RolesAllowed("ADMIN")` nos métodos de escrita.
- `TarefaService` filtrará por `tenantId` nas consultas.
- `application.properties` usará `${env.VAR}` para segredos.
