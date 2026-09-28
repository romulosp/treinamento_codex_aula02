# Proposta — 067 Android Emulator Transport Bridge

Autor: Rômulo Penha

## Status

`SPEC_APROVADA`

## Objetivo

Criar a fundação de transporte que permita executar o core Go ABECS no Android
Emulator e alcançar um pinpad físico conectado ao Windows, sem duplicar o
protocolo ABECS, seus comandos, CRC, parser ou máquina de estados.

Esta Change é deliberadamente separada do aplicativo Android de diagnóstico.
O aplicativo `diagnosticopinpad` será especificado e implementado na Change
068, consumindo somente o artefato de binding aprovado nesta Change.

## Contexto observado

- O módulo Go existente está em `apps/desktop/libpinpadabecsgo`.
- A fachada atual `internal/application/service.PinpadService` recebe uma
  interface de porta serial e usa `context.Context` internamente.
- O adaptador físico encapsula `go.bug.st/serial`.
- Não existe aplicativo Android no repositório.
- O ambiente desta revisão não possui `gomobile`, Java, Android SDK, ADB ou
  Gradle; portanto, a compatibilidade Go → AAR → Kotlin precisa ser um gate
  executado em ambiente Android preparado.

## Escopo

- contrato de transporte independente de COM, TCP e Android;
- `EmulatorTransport` para stream bidirecional persistente;
- Android Transport Bridge no desktop Windows;
- envelope versionado para o stream do Bridge;
- ownership exclusivo da porta COM por sessão;
- configuração, `adb reverse`, fallback explícito `10.0.2.2` e observabilidade;
- fachada Go mínima e estável para o binding Android;
- testes unitários e de integração sem hardware;
- gate de viabilidade `gomobile bind` e AAR mínimo.

## Fora do escopo

- telas, ViewModel, Compose e UX do `diagnosticopinpad`;
- publicação ou distribuição do aplicativo Android;
- Android USB Host, `AndroidUsbTransport` e comunicação USB direta;
- reimplementação de comandos ABECS;
- uso da API REST como fronteira Android → Windows;
- exposição do Bridge em LAN ou transformação em serviço de produção;
- validação funcional completa pelo pinpad, que pertence ao laboratório da
  Change 068 após os gates desta Change.

## Resultado esperado

Ao final desta Change haverá um contrato aprovado e uma implementação validável
que permita:

```text
core Go ABECS → Transport → TCP persistente → Bridge → Transport serial → COM
```

O app Android será uma consumidora posterior, não uma responsabilidade desta
entrega.
