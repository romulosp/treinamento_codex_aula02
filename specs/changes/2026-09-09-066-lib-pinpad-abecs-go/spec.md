# SPEC: 066-lib-pinpad-abecs-go

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Descrição executiva

Esta Change define a integração da comunicação com pinpad ABECS v2.12 em uma biblioteca Go headless. O produto não é uma API web, não é um servidor e não deve abrir listeners de rede: ele fornece uma fachada Go interna para controlar uma porta serial real, serializar comandos e interpretar respostas do pinpad.

O objetivo de integração completa não será considerado atendido pela simples existência de constantes, builders ou stubs. Cada comportamento relevante do protocolo deverá ser classificado como integrado, parcialmente integrado, fora de escopo formal ou pendente de uma Change específica. Comandos que tenham contrato próprio deverão possuir uma SPEC individual nesta Change ou em uma Change explicitamente referenciada.

As validações unitárias podem usar adaptadores determinísticos para framing, CRC, parser e fila. A validação de comunicação, status, timeout, cancelamento e comportamento do dispositivo deverá ser feita com pinpad físico e porta serial real. Um fake não pode ser usado para declarar a conversão funcional concluída.

## Identificação

- `groupId`: `br.com.romulopenha`
- `artifactId`: `lib-pinpad-abecs-go`
- módulo: `br.com.romulopenha/lib-pinpad-abecs-go`
- diretório: `apps/desktop/libpinpadabecsgo/`
- executável de validação: `cmd/libpinpadabecsgo/`

## Referências e dependências

- `proposal.md`, `DESIGN.md`, `tasks.md`
- `specs/shared/process/workflow.md`
- `specs/shared/architecture/backend-golang.md`
- `specs/shared/testing/golang-testing.md`
- `.agents/skills/golang-concurrency/SKILL.md`
- `.agents/skills/golang-context/SKILL.md`
- `.agents/skills/golang-safety/SKILL.md`
- `.agents/skills/golang-error-handling/SKILL.md`
- `.agents/skills/golang-security/SKILL.md`
- `.agents/skills/golang-documentation/SKILL.md`
- `go.bug.st/serial`
- SPECs individuais desta Change: `spec-command-can.md`, `spec-command-opn.md`,
  `spec-command-clo.md`, `spec-command-clx.md`, `spec-command-gix.md`,
  `spec-command-dsp.md`, `spec-command-dex.md`, `spec-command-mnu.md`,
  `spec-command-dsi.md`, `spec-command-qrcode.md`, `spec-command-mli.md`,
  `spec-command-mlr.md`, `spec-command-mle.md`, `spec-command-lmf.md`,
  `spec-command-dmf.md`, `spec-command-tli.md`,
  `spec-command-tlr.md`, `spec-command-tle.md`, `spec-command-gky.md`,
  `spec-command-gcx.md`, `spec-command-gtk.md`, `spec-command-gox.md`,
  `spec-command-fcx.md`, `spec-command-gpn.md` e `spec-command-rst.md`.
- SPECs transversais desta Change: `spec-logging.md`,
  `spec-infra-serial-cancel.md` e `spec-protocolo-seguro.md`.
- Manual ABECS v2.12 fornecido para esta Change e requisitos comportamentais derivados do legado. Caso uma implementação seja baseada em outra revisão do manual, a versão deverá ser registrada na SPEC individual e validada contra o dispositivo utilizado.

## Requisitos funcionais

### RF-001 — Biblioteca e arquitetura

A entrega deverá ser uma biblioteca Go reutilizável, consumida por outro processo através de import de pacote Go, nunca por rede. "Sem service" nesta SPEC significa exclusivamente a ausência de processo servidor externo, listener de rede, REST, WebSocket, gRPC, UI ou daemon. A fachada interna equivalente a `PinpadService`, descrita em RF-013, é a única camada de aplicação permitida e deverá viver em `internal/application/service`; ela não expõe rede, não abre porta TCP/HTTP e não é instanciada como processo independente. Deverá usar Clean Architecture, DDD e Ports and Adapters. Nenhum pacote deverá depender de Gin, Echo, Fiber, WebSocket, gRPC, frontend ou desktop framework. Todo acesso externo deverá ocorrer por interfaces.

A estrutura deverá ser:

```text
apps/desktop/libpinpadabecsgo/
├── cmd/libpinpadabecsgo/
├── internal/domain/{model,protocol,parser,command,queue,session,state,error}
├── internal/application/service
├── internal/infrastructure/{serial,config,logging,worker}
├── internal/utilitario/{crc,tlv,bytes}
├── go.mod
└── README.md
```

### RF-002 — Compatibilidade serial e configuração

Suportar Windows e Linux, incluindo portas `COM1..COMn`, `/dev/ttyS*`, `/dev/ttyUSB*` e `/dev/ttyACM*`, utilizando `go.bug.st/serial`, sem CGO.

Variáveis:

- `PORTA_PINPAD`: opcional e prioritária; quando definida no ambiente do
  processo e não vazia, substitui a porta do modelo;
- opcionais: `PINPAD_BAUDRATE`, `PINPAD_TIMEOUT`, `LOG_LEVEL` e
  `PINPAD_LOG_FILE`.

Defaults: porta `COM7`, baud rate `19200`, timeout `30s`, log `INFO`. O modelo
`PinpadConfig` deverá conter `Port`, `BaudRate`, `Timeout`,
`AutoLoadEMVTables`, `AcquirerIndex` e `TableVersion`, com defaults `COM7`,
`19200`, `30s`, `false`, `00` e `TABVER0001`, respectivamente. O carregamento operacional inicia com
esses defaults e aplica as variáveis de ambiente definidas no processo; logo,
ausência de `PORTA_PINPAD` usa `COM7`, enquanto valor presente e não vazio tem
precedência. Um valor vazio, baud rate inválido ou timeout inválido retorna erro
de configuração. O README e `start_aplication.bat` devem refletir essa mesma
precedência sem persistir alteração no sistema.

### RF-003 — Modelos e estados

Implementar `PinpadState` com `CLOSED`, `OPEN`, `BUSY` e `DESYNCHRONIZED`;
`DeviceInfo` com número de série, part number, modelo, fabricante, versão do SO,
versão ABECS e versão do kernel; `DisplayCapabilities` com capacidades textuais,
gráficas, mídia, contactless, ICC, tarja, modelo e fabricante. O estado
`DESYNCHRONIZED` protege a fila contra respostas atrasadas enquanto a recuperação
CAN/EOT e a reconexão controlada não restabelecerem o canal.

Implementar `Response` com `AckType`, `StatusCode`, `RawData` e `Tags`, além de `GCXResponse` completo conforme os fluxos definidos nesta SPEC.

### RF-004 — Protocolo ABECS

Implementar constantes `PP_SYN=0x16`, `PP_ETB=0x17`, `PP_ACK=0x06`, `PP_NAK=0x15`, `PP_EOT=0x04`, `PP_CAN=0x18` e `PP_DC3=0x13`.

Implementar `CRC16CCITT([]byte) uint16` com polinômio `0x1021`, inicial `0x0000` e transmissão high byte first.

Implementar `ApplySubstitution` e `RemoveSubstitution`:

- `0x13 -> 0x13 0x33`;
- `0x16 -> 0x13 0x36`;
- `0x17 -> 0x13 0x37`.

Implementar `BuildPacket(payload []byte)` no formato `SYN + PAYLOAD_SUBSTITUÍDO + ETB + CRC_HIGH + CRC_LOW`, além de `ReadFullResponse` com ACK/NAK/EOT, remoção de substitution, validação de CRC e payload original.

O framing é uma responsabilidade transversal. O comportamento específico de cada comando não será presumido a partir do framing: payload, status, resposta, timeout e redaction devem ser definidos na SPEC individual do comando correspondente.

#### Artefatos especificados a partir dos requisitos ABECS

Esta Change deverá gerar, em Go, os seguintes grupos de constantes e modelos, sem transportar macros C/C++ literalmente:

- `protocol`: bytes de controle, tags `RSP_DATID`, bases de tags dinâmicas, tipos multimídia e limites `MLR`/`TLR`;
- `state`: todos os códigos `RSP_STAT` de `ST_OK` a `ST_MFERR`, com `GetStatusDescription`;
- `command`: todos os parâmetros `SPE_xxx`, códigos de tecla `GKY`, métodos e tamanhos `GPN`, opções, tipos de transação e estados ICC de `GCX`, `GTK`, `GOX` e `FCX`;
- `model`: `BerTLV`, `GCXResponse` e modelos próprios de `GTK`, `GOX` e `FCX`, incluindo campos de dados EMV e tags sensíveis sujeitos a redaction;
- `command`: catálogo, builders e contratos dos comandos `DSP`, `DEX`, `MNU`, `DSI`, `MLI`, `MLR`, `MLE`, `TLI`, `TLR`, `TLE`, `GKY`, `GPN`, `GCX`, `GTK`, `GOX`, `FCX` e `CLX`.

Os artefatos futuros devem expor somente constantes, tipos, builders e contratos nesta Change; não devem implementar fluxo transacional para comandos avançados.

### RF-005 — Parsers

Implementar `ParseAbecsResponse`, suportando ACK, NAK, EOT, `RSP_STAT`, `PP_xxx` e `SPE_xxx`, e mapear no mínimo as tags `8001`, `8002`, `8003`, `8004`, `8005`, `8006`, `8009` e `8010`.

Implementar `BerTLV` com `Tag uint32`, `Length int`, `Value []byte` e `Children []BerTLV`, suportando tags simples e longas, length curto e estendido, TLV aninhado e EMV.

### RF-006 — Catálogo de status e erros

Implementar `StatusOK=000`, `StatusTimeout=012`, `StatusCancel=013`, `StatusNoCard=043`, `StatusDumbCard=060` e `StatusCardBlocked=079`, além de `GetStatusDescription`.

Implementar erros `ErrPortNotConfigured`, `ErrPortUnavailable`, `ErrPinpadClosed`, `ErrPinpadBusy`, `ErrTimeout`, `ErrChecksumInvalid`, `ErrInvalidResponse`, `ErrNakReceived`, `ErrSessionAlreadyOwned`, `ErrNotSessionOwner`, `ErrQueueFull`, `ErrQRCodeGeneratorNotConfigured` e `ErrNotImplemented`.

### RF-007 — Porta serial

Definir a porta `SerialPort` com `Open`, `Close`, `Read(context.Context)`, `Write` e `IsOpen`. O adaptador real deverá encapsular `go.bug.st/serial`; adaptadores determinísticos poderão permitir testes de ACK, NAK, EOT, respostas, timeout e erros sem hardware. O contrato detalhado de cancelamento e propagação de contexto está em `spec-infra-serial-cancel.md` e prevalece sobre qualquer assinatura resumida nesta seção.

### RF-008 — Session Manager

Implementar interface equivalente ao legado: `Claim`, `Renew`, `Release`, `ForceRelease`, `IsOwner`, `HasOwner`, `CurrentOwner` e `IsSessionExpired`. Somente um owner poderá possuir o pinpad. A sessão expirará após 300 segundos sem atividade. Claim pelo mesmo owner será idempotente; claim por outro owner retornará falso; release somente funcionará para o owner atual.

### RF-009 — Command Queue e worker

Implementar `Command` com `ID`, `Type` e `Execute(context.Context) (*Response, error)`.

`CommandQueue` terá duas operações públicas de entrada, com semântica distinta e obrigatória:

- `Enqueue(ctx context.Context, cmd Command) error`: insere o comando no final da fila e retorna imediatamente. Não aguarda execução. Retorna `ErrQueueFull` de forma não bloqueante quando a capacidade estiver esgotada, e retorna o erro do contexto se `ctx` já estiver cancelado no momento da tentativa de inserção. O resultado da execução é entregue exclusivamente pelo mecanismo de retorno definido em `Command` (por exemplo, canal ou callback contido no próprio `Command`), nunca pelo retorno de `Enqueue`;
- `Submit(ctx context.Context, cmd Command) (*Response, error)`: função de conveniência que chama `Enqueue` e aguarda o resultado do comando ou o cancelamento de `ctx`, o que ocorrer primeiro. `Submit` é a única operação bloqueante da fila.

A fila terá capacidade 100, será FIFO, thread-safe e terá um único worker por instância, responsável por executar `Command.Execute` sequencialmente. `Size`, `Clear` e `Stop` também serão expostos: `Clear` remove comandos ainda não iniciados e conclui seus resultados como cancelados; `Stop` é idempotente, cancela a operação em execução, drena a fila com o mesmo efeito de `Clear` e encerra o worker sem vazamento de goroutine. Nenhum comando poderá ser executado após `Stop` retornar.

### RF-010 — Serviço principal

Implementar `PinpadService` com `Open`, `Close`, `Reset` por CAN/EOT, `SendCommand`, `GetInfo`, `GetDisplayCapabilities` e `GetState`.

Implementar completamente os comandos CAN, OPN, GIX e CLO:

- CAN: entrada `0x18`, resposta esperada EOT;
- OPN: payload literal `OPN`, resposta ACK seguida de `OPN000`;
- GIX: payload `GIX000`, resposta ACK e payload;
- CLO: payload `CLO032` + mensagem S32, resposta `CLO000`.

`CLX` será implementado como encerramento visual não bloqueante, separado de
`CLO` e da comunicação segura, conforme `spec-command-clx.md`. Para DSP, DEX,
MNU, DSI, MLI, MLR, MLE, GCX, GTK, GOX, FCX, GKY, GPN, TLI, TLR e TLE criar
os contratos, tipos, builders, parsers e fluxos definidos em RF-012 e nas SPECs
individuais citadas. Os fluxos não poderão introduzir REST, servidor, UI ou
transporte diferente da porta serial.

### RF-011 — Logging

Usar `log/slog` em formato estruturado para operação, duração e resultado e o
`Tracer` definido em `spec-logging.md` para persistir o rastro serial ABECS. O
utilitário local deve gravar no arquivo canônico os eventos `open`, `close`,
`SPE`, `PP` e `RSP` de todo comando tipado que alcançar a porta serial. A
criação de um arquivo vazio ou de um segundo arquivo homônimo em função do
diretório de trabalho não satisfaz este requisito. Para comandos não sensíveis,
`SPE` deve conter o pacote hexadecimal integral efetivamente escrito e cada
`PP` deve conter exatamente os bytes de uma leitura física; o log estruturado
de conclusão e a linha `RSP` não os substituem. Não registrar PAN, TRACK2, PIN,
PIN block, KSN, chaves ou dados EMV sensíveis.

### RF-012 — Artefatos funcionais da comunicação ABECS

A implementação Go deverá gerar os artefatos abaixo. Os nomes são contratos funcionais; a organização interna poderá seguir Clean Architecture e Ports and Adapters. Nenhum arquivo legado C/C++ será incluído no repositório.

#### 1. Logging e transporte serial

- `Logging`: inicialização, encerramento, mensagens estruturadas e dump hexadecimal controlado, com mutex seguro e redaction obrigatório;
- `SerialPort`: abertura, configuração 8N1, baud rate, timeout, leitura, escrita, limpeza de buffers, fechamento e estado;
- `ReadByte`, `WaitResponse` e `SendCommand`: equivalentes comportamentais às funções C++, porém usando `context.Context`, `time.Duration` e erros tipados;
- fake serial determinístico para ACK, NAK, EOT, timeout, erro de leitura/escrita e frames completos.

#### 2. Protocolo, conversão e parser

- `CRC16CCITT`, substitution, construção de pacote e construção de comando com comprimento decimal de três dígitos;
- leitor de resposta completo que detecta SYN, ETB, CRC, bytes agrupados pelo driver e bytes excedentes sem perda;
- conversão `BytesToString`, `BytesToHex`, `HexStringToBytes` e `BytesToHexString`, sem normalização silenciosa de entrada inválida;
- parser ABECS por `RSP_ID`, status e TLV, com erro para truncamento, CRC inválido, NAK e resposta incompatível;
- parser BER-TLV com tags longas, lengths curto/estendido, objetos aninhados e dados EMV.

#### 3. Builders e parsers de display

- `BuildDSPCommand`: duas linhas de 16 caracteres, preenchimento/truncamento conforme contrato;
- `BuildDEXCommand`: mensagem de até 160 caracteres, prefixada por `DEX_MSGLEN` (N3), sem campos de alinhamento ou tipo de mensagem;
- `BuildMNUCommand`: timeout, título, opções e tags de parâmetros conforme manual;
- `ParseMNUResponse` e `IsDisplayResponseSuccess`, com índice selecionado e status validado.

#### 4. Builders e fluxo de multimídia

- `BuildMLICommand`, `BuildMLRCommand`, `BuildMLECommand`, `BuildLMFCommand`,
  `BuildDMFCommand` e `BuildDSICommand`;
- cálculo de CRC do arquivo, preenchimento do tipo B1 como PNG/JPG/GIF ou RUF
  para tipo desconhecido sem rejeição durante MLI, limite de bloco
  `MLR_MAX_BLOCK_SIZE` e callback de progresso;
- listagem de nomes repetidos em `PP_MFNAME`, normalizados para maiúsculas, e exclusão de uma ou mais mídias por `SPE_MFNAME`;
- `SendMultimediaFile`, com sequência MLI → MLR* → MLE, e `DisplayImage`, como
  operação DSI independente sobre uma mídia persistida, com ACK, resposta de
  status, timeout e cancelamento;
- `ListMultimediaFiles` e `DeleteMultimediaFiles`, com lista vazia válida, validação de nomes A8 e status ABECS preservados;
- após timeout de ACK/resposta ou resposta de outro comando, enviar CAN e
  confirmar EOT; se as três tentativas falharem, rejeitar comandos comuns
  durante uma reconexão serial controlada, composta por fechamento físico,
  abertura, CAN/EOT inicial e OPN; nunca repetir automaticamente o comando cujo
  resultado ficou indeterminado;
- nenhum log poderá registrar conteúdo de arquivo indiscriminadamente; somente metadados não sensíveis e resumo controlado.

#### 5. Builders e fluxo de tabelas EMV

- `BuildTLICommand`, `BuildTLRCommand` e `BuildTLECommand`;
- validação de `AcquirerIndex`, `TableVersion`, quantidade máxima `TLR_MAX_RECORDS`, registros vazios e tamanho do payload;
- envio e confirmação de cada etapa usando fila, contexto e erros tipados.

#### 6. GKY

- builder do comando literal `GKY`, sem modo ou parâmetro adicional;
- parser dos códigos `GKY_KEY_OK`, `GKY_KEY_CANCEL`, `GKY_KEY_CLEAR`, `GKY_KEY_F1`, `GKY_KEY_F2`, `GKY_KEY_F3`, `GKY_KEY_F4` e `GKY_KEY_NONE`;
- timeout e cancelamento distinguíveis de tecla não pressionada.

#### 7. GCX

- modelo `GCXResponse` e parser somente dos campos realmente retornados pelo comando `GCX`, conforme `spec-command-gcx.md`;
- builder parametrizado por valor, data, hora e opções de transação, serializados
  como parâmetros ABECS `SPE_AMOUNT` (`0x0013`, N12), `SPE_TRNDATE`
  (`0x0015`, N6), `SPE_TRNTIME` (`0x0016`, N6) e `SPE_GCXOPT`
  (`0x0017`, N5), cada um com identificador e comprimento binários;
- `SendGCXCommand` e conveniência de compra somente com dados sensíveis protegidos em logs e respostas;
- a conveniência de compra envia exatamente um `GCX` com os dados informados;
  não existe envio preliminar com data/hora zeradas;
- trilhas completas, KSN de trilha, PIN block, KSN de PIN e Issuer Script Results não serão inferidos de `GCX`; esses dados pertencem, quando aplicável, a `GTK`, `GOX` e `FCX`.

#### 8. GPN

- builders para MK/WK e DUKPT;
- validação de método, índice de chave, WKENC, PAN, limites de PIN e mensagem de 32 caracteres;
- parser de PIN block H16 para 8 bytes e KSN H20 para 10 bytes;
- `SendGPNCommand`, `SendGPNCommandMK` e `SendGPNCommandDUKPT`, sem registrar PAN, PIN, PIN block, WKENC ou KSN.

#### 9. Exclusão de RST

RST não existe no manual ABECS 2.12 e não integra a biblioteca. O reset da operação usa CAN/EOT.

#### 10. GTK, GOX, FCX e CLX

- `GTK`: builder, parser e modelo próprios, com pré-condição de captura,
  seleção de PAN/trilhas, criptografia, KSN e redaction conforme
  `spec-command-gtk.md`;
- `GOX`: builder, parser e modelo próprios para continuação EMV, decisão,
  PIN online, KSN e TLV, conforme `spec-command-gox.md`;
- `FCX`: builder, parser e modelo próprios para resposta da credenciadora,
  objetos EMV e Issuer Script Results, conforme `spec-command-fcx.md`;
- `CLX`: builder e parser para mensagem/mídia de encerramento visual não
  bloqueante, conforme `spec-command-clx.md`; não fecha a porta física, mas o
  pinpad encerra comunicação segura e limpa `KSEC` quando ela estiver ativa.

#### 11. Artefatos que não serão gerados

Não serão gerados: REST, HTTP, WebSocket, servidor, Windows Service, Linux daemon, UI, instalador, API de comando hexadecimal bruto, armazenamento de PAN/PIN, logs de dados sensíveis ou tradução literal de `HANDLE`, `DWORD`, `CRITICAL_SECTION` e `CreateFileA`.

#### Matriz de especificação dos arquivos fornecidos

Os arquivos C++ fornecidos como anexos são somente entrada de requisitos para esta Change. Eles não serão copiados ou armazenados no repositório. A geração Go deverá ser rastreável pela seguinte matriz:

| Origem funcional fornecida | Artefato Go especificado |
| --- | --- |
| `logInit`, `logClose`, `logString`, `logBytes` | infraestrutura de logging com `slog`, tracer serial em append, caminho canônico, mutex, visibilidade de escrita e redaction |
| `openPinpad`, `closePinpad`, `readByte`, `waitResponse`, `sendCommand` | porta `SerialPort`, adaptador serial e serviço de transporte com `context.Context` |
| `crc16_abecs`, `applySubstitution`, `buildPacket`, `buildAbecsCommand`, `readFullResponse` | pacote de protocolo ABECS e utilitários CRC/framing |
| `bytesToString`, `bytesToHex`, `hexStringToBytes`, `bytesToHexString` | utilitários Go de bytes/hex com validação explícita |
| `parseAbecsResponse`, `parseBerTlvData`, `printBerTlvObject` | parsers ABECS e BER-TLV; impressão será substituída por logging seguro |
| `buildDSPCommand`, `buildDEXCommand`, `buildMNUCommand`, `parseMNUResponse`, `isDisplayResponseSuccess` | builders e parsers de display |
| `buildMLICommand`, `buildMLRCommand`, `buildMLECommand`, `buildDSICommand`, `calculateFileCRC`, `isMultimediaResponseSuccess` | builders, CRC de arquivo e parser de multimídia |
| `sendMultimediaFile`, `displayImage` | aplicação de multimídia serializada pela fila |
| `buildTLICommand`, `buildTLRCommand`, `buildTLECommand` | builders de carga de tabelas EMV |
| `sendGKYCommand` | comando GKY, parser de teclas e resultado tipado |
| `readFullResponseGCX`, `buildGCXOpt`, `buildPacketGCX`, `buildGCXCommand`, `sendGCXCommand`, `sendGCXPurchase` | modelo, builder, framing, parser e serviço GCX |
| `comparePackets`, `logPacketFormatted`, `buildFixedGCXTest` | somente utilitários de teste/debug; não entram no fluxo produtivo nem registram dados sensíveis |
| `sendGPNCommand`, `sendGPNCommandMK`, `sendGPNCommandDUKPT` | builders, validações, parser binário e serviço GPN com redaction obrigatória |
| `sendRSTCommand` | excluído: RST não existe no manual ABECS 2.12; reset usa CAN/EOT |
| `getTracks`, `closeEx`, `startGoOnChipEx`, `goOnChipEx`, `finishChipEx` | comandos e modelos próprios GTK, CLX, GOX e FCX, sem mistura de respostas entre fluxos |
| `open` seguro, geração RSA e cifragem/decifragem AES do legado | comunicação segura isolada conforme `spec-protocolo-seguro.md`, sem JNI, CGO ou logging de chave |
| constantes, `BerTlvObject` e `GCXResponse` | constantes Go, `BerTLV` e `GCXResponse` documentados nesta SPEC |

### RF-013 — Fachada de serviço da biblioteca

A biblioteca deverá gerar uma fachada Go equivalente ao contrato funcional de `PinpadService`. Ela não será um servidor nem um processo independente; será apenas uma API interna/reutilizável para orquestrar a comunicação serial já definida.

#### Configuração e ciclo de vida

- `SetConfig` e `GetConfig`, com cópia segura da configuração e validação dos campos;
- `Open`, `Close` e `Reset` por CAN/EOT, com transições normais
  `CLOSED → OPEN → BUSY → OPEN` e transição excepcional para
  `DESYNCHRONIZED`; quando CAN/EOT não recuperar o diálogo, `Reset` e a
  recuperação pós-timeout deverão tentar uma única reconexão serial controlada;
- `Close` executa o encerramento seguro por `CLO` quando houver sessão segura,
  limpa material temporário e só então fecha a porta; `CLX` também desativa a
  comunicação segura no pinpad, mas não fecha a porta física;
- `GetState`, `EnsureOpen` e erros distinguíveis para porta fechada, ocupada, indisponível, timeout, NAK e resposta inválida;
- `Shutdown` idempotente, encerrando worker, cancelando operações pendentes e fechando a porta quando aplicável;
- substituição de `HANDLE`, `recursive_mutex`, `Sleep` e `DWORD` por interfaces Go, `sync`, `context.Context` e `time.Duration`.

#### Comandos básicos

- `GetInfo` e `GetInfoRaw`, usando `GIX`, parser ABECS e conversão segura de resposta;
- sincronização de uma única operação de hardware por instância, sempre passando pela fila/worker;
- nenhum método `SendRawCommand` ou equivalente será exposto publicamente pela fachada; toda operação de hardware exposta corresponde a um comando tipado e documentado nesta SPEC, em conformidade com a exclusão de API de comando hexadecimal bruto definida em RF-015.

#### Display e capacidades

- `DisplayDSP`, `DisplayDEX` e `DisplayMNU`, com validação de estado, ACK, resposta, timeout e resultado selecionado;
- `GetDisplayCapabilities`, interpretando `PP_MODEL`, `PP_MNNAME`, `PP_CAPAB`, `PP_DSPTXTSZ`, `PP_DSPGRSZ` e `PP_MFSUP`;
- suporte a capacidades textuais, gráficas, cor, PNG, JPG, GIF, CTLS, ICC e tarja;
- regras específicas de modelo somente quando documentadas como regra ABECS/configuração, sem heurísticas silenciosas.

#### Imagens e QR Code

- `DisplayDSI`, `LoadMultimediaFile` e `DisplayImage`, delegando aos fluxos de RF-012;
- `DisplayQRCode` deverá depender exclusivamente de uma interface Go definida nesta Change, `QRCodeGenerator`, com o método `Generate(data string, size int) ([]byte, error)` retornando PNG codificado. Nenhuma biblioteca concreta de geração de QR Code será adicionada como dependência direta do módulo nesta Change; a fachada aceitará um `QRCodeGenerator` injetado pelo consumidor. Quando nenhum gerador for configurado, `DisplayQRCode` retornará `ErrQRCodeGeneratorNotConfigured`;
- limites de tamanho (50 a 320) e margem (0 a 10) serão validados antes da chamada ao gerador injetado, retornando erro de validação quando fora da faixa;
- posicionamento customizado (`xPos`, `yPos`) não é suportado pelo protocolo ABECS documentado nesta SPEC; `DisplayQRCode` deverá ignorar esses parâmetros e retornar um aviso estruturado (campo específico do resultado), nunca de forma silenciosa no log;
- callback de progresso com contexto e cancelamento.

#### Tabelas EMV

- `TableLoadInitiate`, `TableLoadRecord` e `TableLoadEnd`;
- `LoadCompleteEMVTable`, executando TLI → lotes TLR → TLE, com progresso, cancelamento e rollback operacional documentado quando houver falha;
- reconhecimento de `StatusTableVersionDifferent` como resultado específico de TLI, conforme regra do protocolo;
- validação de quantidade de registros, lotes e limites `TLR_MAX_RECORDS`.

#### Transação GCX

Esta Change implementará o fluxo GCX no mesmo subconjunto já serializado em RF-012.7: valor, data (`AAMMDD`), hora (`HHMMSS`) e opções de transação (`GCXOpt`), usando as tags `SPE_GCXOPT`, `SPE_TRNDATE`, `SPE_TRNTIME` e `SPE_AMOUNT`.

- `PurchaseGCX` delegará uma única vez a `SendGCXCommand`, aceitando
  `enableCTLS` para produzir `SPE_GCXOPT="10000"`; quando falso, produzirá
  `SPE_GCXOPT="00000"`;
- a opção 11 do utilitário local perguntará se deve aceitar chip/tarja ou
  chip/tarja/contactless e se o valor deve ser mostrado ou ocultado, produzindo
  `SPE_GCXOPT` igual a `00000`, `01000`, `10000` ou `11000`;
- durante o GCX blocante, frames `NTM000` válidos serão tratados como
  notificações intermediárias e a leitura continuará até a resposta final
  `GCX`, sem enviar ACK para a notificação;
- o prazo do GCX no utilitário local começará depois da leitura de modo,
  visibilidade, valor, data e hora; a fachada preservará o prazo do contexto do
  consumidor sem reduzi-lo ao timeout genérico de `PinpadConfig`;
- `SendGCXInitialization`, `skipInit`, `UseGCXInitialization` e
  `GCXInitTimeout` serão removidos porque o manual ABECS v2.12 não define uma
  etapa GCX preliminar e exige data e hora válidas no próprio comando;
- `TransactionGCX` com tipo de transação, referência do adquirente, tipo de aplicação, lista de AIDs, cashback, moeda, máscara de PAN, dados EMV e lista de tags **não será implementado nesta Change**: a serialização completa desses campos depende de tabela de tags, ordem e limites ainda não documentados nesta SPEC. O contrato Go (assinatura da interface/método) poderá existir como stub retornando `ErrNotImplemented`, para reserva de nome, mas nenhuma serialização inventada será escrita. A implementação completa de `TransactionGCX` fica para uma Change futura, quando a tabela de parâmetros for especificada;
- nenhuma entrada ou resposta sensível será registrada em texto ou hexadecimal.

#### Continuação e finalização EMV

- `GetTracks`/`GTK` só opera após captura elegível e retorna seu próprio modelo
  de dados sensíveis, nunca campos adicionados a `GCXResponse`;
- `ContinueEMV`/`GOX` exige `GCX` ICC/CTLS EMV bem-sucedido, processa os
  parâmetros EMV especificados e retorna decisão, PIN block/KSN quando
  aplicáveis e objetos TLV no modelo próprio;
- `FinishEMV`/`FCX` sucede GOX quando requerido pela decisão EMV ou pela regra
  da credenciadora e retorna resultado e Issuer Script Results no modelo próprio;
- todos os três fluxos passam por fila, contexto, autorização do consumidor e
  redaction integral, conforme suas SPECs individuais.

#### Teclas, reset e PIN

- `WaitForKeyPress`, com timeout em segundos e retorno dos códigos `GKY_KEY_*`;
- Cancelamento e limpeza de operação usam CAN/EOT; não há ResetPinpad/RST.
- `GetPIN`, `GetPIN_MK` e `GetPIN_DUKPT`, com validação de método, chave, WKENC, PAN, mensagem e timeout;
- PIN block e KSN serão retornados somente ao chamador autorizado e nunca serão incluídos em logs, mensagens de erro ou métricas.

#### Contratos de retorno e erro

Todos os métodos da fachada deverão retornar erros Go estruturados, preservando causa com `%w` e permitindo distinguir falhas de validação, transporte, protocolo, status ABECS, cancelamento e timeout. Onde o legado retorna `bool`, a fachada poderá fornecer `error` e um resultado tipado, sem perder a informação do status original.

#### Matriz de especificação da fachada

| Origem funcional fornecida | Artefato Go especificado |
| --- | --- |
| construtor, destrutor, `setConfig`, `getConfig` | `New`, `SetConfig`, `GetConfig` e ciclo de vida seguro |
| `open`, `close`, `reset`, `getState`, `ensureOpen`, `setState` | fachada de serviço e máquina de estados |
| `getInfo`, `getInfoRaw` | operações básicas GIX; `sendRawCommand` não será exposto |
| `displayDSP`, `displayDEX`, `displayMNU`, `getDisplayCapabilities` | operações de display e capabilities |
| `displayDSI`, `loadMultimediaFile`, `displayImage`, `displayQRCode` | operações de imagem/QR (via `QRCodeGenerator` injetado) e progresso |
| `tableLoadInitiate`, `tableLoadRecord`, `tableLoadEnd`, `loadCompleteEMVTable` | operações EMV individuais e orquestração completa |
| `sendGCXInitialization`, `purchaseGCX` | fachada transacional GCX no subconjunto especificado em RF-012.7 |
| `transactionGCX` (parâmetros completos) | reservado como stub `ErrNotImplemented`; serialização completa fica para Change futura |
| `getTracks`, `startGoOnChipEx`, `goOnChipEx`, `finishChipEx` | fachadas GTK, GOX e FCX com modelos de resposta próprios e redaction integral |
| `closeEx` | fachada CLX visual não bloqueante; não fecha porta, mas o pinpad encerra sessão segura ativa |
| `waitForKeyPress`, `resetPinpad` | GKY e reset por CAN/EOT na fachada |
| `getPIN`, `getPIN_MK`, `getPIN_DUKPT` | fachada GPN com proteção de dados sensíveis |
| `sendAndWaitResponse`, `sendEMVCommand`, `setError` | helpers internos, não exportados; `getMutex` não será exposto |

### RF-014 — Fila de comandos e sessões

#### Fila de comandos

A fila de comandos é definida integralmente em RF-009 (`Command`, `Enqueue`, `Submit`, `Size`, `Clear`, `Stop`). Esta seção apenas reforça propriedades adicionais exigidas para a fila operar sem HTTP ou WebSocket:

- nenhuma operação da fila depende de conexão de rede, callback de rede ou sessão WebSocket;
- `IsEmpty` é exposto como conveniência thread-safe equivalente a `Size() == 0`;
- `Clear` e `Stop` seguem exatamente o comportamento definido em RF-009, sem exceção adicional;
- resultados e callbacks internos não poderão capturar mutexes durante I/O.

#### Gerenciamento de sessão

A implementação Go deverá gerar `SessionManager` para posse lógica do pinpad, sem vínculo com conexão WebSocket:

- `SessionID` será um identificador textual opaco;
- `Claim` adquire a posse quando não há owner ou quando a sessão anterior expirou;
- `Claim` pelo mesmo owner renova a atividade e é idempotente;
- `Claim` por owner diferente retorna `false` ou `ErrSessionAlreadyOwned`, conforme a API usada;
- `Renew` só funciona para o owner atual;
- `Release` só libera quando chamado pelo owner atual;
- `ForceRelease` libera independentemente do owner;
- `IsOwner`, `HasOwner`, `CurrentOwner` e `IsSessionExpired` serão thread-safe;
- expiração ocorrerá após 300 segundos de inatividade, com relógio injetável nos testes;
- consultas de sessão expirada não deverão expor o identificador como owner ativo.

#### Matriz de especificação da fila e sessão

| Origem funcional fornecida | Artefato Go especificado |
| --- | --- |
| `Command`, callback, `enqueue`, `dequeue` | comando tipado, `Enqueue`/`Submit` conforme RF-009 e worker contextual |
| `isEmpty`, `clear`, `size`, `stop` | operações thread-safe, cancelamento e shutdown idempotente |
| `claim`, `renew`, `release`, `forceRelease` | gerenciamento de posse e expiração |
| `isOwner`, `hasOwner`, `getCurrentOwner`, `isSessionExpired` | consultas thread-safe com relógio injetável |

### RF-015 — Exclusão explícita da bridge HTTP

O arquivo funcional equivalente a `bridge_server.cpp` não será convertido para Go nesta Change. Não serão gerados:

- `RunServer`, `HandleClient`, `HandleRequest` ou listener TCP;
- HTTP, JSON de transporte, CORS, endpoints, WebSocket, Winsock ou `ws2_32`;
- handlers `handleOpen`, `handleClose`, `handleStatus`, `handleReset`, `handleGetInfo`, `handleDSP`, `handleDEX`, `handleMNU`, `handleGCX`, `handleGKY`, `handleRST`, `handleGPN` ou similares;
- estado global de pinpad, sessão ou fila para atender múltiplos clientes;
- conversão `hexToAscii`, `BuildHttpResponse`, `successResponse`, `errorResponse` e decodificação Base64 como camada HTTP.

As funcionalidades de pinpad, fila e sessão serão disponibilizadas somente como componentes Go reutilizáveis e testáveis, sem servidor embutido.

### RF-015.1 — Matriz obrigatória de SPECs por comando

Cada comando abaixo deverá possuir uma SPEC individual, ou uma SPEC individual explicitamente referenciada que contenha o contrato completo do comando:

| Grupo | Comandos | Conteúdo mínimo da SPEC |
| --- | --- | --- |
| Controle | `CAN`, `OPN`, `CLO`, `CLX` | frame/payload, ACK/EOT, ciclo de vida, cancelamento e erros; RST é exclusão formal |
| Informações | `GIX` | tags de dispositivo, display, multimídia e parsing |
| Display | `DSP`, `DEX`, `MNU`, `DSI` | limites, formato de texto/imagem, retorno e timeout |
| Multimídia | `MLI`, `MLR`, `MLE` | preparação, blocos, encerramento, CRC e progresso |
| Tabelas | `TLI`, `TLR`, `TLE` | versão, lotes, status 020, limites e retomada |
| Teclas | `GKY` | payload literal, status de tecla, timeout e cancelamento |
| Transação/cartão | `GCX`, `GTK`, `GOX`, `FCX` | parâmetros, tags permitidas, dados sensíveis e sequência |
| PIN | `GPN` | MK/WK/DUKPT, validação e redaction integral |

Também devem existir SPECs próprias para logging SPE/PP/RSP, cancelamento de
leitura serial e comunicação segura RSA/AES/KSEC, por serem contratos
transversais com critérios observáveis. Uma fachada que apenas encaminha bytes
sem implementar o contrato do comando deve permanecer marcada como parcial ou
`ErrNotImplemented`.

### RF-016 — Execução local sem privilégios administrativos

Deverá existir `start_aplication.bat` na raiz do módulo para execução por usuário comum do Windows, sem exigir elevação de privilégio e sem gravar configuração permanente no sistema. O script deverá:

- preservar `PORTA_PINPAD` já definida no ambiente da sessão e atribuir `COM7`
  somente quando ela estiver ausente ou vazia; definir `PINPAD_BAUDRATE` e
  `PINPAD_TIMEOUT` somente na sessão do processo;
- preservar `PINPAD_LOG_FILE` quando definida; quando ausente, definir o
  caminho absoluto `<raiz-do-módulo>/logs/LogPinpadAbecs.txt`, criar o
  diretório e exibir o destino efetivamente ativo;
- definir `GOROOT` explicitamente quando o ambiente Go configurado estiver fora do PATH padrão;
- usar cache de compilação e módulo dentro do diretório do módulo, quando necessário;
- compilar o executável em um diretório local do módulo e executá-lo a partir desse diretório, evitando depender do executável temporário em `%LOCALAPPDATA%\go-build`;
- exibir a porta, versão do Go, caminho do binário e código de saída;
- não solicitar senha, UAC ou alteração de política de grupo.

Essa regra não contorna políticas corporativas que bloqueiem a execução de binários não autorizados. Nesse caso, o script deverá informar o bloqueio e a necessidade de autorização do administrador, sem tentar burlar a política.

### RF-017 — Documentação permanente do código Go

Todo código Go novo, gerado, alterado ou convertido nesta Change deverá ser documentado conforme `.agents/skills/golang-documentation/SKILL.md`. Esta regra permanece válida durante toda a vida da Change e para qualquer correção posterior relacionada a ela.

O requisito aplica-se ao código de produção e aos exemplos executáveis que fizerem parte da entrega:

- todo pacote Go deverá possuir comentário de pacote iniciado pelo nome do pacote;
- toda função, método, tipo, interface, constante ou variável exportada deverá possuir comentário Go iniciado pelo identificador documentado;
- comentários deverão explicar propósito, quando utilizar, restrições, parâmetros, retornos e erros relevantes, sem inventar comportamento não comprovado pelo código ou pela SPEC;
- funções e métodos internos complexos deverão receber comentários quando o fluxo de protocolo, concorrência, segurança, redaction ou cancelamento não for autoexplicativo;
- interfaces deverão documentar o contrato de implementação, propriedade de recursos, concorrência, cancelamento e erros observáveis;
- comandos ABECS, builders, parsers, modelos, erros, adaptadores seriais, fila, worker, sessão, tracer e fachada deverão possuir documentação coerente com a SPEC individual correspondente;
- exemplos executáveis (`ExampleXxx`) deverão ser adicionados quando documentarem uma API pública ou um fluxo de uso relevante e deverão ser validados por `go test`;
- comentários não poderão expor PAN, trilhas, PIN, PIN block, KSN, chaves, IV ou qualquer material criptográfico real;
- README, documentação do módulo, instruções de configuração e documentação orientada a agentes deverão permanecer coerentes com o código aprovado;
- código gerado automaticamente também deverá sair documentado; caso a ferramenta não suporte comentários, o gerador, o template ou um arquivo de documentação complementar deverá ser ajustado antes da aceitação;
- não será aceito código novo sem documentação apenas porque o método é interno, se sua ausência dificultar a compreensão de protocolo, segurança, concorrência ou conversão do legado.

A revisão da implementação deverá verificar a cobertura documental de todos os pacotes e símbolos exportados, e a validação deverá registrar a inspeção executada. A ausência de documentação exigida por este RF bloqueia a aprovação da implementação.

### RF-018 — Comunicação segura ABECS

Comunicação segura integra o escopo desta Change e é definida por
`spec-protocolo-seguro.md`, que prevalece para negociação por OPN, ciclo de vida
de `KSEC`, pacotes protegidos, AES-CBC, `DC2`, CRC, redaction e encerramento por
`CLO`. O perfil inicialmente permitido é o comprovado pelo legado e pelo
manual ABECS v2.12: RSA de 2048 bits com expoente público 65537 para negociar
uma chave de sessão temporária. Algoritmo, padding, IV, ordem de campos e
formato de pacote não poderão ser deduzidos ou alterados fora da SPEC
transversal e da confirmação no dispositivo de laboratório. `CLX` é o comando
visual definido em `spec-command-clx.md` e também desativa a comunicação segura
no pinpad, sem fechar a porta física.

## Requisitos não funcionais

- Go idiomático, sem tradução literal de tipos ou primitivas C++.
- Compatibilidade Windows/Linux e ausência de CGO.
- Concorrência segura e sem corridas em `go test -race ./...`.
- Erros retornados com contexto, sem `panic` para falhas operacionais.
- Testabilidade sem dispositivo físico.
- Cobertura de produção aplicável mínima de 80%.
- Nenhum servidor ou protocolo de transporte de aplicação nesta Change.
- Código Go novo, convertido ou gerado deve cumprir RF-017 e a checklist da Skill `golang-documentation`.

## Regras de negócio

- O pinpad possui um único owner lógico.
- Apenas uma operação de hardware pode estar ativa por vez.
- Operações dependentes de porta fechada retornam `ErrPinpadClosed`.
- Fila cheia falha imediatamente com `ErrQueueFull`.
- CRC inválido, pacote inválido, NAK e timeout devem ser distinguíveis por erro.

## Cenários e critérios de aceite

- [ ] **CA-001:** módulo e `go.mod` usam exatamente `br.com.romulopenha/lib-pinpad-abecs-go` no diretório definido.
- [ ] **CA-002:** biblioteca compila em Windows e Linux sem CGO.
- [ ] **CA-003:** vetores de CRC cobrem payload vazio, simples e bytes `0x13`, `0x16`, `0x17`.
- [ ] **CA-004:** substitution, un-substitution, packet builder e leitura validam casos normais e inválidos.
- [ ] **CA-005:** parsers ABECS e BER-TLV cobrem tags, lengths, nesting, truncamento e CRC inválido.
- [ ] **CA-006:** fake serial comprova CAN/EOT, OPN/ACK, GIX/ACK+payload e CLO/ACK.
- [ ] **CA-007:** session manager comprova claim, conflito, renew, release e expiração em 300 segundos.
- [ ] **CA-008:** queue comprova FIFO, capacidade 100, `ErrQueueFull`, cancelamento e shutdown.
- [ ] **CA-009:** estados e erros são cobertos por testes.
- [ ] **CA-010:** builders, parsers e fluxos definidos em RF-012 possuem contratos e testes, sem escopo de servidor.
- [ ] **CA-011:** `go test ./...`, cobertura, `go test -race ./...` e `go vet ./...` são executados e registrados.
- [ ] **CA-012:** cobertura aplicável é igual ou superior a 80%, sem percentual inventado.
- [ ] **CA-013:** nenhum log/teste contém dados sensíveis reais.
- [ ] **CA-014:** builders e parsers cobrem display, multimídia, tabelas EMV, GKY, GCX (subconjunto RF-012.7), GTK, GOX, FCX, CLX e GPN; RST permanece excluído.
- [ ] **CA-015:** transporte serial preserva bytes excedentes e suporta frames agrupados em uma ou várias leituras.
- [ ] **CA-016:** fachada Go cobre ciclo de vida com reset por CAN/EOT, `GetInfo`/`GetInfoRaw`, display, imagem, EMV, GCX (subconjunto), GTK, GOX, FCX, CLX, GKY e GPN, sem expor `SendRawCommand`.
- [ ] **CA-016a:** `DisplayQRCode` usa `QRCodeGenerator` injetado, retorna `ErrQRCodeGeneratorNotConfigured` quando ausente, valida tamanho 50-320 e margem 0-10, e reporta `xPos`/`yPos` como não suportados de forma explícita no resultado.
- [ ] **CA-016b:** `TransactionGCX` completo retorna `ErrNotImplemented` sem serializar campos não especificados nesta SPEC.
- [ ] **CA-016c:** `LoadCompleteEMVTable` reconhece `StatusTableVersionDifferent` como resultado válido de TLI e interrompe corretamente em falha de TLR/TLE.
- [ ] **CA-017:** concorrência, cancelamento, shutdown e proteção de dados sensíveis são testados na fachada.
- [ ] **CA-018:** `Enqueue` nunca bloqueia o produtor e retorna `ErrQueueFull` na capacidade máxima; `Submit` bloqueia até resultado ou cancelamento de contexto; `Clear` e `Stop` cancelam comandos pendentes sem executar após o encerramento; `SessionManager` cobre posse, conflito, renovação, liberação e expiração de 300 segundos com relógio injectável.
- [ ] **CA-019:** nenhum artefato de bridge HTTP, WebSocket, listener TCP ou estado global de servidor é gerado.
- [ ] **CA-020:** carregamento usa `COM7` somente quando `PORTA_PINPAD` estiver ausente; valor não vazio definido no ambiente do processo tem precedência. `start_aplication.bat` preserva essa variável quando já existente, executa como usuário comum, não persiste configuração, compila o binário em diretório do módulo e informa claramente bloqueios de política de grupo.
- [ ] **CA-021:** todo pacote Go entregue possui comentário de pacote, todos os símbolos exportados possuem comentários iniciados pelo identificador e os fluxos internos complexos de protocolo, segurança, concorrência e cancelamento estão documentados conforme RF-017.
- [ ] **CA-022:** em pinpad físico, um timeout real seguido de três tentativas
  CAN sem EOT aciona uma única reconexão controlada; a fila permanece protegida,
  a nova abertura exige CAN/EOT e OPN válidos, e o comando original não é
  reenviado automaticamente.
- [ ] **CA-023:** a validação física de DSI registra separadamente status ABECS,
  nome solicitado e confirmação visual do operador; `DSI000` sem observação do
  display não é aceito como prova de que a imagem apareceu.
- [ ] **CA-022:** exemplos executáveis definidos como parte da documentação passam em `go test`, não expõem dados sensíveis e refletem somente contratos aprovados nas SPECs.
- [ ] **CA-023:** a revisão da implementação registra a inspeção documental, incluindo pacotes, símbolos exportados, código gerado e divergências encontradas; qualquer lacuna bloqueia a aprovação.
- [ ] **CA-024:** GTK, GOX, FCX e CLX possuem builders, parsers, modelos e fluxos próprios conforme suas SPECs individuais; nenhum campo de trilha, PIN/KSN ou Issuer Script Results é atribuído a `GCXResponse`.
- [ ] **CA-025:** OPN seguro, pacote protegido e encerramento por CLO/CLX são validados no pinpad físico conforme `spec-protocolo-seguro.md`, sem registrar KSEC, RSA, AES, IV, PIN, PAN, KSN ou criptogramas; `CLX` é validado como comando visual que encerra a sessão segura do pinpad sem fechar a porta física.
- [ ] **CA-026:** o utilitário local informa o caminho absoluto do único arquivo
  de rastro ativo; o arquivo recebe um marcador de ativação antes do menu e,
  para cada comando tipado que alcança a serial, registra a sequência aplicável
  `SPE`, `PP*` e `RSP` conforme `spec-logging.md`, sem depender do diretório de
  trabalho e sem expor dados sensíveis. Para GIX, a validação compara o pacote
  hexadecimal integral da linha `SPE` com os bytes escritos e cada linha `PP`
  com o respectivo retorno do driver.


## RF-018 — Conformidade normativa ABECS 2.12

Builders, parsers, transporte, comunicação segura, limites e testes seguem
`spec-conformidade-abecs-v212.md`, que prevalece sobre texto anterior
incompatível. RST fica excluído do catálogo e da fachada por não existir no
manual adotado.
