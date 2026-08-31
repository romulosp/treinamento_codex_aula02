# SPEC: 028-exemplo-site-web-001

## Status
`SPEC_APROVADA`

## Referências e dependências

- `proposal.md` desta mudança.
- `AGENTS.md` e `specs/shared/process/workflow.md`.
- Aplicação em `apps/frontend/web/exemplo-site-web-001`.
- React 19, TypeScript e Vite como base da aplicação demonstrativa.

## Requisitos funcionais

1. A página inicial deve apresentar a marca Terra & Torra, uma proposta de valor, navegação, categorias, produtos em destaque, narrativa da marca e rodapé.
2. O catálogo deve permitir filtrar os produtos fictícios por tipo de café, torra, moagem e faixa de preço.
3. Cada produto deve exibir imagem, nome, origem, notas sensoriais, preço e ação para adicioná-lo ao carrinho.
4. O detalhe do produto deve permitir selecionar moagem e quantidade antes de adicionar o item ao carrinho.
5. O carrinho demonstrativo deve mostrar itens, quantidade, subtotal, alteração de quantidade e remoção de item.
6. A ação de finalizar compra deve informar de forma clara que se trata de uma demonstração e não deve processar pagamento ou coletar dados pessoais.
7. A interface deve oferecer estados de carregamento, lista vazia, produto indisponível e erro com orientação útil.

## Requisitos não funcionais

1. A interface deve funcionar em celular, tablet e desktop sem ocultar funcionalidades essenciais.
2. A navegação deve ser possível por teclado, com foco visível e ordem lógica.
3. Elementos semânticos, rótulos, alternativas textuais e contraste devem atender ao nível AA aplicável do WCAG 2.1.
4. Imagens devem possuir dimensões definidas e carregamento adequado ao contexto para reduzir deslocamento visual.
5. A página deve respeitar a preferência de movimento reduzido.
6. Metadados de título e descrição devem identificar a loja demonstrativa e suas páginas principais.
7. O código não deve conter segredos, chaves ou scripts externos não justificados.
8. A aplicação deve usar dados locais fictícios e não chamar APIs de pagamento, autenticação ou dados de clientes.
9. A aplicação deve usar React 19, TypeScript e Vite, com testes de componentes e comportamento definidos na implementação.
10. `apps/frontend/` deve conter somente os agrupadores de plataforma `web/`, `smartphone/` e `desktop/`, além da documentação de orientação necessária.
11. `apps/frontend/web/` deve conter a aplicação `exemplo-site-web-001/` e um README que oriente a inclusão de aplicações web independentes.
12. `apps/frontend/smartphone/` e `apps/frontend/desktop/` devem conter um README cada, informando a finalidade do diretório e a convenção de uma pasta por aplicação.

## Regras de negócio

1. Todo produto pertence a uma categoria, possui preço positivo, torra, opções de moagem e disponibilidade.
2. Não é permitido adicionar produto indisponível ao carrinho.
3. A quantidade mínima de cada item no carrinho é uma unidade.
4. O subtotal é a soma do preço unitário multiplicado pela quantidade de cada item.
5. Filtros podem ser combinados; quando não houver resultado, a interface deve explicar a situação e permitir limpar filtros.

## Cenários e critérios de aceite

### Catálogo e filtros

- Dado que a pessoa visitante abre o catálogo, quando aplica um filtro de torra, então visualiza apenas produtos compatíveis.
- Dado que filtros não retornam produtos, quando o catálogo atualiza, então é exibido um estado vazio com ação para limpar filtros.

### Carrinho

- Dado que existe um produto disponível, quando a pessoa o adiciona ao carrinho, então o carrinho atualiza quantidade e subtotal sem recarregar a página.
- Dado que um item está no carrinho, quando a quantidade é reduzida abaixo de uma unidade, então a quantidade não se torna inválida.
- Dado que o carrinho contém itens, quando a pessoa seleciona finalizar compra, então recebe aviso de demonstração e nenhum pagamento é processado.

### Qualidade de interface

- Dado que a pessoa navega somente por teclado, quando percorre controles interativos, então todos recebem foco visível e acionável.
- Dado que a preferência por movimento reduzido está ativa, quando a página carrega ou muda de estado, então animações não essenciais são reduzidas ou removidas.
- Dado que a página é visualizada em celular, quando funções de catálogo e carrinho são usadas, então permanecem disponíveis e legíveis.

### Organização de frontend

- Dado que uma aplicação web é criada, quando seus arquivos são gerados, então ficam em `apps/frontend/web/exemplo-site-web-001/`, sem arquivos de aplicação diretamente em `apps/frontend/`.
- Dado que alguém consulta os diretórios de plataforma, quando abre seus READMEs, então encontra orientação objetiva para incluir aplicações web, smartphone e desktop em uma pasta própria.
