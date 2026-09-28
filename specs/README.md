# Especificações

Autor: Rômulo Penha

`system/` documenta o sistema vigente. `changes/` contém propostas e implementações em andamento. Após aprovação formal, a mudança atualiza `system/`, é preparada em `archive/` e ambas as alterações são registradas no mesmo commit.

Use os modelos em `templates/`, as regras comuns em `shared/` e o fluxo oficial em `shared/process/workflow.md`. Os prompts da Aula 01 permanecem como referência em `legacy-prompts/`.

`sprint/` organiza a ordem e o acompanhamento de Changes sem substituir o workflow Spec Driven. Cada Sprint concreta usa o modelo e os prompts desse diretório, referencia somente Changes em andamento e registra os gates, evidências, riscos e resultados de cada entrega.

## Change 066 — lib-pinpad-abecs-go

A Change [2026-09-09-066-lib-pinpad-abecs-go](changes/2026-09-09-066-lib-pinpad-abecs-go/) descreve a integração de comunicação com pinpad ABECS v2.12 em uma biblioteca Go headless, sem HTTP, WebSocket ou UI.

O projeto conversa com pinpad físico por porta serial e usa o manual ABECS v2.12 fornecido para a Change como referência normativa. A base de referência comportamental é a documentação do protocolo e os testes de hardware do dispositivo.

Cada comando possui uma SPEC individual, incluindo ciclo de vida (`CAN`, `OPN`, `CLO`, `CLX`, `RST`), informações (`GIX`), display (`DSP`, `DEX`, `MNU`, `DSI`), multimídia (`MLI`, `MLR`, `MLE`), tabelas EMV (`TLI`, `TLR`, `TLE`), teclas (`GKY`), transação/cartão (`GCX`, `GTK`, `GOX`, `FCX`) e PIN (`GPN`). Logging SPE/PP/RSP, cancelamento serial e comunicação segura possuem especificações transversais próprias.

Uma SPEC em `RASCUNHO` não autoriza implementação. Cada contrato deve passar por revisão formal, implementação, revisão da implementação, testes automatizados e, quando envolver hardware, validação com pinpad físico real.

## Change 067 — Android Emulator Transport Bridge

A Change [2026-09-27-067-android-emulator-transport-bridge](changes/2026-09-27-067-android-emulator-transport-bridge/) especifica a fundação de transporte entre o core Go executado no Android Emulator e o pinpad físico conectado ao Windows. Ela separa `Transport`, `EmulatorTransport`, Bridge, envelope, ownership da COM e binding Go da UI Android.

O aplicativo `diagnosticopinpad` pertence à Change 068 dependente. Sua implementação funcional começa somente após a prova do Gate 1 (`gomobile` → AAR → Kotlin). A Change 067 não contém telas, código Android ou validação funcional pelo pinpad.

## Change 068 — diagnosticopinpad

A Change [2026-09-27-068-diagnosticopinpad](changes/2026-09-27-068-diagnosticopinpad/) especifica o primeiro aplicativo Android nativo de diagnóstico. Ela consome a fundação da Change 067, preserva o envelope binário `PBRG` v1 e limita o laboratório inicial a versão, ping, conexão, `GetInfo`, fechamento e cancelamento.

A SPEC está aprovada, mas sua implementação depende da revisão de implementação e do Gate 1 de `gomobile bind` da Change 067. O catálogo completo de comandos ABECS permanece para Change posterior.

## Changes 069–071 — catálogo, log e launcher

- [069 — Functional Lab](changes/2026-09-27-069-diagnosticopinpad-functional-lab/): catálogo tipado Android; gates físicos permanecem explícitos.
- [070 — Bridge log Android](changes/2026-09-27-070-bridge-log-android/): destino compartilhado `LogPinpadAbecs.txt`.
- [071 — BAT de teste Windows](changes/2026-09-27-071-bat-teste-modulo-windows/): launcher físico; contrato da variável retificado pela 072.

## Change 072 — correção da abertura Android/Bridge

A [Change 072](changes/2026-09-27-072-corrigir-abertura-bridge-android/)
especifica preservação de PORTA_PINPAD, readiness real, reverse verificável,
alimentação do log por eventos técnicos, erros visíveis e estado de sessão
coerente com o Go. Inclui regressão scripted e gate físico COM14. Consulte os
documentos da change para o status atual; revisão de SPEC não equivale a
correção implementada ou hardware validado.
