# Revisão de SPEC — conformidade ABECS 2.12

**Data:** 2026-09-13  
**Estado de entrada:** `EM_REVISAO_SPEC`  
**Escopo:** correções normativas decorrentes de
`2026-09-12-implementation-review-3.md`.

## Fontes revisadas

- `proposal.md`, `spec.md`, `DESIGN.md` e `tasks.md`;
- SPECs individuais e transversais da Change 066;
- `spec-conformidade-abecs-v212.md`;
- manual
  `docs/Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212/Pinpad-Abecs-Protocolo-de-Comunicacao-e-Funcionamento-v212190411212.pdf`,
  revisão 2.12 de 11-abr-2019;
- `specs/shared/process/workflow.md`.

## Verificações

- A fonte normativa e sua precedência estão explícitas.
- Transporte define limites, prazos, ACK/NAK, três tentativas, resposta válida
  sem ACK e cancelamento CAN/EOT.
- OPN, CLO e a proteção segura distinguem comandos e respostas claras.
- Builders e parsers possuem tipos, comprimentos e condições verificáveis byte
  a byte.
- GCX define data `AAMMDD` e opções N5 sem ambiguidade.
- GKY e GPN seguem seus formatos clássicos, sem parâmetros inventados.
- Multimídia e tabelas EMV definem TLVs, CRC16, tamanhos e semântica dos status.
- RST está formalmente excluído por ausência no manual 2.12.
- O aceite automatizado e a evidência física estão separados.

## Achados

Não foram encontrados bloqueios, ambiguidades materiais ou requisitos
incompatíveis no escopo corrigido.

## Decisão

`SPEC_APROVADA`

A implementação está autorizada somente para os contratos aprovados nesta
revisão. A validação física permanece necessária para declarar funcionamento
real dos comandos que dependem do pinpad, cartão ou interação do operador.

