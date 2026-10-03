# Evidências de implementação — 074 Pinpad Android Bridge Stack

## Status

`IMPLEMENTADA — TESTE HUMANO PENDENTE`

Este documento registra somente a execução técnica da implementação. Revisão da
implementação, validação formal e aprovação permanecem pendentes.

## Alterações realizadas

- `PORTA_PINPAD` tornou-se obrigatória em `config.Load()`.
- `DefaultConfig()` deixou de definir qualquer porta COM.
- `start_aplication.bat` passou a falhar quando `PORTA_PINPAD` não está
  definida, sem atribuir `COM7` ou outra porta fixa.
- Testes de configuração foram atualizados para cobrir ausência, vazio,
  espaços e valor fornecido pelo ambiente.
- O fechamento do `EmulatorTransport` passou a aceitar EOF/closed-pipe de
  Bridge legado após `RELEASE`, preservando a semântica de fechamento normal.
- As rotas REST legadas de reset passaram a apontar para o handler CAN/EOT;
  nenhuma delas serializa o comando RST.
- README do módulo foi alinhado à regra de configuração por ambiente.

## Comandos executados

Ambiente Go: `go1.26.5 windows/386`.

| Comando | Resultado |
|---|---|
| `go test ./...` | PASS, código 0 |
| `go vet ./...` | PASS, código 0 |
| `go test -tags=integration ./...` | PASS, código 0 |
| `go test ./internal/infrastructure/bridge -run TestBridgeForwardsDataToFakeSerial -count=10` | PASS 10/10, código 0 |
| `go test -race ./...` | Não suportado em `windows/386`, código 1 |
| `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/diagnosticopinpad` | Falhou por `local.properties` preexistente, código 1 |
| `gradlew.bat :app:assembleDebug --no-daemon` | Não iniciou: `java` não disponível |

Teste físico, geração/validação de AAR/APK e revisão da implementação não foram
declarados como concluídos.

## Correção de conectividade Android — 2026-09-29

- Causa confirmada: o app usava `localhost:39100` sem mapeamento `adb reverse`;
  o Bridge permanecia saudável em `127.0.0.1:39100` e não recebia conexão.
- O laboratório Android passou a usar `10.0.2.2` como host padrão do Emulator;
  `localhost` continua disponível quando houver reverse confirmado.
- `DiagnosticUiState` passou a consumir os defaults do `BuildConfig`, evitando
  divergência entre build e estado inicial da tela.
- `:app:testDebugUnitTest`, `:app:lintDebug` e `:app:assembleDebug` passaram com
  código 0 usando o JBR local.
- O APK debug foi instalado no `emulator-5554`.
- Com `adb reverse` removido, Ping retornou `Bridge: alcançável`; o log registrou
  `hello_ok` e `ping_ok`.
- Abrir concluiu com `Sessão: OPEN`; o log registrou `ownership_acquired`,
  `open(COM10,19200,8,N,1)=>OK`, `session_acquired` e resposta ABECS ao OPN.
