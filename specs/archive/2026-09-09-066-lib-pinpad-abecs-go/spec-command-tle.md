# SPEC: 066-lib-pinpad-abecs-go — Comando TLE

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Encerrar e confirmar a carga de tabela EMV iniciada por TLI e alimentada por TLR.

## Contrato
- Enviar após TLI status `000` ou `020` e todos os TLR concluídos.
- Enviar também quando TLI retornar `020`.
- Interpretar confirmação final e status ABECS sem declarar sucesso antecipado.
- Atualizar o progresso/estado da tabela somente após confirmação do pinpad.
- O rastro serial registra `SPE CMD=TLE`, `PP` e `RSP CMD=TLE STATUS=...`
  conforme a política de redaction de `spec-logging.md`.

## Critérios de aceite
- [ ] TLE conclui tabela válida em pinpad real.
- [ ] TLE não é enviado depois de TLI `020`.
- [ ] Falha real de TLE retorna erro e não marca tabela como carregada.
- [ ] Nova tentativa é possível sem deixar worker ou sessão presos.
- [ ] O rastro identifica `CMD=TLE` sem expor registros da tabela EMV.

## Referências
`spec.md` RF-012.5, `spec-logging.md`, `spec-command-tli.md`,
`spec-command-tlr.md`; manual ABECS v2.12.


## Correção normativa de 2026-09-13

TLE é o payload literal `TLE` e encerra cargas iniciadas por TLI status 000 ou
020, depois da aceitação de todos os TLR.
