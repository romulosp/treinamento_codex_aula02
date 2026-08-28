# Validação: 021-definir-diretorio-projeto-gerado

## Ambiente

- Workspace: `D:\desenvolvimento\ia\aula02`
- Data: 2026-08-28
- Tipo: mudança documental; nenhum código gerado foi alterado.

## Comandos e códigos de saída

| Comando | Resultado | Código |
| --- | --- | --- |
| `rg -n -i 'apps/backend/(pom\\.xml|src/|start_aplicacao\\.bat)|a partir de apps/backend/|em apps/backend/|criar o módulo apps/backend' specs -g '!specs/archive/**' README.md NotasProjeto.md .github AGENTS.md` | Nenhuma referência normativa vigente aponta arquivos ou execução diretamente para `apps/backend/` | `0` |
| Verificação determinística da derivação dos nomes | `gerenciar-categorias` → `apps/backend/gerenciarcategorias/`; `gerenciar-tarefas` → `apps/backend/gerenciartarefas/` | `0` |
| `git diff --check` nos arquivos da mudança | Nenhum erro de whitespace nos arquivos desta mudança; a alteração preexistente da 019 foi excluída | `0` |

## Cenários executados

- `gerenciar-categorias` → `apps/backend/gerenciarcategorias/`.
- `gerenciar-tarefas` → `apps/backend/gerenciartarefas/`.
- Cada projeto mantém seu próprio `pom.xml`, `src/` e script de inicialização.

## Evidências

- `VAL-001` — busca textual confirmou que as referências normativas atuais usam a pasta específica derivada, mantendo ocorrências antigas somente nos archives.
- `VAL-002` — os dois exemplos derivam para pastas distintas e sem hífens.
- `VAL-003` — `git diff --check` dos arquivos da mudança foi aprovado.
- `VAL-004` — nenhum teste Maven foi executado, pois a mudança altera somente documentação e não código gerado.

## Veredito
`VALIDADA`
