# Revisão da SPEC — revisão 2

Resultado: `SPEC_APROVADA` para implementação, sem aprovação final da Change.

Revisados proposal.md, spec.md, DESIGN.md, tasks.md, ADR-001, workflow e padrão compartilhado de plugins. A autorização do usuário inclui corrigir as pendências e seguir autonomamente até teste manual.

| Achado | Severidade anterior | Evidência de correção e conclusão |
| --- | --- | --- |
| REV-001 | Alta | DESIGN define IComponentHostContext, ICardWithdrawalComponent, sessão/View/cancel, resultados e API 1.3 aditiva. Encerrado. |
| REV-002 | Alta | Ordem definida: cancelar/fechar, emitir uma vez, restaurar chamador. Detach/logout invalidam callback; cleanup falho bloqueia sessão; recriação não reenvia operação. Encerrado. |
| REV-003 | Alta | Capacidade, serviço, identidade, staging e validação definidos. Identidade antiga excluída do menu antes da instanciação; duplicidade de componente falha fechada. Encerrado. |
| REV-004 | Alta | Consumidor é plugin-negocio, com ação explícita e resultado local. Aceites 6–8 e design definem cenários sintéticos e teste manual, sem iniciar hardware automaticamente. Encerrado. |

Objetivo, escopo, exclusões, dados sensíveis, compatibilidade, falhas e critérios de aceite estão definidos. ADR-001 documenta a mudança de categoria e a compatibilidade do evento de autenticação. Nenhuma ressalva material impede implementação. Revisão independente de segurança e homologação física permanecem gates posteriores; não são declaradas concluídas.
