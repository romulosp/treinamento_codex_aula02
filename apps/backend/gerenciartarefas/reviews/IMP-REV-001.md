# Revisão da Implementação – 001-hardening

**Estado:** **REPROVADA**

## Resumo
Esta revisão analisa a implementação da mudança **001‑hardening** conforme o **SPEC**, **DESIGN** e **TASKS**. O objetivo é garantir que os requisitos de segurança (autenticação, autorização, isolamento de tenant, validação de entrada, gestão de segredos e cobertura de testes) foram atendidos.

## Pontos Verificados

| IMP‑REV | Item | Descrição | Conformidade |
|---------|------|-----------|--------------|
| **IMP‑REV‑001** | **Autenticação** | Todas as rotas requerem token JWT/OIDC via `quarkus.http.auth.permission.authenticated.paths=/*`. | ✅ Cumprido (configurado em `application.properties`). |
| **IMP‑REV‑002** | **Autorização** | Métodos de escrita (`POST`, `PUT`, `DELETE`) anotados com `@RolesAllowed("ADMIN")`. | ✅ Cumprido (presente em `TarefaResource`). |
| **IMP‑REV‑003** | **Isolamento de Tenant** | Uso de `TenantContext` para obter `tenantId` e filtrar consultas/alterações. | **❌ Falha** – Classe `TenantContext` não encontrada no código‑fonte; ausência de filtro que extrai `tenantId` do token. |
| **IMP‑REV‑004** | **Validação de Entrada** | DTO `TarefaRequest` contém Bean Validation (`@NotBlank`, `@Size`). | ✅ Cumprido. |
| **IMP‑REV‑005** | **Gestão de Segredos** | `application.properties` referencia variáveis de ambiente (`${env.VAR}`). | ✅ Cumprido. |
| **IMP‑REV‑006** | **Cobertura de Testes** | 100 % de cobertura nas rotas modificadas (unit + integração). | **❌ Falha** – Não há testes que cobrem autenticação, autorização e isolamento de tenant. |
| **IMP‑REV‑007** | **Configuração de Segurança (SecurityConfig / TenantFilter)** | Projeto prevê classes `SecurityConfig` e `TenantFilter` (design). | **❌ Falha** – Arquivos não existem; nenhuma configuração explícita de OIDC além das propriedades. |

## Conclusões
- **Autenticação** e **autorização** estão corretas.
- **Validação** e **gestão de segredos** também atendem ao SPEC.
- **Isolamento de tenant** está **incompleto**, pois não há implementação de `TenantContext` nem de `TenantFilter` que extraia o `tenantId` do token.
- **Cobertura de testes** não foi verificada; não há testes de segurança.
- Falta a classe de configuração de segurança (`SecurityConfig`) mencionada no DESIGN.

### Recomendação
Implementar:
1. Classe `TenantContext` (por exemplo, usando `ThreadLocal` ou `CDI` para armazenar o `tenantId`).
2. `TenantFilter` (ou `ContainerRequestFilter`) que leia o token OIDC, extraia `tenantId`/`userId` e registre no `TenantContext`.
3. Configurar `SecurityConfig` se necessário para customizar o OIDC.
4. Adicionar testes unitários e de integração que verifiquem:
   - Rejeição de chamadas sem token (401).
   - Rejeição de usuários sem papel `ADMIN` nas rotas de escrita (403).
   - Garantia de que usuários só acessam tarefas do seu tenant.
5. Atualizar o README com instruções de definição das variáveis de ambiente.

Só após a implementação desses itens a mudança poderá ser **aprovada**.

---
*Esta revisão foi gerada automaticamente por um agente sub‑substantivo.*
