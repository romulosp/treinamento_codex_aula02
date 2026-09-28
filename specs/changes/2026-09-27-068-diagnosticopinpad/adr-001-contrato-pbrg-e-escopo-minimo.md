# ADR-001 — Preservar PBRG e limitar o primeiro laboratório Android

## Contexto

O rascunho v7 da Change 068 propõe um protocolo length-prefixed JSON/base64 e
uma API genérica `Execute`. A Change 067, porém, já aprovou e implementou o
envelope binário `PBRG` v1 com payload raw e proibiu uma fachada de comando
arbitrário. A 067 também ainda precisa concluir seus gates formais.

## Decisão

1. preservar `PBRG` v1 como único protocolo Android ↔ Bridge;
2. usar JSON somente como representação de saída tipada de `GetInfoJSON`;
3. expor no app apenas versão, ping, abertura, `GetInfo`, fechamento e
   cancelamento;
4. adiar o catálogo completo de comandos para Change posterior;
5. condicionar a implementação Android à revisão e ao Gate 1 da Change 067;
6. manter `PORTA_PINPAD` como configuração do processo Windows do Bridge, que
   a aplica tanto ao transporte serial quanto ao ownership; o Android consome
   essa configuração somente de forma indireta pelo fluxo TCP.

## Alternativas rejeitadas

- **JSON/base64 no fio:** aumenta bytes, cria segundo protocolo e invalida a
  decisão aprovada na 067.
- **`Execute(operation, payloadJSON)`:** transforma a fronteira em executor
  genérico, enfraquece tipos e aproxima o Kotlin da lógica ABECS.
- **implementar toda a matriz ABECS na 068:** amplia a Change antes de provar
  AAR, lifecycle e transporte real.
- **REST como fallback:** desloca o core para o Windows e não prova o cenário
  em que o core Go roda no Android.

## Consequências

A Change 068 é pequena o bastante para diagnosticar a integração real e grande
o bastante para provar um comando tipado com pinpad. Novas ações exigirão
fachadas tipadas e critérios próprios em Change posterior.
