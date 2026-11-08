# Design

Adicionar facetas 1.5 com overloads e opções separadas do pedido monetário. Isso mantém ABI 1.4 do data class e métodos antigos. BusinessPluginManager oferece nova faceta ao consumidor; ComponentPluginManager exige o novo tipo para chamadas com opções, sem fallback, e usa o mesmo Invocation/finish já existente. Ingressos solicita CALLER; não há condição por nome de plugin no host ou componente.

PluginSaqueCartaoApp implementa ambas as rotas herdadas e a nova. Cria owner com returnCompletedToCaller=true somente na nova rota CALLER. O owner aguarda cleanup em begin/finally e, se APPROVED/CAPTURED e nenhuma falha/cancelamento/detach, publica closedState e faz retorno cooperativo uma vez. Não chamar requestReturn de um efeito Compose que dependa da tela terminal; a decisão pertence ao owner após close. Remoção da View encontra owner disposed, sem repetir I/O.

Factory de SaqueRepository no construtor interno permite testar a fábrica real e o attach/collect/state/callback com implementação exclusivamente sintética. Construtor público sem argumentos mantém produção com GoSaqueRepository. Não acessar arquivos privados, hardware ou rede nos testes.

CinemaJourney amplia somente o conjunto local de conclusão demonstrativa para APPROVED/CAPTURED com cleanup; status não é alterado no componente/host. Manter ID capturado de solicitação e timer identificado. A tela de sucesso já contém aviso explícito de demonstração.
