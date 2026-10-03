# Tasks — 074 Pinpad Android Bridge Stack

## Especificação e consolidação

- [x] Verificar disponibilidade do ID 073 em changes e archive.
- [x] Selecionar o próximo ID livre, 074, e registrar a decisão.
- [x] Ler as quatro fontes originais e a 071 adicionada, com seus contratos
  transversais relevantes.
- [x] Identificar responsabilidades, sobreposições, correções, supersession,
  contradições, comportamento final e lacunas.
- [x] Confrontar a baseline consolidada com o código atual.
- [x] Criar proposal, SPEC, DESIGN, rastreabilidade e plano de testes.
- [x] Registrar evidências documentais e limitações do ambiente.
- [x] Obter revisão formal da SPEC.
- [x] Obter aprovação humana da SPEC.
- [x] Incorporar o contrato final da 071 ao aditivo documental da 074.
- [x] Revisar e obter aprovação humana do aditivo 071 antes de considerá-lo
  parte formalmente aprovada da baseline.
- [x] Reauditar a incorporação integral da 072 na 074.
- [x] Explicitar que a decisão aprovada da 074 supersede o fallback direto da
  072 e exige `PORTA_PINPAD` em todo processo físico.
- [x] Registrar nova revisão técnica da consolidação 072.

## Implementação

- [x] Não iniciar antes de proposal.md e spec.md estarem formalmente
  aprovados.
- [ ] Corrigir ou decidir as divergências D-001 a D-008.
- [x] Implementar core ABECS, transporte, Bridge, mobile/AAR e REST conforme
  a revisão anterior de `spec.md`.
- [x] Remover fallback de `DefaultConfig()`/`config.Load()` conforme C-003.
- [x] Executar testes técnicos de ausência, vazio, inválido e ambiente válido;
  validação formal permanece pendente.
- [ ] Implementar testes unitários, integração, Android, multiprocesso e físico.
- [ ] Atualizar documentação e GoDoc/KDoc.

## Validação e encerramento

- [ ] Executar quality gates Go e Android com evidências novas.
- [ ] Executar scripted e físico separadamente.
- [ ] Registrar revisão da implementação e validation.md.
- [ ] Obter aprovação formal de implementação e validação.
- [ ] Atualizar specs/system/.
- [ ] Preparar commit rastreável e arquivar somente após aprovação.

## Estado atual

## Estado da execução

`IMPLEMENTADA — TESTE HUMANO PENDENTE`

Aditivo 071 implementado para teste humano em 2026-09-30: o BAT já existente
foi confrontado com a SPEC, recebeu validação de formato COM e teste de
regressão para valor inválido. Testes Go e integração passaram; gate Android
continua bloqueado por `local.properties` preexistente. Sem teste físico.

A implementação Go e os testes técnicos disponíveis foram executados. O JDK
está definido em
`C:\Desenvolvimento\jdk-17.0.11` e o Maven em
`C:\Desenvolvimento\apache-maven-3.8.8`; a alegação histórica de ausência de
Java não é mais um bloqueio válido. Os gates Android ainda precisam de
evidência própria nesta Change.
