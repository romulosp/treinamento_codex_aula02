# SPEC: 066-lib-pinpad-abecs-go — exclusão de RST

Autor: Rômulo Penha

## Status

`SPEC_APROVADA`

## Objetivo

Registrar que o manual ABECS 2.12 não define um comando `RST` e impedir o envio
de um payload proprietário sob o nome de um comando ABECS.

## Contrato

- Remover `RST` do catálogo, builders, fachada e menu local.
- Não enviar `RST000`, `RST` ou qualquer variante pela serial.
- Para abortar uma operação ou limpar comunicação residual, executar o
  handshake CAN/EOT de `spec-infra-serial-cancel.md`.
- Uma reinicialização física futura exige outra Change e documentação normativa
  do fabricante do dispositivo.

## Critérios de aceite

- [ ] Nenhuma API ou opção do executável anuncia RST como comando ABECS 2.12.
- [ ] Nenhum teste aceita `RST000` como payload válido.
- [ ] O catálogo e a matriz de rastreabilidade registram RST como excluído.

## Referências

`spec-conformidade-abecs-v212.md`, `spec-command-can.md` e manual ABECS 2.12.
