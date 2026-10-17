# Revisão da SPEC — 10006-layout-login-referencia-svg

## Status

`SPEC_APROVADA`

## Escopo revisado

Foram revisados `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, o workflow
compartilhado e o contrato arquivado da Change 10001.

## Achados

| ID | Severidade | Evidência | Impacto | Recomendação | Resultado |
| --- | --- | --- | --- | --- | --- |
| REV-001 | Baixa | O SVG posiciona `CONFIRMAR` verticalmente e não apresenta `ENTER`; o contrato arquivado da Change 10001 ainda descrevia `ENTER`. | O histórico estava desatualizado em relação ao produto informado pelo solicitante. | Registrar que `ENTER` já foi excluído, manter o espaço livre ao lado de `ESPAÇO` e posicionar `CONFIRMAR` na coluna vertical do SVG. | Corrigido na SPEC após esclarecimento do solicitante. |

## Verificações

- Objetivo e escopo estão limitados ao layout Compose do plugin de login.
- Estado, eventos, autenticação, foco, sessão e navegação estão
  explicitamente fora de alteração.
- A referência visual possui caminho, `viewBox` e SHA-256 registrados.
- Os critérios de aceite são verificáveis por semântica, build, testes e
  inspeção visual em paisagem.
- Não há mudança arquitetural nem decisão que exija ADR.

## Conclusão

O único achado foi resolvido no contrato, incluindo o esclarecimento posterior
de que `ENTER` já havia sido excluído. Não restam ressalvas materiais;
`proposal.md`, `spec.md` e `DESIGN.md` podem ser promovidos para
`SPEC_APROVADA` e a implementação está autorizada.
