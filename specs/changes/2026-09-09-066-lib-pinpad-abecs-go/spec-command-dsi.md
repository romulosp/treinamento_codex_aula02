# SPEC: 066-lib-pinpad-abecs-go — Comando DSI

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Solicitar a exibição de um arquivo de imagem/multimídia previamente carregado no pinpad.

## Contrato
- Entrada: nome do arquivo identificado no armazenamento lógico do pinpad.
- O fluxo deve verificar que o carregamento `MLI/MLR/MLE` foi concluído quando necessário.
- Enviar `DSI` por fila e interpretar ACK/status/resposta.
- Não incluir o conteúdo binário da imagem no log de comando.

## Critérios de aceite
- [ ] Imagem carregada é exibida em pinpad físico compatível.
- [ ] Nome inválido, arquivo ausente e status de display são diferenciados.
- [ ] Cancelamento e timeout fecham corretamente a operação.
- [ ] A validação QR Code só pode referenciar esta SPEC depois de testar a exibição física.

## Referências
`spec.md` RF-012.4/RF-013, `spec-command-qrcode.md`; manual ABECS v2.12.


## Correção normativa de 2026-09-13

DSI serializa o nome alfanumérico A8 em `SPE_MFNAME`.
