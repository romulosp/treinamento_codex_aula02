# Reavaliação da SPEC LMF — padding A8 da lista publicada

**Data:** 2026-09-14  
**Escopo:** correção complementar de `spec-command-lmf.md`.  
**Fonte:** manual ABECS v2.12, seção 3.4.4, p. 100, vetor de cinco nomes.

## Achado

### REV-004 — Nomes A8 com preenchimento à direita

**Severidade:** média.  
**Evidência:** o vetor LMF publica valores A8 como `SIGNALS ` e `PRESTO  `,
além de nomes com oito caracteres.  
**Impacto:** rejeitar qualquer espaço faria o parser descartar uma resposta
válida do próprio manual; expor o padding também prejudicaria a listagem CLI.
**Decisão:** resolvido. Espaços finais são tratados como padding do campo A8 e
removidos apenas na apresentação. Após remover padding, o parser exige um a
oito caracteres ASCII alfanuméricos e rejeita espaço interno ou outros bytes.

O contrato permanece restrito ao comando LMF e não altera o formato transmitido
nem os contratos MLI/DSI já aprovados. Os bytes recebidos continuam disponíveis
em `Response.RawTags` e `Response.RawTagValues` para diagnóstico tipado.

## Decisão

`SPEC_APROVADA`
