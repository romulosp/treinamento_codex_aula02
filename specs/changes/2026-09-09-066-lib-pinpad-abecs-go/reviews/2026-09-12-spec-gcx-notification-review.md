# Revisão da SPEC — interface e notificações GCX

Autor: Rômulo Penha

**Data:** 2026-09-12

**Skill aplicada:** `spec-review`

**Escopo:** `proposal.md`, `spec.md`, `spec-command-gcx.md`, `DESIGN.md`,
`tasks.md`, `implementation-plan.md`, rastro físico das 22:50 e manual ABECS
v2.12, seções 2.2.2, 2.2.2.2, 2.3.3, 3.7.1 e 6.8.6.

## Achados tratados

### REV-GCX-005 — Notificação NTM era aceita no texto, mas não possuía fluxo verificável

- **Severidade:** bloqueante.
- **Evidência:** depois da seleção da aplicação, o rastro contém ACK, um chunk
  redigido de 1 byte e outro de 44 bytes. Esse total corresponde ao frame
  `NTM000032` definido pelo manual: 41 bytes de payload e 4 de enlace. O serviço
  encerrava a leitura no primeiro frame e tentava interpretar `NTM_MSG` como TLV.
- **Impacto:** a seleção funcionava no pinpad, mas a aplicação retornava
  `invalid pinpad response` com status vazio antes da resposta final GCX.
- **Tratamento:** RF-GCX-000.3 exige consumir zero ou mais `NTM`, sem ACK, até a
  resposta final do comando, e CA-GCX-012 fixa a regressão observada.

### REV-GCX-006 — As opções GCX não eram escolhidas no utilitário local

- **Severidade:** importante.
- **Evidência:** a opção 11 chamava `PurchaseGCX(..., false)` sem consultar o
  operador, embora `SPE_GCXOPT` defina a interface no primeiro bit e a
  visibilidade do valor no segundo.
- **Impacto:** o utilitário sempre habilitava apenas chip/tarja e sempre mostrava
  o valor.
- **Tratamento:** RF-GCX-000.2 define as duas decisões compatíveis com o manual,
  suas quatro combinações e a rejeição de entrada inválida em CA-GCX-011.

### REV-GCX-007 — O prazo do GCX era consumido pela digitação no CLI

- **Severidade:** bloqueante.
- **Evidência:** no rastro físico das 23:03, o contexto foi criado às 23:02:21,
  antes das cinco entradas da opção 11. O GCX foi enviado às 23:03:19, recebeu
  ACK e expirou 1,76 segundo depois, sem qualquer frame de resposta.
- **Impacto:** o operador tinha quase nenhum tempo para apresentar o cartão,
  embora o CLI pretendesse fornecer 60 segundos ao fluxo.
- **Tratamento:** RF-GCX-000.2 inicia o prazo depois das entradas;
  RF-GCX-000.4 preserva o contexto do consumidor sem o reduzir pelo timeout
  genérico; CA-GCX-013 fixa a regressão na fronteira serial.

## Verificação final

- A proposta e o escopo reduzido do GCX permanecem coerentes.
- O comportamento novo deriva diretamente das notificações de comandos
  blocantes e das opções `SPE_GCXOPT` do manual.
- A resposta e as notificações GCX continuam integralmente redigidas no rastro.
- Os critérios automatizados permitem reproduzir o frame físico sem dados do
  cartão.

## Conclusão

Os requisitos agora definem de forma verificável a escolha da interface, o
tratamento da resposta intermediária e o prazo efetivo observado pelo pinpad.

**Resultado:** `SPEC_APROVADA`

**Próxima fase autorizada:** implementar RF-GCX-000.2/RF-GCX-000.3/
RF-GCX-000.4 e executar CA-GCX-011 a CA-GCX-013.
