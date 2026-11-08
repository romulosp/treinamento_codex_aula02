# Design — apresentação da bilheteria

## Estrutura
Usar `ComposeView` e a rota existentes. Separar a apresentação em arquivo próprio quando reduzir o tamanho do ponto de entrada. `CinemaScreen` permanece uma função de estado/callbacks, sem acesso ao host ou hardware. O único novo estado local permitido é expansão do editor de sessão; campos e jornada mantêm seus owners.

Revisão r4: seguir a imagem enviada pelo usuário. O carrossel de cards largos ocupa o topo inteiro; abaixo ficam horário e mapa sem cartões brancos à esquerda e resumo branco à direita. Fundo azul claro em gradiente, barra cinza TELA, desenho de poltronas e CTA azul arredondado. Não há slogan, etapas numeradas ou cabeçalho escuro do resumo. O editor existente abre em diálogo por Configurar sessão; cartaz preenche apenas o rascunho do filme e abre o mesmo diálogo. Sem sessão criada automaticamente. Em compacto, empilhar conteúdo rolável. Layout usa a largura interna recebida do host.

O cartaz utiliza ilustrações do gerador integrado, conforme escolha final do usuário após falhas de cota da API Google. Associar Batman, Avatar e Coringa/Joker por título normalizado; outros títulos usam a capa genérica de cinema. Preservar os PNGs originais no workspace e produzir WebP de 512×768 para uso no APK. A galeria permite navegação visual e preenchimento do cadastro existente; não é catálogo de estoque/sessões. Não criar horários alternativos.

O host fornece seu Context à View do plugin e não injeta Resources do APK externo. Usar recursos Java em `src/main/resources/cinema/posters/`, carregados pelo classloader de PluginIngressosApp e decodificados em Dispatchers.IO por produceState. Não usar IDs R do plugin contra Resources do host. Sem download em execução, segredos ou dependências novas.

Fileiras A e B contêm seis poltronas cada e espaçamento de corredor entre a terceira e a quarta. Cada ícone possui encosto, braços e base desenhados localmente, com alvo de toque de pelo menos 48dp. Texto do assento permanece legível; estados são expostos em semântica e legenda. A fileira tem rolagem horizontal se necessário. O resumo mostra preço base, pedido, estado financeiro local e ingressos demonstrativos já emitidos, preservando condições existentes. A imagem de referência tem mais lugares/horários e dois itens no pedido; esses dados não serão inventados nesta correção apenas visual.

## Fontes e decisões — consulta em 08/11/2026
- [Layouts adaptativos Android](https://developer.android.com/develop/ui/compose/layouts/adaptive/get-started-with-adaptive-apps): referência para adaptação ao espaço disponível. Classificação RECOMMENDED; breakpoint 840dp é decisão deste produto.
- [Acessibilidade dos componentes Compose](https://developer.android.com/develop/ui/compose/accessibility/api-defaults): referência para alvos mínimos e semântica. Classificação REQUIRED nesta Change para os controles modificados.
- [Catálogo Android Skills](https://developer.android.com/tools/agents/android-skills/browse): consultado como candidata; decisão REJECT para incorporação externa nesta Change porque as skills locais já cobrem apresentação, estado e testes. Nenhuma skill externa copiada/instalada; versão/commit e licença não avaliados para importação.

## Skills e contratos
Aplicar android-native-engineering e especializações compose-component-design, compose-state-and-effects, compose-focus-navigation e compose-ui-testing-patterns. Documentar componentes e fronteiras em KDoc. Sem reestruturação de camadas, persistência ou nova biblioteca. Nenhum ADR de alteração arquitetural é necessário porque os limites existentes permanecem.
