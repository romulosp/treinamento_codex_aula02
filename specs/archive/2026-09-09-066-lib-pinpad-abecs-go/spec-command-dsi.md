# SPEC: 066-lib-pinpad-abecs-go — Comando DSI

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Solicitar a exibição de um arquivo de imagem/multimídia previamente carregado no pinpad.

## Contrato
- Entrada: nome do arquivo identificado no armazenamento lógico do pinpad.
- O nome é alfanumérico A8 em `SPE_MFNAME`. A mídia pode ter sido persistida em
  uma sessão anterior; DSI não depende de um histórico local de MLI/MLR/MLE.
- Enviar `DSI` por fila e interpretar ACK/status/resposta.
- `DSI000` significa que o firmware aceitou o comando. O retorno não contém
  confirmação de renderização, conteúdo exibido ou comparação de pixels.
- A validação funcional em hardware exige observação do display pelo operador.
  Se houver `DSI000` sem imagem visível, registrar o fato separadamente e
  coletar GIX/capacidades, nome consultado por LMF e propriedades do arquivo para
  diagnóstico; não declarar exibição bem-sucedida apenas pelo status.
- Não incluir o conteúdo binário da imagem no log de comando.

## Critérios de aceite
- [ ] Imagem carregada é exibida em pinpad físico compatível.
- [ ] A evidência registra separadamente `DSI000` e a confirmação visual do
  conteúdo esperado no display.
- [ ] Nome inválido, arquivo ausente e status de display são diferenciados.
- [ ] Cancelamento e timeout fecham corretamente a operação.
- [ ] A validação QR Code só pode referenciar esta SPEC depois de testar a exibição física.

## Referências
`spec.md` RF-012.4/RF-013, `spec-command-qrcode.md`; manual ABECS v2.12.


## Correção normativa de 2026-09-13

DSI serializa o nome alfanumérico A8 em `SPE_MFNAME`.
