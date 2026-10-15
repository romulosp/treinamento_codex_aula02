# Revisão da SPEC — 10013

REV-001, 2026-10-14: `SPEC_APROVADA` após examinar proposal/spec/DESIGN/tasks/ADR
e regras de processo/privacidade da 10011. Instrução explícita autoriza valores
criptografados em log da aplicação; ADR delimita exceção DES privada.

Retorno proprietário aditivo preserva APIs genéricas/resumos; offline não exige
par e online valida8/10. Log não deve alterar resultado nem causar replay.
Os três campos vêm de fonte integral; WK validada/normalizada não será substituída
por literal no adapter. Testar com índices sintéticos e valores controlados.

Sem ressalva impeditiva de código. Não é aprovação de captura física nem de
instalação de chave; registros GIX/GTK não autorizam escolher outro slot sozinho.
