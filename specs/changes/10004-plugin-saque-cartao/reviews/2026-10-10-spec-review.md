# REV-003 — Jornada física de saque e integração com a 074

**Data:** 2026-10-10  
**Revisor:** Codex  
**Decisão:** `REPROVADA`

Atualização posterior na mesma data: homologação DES decidida e arquivo
inspecionado. A conclusão de necessidade de nova fachada GPN foi corrigida
após consultar o manual: PAN vazio pode ser usado antes de GTK, sob condições
normativas. Ver [reavaliação DES](2026-10-10-spec-review-des.md).

## Base da revisão

Pedido do solicitante: usar a Change 10004, combinar duas sugestões de SVGs,
integrar eventos de hardware ao componente ABECS e subir Bridge Go antes do
Android. Revisados proposal.md, spec.md, DESIGN.md, tasks.md, o workflow,
a SPEC 074 e as implementações Go/mobile e diagnosticopinpad.

A aprovação anterior cobria cartão/senha/conclusão locais, sem leitura física
ou rede. Foi preservada em `references/baseline-2026-09-25/`; não aprova os
novos parâmetros nem promove o stub GCX da 074.

## Achados

| ID | Severidade | Evidência | Impacto e resolução necessária |
| --- | --- | --- | --- |
| REV-003-01 | Bloqueante | 074/spec.md seção 8; `internal/application/service/service.go`, TransactionGCX retorna ErrNotImplemented; `mobile/catalog.go`, PurchaseGCXSummaryJSON não recebe modalidade/AIDs. | RF-003 não é implementável pelo subconjunto existente. Definir contrato completo no Go e aprovar sua relação com a 074; não criar protocolo em Kotlin. |
| REV-003-02 | Bloqueante | `mobile/catalog.go`: GTK devolve só tamanhos; CapturePINMK/DUKPT recebem PAN do chamador. | Encadeamento seguro tarja/PIN não está disponível como operação de sessão. Definir handle/sessão Go para GPN mantendo PAN interno. |
| REV-003-03 | Bloqueante | Anexos pedem sucesso/negada/erro, mas não fornecem autorizador ou perfil; FCXRequest exige opções e ARC condicional. | Sucesso de comando não autoriza saque. Decidir homologação ou integração adquirente, perfil EMV/PIN/AIDs e origem de decisão/ARC. Não preencher valores de teste como parâmetros de produção. |
| REV-003-04 | Importante | Jornada diz cálculo valor × taxa/100; SVGs somam tarifa; taxa 5% e parcelas no débito não têm regra definida. | Confirmar fórmula, origem da taxa, arredondamento, limites, parcelas e prazos por fase antes de aprovar o comportamento. |

## Riscos de integração a validar depois do contrato

O plugin roda em processo host e usa carregamento dinâmico: resolução do AAR,
ABIs e bibliotecas nativas, INTERNET efetiva e cleanup devem ser testados.
Esses pontos não foram comprovados por leitura de código. Logs de hardware
devem preservar redaction da 074. Não há evidência física nesta revisão.

## Encaminhamento

Proposta/SPEC retornam à especificação. A revisão já contém avaliação visual,
fronteiras, fluxo do launcher, estados e critérios de aceite; restam as
decisões acima e a re-revisão. Não iniciar código de produção com os achados
materiais pendentes, conforme
[workflow](../../../shared/process/workflow.md) e
[android-native-engineering](../../../../.agents/skills/android-native-engineering/SKILL.md):
"Código de produção exige SPEC_APROVADA."

Não se trata de aprovação final, validação ou arquivamento. As evidências de
2026-09-25 continuam históricas e não são reaproveitadas como prova da jornada física.
