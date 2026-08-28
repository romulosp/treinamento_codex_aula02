# Validação: 012-estabelecer-diretrizes-testes-unitarios

## Ambiente

- Windows.
- PowerShell 5.1.
- Repositório sem módulo Maven/Quarkus executável e sem arquivos `.java` no momento da validação.

## Comandos e códigos de saída

1. Verificação estática da existência de arquivos Java, de nove conteúdos obrigatórios da política e de whitespace do diff — código `0`.

## Testes unitários e cobertura

- Ferramenta e versão: não aplicável; o módulo Maven executável ainda não existe.
- Escopo de classes aplicáveis: nenhuma classe Java encontrada.
- Classes excluídas e justificativas: nenhuma; não há arquivo Java no inventário atual.
- Cobertura de linhas: não aferível, sem código de produção Java e sem ferramenta de cobertura configurada.
- Cobertura de branches: não aferível, sem código de produção Java e sem ferramenta de cobertura configurada.
- Comando executado: inventário de `*.java` e `*Test.java`, conferência dos nove conteúdos obrigatórios com `Select-String` e `git diff --check`.
- Resultado: aprovado; `0` arquivos Java, `0` testes Java e `9` verificações documentais aprovadas.
- Código de saída: `0`.
- Indisponibilidade de aferição ou observações: a política veda declarar percentual sem medição reproduzível. A ferramenta, o baseline e o comando de cobertura serão definidos pela mudança que introduzir o módulo Maven executável.

## Cenários executados

- A estratégia contém diretrizes de Mockito, Panache sem container, consultas, varargs, reflexão, enums, SonarQube, estabilidade, exceções, branches, inventário e cobertura.
- A Skill `java-unit-test` foi encontrada com nome e descrição esperados.
- `AGENTS.md`, o índice de Skills e o modelo de validação referenciam a política unitária.
- Não existem classes Java para testes unitários nem cobertura nesta mudança, conforme o escopo aprovado.

## Evidências

- `VAL-001` — Os nove conteúdos documentais obrigatórios foram encontrados.
- `VAL-002` — O inventário atual contém zero arquivos `.java` e zero classes de teste Java.
- `VAL-003` — Nenhum percentual de cobertura foi declarado sem aferição reproduzível.
- `VAL-004` — `git diff --check` foi aprovado sem erro de whitespace.

## Veredito
`VALIDADA`
