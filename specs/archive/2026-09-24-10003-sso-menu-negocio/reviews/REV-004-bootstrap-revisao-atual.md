# REV-004 — revisão atual do bootstrap debug

## Status

`SPEC_APROVADA`

## Motivação

A reinstalação incremental preservou uma revisão verificada do plugin anterior
à alteração da porta da API. O bootstrap existente recuperava essa revisão
antes de consultar o asset atual e, portanto, mantinha configuração obsoleta.

## Decisão

A solicitação explícita de correção em 2026-09-23 aprova a extensão de RF-07:
no build debug, a revisão incorporada atual deve ser validada e ativada antes da
recuperação de uma revisão verificada anterior. A rejeição do candidato atual
mantém o fallback seguro para o repositório verificado.

## Impacto

Não há mudança de contrato público, assinatura, pipeline de segurança nem
variante release. A correção altera somente a ordem do bootstrap debug.
