# Revisão da SPEC — aditivo 071 na Change 074

Data: 2026-09-30

## Escopo e evidências

- Fonte histórica: `specs/archive/2026-09-27-071-bat-teste-modulo-windows/`.
- Correção posterior: `specs/archive/2026-09-27-072-corrigir-abertura-bridge-android/`.
- Contrato consolidado: `proposal.md`, `spec.md` seção 4.1.1 e CA-074-18,
  `DESIGN.md`, `analysis.md`, `traceability.md` e `test-matrix.md`.
- Código confrontado: `apps/desktop/libpinpadabecsgo/testar_bridge_pinpad.bat`.

## Parecer técnico

O contrato BAT da 071 está autocontido na 074. A atribuição original de COM
fixa foi explicitamente substituída por `PORTA_PINPAD` herdada, conforme 072.
Foram preservados `setlocal`, execução a partir da raiz do módulo, seleção do
modo físico, configuração/log visíveis, paths com espaços, saída legível e
propagação do exit code. A matriz adiciona cenários próprios do launcher.

O BAT observado contém as principais operações exigidas, mas a revisão
documental não executou BAT, Bridge, ADB ou pinpad. Seu comportamento em
hardware e todos os casos de falha permanecem sujeitos à validação formal.

## Gate

Parecer técnico favorável ao aditivo. A aprovação humana anterior da 074 não
cobria a 071 recém-incorporada; a autorização posterior está registrada em
`2026-09-30-spec-approval-071-amendment.md`. As cinco fontes arquivadas
permanecem intactas.
