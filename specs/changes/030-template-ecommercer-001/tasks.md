# Tarefas: 030-template-ecommercer-001

## Pré-condições

- [x] Obter `SPEC_APROVADA` para `proposal.md` e `spec.md`.
- [x] Obter autorização para implementar e avaliar a direção de design registrada em `DESIGN.md`.

## Fase 1 — Design system e shell

- [x] Corrigir a descoberta de classes do Tailwind afetada pelo `.gitignore` global.
- [x] Criar exceções mínimas de versionamento para o código do template, sem incluir dependências nem artefatos gerados.
- [x] Importar as duas famílias aprovadas do Google Fonts com estratégia de carregamento adequada.
- [x] Implementar HeroSection com imagens editoriais estáticas, responsivas e acessíveis.
- [x] Refinar Header, tokens e shell para a nova direção visual.
- [x] Executar as verificações técnicas aplicáveis à reapresentação da Fase 1.
- [x] Registrar a apresentação e aprovação visual/comportamental da Fase 1 em `validation.md`.

## Fase 2 — Hero e vitrine

- [x] Implementar HeroSection, ProductGrid, ProductCard e Footer com dados locais fictícios.
- [x] Aplicar imagens ou placeholders acessíveis, responsividade e carregamento preguiçoso fora da dobra.
- [x] Executar as verificações técnicas aplicáveis à Fase 2.
- [x] Registrar a apresentação e aprovação visual/comportamental da Fase 2 em `validation.md`.

## Replanejamento Vintage — autorizado

- [x] Criar o registro de 30 telas e 71 módulos com cobertura rastreável do inventário de referência.
- [x] Substituir a marca e a composição atual pela direção original Orla.
- [x] Criar os componentes compartilhados de chrome, comércio, editorial, conteúdo e sistema.
- [x] Implementar páginas demonstrativas de início, coleção, produto, carrinho, busca, conteúdo, erro, senha, gift card e cliente.
- [x] Criar mídia original própria; não importar Liquid, CSS, JavaScript, imagens ou texto do tema Vintage.
- [x] Criar testes de contagem de inventário, navegação, carrinho local e mensagens demonstrativas.
- [x] Apresentar o novo layout integrado para aprovação visual antes de revisão formal.

## Fase 3 — FastCart

- [x] Implementar o estado indexado, subtotal e persistência defensiva em `localStorage`.
- [x] Implementar o slide-over acessível, feedback de adição, teclado, Escape e restauração de foco.
- [x] Criar ou atualizar testes de comportamento observável do carrinho.
- [x] Executar as verificações técnicas aplicáveis à Fase 3.
- [x] Registrar a apresentação e aprovação visual/comportamental da Fase 3 em `validation.md`.

## Fase 4 — Revisão técnica

- [ ] Executar testes, lint, build e auditorias aplicáveis de acessibilidade, SEO, desempenho e segurança.
- [ ] Corrigir somente achados dentro do escopo aprovado.
- [ ] Registrar ambiente, comandos, resultados, códigos de saída e evidências em `validation.md`.
- [ ] Aguardar aprovação visual final antes de iniciar as fases formais posteriores.

## Correção da revisão de implementação

- [x] Adicionar JSON-LD local de produtos fictícios, sem oferta comercial.
- [x] Corrigir Escape, foco inicial, retenção de Tab e restauração de foco da FastCart.
- [x] Extrair `Header`, `HeroSection`, `ProductGrid`, `ProductCard`, `FastCart` e `Footer` ativos para componentes dedicados.
- [x] Compor coleções e Caderno Orla na página inicial.
- [x] Remover referências remotas legadas do HeroSection.
- [x] Ampliar testes de comportamento e executar teste, lint e build.

## Ajuste da biblioteca de módulos

- [x] Substituir o inventário passivo de nomes por um ateliê interativo com seleção explícita e prévia visual de cada um dos 71 módulos.
- [x] Organizar as prévias em cinco famílias de composição reutilizáveis, mantendo título, propósito e identificador próprios para cada módulo.
- [x] Corrigir rótulos dos controles de quantidade, retorno de foco da FastCart ao acionador e feedback local dos formulários demonstrativos.
- [x] Cobrir a troca de módulo no teste de interface e executar teste, lint e build.

## Estado da implementação

`IMPLEMENTADA — biblioteca de módulos interativa entregue; aguardando nova revisão de implementação`
