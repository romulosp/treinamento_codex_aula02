# Inventário de importação das Skills Go

## Status

`SPEC_APROVADA`

## Origem

`c:\Desenvolvimento\ia\estudo\outras_skill_go_lange_importar.txt`

O arquivo de origem é um documento concatenado com o conteúdo textual das Skills. Cada bloco começa pelo identificador da Skill e pelo marcador `Cópia`. A implementação deverá separar os blocos e criar um `SKILL.md` por destino.

## Regra de importação

Após `SPEC_APROVADA`, copiar o corpo de cada bloco para `.agents/skills/<destino>/SKILL.md`, preservando o texto fornecido. Um frontmatter mínimo com `name` e `description` poderá ser adicionado antes do corpo para permitir a descoberta operacional. Os nomes de destino abaixo normalizam títulos descritivos em português e corrigem identificadores referenciados internamente pelas próprias Skills, como `golang-nameing` para `golang-naming`.

Links para `references/`, `assets/` ou outros arquivos auxiliares serão preservados no texto, mas não serão inventados: esses arquivos não estão presentes no anexo fornecido e deverão ser importados em uma mudança posterior se houver uma origem disponível.

## Inventário aprovado para a revisão

| # | Identificador no anexo | Destino operacional |
| ---: | --- | --- |
| 1 | `golang-benchmark` | `golang-benchmark` |
| 2 | `golang-cli` | `golang-cli` |
| 3 | `estilo de código golang` | `golang-code-style` |
| 4 | `concorrência em golang` | `golang-concurrency` |
| 5 | `contexto golang` | `golang-context` |
| 6 | `integração contínua em golang` | `golang-continuous-integration` |
| 7 | `estruturas-de-dados-golang` | `golang-data-structures` |
| 8 | `banco de dados golang` | `golang-database` |
| 9 | `injeção de dependência em golang` | `golang-dependency-injection` |
| 10 | `gerenciamento-de-dependências-golang` | `golang-dependency-management` |
| 11 | `padrões de design golang` | `golang-design-patterns` |
| 12 | `documentação-golang` | `golang-documentation` |
| 13 | `tratamento de erros em golang` | `golang-error-handling` |
| 14 | `golang-grpc` | `golang-grpc` |
| 15 | `golang-lint` | `golang-linter` |
| 16 | `golang-modernizar` | `golang-modernize` |
| 17 | `golang-nameing` | `golang-naming` |
| 18 | `golang-observabilidade` | `golang-observability` |
| 19 | `desempenho em golang` | `golang-performance` |
| 20 | `bibliotecas populares de golang` | `golang-popular-libraries` |
| 21 | `layout-do-projeto-golang` | `golang-project-layout` |
| 22 | `segurança em golang` — segurança defensiva | `golang-safety` |
| 23 | `golang-samber-do` | `golang-samber-do` |
| 24 | `golang-samber-oops` | `golang-samber-oops` |
| 25 | `segurança em golang` — segurança contra vulnerabilidades | `golang-security` |
| 26 | `golang-stay-updated` | `golang-stay-updated` |
| 27 | `golang-stretchr-testify` | `golang-stretchr-testify` |
| 28 | `golang-structs-interfaces` | `golang-structs-interfaces` |
| 29 | `testes em golang` | `golang-testing` |
| 30 | `solução de problemas em golang` | `golang-troubleshooting` |

## Observação de contagem

O inventário contém 30 blocos de Skill no arquivo anexado. A contagem anterior de 29 estava incorreta e deve ser desconsiderada.

## Critério de integridade

A validação deverá comparar os 30 identificadores de origem com os 30 diretórios de destino, confirmar um `SKILL.md` em cada diretório e registrar qualquer colisão ou bloco ausente antes de marcar a importação como concluída.
