# DESIGN — 067 Android Emulator Transport Bridge

Autor: Rômulo Penha

## 1. Decisão de decomposição

O documento de referência mistura fundação de transporte, binding e aplicativo
Android. A decisão desta Change é separá-los:

```text
067 — Transport/Bridge/Mobile binding
        ↓ artefato AAR e contratos aprovados
068 — diagnosticopinpad/Android Functional Lab
```

Essa separação permite falhar no Gate 1 sem criar uma UI que esconda uma
incompatibilidade de toolchain ou de transporte.

## 2. Arquitetura proposta

```text
PinpadService
    ↓ internal/application/port.Transport
    ├── WindowsSerialTransport → COM física
    └── EmulatorTransport → TCP persistente → BridgeEnvelope
                                               ↓
                                      Android Transport Bridge
                                               ↓
                                      WindowsSerialTransport
                                               ↓
                                             COM
```

O pacote público `mobile` é uma fachada fina sobre `PinpadService` e
`EmulatorTransport`. O pacote não exporta os tipos internos do core.

Estrutura planejada, sujeita ao código real durante a implementação:

```text
apps/desktop/libpinpadabecsgo/
├── mobile/                         # API pública para gomobile
├── internal/application/port/      # Transport
├── internal/infrastructure/transport/emulator/
├── internal/infrastructure/bridge/
├── internal/infrastructure/ownership/
└── cmd/libpinpadabecsgo-bridge/
```

O AAR será artefato de build e não será versionado no repositório. A Change
068 decidirá onde consumi-lo no Gradle.

## 3. Decisões técnicas

### D-001 — Interface Core → Transport

Usar `internal/application/port.Transport` com `Open`, `Close`, `Read(ctx)`,
`Write` e `IsOpen`. A interface atual `service.SerialPort` é compatível em
forma, mas será consolidada sob a porta única para que o serviço não nomeie
uma tecnologia que também será usada por TCP.

### D-002 — TCP persistente

TCP persistente foi escolhido por se comportar como stream bidirecional, estar
disponível no Android Emulator e não introduzir uma dependência WebSocket. TCP
exige framing próprio; por isso o BridgeEnvelope é obrigatório.

### D-003 — Envelope

O envelope é binário, versionado e limitado. `DATA` não é parseado; somente
frames de controle têm semântica do Bridge. O decoder valida o tamanho antes
de alocar e o writer usa loop para completar partial writes.

### D-004 — Ownership

Ownership é de sessão, não de chamada. No Windows, o adaptador de ownership
usará mutex nomeado por porta para coordenação entre processos; testes usarão
fake determinístico. A serial só abre depois de `ACQUIRE_OK`.

### D-005 — Host e rede

O contrato global usa `localhost`. A topologia padrão usa `adb reverse`, de
modo que o Android continua conectando em `localhost` e o host escuta em
`127.0.0.1`. `10.0.2.2` é uma alternativa explícita do Emulator, nunca um
novo default.

### D-006 — Mobile binding

O package `mobile` será a única superfície bindada. Ele usa `operationID` e
métodos síncronos simples; o consumidor Kotlin executará trabalho bloqueante
fora da thread principal e chamará `Cancel` em lifecycle/cancelamento. O
`context.Context` nunca será tipo de fronteira.

### D-007 — Android profile

Não haverá dependência Android no core Go. A arquitetura Android da Change 068
será SIMPLE/um módulo `app`, Compose, ViewModel, UDF e DI manual, apenas
porque o aplicativo será um laboratório pequeno sem persistência de produto.
Essa decisão segue a orientação oficial de separar UI/data e manter estado fora
da Activity; ela não altera esta Change.

## 4. Gates

1. **Gate 1 — Binding:** package mínimo → `gomobile bind` → AAR → Kotlin.
2. **Gate 2 — Facade:** chamada sem hardware com fake transport.
3. **Gate 3 — EmulatorTransport:** stream fragmentado com fake Bridge.
4. **Gate 4 — Bridge:** Bridge com fake serial e ownership.
5. **Gate 5 — Serial real:** bytes raw Bridge → COM, sem exigir todas as telas.

O Gate 6, composto pelo Functional Lab e pinpad real a partir do Android, é da
Change 068.

## 5. Fontes de decisão

- Go Mobile documenta geração de bindings para Android, AAR e a limitação a
  um subconjunto de tipos Go: `https://go.dev/wiki/Mobile`.
- A documentação de `gomobile bind` descreve AAR, targets e arquiteturas:
  `https://pkg.go.dev/golang.org/x/mobile/cmd/gomobile`.
- A arquitetura Android oficial recomenda separação de responsabilidades,
  UI/data layers, UDF e estado fora de componentes efêmeros:
  `https://developer.android.com/topic/architecture`.
- Compose recomenda UI imutável e fluxo unidirecional:
  `https://developer.android.com/develop/ui/compose/architecture`.

As fontes foram consultadas em 2026-09-27. Versões de AGP, Kotlin, Compose e
SDK não são fixadas nesta Change; serão decididas na Change 068 com toolchain
disponível.

## 6. Fallback formal

Se a dependência atual impedir `gomobile bind`, a primeira alternativa é
separar um pacote mobile puro que exclua serial Windows, REST, CLI e dependências
não suportadas, mantendo o mesmo core e a mesma porta `Transport`. Se ainda
assim o binding for inviável, a implementação deve parar e abrir nova revisão
com ADR para JNI/NDK. Não é aceitável substituir o core por REST ou copiar
comandos ABECS para Kotlin.
