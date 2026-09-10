# SPEC: 066-lib-pinpad-abecs-go — Comando MNU

## Status
`RASCUNHO`

## Objetivo
Apresentar um menu no pinpad e devolver a opção selecionada pelo usuário.

## Contrato
- Entrada: timeout, título e opções, com limites e formato definidos pelo manual.
- Builder não deve aceitar quantidade, título ou opção que gere payload inválido.
- Parser deve distinguir seleção válida, cancelamento, timeout, tecla e status ABECS.
- A espera é bloqueante e deve receber `context.Context`.
- Rastro identifica `CMD=MNU`; texto de menu deve ser redigido caso o consumidor o classifique como sensível.

## Critérios de aceite
- [ ] Menu é exibido e uma opção é selecionada em pinpad real.
- [ ] Cancelamento físico e timeout de protocolo são diferenciados.
- [ ] Índice selecionado não é inferido quando a resposta for inválida.
- [ ] Contexto cancelado interrompe a operação e o worker retorna ao estado OPEN.

## Referências
`spec.md` RF-012.3/RF-013; manual ABECS v2.12.
