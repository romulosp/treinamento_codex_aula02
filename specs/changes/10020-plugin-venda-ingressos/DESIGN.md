# Design do teste de cinema

Host → descoberta segura por manifesto/serviço → `IPluginNegocioApp` do módulo `:plugin-ingressos` → ComposeView e estado de jornada em memória. Caminho `Principal > Venda de ingressos`; a montagem é responsabilidade do host. Staging debug usa a tarefa existente e `plugins/business`. A jornada de cartão é acessada apenas pelo contrato compartilhado mediado pelo host, sem acesso a dados sensíveis.

O núcleo Kotlin detém uma sessão, um pedido pendente e vendas demonstrativas. Validação de moeda usa decimal exato; assentos são identificadores fixos. O plugin mantém referência opcional a IComponentHostContext e chama launchCardWithdrawal na thread principal após ação humana. O host controla lifecycle e callback; APPROVED com cleanup confirmado só habilita emissão demonstrativa explícita. Outros resultados não emitem. O valor não é transmitido e nenhum resultado é interpretado como autorização financeira. Dados não sobrevivem à perda da instância ou processo.

O layout adota mapa de assentos com estados, espaço de tela e resumo destacados, hierarquia visual e ações primária/secundárias. Em telas estreitas os painéis empilham. Usar tons azuis e superfícies claras coerentes com a 10004; não acrescentar dependências.

Reutilizar os SDKs e dependências do módulo de negócio existente; não adicionar bibliotecas. Testar núcleo com JVM e interfaces com testes Compose/instrumentados; validar descoberta no host e preservar plugins existentes. Documentar KDoc, cobertura, ambiente e falhas. Change independente futura deve definir valor, idempotência, autorização, reconciliação e estados reais no contrato de infraestrutura.
