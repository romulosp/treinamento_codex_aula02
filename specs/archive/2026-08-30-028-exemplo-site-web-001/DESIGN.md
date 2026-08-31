# Design: 028-exemplo-site-web-001

## Contexto

Terra & Torra será uma loja demonstrativa de cafés especiais e o primeiro frontend do projeto. O objetivo é testar uma implementação React com interface distinta, não genérica, e com qualidade técnica verificável.

## Referências

- Skill `frontend-blueprint` para planejamento e recorte da interface.
- Skill `frontend-design` para direção editorial, artesanal e contemporânea.
- Skills de React, acessibilidade, desempenho, segurança, SEO e revisão para implementação e validação.

## Decisões

- A primeira versão será uma aplicação web isolada em `apps/frontend/web/exemplo-site-web-001`, construída com React 19, TypeScript e Vite, com dados fictícios locais.
- `apps/frontend/` será um agrupador de plataformas: `web/`, `smartphone/` e `desktop/`. Cada produto terá uma pasta própria dentro da plataforma correspondente; os diretórios ainda sem implementação terão README de orientação.
- O estado do carrinho será mantido somente no cliente e não representará uma compra real.
- A direção visual combina fundo quente, paleta espresso/creme/terracota/verde, tipografia editorial nos títulos e composição assimétrica.
- Componentes serão semânticos e responsivos; efeitos visuais não podem comprometer contraste, navegação por teclado ou preferência por redução de movimento.

## Arquitetura e componentes

```text
apps/frontend/
├── README.md
├── web/
│   ├── README.md
│   └── exemplo-site-web-001/
│       ├── páginas: início, catálogo e detalhe de produto
│       ├── componentes de loja: cabeçalho, filtros, card de produto, seletor de variação, carrinho e rodapé
│       ├── dados locais: catálogo de cafés e categorias
│       ├── estado local: filtros e carrinho demonstrativo
│       └── estilos e tokens: cores, tipografia, espaçamento, foco e responsividade
├── smartphone/
│   └── README.md
└── desktop/
    └── README.md
```

O cabeçalho abre o carrinho. O catálogo consome o conjunto local de produtos e aplica filtros no cliente. O detalhe valida disponibilidade e opções antes de inserir item no estado do carrinho. O carrinho calcula subtotal derivado de seus itens.

## Alternativas e consequências

- Integrar backend agora foi descartado: amplia escopo e inviabiliza o objetivo de validar o frontend isoladamente.
- Implementar checkout real foi descartado: exigiria requisitos de segurança, dados e conformidade que não fazem parte deste experimento.
- Usar uma biblioteca visual completa só será considerado se a SPEC aprovada justificar; inicialmente, componentes próprios mantêm o teste focado na qualidade das skills.
- Next.js foi descartado nesta change: renderização no servidor não é necessária para a demonstração local e aumentaria a complexidade inicial. Caso SEO indexável em produção seja requerido, será avaliado em uma mudança futura.
- Não haverá uma aplicação genérica diretamente em `apps/frontend/`: a separação por plataforma evita colisões de configuração, dependências e ciclos de entrega entre produtos distintos.
