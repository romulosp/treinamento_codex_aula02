# SPEC: 066-lib-pinpad-abecs-go — Comando MLE

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Finalizar o carregamento multimídia iniciado por `MLI` e transmitido por `MLR`.

## Contrato
- Enviar somente depois que todos os blocos esperados forem aceitos.
- Confirmar nome/tamanho/CRC conforme contrato do manual.
- Somente após resposta de sucesso o serviço pode considerar o arquivo disponível para `DSI`.
- Não expor bytes nem CRC de conteúdo sensível no log; o CRC pode ser registrado somente como metadado não reversível quando aprovado.
- O rastro serial registra `SPE CMD=MLE`, `PP` e `RSP CMD=MLE STATUS=...`
  conforme a política de redaction de `spec-logging.md`.

## Critérios de aceite
- [ ] MLE conclui um arquivo de teste no pinpad físico.
- [ ] Quantidade incompleta de blocos produz erro e não habilita DSI.
- [ ] Falha de MLE mantém estado consistente e permite nova tentativa documentada.
- [ ] Progresso final ocorre somente após confirmação real.
- [ ] O rastro identifica `CMD=MLE` sem expor conteúdo de mídia sensível.

## Referências
`spec.md` RF-012.4, `spec-logging.md`, `spec-command-mli.md`,
`spec-command-mlr.md`, `spec-command-dsi.md`; manual ABECS v2.12.


## Correção normativa de 2026-09-13

MLE é o payload literal `MLE`, sem nome, tamanho ou CRC.

## Confirma??o da carga

O callback de progresso somente comunica current=total depois de MLE000.
MLE102 (tamanho/CRC divergente) e MLE040 devem propagar erro sem comunicar
conclus?o. Uma nova carga reinicia pelo MLI; limpeza de tempor?rios ? fun??o
do pinpad conforme se??o 6.6.3. MLE n?o transporta nome/tamanho/CRC.
