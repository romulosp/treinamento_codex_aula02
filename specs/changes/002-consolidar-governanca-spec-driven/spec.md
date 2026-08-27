# SPEC: 002-consolidar-governanca-spec-driven

## Status
`SPEC_APROVADA`

## Requisitos

1. Deve existir uma única referência canônica para fluxo, estados, gates e evidências.
2. As Skills devem produzir relatórios rastreáveis dentro de `reviews/`.
3. O fluxo deve incluir revisão da SPEC, implementação, revisão da implementação, validação, aprovação, preparação de arquivamento e commit.
4. Os prompts de Aula 01 devem ser preservados, mas não podem competir com as Skills como procedimento operacional.
5. Nenhuma mudança pode ser movida para `archive/` sem aprovação formal e atualização de `system/`.

## Critérios de aceite

- [ ] `AGENTS.md`, `README.md` e as Skills apontam para o fluxo canônico.
- [ ] Existe uma Skill para cada fase operacional, inclusive commit.
- [ ] O processo descreve entradas, saídas, status permitidos e condições de retorno.