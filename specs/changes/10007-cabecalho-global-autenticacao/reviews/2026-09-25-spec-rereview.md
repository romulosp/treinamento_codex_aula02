# Nova revisão da SPEC — 10007-cabecalho-global-autenticacao

## Status

`SUPERADA`

## Escopo revisado

Foram reavaliados `proposal.md`, `spec.md`, `DESIGN.md` e `tasks.md` depois das
decisões formais fornecidas para resolver `REV-001`, `REV-002` e `REV-003` do
relatório `2026-09-25-spec-review.md`.

## Resolução dos achados

| ID | Evidência da resolução | Resultado |
| --- | --- | --- |
| `REV-001` | RF-02 agora define JWKS do realm, assinatura RSA, allowlist de algoritmos, issuer exato, `exp`, `iat`, `azp`, `typ`, tolerância, configurações e comportamento de falha. | Resolvido |
| `REV-002` | RF-02 publica a forma completa do JSON e preserva `autenticado=true`; o cenário 3 foi alinhado. | Resolvido |
| `REV-003` | RF-04 define os dois campos de `BuildConfig`, suas propriedades Gradle/ambiente, string vazia padrão, normalização e fallback localizado. | Resolvido |

## Verificações

- O contrato de segurança do token é determinístico e testável sem expor o
  JWT fora do backend.
- O DTO REST possui forma inequívoca e minimizada.
- A configuração visual do host possui nomes, precedência e fallback
  reproduzíveis.
- Objetivo, escopo, arquitetura, riscos e demais critérios de aceite permanecem
  coerentes com a proposta original.
- Não restam achados bloqueantes ou importantes nem decisão pendente de ADR.

## Veredito original

`SPEC_APROVADA`

Os três achados anteriores foram resolvidos no contrato. A Change pode ser
promovida para implementação.

## Decisão posterior

Este parecer foi superado antes de qualquer alteração em código. O solicitante
retirou a validação criptográfica adicional e as configurações de terminal e
lotérica do escopo. A revisão vigente está em
`2026-09-25-spec-rereview-02.md`.
