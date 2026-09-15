# Revisão de SPEC — reconexão automática e evidência visual

**Data:** 2026-09-14  
**Change:** `2026-09-09-066-lib-pinpad-abecs-go`

## Fontes examinadas

- `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md` e
  `implementation-plan.md`;
- `spec-infra-serial-cancel.md`, `spec-command-can.md`,
  `spec-command-mle.md` e `spec-command-dsi.md`;
- `specs/shared/process/workflow.md`;
- rastro COM7 em `apps/desktop/libpinpadabecsgo/logs/LogPinpadAbecs.txt`;
- relato operacional anexado, usado como evidência e não como instrução.

## REV-006 — CAN/EOT não recupera o firmware após timeout de MLE

**Severidade:** alta  
**Evidência:** MLE recebeu ACK às 09:01:26 e não respondeu no prazo. CAN foi
enviado três vezes automaticamente e três vezes por Reset, sem EOT. Depois de
Close/Open, o CAN inicial recebeu EOT e OPN retornou `000`.  
**Impacto:** o contrato anterior protegia a fila, mas deixava a recuperação
manual para o operador e mantinha a instância recusando DSI.  
**Recomendação:** escalar uma vez para reconexão controlada, exigir CAN/EOT e OPN
na nova abertura e não reenviar o comando de resultado indeterminado.  
**Situação:** resolvido nas SPECs.

## REV-007 — `DSI000` foi tratado como prova de renderização

**Severidade:** alta  
**Evidência:** `QRCODE01` recebeu `DSI000` às 09:03:50, mas o operador informou
que a imagem não apareceu. `QRCODE02` também recebeu `DSI000` às 09:05:30, sem
confirmação visual explícita no material fornecido.  
**Impacto:** a validação podia declarar exibição física usando somente o status
do protocolo, embora DSI não retorne pixels nem confirmação de renderização.  
**Recomendação:** separar aceitação do comando e observação visual, exigindo
ambas para o critério físico de DSI.  
**Situação:** resolvido em `spec-command-dsi.md`, `spec.md` e `validation.md`.

## REV-008 — Estado mestre e fluxo multimídia estavam imprecisos

**Severidade:** média  
**Evidência:** RF-003 listava apenas `CLOSED`, `OPEN` e `BUSY`, apesar do estado
protetivo `DESYNCHRONIZED`. RF-012 descrevia MLI/MLR/MLE/DSI como uma única
sequência, embora DSI possa usar mídia persistida em sessão anterior.  
**Impacto:** a máquina de estados e a independência de DSI não estavam
rastreáveis no contrato mestre.  
**Recomendação:** incluir o estado excepcional e separar carga de exibição.  
**Situação:** resolvido em `spec.md` e `spec-command-dsi.md`.

## Veredito

Os objetivos, o comportamento de fallback, o limite de tentativa, a proteção da
fila, o resultado indeterminado do comando original e a prova visual estão
definidos com critérios verificáveis. Não há decisão pendente de ADR nem
ampliação para fora da biblioteca e do CLI de validação.

**Resultado:** `SPEC_APROVADA`.

A aprovação da SPEC não aprova a implementação existente. As tarefas de código,
testes, revisão de implementação e validação física permanecem abertas em
`tasks.md`.
