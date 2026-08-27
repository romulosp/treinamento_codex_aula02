---
name: git-commit
description: 'Use when: encerrar uma mudança Spec Driven aprovada, atualizar a especificação vigente, preparar o archive e criar o commit rastreável.'
argument-hint: 'Informe o caminho da mudança aprovada e a mensagem de commit.'
---

# Encerramento e commit

1. Confirme o relatório de aprovação com estado `APROVADA` e a árvore Git conhecida.
2. Atualize `specs/system/` para refletir a verdade vigente; não copie relatórios transitórios para essa área.
3. Mova a pasta da mudança para `specs/archive/AAAA-MM-DD-<id>-<nome>/`.
4. Revise o conjunto de arquivos que será incluído no commit; não inclua segredos, `target/` ou artefatos locais.
5. Crie um único commit que contenha código, atualização de `system/` e o archive da mudança.
6. Atualize `STATUS.md` para `ARQUIVADA` somente depois do commit bem-sucedido e registre o hash no relatório de aprovação.