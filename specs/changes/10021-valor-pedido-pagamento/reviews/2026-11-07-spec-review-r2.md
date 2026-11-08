# REV-002 — tarifa sobre valor base bloqueado

Decisão: `SPEC_APROVADA`.

O usuário identificou regressão: a revisão anterior interpretou incorretamente
o valor fixo como total sem tarifa. A correção mantém a base bloqueada e
restaura explicitamente as fórmulas da 10004, o arredondamento, o limite e o
congelamento do total. Exemplos verificáveis e testes de modalidade, parcelas,
captura e limite estão especificados. O retorno compartilhado não informa
tarifa/total; não alegar reconciliação financeira ou venda real.
