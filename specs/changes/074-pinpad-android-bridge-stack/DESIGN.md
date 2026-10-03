# DESIGN — 074 Pinpad Android Bridge Stack

## Status

IMPLEMENTADA — TESTE HUMANO PENDENTE

Este documento descreve a solução consolidada. Não é autorização de
implementação.

## 1. Decisão arquitetural

A solução é um modular monolith Go com portas explícitas e adaptadores
substituíveis:

~~~text
                         ┌──────────────┐
                         │ Android APK  │
                         │ Compose      │
                         └──────┬───────┘
                                │ gomobile/AAR
                         ┌──────▼───────┐
                         │ mobile       │
                         └──────┬───────┘
                                │ Transport TCP/PBRG
                         ┌──────▼───────┐
                         │ Emulator     │
                         │ Transport    │
                         └──────┬───────┘
                                │ loopback
                         ┌──────▼───────┐
                         │ Bridge       │
                         └──────┬───────┘
                                │ ownership
                         ┌──────▼───────┐
                         │ Serial       │
                         │ Adapter      │
                         └──────┬───────┘
                                │ ABECS
                         ┌──────▼───────┐
                         │ Pinpad       │
                         └──────────────┘
~~~

O domínio ABECS depende apenas de interfaces locais. A rede existe somente no
adaptador EmulatorTransport e no processo Bridge. HTTP existe somente no
adaptador REST. A implementação futura não deve criar um segundo core para
Android.

## 2. Responsabilidades

| Componente | Responsabilidade | Não deve fazer |
|---|---|---|
| domain/protocol | framing ABECS, CRC, substitution, enlace e secure packet | conhecer TCP/COM/UI |
| domain/parser | respostas ABECS, BER-TLV e modelos condicionais | inferir campos ausentes |
| domain/command | builders, contratos, constantes e catálogo | executar I/O |
| domain/session/state/error | posse lógica, estados e erros | manter socket ou driver |
| application/service | fila, worker, lifecycle e orquestração | expor comando raw |
| application/port.Transport | bytes bidirecionais, lifecycle e contexto | conhecer ABECS |
| infrastructure/serial | go.bug.st/serial, 8N1, COM/TTY e fake | interpretar negócio |
| infrastructure/bridgeprotocol | PBRG v1 e limites de frame | recalcular CRC ABECS |
| infrastructure/transport/emulator | sessão TCP persistente, PBRG e partial I/O | abrir COM |
| infrastructure/bridge | listener loopback, ownership e encaminhamento | interpretar payload |
| infrastructure/ownership | mutex Windows por COM e fake | decidir estado ABECS |
| infrastructure/logging | tracer serial/Bridge único e redaction | registrar dados sensíveis |
| mobile | API simples, operationID, cancelamento e summaries | expor tipos internos |
| internal/api | REST, DTOs e ErrorBody | possuir serial própria |
| Android | estado de tela, eventos e acessibilidade | montar protocolo |

## 3. Sequências normativas

### Launcher físico Windows

O BAT de teste opera em `setlocal`, muda para a raiz do módulo, valida Go,
`go.mod` e `PORTA_PINPAD` herdada, mantém overrides válidos e usa defaults
somente para baudrate, timeout, porta TCP e log. Limpa o seletor scripted,
exibe configuração não sensível, tenta a preparação opcional do emulador e
inicia o Bridge em foreground. Depois captura seu exit code, mantém a saída
legível e o devolve ao chamador. Não escolhe COM, não mata listener concorrente
e não considera reverse/listener prova de OPN. A seção 4.1.1 da SPEC contém o
contrato completo e independe da leitura da 071 arquivada.

### 3.1 Startup do Bridge

~~~text
launcher
  → valida Go/módulo/ambiente
  → resolve log absoluto e ativa tracer
  → registra bridge_starting
  → carrega COM efetiva e modo
  → cria serial/ownership
  → net.Listen(127.0.0.1:porta)
  → registra bridge_listening
  → opcionalmente prepara adb reverse
  → Serve em foreground
~~~

Falha antes do bind não emite bridge_listening. Falha do log impede o startup.
Ao terminar, o processo fecha sessões/recursos, registra bridge_stopped e só
depois fecha o tracer.

### 3.2 Ping

~~~text
mobile Client
  → HELLO(correlationId)
  ← HELLO_OK
  → PING(correlationId)
  ← PONG(correlationId)
  → fecha conexão temporária, se não houver sessão
~~~

Ping não adquire ownership nem toca a serial.

### 3.3 Open

~~~text
mobile Open(operationID)
  → Ping controlado
  → EmulatorTransport.Open
      → HELLO/HELLO_OK
      → ACQUIRE/ACQUIRE_OK
  → Bridge Acquire ownership
  → Serial.Open
  → CAN/EOT inicial
  → OPN clássico ou seguro
  ← resposta OPN000
  → sessão confirmada OPEN
~~~

Falha em qualquer fase retorna categoria/fase, libera recursos e não fabrica
OPEN.

### 3.4 Comando

~~~text
operação tipada
  → validação de entrada
  → fila FIFO
  → worker único
  → builder ABECS
  → framing/substitution/CRC
  → Transport.Write
  → ACK/NAK
  → Transport.Read em chunks
  → parser/verificação de RSP_ID/status
  → modelo/erro
  → RSP/log correlato
~~~

### 3.5 Close/disconnect

~~~text
cancel/close/disconnect
  → cancelar contextos
  → impedir novas operações
  → CAN/EOT quando o protocolo exigir
  → CLO/limpeza KSEC quando aplicável
  → fechar socket e serial
  → liberar mutex/ownership
  → aguardar workers
  → sessão CLOSED/erro explícito
~~~

Nenhuma reabertura começa antes de todos os passos de cleanup terminarem.

## 4. Decisões consolidadas

### D-074-001 — Identificador

073 não pode ser reutilizado porque já está arquivado em outra Change. A
baseline usa 074-pinpad-android-bridge-stack.

### D-074-002 — PBRG opaco

O envelope PBRG é de transporte e correlação. Apenas o core conhece ABECS.
Isso permite testar Bridge, TCP e AAR sem duplicar regras de negócio.

### D-074-003 — Um tracer

O Bridge inicializa e injeta um único tracer no transporte físico/scripted. O
Android não escreve o arquivo Windows.

### D-074-004 — COM por ambiente

Todo processo físico, inclusive launcher e entrypoint direto, exige
`PORTA_PINPAD` herdada e nunca escolhe COM fixa/fallback. A origem é observável
e a mesma string efetiva é usada no adaptador e no ownership. Esta decisão
supersede explicitamente o fallback direto da 072.

### D-074-005 — Ping separado de Open

Ping é preflight de conectividade. Open é a única operação que pode confirmar a
sessão ABECS.

### D-074-006 — Alias de reset

O protocolo não possui RST. Se a compatibilidade REST exigir uma rota de reset,
ela chama CAN/EOT e não deve contaminar catálogo, fachada mobile ou serialização.

### D-074-007 — Falha indeterminada

Após timeout ou disconnect, a operação original é indeterminada e não pode ser
repetida automaticamente. A recuperação existe para restabelecer o canal, não
para declarar sucesso.

### D-074-008 — Laboratório Android

O subsistema inclui o contrato AAR e o laboratório consumidor necessário para
provar Android→Bridge. A UI continua uma consumidora do binding e não uma nova
fonte de regras ABECS.

## 5. Segurança de fronteiras

- O Bridge aceita apenas loopback.
- Frames PBRG excedentes, inválidos ou com payload acima do limite são rejeitados
  antes do encaminhamento.
- correlationId é UTF-8 limitado e sanitizado.
- Categorias públicas são allowlist; mensagens remotas não são ecoadas.
- Logs são append, sincronizados e serializados.
- Conteúdo sensível é redigido antes do tracer, inclusive em erro e cleanup.
- A chave segura existe apenas em memória durante a sessão e é descartada.

## 6. Consequências

O desenho adiciona uma fronteira TCP e um binding, mas preserva um único core e
permite testes sem hardware. Em contrapartida, exige gates distintos para
fakes, AAR, processo Windows, concorrência real e pinpad físico. Um resultado
scripted não fecha o gate físico.
