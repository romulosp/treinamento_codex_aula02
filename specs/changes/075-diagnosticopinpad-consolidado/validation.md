# Validation — 075 diagnosticopinpad consolidado
 
## Status
 
`IMPLEMENTADA — TESTES TÉCNICOS EXECUTADOS; VALIDAÇÃO INDEPENDENTE PENDENTE`
 
O código foi implementado e testado contra a primeira revisão da SPEC. Após a
correção da consolidação 072/074, essas evidências permanecem históricas e não
comprovam conformidade com o contrato atual. A implementação deve ser
reconciliada. Essa execução ocorreu em 2026-10-03 e está registrada abaixo;
os registros anteriores continuam históricos.

## Evidências técnicas novas — 2026-10-03

Execução de implementação conforme `implementar-mudanca-para-teste`, sem
revisão de implementação, validação independente, aprovação, archive ou commit.

Ambiente: Windows PowerShell; JDK `C:\Desenvolvimento\jdk-17.0.11`;
Maven `C:\Desenvolvimento\apache-maven-3.8.8` disponível no PATH dos gates finais,
sem uso de Maven; SDK `D:\desenvolvimento\ferramentas_android\Sdk`; NDK
`28.2.13676358`; Emulator `Medium_Tablet`, API 35, `emulator-5554`.
Go de testes: `go1.26.5 windows/386` no PATH original. Go do AAR:
`go1.26.5 windows/amd64`, extraído temporariamente em
`%TEMP%\diag075-go-amd64-tar\go`, sem alterar PATH persistente.

| ID | Comando / evidência | Resultado | Saída |
|---|---|---|---:|
| REC-075-01 | `go test -count=1 -tags=integration ./...` | todos os pacotes passaram, incluindo cleanup/read fatal/ownership/launchers/preflight | 0 |
| REC-075-02 | `go vet ./...` | passou | 0 |
| REC-075-03 | `go build ./...` | passou | 0 |
| REC-075-04 | `scripts/build-mobile-aar.ps1 -AllowDirty`, Go amd64 | AAR novo gerado, proveniência registrada | 0 |
| REC-075-05 | extração AAR + `javap` JDK 17 | arm64-v8a, armeabi-v7a, x86 e x86_64; minSdk 26; helpers de erro presentes | 0 |
| REC-075-06 | `testDebugUnitTest lintDebug assembleDebug connectedDebugAndroidTest` com argumentos quoted `-Pandroid.testInstrumentationRunnerArguments.bridgeMode=scripted` e `pinpadPort=COM7` | 20 JVM e 6 instrumentados; zero falhas/erros/skips; lint/build passaram | 0 |
| REC-075-07 | validador Android em cópia temporária de fontes | invariantes atendidos; omitidos apenas builds/IDE/libs/local.properties ignorados | 0 |
| REC-075-08 | `adb -s emulator-5554 reverse tcp:39100 tcp:39100` e `reverse --list` | reverse confirmado; listener real em 127.0.0.1:39100, PID 21568, modo scripted | 0 |
| REC-075-09 | teste JNI `openInfoDisplayCloseAndReopenThroughRealBinding` | Ping não abriu sessão; Open/GIX/DSP/Close/reabertura concluídos | 0 |
| REC-075-10 | `go test -tags=integration -coverpkg=./... -coverprofile=build/075-coverage-full.out ./...` + `go tool cover -func=...` | cobertura agregada Go 80,1%; alvo 90% não atingido | 0 |

AAR final: SHA-256
`C002DD8C48AF23F2088011603F805C5695881010A51CE80490BAAE46CE706AEF`.
Proveniência local: `build/mobile-aar-provenance.json`, revision `f372cf3`,
dirty `true`, x/mobile fixado na versão da SPEC. `Mobile.version()` foi chamado
por teste JNI real e confirmou versão/revision/dirty.

Os testes usam COM7 apenas como rótulo do transcript scripted herdado do
ambiente; nenhum hardware foi aberto. O log em
`%TEMP%\diag075-scripted-20261003.txt` registra `ping_ok` com ID
`072-scripted-ping`, Acquire/Open com `072-scripted-open` e liberações finais
com `072-scripted-close`/`072-scripted-reclose`. Os prefixos 072 são IDs do
teste histórico reutilizado e não identificam a Change de evidência.

Falhas encontradas e resolvidas: Go host 386 incompatível com NDK x86_64;
frame interrompido classificado como EOF depois do header; instalação APK
antes de o boot do Emulator completar; argumentos `-P`/`-coverprofile` divididos
pelo PowerShell sem aspas. A primeira instrumentação reportou build zero com
erro de instalação; esse resultado **não** foi considerado aprovação.
Os XMLs finais comprovam seis testes e zero falhas.

O validador direto rejeita `local.properties` mesmo ignorado pelo Git. Esse
arquivo local do usuário foi preservado; a cópia de fontes usa o mesmo
validador e está explicitamente identificada como tal. A cobertura por pacote
sem `-coverpkg` foi 78,2%; a cobertura agregada de todos os testes, incluindo
chamadas entre pacotes, foi 80,1%. Não há percentual Kotlin aferido.

Limitações: hardware físico e race detector não executados; Kotlin coverage,
rotação/saída/cancelamento durante operação na UI permanecem para avaliação.
Avisos de depreciação do runner/Unsafe das dependências não impediram os testes.

Correções: enums independentes; classificação via Mobile; QR condicionado à
sessão; foco e live region; log Android interno allowlist; validação UTF-8/128
bytes do ID; normalização remota; fase Ping e correlação Close; saída não
encerra Activity quando Close falha; scripts/README/proveniência corrigidos.

Ao final, o Bridge scripted criado para os testes (PID 21568) foi encerrado
para liberar a porta 39100. O Emulator e o reverse configurado permanecem
disponíveis para a avaliação humana. O log temporário e os artefatos foram
preservados.
 
## Complemento técnico de lifecycle e cobertura — 2026-10-03

Ambiente Java/Maven/SDK preservado conforme REC-075; AAR mantido com o mesmo
SHA-256. Testes de Activity executados no `emulator-5554`, API 35.

| ID | Comando/evidência | Resultado | Saída |
|---|---|---|---:|
| LIFE-075-01 | `gradlew.bat tarefaInexistenteParaVerificarCodigo` | falha proposital confirma propagação do código do Gradle | 1 esperado |
| LIFE-075-02 | `gradlew.bat createDebugUnitTestCoverageReport lintDebug assembleDebug` | 27 JVM aprovados; relatório criado; lint/APK passaram | 0 |
| LIFE-075-03 | `connectedDebugAndroidTest` com filtro `DiagnosticLifecycleTest` | dois testes passaram: recriação/Cancel/Close e exit durante operação | 0 |
| LIFE-075-04 | `connectedDebugAndroidTest` com bridgeMode=scripted, pinpadPort=COM7 | oito testes; zero falhas/erros/skips, XML conferido | 0 |
| LIFE-075-05 | validador na cópia de fontes, preservando local.properties do usuário | invariantes atendidos | 0 |

Os argumentos `-Pandroid.testInstrumentationRunnerArguments.*` foram passados
entre aspas no PowerShell. Em LIFE-075-04, listener scripted PID 28460 em
127.0.0.1:39100, reverse previamente confirmado, log temporário
`%TEMP%\diag075-lifecycle-scripted.txt`. O modo não abre hardware físico.

Foi corrigido um defeito real de `gradlew.bat`: `endlocal` descartava o código
da JVM, permitindo saída zero após build falho. Os registros anteriores
continuam suportados pelos XMLs/logs, mas código zero do wrapper antigo sozinho
não prova aprovação. A nova verificação negativa retornou 1 como esperado.

Também foi corrigida a opção Sair, antes desabilitada enquanto ocupado.
Agora o teste instrumentado aciona essa opção durante I/O pendente e comprova
Cancel/Close antes de a Activity terminar. O teste de recriação conserva o
mesmo ViewModel, ID ativo e uma única chamada ao repository.

A injeção do fake ocorre exclusivamente no ViewModelStore do teste; nenhuma
API de produção ou DI foi ampliada. Há sete testes JVM adicionais de Cancel,
Close, saída, falha Close, onCleared, GetInfo e rejeição de entradas.

Cobertura: ViewModel 267/278 linhas (96,04%); escopo JVM inventariado 385/396
(97,22%); relatório do módulo inteiro sob JVM 388/736 (52,72%). Consulte
`coverage-inventory.md`; cobertura integrada Android/JNI ainda pendente.
Configuração baseada na [documentação oficial do AGP](https://developer.android.com/studio/test/coverage-report).

Tentativas intermediárias não promovidas a sucesso: teste inicial com
pressuposto de limite de texto não implementado, seletor de item LazyColumn
fora da composição e import inválido no teste. Foram corrigidos no teste.
Execução sem modo Bridge retornou zero do runner mas representou uma
AssumptionViolatedException como falha no XML; a execução final usou scripted
e confirmou XML sem falhas. Isso reforça a conferência de XML além do código.

O Bridge scripted PID 28460 foi encerrado após os testes para liberar a porta;
artefatos, log temporário, Emulator e reverse foram preservados.

## Evidências históricas da implementação anterior
 
As fontes arquivadas 068 e 069 permanecem referências históricas. Seus testes
não são promovidos automaticamente a evidências novas da Change 075.
 
Ambiente em 2026-09-30:
 
- Windows PowerShell;
- Java `C:\Desenvolvimento\jdk-17.0.11`;
- Maven disponível em `C:\Desenvolvimento\apache-maven-3.8.8`, não utilizado
  porque o projeto Android usa Gradle Wrapper;
- Android SDK `D:\desenvolvimento\ferramentas_android\Sdk`;
- Emulator `emulator-5554`, `Medium_Tablet` API 35;
- Go `go1.26.5 windows/amd64`.
 
| Evidência | Diretório/comando | Resultado | Código |
|---|---|---|---:|
| IMP-075-001 | `go test -count=1 ./...` no módulo Go | aprovado | 0 |
| IMP-075-002 | `go test -count=1 -tags=integration ./...` no módulo Go | aprovado | 0 |
| IMP-075-003 | `go vet ./...` no módulo Go | aprovado | 0 |
| IMP-075-004 | `go build ./...` no módulo Go | aprovado | 0 |
| IMP-075-005 | validador estrutural Android | invariantes atendidos | 0 |
| IMP-075-006 | `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug --offline` | aprovado | 0 |
| IMP-075-007 | `:app:connectedDebugAndroidTest --offline` no `emulator-5554` | aprovado | 0 |
 
Os comandos Gradle foram executados com `JAVA_HOME` apontando para o JDK
informado, `MAVEN_HOME` disponível apenas no ambiente e `ANDROID_HOME`
apontando para o SDK informado. O aviso de depreciação de `createComposeRule`
não impediu a execução; a revisão posterior deve avaliar a migração para a API
v2.
 
## Limitacao de regeneracao do AAR
 
O script build-mobile-aar.ps1 -AllowDirty foi executado com o SDK explicito,
mas terminou com codigo 1 porque o gomobile procurou clang.exe especifico
para ARM no NDK local, enquanto a instalacao disponivel fornece wrappers CMD.
O AAR local pre-existente foi consumido pelos gates Gradle. Esta limitacao
impede declarar a regeneracao reproduzivel como validada neste ambiente.
SHA-256 do AAR consumido: C2AEB901A8E55562BA434F1B22CFB046B4E3516A09708246455D2722EDFC641D.
 
## Alteracoes implementadas
 
- host padrao alinhado a SPEC para localhost, mantendo 10.0.2.2 como override;
- I/O de logging e fechamento do repository deslocados para o dispatcher de IO;
- DiagnosticScreenContent recebeu Modifier no limite publico;
- fontes do projeto diagnostico passaram a ser versionaveis; AAR, builds, IDE
  e local.properties continuam ignorados.
 
## Gates da revisão anterior
 
| Gate | Estado |
|---|---|
| P0 - dependencias Go/Bridge | evidencia tecnica executada |
| 1 - Go para AAR | AAR existente consumido; regeneracao limitada pelo NDK |
| 2 - AAR para Kotlin | aprovado pelos gates Gradle |
| 3 - Emulator para Bridge | pendente de execucao end-to-end com adb reverse |
| 4 - fluxo scripted sem hardware | pendente de execucao end-to-end |
| 5 - pinpad fisico | nao executado; nao simular |
| 6 - falhas e lifecycle | cobertura automatizada executada; revisao formal pendente |
 
## Próximo passo após teste humano
 
A reconciliação e os testes técnicos foram executados em 2026-10-03.
Realizar avaliação humana, incluindo as pendências listadas nas evidências novas.
Somente depois da autorização explícita, usar `executar-mudanca-spec-driven`
para revisão e validação independente da mesma Change. Archive/commit seguem
dependentes de aprovação formal.
