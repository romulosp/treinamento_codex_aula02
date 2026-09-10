# Revisão documental das SPECs — Change 066 lib-pinpad-abecs-go

**Data:** 2026-09-10
**Escopo:** README raiz, índice de SPECs, `proposal.md`, `spec.md`, `DESIGN.md`, `implementation-plan.md`, `tasks.md` e SPECs individuais da Change `2026-09-09-066-lib-pinpad-abecs-go`.

## Objetivo da revisão

Verificar se a documentação descreve exclusivamente a biblioteca Go de comunicação serial com pinpad ABECS, se a conversão Java + JNI + C está rastreável e se existe uma SPEC própria para cada comando ou fluxo transversal relevante.

## Alterações verificadas

- README raiz reescrito para o branch exclusivo `lib-pinpad-abecs`.
- Índice `specs/README.md` atualizado com a finalidade da Change 066.
- `spec.md`, `proposal.md`, `DESIGN.md`, `implementation-plan.md` e `tasks.md` alinhados ao produto headless, sem rede.
- Manual ABECS v2.12 identificado como referência normativa adotada nesta documentação.
- Matriz individual de comandos criada para `CAN`, `OPN`, `CLO`, `CLX`, `GIX`, `DSP`, `DEX`, `MNU`, `DSI`, `MLI`, `MLR`, `MLE`, `TLI`, `TLR`, `TLE`, `GKY`, `GCX`, `GTK`, `GOX`, `FCX`, `GPN` e `RST`.
- SPECs transversais mantidas para QR Code, logging SPE/PP/RSP, cancelamento serial e comunicação segura.
- GCX explicitamente limitado aos campos reais de GCX; GTK, GOX e FCX não são mais misturados no modelo de GCX.
- Critérios de aceitação física separados de testes unitários determinísticos.
- SPEC de logging retornada a `RASCUNHO`, pois o contrato foi ampliado depois da revisão anterior e precisa de nova aprovação formal.

## Achados

### REV-DOC-001 — SPECs individuais ainda não aprovadas

- **Severidade:** bloqueante para implementação.
- **Evidência:** as SPECs novas e alteradas permanecem com status `RASCUNHO`.
- **Impacto:** não é permitido iniciar a implementação desses contratos até revisão formal.
- **Recomendação:** executar revisão da SPEC, corrigir ressalvas e alterar para `SPEC_APROVADA` somente após aprovação.

### REV-DOC-002 — Conversão integral ainda depende do inventário do legado

- **Severidade:** alta.
- **Evidência:** a documentação define a matriz legado → Go → SPEC → evidência, mas ela ainda precisa ser preenchida com todas as funções Java, JNI e C/C++.
- **Impacto:** não é possível declarar equivalência integral somente pela presença dos arquivos de SPEC.
- **Recomendação:** inventariar as fontes legadas e marcar cada item como convertido, parcial, ausente ou fora de escopo.

### REV-DOC-003 — Comunicação segura depende de confirmação documental e física

- **Severidade:** alta.
- **Evidência:** `spec-protocolo-seguro.md` foi criada, mas está em `RASCUNHO` e ainda exige confirmação de formato RSA/AES, KSEC, IV, padding e sequência no manual/dispositivo.
- **Impacto:** não implementar criptografia por suposição nem declarar o legado integralmente convertido.
- **Recomendação:** aprovar a SPEC com os parâmetros confirmados e validar OPN seguro e encerramento em pinpad de laboratório.

### REV-DOC-004 — Evidência física ainda pendente

- **Severidade:** alta.
- **Evidência:** não há `validation.md` desta etapa com evidências reais para todos os comandos.
- **Impacto:** builders, parsers e fakes comprovam somente componentes determinísticos, não o comportamento do pinpad.
- **Recomendação:** executar validação por comando com porta serial e pinpad físico, usando cartão de laboratório e logs sanitizados.

## Resultado

`REPROVADA`

A documentação está significativamente mais completa e consistente, mas as SPECs permanecem em `RASCUNHO`, o inventário integral do legado ainda não foi concluído e a validação física ainda não foi registrada. Esses pontos impedem a aprovação da Change e a declaração de conversão integral.
