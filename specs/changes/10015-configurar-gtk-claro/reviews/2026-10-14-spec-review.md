# Revisão de SPEC — 10015

REV-001 — `SPEC_APROVADA`. Proposta/SPEC/DESIGN/tasks/ADR confrontados com
workflow e instrução explícita do usuário. Escopo local claro/30, não métodos
arbitrários. Vazio significa opção explícita, ausente/null rejeitados; nenhum
fallback. PIN e DATA separados; perfil mantém08 para mudança posterior.
Manual ABECS localp85 confirma omissão de método para em claro.

Retenção privada e modo identificável suportam futura autorização, sem API
pública nova ou persistência sensível. ADR limita a DES e mantém redaction.
Critérios cobrem ambos os modos e source/config/gates. Não exigir nova aprovação
do usuário para executar o que ele acabou de instruir. Revisão local não é
auditoria independente HIGH_ASSURANCE.
