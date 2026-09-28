# Validation — 072 Corrigir abertura, estado e log Android/Bridge

## Status

`SPEC_APROVADA` — implementação não iniciada.

Este arquivo planeja validação e registra inspeção documental. Nenhum código
da 072 foi implementado, nenhum APK corrigido foi instalado e nenhum fluxo
COM14 foi declarado aprovado nesta etapa.

## Ambiente da especificação

- Windows 10 / PowerShell, fuso America/Sao_Paulo, 27/09/2026.
- Workspace: `D:\desenvolvimento\ia\lib-pinpad-abecs`.
- `go version`: `go1.26.5 windows/386`, comando com saída 0. A implementação
  deve registrar também o Go efetivo de build; não presumir amd64 a partir de
  evidência antiga feita com toolchain isolado.
- Módulo declara `go 1.26.0`.
- ADB encontrado em `D:\desenvolvimento\ferramentas_android\Sdk\platform-tools\adb.exe`.
- Há alterações preexistentes no worktree; esta etapa altera somente specs.

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

Os checks acima validam a entrega documental. Todos os testes de implementação
abaixo continuam não executados, incluindo COM14.

## Matriz obrigatória após implementação

| VAL | Cenário | Asserção / critérios | Estado |
|---|---|---|---|
| VAL-072-01 | BAT com COM14, outra COM, ausência/vazio, espaços nos caminhos | preservar ambiente; ausência falha; nenhum fallback indevido; CA-01/02 | não executado |
| VAL-072-02 | build inválido, bind ocupado, destino inválido, shutdown | readiness só após bind e código real; fallback stderr; CA-03/06 | não executado |
| VAL-072-03 | helper com ADB fake: zero/um/vários emuladores, offline, seleção explícita, porta customizada | reverse correto e aviso; sem escolha arbitrária; CA-04 | não executado |
| VAL-072-04 | preflight indisponível, PONG e handshake inválido | PONG sem COM; orçamento único; falha segura; CA-05/07 | não executado |
| VAL-072-05 | Bridge + tracer real e fake serial | arquivo cresce em Ping, BUSY, ownership falho, Open/Close; CA-06 | não executado |
| VAL-072-06 | erros por fase e textos sintéticos sensíveis/de controle | código/ID preservados; sem payload/segredo/injeção; CA-07 | não executado |
| VAL-072-07 | ViewModel: Ping, Version, Estado CLOSED, Open falho, endpoint trocado | sessão fiel; ações bloqueadas; causa preservada; CA-08/09 | não executado |
| VAL-072-08 | Compose com catálogo e usuário no fim da lista | erro visível/acessível e resultado antigo limpo; CA-09 | não executado |
| VAL-072-09 | read fatal enquanto peer ocioso, disconnect, close/cancel e reabertura | supervisor notificado; workers/serial/lock liberados; CA-10 | não executado |
| VAL-072-10 | dois processos Windows no mesmo lock e mutex abandonado | exclusividade, thread correta, retorno controlado; CA-11 | não executado |
| VAL-072-11 | Emulator → AAR → Bridge scripted | Open/GIX/DSP/Close e reabrir; log real de eventos; CA-12 | não executado |
| VAL-072-12 | Emulator → Bridge físico COM14 | Open/GIX/DSP/Close e reabrir; DSP observado no pinpad; CA-13 | não executado |
| VAL-072-13 | regressão Go/Android e proveniência AAR/APK | build/testes/log redigido e hashes; CA-14 | não executado |
| VAL-072-14 | revisão documental de changes e execução/runbook | precedência e limites coerentes; CA-15 | planejado |

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

A entrega atual é exclusivamente documental: auditoria de implementação e
PDF de segurança não se aplicam nesta etapa. Implementação posterior exige
auditoria atual com escopo/achados/evidências, sem reaproveitar relatório
histórico como aprovação.

Registrar ambiente, comando/procedimento, resultado, código de saída quando
aplicável e caminho das evidências em cada VAL. Teste manual deve informar
`não aplicável` para exit code em vez de inventar zero.
Sem revisão da implementação, validação formal, teste físico e aprovação não
arquivar, atualizar system como entregue ou preparar commit de encerramento.
