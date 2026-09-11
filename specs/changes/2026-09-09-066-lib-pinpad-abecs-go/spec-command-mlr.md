# SPEC: 066-lib-pinpad-abecs-go — Comando MLR

## Status
`SPEC_APROVADA`

## Objetivo
Transmitir blocos de um arquivo multimídia iniciado por `MLI`.

## Contrato
- Cada bloco deve respeitar `MLR_MAX_BLOCK_SIZE` e o limite do manual.
- A ordem dos blocos deve ser preservada pelo worker.
- Progresso deve ser calculado por bytes efetivamente aceitos, com contexto.
- Retransmissão e ACK/NAK devem seguir o protocolo ABECS.
- Não registrar conteúdo binário; logar apenas índice/tamanho e resultado seguro.

## Critérios de aceite
- [ ] Todos os blocos chegam ao pinpad real na ordem correta.
- [ ] Bloco intermediário rejeitado interrompe o fluxo sem enviar MLE.
- [ ] Cancelamento não deixa arquivo parcialmente anunciado como concluído.
- [ ] Arquivos com tamanho exato, menor e maior que um bloco são cobertos.

## Referências
`spec.md` RF-012.4, `spec-command-mli.md`, `spec-command-mle.md`; manual ABECS v2.12.
