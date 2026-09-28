# Validation — 072 Corrigir abertura, estado e log Android/Bridge

## Status

`VALIDADA`

A confirmação visual foi fornecida pelo operador após o fluxo físico: o display
mostrou `TESTE ANDROID` e `HOST COM10`. O valor continua sendo apenas evidência
do ambiente; a implementação lê exclusivamente `PORTA_PINPAD`.

A porta não é fixada neste documento nem na implementação. Cada execução lê
`PORTA_PINPAD` do processo. Na retomada de 28/09/2026, a variável persistente de
usuário foi a origem e seu valor efetivo era `COM10`; esse nome é evidência
temporal do ambiente, não configuração normativa.

## Evidências atuais da retomada em 28/09/2026

| Evidência | Procedimento | Resultado/código |
|---|---|---|
| VAL-072-R01 | Ler `[Environment]::GetEnvironmentVariable('PORTA_PINPAD','User')`, repassar ao processo e validar com `mode`/WMI | Origem `User`; valor efetivo obtido do ambiente; porta enumerada e válida, 0 |
| VAL-072-R02 | Go amd64: `go test -tags=integration ./... -count=1 -timeout=120s`, `go vet ./...`, `go build ./cmd/libpinpadabecsgo-bridge` após a revisão | Passou com `go1.26.5 windows/amd64`, 0 |
| VAL-072-R03 | Cobertura selecionada dos componentes alterados e `go tool cover -func` | Passou, 0; total selecionado 73,7% incluindo legado; `NewDiagnosticScriptedTransportForPort` 100% |
| VAL-072-R04 | Gradle/JDK17: `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest` após a revisão | `BUILD SUCCESSFUL`, 0 |
| VAL-072-R05 | AAR gomobile consumido pelo Gradle | SHA256 `C2AEB901A8E55562BA434F1B22CFB046B4E3516A09708246455D2722EDFC641D` |
| VAL-072-R06 | APK debug produzido e instalado | SHA256 `A9706790533E2686045CD260CEC5D305E4D8A171E24C4CCB78488866028FD4A4` |
| VAL-072-R07 | Instrumentação scripted no `emulator-5554`, recebendo `pinpadPort` a partir de `PORTA_PINPAD` | Ping, Open, GIX, DSP, Close e reabertura; 4 testes passaram; comando ADB 0 |
| VAL-072-R08 | BAT versionado em modo físico, com `PORTA_PINPAD` herdada, reverse `tcp:39100` e a mesma instrumentação | 4 testes passaram; Open, GIX, DSP, Close e reabertura concluídos; comando ADB 0 |
| VAL-072-R09 | `logs/LogPinpadAbecs.txt` após o fluxo físico | Cresceu em append; registrou `bridge_listening`, `ownership_acquired`, `session_acquired`, TX/RX redigidos, `session_released` e duas aberturas físicas `#001/#002` |
| VAL-072-R10 | Encerramento do Bridge pelo harness após a instrumentação | O fluxo já havia passado; encerramento forçado produziu código 1 no BAT/`0xffffffff` no processo, portanto não é evidência de shutdown gracioso |
| VAL-072-R11 | `go test -race` | Não suportado nesta sessão com `CGO_ENABLED=0` e sem compilador C; não declarado como aprovado |
| VAL-072-R12 | Auditoria atual e renderização visual do PDF de 3 páginas | Nenhum achado de segurança confirmado; PDF íntegro em `docs/security-audit/relatorio-072-corrigir-abertura-bridge-android.pdf` |
| VAL-072-R13 | Confirmação manual do operador sobre o display físico | Confirmado: `TESTE ANDROID` e `HOST COM10`; exit code não aplicável |

O log comprova que o Bridge abriu a porta recebida do ambiente, executou o
tráfego físico e liberou serial/ownership antes da reabertura. Como o protocolo
é opaco e o rastro é redigido, o log não substitui a observação visual do texto
`TESTE ANDROID` e da linha `HOST <porta efetiva>` no display.

## Evidências da tentativa histórica em 28/09/2026

| Evidência | Procedimento | Resultado/código |
|---|---|---|
| VAL-072-01 | `go test -tags=integration ./... -count=1 -timeout=120s` | Passou, 0 |
| VAL-072-02 | `go vet ./...` e `go build ./cmd/libpinpadabecsgo-bridge` | Passou, 0 |
| VAL-072-03 | Gradle `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest` com JDK 17 | Passou, 0 |
| VAL-072-04 | Validator Android em cópia de fontes sem `local.properties` | Passou, 0; o arquivo local permanece ignorado no projeto real |
| VAL-072-05 | AAR gomobile Go amd64/JDK17, SHA256 `DFE925694A06CB7D885A51A01D10268DBDA6EE3A22F53A49B4CF258089377C04` | Gerado, 0 |
| VAL-072-06 | Instrumentação scripted no `emulator-5554`: Ping, Open, GIX, DSP, Close e reabertura | Passou, 4 testes, 0 do comando ADB |
| VAL-072-07 | `PORTA_PINPAD` obtida do ambiente, modo físico, mesma instrumentação | Falhou em Open: `SERIAL_UNAVAILABLE:serial_open` |
| VAL-072-08 | `mode COM14` e enumeração WMI após falha | `COM14` inválida; dispositivos enumerados: COM1 e COM10 |
| VAL-072-09 | `LogPinpadAbecs.txt` após tentativa física | Cresceu e registrou `serial_open_failed`, `session_error` e correlação `072-physical-open`; sem payload/driver bruto |
| VAL-072-10 | `go test -race` | Não executado com sucesso: toolchain/sessão sem CGO; não declarar race aprovado |

Na tentativa histórica, o erro físico foi reproduzível como configuração de dispositivo inconsistente:
o Bridge permaneceu em loopback, o Ping não abriu COM, ownership foi liberado e
o log compartilhado recebeu a falha. A evidência não prova o DSP no pinpad.

## Ambiente da especificação

- Windows 10 / PowerShell, fuso America/Sao_Paulo, 27/09/2026.
- Workspace: `D:\desenvolvimento\ia\lib-pinpad-abecs`.
- `go version` da validação atual: `go1.26.5 windows/amd64`, comando com saída 0.
- Módulo declara `go 1.26.0`.
- ADB encontrado em `D:\desenvolvimento\ferramentas_android\Sdk\platform-tools\adb.exe`.
- Há alterações preexistentes no worktree; elas foram preservadas.

## Inspeção registrada

| Evidência | Comando/procedimento | Resultado/código |
|---|---|---|
| VAL-072-D01 | Get-Content/rg sobre fontes referenciadas em diagnostico.md | baseline inspecionado, 0 |
| VAL-072-D02 | Get-NetTCPConnection -LocalPort 39100 -State Listen -ErrorAction SilentlyContinue | nenhum listener listado; ausência capturada, não prova da causa original |
| VAL-072-D03 | adb -s emulator-5554 reverse --list | nenhuma linha, 0 |
| VAL-072-D04 | revisão de contratos 067–071 e shared/process/testing/security | SPEC_APROVADA; relatório em reviews/2026-09-27-spec-review.md |

Consultas D02/D03 são temporais e não são validação do comportamento corrigido.

## Verificação documental executada

- `VAL-072-D05`: verificador inline PowerShell com inventário por
  `rg --files specs/changes specs/README.md`, checagem `Test-Path` dos oito
  artefatos, resolução de links Markdown relativos, busca de whitespace final,
  contagem de critérios e gates de proposal/SPEC. Resultado:
  `OK: 30 documentos; 24 links locais; 8 artefatos obrigatorios; 15 criterios; gates SPEC_APROVADA.`
  Código de saída 0. Não executa código Go, ADB mutável ou testes físicos.
- `VAL-072-D06`: `git diff --check -- specs`, código 0. Git informa apenas
  normalização futura LF→CRLF em `specs/README.md`. Esse comando não cobre
  arquivos untracked; eles foram verificados pelo procedimento D05.

Os checks acima são a inspeção documental inicial e permanecem como histórico;
as execuções atuais estão registradas em VAL-072-R01 a VAL-072-R13.

## Matriz obrigatória após implementação

| VAL | Cenário | Asserção / critérios | Estado |
|---|---|---|---|
| VAL-072-01 | BAT com valores válidos herdados distintos, ausência/vazio e espaços nos caminhos | preservar ambiente; ausência falha; nenhum fallback indevido; CA-01/02 | executado |
| VAL-072-02 | build inválido, bind ocupado, destino inválido, shutdown | readiness só após bind e código real; fallback stderr; CA-03/06 | executado |
| VAL-072-03 | helper com ADB fake: zero/um/vários emuladores, offline, seleção explícita, porta customizada | reverse correto e aviso; sem escolha arbitrária; CA-04 | executado |
| VAL-072-04 | preflight indisponível, PONG e handshake inválido | PONG sem COM; orçamento único; falha segura; CA-05/07 | executado |
| VAL-072-05 | Bridge + tracer real e fake serial | arquivo cresce em Ping, BUSY, ownership falho, Open/Close; CA-06 | executado |
| VAL-072-06 | erros por fase e textos sintéticos sensíveis/de controle | código/ID preservados; sem payload/segredo/injeção; CA-07 | executado |
| VAL-072-07 | ViewModel: Ping, Version, Estado CLOSED, Open falho, endpoint trocado | sessão fiel; ações bloqueadas; causa preservada; CA-08/09 | executado |
| VAL-072-08 | Compose com catálogo e usuário no fim da lista | erro visível/acessível e resultado antigo limpo; CA-09 | executado |
| VAL-072-09 | read fatal enquanto peer ocioso, disconnect, close/cancel e reabertura | supervisor notificado; workers/serial/lock liberados; CA-10 | executado |
| VAL-072-10 | dois processos Windows no mesmo lock e mutex abandonado | exclusividade, thread correta, retorno controlado; CA-11 | executado |
| VAL-072-11 | Emulator → AAR → Bridge scripted | Open/GIX/DSP/Close e reabrir; log real de eventos; CA-12 | executado |
| VAL-072-12 | Emulator → Bridge físico na porta indicada por `PORTA_PINPAD` | Open/GIX/DSP/Close/reabertura e confirmação visual passaram; CA-13 | validado |
| VAL-072-13 | regressão Go/Android e proveniência AAR/APK | build/testes/log redigido e hashes; CA-14 | executado |
| VAL-072-14 | revisão documental de changes e execução/runbook | precedência e limites coerentes; CA-15 | executado |

## Comandos de verificação previstos

No módulo Go, registrar executável/arquitetura/versão realmente usados:

```powershell
go version
go test ./...
go vet ./...
go build ./cmd/libpinpadabecsgo-bridge
go test ./... -coverprofile=coverage.out
go tool cover -func=coverage.out
go test -race ./...
```

`-race` requer ambiente compatível; se indisponível, registrar motivo/código e
limitação, sem declarar sucesso. Inventariar produção Go alterada e testes
relacionados; métricas com escopo e exclusões. Testes multiprocesso Windows
não serão substituídos por mock/in-memory. Não executar a operação física só
para aumentar cobertura ou repetir compra/PIN.

No Android, sem alterar versões por esta SPEC:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Rebuild AAR se fachada/transporte/core bindado mudar, pelo script existente
`scripts/build-mobile-aar.ps1`; registrar comando/flags realmente necessários
e hash do AAR consumido pelo Gradle. Registrar hash do APK instalado e versão
reportada pelo app, não apenas APK produzido em outro diretório.

Teste físico deve usar o BAT corrigido e PORTA_PINPAD real, executar Ping e
Abrir → GIX → DSP com texto de teste → Fechar → Abrir → Fechar. Documentar PID,
bind, COM/baudrate, reverse/emulador, modelo do pinpad, firmware público,
operação/correlação, linhas seguras do log e confirmação visual DSP.
`Get-NetTCPConnection`, existência do log e Device Manager são pré-requisitos,
não substitutos do fluxo. Contaminação por scripted reprova o gate físico.

## Segurança e encerramento

A auditoria atual foi executada e o PDF foi gerado/renderizado. Não houve
achado de segurança confirmado em aberto. A confirmação visual foi registrada
separadamente como evidência manual, sem substituir o rastro técnico.

Registrar ambiente, comando/procedimento, resultado, código de saída quando
aplicável e caminho das evidências em cada VAL. Teste manual deve informar
`não aplicável` para exit code em vez de inventar zero.
A aprovação formal e o encerramento desta Change ainda dependem do relatório de
aprovação e do commit controlado.
