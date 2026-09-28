# IMP-REV-001 — Plugin de saque por cartão

## Decisão

`IMPLEMENTACAO_APROVADA`

## Evidências

- O módulo depende de `:shared-api` somente por `compileOnly` e não referencia `:app`.
- O manifesto e o descritor de serviço identificam `PluginSaqueCartaoApp`; não existe Activity `MAIN/LAUNCHER`.
- O plugin declara a folha e o caminho `Principal > Outros Serviços > Saque Cartão`.
- A senha fica na composição, é mascarada, é limpa antes da conclusão e não atravessa o roteador.
- A conclusão pede `menu-principal` ao `IPluginRouter`.
- Testes JVM, lint, assemble, teste instrumentado do host e execução manual no emulador passaram.

Não foram encontradas divergências materiais ou escopo indevido.
