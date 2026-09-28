# SPEC — 069 diagnosticopinpad Functional Lab

## 1. Status e decisão

Esta SPEC foi revisada e está em `SPEC_APROVADA`. A implementação seguirá
somente os contratos descritos neste arquivo.

## 2. Contrato funcional

O aplicativo manterá uma única Activity Compose e organizará as ações em cinco
grupos: conexão, display/teclas, captura/transação, EMV/PIN e multimídia/tabelas.
Cada ação terá um método nomeado no binding; não haverá método `execute` com
payload genérico.

| Opção | Ação Android | Contrato |
|---:|---|---|
| 1 | Abrir conexão | `Open(operationID)` |
| 2 | Fechar conexão | `Close(operationID)` |
| 3 | Estado atual | `GetState()` |
| 4 | Informações | `GetInfoJSON(operationID)` |
| 5 | Reset rápido | `Reset(operationID)` / CAN |
| 6 | Indisponível | UI desabilitada com explicação; sem chamada ao dispositivo |
| 7 | Mensagem fixa | `DisplayDSP(operationID, line1, line2)` |
| 8 | Mensagem estendida | `DisplayDEX(operationID, message)` |
| 9 | Menu interativo | `DisplayMNU(operationID, timeout, title, options)` |
| 10 | Aguardar tecla | `WaitForKeyPress(operationID, timeoutSeconds)` |
| 11 | Compra GCX | `PurchaseGCXSummary(operationID, amount, date, clock, ctls, hideAmount)` |
| 12 | GIX resumido | status e tamanho, nunca bytes raw |
| 13 | Capacidades do display | JSON tipado sem raw |
| 14 | Sessão segura | `OpenSecure(operationID)` com chave efêmera gerada no Go |
| 15 | CLX | `CloseVisual(operationID, message, mediaName)` |
| 16 | Carregar QR/mídia | `LoadQRCodeMultimedia(operationID, name, text, size)` |
| 17 | Exibir mídia | `DisplayImage(operationID, name)` |
| 18 | Tabela EMV | `LoadCompleteEMVTable(operationID, acquirer, version, records)` |
| 19 | GTK | parâmetros tipados; retorno somente resumo redigido |
| 20 | GOX | parâmetros tipados; retorno sem PIN block/KSN/EMV bruto |
| 21 | FCX | parâmetros tipados; retorno sem EMV bruto |
| 22 | GPN MK/WK | entrada protegida; sucesso/erro sem PIN block/KSN |
| 23 | GPN DUKPT | entrada protegida; sucesso/erro sem PIN block/KSN |
| 24 | Gerar QR | `DisplayQRCodeSummary`; geração PNG local, sem bytes na UI |
| 25 | GCX completo | UI desabilitada como reservado na SPEC vigente |
| 26 | Listar mídias | nomes validados retornados pelo core |
| 27 | Excluir mídias | lista validada e status resumido |
| 28 | Sair | cancelar operação, fechar cliente e encerrar Activity |

## 3. Contrato da fachada Go

Além dos métodos de 068, o pacote `mobile` deverá expor somente métodos
documentados e compatíveis com gomobile. Tipos complexos internos não cruzam a
fronteira. Respostas estruturadas serão JSON somente como saída de métodos
nomeados, com schema versionado e allowlist de campos.

Métodos mínimos novos:

```go
Reset
GetInfoRawSummaryJSON
GetDisplayCapabilitiesJSON
DisplayDSP
DisplayDEX
DisplayMNU
WaitForKeyPress
PurchaseGCXSummaryJSON
OpenSecure
CloseVisual
LoadQRCodeMultimedia
DisplayImage
LoadCompleteEMVTable
GetTracksSummaryJSON
ContinueEMVSummaryJSON
FinalizeEMVSummaryJSON
CapturePINMK
CapturePINDUKPT
DisplayQRCodeSummaryJSON
ListMultimediaFilesJSON
DeleteMultimediaFiles
```

Todos os métodos operacionais recebem `operationID`, executam fora da main
thread no Android, passam pela serialização de operações existente e propagam
cancelamento. Operações sensíveis retornam somente sucesso, erro sanitizado ou
metadados de tamanho/status.

## 4. Parâmetros e validações

- host, porta TCP e timeout seguem os limites da Change 068;
- DSP: duas linhas limitadas pelo builder Go;
- DEX: mensagem limitada pelo builder Go;
- MNU: timeout positivo, título/opções limitados pelo builder Go;
- GCX/GOX/FCX/GTK/GPN: validação final permanece no core Go; o Android valida
  formato básico e usa campos de senha para chaves/PAN;
- nomes de mídia e registros EMV são enviados como listas/strings tipadas,
  nunca como comando livre;
- arquivos e dados sensíveis não são persistidos nem registrados;
- opção 16 usa o fluxo QR já existente (`GIX` + `MLI/MLR/MLE`), não permite
  caminho arbitrário de arquivo no Android;
- opção 24 usa o gerador QR configurado na fachada e exibe apenas resumo da
  geração; o fluxo que carrega/exibe no pinpad é a opção 16.

## 5. Segurança e privacidade

- PAN, trilhas, PIN block, KSN, chaves, EMV bruto e payload raw nunca aparecem
  no resultado, log ou estado Compose;
- `PORTA_PINPAD` nunca é lida pelo app Android;
- a UI informa quando o transporte é scripted ou físico somente por metadado;
- todas as operações têm timeout/cancelamento e não fazem retry automático de
  resultado indeterminado;
- o botão de saída fecha o cliente antes de destruir a Activity;
- a opção 25 não chama a transação reservada.

## 6. Critérios de aceite

- CA-069-01: as 28 opções aparecem no laboratório, com 6 e 25 explicitamente
  desabilitadas e 28 encerrando com lifecycle seguro;
- CA-069-02: opções implementadas chamam somente métodos tipados da fachada;
- CA-069-03: core Go continua compilando sem parser ou regra ABECS no Kotlin;
- CA-069-04: os métodos gomobile têm GoDoc e testes unitários/integração;
- CA-069-05: formulários rejeitam entradas inválidas antes da chamada;
- CA-069-06: resultados sensíveis são redigidos e testes provam a ausência de
  PAN, trilhas, PIN block, KSN, chaves e bytes raw;
- CA-069-07: `PORTA_PINPAD=COM14` é consumida pelo Bridge Windows e não pelo
  Android;
- CA-069-08: `testDebugUnitTest`, `lintDebug`, `assembleDebug`, validador
  estrutural e testes Go passam;
- CA-069-09: Emulator + `adb reverse` prova pelo menos Ping, Open, GetInfo,
  Close e um fluxo scripted de cada grupo;
- CA-069-10: documentação de build, uso e limitações é atualizada.

## 7. Não conformidades explícitas

A operação real das opções dependentes de pinpad, EMV, mídia, PIN e tabela exige
hardware/configuração compatível. Sem transcript ou pinpad físico, os gates de
integração devem registrar `não executado`, nunca simular sucesso.

## 8. Complemento corretivo — Change 072

A [SPEC 072](../2026-09-27-072-corrigir-abertura-bridge-android/spec.md)
governa a correção do estado e dos erros em botões rápidos e catálogo. Opção 3
que retorna `CLOSED` deve exibir sessão fechada; Ping não abre sessão. Ações
dependentes, incluindo DSP/GIX, exigem sessão confirmada no Go. Um resultado
de consulta bem-sucedida não autoriza marcar `OPEN` incondicionalmente.

CA-069-07/09/10 serão complementados por testes CA-072-* de variável herdada,
log alimentado, estado fiel e fluxo físico. Não acrescentar comandos nem
habilitar opções reservadas por essa correção. Aprovação/encerramento da
entrega afetada exige evidências da 072, sem transformar testes históricos
scripted em sucesso físico.
