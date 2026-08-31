# Proposta: 030-template-ecommercer-001

## Status

`SPEC_APROVADA`

## Responsável e data

- Responsável: equipe do projeto
- Data: 2026-08-31

## Referências

- `AGENTS.md`
- `specs/shared/process/workflow.md`
- Prompt anexado `template-ecommercer-001 (Production-Grade V1)`
- Aplicação de referência existente: `apps/frontend/web/exemplo-site-web-001/`

## Problema e objetivo

O projeto possui uma vitrine demonstrativa de cafés, mas não uma base isolada para uma loja de moda feminina premium. Esta mudança cria o template web demonstrativo `template-ecommercer-001`, com uma navegação editorial, vitrine de produtos fictícios e um FastCart responsivo, para exercitar uma experiência de compra de alta percepção de velocidade sem serviços externos ou processamento de pagamento.

## Escopo

- Criar uma aplicação React, TypeScript, Vite e Tailwind CSS em `apps/frontend/web/template-ecommercer-001/`.
- Criar exceções mínimas no `.gitignore` para que o código-fonte dessa aplicação seja versionável e detectável pelo Tailwind, mantendo artefatos gerados ignorados.
- Entregar em fases revisáveis: shell e design system; hero e vitrine; FastCart; auditoria e ajustes.
- Criar componentes dedicados para `Header`, `HeroSection`, `ProductGrid`, `ProductCard`, `FastCart` e `Footer`.
- Exibir dados locais e fictícios de produtos de moda feminina.
- Usar as fontes `DM Serif Display` e `DM Sans` pelo Google Fonts e imagens editoriais originais geradas para a demonstração visual.
- Persistir o carrinho somente em `localStorage`, de forma tolerante a dados inválidos.
- Incluir requisitos proporcionais de responsividade, acessibilidade, desempenho, SEO técnico, segurança no cliente e testes de comportamento observável.

## Fora de escopo

- Alterar `apps/frontend/web/exemplo-site-web-001/` ou qualquer aplicação existente.
- Backend, autenticação, catálogo remoto, estoque, pagamento, frete, pedidos reais, coleta de dados pessoais ou integração de terceiros.
- Identidade visual definitiva, logotipo de marca ou publicação em produção.
- Garantir uma pontuação específica de Lighthouse em ambiente externo; a meta será medida localmente e registrada conforme o ambiente disponível.

## Impactos e riscos

- O novo template introduz Tailwind CSS como dependência isolada da nova aplicação, sem converter o frontend existente.
- A percepção de instantaneidade do FastCart depende de catálogo local pequeno; carrinhos muito extensos exigiriam virtualização ou outra estratégia em uma mudança futura.
- Fontes e imagens remotas dependem da disponibilidade de seus provedores. A aplicação deve manter fallbacks locais de fonte, atributos alternativos e layout estável caso uma imagem não carregue.
- Dados em `localStorage` pertencem ao navegador e podem ser alterados pelo usuário; nenhum valor persistido será tratado como dado confiável de compra.

## Critérios para aprovação da SPEC

- Escopo demonstração e exclusões de backend/pagamento estão explícitos.
- Cada fase possui entrega, critério de aceite e gate de aprovação humana.
- O contrato explica a persistência local e a definição técnica verificável do FastCart.
- Requisitos de acessibilidade, SEO, desempenho e segurança são verificáveis e não pressupõem serviços externos.

## Adendo — referência Vintage V8.0

### Decisão aprovada em 2026-08-31

A pessoa usuária autorizou usar o pacote local `Theme File - Vintage` somente como referência de cobertura funcional e atmosfera visual. O resultado será uma loja fictícia nova, com marca, textos, dados, componentes, CSS e imagens próprios.

### Escopo adicional

- Cobrir em React as 30 telas de `templates/` como rotas demonstrativas locais.
- Cobrir as 71 sections de `sections/` em um catálogo de módulos React reutilizáveis, usando variações de composição e dados locais.
- Criar imagens editoriais próprias para a aplicação porque o diretório de referência `assets/` contém somente CSS e JavaScript, sem mídia fotográfica.
- Preservar os limites existentes: nenhum backend, login real, pedido real, pagamento, integração Shopify ou chamada a catálogo remoto.

### Propriedade intelectual

- Não copiar arquivos Liquid, CSS, JavaScript, textos, marca, configurações, imagens `shopify://` ou estrutura de markup do tema de referência.
- Nomes de arquivos do inventário podem ser usados exclusivamente para rastrear a cobertura de módulos; a implementação usa nomes, dados e apresentação originais em português do Brasil.
