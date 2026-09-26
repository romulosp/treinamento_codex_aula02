# ADR-001: pasta dinâmica e fronteira de confiança

## Status

`ACEITO`

## Contexto

O host precisa descobrir APKs depois de instalado. Armazenamento app-specific
externo permite entrega controlada por `adb` no desenvolvimento, mas continua
gravável e não é origem segura para execução de código.

## Decisão

- Observar `<externalFilesDir>/plugins/inbox` como staging não confiável.
- Nunca carregar diretamente dessa pasta.
- Copiar para quarentena privada, validar e promover para repositório privado
  somente leitura.
- Exigir assinatura igual à do host para todos os builds.
- Usar `FileObserver` com `MOVED_TO`/`CLOSE_WRITE`, debounce e varredura no boot.
- Manter atualização de plugin ativo como `PENDING_RESTART`.
- Excluir a quarentena em falha, em vez de manter APK rejeitado.
- Recarregar no boot a revisão mais recente do repositório verificado.
- Conter falhas cooperativas com `ERROR`, `onDetach` e remoção da UI.

## Alternativas rejeitadas

- Asset no host: não oferece descoberta dinâmica.
- Carregar diretamente do staging: viola a fronteira de confiança.
- Polling contínuo: desperdiça recursos e ainda exige tratamento de lifecycle.
- Hot swap: não há descarregamento seguro das classes já usadas pelo processo.

## Consequências

O fluxo manual passa a ter dois APKs e uma etapa explícita de entrega. O host
ganha ativação dinâmica inicial, mas uma atualização de código ativo continua
dependendo de reinício controlado.
