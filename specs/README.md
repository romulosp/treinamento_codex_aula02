# Especificações

`system/` documenta o sistema vigente. `changes/` contém propostas e implementações em andamento. Após aprovação formal, a mudança atualiza `system/`, é preparada em `archive/` e ambas as alterações são registradas no mesmo commit.

Use os modelos em `templates/`, as regras comuns em `shared/` e o fluxo oficial em `shared/process/workflow.md`. Os prompts da Aula 01 permanecem como referência em `legacy-prompts/`.

`sprint/` organiza a ordem e o acompanhamento de Changes sem substituir o workflow Spec Driven. Cada Sprint concreta usa o modelo e os prompts desse diretório, referencia somente Changes em andamento e registra os gates, evidências, riscos e resultados de cada entrega.

## Change 066 — lib-pinpad-abecs-go

A Change [2026-09-09-066-lib-pinpad-abecs-go](changes/2026-09-09-066-lib-pinpad-abecs-go/) descreve a conversão do domínio Java + JNI + C de comunicação com pinpad ABECS para uma biblioteca Go headless, sem HTTP, WebSocket ou UI.

O projeto conversa com pinpad físico por porta serial e usa o manual ABECS v2.12 fornecido para a Change como referência normativa. O código legado é usado somente para rastreabilidade comportamental.

Cada comando possui uma SPEC individual, incluindo ciclo de vida (`CAN`, `OPN`, `CLO`, `CLX`, `RST`), informações (`GIX`), display (`DSP`, `DEX`, `MNU`, `DSI`), multimídia (`MLI`, `MLR`, `MLE`), tabelas EMV (`TLI`, `TLR`, `TLE`), teclas (`GKY`), transação/cartão (`GCX`, `GTK`, `GOX`, `FCX`) e PIN (`GPN`). Logging SPE/PP/RSP, cancelamento serial e comunicação segura possuem especificações transversais próprias.

Uma SPEC em `RASCUNHO` não autoriza implementação. Cada contrato deve passar por revisão formal, implementação, revisão da implementação, testes automatizados e, quando envolver hardware, validação com pinpad físico real.
