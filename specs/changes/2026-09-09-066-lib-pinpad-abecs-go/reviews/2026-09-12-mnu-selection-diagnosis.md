# Diagnóstico da seleção MNU — 2026-09-12

Skills aplicadas: `golang-troubleshooting`, `spec-implement` e
`golang-testing`.

## Sintoma

O pinpad exibiu o menu e aceitou a confirmação, mas o CLI retornou
`invalid MNU selection` apesar de registrar `RSP CMD=MNU STATUS=000`.

## Evidência e causa

O rastro físico contém as respostas:

```text
MNU000006 80 4D 00 02 30 33
MNU000006 80 4D 00 02 30 31
```

O status `000` confirma o sucesso do comando. O campo TLV `0x804D`, com dois
bytes ASCII, informa respectivamente as seleções `03` e `01`.

O executável usado no teste havia sido compilado em 2026-09-12 17:25:54. O
parser com suporte ao retorno TLV foi salvo em 2026-09-12 17:29:17, portanto a
execução iniciada às 17:30:49 continuou usando a implementação anterior.

## Tratamento

Foi adicionado um teste de regressão na camada de serviço com o envelope físico
`MNU000006`, o TLV `804D`, três opções e seleção `03`. O teste comprova status
`000` e índice selecionado `3`. A suíte completa e o `go vet` foram aprovados,
e `bin/libpinpadabecsgo.exe` foi recompilado às 17:34:14.

Esta é uma evidência de implementação e diagnóstico. Não constitui revisão
independente, validação formal nem aprovação da Change.
