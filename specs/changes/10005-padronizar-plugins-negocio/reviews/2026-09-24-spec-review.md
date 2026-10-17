# Revisão da SPEC 10005

## Veredito
`SPEC_APROVADA`

| ID | Severidade | Evidência | Decisão |
| --- | --- | --- | --- |
| REV-001 | Corrigido | Um launcher debug contornava login, discovery e menu do microkernel. | Toda execução humana ocorre por `:app`; launcher em plugin é proibido. |
| REV-002 | Resolvido | Credenciais poderiam atravessar a fronteira do host. | Padrão proíbe credenciais em rotas, logs, manifestos e menu e exige descarte local. |

Objetivo, escopo, segurança e critérios de aceite são verificáveis; não há ressalva material.
