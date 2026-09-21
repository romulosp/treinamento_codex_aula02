# Especificação complementar: tela-catálogo

> **SUPERSEDIDA PARA OPERAÇÃO.** A partir da Change
> `10001-plugin-login-autenticacao`, esta tela e suas regras de identificação
> pertencem exclusivamente ao APK `:plugin-login`. Este documento permanece
> somente como registro histórico da composição visual entregue na Change 9999
> e não autoriza a presença de login no módulo `:app`.

**Autor:** Rômulo Penha

## Objetivo

Ser a evidência executável de que todos os componentes criados nesta Change existem, são interativos e formam uma interface coerente semelhante à imagem local de referência.

## Adendo de composição — 2026-09-20

A tela inicial deixa de exibir a galeria técnica abaixo da primeira dobra e
passa a reproduzir somente a interface operacional anexada pelo solicitante.
O conteúdo visível é: cabeçalho institucional, data/hora demonstrativa,
Gateway/DNS/HTTP, identificação, usuário, senha mascarada, teclado
alfanumérico, teclado numérico, confirmar, limpar, retorno, fixar,
sair/cancelar e instrução inferior.

Os campos são sempre editáveis e selecionam automaticamente o alvo do teclado
virtual ao receber foco. Não existem botões intermediários para escolher
usuário ou senha.

## Componentes reutilizáveis preservados fora da tela inicial

1. Cabeçalho com nome do protótipo, status e ações auxiliares.
2. Painel de identificação com campo de usuário e campo de senha.
3. Teclado numérico.
4. Teclado alfanumérico.
5. Botões primário, secundário, confirmar, cancelar, menu, auxiliar e alternável.
6. Rótulos, painéis, imagens e indicadores de estado.
7. Entradas simples, mascaradas e área multilinha.
8. Escolha exclusiva, múltipla e lista suspensa.
9. Tabela/lista rolável.
10. Ações que abrem diálogos e mensagens.
11. Progresso determinado e indeterminado controlável.
12. Teclado de calculadora demonstrativo.
13. Acesso ao menu demonstrativo.

Esta lista permanece como inventário de componentes implementados. O adendo
de 2026-09-20 substitui a obrigação de exibi-los na mesma rota operacional.

Uma família adicionada durante a implementação deve ser acrescentada a esta lista antes de sua criação.

## Primeiro enquadramento expandido

Em tablet horizontal, o único enquadramento deve reproduzir a hierarquia da nova referência: cabeçalho no topo, credenciais na área central, teclado alfanumérico retangular à esquerda, teclado numérico à direita, ações integradas ao teclado e instrução no rodapé. Não há continuação de galeria abaixo da tela.

## Layout compacto

Em largura compacta, a ordem é: cabeçalho, identificação, ações, teclado numérico, teclado alfanumérico e demais seções. A tela inteira é alcançável verticalmente; grades internas não devem exigir rolagem horizontal.

## Compatibilidade de janela

- Não usar largura/altura em pixels físicos nem reproduzir coordenadas absolutas da referência.
- Recalcular composição sempre que o tamanho real da janela mudar.
- Em largura média, distribuir seções em uma ou duas colunas somente quando a largura mínima de cada controle for preservada.
- Em largura expandida, limitar a largura útil do conteúdo quando necessário para evitar distorção e usar margens responsivas.
- Em dobráveis, não posicionar ações críticas sobre separação ou dobradiça informada pelo sistema.
- Em edge-to-edge, aplicar insets de sistema e gestos às áreas de conteúdo e ação.
- Em qualquer classe de janela suportada, todo componente permanece alcançável por toque e acessibilidade.

## Estado e eventos

`CatalogUiState` deve conter valores dos campos, entrada-alvo, seleções, diálogo ativo, progresso e destino atual. `CatalogEvent` representa ações do usuário. Dados são somente demonstrativos.

## Previews e screenshots

Devem existir previews determinísticos e testes de screenshot para:

- 320 x 568;
- 360 x 800;
- 400 x 500;
- 600 x 960;
- 800 x 600;
- 840 x 900;
- 900 x 1000;
- 1200 x 800;
- 400 x 500 com escala de fonte 1,5;
- ao menos uma janela compacta com escala de fonte 2,0 para inspeção de bloqueios;
- estados normal e com diálogo aberto.

## Critérios específicos

- Um teste de UI percorre a tela e encontra um identificador semântico de cada família.
- O screenshot 800 x 600 é comparado lado a lado com `source-material/imagens/referencia/tela1.jpg` por revisor humano.
- Diferenças por acessibilidade, adaptabilidade, marca ou recurso ausente são registradas e não tratadas como falha se aprovadas.
- Testes de redimensionamento comprovam que valores digitados, seleção, diálogo, foco e posição de navegação não são perdidos.
