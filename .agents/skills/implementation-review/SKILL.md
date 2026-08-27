---
name: implementation-review
description: 'Use when: comparar código implementado com uma SPEC aprovada e identificar divergências de implementação.'
argument-hint: 'Informe o caminho da mudança e do módulo implementado.'
---

# Revisão de implementação

1. Confirme que a mudança está `IMPLEMENTADA` e compare cada requisito e critério de aceite com o código, configurações e testes.
2. Verifique escopo indevido, segredos, dependências, convenções de arquitetura e aderência a `AGENTS.md`.
3. Registre divergências como `IMP-REV-001`, com severidade, evidência, impacto e ação necessária.
4. Salve `AAAA-MM-DD-implementation-review.md` em `reviews/`.
5. Conclua com `IMPLEMENTACAO_APROVADA` ou `REPROVADA`. Não altere código durante a revisão.
