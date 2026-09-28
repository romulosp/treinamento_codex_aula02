# Validation — 067 Android Emulator Transport Bridge

## Status

`VALIDADA`

Esta validação registra evidências da análise, da implementação técnica e dos
limites ainda não executados. Não há AAR, teste Kotlin ou teste funcional
Android a declarar como concluído.

## Ambiente

- Sistema: Windows `windows/386`.
- Diretório do módulo: `apps/desktop/libpinpadabecsgo`.
- Go: `go1.26.5`.
- Data: 2026-09-27.
- Ausentes: `gomobile`, Java, Android SDK, ADB e Gradle.

## Evidências executadas

| Comando | Código | Resultado |
|---|---:|---|
| `go test ./...` | 0 | Baseline Go existente aprovado. |
| `go vet ./...` | 0 | Baseline Go existente aprovado. |
| `GOOS=android GOARCH=arm64 go list -deps ./...` | 0 | Grafo atual lista para Android; não prova `gomobile bind`. |
| busca inicial de `diagnosticopinpad` em `apps/frontend` | 0 | Evidência histórica da análise da 067; o projeto Android foi criado incrementalmente na Change 068. |
| busca de interfaces/transporte/configuração | 0 | Encontrados `service.SerialPort`, `serial.Adapter`, `PORTA_PINPAD` e defaults da Change 066. |

## Evidências da implementação

| Comando | Código | Resultado |
|---|---:|---|
| `gofmt` nos arquivos novos e adaptados | 0 | Formatação aplicada. |
| `go test ./...` | 0 | Pacotes existentes e novos aprovados. |
| `go test -tags=integration ./...` | 0 | Bridge real em loopback com transporte serial roteirizado e fluxo `Open → GetInfo → Close` aprovados. |
| `go vet ./...` | 0 | Nenhum diagnóstico. |
| `go build ./...` | 0 | Build dos pacotes desktop, Bridge e fachada concluído. |
| `go test -race ./...` | 1 | Limitação objetiva: `-race is not supported on windows/386`. |
| `GOOS=android GOARCH=arm64 go list ./...` | 0 | Pacote `mobile` e dependências listam para Android; não prova AAR. |
| `git diff --check` | 0 | Nenhum erro de whitespace nos artefatos da Change. |
| `go test ./... -coverprofile=coverage.out` | 0 | Testes aprovados; cobertura total registrada em 82,0%. |
| `go tool cover -func coverage.out` | 0 | Cobertura total de 82,0% das instruções. |
| testes de `BridgeEnvelope` | 0 | Encode/decode, fragmentação e limites aprovados. |
| teste Bridge → fake serial | 0 | Payload encaminhado byte a byte sem interpretação. |
| teste `PING/PONG` pré-`ACQUIRE` | 0 | Conectividade TCP não adquire nem disputa a porta COM. |
| teste de correlação `DATA` | 0 | O `correlationId` do comando é preservado no retorno do Bridge. |
| teste de configuração `PORTA_PINPAD` | 0 | Variável de ambiente tem precedência, valores vazios/espaços são rejeitados e o Bridge usa `config.Load()`. |

Arquivos principais implementados: `internal/application/port`,
`internal/infrastructure/bridgeprotocol`, `internal/infrastructure/transport/emulator`,
`internal/infrastructure/bridge`, `internal/infrastructure/ownership`,
`cmd/libpinpadabecsgo-bridge` e `mobile`.

## Validação sucessora

As limitações registradas na validação histórica foram encerradas por evidência sucessora, sem reescrever o ambiente original. A Change 068 criou o aplicativo Android e o AAR; a Change 069 validou o laboratório funcional; e a Change 072 comprovou o fluxo físico com `PORTA_PINPAD=COM10`, incluindo `PING`, `Open`, `GIX`, `DSP`, `Close`, reconexão e crescimento do log compartilhado `LogPinpadAbecs.txt`.

- [Change 068 arquivada](../../archive/2026-09-27-068-diagnosticopinpad/validation.md)
- [Change 069 arquivada](../../archive/2026-09-27-069-diagnosticopinpad-functional-lab/validation.md)
- [Change 072 arquivada](../../archive/2026-09-27-072-corrigir-abertura-bridge-android/validation.md)

## Limitações históricas

- O Gate 1 não pode ser executado neste ambiente por falta do toolchain mobile.
- A compatibilidade real de `go.bug.st/serial` não será presumida; o pacote
  bindado deverá excluir o adaptador físico Windows.
- A porta COM e o pinpad físico não participam da validação desta Change.
- A validação do aplicativo, AAR importado, lifecycle Android e funcionalidade
  com pinpad será registrada na Change 068.

## Estado da implementação

`VALIDADA`. A implementação e os testes técnicos aplicáveis foram concluídos e os gates originalmente indisponíveis foram comprovados por suas Changes sucessoras. A aprovação formal desta Change está registrada em `reviews/2026-09-28-approval.md`.

## Evidências previstas após aprovação

- `VAL-001`: envelope inteiro, fragmentado, agregado, truncado e inválido.
- `VAL-002`: partial read/write e preservação byte a byte.
- `VAL-003`: timeout, cancelamento e desconexão sem goroutine/lock vazado.
- `VAL-004`: ownership concorrente entre dois processos e retorno `BUSY`.
- `VAL-005`: `localhost`, `PINPAD_BRIDGE_HOST`, `PINPAD_BRIDGE_PORT`,
  `adb reverse` e override `10.0.2.2`.
- `VAL-006`: `gomobile bind`, importação do AAR e chamada mínima Kotlin → Go.
- `VAL-007`: Bridge com fake serial, sem alteração do payload ABECS.
- `VAL-008`: testes Go, vet, cobertura mínima de 80% do código novo aplicável
  e auditoria de segurança.
