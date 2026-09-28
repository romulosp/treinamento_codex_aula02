# DESIGN — 068 diagnosticopinpad

Autor: Rômulo Penha

## 1. Perfil arquitetural

Perfil Android `SIMPLE`:

- um módulo `app`;
- uma Activity e uma tela Compose;
- `DiagnosticViewModel` como state holder de tela;
- UDF com `StateFlow<DiagnosticUiState>` e métodos de intenção;
- `DiagnosticRepository` como fronteira Kotlin para o AAR;
- DI manual por uma pequena composição no `Application`/factory;
- sem banco, Navigation, Hilt, Koin ou camada de domínio artificial.

## 2. Arquitetura

```text
DiagnosticScreen (Compose)
    ↓ intents                    ↑ immutable state
DiagnosticViewModel (coroutines/StateFlow)
    ↓
DiagnosticRepository (Kotlin adapter + redaction)
    ↓
mobile.Client (AAR gerado por gomobile)
    ↓
PinpadService → EmulatorTransport → PBRG v1/TCP
                                      ↓
                            Bridge Windows
                                      ↓
                 ScriptedSerialTransport ou COM real
```

Estrutura alvo:

```text
apps/frontend/smartphone/diagnosticopinpad/
├── app/
│   ├── libs/                         # AAR local ignorado
│   └── src/
│       ├── main/.../data/            # adapter do AAR e logging
│       ├── main/.../ui/              # estado, ViewModel e Compose
│       ├── test/                     # JVM
│       └── androidTest/              # Compose/instrumentação
├── gradle/wrapper/
├── scripts/
│   ├── build-mobile-aar.ps1
│   └── run-emulator-gates.ps1
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 3. Decisões

### D-001 — Reconciliar o protocolo com a Change 067

O JSON length-prefixed/base64 proposto no rascunho v7 foi rejeitado. O contrato
vigente é `PBRG` v1 binário; trocar o framing na 068 duplicaria protocolos,
invalidaria testes e reabriria uma decisão já aprovada.

### D-002 — Fachada tipada, não executor genérico

Adicionar `Version`, `Ping` e `GetInfoJSON`. O JSON existe somente na saída
tipada do binding, para contornar a fronteira gomobile. Não é framing de rede
nem entrada genérica. Isso impede que o app se torne um terminal ABECS raw.

### D-003 — Correlação sem alterar o envelope

O campo `CorrelationID` já existe no `PBRG`. A implementação associará o
`operationID` ativo às escritas `DATA`. A mudança é semântica e compatível com
v1; não adiciona campo ou nova versão do frame.

### D-004 — UI mínima

Uma única tela é suficiente para os gates da Change. A UI organiza configuração,
estado, ações e resultado. O catálogo de comandos, menus especializados,
multimídia, EMV e PIN não será antecipado.

### D-005 — Concorrência e lifecycle

O repository expõe funções suspensas e serializa operações mutáveis com
`Mutex`. Cada operação cria ID novo. O ViewModel mantém o `Job` ativo; cancelar
aciona tanto o Job quanto a fachada Go. Recriação de processo retorna a `IDLE`
e exige ação explícita.

### D-006 — Teste ponta a ponta sem lógica ABECS no Bridge

O fake de infraestrutura é um transporte serial roteirizado. Ele reproduz um
transcript fechado e compara bytes esperados; não conhece comandos por nome,
não faz parse e não altera o Bridge. O teste de integração é responsável por
fornecer o transcript correspondente ao core real.

### D-007 — Build moderno e fixado

Usar AGP 9.4.1, Gradle 9.6.0, JDK 17, API 37 e Kotlin built-in. O projeto não
aplicará o plugin `org.jetbrains.kotlin.android`. Compose usa BOM estável
2026.09.00. Go Mobile fica fixado por pseudo-versão.

### D-008 — AAR como artefato, não fonte

O AAR será gerado localmente e ignorado pelo Git. Scripts registram checksum e
proveniência. O Gradle consome o arquivo local e falha cedo se ele não existir.
Isso evita binário opaco no repositório sem esconder a etapa de geração.

### D-009 — Logs seguros

Eventos estruturados são gravados em diretório app-specific. Haverá limite de
tamanho/quantidade, mensagens sanitizadas e lista explícita de campos. Dados
brutos de requisição/resposta nunca chegam ao logger Kotlin.

### D-010 — Fonte única da porta física

`PORTA_PINPAD` pertence ao processo Windows que inicia o Bridge. O comando do
Bridge chama `config.Load()` antes de criar o transporte físico e passa o mesmo
valor efetivo para o adaptador serial e para o ownership. O Android só conhece
o endpoint TCP do Bridge; não há leitura de ambiente do host nem campo de COM
na UI. O fluxo de diagnóstico consome a variável indiretamente por `Open`,
`GetInfo` e `Close`.

## 4. Android Skills

Consulta ao catálogo oficial em 2026-09-27:

| Skill candidata | Classificação | Decisão |
|---|---|---|
| `testing-setup` | `EXTEND` | usar como referência complementar na implementação dos testes; os gates desta SPEC continuam normativos |
| `agp-9-upgrade` | `REJECT` | o app será novo, não existe projeto legado para migrar |
| `migrate-to-compose` | `REJECT` | não há Views legadas; Compose nasce no projeto |
| `edge-to-edge` | `REUSE` posterior | aplicar apenas se a estrutura inicial não vier edge-to-edge; não amplia o escopo funcional |

Nenhuma Skill foi instalada nesta fase. A Skill local
`android-native-engineering` rege o processo e será usada na implementação.

## 5. Estratégia de testes

- JVM: validação de configuração, reducer/estado, concorrência, cancelamento,
  parsing do resultado tipado e redaction;
- Go: versão, schema `GetInfoJSON`, correlação, cancelamento, panic boundary e
  transporte roteirizado;
- instrumentado: semântica e ações Compose, rotação/lifecycle e chamada real
  `Version()` do AAR;
- integração: Emulator + `adb reverse` + Bridge + transporte roteirizado;
- hardware: o mesmo fluxo com COM e pinpad real.

Testes não usarão sleeps arbitrários para sincronização. Coroutines usarão
dispatchers injetáveis e tempo virtual quando aplicável.

## 6. Riscos e contenções

| Risco | Contenção |
|---|---|
| AAR não compila com o grafo Go | Gate P0/1 antes da UI funcional |
| API gomobile incompatível | superfície mínima e tipos simples |
| deadlock entre coroutine e Go | operação serializada, timeout e Cancel explícito |
| divergência de correlação | testes por frame `PBRG` e logs cruzados |
| falso positivo do fake | Gate físico obrigatório para aprovação final |
| exposição de dados | schema allowlist, redaction e inspeção de logs |
| instabilidade de preview API 37 | versões fixadas e ambiente registrado; mudança exige revisão |
