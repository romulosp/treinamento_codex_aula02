# Validação: 028-exemplo-site-web-001

## Ambiente

Windows 10, PowerShell 5.1, Node.js 24.16.0 e npm 11.13.0. Validação executada em 30/08/2026 no diretório `apps/frontend/web/exemplo-site-web-001/`.

## Comandos e códigos de saída

| Comando | Resultado | Código de saída |
| --- | --- | --- |
| `npm run test` | 3 testes Vitest aprovados | 0 |
| `npm run lint` | Oxlint sem achados | 0 |
| `npm run build` | TypeScript e Vite concluídos; bundle de entrada 62,71 kB gzip | 0 |
| `npm audit --audit-level=moderate` | nenhuma vulnerabilidade | 0 |
| verificação da árvore `apps/frontend/` | agrupadores e READMEs presentes | 0 |

## Testes unitários e cobertura

- Ferramenta e versão: Vitest 4.1.11 com Testing Library.
- Escopo: filtros, detalhe de produto e carrinho demonstrativo.
- Cobertura de linhas e branches: não configurada nesta aplicação inicial; os três cenários observáveis foram executados.
- Comando executado: `npm run test`.
- Resultado: 1 arquivo e 3 testes aprovados.
- Código de saída: 0.

## Cenários executados

- [x] Filtro de catálogo, estado vazio e limpeza de filtros.
- [x] Detalhe de produto, seleção de moagem e adição ao carrinho.
- [x] Alteração de quantidade, subtotal e remoção de item do carrinho.
- [x] Bloqueio de produto indisponível.
- [x] Ação demonstrativa de finalizar compra.
- [x] Navegação por teclado, foco visível, semântica e preferência por movimento reduzido, revisados no código.
- [x] Responsividade por media queries revisada no código.
- [x] Estrutura de diretórios e READMEs das plataformas frontend.

## Evidências

### VAL-001 — Testes funcionais

`npm run test` concluiu 3 testes aprovados (filtros e vazio; detalhe/moagem; carrinho/checkout), com código 0.

### VAL-002 — Build, lint e segurança de dependências

`npm run lint`, `npm run build` e `npm audit --audit-level=moderate` concluíram com código 0; a auditoria não encontrou vulnerabilidades.

### VAL-003 — Organização aprovada

A verificação da árvore confirmou `apps/frontend/{web,smartphone,desktop}` e seus READMEs, sem aplicação diretamente em `apps/frontend/`.

## Veredito
`VALIDADA`
