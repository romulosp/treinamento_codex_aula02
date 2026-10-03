# SPEC — 067 Android Emulator Transport Bridge

Autor: Rômulo Penha

## Status

`SPEC_APROVADA`

## 1. Referências e baseline

Esta Change deriva da especificação recebida em:

`D:\desenvolvimento\ia\estudo\pinpad-abecs\spec_transporte_android.md`

O desenho foi decomposto em duas Changes: esta fundação de transporte e a
Change 068, que conterá o aplicativo Android Functional Lab.

Baseline confirmado no repositório:

| Evidência | Resultado |
|---|---|
| Core Go | `apps/desktop/libpinpadabecsgo` existente |
| Serviço | `internal/application/service.PinpadService` existente |
| Transporte atual | `internal/application/service.SerialPort` e `internal/infrastructure/serial.Adapter` |
| Driver físico | `go.bug.st/serial v1.6.2` |
| Configuração serial | `PORTA_PINPAD`, `PINPAD_BAUDRATE`, `PINPAD_TIMEOUT`; porta efetiva lida do ambiente |
| REST | existente em `cmd/libpinpadabecsgo-api`; não será usado como fronteira principal |
| Android app | inexistente |
| `go test ./...` | código 0 nesta revisão |
| `go vet ./...` | código 0 nesta revisão |
| `GOOS=android GOARCH=arm64 go list -deps ./...` | código 0; não substitui `gomobile bind` |
| Toolchain mobile | `gomobile`, Java, ADB e Gradle ausentes neste ambiente |

## 2. Invariantes

1. O core Go continua sendo a única fonte de verdade do protocolo ABECS.
2. O Bridge transporta bytes ABECS opacos e não interpreta seu significado.
3. O core não conhece TCP, COM, `localhost`, `10.0.2.2` ou Android.
4. Nenhuma operação em andamento será reenviada automaticamente após
   desconexão ou resultado indeterminado.
5. A COM terá um único proprietário por sessão de Bridge.
6. O default global de host continuará sendo `localhost`.
7. Nenhum segredo, PAN, trilha, PIN block, KSN ou EMV sensível será registrado.
8. O aplicativo Android não será introduzido nesta Change.

## 3. Requisitos funcionais

### RF-001 — Porta de transporte

Criar um contrato em camada de aplicação, proposto como
`internal/application/port.Transport`, com as operações:

```go
Open() error
Close() error
Read(context.Context) ([]byte, error)
Write([]byte) error
IsOpen() bool
```

O contrato representa um fluxo bidirecional de bytes e ciclo de vida. Não deve
conter tipos de `go.bug.st/serial`, `net.Conn`, Android, ABECS ou REST.

`PinpadService` e os adaptadores serial/Emulator deverão depender desse
contrato, preservando o comportamento da implementação serial existente.

### RF-002 — Windows Serial Transport

O adaptador serial existente continuará sendo o único componente que conhece
`go.bug.st/serial`, baud rate e COM. A extração para `Transport` não poderá
alterar bytes, timeouts, redaction, cancelamento ou o protocolo ABECS já
validado na Change 066.

### RF-003 — EmulatorTransport

Implementar um adaptador que mantenha uma conexão TCP persistente com o Bridge.
Ele deverá:

- abrir e negociar a sessão do Bridge;
- transportar dados `DATA` nos dois sentidos;
- aceitar partial read e partial write;
- aplicar timeout e cancelamento por `context.Context` internamente;
- preservar a ordem dos bytes;
- rejeitar frame inválido antes de alocar payload fora do limite;
- retornar erros controlados para timeout, cancelamento e desconexão;
- não reenviar automaticamente dados de uma operação interrompida.

### RF-004 — Bridge Windows

Criar um processo/entrypoint explícito para o Bridge, por exemplo
`cmd/libpinpadabecsgo-bridge`, iniciado em foreground e encerrado por
cancelamento do processo. O Bridge deverá:

- escutar somente em `127.0.0.1` por padrão;
- aceitar uma sessão por vez para a COM configurada;
- adquirir ownership antes de abrir a serial;
- liberar ownership em `Close`, erro fatal ou desconexão;
- encaminhar bytes sem modificar seu conteúdo;
- devolver erros de infraestrutura como `BUSY`, `SERIAL_UNAVAILABLE`,
  `TIMEOUT`, `CANCELED` ou `DISCONNECTED`;
- não expor API REST, WebSocket, comando hexadecimal ou regra ABECS.

### RF-005 — Envelope do Bridge

Quando o transporte for TCP, cada frame deverá usar o envelope binário abaixo,
em ordem big-endian:

```text
magic             4 bytes: PBRG
version           1 byte:  1
messageType       1 byte
flags             2 bytes
correlationLen    2 bytes, máximo 128
payloadLen        4 bytes, máximo 1 MiB
correlationId     N bytes UTF-8
payload           M bytes
```

`payload` de `DATA` é opaco e contém somente bytes do transporte ABECS. Frames
de controle usam tipos próprios (`HELLO`, `HELLO_OK`, `ACQUIRE`, `ACQUIRE_OK`,
`RELEASE`, `PING`, `PONG`, `ERROR` e `CLOSE`). O envelope não é um protocolo
ABECS e não recalcula CRC ABECS.

O encoder deverá escrever integralmente mesmo em partial write. O decoder
deverá usar leitura integral, validar magic, versão, limites, tipo e
`correlationId`, e rejeitar truncamento ou tamanho inválido sem aguardar
indefinidamente.

### RF-006 — Ownership da COM

O lock será de sessão, desde `ACQUIRE_OK` até `RELEASE` ou falha da sessão;
não será um mutex independente por `Read` ou `Write`. O lock deverá funcionar
entre processos Windows para impedir concorrência entre CLI/API/Bridge.

O mecanismo concreto será um adaptador de ownership isolado, com mutex nomeado
por porta no Windows e fake determinístico nos testes. A tentativa concorrente
deverá retornar `BUSY` sem abrir ou escrever na porta.

### RF-007 — Configuração

Os contratos desta Change serão:

| Configuração | Default | Regra |
|---|---:|---|
| `PINPAD_BRIDGE_HOST` | `localhost` | override explícito do cliente; vazio explícito é inválido |
| `PINPAD_BRIDGE_PORT` | `39100` | inteiro entre 1 e 65535 |
| bind do Bridge | `127.0.0.1` | não aceitar `0.0.0.0` por padrão |
| porta serial física | `PORTA_PINPAD`; fallback da configuração 066 | o processo Windows do Bridge consome `PORTA_PINPAD` por `config.Load()` e usa o valor efetivo no adaptador e no ownership |

O host efetivo e sua origem (`default`, ambiente ou BuildConfig futuro) devem
ser observáveis sem registrar dados sensíveis. A variável do Windows não será
presumida como variável do processo Android.

`PORTA_PINPAD` é uma variável do processo Windows que inicia o Bridge. O
aplicativo Android não deve tentar ler essa variável do próprio processo nem
receber uma porta COM na tela: ele consome a configuração indiretamente ao
executar `Open → GetInfo → Close` contra o Bridge. Assim, a mesma porta efetiva
é usada tanto pelo adaptador serial quanto pelo lock de ownership.

O fluxo padrão do Emulator será:

```text
adb reverse tcp:39100 tcp:39100
Android → localhost:39100
Bridge → 127.0.0.1:39100
```

`10.0.2.2` será aceito apenas como override específico do Android Emulator,
quando `adb reverse` não for utilizado. Ele nunca substituirá o default global.

### RF-008 — Mobile Facade

Criar um pacote público de binding, proposto como `mobile`, que encapsule os
pacotes `internal` e exponha somente tipos simples compatíveis com o Gate 1.
Nenhum pacote interno, `context.Context`, `slog`, `rsa.PrivateKey`, função,
canal, mapa ou entidade de domínio deverá cruzar a fronteira.

O contrato mínimo do spike deverá provar:

```go
type Client struct { /* estado interno */ }

func NewClient(host string, port int, timeoutMillis int64) (*Client, error)
func (c *Client) Open(operationID string) error
func (c *Client) Close(operationID string) error
func (c *Client) GetState() string
func (c *Client) Cancel(operationID string) error
```

As operações de negócio tipadas serão adicionadas somente após o Gate 1, com
assinaturas compostas de `string`, inteiros, `bool`, `[]byte`, structs simples
e `error`. O contrato não poderá criar um `SendRawCommand`.

Cada operação receberá `operationID`; a fachada manterá internamente o
`context.Context` e o cancelamento correspondente. O ID será propagado ao
Bridge e aos logs de metadados.

### RF-009 — Fronteira de erros e panic

Nenhum `panic` poderá atravessar a fachada. Cada método exportado deverá
converter falhas recuperáveis em erro controlado e preservar a causa no lado
Go. Falhas inesperadas deverão ser capturadas na borda, redigidas no log e
devolvidas como `BINDING_ERROR`.

### RF-010 — Lifecycle e cancelamento

- `Cancel(operationID)` cancela o contexto interno da operação.
- `Close` cancela operações, fecha a conexão e libera ownership.
- desconexão invalida a sessão e não libera o resultado como sucesso;
- a próxima operação exige `Open`/reconexão explícita;
- shutdown do Bridge fecha sockets, serial, locks e goroutines;
- nenhum teste poderá depender de sleep não controlado para provar o lifecycle.

### RF-011 — Observabilidade e segurança

Registrar, com `operationId`/`correlationId`, conexão, desconexão, duração,
timeout, cancelamento, bytes TX/RX, erro, reconnect, host efetivo, origem da
configuração, ownership e versão do Bridge Protocol. Não registrar payload
ABECS bruto por padrão. Qualquer modo de diagnóstico de bytes deverá ser
redigido e explicitamente habilitado apenas em ambiente local de teste.

O Bridge permanecerá restrito a loopback nesta Change. Acesso LAN, token,
autenticação remota e TLS ficam para uma Change de segurança distinta.

## 4. Viabilidade Go → Android

O caminho preferencial é `gomobile bind` gerando AAR para o pacote `mobile`.
O Gate 1 deverá provar, em ambiente com Android SDK/NDK/Java/ADB:

```text
package mobile → gomobile bind → AAR → Android Studio → Kotlin chama Go
```

A evidência local atual é apenas que `GOOS=android GOARCH=arm64 go list -deps`
foi concluído com código 0. Isso não prova binding, compilação AAR ou
execução no Emulator. A presença de `go.bug.st/serial` no grafo atual exige
que o pacote bindado seja isolado do adaptador serial Windows e use somente o
EmulatorTransport.

Se o Gate 1 falhar por dependência ou API incompatível, a implementação deve
parar. A alternativa será registrada em ADR: primeiro tentar isolar um
subconjunto mobile puro do core; somente uma Change posterior poderá aprovar
JNI/NDK manual. Não haverá fallback silencioso para REST ou para lógica ABECS
duplicada no Kotlin.

## 5. Matriz de comandos para a Change 068

Esta Change não cria o laboratório, mas fixa o baseline que o laboratório
deverá consumir após o Gate 1:

| Classificação | Comandos/fluxos derivados da Change 066 |
|---|---|
| Implementados no core e candidatos a teste | CAN, OPN, CLO, CLX, GIX, DSP, DEX, MNU, DSI, MLI, MLR, MLE, LMF, DMF, TLI, TLR, TLE, GKY, GTK, GOX, FCX, GPN, QR Code |
| Parciais/condicionados | GCX no subconjunto especificado; validação visual de DSI; validação física completa dos comandos |
| Stub ou fora do laboratório inicial | `TransactionGCX` completo (`ErrNotImplemented`); RST removido pela conformidade 066; comandos não implementados no código real |

A Change 068 deverá revalidar essa matriz contra o código no momento da
implementação, não apenas contra esta tabela.

## 6. Critérios de aceite

- **CA-001:** existe uma porta `Transport` sem dependência de driver, rede,
  Android ou ABECS no contrato.
- **CA-002:** `PinpadService` opera com o transport fake e com o serial sem
  regressão nos testes existentes.
- **CA-003:** `EmulatorTransport` mantém stream persistente, preserva bytes,
  suporta partial read/write e reporta disconnect/timeout/cancelamento.
- **CA-004:** o Bridge usa envelope `PBRG` versão 1 com limites e validações.
- **CA-005:** o Bridge não interpreta, recalcula ou altera bytes ABECS.
- **CA-006:** ownership de COM é exclusivo por sessão e conflito retorna
  `BUSY` controlado.
- **CA-007:** Bridge padrão escuta em loopback e não em `0.0.0.0`.
- **CA-008:** `PINPAD_BRIDGE_HOST` tem default `localhost`; `10.0.2.2` é apenas
  override explícito; a porta padrão é `39100`.
- **CA-009:** o fluxo `adb reverse` é documentado e testado sem modificar o
  default global.
- **CA-010:** a Mobile Facade usa apenas tipos suportados pelo binding e não
  expõe `context.Context` ou API de comando bruto.
- **CA-011:** cancelamento por `operationID` alcança o contexto Go e libera
  recursos.
- **CA-012:** nenhum panic atravessa a fronteira Go → Java/Kotlin.
- **CA-013:** logs possuem correlação, host/origem/ownership e redaction.
- **CA-014:** Gate 1 produz AAR mínimo importável e uma chamada Kotlin → Go,
  ou registra bloqueio formal com ADR antes de qualquer implementação completa.
- **CA-015:** testes unitários e de integração cobrem framing, fragmentação,
  agregação, truncamento, limites, ownership, cancelamento e desconexão sem
  hardware físico.
- **CA-016:** a validação física e o app `diagnosticopinpad` permanecem
  explicitamente como dependências da Change 068.

## 7. Dependências e riscos

- Android SDK/NDK, Java, ADB e `gomobile` precisam ser disponibilizados para o
  Gate 1.
- A dependência serial atual precisa ficar fora do pacote bindado.
- O lock entre processos Windows exige teste real de concorrência, não apenas
  mutex em memória.
- A desconexão durante GCX/GTK/GOX/FCX pode produzir resultado indeterminado;
  não é permitido retry automático.
- O laboratório da Change 068 dependerá do contrato final do AAR, mas não pode
  redefinir a fronteira do Bridge.

## 8. Complemento corretivo — Change 072

A [SPEC 072](../2026-09-27-072-corrigir-abertura-bridge-android/spec.md)
detalha RF-004/006/007/010/011: readiness após bind, preservação da configuração
Windows, erros correlacionados de sessão/ownership antes do Open serial,
afinidade de thread do mutex Windows, falha serial com peer ocioso e cleanup
antes da reabertura. Layout PBRG e separação Bridge/core permanecem.

O launcher físico exige PORTA_PINPAD herdada; o entrypoint direto mantém o
fallback de configuração desta SPEC. Preparação reverse não prova abertura
de COM nem OPN. Logs técnicos alimentam o mesmo arquivo da 070/072, sem payload
bruto desconhecido. Implementação e aceites desses complementos são
rastreáveis pelos CA-072-*; não se presume validação a partir do baseline acima.
