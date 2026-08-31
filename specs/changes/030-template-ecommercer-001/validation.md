# Validação: 030-template-ecommercer-001

## Status

`EM_ANDAMENTO — evidências técnicas da Fase 1 registradas; validação formal não iniciada`

## Aprovações visuais e comportamentais

| Fase | Entrega apresentada | Decisão da pessoa usuária | Data |
| --- | --- | --- | --- |
| 1 — Shell | Primeira apresentação reprovada; nova apresentação com hero, fontes e imagens pronta | Pendente | 2026-08-31 |
| 2 — Vitrine | Pendente | Pendente | — |
| 3 — FastCart | Pendente | Pendente | — |
| Final | Pendente | Pendente | — |

## Ambiente, comandos e evidências

Ambiente da Fase 1: Windows, PowerShell, Node.js 24.16.0, npm 11.13.0, React 19.2.8, Vite 8.2.2 e Tailwind CSS 4.3.3.

| Comando | Resultado | Código de saída |
| --- | --- | --- |
| `npm install` | 131 pacotes instalados; auditoria de dependências sem vulnerabilidades | 0 |
| `npm run test` | Vitest: 1 teste aprovado para atalho de conteúdo, navegação e botão de carrinho acessível | 0 |
| `npm run lint` | Oxlint sem achados nos arquivos TypeScript/TSX da Fase 1 | 0 |
| `npm run build` | TypeScript e Vite concluídos; JavaScript de entrada com 61,27 kB gzip | 0 |

## Skills aplicadas na Fase 1

- `frontend-blueprint`: confirmou a entrega atômica e o gate visual antes da Fase 2.
- `frontend-design`: direcionou a composição editorial, paleta cálida, tipografia e ritmo assimétrico do shell.
- `react-composition-patterns`: manteve o `Header` como componente de responsabilidade única, com contrato explícito de contagem, sem props booleanas de aparência.
- `react-best-practices`: evitou dependências de execução, dados remotos e trabalho de renderização desnecessário na primeira dobra.

## Evidências da reapresentação visual

| Comando ou verificação | Resultado | Código de saída |
| --- | --- | --- |
| `npm run test` | Vitest: 1 teste aprovado, incluindo título do hero e prioridade da imagem principal | 0 |
| `npm run lint` | Oxlint sem achados em `App`, `Header` e `HeroSection` | 0 |
| `npm run build` | TypeScript e Vite concluídos; CSS de 17,41 kB (4,39 kB gzip) e JavaScript de 61,90 kB gzip | 0 |
| inspeção do CSS de build | `bg-blush`, `font-display`, `text-terracotta` e `shadow-whisper` presentes | 0 |
| `HEAD` das duas imagens Unsplash | duas respostas `200 image/jpeg` | 0 |
| `git check-ignore` | código do template versionável; `node_modules` permanece ignorado | 0 |

### Correção registrada

O `.gitignore` global ignorava todos os arquivos de código. Isso impedia tanto o versionamento quanto a descoberta automática de classes do Tailwind 4, deixando a página sem as utilitárias. Foram criadas exceções exclusivas para `apps/frontend/web/template-ecommercer-001/`, mantendo `node_modules`, `dist` e `coverage` ignorados.

As fontes `Instrument Sans` e `DM Sans` são carregadas pelo CSS do Google Fonts com `display=swap` e preconexão. As imagens estáticas do hero vêm do Unsplash, possuem alt text, proporção reservada e retornaram HTTP 200 na verificação.

## Limites desta evidência

Não foi realizada validação formal, revisão de implementação, auditoria final, aprovação, arquivamento ou commit.

## Evidências de implementação — reinterpretação Orla

Em 2026-08-31, foi implementada a reinterpretação original autorizada da referência Vintage. Esta seção registra somente a execução da implementação; não constitui revisão nem validação formal.

| Comando | Resultado | Código de saída |
| --- | --- | --- |
| `npm run test` | Vitest: 3 testes aprovados (inventário de 30 telas e 71 módulos, navegação, FastCart com Escape e recuperação de `localStorage` inválido) | 0 |
| `npm run lint` | Oxlint sem achados em arquivos TypeScript/TSX da aplicação | 0 |
| `npm run build` | TypeScript e Vite concluídos; CSS 29,69 kB (6,26 kB gzip) e JavaScript 219,92 kB (67,63 kB gzip) | 0 |

### Decisões e origem dos ativos

- A marca, todos os textos, produtos e componentes foram substituídos pela proposta original **Orla**; não há arquivos Liquid, CSS ou JavaScript do tema de referência na aplicação.
- `data/storefront.ts` registra 30 telas e 71 módulos; o teste confirma as contagens e a ausência de referências `shopify://`.
- As imagens locais `public/media/orla-hero-v1.png` e `public/media/orla-still-life-v1.png` foram geradas exclusivamente para este projeto. A Skill `imagegen` orientou sua geração e o uso local com texto alternativo.
- A tipografia foi atualizada para `DM Serif Display` e `DM Sans`, carregadas com `display=swap`, preconexão e fallbacks.
- A FastCart atualiza o estado em memória, persiste somente quantidades válidas em `localStorage`, abre após adicionar uma peça e fecha por sobreposição, botão ou tecla Escape. A conclusão continua explicitamente demonstrativa.

### Registro de aprovação visual

A pessoa usuária aprovou visualmente e comportamentalmente a composição integrada em 2026-08-31 (“tudo certo ficou top”). Esta aprovação atende aos gates visuais das Fases 1 a 3 e autoriza a revisão formal da implementação; não substitui a revisão, validação técnica, aprovação formal ou commit.

| Fase | Decisão da pessoa usuária | Data |
| --- | --- | --- |
| 1 — Shell | Aprovada | 2026-08-31 |
| 2 — Vitrine | Aprovada | 2026-08-31 |
| 3 — FastCart | Aprovada | 2026-08-31 |
| Final visual | Aprovada | 2026-08-31 |

## Correção dos achados de implementação

Esta seção registra o retorno à implementação após `IMP-REV-001` a `IMP-REV-005`; não é uma nova revisão ou validação formal.

- `seo`: orientou o JSON-LD estático no documento com `ItemList` e produtos fictícios, sem preço, disponibilidade ou avaliação comercial.
- `accessibility`: orientou foco inicial, retenção de Tab, Escape e retorno do foco ao botão de abertura no diálogo FastCart.
- `react-composition-patterns`: orientou a extração das seis responsabilidades aprovadas para componentes ativos e com contratos explícitos.
- `react-best-practices`: preservou dados locais, inicialização preguiçosa de `localStorage` e busca indexada por identificador.
- `security-best-practices`: confirmou o uso exclusivo de dados locais, sem HTML cru, segredos, chamadas de catálogo ou ativos remotos além da folha de fontes autorizada.

| Comando | Resultado | Código de saída |
| --- | --- | --- |
| `npm run test` | Vitest: 3 testes aprovados, incluindo inventário, FastCart com Escape e estado inválido no armazenamento local | 0 |
| `npm run lint` | Oxlint recursivo em `src`, sem achados | 0 |
| `npm run build` | TypeScript e Vite concluídos; CSS 25,55 kB (5,69 kB gzip) e JavaScript 222,21 kB (68,28 kB gzip) | 0 |
| busca estática de escopo | JSON-LD presente; nenhuma URL Unsplash, `shopify://`, chamada de catálogo, segredo ou HTML cru no código de produção | 0 |

## Ajuste da biblioteca de módulos

Em 2026-08-31, após o retorno de que a página de módulos apenas listava nomes, o inventário foi convertido em um ateliê interativo. Cada uma das 71 entradas agora é um botão de seleção e atualiza título, propósito, identificador e uma prévia visual funcional da família correspondente. Esta é uma evidência de implementação e checagem técnica; não constitui nova revisão, validação formal, aprovação ou commit.

- A implementação usa cinco famílias reutilizáveis (`chrome`, `cart`, `discovery`, `editorial` e `content`) com variações orientadas pelo identificador. Isso evita copiar 71 marcações quase idênticas sem reduzir a cobertura do inventário.
- A FastCart preserva o acionador que abriu o diálogo e devolve o foco a ele ao fechar; o formulário demonstrativo informa que nenhum e-mail foi enviado ou armazenado; e os controles de quantidade possuem nomes acessíveis.
- Foram usados retornos independentes de teste, acessibilidade e cobertura do inventário para guiar os ajustes. Os três confirmaram a contagem de 71 módulos e a seleção da composição ativa.

| Comando | Resultado | Código de saída |
| --- | --- | --- |
| `npm run test` | Vitest: 4 testes aprovados, incluindo seleção de `cart-drawer`, FastCart com Escape e recuperação de `localStorage` inválido | 0 |
| `npm run lint` | Oxlint sem achados em `src` | 0 |
| `npm run build` | TypeScript e Vite concluídos; CSS 28,13 kB (6,04 kB gzip) e JavaScript 235,56 kB (70,72 kB gzip) | 0 |
