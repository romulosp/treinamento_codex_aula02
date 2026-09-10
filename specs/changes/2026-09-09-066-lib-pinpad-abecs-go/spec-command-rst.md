# SPEC: 066-lib-pinpad-abecs-go — Comando RST

## Status
`RASCUNHO`

## Objetivo
Reinicializar o estado lógico do pinpad por comando tipado, preservando o ciclo de vida da porta e do worker.

## Contrato
- Builder deve produzir somente o payload RST documentado.
- Operação exige pinpad aberto e estado BUSY exclusivo.
- Aguardar ACK/status e o tempo de estabilização definido pelo manual/configuração usando contexto, nunca sleep não cancelável.
- Após sucesso, invalidar estado transitório da transação, sem fechar a porta automaticamente salvo regra do dispositivo.
- Registrar `SPE CMD=RST`, `PP` e `RSP CMD=RST STATUS=...`.

## Critérios de aceite
- [ ] RST real reinicializa o pinpad e permite nova operação.
- [ ] Falha ou cancelamento devolve erro e deixa estado documentado.
- [ ] Operações concorrentes são rejeitadas sem interleaving.
- [ ] Shutdown durante RST é idempotente.

## Referências
`spec.md` RF-012.9/RF-013, `spec-infra-serial-cancel.md`; manual ABECS v2.12.
