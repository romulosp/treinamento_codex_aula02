# Revisão de SPEC: 028-exemplo-site-web-001 — reestruturação frontend

## Veredito

`SPEC_APROVADA`

## Escopo revisado

Foram revisados `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md` e `validation.md`, em conjunto com `AGENTS.md` e `specs/shared/process/workflow.md`.

## Achados

### REV-001 — Caminho da aplicação definido por plataforma

- Severidade: resolvida antes do veredito.
- Evidência: a versão anterior reservava `apps/frontend/` para uma única aplicação, enquanto a organização solicitada prevê aplicações web, smartphone e desktop.
- Impacto: manter o caminho antigo permitiria arquivos de uma aplicação diretamente no agrupador de plataformas e dificultaria a inclusão de novos produtos.
- Recomendação aplicada: a aplicação React foi definida em `apps/frontend/web/exemplo-site-web-001/`; os diretórios `smartphone/` e `desktop/` receberão READMEs de orientação.

### REV-002 — Critérios de aceite estruturais verificáveis

- Severidade: informativa.
- Evidência: a SPEC exige os três agrupadores, a ausência de arquivos de aplicação diretamente em `apps/frontend/` e um README em cada plataforma.
- Impacto: a organização pode ser inspecionada sem depender da execução da interface.
- Recomendação: validar a árvore final e o conteúdo orientativo dos READMEs durante a validação.

## Conclusão

O contrato revisado mantém o escopo do e-commerce demonstrativo, define a estrutura de diretórios sem ampliar responsabilidades de produto e contém critérios funcionais, não funcionais e estruturais verificáveis. Não há pendências bloqueantes para iniciar a implementação.
