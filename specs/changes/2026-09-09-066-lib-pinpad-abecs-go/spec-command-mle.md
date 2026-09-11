# SPEC: 066-lib-pinpad-abecs-go — Comando MLE

## Status
`SPEC_APROVADA`

## Objetivo
Finalizar o carregamento multimídia iniciado por `MLI` e transmitido por `MLR`.

## Contrato
- Enviar somente depois que todos os blocos esperados forem aceitos.
- Confirmar nome/tamanho/CRC conforme contrato do manual.
- Somente após resposta de sucesso o serviço pode considerar o arquivo disponível para `DSI`.
- Não expor bytes nem CRC de conteúdo sensível no log; o CRC pode ser registrado somente como metadado não reversível quando aprovado.

## Critérios de aceite
- [ ] MLE conclui um arquivo de teste no pinpad físico.
- [ ] Quantidade incompleta de blocos produz erro e não habilita DSI.
- [ ] Falha de MLE mantém estado consistente e permite nova tentativa documentada.
- [ ] Progresso final ocorre somente após confirmação real.

## Referências
`spec.md` RF-012.4, `spec-command-mli.md`, `spec-command-mlr.md`, `spec-command-dsi.md`; manual ABECS v2.12.
