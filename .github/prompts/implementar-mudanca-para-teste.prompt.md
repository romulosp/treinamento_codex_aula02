---
name: "Implementar mudança para teste humano"
description: "Executa uma mudança Spec Driven até IMPLEMENTADA e para antes de revisão, validação e commit."
argument-hint: "Informe o caminho, por exemplo specs/changes/029-minha-mudanca"
agent: "implementador-para-teste"
---

Execute a mudança indicada em `${input:changePath}` usando o agente `implementador-para-teste`. Pare obrigatoriamente quando a implementação e os testes técnicos aplicáveis terminarem, mantendo a mudança em `specs/changes/` para avaliação humana.
