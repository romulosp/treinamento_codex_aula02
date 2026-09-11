# SPEC: 066-lib-pinpad-abecs-go — Comando TLE

## Status
`SPEC_APROVADA`

## Objetivo
Encerrar e confirmar a carga de tabela EMV iniciada por TLI e alimentada por TLR.

## Contrato
- Enviar somente após TLI status `000` e todos os TLR concluídos.
- Não enviar quando TLI retornar `020`.
- Interpretar confirmação final e status ABECS sem declarar sucesso antecipado.
- Atualizar o progresso/estado da tabela somente após confirmação do pinpad.

## Critérios de aceite
- [ ] TLE conclui tabela válida em pinpad real.
- [ ] TLE não é enviado depois de TLI `020`.
- [ ] Falha real de TLE retorna erro e não marca tabela como carregada.
- [ ] Nova tentativa é possível sem deixar worker ou sessão presos.

## Referências
`spec.md` RF-012.5, `spec-command-tli.md`, `spec-command-tlr.md`; manual ABECS v2.12.
