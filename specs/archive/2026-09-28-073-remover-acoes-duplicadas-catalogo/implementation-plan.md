# Plano de implementação — 073

## Estratégia

Adicionar uma coleção derivada para apresentação, excluindo somente
`CatalogAction.OPEN` e `CatalogAction.CLOSE`, e fazer `DiagnosticScreenContent`
iterar essa coleção. O enum e o repository permanecem compatíveis.

## Testes

- atualizar `CatalogActionTest` para verificar 26 ações visíveis, primeira ação
  `STATE`, ausência de OPEN/CLOSE e preservação da ordem relativa;
- adicionar teste Compose/instrumentado que encontre exatamente um `Abrir` e
  um `Fechar` e não encontre `Abrir conexão`/`Fechar conexão`;
- executar validador Android, testes JVM, lint, assemble e instrumentação no
  emulador.

## Riscos

O risco é filtrar também os callbacks rápidos. A implementação deve manter
`onOpen`/`onClose` fora da coleção filtrada e os testes devem comprovar a
presença dos controles superiores.
