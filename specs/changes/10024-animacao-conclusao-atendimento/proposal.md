# Proposta — animação e conclusão do atendimento

## Status
`SPEC_APROVADA` — [revisão](reviews/2026-11-08-spec-review.md).

## Objetivo
Atender ao pedido do usuário: carrossel animado e, ao concluir pagamento com sucesso, mostrar “Transação Realizada com Sucesso” e voltar ao início para um novo atendimento.

## Escopo
Deslocamento suave pelas setas/seleção dos cartazes; confirmação após APPROVED local com cleanup confirmado; emissão demonstrativa automática uma única vez; tela de sucesso por 3 segundos; retorno automático à seleção inicial de filmes no mesmo plugin, limpando dados do pedido e preservando estoque vendido.

## Limites
Sem alterar horário/configuração do cinema, preços, contrato financeiro, autenticação, host de produção, componente de cartão ou Go/Bridge. A mensagem descreve conclusão local da demonstração, mantendo aviso de ausência de comprovação financeira e de validade do ingresso. Não automatizar pagamento físico, impressão ou chamada financeira. Sem nova biblioteca/versão ou uso de API de imagens.

Substitui a etapa manual Emitir ingresso demonstrativo das Changes anteriores, conforme solicitação atual de conclusão e reinício automáticos. “Tela inicial” significa início da bilheteria, preservando login/Host.
