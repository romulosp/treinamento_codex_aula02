# Revisão da SPEC — 10012

REV-001, 2026-10-14. Revisados proposal/spec/DESIGN/tasks, workflow e fronteiras
da 10011/Android. Decisão: `SPEC_APROVADA`.

Causa comprovada: arquivo PIN ausente no pacote host, BAT chama reverse-only.
Correção é de provisionamento; alterar protocolo/relaxar PIN não se justifica.
RF/AC distinguem listener, reverse, arquivos e hardware. Modo de arquivos não
assume ownership do Bridge, instala APK ou envia comando ABECS. Testes mutantes
devem restaurar fonte válida no finally. Fonte inválida nunca vai à UI/log.

Sem ressalva impeditiva de implementação. Aprovação da SPEC não aprova compra
física nem encerra os gates HIGH_ASSURANCE/race da 10011.
