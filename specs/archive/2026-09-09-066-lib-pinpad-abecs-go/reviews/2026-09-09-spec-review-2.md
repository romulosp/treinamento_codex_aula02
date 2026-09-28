# Revisão da SPEC (reavaliação) — 066-lib-pinpad-abecs-go

**Data:** 2026-09-09
**Resultado:** `SPEC_APROVADA`
**Referência:** revisão anterior em [2026-09-09-spec-review.md](2026-09-09-spec-review.md)

## Escopo revisado

Reavaliação de `spec.md`, `proposal.md`, `DESIGN.md`, `tasks.md` e `implementation-plan.md` após as correções aplicadas para resolver os achados REV-001 a REV-007.

## Verificação dos achados anteriores

### REV-001 — Contradição sobre a existência de service — Resolvido

RF-001 foi reescrito para distinguir explicitamente "sem service" (ausência de processo servidor externo, rede, REST, WebSocket, gRPC, UI, daemon) da fachada interna de aplicação (`internal/application/service`), que é a única camada de orquestração permitida e não abre rede. `DESIGN.md` foi alinhado à mesma distinção.

### REV-002 — Dependência para geração de QR Code não definida — Resolvido

RF-013 define a interface `QRCodeGenerator` com `Generate(data string, size int) ([]byte, error)`, injetada pelo consumidor. Nenhuma dependência concreta é adicionada ao módulo nesta Change. Erro `ErrQRCodeGeneratorNotConfigured` foi adicionado a RF-006. Limites de tamanho (50–320) e margem (0–10) são validados antes da chamada ao gerador; `xPos`/`yPos` são reportados como não suportados de forma explícita no resultado, não silenciosamente.

### REV-003 — Contrato de `SendRawCommand` permanece ambíguo — Resolvido

`SendRawCommand` foi removido da fachada pública. RF-013 declara explicitamente que nenhuma API de comando bruto será exposta, em conformidade com RF-015. A matriz de rastreabilidade foi atualizada para refletir a remoção.

### REV-004 — Semântica de `CommandQueue.Enqueue` e `Dequeue` precisa ser única — Resolvido

RF-009 agora define univocamente duas operações: `Enqueue` (inserção não bloqueante, retorno imediato, `ErrQueueFull` sem bloquear o produtor) e `Submit` (bloqueante, aguarda resultado ou cancelamento de contexto). RF-014 foi ajustado para remover a duplicidade e referenciar RF-009 como fonte única do contrato da fila.

### REV-005 — Contrato de GCX possui campos sem serialização especificada — Resolvido

O escopo de `TransactionGCX` com parâmetros completos (AIDs, cashback, moeda, EMV, lista de tags) foi explicitamente removido desta Change e substituído por um stub `ErrNotImplemented`, sem serialização inventada. `SendGCXInitialization` e `PurchaseGCX` permanecem no subconjunto já especificado em RF-012.7 (valor, data, hora, opções). Erro `ErrNotImplemented` adicionado a RF-006.

### REV-006 — Critérios de aceite não refletem a nova matriz completa — Resolvido

Foram adicionados critérios específicos: CA-016a (QR Code), CA-016b (stub `TransactionGCX`), CA-016c (`StatusTableVersionDifferent` em TLI), e CA-018 foi reescrito para detalhar `Enqueue`, `Submit`, `Clear`, `Stop` e `SessionManager` separadamente.

### REV-007 — Plano de implementação estava desatualizado — Resolvido

`implementation-plan.md` foi atualizado para refletir a fachada de aplicação, a semântica `Enqueue`/`Submit`, o `QRCodeGenerator` injetável e o stub `ErrNotImplemented` de `TransactionGCX`, mantendo sua natureza de plano preparatório sem alterar os gates do workflow.

## Conclusão

Todos os achados bloqueantes e de severidade alta/média da revisão anterior foram resolvidos com alterações rastreáveis em `spec.md`, `DESIGN.md`, `tasks.md` e `implementation-plan.md`. O contrato está claro, consistente, testável e sem escopo de servidor.

**Resultado:** `SPEC_APROVADA`
