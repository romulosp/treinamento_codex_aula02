# Revisão de SPEC

Decisão: `SPEC_APROVADA`.

Revisados proposal/spec/DESIGN/tasks e ADR contra workflow, memória 10011 e contratos 10019/10021/10024. Usuário confirmou explicitamente CAPTURED como conclusão apenas demonstrativa no plugin de ingressos.

- REV-001 — Importante, resolvido: não converter captura em aprovação. Status original preservado; consumidor aceita CAPTURED com aviso e somente para simulação.
- REV-002 — Importante, resolvido: retorno precisa acontecer depois de cleanup e sem repetir close/callback. Decisão fica no owner do componente, com terminal/closedState único e guards de detach/cancelamento/falha.
- REV-003 — Importante, resolvido: personalização por ID hardcoded e campo novo no pedido quebrariam isolamento/ABI. ADR usa facetas 1.5 e opções separadas; legado preservado, sem fallback que ignore opção.
- REV-004 — Importante, resolvido: testes anteriores usavam somente componente sintético que devolvia diretamente resultado, sem cobrir o botão real VOLTAR. SPEC exige fábrica real do componente com repositório injetado sintético e captura automática/legado manual.

Sem pendência material para implementação delimitada. Não representa aprovação da implementação, hardware ou encerramento.
