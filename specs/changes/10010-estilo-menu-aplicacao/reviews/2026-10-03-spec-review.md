# Revisão de SPEC é 2026-10-03

**Decisão:** SPEC_APROVADA
**Objeto:** proposal.md, spec.md, DESIGN.md e tasks.md consolidados da Change 10010.

| Achado | Severidade | Evidência e impacto | Resolução |
| --- | --- | --- | --- |
| REV-001 | Importante | Rascunho mapeava 1200/1024 para 1000/860 sem evidência de densidade; mudava aceite. | SPEC restaura 1200/1024 dp e exige registro de pixels/densidade/área útil. |
| REV-002 | Importante | AuthenticationHeader é compartilhado por CoreShell; mudança irrestrita altera outras telas. | DESIGN exige variante exclusiva do menu e default preservado. |
| REV-003 | Importante | Código mostra TERMINAL sem id e não exibe lotérica; mockup tem valores pessoais fictícios para esta integração. | Contrato preserva fontes atuais e proíbe inventar dados/estender SharedApi. |
| REV-004 | Importante | Rascunho equiparava indicação Material ao focus ring e usava apenas transition scale. | SPEC exige ring explícito e supressão de movimento aplicável é animação utilizada. |
| REV-005 | Importante | Grade plana poderia perder agrupadores, vazio e erro fatal existentes. | Contrato e DESIGN preservam recursão, ordem e estados com testes. |

Todos os achados acima foram resolvidos nos documentos atuais. Sem pendência material para planejamento técnico e implementação. Revisão documental, não execução de testes Android nem aprovação final da mudança.
