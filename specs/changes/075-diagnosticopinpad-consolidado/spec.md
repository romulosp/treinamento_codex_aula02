# SPEC — 075 diagnosticopinpad consolidado

## 1. Status, fontes e precedência

`SPEC_APROVADA`

Esta SPEC consolida integralmente o estado final das Changes arquivadas 068 e
069. Como ambas incorporam correções da 072, esta revisão inclui todos os
efeitos observáveis da 072 aplicáveis ao laboratório. A Change 074 é a baseline
normativa do stack Go/Bridge e já consolida 066, 067, 070, 071 e 072.

Fontes históricas diretas:

- `specs/archive/2026-09-27-068-diagnosticopinpad/spec.md` e `DESIGN.md`;
- `specs/archive/2026-09-27-069-diagnosticopinpad-functional-lab/spec.md` e
  `DESIGN.md`.

Dependência normativa:

- `specs/changes/074-pinpad-android-bridge-stack/spec.md` e `DESIGN.md`.

Precedência: workflow/`AGENTS.md` → esta SPEC para comportamento do produto →
074 para infraestrutura e protocolo → fontes históricas → evidências. A 075 é
autocontida para implementar o aplicativo, mas não duplica o contrato interno
completo do core, REST, serial, ownership, launcher ou tracer da 074.

## 2. Produto, perfil e estrutura

O produto é um laboratório Android nativo de diagnóstico, não um aplicativo de
produção para pagamentos. O perfil é `STANDARD`, em um único módulo `app`, com
vários fluxos, formulários, diálogos e estado coordenado.

Projeto:

`apps/frontend/smartphone/diagnosticopinpad`

Contrato estrutural:

- namespace e `applicationId` `br.com.romulopenha.diagnosticopinpad`;
- Kotlin, Jetpack Compose e Material 3;
- uma Activity e cinco grupos: conexão, display/teclas,
  captura/transação, EMV/PIN e multimídia/tabelas;
- UDF com `StateFlow<DiagnosticUiState>` e ViewModel;
- repository Kotlin como única fronteira do AAR;
- DI manual; sem Hilt, Koin, banco, Navigation ou domain layer artificial.

## 3. Invariantes normativos

1. O core Go é a única fonte de verdade do protocolo, comandos e sessão ABECS.
2. Kotlin não monta ABECS, calcula CRC, interpreta frame raw ou decide regra do
   pinpad.
3. O Bridge transporta bytes opacos e não executa regra ABECS.
4. Android ↔ Bridge usa exclusivamente `PBRG` v1 binário, big-endian, payload
   raw e limite de 1 MiB.
5. JSON só pode ser saída nomeada, versionada, tipada e redigida; nunca framing
   ou entrada genérica.
6. Trabalho bloqueante ocorre fora da thread principal.
7. Cada operação usa `operationID` novo, não vazio e de até 128 bytes UTF-8.
8. Há no máximo uma operação mutável ativa por cliente e não existe retry
   automático de resultado indeterminado.
9. Cancelamento alcança o contexto Go e o lifecycle correspondente.
10. Estado de ação, conectividade do Bridge e sessão Go são distintos.
11. Ping e Version nunca abrem COM, enviam ABECS ou promovem sessão a `OPEN`.
12. Open só confirma `OPEN` após o fluxo Go/ABECS, incluindo OPN válido.
13. O Bridge escuta em loopback e o Android nunca recebe configuração COM.
14. `PORTA_PINPAD` é lida somente pelo processo Windows e a porta efetiva é a
    mesma no transporte serial e no ownership.
15. PAN, trilhas, PIN block, KSN, chaves, EMV bruto, payload raw, bytes de mídia
    e segredos não aparecem em UI, estado, log Android ou resultados públicos.
16. O app não solicita USB Host/OTG nem armazenamento amplo e não persiste dados
    de negócio ou parâmetros sensíveis.

## 4. Configuração, readiness e preflight

### 4.1 Endpoint Android

| Campo | Default | Regra |
|---|---:|---|
| host | `localhost` | não vazio; origem exibida |
| porta TCP | `39100` | inteiro de 1 a 65535 |
| timeout | `10000 ms` | de 100 a 120000 ms |

O fluxo principal usa `adb reverse tcp:<porta> tcp:<porta>`. `10.0.2.2` é
somente override explícito. A tela mostra a porta TCP, nunca a COM. O app não
executa ADB nem conclui, por conexão recusada isolada, se faltou reverse ou
processo host.

O helper/launcher da 074 seleciona um único Emulator online ou exige serial
explícito em caso de ambiguidade, verifica o reverse e relata
`emulator_not_prepared`. Reverse configurado não prova listener, COM ou OPN.

### 4.2 Configuração Windows herdada da 074

- `testar_bridge_pinpad.bat` e o entrypoint direto exigem `PORTA_PINPAD` herdada
  e falham antes da serial quando ausente, vazia, composta apenas por espaços
  ou inválida;
- não existe COM fixa ou fallback em `DefaultConfig`, `config.Load()` ou
  launcher físico;
- baudrate, timeout, porta Bridge e log seguem a precedência/defaults da 074;
- a origem `environment` e a porta efetiva são observáveis sem revelar dados
  sensíveis.

### 4.3 Readiness e preflight Open

`bridge_listening` só existe depois de `net.Listen` bem-sucedido e significa
apenas servidor TCP pronto. Não comprova COM, ownership, sessão ou OPN.

Antes de Open explícito com cliente fechado, o app executa Ping controlado
dentro do orçamento da própria ação. Se Ping falhar:

- não inicia Acquire nem abre serial;
- mantém sessão fechada;
- apresenta categoria/fase e orientação para Bridge, endpoint e reverse;
- permite nova tentativa explícita após correção do ambiente.

## 5. Catálogo funcional consolidado

| Opção | Ação | Contrato público |
|---:|---|---|
| 1 | Abrir conexão | `Open(operationID)` com preflight |
| 2 | Fechar conexão | `Close(operationID)` |
| 3 | Estado atual | `GetState()` |
| 4 | Informações | `GetInfoJSON(operationID)` |
| 5 | Reset rápido | `Reset(operationID)` via CAN/EOT; nunca comando RST |
| 6 | Indisponível | visível e desabilitada; sem chamada ao dispositivo |
| 7 | Mensagem fixa | `DisplayDSP(operationID, line1, line2)` |
| 8 | Mensagem estendida | `DisplayDEX(operationID, message)` |
| 9 | Menu interativo | `DisplayMNU(operationID, timeout, title, options)` |
| 10 | Aguardar tecla | `WaitForKeyPress(operationID, timeoutSeconds)` |
| 11 | Compra GCX | `PurchaseGCXSummaryJSON(operationID, amount, date, clock, ctls, hideAmount)` |
| 12 | GIX resumido | `GetInfoRawSummaryJSON(operationID)`; nunca bytes raw |
| 13 | Capacidades do display | `GetDisplayCapabilitiesJSON(operationID)` |
| 14 | Sessão segura | `OpenSecure(operationID)`; chave efêmera no Go |
| 15 | CLX | `CloseVisual(operationID, message, mediaName)` |
| 16 | Carregar QR/mídia | `LoadQRCodeMultimedia(operationID, name, text, size)` |
| 17 | Exibir mídia | `DisplayImage(operationID, name)` |
| 18 | Tabela EMV | `LoadCompleteEMVTable(operationID, acquirer, version, records)` |
| 19 | GTK | parâmetros tipados; retorno resumido e redigido |
| 20 | GOX | parâmetros tipados; sem PIN block/KSN/EMV bruto |
| 21 | FCX | parâmetros tipados; sem EMV bruto |
| 22 | GPN MK/WK | entrada protegida; sem PIN block/KSN no resultado público |
| 23 | GPN DUKPT | entrada protegida; sem PIN block/KSN no resultado público |
| 24 | Gerar QR | `DisplayQRCodeSummaryJSON`; sem bytes na UI |
| 25 | GCX completo | visível e reservada; não chama a transação stub |
| 26 | Listar mídias | `ListMultimediaFilesJSON`; nomes validados |
| 27 | Excluir mídias | `DeleteMultimediaFiles`; lista validada/resumo |
| 28 | Sair | cancelar, fechar cliente e encerrar Activity |

Não existe caixa de comando livre, payload editável, `SendRawCommand` ou API
`Execute` genérica.

## 6. Fachada Go/mobile

Contrato mínimo:

```text
NewClient
Version
Ping
Open
Close
GetState
GetInfoJSON
Cancel
```

A fachada pode expor somente os métodos nomeados necessários à tabela da seção
5 e já aprovados na baseline 074. Todos os métodos operacionais recebem
`operationID` quando houver operação, serializam mutações, propagam
cancelamento e retornam erro sanitizado ou resumo allowlist.

Tipos internos, `context.Context`, canais, funções, mapas, mutex, drivers e
material criptográfico não cruzam gomobile. Panic é contido na fronteira e vira
`BINDING_ERROR`. JSON de saída possui schema versionado e diferencia campo
ausente de string vazia.

## 7. Estado fiel e disponibilidade

A UI mantém separadamente:

1. ação: `IDLE`, `RUNNING`, `SUCCESS`, `ERROR` ou `CANCELING`;
2. Bridge: `UNKNOWN`, `REACHABLE` ou `UNREACHABLE`;
3. sessão Go: `CLOSED`, `OPEN`, `BUSY` ou equivalente documentado.

| Evento | Efeito obrigatório |
|---|---|
| cliente novo/recriação do processo | sessão fechada; sem retomada automática |
| Version | não modifica conectividade nem sessão |
| Ping com cliente fechado | Bridge alcançável; sessão continua fechada |
| GetState retorna `CLOSED` | UI mostra fechado; GIX/DSP e dependentes ficam bloqueados |
| Open conclui OPN | sessão aberta; comandos dependentes habilitados |
| Open falha | nunca mostra OPEN; causa permanece visível; retry só explícito |
| operação falha com sessão válida | ação em erro; sessão segue o estado real do Go |
| disconnect/timeout invalida sessão | sessão fechada/desconhecida; comandos bloqueados |
| Close | sessão fechada; resultado antigo é limpo |
| troca de endpoint | fecha cliente antigo, invalida conectividade e exige novo Open |

Cancelar e Fechar permanecem disponíveis quando necessários para liberar
recursos. Botões rápidos e catálogo obedecem ao mesmo estado confirmado.

## 8. Erros e apresentação operacional

| Fase | Categoria pública |
|---|---|
| conexão recusada/host sem resposta | `BRIDGE_UNREACHABLE` ou `TIMEOUT` |
| handshake/frame | `PROTOCOL_ERROR` |
| ownership ocupado | `BUSY` |
| mecanismo de lock | `OWNERSHIP_ERROR` |
| driver/open/read/write | `SERIAL_UNAVAILABLE` |
| prazo excedido | `TIMEOUT` |
| interrupção solicitada | `CANCELED` |
| socket/peer | `DISCONNECTED` |
| status ABECS | `PINPAD_ERROR` |
| comando sem sessão | `PINPAD_CLOSED` |
| falha não classificada | `BINDING_ERROR` |

O binding preserva código, fase e `correlationId` sem depender apenas de regex
sobre `Throwable.message`. Mensagem pública é estável, limitada e sem stack,
caminho, texto arbitrário de driver ou payload. Erro secundário de cleanup não
substitui a causa original.

Código, fase, mensagem, duração e último `operationID` aparecem na área de
estado acima do catálogo. O erro deve ser percebido mesmo quando o usuário está
no fim da lista, sobreviver ao fechamento de diálogo/teclado e ser acessível
sem depender somente de cor. Iniciar nova ação limpa o resultado anterior, mas
preserva separadamente o ID final para correlação.

## 9. Logging e observabilidade

O Android mantém apenas JSON Lines privado com metadados allowlist: timestamp,
nível, componente, ação, `operationId`, fase, duração, estados, host, origem,
contagens de bytes e código. O arquivo possui rotação/retenção limitada e uma
janela limitada na UI.

O Android não cria segundo arquivo ABECS Windows. O único tracer Windows é o da
074, compartilhado por Bridge e transporte serial, em append, com eventos
reais como `bridge_starting`, `bridge_listening`, `bridge_start_failed`,
`ping_ok`, `acquire_requested`, `ownership_acquired`, `ownership_failed`,
`serial_open_failed`, `session_acquired`, `session_error`, `session_released`,
`client_disconnected` e `bridge_stopped`.

Sem Bridge, uma tentativa Android só pode registrar a falha na UI/log privado.
Arquivo criado, listener ou reverse isolados não comprovam sessão nem hardware.
Gates end-to-end devem correlacionar o `operationID` Android aos eventos
aplicáveis do Bridge, sem inventar ID antes de recebê-lo.

## 10. Concorrência, lifecycle e reabertura

- chamadas Go usam coroutines main-safe, com trabalho bloqueante em dispatcher
  apropriado;
- cancelar coroutine chama `Client.Cancel(operationID)`;
- `ViewModel.onCleared()` cancela operação e fecha cliente;
- rotação não duplica operação; recriação de processo não retoma hardware;
- Close é idempotente para a UI e aguarda a operação ativa conforme o contrato;
- trocar endpoint exige fechamento do cliente anterior;
- saída da opção 28 cancela, fecha e só então encerra a Activity.

O Gate P0 exige da 074 cleanup idempotente, read fatal propagado, fechamento de
socket/serial/ownership, término de workers, writes completos, mutex Windows
correto e reabertura somente após liberação. A UI não pode mascarar falha dessa
camada com estado local otimista.

## 11. Transporte scripted e físico

O `ScriptedSerialTransport` é ativado apenas por configuração explícita de
desenvolvimento. Ele reproduz transcript binário determinístico, compara bytes
e falha fora do roteiro sem conhecer nomes ABECS. Deve provar ao menos:

`Open → GIX → DSP → Close → Open`

O Gate físico usa `testar_bridge_pinpad.bat`, COM/pinpad compatíveis,
`PORTA_PINPAD` herdada, ownership, liberação, reabertura, log compartilhado e
confirmação visual quando aplicável. Sem hardware, registrar `não executado`;
nunca converter scripted, listener, reverse ou arquivo criado em sucesso físico.

## 12. Segurança, privacidade, acessibilidade e qualidade

- validar endpoint, timeout, `operationID`, mensagens, listas, mídia e registros
  antes da chamada;
- manter limites finais de protocolo no Go;
- entradas de PAN/chave/senha são protegidas e efêmeras;
- não persistir parâmetros sensíveis nem incluí-los em state restaurável;
- manter loopback e não autorizar LAN não autenticada;
- fixar dependências e não usar versões dinâmicas;
- toda declaração Kotlin pública/protegida criada ou alterada possui KDoc em
  português do Brasil; contratos Go públicos possuem GoDoc;
- UI possui semântica, foco previsível, alvos adequados e estado não expresso
  somente por cor;
- cobertura elegível mínima de 80%, alvo 90%, sem substituir cenários críticos.

## 13. Toolchain e artefatos

Baseline herdada das fontes até nova decisão documentada:

| Componente | Baseline |
|---|---|
| AGP | `9.4.1` |
| Gradle Wrapper | `9.6.0` |
| JDK | `17` |
| compile/target SDK | `37` |
| minSdk | `26` |
| Build Tools | `36.0.0` |
| NDK | `28.2.13676358` |
| Kotlin | built-in do AGP; runtime mínimo/default `2.2.10` |
| Compose BOM | `2026.09.00` |
| Go Mobile | `golang.org/x/mobile@v0.0.0-20260908204917-8b95e45f8d3e` |

Ambiente local obrigatório nesta workspace:

- Java: `C:\Desenvolvimento\jdk-17.0.11`;
- Maven: `C:\Desenvolvimento\apache-maven-3.8.8`.

Gradle usa o JDK indicado. Maven fica disponível no `PATH`, mas não deve ser
declarado como utilizado quando o gate executado for apenas Gradle. Todo comando
registra ambiente, resultado e código de saída.

O AAR é local, ignorado pelo Git e acompanhado de versão, commit, estado dirty,
SHA-256, ABI, minSdk, ambiente e comando. APK, AAR, logs completos e capturas de
hardware são artefatos de execução, não fontes versionadas.

## 14. Gates

### Gate P0 — baseline 074

Confirmar a SPEC 074 corrigida, seus testes aplicáveis e ausência de achado
bloqueante em configuração, PBRG, ownership, cleanup, logging ou segurança.

### Gate 1 — Go → AAR

Executar `gomobile bind`, inspecionar classes/ABI/minSdk e registrar SHA-256,
proveniência e ambiente.

### Gate 2 — AAR → Kotlin

Executar chamada real a `Version()`, testes JVM, lint, assemble e validador
estrutural usando o JDK definido.

### Gate 3 — Emulator → Bridge

Com listener real e reverse verificado, correlacionar Ping sem Acquire/COM.
Cobrir ADB ausente, emulador ambíguo/offline e Bridge ausente.

### Gate 4 — fluxo scripted

Provar `Open → GIX → DSP → Close → Open` por AAR, Go, PBRG, Bridge, transcript,
UI e log compartilhado.

### Gate 5 — pinpad físico

Repetir o fluxo com `PORTA_PINPAD` herdada e hardware real, comprovando
ownership, liberação, reabertura, log e observação visual aplicável.

### Gate 6 — estado, erros e lifecycle

Cobrir Ping/Version/CLOSED sem OPEN, preflight falho, categorias por fase,
BUSY, timeout, cancelamento, disconnect, endpoint alterado, rotação, saída e
erro visível acima do catálogo.

### Gate 7 — cleanup, concorrência e observabilidade

Cobrir read fatal ocioso, socket/serial/lock/workers, mutex multiprocesso,
eventos reais em append, persistência, correlação e redaction.

## 15. Critérios de aceite

- `CA-075-01`: projeto, namespace, Activity e módulo `app` existem no caminho.
- `CA-075-02`: as 28 opções aparecem agrupadas; 6 e 25 ficam desabilitadas e 28
  encerra com lifecycle seguro.
- `CA-075-03`: cada ação usa somente método nomeado da fachada; não há API raw.
- `CA-075-04`: Go é fonte de verdade; Kotlin não contém parser, CRC, framing ou
  regra ABECS.
- `CA-075-05`: PBRG v1, payload raw e `operationID` são preservados.
- `CA-075-06`: Android conhece apenas host/porta TCP; todo processo físico
  Windows exige `PORTA_PINPAD`, sem COM fixa/fallback, conforme 074.
- `CA-075-07`: Open fechado executa preflight Ping; falha não abre serial e
  mantém sessão fechada.
- `CA-075-08`: Ping, Version e GetState `CLOSED` nunca apresentam `OPEN`.
- `CA-075-09`: comandos dependentes são bloqueados sem sessão confirmada;
  Cancel/Close continuam disponíveis para liberar recursos.
- `CA-075-10`: categorias, fase, mensagem segura, duração e ID final são
  estáveis e visíveis acima do catálogo, com acessibilidade.
- `CA-075-11`: trocar endpoint fecha/invalida o cliente anterior; rotação,
  recriação, saída, timeout e cancelamento não vazam operação.
- `CA-075-12`: log Android contém somente metadados; o tracer único Windows
  cresce com eventos reais e correlação sem dados sensíveis.
- `CA-075-13`: cleanup/reabertura da 074 libera socket, serial, ownership e
  workers, inclusive após read fatal ou disconnect.
- `CA-075-14`: AAR é reproduzível, local, não versionado e possui
  proveniência/SHA-256/ABI/minSdk.
- `CA-075-15`: formulários rejeitam entradas inválidas e não persistem dados.
- `CA-075-16`: UI, resultados e logs não contêm material sensível listado.
- `CA-075-17`: testes Go, JVM, Compose/instrumentados, lint, build, validador,
  integração e multiprocesso possuem evidência reproduzível.
- `CA-075-18`: scripted prova o fluxo Gate 4 e não substitui o Gate físico.
- `CA-075-19`: hardware real prova Gate 5 ou permanece explicitamente
  `não executado`; nunca há sucesso inferido.
- `CA-075-20`: GoDoc/KDoc e README documentam build, uso, estados, erros,
  troubleshooting, logs, limites e caminhos Java/Maven usados.

## 16. Estado da implementação e limitações

A implementação anterior permanece histórica. A reconciliação em 2026-10-03
está em `IMPLEMENTADA`, com evidências novas em `validation.md`; revisão e
validação independente permanecem posteriores ao teste humano.
Os pontos reconciliados incluem:

- configuração obrigatória por `PORTA_PINPAD` em todo processo físico;
- preflight antes de Open;
- separação completa de ação/Bridge/sessão;
- categorias/fases e visibilidade de erro;
- correlação com eventos reais do tracer Windows;
- fluxo scripted com DSP e reabertura;
- cleanup/multiprocesso e gates end-to-end.

Race detector, hardware físico e cenários dependentes do ambiente podem ficar
`não executados`, com causa e impacto registrados. Evidência arquivada ou de uma
revisão anterior nunca é promovida automaticamente.
