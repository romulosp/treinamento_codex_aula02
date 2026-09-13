# SPEC: 066-lib-pinpad-abecs-go — Comando DEX

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Exibir uma mensagem textual do tamanho permitido pelo protocolo ABECS usando `DEX`.

## Contrato
- Builder calcula e serializa `DEX_MSGLEN` no formato definido pelo manual.
- Validar tamanho máximo antes do envio; não produzir comprimento inconsistente.
- Enviar pelo worker único, interpretar ACK/status e preservar resposta bruta.
- Rastro: `SPE CMD=DEX`, `PP`, `RSP CMD=DEX STATUS=...`; redigir conteúdo caso contenha dado sensível.

## Critérios de aceite
- [ ] Mensagem válida é exibida em pinpad físico real.
- [ ] Mensagem vazia, excedente ou com codificação inválida produz erro de validação documentado.
- [ ] Frames agrupados e fragmentados são tratados.
- [ ] Timeout, NAK, cancelamento e status ABECS são distinguíveis.

## Referências
`spec.md` RF-012.3/RF-013; manual ABECS v2.12.
