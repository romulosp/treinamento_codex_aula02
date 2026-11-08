# Revisão de SPEC

Decisão: `SPEC_APROVADA`.

Revisados proposta, SPEC, design e tarefas contra workflow e contratos anteriores. O usuário autorizou mensagem de sucesso e reinício após pagamento; isso substitui a emissão manual, sem alterar a condição APPROVED/cleanup ou criar autorização financeira.

- REV-001 — Importante, resolvido: sucesso local pode ser confundido com autorização real. Requisitos 2/3 mantêm guard e aviso explícito demonstrativo.
- REV-002 — Importante, resolvido: reset poderia apagar vendidos ou afetar atendimento posterior. Requisitos 4/5 preservam estoque por sessão e condicionam reset ao ID concluído, com timer vinculado à composição.
- REV-003 — Importante, resolvido: “inicial” poderia representar logout/menu. Escopo define seleção de filmes do mesmo plugin, sem alterar autenticação/Host.
- REV-004 — Melhoria: tempo não especificado pelo usuário. Adotados 3 segundos para permitir leitura antes da continuação automática, sem botão adicional.

Sem ressalva material para implementação delimitada. Aprovação da SPEC não aprova implementação ou encerramento.

REV-005 — Importante, resolvido antes da correção: retorno duplicado após novo atendimento poderia coincidir com outro pagamento ativo. SPEC 2 e DESIGN exigem capturar o ID original na rota e validar no owner interno, sem alterar contratos compartilhados. Teste dedicado deve comprovar que ID antigo não conclui o novo pedido. Mantida SPEC_APROVADA.
