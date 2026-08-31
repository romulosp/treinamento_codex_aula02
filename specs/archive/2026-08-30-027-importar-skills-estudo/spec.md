# SPEC: 027-importar-skills-estudo

## Status

`SPEC_APROVADA`

## Referências e dependências

- `proposal.md`
- `DESIGN.md`
- `tasks.md`
- `specs/shared/process/workflow.md`
- Origem: `D:\desenvolvimento\ia\estudo\skills`
- Destino: `.agents/skills/`

## Requisitos funcionais

### RF-001 — Descoberta de origem

1. A implementação deve considerar uma skill de origem quando houver um arquivo `SKILL.md` no diretório raiz dela.
2. A origem inventariada contém 42 skills distribuídas em nove categorias: `arquitetura` (14), `criacao` (2), `decisao` (2), `desenvolvimento` (1), `design` (2), `ferramentas` (8), `performace` (3), `qualidade` (7) e `seguranca` (3).
3. A origem não contém `proposal.md` ou `spec.md`; `SKILL.md` é o artefato de especificação operacional de cada skill.

#### Inventário aprovado para importação

| Categoria | Skills |
| --- | --- |
| `arquitetura` | `component-common-domain-detection`, `component-flattening-analysis`, `component-identification-sizing`, `coupling-analysis`, `decomposition-planning-roadmap`, `domain-analysis`, `domain-identification-grouping`, `evolutionary-modular-architecture`, `frontend-blueprint`, `legacy-migration-planner`, `modular-decomposition`, `modular-design-principles`, `react-composition-patterns`, `tactical-ddd` |
| `criacao` | `skill-architect`, `subagent-creator` |
| `decisao` | `the-fool`, `the-jury` |
| `desenvolvimento` | `react-native-expert` |
| `design` | `frontend-design`, `web-design-guidelines` |
| `ferramentas` | `chrome-devtools`, `excalidraw-studio`, `gh-fix-ci`, `mermaid-studio`, `nx-ci-monitor`, `nx-generate`, `nx-run-tasks`, `nx-workspace` |
| `performace` | `perf-astro`, `perf-lighthouse`, `perf-web-optimization` |
| `qualidade` | `pr-review`, `react-best-practices`, `seo`, `tlc-generative-engine-optimization`, `web-accessibility`, `web-best-practices`, `web-quality-audit` |
| `seguranca` | `security-best-practices`, `security-ownership-map`, `security-threat-model` |

### RF-002 — Importação íntegra

1. Cada uma das 42 skills deve ser copiada para `.agents/skills/<nome-da-skill>/`, sem o diretório categorizador da origem.
2. A cópia deve preservar recursivamente todos os arquivos e diretórios da skill, sem transformar, renomear ou omitir conteúdo.
3. Arquivos de licença presentes na origem devem ser preservados junto à skill correspondente.
4. Scripts, templates e assets são material importado; não devem ser executados como parte da cópia.

### RF-003 — Proteção do acervo atual

1. As 11 skills preexistentes em `.agents/skills/` não podem ser alteradas ou removidas.
2. Antes de cada cópia, a implementação deve verificar colisão no destino. Uma colisão não prevista deve interromper a operação e ser registrada na validação.
3. Ao fim, o destino deve conter as 11 skills preexistentes e as 42 importadas, totalizando 53 diretórios de skills com `SKILL.md`.

### RF-004 — Documentação

1. `.agents/skills/README.md` deve manter a seção das skills de processo e técnicas existentes e incluir uma seção para as skills importadas.
2. O catálogo das importadas deve agrupá-las pelas nove categorias de origem, citar cada nome e descrever sua finalidade em português do Brasil.
3. O `README.md` raiz deve apontar para `.agents/skills/README.md` como catálogo completo e informar que o acervo inclui skills de processo, backend e estudo reutilizável.

## Requisitos não funcionais

- **Integridade:** a quantidade e os caminhos relativos dos arquivos de cada skill importada devem coincidir com a origem.
- **Rastreabilidade:** `validation.md` deve registrar origem, destino, data, comando, código de saída e resultado da comparação.
- **Segurança:** nenhum script, conexão externa ou instalação de dependência será acionado durante a importação.
- **Compatibilidade:** a estrutura final deve manter `SKILL.md` como ponto de entrada em cada diretório de skill.

## Regras de negócio

- A importação disponibiliza instruções locais, mas não torna obrigatório o uso de nenhuma skill nem altera as regras de `AGENTS.md`.
- O nome de diretório no destino é o nome de diretório da skill na origem.
- A categoria de origem é informação de catálogo, não parte do caminho de instalação.

## Cenários e critérios de aceite

- [ ] **CA-001:** a origem é inventariada com 42 diretórios contendo `SKILL.md`, nas quantidades definidas em RF-001.
- [ ] **CA-002:** cada nome de skill inventariado existe em `.agents/skills/` e contém seu `SKILL.md` correspondente.
- [ ] **CA-003:** a comparação recursiva confirma que cada skill importada possui a mesma lista de caminhos relativos e a mesma quantidade de arquivos da origem.
- [ ] **CA-004:** as quatro licenças da origem permanecem presentes nas skills importadas.
- [ ] **CA-005:** as 11 skills já existentes continuam presentes e inalteradas.
- [ ] **CA-006:** `.agents/skills/README.md` cataloga as 42 skills por categoria, e o `README.md` raiz aponta para esse catálogo.
- [ ] **CA-007:** a validação não registra execução de scripts das skills, instalação de dependências ou alteração fora dos caminhos autorizados.
