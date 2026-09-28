# SPEC: 066-lib-pinpad-abecs-go — Comando GKY

Autor: Rômulo Penha

## Status
`SPEC_APROVADA`

## Objetivo
Aguardar uma tecla não numérica no pinpad com o comando clássico `GKY`.

## Contrato
- Builder emite somente o payload literal `GKY`, sem parâmetros.
- Parser mapeia os status 000, 004–008 e 013 para os resultados de tecla; código desconhecido não pode virar resultado válido silenciosamente.
- Timeout do comando, cancelamento de contexto e tecla de cancelamento são resultados distintos.
- Rastro identifica `CMD=GKY`, sem registrar dados digitados como se fossem tecla comum.

## Critérios de aceite
- [ ] Espera de tecla real retorna códigos OK, CANCEL, CLEAR e F1-F4 corretamente.
- [ ] Timeout e cancelamento são distinguíveis.
- [ ] Código desconhecido produz erro tipado ou resultado explicitamente desconhecido.
- [ ] O estado do serviço volta a OPEN após a operação.

## Referências
`spec.md` RF-012.6/RF-013; manual ABECS v2.12.


## Correção normativa de 2026-09-13

GKY é o payload literal `GKY`, sem modo ou timeout. A tecla é codificada no
`RSP_STAT`: 000 OK, 004..007 F1..F4, 008 CLEAR e 013 CANCEL.
