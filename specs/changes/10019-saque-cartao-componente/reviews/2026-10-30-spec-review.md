# Revisão inicial da SPEC — 2026-10-30

## Resultado
`REPROVADA` — o rascunho ainda não constitui contrato implementável.

- **REV-001 — alta.** O design não define assinaturas e meios de solicitação, resultado e erro. Impacto: host e plugins não podem implementar interoperabilidade verificável. Ação: detalhar API compartilhada, versão e semântica dos resultados.
- **REV-002 — alta.** Lifecycle não determina proprietário da View, ordem de limpeza e devolução ao chamador, nem semântica em detach, logout e concorrência. Impacto: risco de sessão de pinpad órfã ou callback em chamador destruído. Ação: fechar transições e testes.
- **REV-003 — alta.** Descoberta de componente e migração de APK antigo não estão especificadas; `BusinessPluginManager` aceita apenas `business-menu`. Impacto: componente não encontrado ou saque antigo presente no menu. Ação: definir descoberta autenticada, identidade, substituição e compatibilidade.
- **REV-004 — alta.** O consumidor de teste e critérios detalhados para validação de integração não estão definidos. Impacto: chamada real entre plugins não é reproduzível. Ação: definir fixture e cenários sem operar hardware automaticamente.

A revisão não implica aprovação ou alteração do contrato vigente da 10004.

## Rechecagem para implementação e teste humano

Em 2026-10-30, a leitura da proposta, SPEC, design, tarefas, validação e regras compartilhadas confirma que os quatro achados permanecem abertos. A definição da capacidade e do consumidor demonstrativo no design estabelece a direção, mas não resolve as assinaturas, a ordem de cleanup, a seleção de versões ou o cenário reproduzível entre APKs.

Evidências adicionais: `PluginHostContext` só publica eventos e `IPluginRouter` só navega; `BusinessPluginManager` consulta `BusinessRequest` e instancia `IPluginNegocioApp`; `SaqueJourney.dispose()` inicia cleanup assíncrono. Esses contratos atuais não definem a chamada de componente nem garantem que o retorno ocorra após o encerramento. Resultado mantido: `REPROVADA`.

