# Nova revisão da SPEC — 10007-cabecalho-global-autenticacao

## Status

`SPEC_APROVADA`

## Escopo revisado

Foram reavaliados `proposal.md`, `spec.md`, `DESIGN.md` e `tasks.md` após a
decisão posterior do solicitante que substituiu a primeira resolução dos
achados `REV-001`, `REV-002` e `REV-003`.

## Resolução vigente

| ID | Evidência da resolução | Resultado |
| --- | --- | --- |
| `REV-001` | RF-02 limita a Change à decodificação do token recebido do endpoint do Keycloak, proíbe afirmar validação criptográfica adicional e registra o risco aceito. | Resolvido por decisão de escopo |
| `REV-002` | RF-02 publica o JSON completo, preservando `autenticado=true` e acrescentando apenas o perfil usado pelo cabeçalho. | Resolvido |
| `REV-003` | RF-04 remove `LOTÉRICA` e qualquer valor sem fonte; conserva somente o label visual `TERMINAL`, sem configuração ou fallback. | Resolvido |

## Verificações

- O SVG autenticado exige username e nome exibível na primeira linha; por isso
  `username`, `displayName` e `roleLabel` são os únicos campos mantidos no
  perfil sanitizado.
- JWT e refresh token permanecem restritos ao backend.
- O risco residual da decodificação sem verificação criptográfica adicional
  está explícito e não é apresentado como garantia de segurança.
- O contrato visual não inventa terminal, lotérica ou outra identidade ausente.
- Não restam ambiguidades materiais nem decisão pendente de ADR.

## Veredito

`SPEC_APROVADA`

A implementação está autorizada exclusivamente nos limites desta revisão.
