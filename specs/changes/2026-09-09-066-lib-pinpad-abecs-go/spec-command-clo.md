# SPEC: 066-lib-pinpad-abecs-go — Comando CLO

## Status
`RASCUNHO`

## Objetivo
Definir o encerramento lógico da comunicação ABECS antes do fechamento da porta serial. `Close` deve separar o encerramento do protocolo do fechamento do handle físico.

## Escopo
- Pacotes: `internal/application/service`, `internal/domain/command`, `internal/domain/parser` e `internal/infrastructure/serial`.
- Payload lógico: `CLO000`, conforme perfil suportado pelo manual.
- Não fechar a porta enquanto houver comando de hardware em execução, salvo shutdown/cancelamento explicitamente definido.

## Contrato
- Recusar fechamento normal enquanto o serviço estiver `BUSY`.
- Enviar CLO pelo worker quando a sessão ABECS exigir.
- Validar ACK, status, timeout e erros.
- Registrar `SPE CMD=CLO`, `PP`, `RSP CMD=CLO STATUS=...` e `close()`.
- O encerramento seguro `CLX`, se exigido, possui SPEC própria.

## Critérios de aceite
- [ ] CLO é transmitido a um pinpad real quando aplicável.
- [ ] Fechamento idempotente não gera bytes duplicados.
- [ ] Falha de CLO não oculta falha de fechamento da porta.
- [ ] Shutdown encerra worker e porta sem vazamento.
- [ ] Nenhum dado sensível aparece no rastro.

## Referências
`spec.md` RF-010, RF-013, fila/worker e `spec-logging.md`; manual ABECS v2.12.
