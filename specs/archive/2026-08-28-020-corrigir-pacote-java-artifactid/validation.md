# Validação: 020-corrigir-pacote-java-artifactid

## Status
`VALIDADA`

## Ambiente

- Revisão documental local no workspace em 2026-08-28.

## Cenários executados

| Cenário | Comando/evidência | Resultado |
| --- | --- | --- |
| Exemplo do pacote | Busca e leitura de `spec.md`, `DESIGN.md`, `AGENTS.md` e `NotasProjeto.md`. | `gerenciar-tarefas` está documentado como `br.com.romulopenha.gerenciartarefas`. |
| Remoção da regra antiga | `rg -n "nomedaapigerada"` limitado a documentação histórica de revisão/validação. | Nenhuma ocorrência normativa nova permanece. |
| Integridade dos arquivos alterados | `git diff --check -- AGENTS.md NotasProjeto.md STATUS.md specs/archive/2026-08-27-001-criar-projeto-java specs/archive/2026-08-28-019-gerenciar-tarefas/DESIGN.md`. | Código de saída `0`; sem erros de whitespace nos arquivos da mudança. |

## Veredito

`VALIDADA`
