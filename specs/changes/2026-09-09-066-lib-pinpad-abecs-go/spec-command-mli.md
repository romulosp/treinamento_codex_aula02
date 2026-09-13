# SPEC: 066-lib-pinpad-abecs-go — Comando MLI

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Iniciar o carregamento de um arquivo multimídia para o armazenamento do pinpad.

## Contrato
- Entrada: nome, tipo e tamanho do arquivo conforme manual.
- Validar extensão/tipo permitido, tamanho e ausência de path traversal antes do envio.
- Iniciar a sequência `MLI → MLR* → MLE`; não considerar o arquivo disponível antes de `MLE`.
- Registrar somente metadados seguros: nome validado, tamanho e status. Nunca registrar bytes do arquivo.
- O rastro serial registra `SPE CMD=MLI`, `PP` e `RSP CMD=MLI STATUS=...`
  conforme `spec-logging.md`; qualquer campo classificado como sensível é
  redigido antes da persistência.

## Critérios de aceite
- [ ] MLI real aceita arquivo de teste permitido no pinpad físico.
- [ ] Tipo, tamanho e nome inválidos são rejeitados antes do transporte.
- [ ] Falha de MLI não permite enviar MLR/MLE.
- [ ] Timeout, cancelamento, NAK e status ABECS são retornados tipados.
- [ ] O rastro identifica `CMD=MLI` sem expor conteúdo de mídia nem caminho local.

## Referências
`spec.md` RF-012.4/RF-013, `spec-logging.md`, `spec-command-mlr.md`,
`spec-command-mle.md`; manual ABECS v2.12.


## Correção normativa de 2026-09-13

MLI exige nome alfanumérico A8 e `SPE_MFINFO(B10)`: tamanho X4 big-endian,
CRC16 B2, tipo B1 (1 PNG, 2 JPG, 3 GIF) e três bytes RUF zerados.
