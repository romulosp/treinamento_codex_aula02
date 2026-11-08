# Design — sessão programada por filme

## Modelo
`CinemaCatalog` contém nome do cinema e lista de `CinemaMovie`. Cada filme contém ID/título e `CinemaConfiguredSession` com ID globalmente único, sala, horário, preço inteiro em centavos e ocupados configurados. O objeto `DemoCinemaCatalog` fornece a única configuração demonstrativa, sem I/O. `CinemaJourney` recebe catálogo por construtor, valida consistência e publica CinemaState imutável.

CinemaState acrescenta IDs do filme/sessão e ocupados pré-configurados. O campo sold continua representando somente ingressos emitidos localmente. Um mapa privado por ID de sessão preserva essas vendas ao trocar filme; nenhuma garantia de persistência ou concorrência externa. CinemaSession continua projeção de filme/sala/horário/preço consumida pelo pedido e recibo.

## Fluxo
Sem filme selecionado → seleção de filme carrega sua sessão fixa → escolha de um assento livre → criar pedido → abrir cartão por ação explícita → resultado local/cleanup → emissão demonstrativa. Nenhum formulário ou etapa de escolha de horário. Enquanto existe pedido pendente a sessão não muda, mesmo por chamada direta ao owner. Após cancelar ou emitir, seleção de outro filme é permitida; disponibilidade anterior fica preservada na instância.

## UI e integração
Remover estado/controles de edição da rota e tela. Carrossel recebe o catálogo real e emite somente onMovie(id). A faixa abaixo mostra dados de sessão como leitura; não criar callback onTime/onSession. Preservar map/summary, acessibilidade, arte local assíncrona e layout r4. A imagem genérica continua fallback de Spider-Man.

CardPaymentRequest continua construído a partir do pedido e preço base da sessão congelada. SharedApi 1.4, host, componente financeiro, taxas e callbacks não mudam. Atualizar apenas o teste de integração do host para selecionar filme em vez de cadastrar.

## Riscos e decisões
Separar ocupados configurados de ingressos emitidos evita recibos falsos. IDs estáveis impedem misturar estoque de sessões diferentes. Bloqueio no owner evita mudança de valor durante pagamento; bloqueio visual apenas reflete a regra. Preços/salas/horários do catálogo são dados demonstrativos explicitamente classificados como tais. Sem nova consulta externa ou dado sensível.

## Evidência de ambiente de teste de teclado
Consulta oficial em 08/11/2026: [Keyboard focus management in Compose, seção de testes](https://developer.android.com/codelabs/large-screens/keyboard-focus-management-in-compose?hl=en). Testes sem teclado físico precisam solicitar `InputMode.Keyboard` no ambiente de teste. Adotar somente essa preparação no teste, seguida de foco nativo conhecido e teclas TAB/ENTER reais. Não alterar foco, input mode ou grafo de navegação do código de produção para satisfazer a instrumentação. Classificação REQUIRED para o teste de teclado desta Change.
