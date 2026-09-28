# ADR-001 — Separar transporte Android do Functional Lab

## Contexto

A especificação recebida descreve simultaneamente o Bridge Windows, a
portabilidade do core Go, o binding AAR e o aplicativo Android de diagnóstico.
O projeto usa gates Spec-Driven e o ambiente atual não possui toolchain Android.

## Decisão

Dividir a entrega em duas Changes:

- Change 067: contrato e implementação de transporte, Bridge, ownership,
  configuração, fachada Go e gates de viabilidade;
- Change 068: aplicativo `diagnosticopinpad`, integração do AAR aprovado,
  telas Compose e validação funcional física.

## Motivos

- reduz o raio de falha;
- permite aprovar o contrato sem inventar uma UI;
- torna o Gate 1 reproduzível antes do laboratório completo;
- preserva a regra de que o core Go é a fonte única de ABECS;
- mantém a validação física no contexto que realmente executará o laboratório.

## Consequências

O aceite completo do cenário Android exige as duas Changes. A Change 067 pode
ser implementada e validada sem hardware, mas não pode declarar o laboratório
funcional ou a comunicação física pelo Android como concluídos.
