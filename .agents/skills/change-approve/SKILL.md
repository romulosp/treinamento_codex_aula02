---
name: change-approve
description: 'Use when: consolidar revisão de SPEC, revisão de implementação e validação para aprovar ou reprovar uma mudança.'
argument-hint: 'Informe o caminho da mudança a aprovar.'
---

# Aprovação de mudança

1. Confirme `SPEC_APROVADA`, `IMPLEMENTADA`, `IMPLEMENTACAO_APROVADA` e `VALIDADA` sem pendências bloqueantes ou importantes.
2. Produza `AAAA-MM-DD-approval.md` em `reviews/`, com pré-condições, evidências, decisão `APR-001` e veredito `APROVADA` ou `REPROVADA`.
3. A aprovação não corrige código, não executa testes e não cria requisitos.
4. Quando aprovada, atualize `STATUS.md` para `APROVADA` e encaminhe a mudança para a Skill `git-commit`.
5. Não atualize `system/` nem mova para `archive/` antes da aprovação.
