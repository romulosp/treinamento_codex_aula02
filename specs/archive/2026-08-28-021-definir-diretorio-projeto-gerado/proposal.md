# Proposta: 021-definir-diretorio-projeto-gerado

## Status
`IMPLEMENTADA`

## Responsável e data

Equipe do laboratório — 2026-08-28

## Referências

- `specs/archive/2026-08-27-001-criar-projeto-java/`
- `specs/archive/2026-08-26-004-parametrizar-nome-projeto-gerado/`
- `specs/archive/2026-08-28-020-corrigir-pacote-java-artifactid/`
- `specs/shared/process/workflow.md`
- `AGENTS.md`

## Problema e objetivo

As especificações indicam `apps/backend/` como o diretório do projeto gerado. Isso impede manter mais de uma aplicação Java independente sob `apps/backend`, pois os arquivos de uma geração podem ser misturados ou sobrescritos pelos arquivos de outra. Definir uma pasta própria para cada projeto, derivada do `artifactId` com os hífens removidos.

## Escopo

- Definir a regra de diretório `apps/backend/<artifactId-sem-hifens>/` para cada projeto Java gerado.
- Aplicar a regra aos exemplos `gerenciar-categorias` e `gerenciar-tarefas`.
- Atualizar instruções de geração, execução, teste, limpeza e estado vigente que ainda tratem `apps/backend/` como o diretório do projeto.
- Manter o `artifactId` Maven com hífens e o pacote Java sem hífens.

## Fora de escopo

- Alterar contratos HTTP, dependências, versão Java, banco de dados ou comportamento das APIs.
- Gerar ou versionar código de qualquer aplicação.
- Renomear os diretórios dos archives históricos.

## Impactos e riscos

- Com a regra, `gerenciar-categorias` será gerado em `apps/backend/gerenciarcategorias/` e `gerenciar-tarefas` em `apps/backend/gerenciartarefas/`.
- Scripts e comandos que assumiam `apps/backend/` precisarão receber o caminho do projeto específico.
- O diretório-raiz `apps/backend/` continua sendo apenas um contêiner local ignorado; não é um módulo Maven.

## Critérios para aprovação da SPEC

- A regra de derivação do diretório está explícita e não conflita com a regra de pacote Java.
- Os cenários com os dois projetos independentes são verificáveis.
- A documentação de geração, execução e limpeza não deixa `apps/backend/` como diretório do projeto.
