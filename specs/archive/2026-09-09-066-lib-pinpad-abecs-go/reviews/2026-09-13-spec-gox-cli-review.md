# Revisão de SPEC — continuação GOX no utilitário local

**Data:** 2026-09-13

**Estado de entrada:** `EM_REVISAO_SPEC`

## Evidência

O rastro físico registra GCX `000`, cartão `03`, seguido do envio GOX com os
valores fixos adquirente `01`, método DUKPT TDES `3` e chave `01`; o pinpad
respondeu `011` (`ST_INVPARM`). O menu também omitia `SPE_AMOUNT`, que no GOX
assume zero conforme a página 136 do manual.

A seção 3.7.3 torna `SPE_ACQREF`, `SPE_MTHDPIN` e `SPE_KEYIDX` obrigatórios. A
seção 3.7.1 define `PP_AIDTABINFO` como entradas N6 contendo a rede
credenciadora realmente apta ao cartão. Portanto, a correção deve derivar a
rede dessa resposta, conservar o valor da transação e solicitar os dados de
PIN em vez de inventá-los.

Os critérios especificam origem, formato, escolhas e rejeições de maneira
determinística e preservam o builder já validado pelo vetor da página 139.

## Decisão

`SPEC_APROVADA`
