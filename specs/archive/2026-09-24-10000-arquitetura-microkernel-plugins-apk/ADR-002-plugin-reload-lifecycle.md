# ADR-002: Atualização de plugin ativo exige reinício

## Status

`PROPOSTO`

## Contexto

O requisito inicial menciona hot-load e hot-reload. Embora um novo artefato
ainda não carregado possa ser ativado durante a execução, classes, recursos e
referências já carregados não têm contrato seguro de descarregamento ou troca
no processo Android atual.

## Decisão

- Usar `FileObserver` com debounce e varredura no boot para detectar candidatos.
- Ativar sem reinício somente plugin/revisão ainda não carregada.
- Para atualização de plugin `ACTIVE`, validar e registrar a nova revisão como
  `PENDING_RESTART`.
- Fazer `onDetach()` apenas como limpeza cooperativa no término controlado; ele
  não representa descarregamento de classes.

## Consequências

O produto ganha previsibilidade e recuperação, ao custo de exigir reinício para
adotar atualização de componente em uso. A UI deverá comunicar esse estado
quando houver console administrativo futuro.
