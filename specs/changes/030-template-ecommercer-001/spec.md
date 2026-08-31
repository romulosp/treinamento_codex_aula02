# SPEC: 030-template-ecommercer-001

## Status

`SPEC_APROVADA`

## Referências e dependências

- `proposal.md`, `DESIGN.md`, `tasks.md` e `validation.md` desta mudança.
- `AGENTS.md` e `specs/shared/process/workflow.md`.
- React 19, TypeScript e Vite, conforme a base já adotada no frontend web.
- Tailwind CSS, instalado exclusivamente na nova aplicação.
- Google Fonts (CSS) e imagens originais locais, limitados à apresentação demonstrativa.

## Requisitos funcionais

### RF-001 — Aplicação e estrutura

1. Deve existir uma aplicação independente em `apps/frontend/web/template-ecommercer-001/`.
2. A aplicação deve usar React, TypeScript, Vite e Tailwind CSS.
3. A estrutura de componentes deve conter arquivos ou diretórios dedicados para `Header`, `HeroSection`, `ProductGrid`, `ProductCard`, `FastCart` e `Footer`.
4. Produtos e preços devem ser dados locais fictícios; nenhum dado de produto deve ser obtido por rede.
5. O `.gitignore` deve permitir o rastreamento de arquivos de código, configuração e documentação somente em `apps/frontend/web/template-ecommercer-001/`, mas continuar ignorando `node_modules/`, `dist/` e `coverage/` dessa aplicação.

### RF-002 — Fase 1: design system e shell

1. A interface deve iniciar em fundo off-white ou gelo, com superfícies em rosa pó suave e CTAs em tom escuro com texto branco.
2. O shell deve incluir `Header` semântico, navegação acessível e botão de carrinho com contagem de itens e nome acessível.
3. Os tokens de cor, tipografia, raio, sombra, foco e espaçamento devem estar definidos para reutilização por classes utilitárias do Tailwind e/ou tema correspondente, sem criar regras globais de componente desnecessárias.
4. A entrega da fase deve parar para aprovação explícita da pessoa usuária antes de iniciar a Fase 2.
5. Após a reprovação da primeira apresentação, a nova entrega da Fase 1 deve incluir um `HeroSection` visual para permitir avaliação concreta da linguagem da marca, sem implementar catálogo ou FastCart.
6. A tipografia deve usar `DM Serif Display` e `DM Sans` pelo Google Fonts com `display=swap`, preconexão e fallback local.
7. O hero deve usar imagens editoriais originais locais, com texto alternativo, espaço reservado por proporção e tratamento visual que mantenha a legibilidade do texto.

### RF-003 — Fase 2: hero e vitrine

1. A página deve apresentar `HeroSection`, `ProductGrid`, `ProductCard` e `Footer` com composição editorial e responsiva.
2. Cada card deve exibir imagem ou placeholder visual, nome, preço, informação de desconto quando aplicável e ação para adicionar ao carrinho.
3. O hero e os cards devem usar uma estética de fotografia de moda com profundidade suave; quando não houver imagem fornecida, o placeholder deve comunicar claramente que é ilustrativo e preservar o layout.
4. Imagens fora do conteúdo acima da dobra devem usar `loading="lazy"`; toda imagem informativa deve ter texto alternativo adequado.
5. A entrega da fase deve parar para aprovação explícita da pessoa usuária antes de iniciar a Fase 3.

### RF-004 — Fase 3: FastCart

1. Ao adicionar um produto disponível, a aplicação deve atualizar o carrinho sem recarregar a página, abrir o `FastCart` em slide-over e oferecer feedback visual de sucesso.
2. O FastCart deve listar itens, permitir aumentar, diminuir e remover quantidades, apresentar subtotal derivado e disponibilizar CTA de finalização demonstrativa.
3. A finalização deve informar que a aplicação é uma demonstração e não pode processar pagamento, coletar dados pessoais ou fazer chamadas de rede.
4. O estado do carrinho deve ser persistido em `localStorage` após mudança de estado; falhas de leitura, JSON inválido ou formato inesperado devem resultar em carrinho vazio sem quebrar a interface.
5. A busca e a atualização lógica de um item devem ser indexadas por identificador do produto, sem chamada de rede. Esta é a definição verificável de acesso O(1); a renderização da lista continua proporcional à quantidade de itens visíveis.
6. O FastCart deve ser totalmente utilizável por teclado, ter foco inicial apropriado, permitir fechar por Escape e restaurar o foco ao controle que o abriu.
7. A entrega da fase deve parar para aprovação explícita da pessoa usuária antes de iniciar a Fase 4.

### RF-005 — Fase 4: revisão técnica

1. Após a aprovação das três fases visuais, a implementação deve executar as verificações aplicáveis de testes, lint, build, acessibilidade, SEO, desempenho e segurança.
2. Ajustes decorrentes de achados devem permanecer limitados ao escopo desta SPEC.
3. A mudança deve permanecer em `specs/changes/` até a validação visual final explícita da pessoa usuária; não deve haver commit nem arquivamento nesta etapa.

## Requisitos não funcionais

1. A interface deve funcionar em celular, tablet e desktop, sem perder as ações de navegação, vitrine ou carrinho.
2. HTML semântico, foco visível, contraste AA aplicável, rótulos acessíveis e ordem de tabulação lógica são obrigatórios.
3. Animações não essenciais devem respeitar `prefers-reduced-motion`.
4. Não pode haver inserção de HTML cru (`dangerouslySetInnerHTML`) ou interpolação insegura de dados de produto.
5. O documento deve conter título, descrição e JSON-LD básico de produtos fictícios, sem alegar disponibilidade, avaliações ou oferta reais.
6. Não devem ser incluídos segredos, chaves, rastreadores ou scripts remotos não justificados. A folha de estilos do Google Fonts é a única dependência remota documentada para esta demonstração.
7. Deve haver testes automatizados para as interações observáveis do carrinho e os comandos executados devem ser registrados em `validation.md`.

### RF-006 — Cobertura Vintage como demonstração React original

1. A aplicação deve disponibilizar 30 telas demonstrativas equivalentes aos tipos funcionais de `templates/` do pacote local Vintage: início, busca, coleção, produto, pré-venda, quick view, carrinho, lista de coleções, blog, artigo, páginas institucionais, 404, senha, gift card e as sete telas de cliente.
2. As telas de cliente, pedido, endereço, senha e gift card devem declarar visualmente seu caráter demonstrativo e não podem armazenar credenciais, dados pessoais ou dados de pagamento.
3. Deve existir um registro local contendo 71 entradas de módulos, correspondentes às sections inventariadas no pacote de referência, agrupadas em navegação, vitrine, produto, conteúdo, formulário, mídia e utilitários.
4. A página inicial deve compor ao menos: aviso, cabeçalho, hero, faixa de texto, coleções, produtos em destaque, narrativa editorial, shop-the-look, benefícios, depoimentos, blog, newsletter e rodapé. Cada módulo deve ter conteúdo e estrutura originais.
5. A implementação não pode importar, executar, transcrever ou adaptar arquivos Liquid, CSS ou JavaScript do pacote Vintage. Os arquivos de referência são somente fonte de inventário e de direção visual.
6. Imagens adicionadas à aplicação devem ser originais geradas para o projeto ou fornecidas com autorização explícita; não serão usadas referências `shopify://` do tema.

### RF-007 — Navegação demonstrativa

1. A aplicação deve oferecer uma navegação de demonstração entre as telas cobertas, sem depender de servidor.
2. O FastCart deve continuar com itens e subtotal locais; ações de finalização, conta e formulários devem retornar mensagens de demonstração claras, sem solicitações de rede.
3. Componentes de módulo devem receber dados explícitos e não aceitar HTML cru proveniente de configuração.

## Cenários e critérios de aceite

- [ ] **CA-001:** a nova aplicação existe no caminho definido e não altera a aplicação Terra & Torra.
- [ ] **CA-002:** na Fase 1, a pessoa usuária encontra cabeçalho, navegação, botão de carrinho com nome acessível e tokens visuais de acordo com RF-002.
- [ ] **CA-003:** na Fase 2, a pessoa usuária encontra hero, vitrine de produtos e rodapé responsivos; imagens abaixo da dobra usam carregamento preguiçoso.
- [ ] **CA-004:** ao adicionar produto disponível, o FastCart abre sem recarregar, confirma a ação e atualiza quantidade e subtotal.
- [ ] **CA-005:** ao recarregar a página após alterar o carrinho, os itens válidos são restaurados; um valor inválido de armazenamento não quebra a aplicação.
- [ ] **CA-006:** uma pessoa que usa somente teclado abre, utiliza e fecha o FastCart, inclusive com Escape, mantendo foco visível.
- [ ] **CA-007:** finalizar compra informa tratar-se de demonstração e não realiza solicitação de rede ou pagamento.
- [ ] **CA-008:** testes, lint e build aplicáveis são aprovados e as evidências são registradas com comando e código de saída.
- [ ] **CA-009:** antes de cada mudança de fase e antes de qualquer encerramento, há aprovação visual e comportamental explícita da pessoa usuária registrada em `validation.md`.
- [ ] **CA-010:** o registro local contém 30 telas e 71 módulos, com teste automatizado que confirma as contagens e a ausência de URLs `shopify://` nos dados locais.
- [ ] **CA-011:** início, catálogo, produto, carrinho, conteúdo e telas demonstrativas de cliente são alcançáveis pela navegação sem back-end.
- [ ] **CA-012:** a página inicial entrega composição e conteúdos próprios, sem marca, textos ou markup copiados do tema de referência.
