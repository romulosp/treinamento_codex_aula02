# Design: 030-template-ecommercer-001

## Contexto

O template será uma aplicação React isolada para demonstrar uma loja de moda feminina premium. A experiência favorece espaços amplos, superfícies orgânicas e CTAs escuros, mantendo a interação do carrinho imediata e segura para uma demonstração exclusivamente local.

## Direção de design

- **Mood:** editorial, suave e premium.
- **Paleta:** fundo gelo/off-white; superfícies rosa pó; texto e CTA em grafite profundo; terracota/vermelho apenas para desconto e urgência.
- **Tipografia:** `DM Serif Display` nos títulos, com peso e escala fluida, e `DM Sans` no texto de apoio e controles.
- **Layout:** assimétrico e fluido, com espaço em branco generoso e blocos arredondados; não depende de um grid visual rígido.
- **Fotografia:** imagens editoriais originais locais, com enquadramento vertical, cor quente e espaço reservado para preservar a composição durante o carregamento.

## Decisões de arquitetura

1. A aplicação ficará em `apps/frontend/web/template-ecommercer-001/`, com dependências próprias, sem migrar ou alterar a aplicação de cafés.
2. Componentes de apresentação ficarão separados por responsabilidade: `Header`, `HeroSection`, `ProductGrid`, `ProductCard`, `FastCart` e `Footer`.
3. O catálogo local terá identificadores estáveis. O carrinho será um mapa indexado por identificador e o subtotal será derivado de seus itens.
4. O estado de interação permanecerá no React. Um `useEffect` de persistência será acionado somente quando o carrinho mudar; a leitura inicial será defensiva e não bloqueará a renderização.
5. O FastCart será um diálogo lateral acessível, com gerenciamento de foco, Escape, sobreposição e redução de movimento quando solicitada pelo sistema operacional.
6. Tailwind será usado para o estilo de componentes. O `.gitignore` receberá exceções estritamente limitadas a este app, permitindo a detecção automática de classes e o rastreamento do código; `node_modules`, `dist` e cobertura continuam excluídos. Regras globais serão limitadas ao carregamento das camadas Tailwind, tokens globais indispensáveis e preferências de redução de movimento.
7. O SEO será técnico e demonstrativo: metadados no documento e JSON-LD local. Não serão feitas afirmações de oferta, preço promocional real ou disponibilidade comercial.
8. A apresentação visual usará os estilos de fonte do Google Fonts com `display=swap` e duas imagens originais locais. Não haverá scripts, SDKs ou chamadas de dados de catálogo externas.

## Skills obrigatórias por momento

| Momento | Skills e objetivo |
| --- | --- |
| Antes da Fase 1 | `frontend-blueprint` para confirmar a direção visual; `frontend-design` para a execução do design; `react-composition-patterns` e `react-best-practices` para a estrutura React. |
| Fases 1 a 3 | `web-design-guidelines`, `accessibility`, `security-best-practices` e `perf-web-optimization` para orientar a implementação incremental. |
| Fase 4 | `web-quality-audit`, `accessibility`, `seo`, `tlc-generative-engine-optimization`, `perf-web-optimization`, `perf-lighthouse`, `react-best-practices`, `security-best-practices` e `security-audit` para a revisão técnica. |

Cada uso deve ser registrado em `validation.md` com o achado ou decisão que influenciou a entrega. A ausência de ferramenta executável para uma skill será registrada como limitação, sem declarar uma auditoria não realizada.

## Estrutura prevista

```text
apps/frontend/web/template-ecommercer-001/
├── src/
│   ├── components/
│   │   ├── Header/
│   │   ├── HeroSection/
│   │   ├── ProductGrid/
│   │   ├── ProductCard/
│   │   ├── FastCart/
│   │   └── Footer/
│   ├── data/products.ts
│   ├── hooks/useCart.ts
│   ├── types/
│   ├── App.tsx
│   └── index.css
├── public/
└── package.json
```

## Estratégia de entrega e gates

1. **Fase 1:** tokens, shell, Header e HeroSection visual corretivo após a reprovação. Mostrar em execução e aguardar aprovação explícita.
2. **Fase 2:** HeroSection, ProductGrid, ProductCard e Footer. Mostrar em execução e aguardar aprovação explícita.
3. **Fase 3:** estado do carrinho, persistência e FastCart. Mostrar em execução e aguardar aprovação explícita.
4. **Fase 4:** executar as verificações técnicas previstas e apresentar os resultados. A validação formal, aprovação, arquivamento e commit dependem de autorização posterior.

## Alternativas e consequências

- Reaproveitar a aplicação de cafés foi descartado: misturaria domínios e comprometeria sua demonstração existente.
- Backend e checkout real foram descartados: exigiriam contratos, segurança e tratamento de dados que não pertencem ao escopo.
- A interpretação de O(1) como tempo total de atualização de tela foi descartada: React precisa renderizar as linhas visíveis. A garantia ficará restrita ao acesso lógico por identificador e à ausência de espera por rede.
- Foto externa sem origem ou licença definida foi descartada. Serão usadas somente imagens estáticas do Unsplash, cuja licença permite o uso gratuito; elas continuam substituíveis por ativos de marca em mudança futura.

## Reinterpretação Vintage original

### Direção aprovada

**Mood:** boutique vintage contemporânea, tátil e luminosa. A loja fictícia se chamará **Orla**, com linguagem portuguesa, tons de areia, vinho, azul-petróleo e verde-oliva. A inspiração se limita a princípios amplos de e-commerce editorial: fotos grandes, ritmo vertical, camadas tipográficas, navegação de descoberta e módulos de conteúdo.

Não serão copiados grid, espaçamentos, textos, imagens, marca, CSS, Liquid, JavaScript nem a organização de markup do tema Vintage.

### Arquitetura de cobertura

```text
src/
├── app/                 # roteamento demonstrativo e estado do carrinho
├── data/
│   ├── templates.ts     # 30 telas e metadados de rota
│   ├── modules.ts       # 71 módulos do inventário, agrupados por finalidade
│   └── products.ts      # catálogo Orla, inteiramente fictício
├── components/
│   ├── chrome/          # aviso, cabeçalho, rodapé e navegação móvel
│   ├── commerce/        # cards, galeria, filtros, carrinho e produto
│   ├── editorial/       # hero, narrativa, look, depoimentos e mídia
│   ├── content/         # blog, FAQ, formulário, newsletter e páginas
│   └── system/          # tabs, diálogo, aviso e estado vazio
└── pages/               # composição das rotas por tipo de template
```

O registro de módulos mantém a relação de cobertura com as 71 sections da referência, mas usa componentes próprios por grupos. Isso evita 71 cópias de markup e permite que uma mesma unidade acessível seja reutilizada por páginas distintas.

### Inventário de referência e mapeamento

| Grupo React original | Sections de referência cobertas |
| --- | --- |
| Chrome e sobreposições | age verification, announcement, apps, header, footer, toolbar, mobile dock, floating bar, overlay newsletter, predictive search, pickup availability e spacers. |
| Carrinho e compra | cart drawer, recomendações, ícone, live region, notificações, itens, rodapé, bundle, benefícios, recomendações e quick view. |
| Descoberta comercial | hero, slideshow, image banner, collection list, featured collection, tab collections, collection banner, product grid, featured product, image with products e shop the look. |
| Editorial e mídia | collage, image with text, image comparison, media cards, multicolumn, multicolumn icons, scrolling text, text highlight, text with images, vertical tabs, live reel, video, video banner e video reels. |
| Conteúdo e relacionamento | blog, artigo, press, testimonials, FAQ, rich text, contact form, email signup, newsletter, page e custom content. |
| Templates principais | 404, collection, list collections, product, pre-order, quick view, search, cart, index, article, blog, password e 10 páginas institucionais/cliente. |

O arquivo `data/modules.ts` será a evidência de 71 entradas. O arquivo `data/templates.ts` será a evidência de 30 entradas de tela.
