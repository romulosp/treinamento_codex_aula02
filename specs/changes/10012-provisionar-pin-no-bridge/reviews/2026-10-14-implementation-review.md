# Revisão de implementação — 10012

Decisão: `IMPLEMENTACAO_APROVADA` em 2026-10-14. Fonte pronta IMPLEMENTADA e
SPEC aprovada. RF01/02: ramo compartilhado ProvisionOnly/defaults, três hashes,
sem COM/toolchain/Bridge/Activity; ValidateOnly anterior à escrita.
RF03: helper opt-in e BAT integrados, default reverse-only, falha exit1 com aviso
Windows explícito. RF04: parser PIN, run-as/private/hash/catch-invalidação/finally
e erros sanitizados; RF05: listener preservado e memória atualizada.

IMP-REV-001, alta, corrigida: 10011 exigia terceiro arquivo, mas BAT usado
provisionava só reverse. Falha preflight CRYPTO_MISSING comprovada/recuperada.
Sem divergência adicional nesta entrega de scripts. Não altera EMV/GOX/GPN.
Nova falha após entrada manual é GTK011 e pertence ao diagnóstico de cartão,
sem reabrir o provisionamento já comprovado. Hardware financeiro não validado.
