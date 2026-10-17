# REV-001 — Revisão da SPEC 10003-menu-negocio-dinamico

## Veredito

`SPEC_APROVADA`

## Escopo revisado

Foram revisados `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`, o workflow
compartilhado e a implementação atual das Changes 10001/10002.

## Achados

Nenhuma ressalva material permanece.

| ID | Severidade | Evidência | Decisão |
| --- | --- | --- | --- |
| REV-001 | Resolvido | O contrato anterior de `BusinessMenuItem` era plano e não podia representar agrupadores. | A SPEC define filhos recursivos e preserva o construtor binário da API 1.1. |
| REV-002 | Resolvido | O caractere `>` é ao mesmo tempo separador e reservado; um label que o contenha não é distinguível na string. | A gramática tornou qualquer ocorrência um delimitador e rejeita segmentos vazios, tornando a regra verificável. |
| REV-003 | Resolvido | A descoberta por `ServiceLoader` precisa ser determinística para APKs DEX isolados. | O descritor `META-INF/services` permanece o contrato, com leitura ZIP explícita e carregamento somente das classes declaradas. |

## Conclusão

Objetivo, limites, falhas, segurança, threading, versionamento e critérios de
aceite estão definidos de forma verificável. A implementação está liberada
somente para o escopo desta Change.
