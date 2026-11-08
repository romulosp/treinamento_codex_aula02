# Design

Adicionar completedOrderId ao estado da jornada. Após APPROVED/cleanup, o owner atualiza soldBySession e estado de confirmação, limpando pedido pendente. startNewAttendance(completedOrderId) só reseta estado se a conclusão identificada ainda for a atual. Durante confirmação, comandos de seleção/criação são ignorados. O mapa privado de vendidos não é apagado.

O callback da rota captura request.orderId e passa ao método interno paymentCompleted. O owner exige correspondência com pendingOrderId e pagamento ativo; isso impede retorno antigo de concluir pedido novo após o reset. Não alterar CardWithdrawalResult, CardPaymentRequest ou API compartilhada.

CinemaRoute usa LaunchedEffect(completedOrderId), delay(3000) e comando identificado de novo atendimento. Não executar timer/navegação no corpo de composição nem como callback desprotegido. Tela de sucesso substitui a bilheteria; ao retornar, o estado local do carrossel reinicia. Manter o aviso demonstrativo junto da mensagem exata solicitada. Não retornar ao login/menu nem criar Activity.

Carrossel: animateScrollToItem em escopo Compose nas setas/seleção, snap nativo por gesto e animateFloatAsState de escala com leitura em graphicsLayer para realce breve. Teste com relógio controlado mede posição antes/durante/depois, não somente destino final. Remover ação manual de emissão e exibição de recibo antigo no resumo do próximo atendimento.

## Fontes oficiais consultadas em 08/11/2026
- [Listas Compose e controle de scroll](https://developer.android.com/develop/ui/compose/lists#controlling-scroll-position): API nativa para movimento animado. Classificação REQUIRED para atender ao movimento pedido, sem nova biblioteca.
- [Efeitos Compose](https://developer.android.com/develop/ui/compose/side-effects#launchedeffect): escopo, identidade e cancelamento do efeito. Classificação REQUIRED para o timer desta Change.

Skills locais Android, animations, state/effects, componentes, foco e testes já adotadas. Não importar skills externas nem alterar stack. Atenção a callback duplicado, perda da composição e estoque preservado; os testes devem distinguir confirmação local e autorização financeira inexistente.
