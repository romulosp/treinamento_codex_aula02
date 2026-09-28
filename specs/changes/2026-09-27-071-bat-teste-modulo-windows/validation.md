# Validation — 071 BAT de teste do módulo Windows

## Status

`IMPLEMENTADA`

A implementação foi concluída para teste humano. A execução com pinpad físico
permanece manual e não foi simulada.

## Evidências da implementação

- BAT criado na raiz do módulo Go.
- Conteúdo confere `PORTA_PINPAD`, baudrate, timeout, porta Bridge, log padrão
  e remoção de `PINPAD_BRIDGE_TRANSPORT`.
- README atualizado com a distinção entre pinpad físico, scripted e Emulator.
- Nenhum segredo ou dado sensível foi incluído.

## Retificação após teste humano

A implementação existente sobrescreve PORTA_PINPAD com COM14 e não prova
readiness/reverse/exit code. As evidências acima são do contrato original e
não satisfazem o contrato retificado. A
[Change 072](../2026-09-27-072-corrigir-abertura-bridge-android/spec.md)
rastreia correção e nova validação; este registro não declara o BAT corrigido.
