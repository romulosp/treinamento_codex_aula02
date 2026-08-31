# Tarefas: 027-importar-skills-estudo

## Pré-condições

- [x] `proposal.md` e `spec.md` aprovadas com status `SPEC_APROVADA`.
- [x] Confirmar que o inventário da origem ainda contém 42 skills e não há colisões com `.agents/skills/`.

## Implementação

- [x] Copiar recursivamente as 42 skills de origem para `.agents/skills/`, removendo somente o nível categorizador do caminho.
- [x] Preservar referências, scripts, assets, templates, regras, arquivos auxiliares e licenças.
- [x] Atualizar `.agents/skills/README.md` com o catálogo por categoria, finalidade e exemplos de uso por grupo.
- [x] Atualizar `README.md` raiz com referência ao catálogo completo.

## Revisão e validação

- [x] Comparar a lista de skills e os caminhos relativos dos arquivos entre origem e destino.
- [x] Confirmar a integridade das quatro licenças e a preservação das 11 skills preexistentes.
- [x] Verificar links e informações nos dois READMEs.
- [x] Registrar ambiente, comandos, resultados e códigos de saída em `validation.md`.
- [x] Revisar contra a SPEC.
