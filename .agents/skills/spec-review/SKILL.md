---
name: spec-review
description: 'Use when: revisar uma proposta, SPEC, critérios de aceite ou documento de mudança antes da implementação.'
argument-hint: 'Informe o caminho da mudança a revisar.'
---

# Revisão de SPEC

1. Leia `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md` e as regras em `specs/shared/`.
2. Confirme objetivo, escopo, fora de escopo, requisitos funcionais e não funcionais, dependências, riscos e critérios de aceite verificáveis.
3. Identifique ambiguidades, contradições e decisões que ainda exigem ADR. Não altere a implementação nem invente requisitos.
4. Salve `AAAA-MM-DD-spec-review.md` em `reviews/`, com achados `REV-001`, severidade, evidência, impacto e recomendação.
5. Conclua somente com `SPEC_APROVADA` ou `REPROVADA`. Ressalvas importantes impedem a implementação.
