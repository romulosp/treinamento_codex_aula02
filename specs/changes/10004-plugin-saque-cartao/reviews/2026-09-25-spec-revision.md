# REV-002 — Correção do modo de execução do saque

## Decisão

`SPEC_APROVADA`

## Correção

O launcher de depuração violava o microkernel por permitir abrir o saque sem autenticação, discovery e clique do operador. A SPEC foi corrigida para proibir Activity `MAIN/LAUNCHER` em todas as variantes e exigir este fluxo:

`app` → login → menu dinâmico fornecido pelos plugins → clique em `Saque Cartão` → `createBusinessScreen` → retorno pelo `IPluginRouter`.

## Verificação

O emulador confirmou o fluxo completo e a ausência de acesso direto ao módulo de plugin.
