# Revisão de implementação: 030-template-ecommercer-001

## Veredito

`REPROVADA`

## Base da revisão

- `proposal.md` e `spec.md` estão em `SPEC_APROVADA`.
- `tasks.md` registrava a implementação como concluída.
- A aprovação visual da pessoa usuária foi registrada em `validation.md` em 2026-08-31.
- A revisão comparou a SPEC aprovada ao código ativo em `apps/frontend/web/template-ecommercer-001/`. Nenhum código foi alterado nesta fase.

### IMP-REV-001 — JSON-LD de produtos inexistente

- Severidade: alta.
- Evidência: RNF-005 exige JSON-LD básico de produtos fictícios no documento. A busca por `application/ld+json` no aplicativo não encontrou nenhuma ocorrência; `index.html` contém somente título, descrição, fontes e o ponto de montagem React.
- Impacto: o requisito técnico de SEO não é atendido.
- Ação necessária: retornar à implementação e incluir JSON-LD local, coerente com os produtos fictícios e sem alegações de disponibilidade, avaliações ou oferta real.

### IMP-REV-002 — Fechamento por Escape não restaura o foco

- Severidade: alta.
- Evidência: em `Storefront.tsx`, o listener de teclado fecha a FastCart diretamente com `setCartOpen(false)`. A restauração para `cartButtonRef` existe somente em `closeCart`, chamada pelo botão e pela sobreposição.
- Impacto: a exigência de RF-004.6 e CA-006 de devolver o foco ao controle que abriu a cesta não é satisfeita quando a pessoa usa Escape.
- Ação necessária: retornar à implementação e fazer o manipulador de Escape usar o mesmo fechamento com restauração de foco; cobrir esse cenário no teste automatizado.

### IMP-REV-003 — Componentes dedicados previstos não existem na entrega ativa

- Severidade: alta.
- Evidência: RF-001.3 e DESIGN.md exigem unidades dedicadas para `Header`, `HeroSection`, `ProductGrid`, `ProductCard`, `FastCart` e `Footer`. O código ativo concentra `ProductCard`, `FastCart` e `Footer` em `src/components/Storefront/Storefront.tsx`; não há diretórios ou arquivos dedicados para `ProductGrid`, `ProductCard`, `FastCart` ou `Footer`.
- Impacto: a estrutura de componentes aprovada não foi cumprida e a manutenção da vitrine fica concentrada em um arquivo único.
- Ação necessária: retornar à implementação e extrair as responsabilidades previstas, mantendo contratos explícitos e os testes de comportamento.

### IMP-REV-004 — Composição obrigatória da página inicial está incompleta

- Severidade: alta.
- Evidência: RF-006.4 exige ao menos coleções e blog, além de aviso, cabeçalho, hero, faixa, produtos, narrativa, shop-the-look, benefícios, depoimentos, newsletter e rodapé. `HomePage` apresenta os demais blocos, mas não há seção de lista de coleções nem bloco de blog na composição ativa.
- Impacto: CA-012 não pode ser aceito integralmente.
- Ação necessária: retornar à implementação e adicionar composições originais de coleções e blog à página inicial, com navegação local e conteúdo fictício.

### IMP-REV-005 — Recurso remoto legado permanece no código-fonte

- Severidade: média.
- Evidência: `src/components/HeroSection/HeroSection.tsx` ainda declara duas URLs `images.unsplash.com`, enquanto a SPEC vigente limita a dependência remota à folha de estilos do Google Fonts e determina imagens locais originais.
- Impacto: mesmo sem importação atual por `App.tsx`, o arquivo contraria a restrição de origem de ativos e pode ser reintroduzido acidentalmente.
- Ação necessária: retornar à implementação e remover ou atualizar o componente legado para usar somente os ativos locais originais.

## Conclusão

A revisão não encontrou indício de cópia do tema Vintage, segredos, chamadas de catálogo ou HTML cru. Contudo, os achados `IMP-REV-001` a `IMP-REV-004` são bloqueantes. A mudança deve retornar à fase de **implementação**. Não é permitido avançar para validação, aprovação formal, arquivamento ou commit enquanto eles permanecerem abertos.
