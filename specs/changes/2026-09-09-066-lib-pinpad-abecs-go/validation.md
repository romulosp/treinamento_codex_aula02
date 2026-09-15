# ValidaÃ§Ã£o â€” 066-lib-pinpad-abecs-go

Autor: RÃ´mulo Penha

**Data da Ãºltima evidÃªncia executada:** 2026-09-13

**Estado desta evidÃªncia:** `PENDENTE_VALIDACAO_FISICA`

**Fase atual da Change:** `IMPLEMENTACAO_APROVADA`, com validaÃ§Ã£o automatizada
aprovada e matriz fÃ­sica integral pendente.

> As seÃ§Ãµes anteriores a â€œConformidade unitÃ¡ria ABECS 2.12â€ preservam o
> histÃ³rico. A seÃ§Ã£o de 2026-09-13 contÃ©m o resultado automatizado vigente.

## Ambiente

- A biblioteca foi projetada para operar em mÃºltiplas plataformas e arquiteturas, nÃ£o somente em Windows: suporta ambientes Windows 32/64 bits, Linux e macOS, incluindo CPUs ARM e x86, conforme a compatibilidade do runtime Go e do adaptador serial utilizado.
- O uso de Go Ã© intencional e estratÃ©gico porque oferece compilaÃ§Ã£o nativa para essas plataformas e simplifica a manutenÃ§Ã£o da biblioteca em diferentes arquiteturas, sem depender de uma Ãºnica runtime especÃ­fica.
- Ambiente desta execuÃ§Ã£o: Windows `windows/386`; Go `go1.26.5 windows/386`.
- Pinpad fÃ­sico e porta serial real: indisponÃ­veis nesta execuÃ§Ã£o.
- Sonar/scanner e `govulncheck`: indisponÃ­veis no mÃ³dulo; aplicada Auditoria de Qualidade Assistida por LLM.
- Docker Desktop nÃ£o estÃ¡ em execuÃ§Ã£o. O WSL `docker-desktop` nÃ£o possui Go instalado, portanto nÃ£o oferece alternativa para `-race`.

## Comandos e resultados

| Comando | CÃ³digo | Resultado |
| --- | ---: | --- |
| `gofmt -w ...` | 0 | FormataÃ§Ã£o concluÃ­da. |
| `go vet ./...` | 0 | Aprovado. |
| `go test ./...` | 0 | Todos os testes passaram. |
| `go test ./... -coverprofile=coverage` | 0 | Perfil de cobertura gerado. |
| `go tool cover -func=coverage` | 0 | Cobertura total aferida em **81,7%**. |
| `go build ./...` | 0 | Build aprovado. |
| `go build -o libpinpadabecsgo.exe ./cmd/libpinpadabecsgo` | 0 | ExecutÃ¡vel Windows compilado no diretÃ³rio do mÃ³dulo. |
| `go test -race ./...` | 2 | NÃ£o suportado em `windows/386`. |
| `docker run ... go test -race ./...` | 1 | Docker Desktop nÃ£o estava em execuÃ§Ã£o. |
| `wsl -d docker-desktop -- go version` | 127 | DistribuiÃ§Ã£o sem Go instalado. |
| `go test ./...` apÃ³s ampliaÃ§Ã£o do menu local | 0 | Menu e todos os pacotes aprovados. |
| `go vet ./...` apÃ³s ampliaÃ§Ã£o do menu local | 0 | Aprovado. |
| `go build -o libpinpadabecsgo.exe ./cmd/libpinpadabecsgo` apÃ³s ampliaÃ§Ã£o do menu local | 0 | ExecutÃ¡vel atualizado. |

## EvidÃªncias VAL

- **VAL-001:** a cobertura mÃ­nima de 80% foi atendida com 81,7%.
- **VAL-002:** testes adicionais cobrem CLI local, falhas de transporte serial, escrita parcial, cÃ³pias defensivas, sessÃ£o expirada, cancelamento, resposta invÃ¡lida e limites de byte stream.
- **VAL-003:** a execuÃ§Ã£o de `-race` foi tentada nos ambientes disponÃ­veis; nÃ£o hÃ¡ ambiente compatÃ­vel ativo para essa verificaÃ§Ã£o.
- **VAL-004:** a validaÃ§Ã£o fÃ­sica de comunicaÃ§Ã£o, status, timeout, cancelamento e redaction continua pendente de pinpad e porta serial reais.

## Auditoria de seguranÃ§a

Escopo inspecionado: mÃ³dulo Go, configuraÃ§Ã£o por ambiente, adaptador serial, framing, parser, fila, sessÃ£o, logging e dependÃªncia `go.bug.st/serial`.

- Nenhum segredo confirmado foi encontrado pela busca estÃ¡tica.
- NÃ£o foram encontrados listener de rede, API HTTP, execuÃ§Ã£o de comando do sistema, SQL ou renderizaÃ§Ã£o HTML no mÃ³dulo.
- O log aplica redaction a payloads de GCX, GTK, GOX, FCX e GPN.
- AutenticaÃ§Ã£o, autorizaÃ§Ã£o, tenant e IDOR sÃ£o nÃ£o aplicÃ¡veis: a entrega Ã© uma biblioteca local sem servidor ou identidade de usuÃ¡rio.

O gerador foi atualizado com a opÃ§Ã£o `--change-066`, destinada a criar `docs/security-audit/relatorio-066-lib-pinpad-abecs-go.pdf`. O PDF ainda nÃ£o foi produzido porque a skill `pdf` exige a execuÃ§Ã£o prÃ©via do utilitÃ¡rio `container_tools/mark_artifact_operation_started.mjs`, ausente neste ambiente. O relatÃ³rio histÃ³rico da Change 010 nÃ£o foi reutilizado.

## ValidaÃ§Ã£o documental da revisÃ£o de logging â€” 2026-09-12

Ambiente: Windows, PowerShell, diretÃ³rio
`D:\desenvolvimento\ia\lib-pinpad-abecs`.

| Comando | CÃ³digo | Resultado |
| --- | ---: | --- |
| Bloco PowerShell com `Test-Path`, verificaÃ§Ã£o de whitespace, tokens obrigatÃ³rios SPE/PP, caminho canÃ´nico, RF-L010/CA-L017 e comparaÃ§Ã£o dos comandos de `internal/domain/command/commands.go` com `spec-logging.md` | 0 | PASS: 14 artefatos, 22 comandos e exemplos SPE/PP verificados. |
| `git diff --check -- <artefatos documentais alterados>` | 0 | Nenhum erro de whitespace; somente avisos informativos de futura normalizaÃ§Ã£o LF para CRLF. |

Testes Go nÃ£o foram executados nesta etapa porque a solicitaÃ§Ã£o alterou somente
o contrato documental. Eles voltam a ser obrigatÃ³rios apÃ³s a implementaÃ§Ã£o dos
itens pendentes de logging, junto dos cenÃ¡rios CA-L001 a CA-L017.

## ConclusÃ£o

Esta conclusÃ£o histÃ³rica foi superada pela implementaÃ§Ã£o registrada na seÃ§Ã£o
seguinte. O fluxo fÃ­sico GIX passou a ter evidÃªncia; a matriz fÃ­sica dos demais
comandos e a geraÃ§Ã£o do PDF atual de seguranÃ§a permanecem pendentes. NÃ£o
avanÃ§ar para aprovaÃ§Ã£o, arquivamento ou commit antes das etapas independentes.

## EvidÃªncias da implementaÃ§Ã£o do logging â€” 2026-09-12

Esta seÃ§Ã£o registra evidÃªncias produzidas durante a implementaÃ§Ã£o. Ela nÃ£o
substitui a revisÃ£o independente nem altera o estado para `VALIDADA`.

Ambiente: Windows `windows/386`, Go `go1.26.5`, PowerShell, mÃ³dulo em
`D:\desenvolvimento\ia\lib-pinpad-abecs\apps\desktop\libpinpadabecsgo` e
pinpad fÃ­sico PPP100 na `COM7`, baud rate 19200.

| Comando | CÃ³digo | Resultado |
| --- | ---: | --- |
| `gofmt -w <arquivos Go alterados>` | 0 | FormataÃ§Ã£o concluÃ­da. |
| `go test ./...` | 0 | Todos os pacotes aprovados. |
| `go vet ./...` | 0 | Aprovado. |
| `go test ./... -coverprofile=coverage` | 0 | Testes e perfil concluÃ­dos. |
| `go tool cover -func=coverage` | 0 | Cobertura total **80,7%**; logging **89,5%** e serial **82,4%**. |
| `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | ExecutÃ¡vel Windows usado pelo launcher recompilado. |
| `GOOS=linux GOARCH=amd64 go build ./...` | 0 | Build Linux amd64 aprovado. |
| `go test -race ./...` | 1 | LimitaÃ§Ã£o objetiva: `-race is not supported on windows/386`. |
| `go test -coverprofile=coverage.out ./...` | 1 | O PowerShell/Go 1.26 separou `.out` como pacote; repetido com o nome simples `coverage`, com cÃ³digo 0. |
| `executar projeto.bat`, opÃ§Ã£o `0` | 0 | Imprimiu o caminho absoluto, entrou no menu e aumentou o arquivo canÃ´nico de 5.299 para 5.500 bytes antes do encerramento. |
| `executar projeto.bat`, sequÃªncia `Open â†’ GIX â†’ Close â†’ Sair` | 0 | GIX retornou status `000`; o arquivo canÃ´nico aumentou de 5.500 para 9.878 bytes. O CLO respondeu status `011`, mas o fechamento fÃ­sico foi executado e registrado. |
| `executar projeto.bat`, binÃ¡rio final, sequÃªncia `Open â†’ GIX â†’ Sair` | 0 | GIX retornou status `000`; o arquivo cresceu de 9.878 para 13.734 bytes, registrando OPN nÃ£o seguro, GIX, ACKs, fragmentos PP, RSP e fechamento fÃ­sico. |

EvidÃªncias observadas no arquivo canÃ´nico durante a execuÃ§Ã£o fÃ­sica:

```text
TRACE destination=D:\desenvolvimento\ia\lib-pinpad-abecs\apps\desktop\libpinpadabecsgo\logs\LogPinpadAbecs.txt enabled
[COM7#001] SPE 16 47 49 58 30 30 30 17 7F 4A CMD=GIX
[COM7#001] PP  06
[COM7#001] RSP CMD=GIX STATUS=000
[COM7#001] close()
```

Os sufixos obrigatÃ³rios `FUNC` e `DATA_HORA` estavam presentes nas linhas
reais. O CRC fÃ­sico `7F 4A` difere do valor ilustrativo `12 34` porque foi
calculado para o pacote efetivamente transmitido. A resposta GIX chegou
fragmentada e cada retorno do driver foi gravado em uma linha `PP`, antes do
parser. O antigo arquivo gerado por teste em
`cmd/libpinpadabecsgo/logs/LogPinpadAbecs.txt` foi removido; os testes agora
usam diretÃ³rios temporÃ¡rios e comprovam que o caminho homÃ´nimo nÃ£o Ã© recriado.

### Resultado desta etapa

- Caminho canÃ´nico absoluto: comprovado pelo launcher e pelo CLI.
- Pacote enviado `SPE`, ACK e fragmentos recebidos `PP`: comprovados byte a
  byte por teste automatizado e por execuÃ§Ã£o fÃ­sica GIX.
- `RSP CMD=GIX STATUS=000`: comprovado apÃ³s parsing completo.
- Falhas de escrita, sincronizaÃ§Ã£o e fechamento do tracer: retornadas e
  cobertas por teste; o CLI encerra com cÃ³digo nÃ£o zero quando o tracer perde
  integridade.
- RedaÃ§Ã£o: matriz dos 22 comandos tipados coberta; OPN seguro, MLR, TLR, GCX, GTK,
  GOX, FCX, GPN, comunicaÃ§Ã£o segura e comandos marcados pelo consumidor sÃ£o
  redigidos, mantendo ACK/NAK/EOT/CAN isolados visÃ­veis.

A implementaÃ§Ã£o estÃ¡ pronta para `implementation-review`. Esta seÃ§Ã£o nÃ£o
aprova, nÃ£o valida formalmente e nÃ£o arquiva a Change.

## DiagnÃ³stico do diretÃ³rio consultado â€” 2026-09-12

Ambiente: Windows `10.0.19045.0`, PowerShell `5.1.19041.6456`, Go
`go1.26.5 windows/386`. InspeÃ§Ã£o executada a partir da raiz do repositÃ³rio;
testes executados em `apps/desktop/libpinpadabecsgo`.

| Comando | CÃ³digo | Resultado |
| --- | ---: | --- |
| `Get-ChildItem -LiteralPath 'apps/desktop/libpinpadabecsgo/logs', 'logs' -Force \| Select-Object FullName,Length,LastWriteTime` | 0 | Arquivo do mÃ³dulo com 17.588 bytes, modificado Ã s 11:27:21; diretÃ³rio `logs` da raiz sem arquivos. |
| `Get-Content -Encoding UTF8 -LiteralPath 'apps/desktop/libpinpadabecsgo/logs/LogPinpadAbecs.txt' -Tail 25` | 0 | Encontrados TRACE, abertura, OPN, SPE GIX, PP, RSP GIX `000` e fechamento da execuÃ§Ã£o relatada Ã s 11:26. |
| `go version` | 0 | `go version go1.26.5 windows/386`. |
| `go test ./cmd/libpinpadabecsgo ./internal/infrastructure/logging ./internal/infrastructure/serial` | 0 | TrÃªs pacotes aprovados; resultados reutilizados pelo cache do Go. |

DiagnÃ³stico registrado em
`reviews/2026-09-12-logging-directory-diagnosis.md`: a consulta ocorreu em
outro diretÃ³rio. Nenhum cÃ³digo foi alterado e nenhuma nova operaÃ§Ã£o fÃ­sica
foi executada. Esta evidÃªncia confirma somente a persistÃªncia da execuÃ§Ã£o
relatada e nÃ£o substitui os gates pendentes da Change.

## RegressÃ£o da seleÃ§Ã£o MNU â€” 2026-09-12

Ambiente: Windows `windows/386`, Go `go1.26.5`, mÃ³dulo em
`D:\desenvolvimento\ia\lib-pinpad-abecs\apps\desktop\libpinpadabecsgo`.

O rastro fÃ­sico apresentado pelo operador contÃ©m status `000` e o valor
selecionado nos TLVs `80 4D 00 02 30 33` e `80 4D 00 02 30 31`. O erro do CLI
foi reproduzido com um executÃ¡vel compilado antes da atualizaÃ§Ã£o do parser.

| Comando | CÃ³digo | Resultado |
| --- | ---: | --- |
| `gofmt -w internal/application/service/service_test.go internal/domain/parser/advanced.go internal/domain/parser/advanced_response_test.go` | 0 | Arquivos formatados. |
| `go test ./internal/domain/parser ./internal/application/service -count=1` | 0 | Parser e serviÃ§o aprovados, incluindo `MNU000006` com TLV `0x804D` e seleÃ§Ã£o `03`. |
| `go vet ./internal/domain/parser ./internal/application/service` | 0 | Nenhum problema encontrado. |
| `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | ExecutÃ¡vel recompilado em 2026-09-12 17:34:14. |
| `go test ./... -count=1` | 0 | Todos os pacotes aprovados sem reutilizar cache. |
| `go vet ./...` | 0 | Nenhum problema encontrado. |

A validaÃ§Ã£o automatizada confirma a interpretaÃ§Ã£o da resposta fÃ­sica. A
confirmaÃ§Ã£o interativa no pinpad exige encerrar a instÃ¢ncia iniciada Ã s
17:30:49, abrir o executÃ¡vel recompilado e repetir a opÃ§Ã£o 9. Esta seÃ§Ã£o nÃ£o
altera os gates da Change.

## CorreÃ§Ã£o da serializaÃ§Ã£o GCX â€” 2026-09-12

Esta seÃ§Ã£o registra as evidÃªncias produzidas durante a implementaÃ§Ã£o da correÃ§Ã£o
do status `011`. Ela nÃ£o substitui a validaÃ§Ã£o independente nem altera o estado
da Change para `VALIDADA`.

Ambiente: Windows `windows/386`, Go `go1.26.5`, PowerShell, mÃ³dulo em
`D:\desenvolvimento\ia\lib-pinpad-abecs\apps\desktop\libpinpadabecsgo`.

O manual ABECS v2.12, seÃ§Ã£o 3.7.1, pÃ¡ginas 129 a 134, foi confrontado com o
rastro fÃ­sico apresentado. O cÃ³digo emitia primeiro um GCX com data e hora
`000000`, embora ambos os parÃ¢metros sejam obrigatÃ³rios, e montava o comando de
compra por concatenaÃ§Ã£o posicional. A correÃ§Ã£o removeu esse envio preliminar e
passou a serializar `SPE_AMOUNT` (`0x0013`, N12), `SPE_TRNDATE` (`0x0015`, N6),
`SPE_TRNTIME` (`0x0016`, N6) e `SPE_GCXOPT` (`0x0017`, N5) como parÃ¢metros
ABECS com identificador e comprimento binÃ¡rios.

| Comando | CÃ³digo | Resultado |
| --- | ---: | --- |
| `gofmt -w <arquivos Go alterados>` | 0 | Arquivos formatados. |
| `go test ./internal/domain/command ./internal/application/service -run 'TestBuildGCXCommand\|TestServiceAdditionalFacadeFlows' -count=1 -v` | 0 | Vetor GCX byte a byte, entradas invÃ¡lidas e uma Ãºnica escrita aprovados. |
| `go test ./internal/domain/command ./internal/application/service ./internal/domain/model ./cmd/libpinpadabecsgo -count=1` | 0 | Pacotes diretamente afetados aprovados. |
| `go test ./... -count=1` | 0 | Todos os pacotes aprovados sem reutilizar o cache. |
| `go vet ./...` | 0 | Nenhum problema encontrado. |
| `go test ./... -coverprofile=coverage-gcx` | 0 | Testes e perfil concluÃ­dos. |
| `go tool cover -func=coverage-gcx` | 0 | Cobertura total **81,0%**. |
| `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | ExecutÃ¡vel Windows recompilado em 2026-09-12 17:51:04; SHA-256 `D1B9E8B743A731FE833D165E50B1DEE4FEBA5779F251943B6B12FB3E0189AE43`. |
| `GOOS=linux GOARCH=amd64 go build ./...` | 0 | Build Linux amd64 aprovado. |
| `git diff --check` | 0 | Nenhum erro de whitespace; somente avisos informativos de futura normalizaÃ§Ã£o LF para CRLF. |

Para o vetor valor `000000010000`, data `120926`, hora `173800` e CTLS
desabilitado, o teste compara exatamente o prefixo `GCX045` e os quatro TLVs na
ordem normativa, terminando em `SPE_GCXOPT="00000"`. Com CTLS habilitado, o
valor Ã© `"10000"`. Datas, horas, valores e bits RUF invÃ¡lidos sÃ£o recusados
antes de qualquer escrita.

A regressÃ£o automatizada demonstra que a causa de formaÃ§Ã£o do pacote associada
ao status `011` foi corrigida. A confirmaÃ§Ã£o do status retornado pelo dispositivo
continua pendente da repetiÃ§Ã£o da opÃ§Ã£o 11 no pinpad fÃ­sico pela COM7.

## NotificaÃ§Ãµes e opÃ§Ãµes GCX â€” 2026-09-12

Esta seÃ§Ã£o registra evidÃªncias de implementaÃ§Ã£o e nÃ£o altera o estado da Change
para `VALIDADA`.

Ambiente: Windows `windows/386`, Go `go1.26.5`, PowerShell, mÃ³dulo em
`D:\desenvolvimento\ia\lib-pinpad-abecs\apps\desktop\libpinpadabecsgo` e
pinpad fÃ­sico PPP100 na `COM7`, baud rate 19200.

O rastro fÃ­sico das 22:50 registra ACK do GCX seguido por leituras redigidas de
1 e 44 bytes, sem linha `RSP` e com status estruturado vazio. Os 45 bytes formam
o tamanho exato de uma notificaÃ§Ã£o `NTM000032` enquadrada: 9 bytes de cabeÃ§alho,
32 de mensagem, SYN, ETB e CRC de 2 bytes. O manual ABECS v2.12 confirma nas
seÃ§Ãµes 2.2.2, 2.3.3 e 6.8.6 que comandos blocantes podem enviar notificaÃ§Ãµes
antes da resposta final e que a seleÃ§Ã£o da aplicaÃ§Ã£o gera essa notificaÃ§Ã£o.

| Comando | CÃ³digo | Resultado |
| --- | ---: | --- |
| `gofmt -w <arquivos Go alterados>` | 0 | Arquivos formatados. |
| `go test ./internal/domain/parser ./internal/domain/command ./internal/application/service ./cmd/libpinpadabecsgo -count=1` | 0 | Pacotes diretamente afetados aprovados. |
| `go test ./... -count=1` | 0 | Todos os pacotes aprovados. |
| `go vet ./...` | 0 | Nenhum problema encontrado. |
| `go test ./... -coverprofile=coverage-gcx-ntm` | 0 | Testes e perfil concluÃ­dos. |
| `go tool cover -func=coverage-gcx-ntm` | 0 | Cobertura total **81,0%**. |
| `go test <pacotes afetados> -run '<regressÃµes GCX/NTM>' -count=1 -v` | 0 | Quatro combinaÃ§Ãµes GCXOPT, escolhas invÃ¡lidas, parser NTM e sequÃªncia `ACK + NTM + NTM + GCX` aprovados. |
| `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | ExecutÃ¡vel Windows recompilado em 2026-09-12 22:59:46; SHA-256 `648F061A76B4745F68A1F9BA492CB4F9B3F5BDA39243D5C2AB0E4A4C96F2F897`. |
| `GOOS=linux GOARCH=amd64 go build ./...` | 0 | Build Linux amd64 aprovado. |

Os testes comprovam:

- `SPE_GCXOPT` igual a `00000`, `01000`, `10000` ou `11000`, conforme as
  escolhas de interface e visibilidade do valor;
- recusa de escolha invÃ¡lida antes de `PurchaseGCX`;
- reconhecimento estrutural de `NTM000` com mensagem entre 0 e 32 bytes;
- consumo de duas notificaÃ§Ãµes no mesmo chunk serial antes do `GCX000` final;
- somente uma escrita serial de comando durante toda a compra, pois respostas
  vÃ¡lidas do pinpad nÃ£o recebem ACK;
- retorno final com status `000` e tipo de cartÃ£o ICC no cenÃ¡rio de regressÃ£o.

A causa automatizÃ¡vel do `invalid pinpad response` foi corrigida. A confirmaÃ§Ã£o
fÃ­sica exige repetir a opÃ§Ã£o 11 com o executÃ¡vel recompilado; essa execuÃ§Ã£o deve
produzir uma ou mais linhas `PP` redigidas e, ao final, `RSP CMD=GCX
STATUS=<cÃ³digo>`.

## Prazo efetivo do GCX â€” 2026-09-12

Esta seÃ§Ã£o registra evidÃªncias da correÃ§Ã£o do `context deadline exceeded`
observado na opÃ§Ã£o 11 e nÃ£o altera o estado da Change para `VALIDADA`.

Ambiente: Windows `windows/386`, Go `go1.26.5`, PowerShell, mÃ³dulo em
`D:\desenvolvimento\ia\lib-pinpad-abecs\apps\desktop\libpinpadabecsgo` e
pinpad fÃ­sico PPP100 na `COM7`, baud rate 19200.

O rastro das 23:03 contÃ©m `SPE CMD=GCX` Ã s 23:03:19.351, ACK Ã s
23:03:19.360 e expiraÃ§Ã£o Ã s 23:03:21.110, sem frame de resposta. O contexto de
60 segundos havia sido criado antes das perguntas da opÃ§Ã£o 11, por volta de
23:02:21; a digitaÃ§Ã£o consumiu cerca de 58 segundos e deixou somente 1,76
segundo para apresentar o cartÃ£o. AlÃ©m disso, a fachada aplicava o timeout
genÃ©rico de 30 segundos sobre qualquer prazo maior fornecido pelo consumidor.

| Comando | CÃ³digo | Resultado |
| --- | ---: | --- |
| `gofmt -w cmd/libpinpadabecsgo/main.go internal/application/service/service.go internal/application/service/service_test.go` | 0 | Arquivos formatados. |
| `go test ./internal/application/service ./cmd/libpinpadabecsgo -run 'TestPurchaseGCXConsumesNotificationsAndPreservesCallerDeadline\|TestReadGCXOptions\|TestRunMenuRejectsInvalidGCXOption' -count=1 -v` | 0 | RegressÃµes GCX e opÃ§Ãµes do CLI aprovadas. |
| `go test ./... -count=1` | 0 | Todos os pacotes aprovados sem reutilizar cache. |
| `go vet ./...` | 0 | Nenhum problema encontrado. |
| `go test ./... -coverprofile=coverage-gcx-timeout -count=1` | 0 | Testes e perfil concluÃ­dos. |
| `go tool cover -func=coverage-gcx-timeout` | 0 | Cobertura total **81,2%**. |
| `GOOS=linux GOARCH=amd64 go build ./...` | 0 | Build Linux amd64 aprovado. |
| `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | ExecutÃ¡vel Windows recompilado em 2026-09-12 23:09:55; 4.408.832 bytes; SHA-256 `347C5CEB0F2AB25576208A3081F287664B90607FDED01D43EEF6E63203742E51`. |
| `git diff --check` | 0 | Nenhum erro de whitespace; somente avisos informativos de futura normalizaÃ§Ã£o LF para CRLF. |

A opÃ§Ã£o 11 agora coleta e valida modo de leitura, visibilidade, valor, data e
hora antes de criar seu contexto de 60 segundos. `SendGCXCommand` envia o
comando pela mesma fila e pelo mesmo worker, mas preserva diretamente o prazo
do consumidor em vez de aplicar o timeout genÃ©rico de `PinpadConfig`.

CA-GCX-013 usa a configuraÃ§Ã£o padrÃ£o de 30 segundos e chama `PurchaseGCX` com
um contexto de 60 segundos. O fake serial registra o deadline recebido pela
leitura e o teste exige mais de 50 segundos restantes, comprovando que o prazo
nÃ£o foi encurtado. A confirmaÃ§Ã£o fÃ­sica requer executar novamente a opÃ§Ã£o 11
com o binÃ¡rio recompilado e apresentar o cartÃ£o dentro dos 60 segundos que
comeÃ§am apÃ³s a Ãºltima entrada.

## Conformidade unitÃ¡ria ABECS 2.12 â€” 2026-09-13

**Estado de entrada:** `IMPLEMENTACAO_APROVADA`

**Ambiente:** Windows, PowerShell, `go1.26.5`, `GOOS=windows`, `GOARCH=386`.

**Manual:** *Pinpad Abecs â€” Protocolo de ComunicaÃ§Ã£o e Funcionamento*, versÃ£o
2.12 de 11-abr-2019.

| EvidÃªncia | Comando/cenÃ¡rio | CÃ³digo | Resultado |
| --- | --- | ---: | --- |
| VAL-ABECS-001 | `go test ./... -count=1` | 0 | Todos os 16 pacotes aprovados sem cache. |
| VAL-ABECS-002 | `go vet ./...` | 0 | Nenhum diagnÃ³stico. |
| VAL-ABECS-003 | `go test ./... -coverprofile=coverage-abecs212-final -count=1` | 0 | Perfil concluÃ­do. |
| VAL-ABECS-004 | `go tool cover -func=coverage-abecs212-final` | 0 | Cobertura total **81,6%**; command 87,9%, parser 89,2% e protocol 90,6%. |
| VAL-ABECS-005 | `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Build Windows aprovado; SHA-256 `E15A45C80A1250BEAD8B6ADC9052676301CD0FED51391759B58F9493F348387E`. |
| VAL-ABECS-006 | `GOOS=linux GOARCH=amd64 go build ./...` | 0 | Build Linux amd64 aprovado. |
| VAL-ABECS-007 | `go run golang.org/x/vuln/cmd/govulncheck@latest ./...` | 0 | Zero vulnerabilidades alcanÃ§Ã¡veis; foram informadas vulnerabilidades nÃ£o alcanÃ§Ã¡veis em dependÃªncias. |
| VAL-ABECS-008 | `go test -race ./... -count=1` | 1 | LimitaÃ§Ã£o de ambiente: `-race is not supported on windows/386`. |
| VAL-ABECS-009 | `go run golang.org/x/lint/golint@latest ./...` | 0 | Nenhum comentÃ¡rio exportado ausente; somente sugestÃµes de nomes preservados. |
| VAL-ABECS-010 | `git diff --check` | 0 | Nenhum erro de whitespace; avisos LF/CRLF sÃ£o informativos. |
| VAL-ABECS-011 | `go test ./internal/domain/command ./internal/domain/parser ./internal/domain/protocol ./internal/application/service -run 'ABECS212\|Published\|Exchange\|CancelHandshake\|TableLoad' -count=1 -v` | 0 | Vetores publicados, limites, enlace, CAN/EOT e TLR aprovados individualmente. |

### CenÃ¡rios comprovados

- OPN clÃ¡ssico, OPN seguro RSA e pacote AES-CBC da pÃ¡gina 170;
- CLO S32, CLX, GIX, DSP, DEX, MNU da pÃ¡gina 90 e todas as teclas GKY;
- GPN da pÃ¡gina 83, GTK da pÃ¡gina 86, GOX da pÃ¡gina 139 e comando FCX da
  pÃ¡gina 141;
- MLI/MLR/MLE/DSI com CRC16 e vetores publicados;
- TLI/TLR/TLE, continuaÃ§Ã£o apÃ³s status 000/020 e particionamento TLR por NREC e
  `CMD_LEN1 <= 999`; o PKTDATA TLR mÃ¡ximo Ã© 1005 e permanece abaixo de 1024;
- GCX com AAMMDD, quatro combinaÃ§Ãµes de GCXOPT, campos condicionais de resposta
  e retentativas/mudanÃ§a de interface CTLS;
- respostas ABECS com mÃºltiplos blocos N3 e rejeiÃ§Ã£o de campos obrigatÃ³rios,
  comprimentos, bits RUF, datas e BER-TLV invÃ¡lidos;
- enlace com ACK, NAK, trÃªs tentativas, NAK apÃ³s CRC invÃ¡lido, prazo CAN/EOT de
  2 segundos e nenhuma confirmaÃ§Ã£o enviada apÃ³s resposta vÃ¡lida;
- logging SPE/PP/RSP e redaÃ§Ã£o de PAN, trilhas, chaves, PIN block e KSN.

### Limite da evidÃªncia

Os testes comprovam serializaÃ§Ã£o, parsing, regras condicionais, estado e fluxo
contra o manual. Eles nÃ£o comprovam interoperabilidade de todos os comandos com
um equipamento, cartÃµes, kernels e tabelas reais.

**Resultado automatizado:** `VALIDADA`

**Estado da validaÃ§Ã£o integral da Change:** `PENDENTE_VALIDACAO_FISICA`

Para concluir a validaÃ§Ã£o integral, ainda Ã© necessÃ¡ria a matriz fÃ­sica dos
comandos aplicÃ¡veis. A limitaÃ§Ã£o do race detector deve ser reavaliada em
`windows/amd64` ou Linux, mas nÃ£o invalida a suÃ­te unitÃ¡ria executada.

## SeleÃ§Ã£o de trilhas GTK no utilitÃ¡rio local â€” 2026-09-13

**Estado de entrada:** `IMPLEMENTACAO_APROVADA`

**Ambiente:** Windows, PowerShell, `go1.26.5`, `GOOS=windows`, `GOARCH=386`.

O rastro fÃ­sico apresentou `GTK042` porque a opÃ§Ã£o 19 solicitava sempre o
mÃ©todo DUKPT `50` no Ã­ndice 02. Conforme a seÃ§Ã£o 3.3.12 do manual ABECS 2.12,
o status `042` informa chave MK/DUKPT ausente. A correÃ§Ã£o permite escolher o
retorno em claro, cujo comando Ã© `GTK000` e nÃ£o depende de chave, ou preservar
o retorno criptografado com DUKPT.

| EvidÃªncia | Comando/cenÃ¡rio | CÃ³digo | Resultado |
| --- | --- | ---: | --- |
| VAL-GTK-CLI-001 | `go test ./cmd/libpinpadabecsgo ./internal/domain/command ./internal/application/service -run 'TestReadGTKRequest\|TestGTK\|TestAdvancedFlows' -count=1 -v` | 0 | Modo claro, DUKPT, entradas invÃ¡lidas e contratos GTK aprovados. |
| VAL-GTK-CLI-002 | `go test ./... -count=1` | 0 | Todos os 16 pacotes aprovados sem cache. |
| VAL-GTK-CLI-003 | `go vet ./...` | 0 | Nenhum diagnÃ³stico. |
| VAL-GTK-CLI-004 | `go test ./... -coverprofile=coverage-abecs212-final -count=1` e `go tool cover -func=coverage-abecs212-final` | 0 | Cobertura total **81,5%**; CLI 62,4%, service 78,3%, command 87,9%, parser 89,2% e protocol 90,6%. |
| VAL-GTK-CLI-005 | `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | Build Windows aprovado; SHA-256 `B93505AF1F67EDCCDF91CF77B8651CD5C851E0F826CE826B2583201E571A6BD8`. |
| VAL-GTK-CLI-006 | `GOOS=linux GOARCH=386 go build -o build/libpinpadabecsgo-linux-386 ./cmd/libpinpadabecsgo` | 0 | Build Linux 386 aprovado; SHA-256 `0656930FB3681032E8EAB49D1CBD259F6D2398096E1411A58423921819AEABC3`. |

Os testes verificam byte a byte que a escolha 1 produz `GTK000`, sem Ã­ndice de
chave, e que a escolha 2 produz os TLVs `SPE_MTHDDAT=50`,
`SPE_TRACKS=1111` e `SPE_KEYIDX=02`. Modos invÃ¡lidos, Ã­ndice nÃ£o numÃ©rico e
Ã­ndice acima de 99 sÃ£o recusados antes da comunicaÃ§Ã£o serial.

**Resultado automatizado da correÃ§Ã£o:** `VALIDADA`

**ValidaÃ§Ã£o fÃ­sica da correÃ§Ã£o:** `VALIDADA`

O fluxo fÃ­sico de 20:43 registrou `GCX000`, `GTK000` em modo claro e a linha
`GTK_CLEAR`, sem o antigo status `042`. O frame serial permaneceu redigido.

## Registro das trilhas GTK em claro â€” 2026-09-13

**Estado de entrada:** `IMPLEMENTACAO_APROVADA`

**Ambiente:** Windows, PowerShell, `go1.26.5`, `GOOS=windows`, `GOARCH=386`.

| EvidÃªncia | Comando/cenÃ¡rio | CÃ³digo | Resultado |
| --- | --- | ---: | --- |
| VAL-GTK-LOG-001 | `go test ./cmd/libpinpadabecsgo ./internal/infrastructure/logging -run 'TestReadGTKRequest\|TestRecordGTKResult\|TestTracerRecordsGTKClearTracks' -count=1 -v` | 0 | SeleÃ§Ã£o, conteÃºdo, campos vazios, escaping e ausÃªncia no modo DUKPT aprovados. |
| VAL-GTK-LOG-002 | `go test ./... -count=1` | 0 | Todos os 16 pacotes aprovados sem cache. |
| VAL-GTK-LOG-003 | `go vet ./...` | 0 | Nenhum diagnÃ³stico apÃ³s correÃ§Ã£o do cancelamento no caminho de falha do tracer. |
| VAL-GTK-LOG-004 | `go test ./... -coverprofile=coverage-abecs212-final -count=1` e `go tool cover -func=coverage-abecs212-final` | 0 | Cobertura total **81,3%**; logging **88,8%**. |
| VAL-GTK-LOG-005 | `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | ExecutÃ¡vel Windows aprovado; SHA-256 `36DB8CAACE5B1735943BAF85561EFC16279F4B22B22811812874CBEB22F0F4A6`. |

A linha esperada no arquivo ativo possui o formato:

```text
[COM7#001] GTK_CLEAR TRACK1="<valor>" TRACK2="<valor>" TRACK3="<valor>" FUNC=cmd.libpinpadabecsgo.GTK DATA_HORA=<RFC3339Nano>
```

Os frames `SPE` e `PP` de GTK permanecem com `**REDACTED(<n> bytes)**`. A linha
`GTK_CLEAR` sÃ³ Ã© produzida apÃ³s resposta vÃ¡lida quando a escolha do CLI gera
`GTK000`; o modo DUKPT nÃ£o produz essa linha.

**Resultado automatizado da correÃ§Ã£o:** `VALIDADA`

**ValidaÃ§Ã£o fÃ­sica do conteÃºdo das trilhas:** `VALIDADA`

## DecodificaÃ§Ã£o BCD/nibble do GTK em claro â€” 2026-09-13

**Estado de entrada:** `IMPLEMENTACAO_APROVADA`

**Fonte normativa:** manual ABECS 2.12, seÃ§Ãµes 5.4.2.1 e 5.4.2.2, pÃ¡ginas
181â€“182.

| EvidÃªncia | Comando/cenÃ¡rio | CÃ³digo | Resultado |
| --- | --- | ---: | --- |
| VAL-GTK-BCD-001 | `go test ./internal/infrastructure/logging ./cmd/libpinpadabecsgo -run 'TestDecodeGTKClearNumericTrack\|TestTracerRecordsGTKClearTracks\|TestRecordGTKResult' -count=1 -v` | 0 | Vetor fÃ­sico, vetor solicitado, separador, filler, campos vazios, escaping e entradas invÃ¡lidas aprovados. |
| VAL-GTK-BCD-002 | `go test ./... -count=1` | 0 | Todos os 16 pacotes aprovados sem cache. |
| VAL-GTK-BCD-003 | `go vet ./...` | 0 | Nenhum diagnÃ³stico. |
| VAL-GTK-BCD-004 | `go test ./... -coverprofile=coverage-abecs212-final -count=1` | 0 | Cobertura total **81,4%**; logging **88,8%**. |
| VAL-GTK-BCD-005 | `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | ExecutÃ¡vel Windows aprovado; SHA-256 `08D46A0A73A3753493AAD13FDCEF7295337DA81C86AE94E4CC97BB9CE07B70B3`. |

O rastro fÃ­sico fornecido foi usado diretamente como orÃ¡culo:

```text
54 28 20 60 97 98 40 97 D2 11 12 01 38 29 95 58 46 37 0F
=> 5428206097984097=21112013829955846370
```

O nibble `D` foi convertido no delimitador `=` e o filler `F` final foi
removido. O vetor correspondente ao formato apresentado pelo operador tambÃ©m
foi validado:

```text
54 28 20 60 97 98 40 97 D1 12 23 36 65 5F
=> 5428206097984097=1122336655
```

**Resultado automatizado:** `VALIDADA`

**ConfirmaÃ§Ã£o no arquivo fÃ­sico:** `VALIDADA`

## CorreÃ§Ã£o da opÃ§Ã£o 20 GOX â€” 2026-09-13

**Estado de entrada:** `IMPLEMENTACAO_APROVADA`

**Ambiente:** Windows, PowerShell, `go1.26.5`, `GOOS=windows`, `GOARCH=386`.

| EvidÃªncia | Comando/cenÃ¡rio | CÃ³digo | Resultado |
| --- | --- | ---: | --- |
| VAL-GOX-CLI-001 | `go test ./cmd/libpinpadabecsgo ./internal/domain/command ./internal/application/service -run 'TestReadGOXRequest\|TestGOXAcquirerReferences\|TestGOXBuilder\|TestAdvancedFlows' -count=1 -v` | 0 | Contexto GCX, redes AID, PIN/WKENC, payload mÃ­nimo, vetor publicado e fluxo GCXâ†’GTKâ†’GOX aprovados. |
| VAL-GOX-CLI-002 | `TestRunMenuRejectsGOXWithoutEligibleGCXBeforeService` | 0 | AusÃªncia de GCX elegÃ­vel Ã© recusada antes do serviÃ§o. |
| VAL-GOX-CLI-003 | `go test ./... -count=1` | 0 | Todos os 16 pacotes aprovados sem cache. |
| VAL-GOX-CLI-004 | `go vet ./...` | 0 | Nenhum diagnÃ³stico. |
| VAL-GOX-CLI-005 | `go test ./... -coverprofile=coverage-abecs212-final -count=1` | 0 | Cobertura total **81,4%**; CLI **65,8%**, command **87,9%** e service **78,3%**. |
| VAL-GOX-CLI-006 | `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | ExecutÃ¡vel Windows aprovado; SHA-256 `B5F5668881A17019DAB9D04BDD18C4AA71A0877C8B936D873097EC37B7206207`. |

Para `PP_AIDTABINFO="080301"`, valor GCX `000000010000`, DUKPT TDES e Ã­ndice
07, o teste exige exatamente:

```text
GOX033
  0013 000C 303030303030303130303030
  0002 0001 33
  0009 0002 3037
  0010 0002 3038
```

O payload contÃ©m `SPE_ACQREF="08"`, derivado do GCX, e nÃ£o contÃ©m o antigo
valor fixo `01`. A confirmaÃ§Ã£o de que o pinpad deixa de devolver `011` depende
de repetir fisicamente GCX e GOX com uma chave PIN existente no Ã­ndice
escolhido.

**Resultado automatizado:** `VALIDADA`

**ConfirmaÃ§Ã£o fÃ­sica do GOX:** `VALIDADA`

## CorreÃ§Ã£o da opÃ§Ã£o 21 FCX e diagnÃ³stico do GOX047 â€” 2026-09-13

**Estado de entrada:** `IMPLEMENTACAO_APROVADA`

**Ambiente:** Windows, PowerShell, `go1.26.5`, `GOOS=windows`, `GOARCH=386`.

**Fonte normativa:** manual ABECS 2.12, seÃ§Ã£o 3.7.3, pÃ¡ginas 136â€“139, e seÃ§Ã£o
3.7.4, pÃ¡ginas 140â€“141.

| EvidÃªncia | Comando/cenÃ¡rio | CÃ³digo | Resultado |
| --- | --- | ---: | --- |
| VAL-FCX-CLI-001 | `go test ./cmd/libpinpadabecsgo ./internal/domain/command ./internal/infrastructure/logging ./internal/application/service -run 'TestReadGOXRequestMatchesSuccessfulPhysicalConfig\|TestReadFCXRequest\|TestRunMenuRejectsFCX\|TestFCXBuilder\|TestTracerRecordsOnlyNonSensitiveGOXConfig\|TestGOXAndFCXRejectInvalidConditionalFields\|TestAdvancedFlows' -count=1 -v` | 0 | Vetor fÃ­sico GOX, decisÃµes FCX, ARC A2, opcionais, sequÃªncia, payloads, vetor publicado, estado e diagnÃ³stico aprovados. |
| VAL-FCX-CLI-002 | `go test ./... -count=1` | 0 | Todos os 16 pacotes aprovados sem cache. |
| VAL-FCX-CLI-003 | `go vet ./...` | 0 | Nenhum diagnÃ³stico. |
| VAL-FCX-CLI-004 | `go test ./... -coverprofile=coverage-abecs212-final -count=1` e `go tool cover -func=coverage-abecs212-final` | 0 | Cobertura total **81,4%**; CLI **68,0%**, command **88,4%**, parser **89,2%** e protocol **90,6%**. |
| VAL-FCX-CLI-005 | `go build -o .\bin\libpinpadabecsgo.exe .\cmd\libpinpadabecsgo` | 0 | ExecutÃ¡vel Windows com 4.525.568 bytes; SHA-256 `4F305A0246D773A2637E6370F4B3E0910A69BD7EA77D0864811D7C15CC7A02C1`. |
| VAL-FCX-CLI-006 | `GOOS=linux GOARCH=amd64 go build ./...` | 0 | Build Linux amd64 aprovado. |
| VAL-FCX-CLI-007 | `git diff --check` | 0 | Nenhum erro de whitespace; avisos LF/CRLF sÃ£o informativos. |

O teste `TestReadGOXRequestMatchesSuccessfulPhysicalConfig` fixa byte a byte o
GOX que funcionou Ã s 17:43: valor `000000010000`, `SPE_MTHDPIN=3`,
`SPE_KEYIDX=02` e `SPE_ACQREF=04`. Portanto, a montagem atual nÃ£o divergiu
dessa execuÃ§Ã£o comprovada.

Os testes exigem os seguintes comandos mÃ­nimos:

```text
aprovaÃ§Ã£o: FCX014 001C 0002 <ARC-A2> 0019 0004 0000
negaÃ§Ã£o:   FCX014 001C 0002 <ARC-A2> 0019 0004 1000
falha:     FCX008 0019 0004 2000
```

`PP_FCXRES` (`8056`) nÃ£o aparece nos payloads de entrada. ApÃ³s resposta FCX
vÃ¡lida, o CLI exibe o campo retornado pelo pinpad. A elegibilidade GOX/FCX sÃ³ Ã©
alterada depois da validaÃ§Ã£o dos campos obrigatÃ³rios da resposta.

O rastro fÃ­sico fornecido comprova `GOX000` Ã s 17:43 com adquirente `04`, mÃ©todo
DUKPT TDES `3` e Ã­ndice `02`, seguido por `FCX047`. Ã€s 18:07 e 18:09, novas
execuÃ§Ãµes devolveram `GOX047` em poucos milissegundos. Como `047` nÃ£o estÃ¡ na
tabela da versÃ£o 2.12 e os frames sensÃ­veis anteriores estavam redigidos, o novo
binÃ¡rio passa a registrar antes do GOX:

```text
GOX_CONFIG ACQ=<N2> PIN_METHOD=<N1> KEY_INDEX=<N2>
```

Essa linha nÃ£o contÃ©m chave, PIN, PIN block, KSN, PAN, trilhas ou dados EMV e
permite comparar a prÃ³xima execuÃ§Ã£o com a configuraÃ§Ã£o fÃ­sica jÃ¡ comprovada.

**Resultado automatizado:** `VALIDADA`

### EvidÃªncia fÃ­sica da correÃ§Ã£o

O teste confirmado pelo operador na COM7 gerou, no arquivo ativo, a sequÃªncia:

```text
2026-09-13T20:43:39.6199468-03:00 RSP CMD=GCX STATUS=000
2026-09-13T20:43:48.9051234-03:00 RSP CMD=GTK STATUS=000
2026-09-13T20:44:01.7505529-03:00 GOX_CONFIG ACQ=04 PIN_METHOD=3 KEY_INDEX=02
2026-09-13T20:44:06.2239606-03:00 RSP CMD=GOX STATUS=000
2026-09-13T20:44:28.6915428-03:00 RSP CMD=FCX STATUS=000
```

Os frames GOX e FCX permaneceram integralmente redigidos. A configuraÃ§Ã£o GOX
coincide com o vetor fÃ­sico unitÃ¡rio e ambos os comandos terminaram com status
`000`. O operador confirmou o funcionamento apresentado pelo CLI, o que tambÃ©m
comprova que o parser aceitou o `PP_FCXRES` obrigatÃ³rio.

**ConfirmaÃ§Ã£o fÃ­sica do GOX e FCX corrigidos:** `VALIDADA`

A matriz fÃ­sica integral dos demais comandos da Change permanece pendente e
nÃ£o Ã© alterada por esta confirmaÃ§Ã£o especÃ­fica.

## ValidaÃ§Ã£o multimÃ­dia e ressincronizaÃ§Ã£o â€” 2026-09-14

**Estado de entrada:** `IMPLEMENTACAO_APROVADA` pela revisÃ£o
`reviews/2026-09-14-implementation-review-10.md`.

**Ambiente:** Windows, PowerShell, Go 1.26.5, `GOOS=windows`, `GOARCH=386`.

| EvidÃªncia | Comando/cenÃ¡rio | CÃ³digo | Resultado |
| --- | --- | ---: | --- |
| VAL-MM-001 | `go test ./...` | 0 | Todos os 16 pacotes passaram. Cobrem LMF vazio/vetor publicado, DMF plural, padding A8, MLI com tipo RUF, MLR/MLE, progresso sÃ³ depois de MLE000, timeout/CAN/EOT, resposta incompatÃ­vel, estado dessincronizado, Reset e opÃ§Ãµes 26/27. |
| VAL-MM-002 | `go test ./... -coverprofile=coverage` e `go tool cover -func=coverage` | 0 | Cobertura total: **81,8%**. ServiÃ§o: 80,2%; domÃ­nio command: 88,8%; parser: 89,6%. |
| VAL-MM-003 | `go vet ./...` | 0 | Nenhum diagnÃ³stico. |
| VAL-MM-004 | `go build ./...` | 0 | Todos os pacotes construÃ­dos para Windows/386. |
| VAL-MM-005 | `go run golang.org/x/vuln/cmd/govulncheck@latest -show verbose ./...` | 0 | Zero vulnerabilidades alcanÃ§Ã¡veis; scanner reportou dois avisos de pacote e sete de mÃ³dulo fora do call graph. AtualizaÃ§Ã£o do toolchain e de `golang.org/x/sys` fica recomendada em Change prÃ³pria. |
| VAL-MM-006 | `go test -race ./...` | 1 | NÃ£o executÃ¡vel neste ambiente: a distribuiÃ§Ã£o Ã© `windows/386`, combinaÃ§Ã£o em que Go informa que `-race` nÃ£o Ã© suportado. |
| VAL-MM-007 | `git diff --check` | 0 | Sem erros de whitespace; apenas avisos informativos de conversÃ£o LF/CRLF. |
| VAL-MM-008 | COM7: `go run ./cmd/libpinpadabecsgo`, opÃ§Ãµes `1 â†’ 26 â†’ 2 â†’ 0` | 0 | `OPN000`, `LMF000`; o pinpad listou `IMGALT01`, `QRCODE01` e `QRCODE02`; `CLO` concluiu. |
| VAL-MM-009 | COM7: `go run ./cmd/libpinpadabecsgo`, opÃ§Ãµes `1 â†’ 17 (QRCODE02) â†’ 2 â†’ 0` | 0 | `DSI000` em 8 ms e `CLO` concluÃ­do na mesma sessÃ£o. A resposta comprova aceitaÃ§Ã£o do comando; o rastro nÃ£o contÃ©m evidÃªncia visual dos pixels exibidos. |
| VAL-MM-010 | COM7: `go run ./cmd/libpinpadabecsgo`, opÃ§Ã£o `27` com `MISSING1;MISSING2`, apÃ³s confirmar ambos ausentes em LMF | 0 | `DMF000`; nomes desconhecidos foram ignorados e nenhuma mÃ­dia listada foi removida. |
| VAL-MM-011 | COM7: carga finalizada por MLE Ã s 09:01:26, com timeout e recuperaÃ§Ã£o do cÃ³digo novo | 1 | MLE recebeu ACK, mas nenhuma resposta em 10 s. O serviÃ§o enviou CAN Ã s 09:01:36, 09:01:38 e 09:01:40 sem EOT e protegeu a instÃ¢ncia. Um Reset manual repetiu trÃªs CAN Ã s 09:02:13, 09:02:16 e 09:02:18, tambÃ©m sem EOT. O timeout real e a proteÃ§Ã£o foram reproduzidos; a recuperaÃ§Ã£o apenas por CAN/EOT falhou no firmware. |
| VAL-MM-012 | COM7: recuperaÃ§Ã£o manual e DSI Ã s 09:03â€“09:05 | 0 | Close Ã s 09:03:32 e Open Ã s 09:03:34 restabeleceram o canal: o CAN inicial recebeu EOT e OPN retornou `000`. DSI de `QRCODE01` e `QRCODE02` retornou `000`. O operador informou que `QRCODE01` nÃ£o apareceu no display; para `QRCODE02`, foi fornecido o status `000`, sem confirmaÃ§Ã£o visual explÃ­cita. |
| VAL-MM-013 | MLI com tipo RUF e reconexÃ£o automÃ¡tica depois de ausÃªncia de EOT | â€” | Pendentes em hardware. O novo aditivo exige automatizar o caminho Close/Open comprovado em VAL-MM-012 e preservar o comando original sem reenvio. |

Os testes automatizados nÃ£o demonstram comportamento interno do firmware. O
log original mostra MLI/MLR com `000`, `MLE000` recebido tardiamente e respostas
DSI acumuladas; a listagem confirma que `QRCODE02` estÃ¡ armazenado. A nova
execuÃ§Ã£o reproduziu outro timeout real de MLE: o firmware nÃ£o respondeu aos seis
CAN enviados em duas sequÃªncias, mas respondeu ao CAN inicial depois que a porta
foi fechada e aberta. Isso comprova o caminho de recuperaÃ§Ã£o por reconexÃ£o e
motiva o aditivo aprovado em `reviews/2026-09-14-spec-review-4.md`.

As respostas `DSI000` comprovam que o firmware aceitou os nomes. Elas nÃ£o
permitem inferir renderizaÃ§Ã£o: o prÃ³prio operador informou ausÃªncia de imagem
visÃ­vel para `QRCODE01`. A confirmaÃ§Ã£o visual de `QRCODE02`, a reconexÃ£o
automÃ¡tica e a transmissÃ£o RUF permanecem pendentes.

**Resultado automatizado anterior:** `VALIDADA`. **ValidaÃ§Ã£o fÃ­sica parcial:**
OPN/LMF/aceitaÃ§Ã£o DSI/DMF de nomes ausentes/CLO e reproduÃ§Ã£o do timeout de MLE
`VALIDADOS`; renderizaÃ§Ã£o visual, reconexÃ£o automÃ¡tica e tipo RUF `PENDENTES`.

Como o aditivo de reconexÃ£o e evidÃªncia visual ainda nÃ£o foi implementado, a
validaÃ§Ã£o automatizada da versÃ£o revisada da SPEC volta a
`PENDENTE_IMPLEMENTACAO`.


## ValidaÃ§Ã£o da reconexÃ£o automÃ¡tica â€” 2026-09-15

**Ambiente:** Windows PowerShell, Go 1.26.5, GOOS=windows, GOARCH=386.

| EvidÃªncia | Comando | CÃ³digo | Resultado |
| --- | --- | ---: | --- |
| VAL-MM-014 | `go test ./...` | 0 | Todos os pacotes passaram. |
| VAL-MM-015 | `go test ./... -coverprofile=coverage.out` | 0 | Cobertura total registrada pelo perfil; serviÃ§o 80,9%. |
| VAL-MM-016 | `go vet ./...` | 0 | Nenhum diagnÃ³stico. |
| VAL-MM-017 | `go build ./...` | 0 | Build concluÃ­do. |
| VAL-MM-018 | `git diff --check` | 0 | Sem erros de whitespace; avisos LF/CRLF informativos. |
| VAL-MM-019 | `go run golang.org/x/vuln/cmd/govulncheck@latest -show verbose ./...` | 0 | Zero vulnerabilidades alcanÃ§Ã¡veis; hÃ¡ avisos em dependÃªncias/toolchain fora do call graph. |
| VAL-MM-020 | `go test -race ./...` | 1 | LimitaÃ§Ã£o objetiva: Go nÃ£o suporta race para windows/386. |

Os testes especÃ­ficos demonstram reconexÃ£o apÃ³s trÃªs CAN sem EOT, falhas parciais identificadas por etapa, ausÃªncia de reenvio do MLE/GIX e ausÃªncia de progresso final antes de MLE000. A revisÃ£o correspondente Ã© `reviews/2026-09-15-implementation-review-11.md` e resultou em `IMPLEMENTACAO_APROVADA`.

A validaÃ§Ã£o fÃ­sica da reconexÃ£o automÃ¡tica na COM7, do transporte RUF e da renderizaÃ§Ã£o visual apÃ³s DSI000 permanece pendente; DSI000 continua sendo tratado apenas como aceitaÃ§Ã£o protocolar.`r`n
## Alteração da opção 16 para QR Code gerado internamente — 2026-09-15

A opção 16 deixou de solicitar caminho de arquivo. Ela coleta texto, nome opcional e tamanho opcional; consulta GIX, escolhe a menor dimensão entre largura/altura quando o tamanho não é informado, exige display gráfico com PNG e retorna `ErrUnsupportedMedia` antes de MLI quando o pinpad é incompatível. O PNG é gerado pelo aplicativo e enviado por MLI/MLR/MLE.

`go test ./...` foi executado após a alteração, com código 0. A validação física da compatibilidade GIX e da renderização DSI permanece pendente na COM7.

## Correção de escopo — serviço RESTful na Change 066

A especificação de transporte foi alterada de WebSocket para API RESTful e permanece diretamente em `spec.md`, `DESIGN.md`, `proposal.md` e `tasks.md` da Change `2026-09-09-066-lib-pinpad-abecs-go`. Não existe Change separada para esse requisito.

### Revisão documental REST — 2026-09-15

O aditivo RESTful foi revisado em `reviews/2026-09-15-spec-review-rest.md` e recebeu `SPEC_APROVADA`. Esta evidência aprova o contrato, não a implementação: servidor, handlers, OpenAPI, testes `httptest` e validação por `curl` permanecem pendentes.
