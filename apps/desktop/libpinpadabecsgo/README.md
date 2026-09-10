# lib-pinpad-abecs-go

Biblioteca Go para comunicação serial com dispositivos compatíveis com ABECS 2.20.

## Escopo

Este módulo não possui REST, WebSocket, UI ou servidor. O executável em `cmd/libpinpadabecsgo` apenas valida a configuração local e serve como ponto de composição para testes manuais futuros.

## Configuração

- `PORTA_PINPAD`: porta serial operacional, por exemplo `COM3` ou `/dev/ttyUSB0`.
- `PINPAD_BAUDRATE`: opcional, padrão `19200`.
- `PINPAD_TIMEOUT`: opcional em segundos, padrão `30`.
- `LOG_LEVEL`: reservado para configuração futura do logger.

O carregamento operacional exige `PORTA_PINPAD`; `PinpadConfig` mantém `COM3` como default de modelo para compatibilidade com o legado.

## Verificação

No diretório do módulo:

```text
go test ./...
go test ./... -coverprofile=coverage.out
go tool cover -func=coverage.out
go test -race ./...
go vet ./...
```
