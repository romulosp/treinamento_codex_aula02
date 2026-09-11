# lib-pinpad-abecs-go

Biblioteca Go para comunicação serial com dispositivos compatíveis com ABECS 2.20.

## Escopo

Este módulo não possui REST, WebSocket, UI ou servidor. O executável em `cmd/libpinpadabecsgo` apenas valida a configuração local e serve como ponto de composição para testes manuais futuros.

## Configuração

- `PORTA_PINPAD`: opcional e prioritária; porta serial operacional, por exemplo `COM7` ou `/dev/ttyUSB0`. Quando ausente, usa `COM7`.
- `PINPAD_BAUDRATE`: opcional, padrão `19200`.
- `PINPAD_TIMEOUT`: opcional em segundos, padrão `30`.
- `LOG_LEVEL`: reservado para configuração futura do logger.
- `PINPAD_LOG_FILE`: habilita o rastro serial SPE/PP/RSP no arquivo indicado. O
  `start_aplication.bat` cria `logs/` e usa `logs/LogPinpadAbecs.txt` quando a
  variável estiver ausente ou vazia.

O rastro é desabilitado por padrão na biblioteca. O arquivo é criado em append
UTF-8, não deve ser versionado e só pode ser compartilhado após confirmar a
ausência de PAN, trilhas, PIN, PIN block, KSN, chaves e dados pessoais. GCX,
GTK, GOX, FCX e GPN são redigidos integralmente pelo tracer.

Cada retorno não vazio de `Adapter.Read` gera uma linha `PP`, inclusive ACK,
NAK, EOT e fragmentos de frame. Cada `Adapter.Write` confirmado gera uma linha
`SPE`; ambas informam o ponto de I/O e a data/hora, por exemplo:

```text
[COM7#001] SPE 16 47 49 58 30 30 30 17 12 34 CMD=GIX FUNC=serial.Adapter.Write DATA_HORA=2026-09-11T12:30:00-03:00
[COM7#001] PP  06 FUNC=serial.Adapter.Read DATA_HORA=2026-09-11T12:30:01-03:00
```

## Verificação

No diretório do módulo:

```text
go test ./...
go test ./... -coverprofile=coverage.out
go tool cover -func=coverage.out
go test -race ./...
go vet ./...
```
