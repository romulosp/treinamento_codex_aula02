# DESIGN — 075 diagnosticopinpad consolidado

## Status

`IMPLEMENTADA`

## 1. Perfil e fronteira arquitetural

O laboratório usa perfil Android `STANDARD` em um único módulo `app`. A 075
possui a UI e a integração Android; a 074 possui o stack Go/Bridge. Essa
separação evita duplicar core, REST, serial, ownership e protocolo.

```text
DiagnosticScreen
  → DiagnosticViewModel / StateFlow<DiagnosticUiState>
  → DiagnosticRepository
  → GoMobileRepository / mobile.Client (AAR)
  → PinpadService / EmulatorTransport / PBRG v1
  → Bridge Windows / ownership / serial scripted ou COM
  → pinpad
```

O estado desce imutável; eventos sobem. O repository é a única fronteira Kotlin
com o AAR. O Android não interpreta ABECS nem acessa REST, COM ou mutex.

## 2. Estrutura alvo

```text
apps/frontend/smartphone/diagnosticopinpad/
├── app/
│   ├── libs/                         # AAR local ignorado
│   └── src/
│       ├── main/.../data/            # adapter, repository e logger privado
│       ├── main/.../ui/              # estado, ViewModel, seções e diálogos
│       ├── test/                     # JVM
│       └── androidTest/              # Compose/instrumentação
├── gradle/wrapper/
├── scripts/
│   ├── build-mobile-aar.ps1
│   └── validate-project.ps1
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 3. Modelo de estado

`DiagnosticUiState` contém domínios independentes:

- `actionState`: IDLE/RUNNING/SUCCESS/ERROR/CANCELING;
- `bridgeState`: UNKNOWN/REACHABLE/UNREACHABLE;
- `sessionState`: CLOSED/OPEN/BUSY ou equivalente vindo do Go;
- endpoint efetivo e origem;
- ação atual e `operationID` em andamento;
- último `operationID` finalizado;
- fase, duração, categoria, mensagem segura e resultado redigido;
- metadado de transporte scripted/físico quando fornecido.

O reducer não deriva sessão a partir de sucesso genérico. Somente retorno
confirmado do Go altera a sessão. Resultado anterior é limpo no início da nova
ação; diagnóstico final permanece disponível para correlação.

Os campos concretos Kotlin são `actionStatus: ActionStatus`,
`bridgeStatus: BridgeStatus` e `sessionStatus: SessionStatus`. O último também
aceita `UNKNOWN` quando o binding não publica um estado reconhecido.

## 4. Sequências normativas

### 4.1 Ping

```text
UI → ViewModel → repository → mobile.Ping(operationID)
   → HELLO/HELLO_OK → PING/PONG
   → Bridge REACHABLE
   → sessão permanece CLOSED quando estava fechada
```

Ping não executa Acquire, ownership, serial ou ABECS.

### 4.2 Open

```text
UI → Open
   → limpar resultado anterior e criar operationID
   → preflight Ping dentro do timeout da ação
   → se falhar: erro visível, sessão CLOSED, sem Acquire
   → se passar: mobile.Open
      → ACQUIRE → ownership → serial.Open → CAN/EOT → OPN
   → OPEN somente após confirmação Go/OPN
```

### 4.3 Comando tipado

```text
UI valida formulário
  → ViewModel serializa operação
  → repository chama método nomeado
  → Go valida/encaminha/processa
  → summary allowlist ou erro categorizado
  → UI preserva estado real da sessão
```

### 4.4 Close, cancelamento e endpoint

Close/Cancel permanecem acionáveis quando necessários. A troca de endpoint
cancela/fecha o cliente anterior, invalida Bridge/sessão e cria novo cliente.
Saída cancela, fecha e só então encerra a Activity. Nenhuma nova abertura começa
antes do cleanup definido pela 074.

A opção Sair permanece habilitada durante uma operação, tal como Fechar,
permitindo acionar a sequência de liberação. A recriação da Activity conserva
o mesmo ViewModel e operationID; isso foi testado com uma operação pendente.

## 5. Decisões

### D-075-01 — Uma Change canônica de produto

068 e 069 deixam de ser fontes operacionais. A 075 reúne o laboratório mínimo,
o catálogo funcional e os efeitos observáveis das correções posteriores.

### D-075-02 — 074 é dependência normativa, não conteúdo duplicado

PBRG, Bridge, serial, ownership, tracer, launcher e REST permanecem na 074. A
075 define como o app consome essa baseline e quais evidências end-to-end exige.

### D-075-03 — PBRG v1 e fachada nomeada

JSON/base64 no fio e executor genérico são rejeitados. Cada ação tem método
próprio; JSON aparece apenas em summaries nomeados e redigidos.

### D-075-04 — Estado triplo

Ação, conectividade e sessão não são colapsadas. Ping/Version nunca promovem
OPEN e uma falha de ação não fecha sessão ainda válida por suposição local.

### D-075-05 — Preflight obrigatório

Open em cliente fechado verifica primeiro a disponibilidade do Bridge. Falha
impede Acquire/serial e produz orientação segura; não há retry automático.

### D-075-06 — Erro estruturado e visível

Categoria, fase, mensagem segura, duração e correlação fazem parte do estado. A
área de diagnóstico fica acima do catálogo e é acessível sem depender de cor.

### D-075-07 — Logs separados por responsabilidade

Android registra apenas metadados privados. Bridge/serial usam o tracer único
Windows da 074. Correlação comprova o fluxo sem copiar payload.

### D-075-08 — Fonte da porta física

Android conhece apenas host/porta TCP. Todo processo físico Windows exige
`PORTA_PINPAD`; não existe COM fixa/fallback. A porta efetiva é compartilhada
por serial e ownership, conforme decisão aprovada da 074.

### D-075-09 — Scripted não comprova hardware

Transcript determinístico prova integração, inclusive DSP e reabertura. Gate
físico exige COM/pinpad, ownership, log e observação aplicável.

### D-075-10 — AAR local e reproduzível

O AAR não é versionado. Script, versão, commit, dirty state, SHA-256, ABI,
minSdk e ambiente formam sua proveniência.

### D-075-11 — Sem persistência sensível

Formulários e segredos vivem apenas pelo tempo necessário. Estado restaurável e
logs excluem material sensível.

## 6. Estratégia de testes

- Go: preflight, erros, correlação, cancelamento, redaction, summaries e
  precedência da configuração;
- Kotlin/JVM: reducer triplo, disponibilidade, endpoint, erros e lifecycle;
- Compose/instrumentado: seções, diálogos, erro acima do catálogo, rotação,
  foco, semântica e saída;
- integração: AAR, listener, reverse, Ping, Open, GIX, DSP, Close e reabertura;
- Windows: launcher físico, entrypoint direto, tracer, ownership, read fatal e
  mutex multiprocesso;
- hardware: comandos e observação somente com COM/pinpad real.

Não usar sleeps arbitrários. Dispatchers, relógio, transportes e sinais devem
ser injetáveis/determinísticos quando aplicável.

## 7. Riscos

| Risco | Contenção |
|---|---|
| implementação anterior parecer conforme | estado `IMPLEMENTAÇÃO A RECONCILIAR` e gates novos |
| AAR incompatível | Gate 1 antes da integração Android |
| falso OPEN | estado confirmado pelo Go e testes Ping/Version/CLOSED |
| erro invisível no catálogo longo | painel superior, foco/scroll e semântica |
| deadlock/vazamento | operação única, timeout, cancelamento e cleanup 074 |
| falso positivo scripted | Gate físico separado |
| log divergente ou sensível | tracer único Windows, logger Android metadata-only e redaction |
| COM divergente | precedência explícita e mesma porta em serial/ownership |
| confusão de toolchain | JDK e Maven locais registrados separadamente |
