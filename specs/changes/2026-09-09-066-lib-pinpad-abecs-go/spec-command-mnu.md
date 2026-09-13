# SPEC: 066-lib-pinpad-abecs-go — Comando MNU

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

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

## Ajustes implementados
- O builder do comando MNU foi corrigido para respeitar o protocolo ABECS: timeout em `SPE_TIMEOUT`, título em `SPE_DSPMSG` e opções em `SPE_MNUOPT`, em vez de serializar um payload textual simples.
- A validação agora rejeita respostas inválidas quando a seleção não é numérica ou quando o payload não contém índice suficiente para concluir a operação.
- Testes de regressão cobrem o cenário do menu interativo e a resposta TLV `PP_VALUE` com índice de dois dígitos.

## Referências
`spec.md` RF-012.3/RF-013; manual ABECS v2.12.


## Complemento normativo de 2026-09-13

MNU aceita 1..20 opções de 1..24 bytes, título opcional de até 128 bytes e
`SPE_TIMEOUT` X1 de 0..255. `PP_VALUE` contém exatamente N2 de `01` a `20`;
resposta com seleção de um dígito é inválida.
