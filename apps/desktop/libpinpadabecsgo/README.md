# lib-pinpad-abecs-go

Biblioteca Go para comunicação serial com dispositivos compatíveis com ABECS 2.12.

## Escopo

Este módulo não possui REST, WebSocket, UI ou servidor. O executável em `cmd/libpinpadabecsgo` apenas valida a configuração local e serve como ponto de composição para testes manuais futuros.

## Configuração

- `PORTA_PINPAD`: opcional e prioritária; porta serial operacional, por exemplo `COM7` ou `/dev/ttyUSB0`. Quando ausente, usa `COM7`.
- `PINPAD_BAUDRATE`: opcional, padrão `19200`.
- `PINPAD_TIMEOUT`: opcional em segundos, padrão `30`.
- `LOG_LEVEL`: reservado para configuração futura do logger.
- `PINPAD_LOG_FILE`: habilita o rastro serial SPE/PP/RSP no arquivo indicado.
  Caminhos relativos são resolvidos contra a raiz deste módulo, nunca contra o
  diretório de trabalho. `start_aplication.bat` e o `executar projeto.bat` da
  raiz criam `logs/` e definem o caminho absoluto
  `<raiz-do-módulo>/logs/LogPinpadAbecs.txt` quando a variável estiver vazia.

Antes de mostrar o menu, o executável imprime
`Log serial ativo: <caminho-absoluto>`. Esse é o único arquivo que deve ser
aberto para acompanhar a execução; um arquivo homônimo dentro de `cmd/` é um
artefato antigo e não é usado pela aplicação.

O rastro é desabilitado por padrão na biblioteca. O arquivo é criado em append
UTF-8, com uma linha de ativação imediatamente visível, não deve ser versionado
e só pode ser compartilhado após confirmar a
ausência de PAN, trilhas, PIN, PIN block, KSN, chaves e dados pessoais. OPN
seguro, MLR, TLR, GCX, GTK, GOX, FCX e GPN são redigidos integralmente pelo
tracer; bytes de controle isolados permanecem visíveis.

Cada retorno não vazio de `Adapter.Read` gera uma linha `PP`, inclusive ACK,
NAK, EOT e fragmentos de frame. Cada `Adapter.Write` confirmado gera uma linha
`SPE`; ambas informam o ponto de I/O e a data/hora, por exemplo:

```text
[COM7#001] SPE 16 47 49 58 30 30 30 17 12 34 CMD=GIX FUNC=serial.Adapter.Write DATA_HORA=2026-09-11T12:30:00-03:00
[COM7#001] PP  06 FUNC=serial.Adapter.Read DATA_HORA=2026-09-11T12:30:01-03:00
```

## Multimídia no menu local

- A opção 16 carrega o arquivo com `MLI/MLR/MLE`; o prazo de transferência
  começa depois que o caminho e o nome forem digitados. O nome A8 tem oito
  caracteres ASCII alfanuméricos.
- A opção 17 envia `DSI` para uma mídia já carregada. O status `000` confirma
  que o firmware aceitou o comando; o menu lembra que a imagem precisa ser
  confirmada visualmente no display.
- A opção 26 lista as mídias com `LMF`; a opção 27 exclui nomes com `DMF`
  (separe vários nomes por ponto e vírgula).
- O fluxo ABECS envia os bytes originais do arquivo; Base64 não é usado e
  aumentaria o volume em cerca de um terço. `MLI`/`DSI` não recebem largura ou
  altura. Consulte as dimensões e formatos informados por `GIX` (opção 4 ou
  13); o próprio pinpad valida suporte e dimensões ao finalizar/exibir.
- Se uma resposta expirar ou não corresponder ao comando, o serviço tenta
  ressincronizar com `CAN/EOT`. Depois de três tentativas sem `EOT`, ele fecha e
  abre a porta uma única vez, exige um novo CAN/EOT e executa OPN antes de
  liberar a fila. O comando que expirou não é reenviado automaticamente. Se a
  reconexão também falhar, feche e abra explicitamente a conexão.

## Verificação

No diretório do módulo:

```text
go test ./...
go test ./... -coverprofile=coverage.out
go tool cover -func=coverage.out
go test -race ./...
go vet ./...
```
