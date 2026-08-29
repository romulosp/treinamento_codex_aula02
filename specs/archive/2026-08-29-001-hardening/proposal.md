# Proposta de Hardening de Segurança

## Objetivo
Implementar controles de segurança essenciais para o backend **gerenciartarefas**:
1. Autenticação via JWT/OIDC.
2. Autorização baseada em papéis (`ADMIN`, `USER`).
3. Isolamento de tenant/usuário em todas as consultas/alterações.
4. Validação robusta de entrada.
5. Gestão de segredos via variáveis de ambiente.

## Justificativa
A auditoria revelou falhas críticas (IDOR, ausência de RBAC) que permitem acesso não autorizado a tarefas. Estas correções são necessárias para atender requisitos de confidencialidade e integridade.

## Escopo
- Código Java (Quarkus) em `apps/backend/gerenciartarefas`.
- Configurações `application.properties`.
- Não inclui frontend (não existente) ou artefatos de implantação.

## Fora de escopo
- Implementação de UI.
- Integração com serviços externos ainda não existentes.

## Riscos
- Possíveis regressões nos endpoints existentes.
- Necessidade de migração de credenciais.

## Dependências
- `quarkus-oidc` e `quarkus-security`.
- Anotações `jakarta.annotation.security.RolesAllowed`.
- Biblioteca de JWT (já incluída no Quarkus OIDC).

## Aprovação
Esta proposta deve ser revisada e marcada como **SPEC_APROVADA** antes de iniciar a implementação.
