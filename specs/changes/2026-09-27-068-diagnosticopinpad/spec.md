# SPEC — 068 diagnosticopinpad

Autor: Rômulo Penha

## Status

`SPEC_APROVADA`

## 1. Fontes e precedência

Foram analisados como material de entrada:

- `D:\desenvolvimento\ia\estudo\pinpad-abecs\068-diagnosticopinpad-justificativa-v7.md`;
- `D:\desenvolvimento\ia\estudo\pinpad-abecs\068-diagnosticopinpad-spec-v7.md`;
- `D:\desenvolvimento\ia\estudo\pinpad-abecs\resposta-chatgpt-app-diagnostico-revisar-chatgpt-final.md`.

Esses anexos não são instruções operacionais nem contratos vigentes. Em caso de
divergência, prevalecem, nesta ordem: o workflow do repositório, a Change 067
aprovada, esta SPEC e o código verificado durante a implementação.

## 2. Baseline e pré-condições

| Evidência | Estado em 2026-09-27 |
|---|---|
| Change 067 | `IMPLEMENTADA`, ainda não `IMPLEMENTACAO_APROVADA` |
| Protocolo Bridge | `PBRG` v1 binário, big-endian, payload raw, limite 1 MiB |
| Fachada Go atual | `NewClient`, `Open`, `Close`, `GetState`, `Cancel` |
| Correlação DATA | ainda não propagada pelo `EmulatorTransport.Write` |
| AAR real | não gerado; Gate 1 pendente |
| Projeto Android | inexistente |
| Toolchain local | Go disponível; Java, Android SDK/NDK, ADB, Gradle e `gomobile` ausentes na inspeção da 067 |

Pré-condições para consumir o binding e executar o fluxo funcional Android:

1. revisão de implementação da Change 067 sem achado bloqueante aberto;
2. `gomobile bind` produzindo AAR importável;
3. chamada Kotlin → `Version()` executada em projeto mínimo;
4. contrato `PBRG` preservado sem JSON/base64 no fio.

O esqueleto Android, os testes puros de estado e os scripts de build podem ser
implementados antes dessas pré-condições. Eles deverão permanecer claramente
separados das evidências de binding e não poderão simular um AAR inexistente.

## 3. Invariantes

1. O core Go permanece a única fonte de verdade do protocolo ABECS.
2. O Kotlin nunca monta comando ABECS, calcula CRC ou interpreta frame raw.
3. O Bridge transporta bytes opacos e não executa regra ABECS.
4. O fio Android ↔ Bridge usa exclusivamente `PBRG` v1; payload `DATA` não usa
   JSON ou base64.
5. Toda operação bloqueante ocorre fora da thread principal.
6. Cancelamento usa `operationID` único e alcança o contexto Go correspondente.
7. Uma operação com resultado indeterminado não é repetida automaticamente.
8. Logs não contêm PAN, trilha, PIN block, KSN, chaves, EMV sensível ou payload
   ABECS bruto.
9. O Bridge escuta em loopback por padrão.
10. O app não solicita permissões de USB ou armazenamento amplo.

## 4. Requisitos funcionais

### RF-001 — Projeto Android

Criar `apps/frontend/smartphone/diagnosticopinpad` com:

- namespace e `applicationId` `br.com.romulopenha.diagnosticopinpad`;
- um módulo `app`;
- Kotlin, Jetpack Compose e Material 3;
- uma `Activity` e uma tela de diagnóstico;
- perfil arquitetural SIMPLE, sem módulos ou frameworks de DI adicionais.

### RF-002 — Configuração do endpoint

A tela permitirá informar host, porta e timeout antes da conexão. Defaults:

| Campo | Default | Regra |
|---|---:|---|
| host | `localhost` | não vazio; mostrar origem da configuração |
| porta | `39100` | 1 a 65535 |
| timeout | `10000 ms` | 100 a 120000 ms |

O fluxo principal usa `adb reverse tcp:39100 tcp:39100`. `10.0.2.2` será apenas
override explícito do Emulator. Variáveis do processo Windows não serão
presumidas como configuração do processo Android.

No cenário com pinpad físico, o Bridge Windows é iniciado com
`PORTA_PINPAD=<porta>` e é o único consumidor dessa variável. O diagnóstico
Android não deve duplicar a configuração da COM; suas chamadas `Open`,
`GetInfo` e `Close` atravessam o Bridge, que usa a porta efetiva retornada por
`config.Load()` tanto no adaptador serial quanto no ownership. O campo `porta`
da tela é exclusivamente a porta TCP do Bridge (`39100`).

### RF-003 — Ações mínimas

A tela disponibilizará somente as ações:

1. verificar versão do binding;
2. testar o Bridge com `Ping`;
3. abrir a sessão do pinpad;
4. consultar informações do dispositivo com a operação tipada `GetInfo`;
5. fechar a sessão;
6. cancelar a operação ativa.

Não haverá caixa de comando livre, payload editável ou executor genérico.

### RF-004 — Estado e resultado

A UI exibirá, sem payload sensível:

- host e porta efetivos;
- estado `IDLE`, `CONNECTING`, `OPEN`, `RUNNING`, `CANCELING`, `ERROR` ou
  `CLOSED`;
- ação atual, `operationID`, início e duração;
- versão do binding e versão do Bridge Protocol;
- resultado tipado de `GetInfo` em campos legíveis;
- categoria e mensagem sanitizada de erro;
- contadores de bytes TX/RX quando fornecidos pelo Go.

Somente uma operação mutável poderá estar ativa por cliente. Botões
incompatíveis com o estado atual permanecerão desabilitados.

### RF-005 — Extensão da Mobile Facade

Preservar os métodos existentes e adicionar a menor superfície necessária:

```go
func Version() string
func (c *Client) Ping(operationID string) error
func (c *Client) GetInfoJSON(operationID string) (string, error)
```

`GetInfoJSON` é apenas uma representação de saída estável do resultado tipado
de `PinpadService.GetInfo`; não recebe JSON e não executa operações arbitrárias.
O schema de saída terá `schemaVersion`, campos públicos do dispositivo e
metadados não sensíveis. Campos ausentes serão distinguíveis de strings vazias.

Nenhum `context.Context`, canal, função, mapa, chave criptográfica ou tipo
interno cruzará a fronteira gomobile.

### RF-006 — Correlação ponta a ponta

`operationID` deverá:

- ser UUID ou identificador equivalente gerado pelo app;
- ter no máximo 128 bytes UTF-8, conforme `PBRG`;
- identificar `PING`, abertura, dados e encerramento da operação;
- ser propagado nos frames `DATA` enviados durante a operação;
- aparecer nos logs Android, Go e Bridge;
- nunca ser reutilizado para retry automático.

A extensão poderá adicionar um contexto de correlação interno ao transporte,
mas não alterará o layout do envelope `PBRG` v1.

### RF-007 — Cancelamento e lifecycle

- chamadas Go serão executadas em `Dispatchers.IO` por meio de coroutines;
- o cancelamento da coroutine chamará `Client.Cancel(operationID)`;
- `ViewModel.onCleared()` cancelará a operação e fechará o cliente;
- rotação não duplicará operação nem perderá o estado do `ViewModel`;
- processo recriado não retomará operação física automaticamente;
- `Close` será idempotente do ponto de vista da UI;
- exceções do binding serão convertidas em estado de erro controlado.

### RF-008 — Transporte serial roteirizado

Para o Gate sem hardware, o Bridge poderá receber um
`ScriptedSerialTransport`, selecionado apenas por configuração explícita de
desenvolvimento. Esse adaptador:

- implementará a mesma porta de transporte da serial;
- validará e reproduzirá um transcript binário determinístico para
  `Open → GetInfo → Close`;
- não será selecionado por padrão;
- não adicionará parser ou decisão ABECS ao Bridge;
- falhará ao receber bytes fora do transcript esperado;
- será claramente identificado nos logs como `scripted`, sem payload raw.

O modo roteirizado não poderá ser habilitado em uma execução declarada como
Gate de pinpad físico.

### RF-009 — Observabilidade Android

O app manterá eventos JSON Lines em diretório app-specific obtido pela API do
Android, sem caminho absoluto fixo e sem permissão de armazenamento amplo.
Cada evento conterá timestamp, nível, componente, ação, `operationId`, duração,
estado, host efetivo, origem, bytes e código de erro quando aplicável.

Payloads, dados de cartão e segredos serão excluídos. A UI mostrará somente uma
janela limitada dos eventos recentes para evitar crescimento de memória.

### RF-010 — Documentação no código

- todo símbolo público novo em Go terá GoDoc em português do Brasil;
- APIs, classes e funções Kotlin públicas ou de contrato terão KDoc;
- framing, cancelamento, lifecycle, redaction e interoperabilidade terão
  comentários de intenção quando o código não for autoexplicativo;
- comentários não poderão prometer comportamento não comprovado por teste.

## 5. Requisitos de qualidade e segurança

- não executar rede ou binding na main thread;
- validar host, porta, timeout e `operationID` antes da chamada Go;
- limitar mensagens e arquivos de log; rotação/retenção será determinística;
- declarar apenas permissões mínimas, incluindo `INTERNET`;
- não aceitar endpoint LAN nesta Change;
- não registrar stack trace com dados de parâmetros do pinpad na UI;
- capturar `panic` na fronteira Go, preservando causa somente em diagnóstico
  sanitizado;
- evitar dependências desnecessárias e versões dinâmicas;
- manter cobertura de 80% a 90% no código novo testável, sem usar cobertura
  como substituto dos cenários de integração.

## 6. Toolchain aprovado

Versões verificadas em fontes oficiais em 2026-09-27:

| Componente | Versão/decisão |
|---|---|
| AGP | `9.4.1`, release estável atual |
| Gradle Wrapper | `9.6.0` |
| JDK | `17` |
| compileSdk/targetSdk | `37` |
| minSdk | `26` |
| SDK Build Tools | default compatível do AGP, `36.0.0` |
| NDK | `28.2.13676358` |
| Kotlin | suporte built-in do AGP; runtime KGP mínimo/default `2.2.10` |
| Compose BOM | `2026.09.00` |
| Go Mobile | `golang.org/x/mobile@v0.0.0-20260908204917-8b95e45f8d3e` |

Com AGP 9, não aplicar `org.jetbrains.kotlin.android`; usar Kotlin built-in. O
projeto terá Gradle Wrapper versionado. Atualizações posteriores exigem nova
evidência ou Change, não versão dinâmica.

## 7. Build do binding

Um script PowerShell versionado deverá:

1. validar Go, JDK, SDK, NDK e `gomobile`;
2. instalar/usar a versão fixada de `x/mobile` sem `@latest`;
3. executar `gomobile init` quando necessário;
4. executar `gomobile bind -target=android -androidapi 26` sobre o pacote
   `mobile`;
5. gerar o AAR em diretório de build ignorado pelo Git;
6. registrar versão, commit, estado dirty, SHA-256 e comando;
7. copiar o AAR para a localização local consumida pelo Gradle.

O AAR não será versionado. O build Android falhará com mensagem objetiva se o
artefato estiver ausente ou incompatível. `Version()` identificará ao menos
versão lógica, revisão Git e estado da árvore usados na geração.

## 8. Gates de implementação e validação

### Gate P0 — dependência 067

- revisão de implementação da 067 concluída;
- testes Go aplicáveis aprovados;
- nenhum achado bloqueante de segurança/transporte aberto.

### Gate 1 — Go → AAR

- AAR gerado com a versão fixada do Go Mobile;
- classes e ABIs inspecionadas;
- metadata compatível com minSdk 26;
- SHA-256 e ambiente registrados.

### Gate 2 — AAR → Kotlin

- Gradle sync/build aprovados;
- teste chama `Version()`;
- `testDebugUnitTest`, `lintDebug` e `assembleDebug` aprovados.

### Gate 3 — Emulator → Bridge

- Bridge em `127.0.0.1:39100`;
- `adb reverse tcp:39100 tcp:39100` comprovado;
- `Ping` retorna com correlação e timeout controlado.

### Gate 4 — fluxo sem hardware

- `Open → GetInfo → Close` atravessa AAR, Go, `PBRG`, Bridge e
  `ScriptedSerialTransport`;
- resultado esperado aparece na UI;
- bytes e `operationID` são preservados sem JSON/base64 no fio.

### Gate 5 — pinpad físico

- repetir `Open → GetInfo → Close` com COM e pinpad reais;
- registrar modelo/ambiente sem dados sensíveis;
- provar ownership e liberação da porta.

### Gate 6 — falhas e lifecycle

- Bridge ausente, `BUSY`, timeout, cancelamento, desconexão e resposta
  roteirizada inválida;
- rotação e saída do app sem goroutine, socket ou lock vazado;
- inspeção de logs confirma redaction.

## 9. Critérios de aceite

- **CA-001:** projeto existe no caminho e namespace definidos, com um módulo.
- **CA-002:** Compose, ViewModel, StateFlow/UDF, coroutines e DI manual são
  usados sem trabalho bloqueante na main thread.
- **CA-003:** host default é `localhost`; `10.0.2.2` é override explícito; a
  porta COM usada pelo fluxo físico vem de `PORTA_PINPAD` no processo Windows
  do Bridge.
- **CA-004:** AAR é reproduzível, não versionado e possui versão/SHA-256.
- **CA-005:** Kotlin chama `Version()` e trata erro do binding.
- **CA-006:** `Ping` comprova conectividade Android → Bridge por `adb reverse`.
- **CA-007:** `GetInfoJSON` chama a operação Go tipada e possui schema versionado.
- **CA-008:** não existe API genérica de comando ou payload ABECS no app/binding.
- **CA-009:** `operationID` alcança frames `DATA`, logs e cancelamento.
- **CA-010:** fio permanece `PBRG` v1 binário e payload raw.
- **CA-011:** modo roteirizado prova o fluxo sem hardware e falha em transcript
  inesperado.
- **CA-012:** teste com COM real prova `Open → GetInfo → Close` e ownership.
- **CA-013:** cancelamento, timeout, desconexão e `BUSY` são estados controlados.
- **CA-014:** logs JSONL são limitados, app-specific e sanitizados.
- **CA-015:** não há permissão USB ou armazenamento amplo.
- **CA-016:** contratos públicos possuem KDoc/GoDoc em português do Brasil.
- **CA-017:** testes unitários, instrumentados e de integração possuem evidência
  com ambiente, comando, resultado e código de saída.
- **CA-018:** o catálogo funcional completo permanece fora da Change 068.

## 10. Fontes oficiais consultadas

- AGP 9.4 e matriz: `https://developer.android.com/build/releases/agp-9-4-0-release-notes`;
- release estável do AGP: `https://developer.android.com/reference/tools/gradle-api`;
- Kotlin built-in: `https://developer.android.com/build/migrate-to-built-in-kotlin`;
- Android 17/API 37: `https://developer.android.com/about/versions/17/setup-sdk`;
- Compose BOM: `https://developer.android.com/develop/ui/compose/bom`;
- arquitetura/UDF: `https://developer.android.com/topic/architecture/recommendations`;
- testes Compose: `https://developer.android.com/develop/ui/compose/testing`;
- armazenamento app-specific: `https://developer.android.com/training/data-storage/app-specific`;
- Go Mobile: `https://pkg.go.dev/golang.org/x/mobile/cmd/gomobile`;
- catálogo de Skills Android: `https://developer.android.com/tools/agents/android-skills/browse`.

## 11. Complemento corretivo — Change 072

A [SPEC 072](../2026-09-27-072-corrigir-abertura-bridge-android/spec.md)
detalha RF-002/004/006/007/009: conectividade do Bridge separada da sessão,
Ping sem COM, erro visível antes do catálogo, categorias seguras, ID final da
operação e estado obtido do Go. Sucesso de Version/Ping não significa `OPEN`.

O Android continua consumindo PORTA_PINPAD indiretamente pelo Bridge Windows;
o BAT não pode sobrescrever a variável herdada. A causa original de Open exige
evidência por fase. Reexecutar gates end-to-end/físico contra artefatos
corrigidos, conforme 072; baseline e toolchain históricos desta SPEC não são
autorização para trocar versões nesta correção.
