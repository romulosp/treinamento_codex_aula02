# SPEC — animação, sucesso e próximo atendimento

## Status
`SPEC_APROVADA` — [revisão](reviews/2026-11-08-spec-review.md).

## Requisitos
1. Manter carrossel/visual/configuração da 10023. Setas movimentam a lista com rolagem animada; selecionar filme também o traz à posição inicial visível com animação. Realce de seleção com transição breve de escala, sem deslocar layout ou criar nova seleção/pagamento. Snap por gesto permanece nativo. Corrotinas de movimento pertencem à composição.
2. Somente callback de pedido em pagamento com status APPROVED e cleanupConfirmed=true conclui atendimento. A rota captura o ID do CardPaymentRequest e o owner compara esse ID ao pedido ativo; retorno atrasado de pedido anterior é ignorado mesmo se um novo pagamento já começou. Marcar o assento vendido localmente uma única vez, conservar estoque por sessão e retirar a ação manual de emissão. Callback duplicado não duplica venda ou confirmação. Demais status ou cleanup não confirmado não mostram sucesso nem limpam pedido/seleção.
3. Apresentar tela de conclusão com o texto exato **Transação Realizada com Sucesso**, ícone de confirmação e aviso discreto de demonstração/resultado local/ingresso sem validade. Anunciar a mensagem por semântica de live region. Não alegar autorização financeira real a partir do contrato local.
4. Após 3000ms de exibição, voltar automaticamente à tela inicial de filmes dentro do plugin, sem logout ou saída para menu do Host. Limpar filme/sessão selecionados, assento, pedido/ID, resultado/cleanup e mensagem do atendimento. Preservar catálogo, ocupados configurados e vendas por sessão na instância; reabrir o filme mantém assento vendido bloqueado.
5. Enquanto a confirmação está ativa, bloquear seleção, novo pedido e novas chamadas financeiras no owner. Reinício é condicionado ao ID da conclusão; timer/callback obsoleto não pode apagar um atendimento posterior. Efeito de timer é cancelado ao sair da composição; reentrada de confirmação ainda pendente pode reapresentá-la sem nova emissão.
6. Não reapresentar recibos do atendimento anterior no resumo de um novo atendimento; o estoque vendido permanece apenas para indisponibilidade. Todos os demais limites demonstrativos/voláteis e a seleção de filme/assento da 10023 permanecem.

## Arquitetura e baseline
Mesmo módulo/package/perfil SIMPLE e stack aprovados na 10023, sem dependências ou versões novas. CinemaJourney coordena conclusão identificada e estoque. CinemaRoute recebe callbacks e usa LaunchedEffect identificado pela conclusão para temporização cancelável. CinemaScreen apresenta estado puro (bilheteria ou sucesso) e eventos. Carrossel mantém estado de scroll e escopo Compose. Nenhuma alteração de shared-api, host de produção ou componente financeiro.

## Critérios de aceite
- CA-01: teste de frames intermediários comprova deslocamento gradual do carrossel, seguido de posição final; setas/seleção/gesto continuam operantes.
- CA-02: APPROVED/cleanup confirma automaticamente sem botão de emissão; texto exato aparece antes do retorno, com aviso demonstrativo.
- CA-03: após 3 segundos, seleção inicial sem pedido anterior, nova compra recebe outro ID e assento anterior continua vendido; callbacks/timers duplicados ou obsoletos não reiniciam outra compra.
- CA-04: recusa, cancelamento, UNKNOWN/CAPTURED, erro e cleanup falho não mostram sucesso nem reiniciam automaticamente; preservados preço/ID/retentativa e mediação pelo Host.
- CA-05: JVM, cobertura elegível mínima 80%, lint, builds/test APKs, instrumentação e integração sintética no Host, capturas de sucesso e início, ambiente/comandos/saídas e KDoc registrados. Validador estrutural executado, sem ocultar reprovação preexistente.
- CA-06: host final instalado para teste humano; sem login/cartão/PIN automáticos, archive ou commit antes dos gates formais.
