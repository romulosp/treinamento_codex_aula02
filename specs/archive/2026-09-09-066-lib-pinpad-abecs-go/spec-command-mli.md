# SPEC: 066-lib-pinpad-abecs-go — Comando MLI

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Iniciar o carregamento de um arquivo multimídia para o armazenamento do pinpad.

## Contrato
- Entrada: nome, tipo e tamanho do arquivo conforme manual.
- Validar o nome A8, tamanho e CRC antes do envio. O tipo PNG/JPG/GIF é
  identificado pela assinatura; conteúdo de tipo desconhecido usa `B1=00h`
  (RUF) e não é rejeitado durante MLI.
- Iniciar a sequência `MLI → MLR* → MLE`; não considerar o arquivo disponível antes de `MLE`.
- Registrar somente metadados seguros: nome validado, tamanho e status. Nunca registrar bytes do arquivo.
- O rastro serial registra `SPE CMD=MLI`, `PP` e `RSP CMD=MLI STATUS=...`
  conforme `spec-logging.md`; qualquer campo classificado como sensível é
  redigido antes da persistência.

## Critérios de aceite
- [ ] MLI real aceita arquivo de teste permitido no pinpad físico.
- [ ] Nome, tamanho e conteúdo vazio inválidos são rejeitados antes do
  transporte; tipo desconhecido é transmitido com `B1=00h`.
- [ ] O tipo desconhecido não impede MLI/MLR/MLE; se não houver suporte, o erro
  de formato só pode vir do pinpad ao usar DSI.
- [ ] Falha de MLI não permite enviar MLR/MLE.
- [ ] Timeout, cancelamento, NAK e status ABECS são retornados tipados.
- [ ] O rastro identifica `CMD=MLI` sem expor conteúdo de mídia nem caminho local.

## Referências
`spec.md` RF-012.4/RF-013, `spec-logging.md`, `spec-command-mlr.md`,
`spec-command-mle.md`; manual ABECS v2.12.


## Correção normativa de 2026-09-13

MLI exige nome alfanumérico A8 e `SPE_MFINFO(B10)`: tamanho X4 big-endian,
CRC16 B2, tipo B1 (1 PNG, 2 JPG, 3 GIF) e três bytes RUF zerados.

## Prazo da carga multimídia local

O CLI coleta caminho e nome antes de criar o contexto de 60 segundos da carga.
O nome deve ter oito caracteres ASCII alfanuméricos (exemplo: QRCODE01); nome
inválido é rejeitado antes da serial. A biblioteca respeita contextos
cancelados ou expirados, sem renová-los. A carga não depende das capacidades
GIX; a avaliação de exibição cabe ao DSI. A tolerância MFINFO >= 7 da seção
6.6.1 é requisito do receptor; o emissor continua enviando B10 completo,
incluindo os três bytes RUF.

## Compatibilidade de tipo desconhecido — revisão de 2026-09-14

A regra de validação local de tipo contrariava a seção 6.6.1: o pinpad deve
aceitar MLI sem avaliar se o formato é suportado e postergar essa crítica para
o uso. O builder enviará `B1=00h` (valor reservado RUF) para uma assinatura
desconhecida e transferirá os dados normalmente. MLI não consultará GIX nem
rejeitará o arquivo pela extensão ou assinatura.
