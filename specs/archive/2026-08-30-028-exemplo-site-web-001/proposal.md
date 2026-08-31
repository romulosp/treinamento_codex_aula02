# Proposta: 028-exemplo-site-web-001

## Status
`SPEC_APROVADA`

## Responsável e data

Equipe do projeto — 30/08/2026.

## Referências

- Diretrizes do projeto em `AGENTS.md`.
- Prompt de experimento de e-commerce "Terra & Torra".
- Skills de frontend, React, acessibilidade, desempenho, segurança, SEO e revisão de interface.

## Problema e objetivo

O projeto ainda não possui uma aplicação frontend implementada. Esta mudança cria um primeiro e-commerce demonstrativo, "Terra & Torra", para validar o fluxo de trabalho de frontend orientado por skills e estabelecer uma base de interface profissional, acessível e responsiva.

## Escopo

- Inicializar a aplicação frontend com a tecnologia JavaScript/React definida no design aprovado.
- Organizar as aplicações frontend por plataforma em `apps/frontend/web/`, `apps/frontend/smartphone/` e `apps/frontend/desktop/`.
- Manter a aplicação Terra & Torra em `apps/frontend/web/exemplo-site-web-001/`, sem arquivos de aplicação diretamente em `apps/frontend/`.
- Criar orientações de uso nos diretórios de smartphone e desktop.
- Implementar uma vitrine de cafés especiais com cabeçalho, hero, categorias, produtos em destaque, catálogo com filtros, detalhe de produto e carrinho demonstrativo.
- Usar dados fictícios locais, sem integração externa.
- Aplicar requisitos de acessibilidade, responsividade, desempenho básico, SEO técnico básico e segurança no lado cliente.
- Registrar testes, revisão e evidências conforme o processo do projeto.

## Fora de escopo

- Backend, banco de dados, autenticação, conta de cliente, meios de pagamento, pedidos reais, estoque real ou integração de frete.
- Persistência do carrinho entre dispositivos.
- Publicação em produção.

## Impactos e riscos

- A mudança define a primeira base tecnológica do frontend e deve evitar decisões irreversíveis sem justificativa.
- Sem backend, todos os produtos e ações de compra serão demonstrativos; isso deve permanecer visível ao usuário.
- Imagens e fontes podem afetar carregamento e estabilidade visual; a implementação deverá medi-los e otimizá-los.
- A segurança do checkout real não será validada porque pagamento não faz parte do escopo.

## Critérios para aprovação da SPEC

- Escopo de e-commerce demonstrativo e limites de responsabilidade claramente definidos.
- Requisitos funcionais e não funcionais verificáveis.
- Arquitetura frontend e estratégia de validação registradas.
- Nenhum requisito implica alteração de backend ou pagamento real.
