# SPEC: 066-lib-pinpad-abecs-go — Comando GOX

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Definir a continuação do processamento EMV iniciado por `GCX` para ICC EMV ou
CTLS EMV, incluindo decisão de processamento, objetos EMV solicitados e,
quando aplicável, PIN block e KSN.

## Contrato

- `GOX` exige `GCX` prévio bem-sucedido para ICC EMV ou CTLS EMV; qualquer
  outro fluxo retorna `ST_INVCALL` e não deve enviar continuação de chip.
- `SPE_ACQREF`, `SPE_MTHDPIN` e `SPE_KEYIDX` são obrigatórios. `SPE_WKENC` é
  obrigatório somente para PIN por MK/WK. O builder valida os métodos de PIN
  suportados pelo manual e nunca registra esses parâmetros sensíveis.
- `SPE_TRNTYPE`, `SPE_AMOUNT`, `SPE_CASHBACK`, `SPE_TRNCURR`, `SPE_GOXOPT`,
  `SPE_DSPMSG`, `SPE_TRMPAR`, `SPE_EMVDATA`, `SPE_TAGLIST` e `SPE_TIMEOUT`
  são opcionais, com os defaults normativos quando ausentes. `SPE_EMVDATA` é
  TLV e substitui objetos de tabela coincidentes somente no processamento ICC.
- A resposta contém `RSP_ID=GOX`, `RSP_STAT`, `PP_GOXRES` e, se solicitado,
  `PP_EMVDATA`. Quando o resultado indicar PIN online, `PP_PINBLK` é
  obrigatório e `PP_KSN` também é obrigatório nos métodos DUKPT.
- O parser preserva blobs binários e TLV, classifica `PP_PINBLK` e `PP_KSN`
  como sensíveis e não mistura o resultado de GOX com `GCXResponse` ou GTK.
- O serviço exige contexto e autorização explícita do consumidor; timeout,
  cancelamento, NAK, erro de chave, falha de cartão e status ABECS são
  distinguíveis.

## Segurança
PIN block, KSN, PAN, WKENC, KSEC e chaves nunca aparecem em logs. Respostas de erro não podem incluir dump binário. A SPEC de logging deve redigir o payload completo.

## Critérios de aceite

- [ ] Builder rejeita ausência de pré-condição, parâmetros obrigatórios e
  combinação inválida de método, índice e WKENC.
- [ ] Parser cobre decisão offline, negação, requisição online, PIN online,
  KSN DUKPT, lista TLV vazia solicitada e status relevantes.
- [ ] Resposta real de pinpad de laboratório é parseada nos tamanhos esperados.
- [ ] Nenhum material sensível aparece em SPE, PP, RSP, slog ou erro.
- [ ] Cancelamento e fechamento limpam o estado da operação.

## Referências
`spec-command-gcx.md`, `spec-command-gpn.md`, `spec-logging.md`; manual
ABECS v2.12, seção 3.7.3.


## Complemento normativo de 2026-09-13

GOX exige adquirente N2, método de PIN N1 e índice N2; WKENC é condicional. A
resposta exige PP_GOXRES N6, PIN block/KSN quando indicado pelo resultado e
PP_EMVDATA quando uma tag list foi solicitada, mesmo que vazio.
