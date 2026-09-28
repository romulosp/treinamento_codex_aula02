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

## Continuação no utilitário local — 2026-09-13

O utilitário local deve conservar a resposta e o valor do último GCX concluído
para construir a opção 20. O GOX não pode usar uma rede credenciadora fixa:

- `PP_AIDTABINFO` é uma lista de entradas N6 formadas por `TAB_ACQ` N2,
  `TAB_RECIDX` N2 e `T1_APPTYPE` N2;
- `SPE_ACQREF` deve ser escolhido entre os valores `TAB_ACQ` presentes nessa
  lista, removendo duplicidades e preservando a ordem retornada;
- quando houver uma única rede, ela é usada como padrão; com múltiplas redes, o
  operador deve selecionar uma das opções apresentadas;
- `SPE_AMOUNT` deve repetir o valor do GCX da mesma transação, pois sua ausência
  no GOX significa valor zero e não herança automática;
- o operador deve escolher `SPE_MTHDPIN` e informar `SPE_KEYIDX`; métodos MK/WK
  também exigem `SPE_WKENC` no tamanho correspondente;
- entradas inválidas ou ausência de GCX ICC/CTLS EMV devem ser recusadas antes
  de `ContinueEMV`.

### Critérios de aceite do utilitário

- [x] CA-GOX-CLI-001: um `PP_AIDTABINFO="080301"` produz
  `SPE_ACQREF="08"`, nunca o valor fixo `01`.
- [x] CA-GOX-CLI-002: listas com redes repetidas oferecem cada `TAB_ACQ` uma
  única vez e rejeitam uma rede fora da lista.
- [x] CA-GOX-CLI-003: o request GOX conserva o N12 de `SPE_AMOUNT` usado no
  GCX e contém método/índice de PIN escolhidos pelo operador.
- [x] CA-GOX-CLI-004: método MK/WK exige WKENC hexadecimal de 8 bytes para DES
  ou 16 bytes para TDES; DUKPT não aceita WKENC.
- [x] CA-GOX-CLI-005: o teste compara byte a byte o payload mínimo resultante e
  comprova que entradas inválidas não chegam ao serviço.
