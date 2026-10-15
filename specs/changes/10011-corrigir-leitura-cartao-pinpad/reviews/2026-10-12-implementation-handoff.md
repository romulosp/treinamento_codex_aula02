# Entrega para revisão da implementação — 10011

Data: 2026-10-12. Estado da etapa: `IMPLEMENTADA`, conforme autorização do solicitante.
Este registro encaminha evidências ao próximo gate. Não é uma revisão formal,
uma validação física nem uma aprovação da própria entrega.

## Alterações entregues

- Compra `00h`, modo tipado e roteamento GTK → GOX para 03/06, GTK → GPN para 00/05.
- Fonte PIN integral validada, snapshot privado e índice importado; DATA GTK separado.
- Contexto PAN privado de uso único, correlação/sessão, descarte e bloqueio após falha.
- GCX interpreta trilhas incompletas pelos identificadores corretos; seleção inconsistente é rejeitada.
- Fallback limitado com filtros preservados e instrução externa antes da repetição.
- Diagnóstico sanitizado com comando/status; fechamento sem Cancel desnecessário e sem falso sucesso após perda do transporte.
- AAR/source jar/proveniência e APKs gerados; Bridge recompilado em `apps/desktop/libpinpadabecsgo/build/10011/`.

## Evidências e próximos gates

Consultar [validation.md](../validation.md) e [plano de implementação](../implementation-plan.md).
O revisor deve comparar os requisitos e AC-01..AC-09 com código/testes/artefatos.
Testes com dados/transporte sintéticos não substituem demonstração no pinpad físico.
Race ficou pendente por ausência de compilador C no ambiente Windows.

Sem atualização de `specs/system/`, commit ou archive nesta etapa. A aprovação
da SPEC continua registrada na REV-001; aprovação final da implementação ainda
depende dos gates posteriores.
