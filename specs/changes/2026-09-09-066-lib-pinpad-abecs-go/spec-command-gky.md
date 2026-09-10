# SPEC: 066-lib-pinpad-abecs-go — Comando GKY

## Status
`RASCUNHO`

## Objetivo
Ler, limpar ou aguardar tecla no pinpad através dos modos ABECS de `GKY`.

## Contrato
- Builder deve aceitar somente modos definidos: espera, limpeza e obtenção, conforme catálogo aprovado.
- Parser deve mapear códigos conhecidos para tipos Go; código desconhecido não pode virar `GKY_KEY_NONE` silenciosamente.
- Timeout do comando, cancelamento de contexto e tecla de cancelamento são resultados distintos.
- Rastro identifica `CMD=GKY`, sem registrar dados digitados como se fossem tecla comum.

## Critérios de aceite
- [ ] Espera de tecla real retorna códigos OK, CANCEL, CLEAR e F1-F4 corretamente.
- [ ] Timeout e cancelamento são distinguíveis.
- [ ] Código desconhecido produz erro tipado ou resultado explicitamente desconhecido.
- [ ] O estado do serviço volta a OPEN após a operação.

## Referências
`spec.md` RF-012.6/RF-013; manual ABECS v2.12.
