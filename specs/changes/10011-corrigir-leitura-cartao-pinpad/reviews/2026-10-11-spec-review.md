# Revisão técnica da SPEC — REV-001

Data de referência: 2026-10-11. Revisor: Codex, revisão documental local, sem declaração de revisão humana independente. Artefatos: proposal, spec, DESIGN, tasks, validation, diagnóstico e ADR-001. Regras: workflow/evidências, arquitetura de plugins Android, testes/segurança Go e skill base Android.

## Achados e resolução

| Achado | Severidade | Evidência/impacto | Resolução no contrato |
| --- | --- | --- | --- |
| REV-001-01 | Bloqueante, resolvido | `SaqueJourney.begin` roteia 03/05 para GOX, mas manual/constantes aceitam 03/06. CTLS EMV não continua e CTLS simulada recebe comando inelegível. | RF-002/005 distinguem os quatro modos e AC-03 exige ordem e ausência de comandos indevidos. A distinção 05/06 é explícita, sem afirmar que todo contactless é EMV. |
| REV-001-02 | Bloqueante, resolvido | Pedido novo exige GPN após GTK; GPN com PAN vazio depende de GTK ainda não ter sido chamado. Apenas mover chamadas produziria regressão. | ADR-001 e RF-005 definem PAN mínimo privado no Go e API de sessão aditiva, bloqueio por PAN inválido, isolamento, single use e descarte. AC-06/08 comprovam fronteiras. |
| REV-001-03 | Importante, resolvido | Adapter configura saque 01h e fixa método/índice/WK, enquanto solicitação exige compra e arquivo externo integral. | RF-001 estabelece compra 00h em ambos os comandos; RF-004 inventaria todos os três campos, normaliza WK vazia e elimina hardcodes sem confundir PIN e DATA GTK. |
| REV-001-04 | Importante, resolvido | AIDLIST pode sobrepor APPTYPE; abreviar ou deduplicar AIDs por prefixo perde candidatas da modalidade. | RF-001/002 omitem AIDLIST na jornada, preservam identidade e mantêm seleção no kernel; AC-01/02/04 incluem filtros e AIDs base/estendido. |
| REV-001-05 | Importante, resolvido | Log redigido e código `PINPAD_ERROR:command` não permitem identificar causa exata nem associar screenshot ao trecho serial. | Diagnóstico distingue defeitos confirmados da causa física não comprovada. RF-006 e AC-08 exigem metadados sanitizados, erro principal/cleanup distintos, sem abertura de frames. |
| REV-001-06 | Importante, resolvido | Liberar socket/ownership não comprova encerramento no pinpad; cancelamento incondicional pode produzir falso aviso de cleanup. | RF-006 exige confirmação real, cancelamento somente quando aplicável e recuperação sem replay da compra; AC-07/09 e roteiro físico verificam próxima operação. |

## Verificações do contrato

Objetivo, escopo e exclusões estão explícitos. Modalidade da tela 1, compra, três interfaces, AIDs e fallback são rastreáveis. O desenho preserva topologia e protocolo Bridge; a alteração de estado sensível tem ADR próprio. Fonte externa tem formato, campos obrigatórios, limites e tratamento de erros definidos. Não autoriza instalar chaves, presumir provisionamento, usar PRD ou aprovar financeiramente uma captura de PIN.

A opção GPN para 05 está delimitada ao perfil de homologação sem EMV solicitado; não é política de produção. A seleção de aplicações pelo kernel exige prova física. Se o dispositivo não fornecer PAN utilizável no ramo sem EMV, o contrato exige falha explícita e não uma adaptação silenciosa de ordem ou segurança.

Os critérios AC-01..AC-09 têm cenários de sucesso/falha e plano de evidências. Testes baseline Go passaram com cache; teste documental passou. Implementação, KDoc alterado, AAR regenerado, cobertura, diagnóstico novo e hardware não foram validados nesta etapa. Essas pendências são gates da implementação, não contradições do contrato.

## Decisão

**SPEC_APROVADA**. Achados de contrato resolvidos documentalmente, sem ressalva material que impeça planejamento da implementação. `proposal.md` e `spec.md` recebem este status, e ADR-001 integra o contrato aprovado.

Esta revisão não atribui aprovação ao solicitante, não aprova implementação inexistente e não autoriza encerramento/archive. A etapa seguinte é `implementation-plan.md` e implementação conforme tasks; aprovação final somente após revisão e validação física exigidas pelo workflow.
