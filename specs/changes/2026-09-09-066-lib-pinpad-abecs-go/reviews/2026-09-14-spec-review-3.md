# Revisão da regra MLI para tipo desconhecido

**Data:** 2026-09-14  
**Escopo:** aditivo a `spec-command-mli.md` e alinhamento da conformidade ABECS.
**Fonte:** manual ABECS v2.12, seções 3.4.1 (p. 93) e 6.6.1 (p. 205).

## Achado

### REV-005 — MLI não pode rejeitar tipo desconhecido no host

**Severidade:** alta.  
**Evidência:** a seção 3.4.1 define tipo `01h` PNG, `02h` JPG, `03h` GIF e
outros valores como RUF. A seção 6.6.1 exige que o pinpad não critique o tipo
durante MLI e só avalie o suporte no uso do arquivo. A SPEC anterior rejeitava
qualquer assinatura desconhecida antes da serial.
**Impacto:** a biblioteca impedia a carga de arquivos opacos que o dispositivo
deveria aceitar, contrariando o ciclo MLI/MLR/MLE definido pelo manual.
**Decisão:** resolvido no contrato. O host transmite tipo desconhecido com
`SPE_MFINFO.B1=00h` (RUF), mantendo nome A8, tamanho X4, CRC16 B2 e três bytes
RUF finais. O firmware pode rejeitar o formato somente no DSI. Arquivo vazio,
nome inválido e tamanho fora do campo continuam rejeitados localmente.

## Consistência e critérios

`proposal.md`, `spec.md`, `spec-command-mli.md`,
`spec-conformidade-abecs-v212.md`, `DESIGN.md`, `tasks.md` e
`implementation-plan.md` foram alinhados. Os critérios automatizados verificam
o vetor MLI com `B1=00h` e que a sequência MLR/MLE continua possível. A resposta
de formato do DSI continua dependente do firmware e validação física.

## Decisão

REV-005 está resolvido. O contrato está completo e consistente com as seções
citadas.

`SPEC_APROVADA`
