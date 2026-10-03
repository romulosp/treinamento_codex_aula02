# SPEC — 074 Pinpad Android Bridge Stack

## Status

`SPEC_APROVADA`

Esta SPEC consolida `066`, `067`, `070`, `071` e `072`. O aditivo documental da
071 foi aprovado pelo usuário em 2026-09-30 para implementação e teste humano.
A aprovação não substitui revisão da implementação nem validação formal.

A re-revisão de 2026-09-30 confirmou que a 074 supersede deliberadamente o
fallback do entrypoint direto descrito na 072. A decisão mais estrita —
`PORTA_PINPAD` obrigatória em todo processo físico — foi aprovada pelo usuário
em `reviews/2026-09-29-approval.md` e é normativa nesta baseline.

## 1. Objetivo e contrato de reconstrução

Uma implementação feita somente a partir desta Change deve construir o caminho:

```text
Android/Compose
    → fachada gomobile
    → AAR
    → EmulatorTransport TCP
    → envelope PBRG v1
    → Bridge Windows loopback
    → ownership da COM
    → Transport serial
    → framing/protocolo ABECS v2.12
    → Pinpad físico
```

O core Go é a fonte única de verdade para comandos ABECS, framing, CRC, parser,
estados e erros de hardware. Kotlin não monta frames, calcula CRC, interpreta
TLV ABECS nem implementa comandos duplicados.

REST local é um adaptador adicional do mesmo core. Não é a fronteira Android e
não participa do caminho PBRG.

## 2. Identificação, perfil e estrutura

- Módulo Go: `br.com.romulopenha/lib-pinpad-abecs-go`.
- Diretório: `apps/desktop/libpinpadabecsgo/`.
- Executáveis: `cmd/libpinpadabecsgo`, `cmd/libpinpadabecsgo-api` e
  `cmd/libpinpadabecsgo-bridge`.
- Fachada binding: pacote Go `mobile`.
- Laboratório Android consumidor: `apps/frontend/smartphone/diagnosticopinpad/`.
- Perfil: `STANDARD` para a biblioteca e `HIGH_ASSURANCE` nos fluxos de dados
  de cartão, PIN, chaves e comunicação segura.
- Go deve operar sem CGO no módulo; o adaptador serial usa
  `go.bug.st/serial` somente na infraestrutura.
- Android usa Java 17/Kotlin/Compose conforme o projeto consumidor e recebe a
  versão de SDK/toolchain decidida pelo projeto Android no momento da
  implementação; esta SPEC não inventa uma atualização de toolchain.

Estrutura lógica mínima:

```text
apps/desktop/libpinpadabecsgo/
├── cmd/libpinpadabecsgo/
├── cmd/libpinpadabecsgo-api/
├── cmd/libpinpadabecsgo-bridge/
├── mobile/
├── internal/api/{dto}
├── internal/application/{port,service}
├── internal/domain/{command,error,model,parser,protocol,session,state}
├── internal/infrastructure/{bridge,bridgeprotocol,config,logging,ownership,serial,transport,worker}
└── internal/utilitario/{bytes,crc,tlv}
```

## 3. Invariantes

1. O Bridge não interpreta nem altera bytes ABECS.
2. O core não conhece TCP, COM, `localhost`, `10.0.2.2`, Android ou REST.
3. Uma instância possui um worker e uma operação de hardware por vez.
4. Uma COM só pode ter um owner entre processos Windows.
5. Desconexão, cancelamento ou resultado indeterminado não geram replay
   automático da operação.
6. `Ping` não abre COM, não adquire ownership e não envia ABECS.
7. `bridge_listening` não significa OPN aceito.
8. Nenhum PAN, trilha, PIN, PIN block, KSN, chave, IV, criptograma ou EMV
   sensível aparece em logs, erros públicos, métricas, fixtures ou JSON mobile.
9. O destino compartilhado do tracer é único, em append, e falha de persistência
   não pode produzir sucesso silencioso.
10. Reset de operação é CAN/EOT; RST não é comando ABECS da baseline.
11. Fakes e scripted demonstram componentes determinísticos, nunca comunicação
    física concluída.
12. A baseline não depende da execução ordenada das Changes arquivadas.

## 4. Configuração efetiva

### 4.1 Core e serial

Todo processo que possa abrir a serial deve ler e validar a configuração do
próprio ambiente. Nesta baseline, `PORTA_PINPAD` não possui default embutido:

- a variável é obrigatória no launcher físico e no entrypoint direto;
- ausência, vazio, somente espaços ou valor inválido é erro de configuração;
- nenhum launcher, `DefaultConfig`, `config.Load()` ou entrypoint escolhe COM
  fixa ou fallback quando a variável está ausente;
- o valor validado é a única porta usada pelo adaptador e pelo ownership;
- um launcher não sobrescreve valor válido herdado nem muda a porta de processo
  já iniciado;
- a origem é observável como `environment`.

Esta regra é uma supersessão aprovada do trecho da 072 que preservava fallback
no entrypoint direto; não deve ser atribuída à 072 como se já existisse nela.

As demais configurações seguem:

| Variável/campo | Default | Regra final |
|---|---:|---|
| `PORTA_PINPAD` | nenhum | obrigatória em todo processo físico; ausência, vazio ou inválido falha |
| `PINPAD_BAUDRATE` | `19200` | inteiro positivo; serial 8N1 |
| `PINPAD_TIMEOUT` | `30s` | inteiro positivo em segundos |
| `PINPAD_LOG_FILE` | raiz do módulo + `logs/LogPinpadAbecs.txt` | caminho não vazio é usado; destino final é absoluto |
| `LOG_LEVEL` | `INFO` | controla somente logging estruturado, sem ampliar dados registrados |

O BAT físico `testar_bridge_pinpad.bat` e o entrypoint direto nunca atribuem COM
fixa e falham antes da serial quando `PORTA_PINPAD` estiver ausente, vazia,
composta apenas por espaços ou inválida. Valores válidos de baudrate, timeout,
porta TCP e log preservam os defaults próprios.

### 4.1.1 Launcher de teste do módulo Windows

`apps/desktop/libpinpadabecsgo/testar_bridge_pinpad.bat` é a entrada manual
para testar o Bridge com pinpad físico. Deve:

1. executar no diretório do módulo Go, independentemente do diretório de onde
   foi chamado, e iniciar `go run .\cmd\libpinpadabecsgo-bridge`;
2. usar `setlocal`, herdar e validar `PORTA_PINPAD` antes da execução; ausência,
   vazio, somente espaços ou valor inválido encerram com mensagem e código não
   zero, sem escolher outra COM;
3. limpar `PINPAD_BRIDGE_TRANSPORT` apenas no escopo local para selecionar o
   transporte físico; não alterar permanentemente o ambiente do operador;
4. preservar valores explícitos válidos de baudrate, timeout, porta TCP e log;
   aplicar apenas os defaults desses campos definidos nesta SPEC;
5. mostrar a configuração efetiva e o caminho do log, sem dados sensíveis;
6. colocar caminhos entre aspas, inclusive em diretórios com espaços, e manter
   a janela legível após o término, propagando o código de saída real do Bridge;
7. tratar a preparação `adb reverse` como auxiliar opcional: falha ou ausência
   de emulador não impede o Bridge host de iniciar; a interface Android deve
   usar host/porta TCP adequados ao acesso efetivo;
8. não encerrar automaticamente o processo que ocupe a porta TCP e manter o
   listener do Bridge restrito ao loopback. O entrypoint do Bridge prepara o
   diretório pai do log antes da escrita.

Este launcher é distinto de `start_aplication.bat` e do modo scripted. Ele não
executa comandos ABECS automaticamente e não prova comunicação física apenas
por iniciar o listener, criar o log ou configurar ADB.

### 4.2 Bridge e Android

| Configuração | Default | Regra |
|---|---:|---|
| `PINPAD_BRIDGE_HOST` | `localhost` | host vazio explícito é inválido; `10.0.2.2` somente por override explícito |
| `PINPAD_BRIDGE_PORT` | `39100` | inteiro entre 1 e 65535 |
| bind do Bridge | `127.0.0.1` | `0.0.0.0` e LAN não são permitidos nesta Change |
| modo Bridge | `physical` | `scripted` só para testes controlados |

Com `adb reverse`, o fluxo é:

```text
adb reverse tcp:39100 tcp:39100
Android localhost:39100 → host 127.0.0.1:39100
```

O helper PowerShell deve localizar ADB no PATH ou SDK configurado, selecionar um
único emulador online ou exigir `ANDROID_SERIAL`/parâmetro explícito quando
houver vários, aplicar somente o reverse solicitado e confirmar o resultado por
`reverse --list`. Dispositivo físico não é selecionado automaticamente.

Falha de ADB, emulador ausente/offline ou seleção ambígua produz
`emulator_not_prepared` e orientação corretiva, sem impedir que o Bridge host
inicie para uso local. Reverse verificado não prova listener, COM ou OPN.

## 5. Protocolo ABECS v2.12

### 5.1 Framing e conversão

Implementar as constantes `SYN=0x16`, `ETB=0x17`, `ACK=0x06`,
`NAK=0x15`, `EOT=0x04`, `CAN=0x18` e `DC3=0x13`.

`CRC16CCITT` usa polinômio `0x1021`, inicial `0x0000` e transmissão
high-byte first. Substitution obrigatória:

```text
0x13 → 0x13 0x33
0x16 → 0x13 0x36
0x17 → 0x13 0x37
```

O pacote clássico é:

```text
SYN + PKTDATA_SUBSTITUÍDO + ETB + CRC_HIGH + CRC_LOW
```

O leitor deve aceitar leituras fragmentadas, frames agrupados e bytes
excedentes, preservando os bytes para o próximo frame. Deve rejeitar
truncamento, CRC inválido, comprimento inválido, resposta incompatível, NAK e
EOT conforme o erro tipado correspondente.

Comandos/respostas podem conter blocos com comprimento decimal N3; parâmetros
TLV permanecem íntegros. O limite clássico do `PKTDATA` é 1024 bytes, com os
limites específicos de cada comando prevalecendo quando menores.

### 5.2 ACK, NAK, timeout e recuperação

- Após cada envio, aguardar ACK/NAK por no máximo 2 segundos.
- NAK ou ausência de confirmação retransmite o mesmo pacote até três tentativas
  totais.
- CRC/framing inválido na resposta provoca NAK e nova leitura até três
  tentativas.
- Resposta válida não recebe ACK do host.
- Comando não bloqueante espera resposta final por no máximo 10 segundos.
- Comando bloqueante usa o contexto/timeout explícito do consumidor.
- Antes da comunicação, o host envia CAN isolado e aguarda EOT por 2 segundos,
  ignorando bytes diferentes de EOT, até três tentativas.
- Cancelamento de comando bloqueante executa CAN/EOT, mas preserva o erro de
  cancelamento como causa original.
- Timeout, resposta de outro comando ou falha de três frames corrompidos exige
  recuperação CAN/EOT antes de liberar o worker.
- Se CAN/EOT não recuperar após três tentativas, marcar
  `DESYNCHRONIZED`, fechar a porta, abrir novamente, executar CAN/EOT inicial e
  OPN uma vez. O comando interrompido não é reenviado.
- Falha da reconexão mantém a instância recusando comandos comuns até nova
  abertura explícita bem-sucedida.

## 6. Modelos, estado, sessão e fila

### 6.1 Estado do pinpad

O core implementa `CLOSED`, `OPEN`, `BUSY` e `DESYNCHRONIZED`. A
transição normal é `CLOSED → OPEN → BUSY → OPEN`; timeout, disconnect ou falha
de recuperação podem levar a `CLOSED` ou `DESYNCHRONIZED`. A UI não pode
declarar `OPEN` a partir de Ping, Version ou consulta local.

`DeviceInfo`, `DisplayCapabilities`, `Response`, `GCXResponse`, modelos
próprios de GTK/GOX/FCX, `BerTLV` e erros tipados devem preservar apenas os
campos definidos por suas tags e contratos. O parser não infere campo sensível
a partir de outro comando.

### 6.2 SessionManager

`SessionManager` deve fornecer `Claim`, `Renew`, `Release`,
`ForceRelease`, `IsOwner`, `HasOwner`, `CurrentOwner` e
`IsSessionExpired` de modo thread-safe.

- `SessionID` é textual e opaco.
- Somente um owner lógico existe por vez.
- Claim do mesmo owner é idempotente e renova atividade.
- Claim de outro owner retorna conflito/`ErrSessionAlreadyOwned`.
- Release só funciona para o owner atual; `ForceRelease` é administrativo.
- Expiração ocorre após 300 segundos de inatividade.
- Relógio é injetável nos testes.
- Sessão expirada não aparece como owner ativo.

### 6.3 CommandQueue

A fila é FIFO, thread-safe, capacidade 100 e possui um worker por instância.

- `Enqueue(ctx, cmd)` insere e retorna sem aguardar execução; fila cheia
  retorna `ErrQueueFull` imediatamente.
- `Submit(ctx, cmd)` chama a fila e aguarda resultado ou cancelamento; é a única
  entrada bloqueante.
- `Size`, `IsEmpty`, `Clear` e `Stop` são thread-safe.
- `Clear` cancela itens ainda não iniciados.
- `Stop` é idempotente, cancela o item ativo, drena a fila e garante que nenhum
  comando seja executado depois do retorno.
- Contextos alcançam serial, parser, Bridge e binding.

## 7. Fachada Go e ciclo de vida

`PinpadService` é o único orquestrador do core. Não expõe
`SendRawCommand`, hexadecimal arbitrário, mutex, tipos de driver ou estado
HTTP.

Operações mínimas:

`SetConfig`, `GetConfig`, `Open`, `OpenSecure`, `Close`, `Reset` via
CAN/EOT, `Shutdown`, `GetState`, `GetInfo`, `GetInfoRaw`,
`GetDisplayCapabilities`, display, multimídia, tabelas EMV, GCX, GTK, GOX,
FCX, GKY, GPN e `CLX` conforme a seção de catálogo.

`Open` abre a porta, executa handshake ABECS clássico ou seguro e só confirma
`OPEN` após OPN válido. Repetição de Open numa sessão aberta não envia OPN
duplicado. `Close` executa CLO quando aplicável, limpa KSEC e buffers e fecha a
porta; falhas de CLO e de fechamento não são ocultadas uma pela outra. `CLX`
é visual, não fecha a porta física, mas encerra a sessão segura no pinpad.

`Shutdown` cancela operações, encerra worker, fecha transportes e é idempotente.

## 8. Catálogo funcional ABECS

Todos os comandos passam pela fila, contexto, parser, estado, timeout e tracer.
Cada um deve ter builder, parser, modelo/resultado e teste byte a byte conforme
os limites do manual. A tabela define o contrato consolidado:

| Comando | Contrato final |
|---|---|
| `CAN` | Byte `0x18`, controle de cancelamento/reset; aguarda EOT; idempotente; não é API raw. |
| `OPN` | Payload clássico `OPN`, ACK e `OPN000`; abre 8N1 antes do envio; OPN seguro substitui o clássico quando selecionado. |
| `CLO` | `CLO032` + mensagem S32; confirma `CLO000`; encerra comunicação segura antes do fechamento físico. |
| `CLX` | Encerramento visual não bloqueante com mensagem e/ou mídia; resposta `RSP_ID=CLX`; não fecha porta, mas limpa KSEC no pinpad. |
| `GIX` | `GIX000`; parseia informações do dispositivo e capacidades `PP_MODEL`, `PP_MNNAME`, `PP_CAPAB`, `PP_DSPTXTSZ`, `PP_DSPGRSZ`, `PP_MFSUP`; ausência de tag mantém zero/false. |
| `DSP` | `DSP032` + duas linhas de 16 bytes; conversão Latin-1 e validação sem truncamento UTF-8 silencioso. |
| `DEX` | Mensagem de até 160 bytes e `DEX_MSGLEN` N3; valida tamanho/codificação. |
| `MNU` | Timeout X1, título até 128 bytes e 1–20 opções até 24 bytes; seleção em `PP_VALUE` N2; distingue seleção, cancelamento, timeout e status. |
| `DSI` | Exibe mídia A8 persistida; `DSI000` prova aceitação do firmware, não pixels; confirmação visual é evidência separada. |
| `MLI` | Nome A8, tamanho X4, CRC B2 e tipo B1 PNG/JPG/GIF ou RUF/`00h`; tipo desconhecido não é rejeitado no MLI por si só. |
| `MLR` | Blocos `SPE_DATAIN` em ordem, até 995 bytes cada, limite `MLR_MAX_BLOCK_SIZE`, progresso por bytes aceitos; sem replay implícito. |
| `MLE` | Payload `MLE`; só anuncia mídia após confirmação; falha/timeout mantém estado consistente. |
| `LMF` | Payload literal `LMF`; preserva zero ou vários `PP_MFNAME`, nomes A8 normalizados para maiúsculas; lista vazia é sucesso. |
| `DMF` | Um ou mais `SPE_MFNAME`; valida lista não vazia e nomes A8; nomes desconhecidos são tratados pelo pinpad sem erro local fabricado. |
| `TLI` | `TLI012 + ACQUIRER N2 + VERSION A10`; status `000` e `020` prosseguem para TLR/TLE; outros interrompem. |
| `TLR` | Registros com N3 e limite de pacote; preserva ordem; falha interrompe TLE sem expor registros. |
| `TLE` | Payload `TLE`; só marca tabela carregada após confirmação final. |
| `GKY` | Payload literal `GKY`; status `000`, `004..008` e `013` viram resultados tipados; timeout, cancelamento e tecla são distintos. |
| `GCX` | Valor N12, data AAMMDD, hora HHMMSS e opções N5; uma única iniciação; aceita notificações `NTM000`; parser usa somente tags retornadas por GCX. |
| `GTK` | Só após captura elegível; seleciona PAN/trilhas e método de dados; resposta própria com trilhas/KSN conforme autorização e redaction. |
| `GOX` | Só após GCX ICC/CTLS EMV; exige adquirente, método PIN e índice; resposta própria com decisão, PIN block/KSN/TLV quando aplicável. |
| `FCX` | Finaliza após GOX; `SPE_FCXOPT`, ARC condicional, EMV/tag list/timeout; resposta própria com `PP_FCXRES` e Issuer Script Results. |
| `GPN` | MK/WK ou DUKPT; valida método, índice, WKENC/PAN/mensagem; retorna PIN block/KSN binários autorizados, nunca em log. |
| `QRCODE` | Usa `QRCodeGenerator` injetado no core; tamanho 50–320, margem 0–10; x/y não suportados geram aviso estruturado; carga/exibição usa MLI/MLR/MLE/DSI. |
| `RST` | Não existe como comando na baseline. Qualquer alias externo deve chamar o caminho CAN/EOT sem payload RST. |

`TransactionGCX` com tipo completo, adquirente, AIDs, cashback, moeda,
máscara, EMV e tag list permanece um stub explícito `ErrNotImplemented` até
existir contrato normativo aprovado. `PurchaseGCX` do subconjunto
valor/data/hora/opções é o fluxo suportado.

## 9. Comunicação segura ABECS

- OPN seguro é enviado em claro no formato normativo aprovado.
- O perfil inicial é RSA 2048 bits, expoente 65537, com formato/padding
  exatamente confirmados no manual e dispositivo.
- `KSEC` é temporária, existe somente na sessão segura e é descartada ao fechar,
  falhar ou cancelar.
- Pacotes protegidos usam `DC2` + bloco AES-CBC conforme manual: comprimento,
  CRC do claro, dados e padding para múltiplo de 16, KSEC de 16 bytes e IV zero.
- Após OPN, comandos ficam protegidos; respostas de CLO e CLX permanecem claras.
- Nenhum material RSA/AES/KSEC/IV/criptograma aparece em tracer, slog, erro,
  métrica, JSON ou fixture.
- Falha criptográfica, timeout, cancelamento e NAK permanecem distinguíveis.
- O gate de hardware deve confirmar sequência, formato, padding, IV e limpeza.

## 10. Porta serial física

A porta neutra deve expor lifecycle, leitura contextual, escrita e estado:

```go
Open() error
Close() error
Read(context.Context) ([]byte, error)
Write([]byte) error
IsOpen() bool
```

O adaptador real é o único componente que conhece `go.bug.st/serial`,
baudrate, COM/TTY, 8N1 e timeout de leitura. Deve verificar contexto
antes/depois de cada leitura curta, retornar `context.Canceled`/
`context.DeadlineExceeded` sem convertê-los silenciosamente em timeout de
protocolo e preservar bytes excedentes.

Fakes determinísticos cobrem ACK, NAK, EOT, CRC, fragmentação, bytes agrupados,
timeout, erro de leitura/escrita, cancelamento e frames completos.

## 11. Bridge e envelope PBRG v1

### 11.1 Envelope

Cada frame TCP é big-endian:

```text
magic          4 bytes: PBRG
version        1 byte:  1
messageType    1 byte
flags          2 bytes
correlationLen 2 bytes, máximo 128
payloadLen     4 bytes, máximo 1 MiB
correlationId  N bytes UTF-8
payload        M bytes
```

Tipos: `HELLO`, `HELLO_OK`, `ACQUIRE`, `ACQUIRE_OK`, `RELEASE`,
`DATA`, `PING`, `PONG`, `ERROR` e `CLOSE`.

Encoder deve concluir partial writes. Decoder deve ler integralmente, validar
magic, versão, tipo, UTF-8, limites, truncamento e tamanho antes de alocar ou
encaminhar payload. `DATA` é opaco e não recalcula CRC ABECS.

### 11.2 Sessão Bridge

O Bridge:

- escuta somente em `127.0.0.1`;
- aceita uma sessão por vez para a COM configurada;
- recebe HELLO, responde HELLO_OK, recebe ACQUIRE e só então tenta ownership;
- adquire ownership antes de `Transport.Open()`;
- retorna `BUSY`, `OWNERSHIP_ERROR`, `SERIAL_UNAVAILABLE`, `TIMEOUT`,
  `CANCELED`, `DISCONNECTED` ou `PROTOCOL_ERROR` conforme a fase;
- encaminha DATA nos dois sentidos sem alteração;
- serializa writes completos no socket e na serial;
- libera ownership em RELEASE, CLOSE, disconnect, erro fatal ou shutdown;
- fecha socket, serial e lock, cancela workers e aguarda término antes da
  próxima sessão;
- não inicia replay depois de desconexão ou resultado indeterminado.

Falha fatal de `Transport.Read()` deve alcançar o supervisor mesmo quando o
peer está ocioso bloqueado em `ReadFrame`. Cleanup é idempotente e erros de
release/close não são descartados.

No Windows, mutex nomeado por porta deve respeitar afinidade de thread exigida
pela API. Abandono deve ser registrado como `ABANDONED`, não virar `BUSY`, e
deve terminar em falha controlada sem replay.

## 12. Fachada mobile e correlação

O pacote `mobile` é público para gomobile e cruza somente tipos simples,
strings, inteiros, booleanos, `[]byte` quando explicitamente seguro, structs
simples e `error`. Não cruza `context.Context`, `slog`,
`rsa.PrivateKey`, goroutines, canais, mapas internos ou entidades de domínio.

Contrato mínimo:

```go
NewClient(host string, port int, timeoutMillis int64) (*Client, error)
Open(operationID string) error
Close(operationID string) error
Ping(operationID string) error
GetState() string
Cancel(operationID string) error
```

A fachada pode expor operações tipadas do catálogo e resultados resumidos, sem
bytes raw ou dados sensíveis. Nenhum `panic` atravessa a fronteira; falhas
recuperáveis viram erro controlado e falhas inesperadas viram
`BINDING_ERROR`.

Cada operação exige `operationID` não vazio. O ID é usado para cancelamento,
metadados e correlação com o Bridge. Cada frame PBRG recebe `correlationId`.
O `correlationId` recebido pelo Bridge deve ser preservado no ERROR e no log;
falha anterior à recepção de ID não inventa ID Android.

`Open` de cliente fechado executa Ping controlado dentro do orçamento da ação,
depois abre transporte e sessão ABECS. `Close` cancela operações ativas,
espera o gate de operação, fecha serviço e transporte e é idempotente. Trocar
endpoint exige novo cliente ou fechamento explícito; não há retomada automática.

## 13. Estados e erros Android

A UI separa:

1. estado transitório da ação (`IDLE`, `RUNNING`, `SUCCESS`, `ERROR`);
2. conectividade do Bridge;
3. estado confirmado da sessão Go (`CLOSED`, `OPEN`, `BUSY` ou equivalente).

Categorias públicas estáveis:

| Fase | Categoria |
|---|---|
| conexão recusada | `BRIDGE_UNREACHABLE` |
| prazo excedido | `TIMEOUT` |
| handshake/frame | `PROTOCOL_ERROR` |
| ownership ocupado | `BUSY` |
| falha de lock | `OWNERSHIP_ERROR` |
| driver/open/read/write | `SERIAL_UNAVAILABLE` |
| cancelamento | `CANCELED` |
| socket/peer | `DISCONNECTED` |
| status ABECS | `PINPAD_ERROR` |
| comando sem sessão | `PINPAD_CLOSED` |
| falha não classificada | `BINDING_ERROR` |

Mensagem apresentada é limitada, segura e sem stack trace, caminho interno,
texto arbitrário de driver ou payload. Causa técnica saneada permanece no log
Windows. Erro secundário de cleanup não substitui a causa original.

Ping bem-sucedido mantém sessão `CLOSED`; Estado `CLOSED` desabilita comandos
físicos; Open falho não exibe OPEN; disconnect/timeout que invalida sessão
bloqueia comandos; Close limpa resultado anterior. Código, fase, mensagem
segura, duração e último `operationID` devem permanecer visíveis e acessíveis,
sem depender somente de cor.

## 14. Logging e observabilidade

O Bridge cria um único `logging.Tracer` antes de `Serve`, configura o destino
e o injeta no transporte serial físico ou scripted. A ordem do destino é:

1. `PINPAD_LOG_FILE` não vazio;
2. caminho absoluto `<raiz-do-módulo>/logs/LogPinpadAbecs.txt`.

O diretório pai é criado quando necessário, o arquivo usa append e a escrita é
serializada. Se o destino obrigatório não puder ser preparado, o Bridge falha
antes de aceitar sessões e informa a causa no stderr/console.

O tracer serial registra, quando aplicável, `open`, `close`, `SPE`, `PP` e
`RSP`. Para comandos não sensíveis, SPE deve refletir os bytes efetivamente
escritos e PP cada leitura física. Para comandos sensíveis, frames são redigidos
por completo; controles isolados podem permanecer visíveis.

Eventos Bridge mínimos:

`bridge_starting`, `bridge_listening`, `bridge_start_failed`,
`client_connected`, `hello_ok`, `ping_ok`, `acquire_requested`,
`ownership_acquired`, `ownership_failed`, `serial_open_failed`,
`session_acquired`, `session_error`, `session_released`,
`client_disconnected` e `bridge_stopped`.

Cada evento contém timestamp, componente/fase, resultado/código, porta/mode,
PID/endereço quando aplicável, `sessionId` quando criado e
`correlationId` quando recebido. Mensagens são limitadas, sanitizadas contra
controle e injeção de linhas. Criar arquivo ou imprimir configuração não
substitui evento real. Android pode manter logger privado apenas para
metadados; não cria um segundo arquivo ABECS Windows.

## 15. API REST local

O executável REST é adaptador independente em `127.0.0.1:8080` por default,
versionado em `/api/v1`, JSON, limites de corpo e timeouts de leitura/escrita/
headers. Handlers não acessam serial diretamente nem mantêm estado separado do
`PinpadService`.

Rotas canônicas incluem conexões OPN/CLO/CAN, GIX, DSP/DEX/MNU/CLX/DSI, mídia,
tabelas TLI/TLR/TLE, GCX, GTK, GOX, FCX, GKY, GPN e QR Code, cada uma com DTO
próprio. RST não é rota de comando ABECS: se a compatibilidade HTTP for
preservada, `/connections/current/resets` deve ser documentada e implementada
somente como alias de CAN/EOT.

Erros usam `ErrorBody` com `code`, `module`, mensagem segura, detalhes
redigidos e `correlationId`; JSON inválido/extra recebe 400, rota 404,
método 405, conflito 409, status ABECS 422, indisponibilidade 503, timeout 504
e falha inesperada 500.

## 16. Segurança, privacidade e documentação

- Não persistir PAN, trilhas completas, PIN, PIN block, KSN, chaves, IV,
  criptogramas, EMV sensível, conteúdo de arquivos ou credenciais.
- Redaction deve ocorrer antes de qualquer persistência, inclusive para chunks
  fragmentados/agregados e erros remotos.
- Bridge permanece loopback; segurança de acesso remoto é outra Change.
- Não usar strings vindas do peer como instruções ou mensagens livres ao usuário.
- Validar inputs, comprimentos, UTF-8/Latin-1, enumerações, limites e estados
  antes de serializar.
- Todo pacote/símbolo Go público, contrato de fronteira e fluxo complexo deve
  ter GoDoc em português, sem dados reais. Código Kotlin alterado deve ter KDoc
  conforme a skill Android aplicável.

## 17. Estratégia de testes

### 17.1 Unitários e determinísticos

Cobrir com tabelas e fakes:

- CRC, substitution, framing, blocos, partial read/write e bytes excedentes;
- parser ABECS, BER-TLV, tags obrigatórias/condicionais e truncamento;
- todos os builders/parsers do catálogo;
- estados, erros, SessionManager e relógio injetável;
- fila FIFO, capacidade 100, `Enqueue` não bloqueante, `Submit`, Clear e Stop;
- cancelamento serial contextual e ausência de goroutine presa;
- envelope PBRG, limites, UTF-8, tipos, truncamento e partial I/O;
- EmulatorTransport, Ping, Open/Close, correlation e erro sanitizado;
- Bridge com transport fake, falhas, cleanup, read fatal ocioso e writes
  concorrentes;
- logging único, append, eventos, redaction e injeção de linha;
- fachada mobile, operação duplicada, cancelamento, panic boundary e estados;
- REST com `httptest`, DTOs, rotas, método, limite e ErrorBody.

Meta: pelo menos 80% do código Go novo aplicável, com inventário e comando de
cobertura; meta recomendada 90%. O percentual nunca substitui os cenários de
negócio.

### 17.2 Processo e Android

- build e testes do Go em Windows e Linux sem CGO quando o ambiente permitir;
- `gomobile bind` do pacote `mobile`, geração de AAR, SHA-256 e proveniência;
- teste Kotlin chamando AAR, sem tipos proibidos ou comando raw;
- teste do ViewModel/reducer para `CLOSED`, Ping, Open, Close, Cancel,
  desconexão, timeout e erro visível;
- testes Compose/instrumentados para ações rápidas, catálogo, acessibilidade e
  preservação de estado;
- instalação do APK correspondente ao AAR gerado;
- Bridge scripted para fluxo `Ping → Open → GIX → DSP → Close → Open`;
- integração real Android Emulator → reverse → Bridge → transport fake.

### 17.3 Windows, físico e recuperação

- teste multiprocesso para exclusividade da COM, release por Close/disconnect,
  reabertura e mutex abandonado;
- launcher com ambiente herdado, ausente/vazio/inválido, caminhos com espaços,
  bind ocupado, log inválido e código de saída real;
- helper ADB com zero/um/vários emuladores, offline, seleção explícita e
  confirmação de reverse;
- pinpad físico para OPN/CLO, comandos aplicáveis, timeout, CAN/EOT,
  reconexão única, GTK/GOX/FCX/GPN e comunicação segura;
- DSI exige status ABECS e confirmação visual separada;
- nunca declarar hardware validado a partir de scripted, arquivo criado,
  listener ou `bridge_listening` isolados.

## 18. Critérios de aceite

- **CA-074-01:** módulo, package, estrutura e fronteiras core/REST/mobile/
  Bridge/serial estão implementados conforme esta SPEC.
- **CA-074-02:** framing ABECS, CRC, substitution, ACK/NAK, CAN/EOT, timeouts,
  parser e preservação de bytes excedentes passam os testes determinísticos.
- **CA-074-03:** catálogo final possui contratos próprios para todos os comandos
  da seção 8; RST não é serializado e `TransactionGCX` completo permanece
  explicitamente reservado.
- **CA-074-04:** lifecycle, estados, SessionManager, fila, cancelamento,
  shutdown, DESYNCHRONIZED e recuperação sem replay passam testes.
- **CA-074-05:** Transport neutro permite serial real, fake e EmulatorTransport
  sem dependência de driver/TCP/Android no domínio.
- **CA-074-06:** PBRG v1 valida magic/versão/tipos/limites/correlação, suporta
  partial I/O e não interpreta bytes ABECS.
- **CA-074-07:** Bridge fica em loopback, adquire ownership antes da COM,
  libera recursos em todos os finais e aguarda workers antes da reabertura.
- **CA-074-08:** mutex Windows respeita afinidade de thread e abandono não vira
  falso BUSY nem gera replay.
- **CA-074-09:** todo processo físico exige `PORTA_PINPAD` do ambiente, não
  possui COM fixa/fallback, rejeita ausência/vazio/inválido e usa o mesmo valor
  validado no serial e no ownership, com origem observável.
- **CA-074-10:** Ping não abre COM nem altera estado ABECS; Open só confirma
  OPEN após OPN; Close/Cancel/Disconnect deixam estado fiel.
- **CA-074-11:** fachada mobile gera AAR consumível, usa apenas tipos simples,
  exige `operationID`, propaga cancelamento e não deixa panic escapar.
- **CA-074-12:** erros públicos possuem categorias/fases estáveis, preservam
  correlação e não ecoam segredos ou texto arbitrário.
- **CA-074-13:** um único tracer append registra eventos reais Bridge e
  SPE/PP/RSP no arquivo absoluto, com falha de persistência observável.
- **CA-074-14:** REST local usa DTOs, fila compartilhada, loopback, limites,
  status HTTP e alias RST somente se explicitamente compatível com CAN/EOT.
- **CA-074-15:** testes Go, AAR, Android, scripted, multiprocesso, integração
  e hardware são executados com evidência de ambiente, comando, resultado e
  código de saída.
- **CA-074-16:** a validação física distingue aceitação de comando, estado da
  sessão e observação visual; logs e artefatos não contêm dados sensíveis.
- **CA-074-17:** divergências D-001 a D-008 e lacunas L-001 a L-004 são
  resolvidas, aceitas como limitação formal ou encaminhadas antes do gate de
  implementação.
- **CA-074-18:** o launcher Windows cumpre integralmente a seção 4.1.1:
  herança e validação da COM, isolamento de ambiente, modo físico, diretório
  correto, configuração/log visíveis, paths com espaços, ADB opcional,
  propagação de exit code e janela legível, sem privilégio ou encerramento
  automático de processo concorrente.

## 19. Definition of Done

A implementação futura só poderá ser considerada concluída quando:

1. todos os requisitos desta SPEC tiverem implementação ou justificativa formal
   de exclusão;
2. os comandos e adaptadores tiverem testes associados;
3. o inventário de produção Go e a cobertura estiverem registrados;
4. GoDoc/KDoc e READMEs estiverem coerentes;
5. AAR/APK consumidos tiverem proveniência e hash;
6. testes de falha, cancelamento, timeout, cleanup, reabertura e concorrência
   estiverem evidenciados;
7. o fluxo scripted e o fluxo físico tiverem evidências separadas;
8. nenhuma evidência declarar sucesso por arquivo vazio, listener, reverse ou
   fake isolado;
9. revisão de implementação, validação, aprovação humana, atualização de
   `specs/system/` e commit rastreável estiverem concluídos;
10. os cenários do launcher físico de CA-074-18 tiverem evidência própria,
    distinta dos testes scripted e do hardware;
11. a Change só então puder ser arquivada, mantendo intactas as cinco fontes.
