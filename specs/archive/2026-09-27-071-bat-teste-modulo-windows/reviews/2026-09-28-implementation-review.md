# Revisão da implementação — 071

## Estado

`IMPLEMENTACAO_APROVADA`

## Evidências

O BAT e o README foram implementados. A primeira versão continha a sobrescrita
de COM14, mas essa divergência foi corrigida e validada pela Change 072, que
passou a governar a implementação efetiva compartilhada.

Foram confirmados os cinco critérios da 071: consumo de `PORTA_PINPAD`,
defaults de baudrate/timeout, remoção local de `PINPAD_BRIDGE_TRANSPORT`,
execução na raiz do módulo e distinção entre físico e Emulator.

## Decisão

`IMPLEMENTACAO_APROVADA`, por evidência técnica e física da Change 072.
