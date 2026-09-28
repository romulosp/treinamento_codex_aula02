# SPEC — 071 BAT de teste do módulo Windows

## Status

`SPEC_APROVADA`

## Contrato

O módulo `apps/desktop/libpinpadabecsgo` deverá possuir um arquivo
`testar_bridge_pinpad.bat` que inicia `go run .\cmd\libpinpadabecsgo-bridge`
na raiz do módulo, remove `PINPAD_BRIDGE_TRANSPORT` no escopo `setlocal`,
mostra configuração/log efetivos e mantém a janela legível após encerramento.

## Correção de contrato pela Change 072

A atribuição fixa `set PORTA_PINPAD=COM14` da primeira versão é substituída
pelo consumo da variável herdada do Windows. COM14 é exemplo do ambiente do
operador. Ausência/vazio no launcher físico exige mensagem e código não zero.
O BAT preserva valores explícitos válidos e aplica defaults somente conforme
[RF-072-01](../2026-09-27-072-corrigir-abertura-bridge-android/spec.md).

A [Change 072](../2026-09-27-072-corrigir-abertura-bridge-android/proposal.md)
governa testes de launcher, preparação ADB, readiness e preservação do exit
code. Esta retificação documental não declara que o BAT atual já foi corrigido.
O status acima registra o gate original da 071; a evolução passa pelos gates
próprios da 072, sem aprovação de implementação implícita.

O BAT deve usar caminhos entre aspas e funcionar em diretório com espaços.
O entrypoint prepara o diretório pai do log, conforme 070/072. Não há segredos
no script nem encerramento automático de processos que ocupem a porta TCP.

## Segurança

- não registrar valores de cartão, PIN, chaves ou credenciais;
- deixar explícito que remover `PINPAD_BRIDGE_TRANSPORT` seleciona o transporte
  físico;
- manter o Bridge em loopback e o log no diretório documentado.
