# DESIGN — 10010-estilo-menu-aplicacao

## Estrutura
Manter host e contratos de plugins existentes. Tokens centralizados em arquivo Kotlin no módulo app, sem dependência adicional. CoreShell continua sendo o único proprietário do cabeçalho.

A aparência específica do menu deve ser selecionada pelo ramo já existente que apresenta BusinessMenuScreen. Pode-se adicionar parâmetro visual opcional com default equivalente ao comportamento atual em CoreShell/AuthenticationHeader; preservar chamadas existentes. Não alterar a variante LoggedOut ou a apresentação das telas hospedadas dos plugins. Essa extensão visual não altera estado de sessão.

BusinessMenuScreen contém fundo em gradiente e estrutura com rodapé e botão de saída fixos. Usar um único contêiner vertical rolável no espaço restante. Não inserir outro cabeçalho em Scaffold. Não adicionar scroll vertical dentro da grade. Se Scaffold for usado, conferir insets e posição da saída: FAB padrão não garante automaticamente os 24 dp normativos.

Renderizar grupos e folhas na ordem recursiva existente. Cada agrupador tem seção identificável; folhas consecutivas são distribuídas em linhas de largura fixa, centralizadas. Preservar ids e callbacks; nenhuma lista fictícia substitui itens reais. Grade/linhas adaptadas às restrições recebidas, sem cálculo baseado em display físico.

## Componentes e estados
MenuDesignTokens (ou equivalente), título de seção, card, grade por seção, rodapé e saída podem ser composables privados. InteractionSource permite observar hover, pressed e focus sem criar estado funcional. Focus ring explícito e cores normativas, com estado disabled prioritário. Movimento reduzido deve usar mecanismo compatível com as APIs e versões presentes, confirmado na implementação; ausência de movimento exclui escala decorativa.

Header trunca textos em uma linha e mantém a leitura integral acessível. Não duplicar contentDescription pai/filho. Uma borda tracejada desenhada localmente substitui a borda sólida na variante do menu.

## Decisões
Sem mudança de módulos, DI, perfil arquitetural ou SharedApi. Sem atualização de versões. Breakpoints 1200/1024 em dp. A SPEC de origem prevalece sobre coordenadas do mockup. Terminal/lotérica reais não são criados por esta mudança.

## Verificação
Testes instrumentados dos callbacks e estados; cenários de tamanho, densidade, hierarquia, foco, hover, saída e reduced motion. Preservar testes funcionais existentes; atualizar somente expectativas visuais que mudaram de forma autorizada. Plano técnico detalhado será registrado após este gate, antes de código.
