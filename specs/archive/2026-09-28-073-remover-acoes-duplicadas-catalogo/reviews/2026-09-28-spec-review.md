# Revisão da SPEC — 073 Remover ações duplicadas do catálogo Android

## Estado

`SPEC_APROVADA`

## Verificações

- O problema é observável na tela e reproduzível pela composição de
  `DiagnosticScreen` com `CatalogAction.values()`.
- O escopo distingue corretamente os botões rápidos superiores das ações
  internas do catálogo.
- A solução não altera binding, Bridge, protocolo, estado de sessão ou
  configuração `PORTA_PINPAD`.
- Os critérios cobrem ausência dos dois rótulos duplicados, ordem das ações,
  preservação dos callbacks, acessibilidade, build e testes.
- O teste instrumentado é necessário para confirmar a tela real; o teste JVM
  cobre a coleção de ações visíveis.

## Achados

Nenhum `REV-073-*` bloqueante ou importante foi encontrado. A decisão de
manter `CatalogAction.OPEN` e `CatalogAction.CLOSE` no enum para preservar o
contrato interno, filtrando somente a apresentação, está explícita no DESIGN.

## Decisão

`SPEC_APROVADA`. A implementação pode começar conforme `DESIGN.md` e as tasks.
