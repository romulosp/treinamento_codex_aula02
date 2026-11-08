# Revisão de SPEC

Decisão: `SPEC_APROVADA`.

Revisados proposta, SPEC, design e tarefas contra workflow, arquitetura de plugins e contratos 10020/10021/10022. A instrução atual do usuário substitui a delimitação anterior de manter cadastro: dados pertencem à configuração do cinema e o usuário não escolhe horário.

| ID | Severidade | Evidência / tratamento |
| --- | --- | --- |
| REV-001 | Importante, resolvido | Primeira interpretação permitiria escolher entre horários. SPEC 1/2/3 exige uma sessão configurada por filme, com sala/horário/preço somente leitura e nenhum formulário. |
| REV-002 | Importante, resolvido | Trocar filme poderia apagar vendas ou confundir ocupados com ingressos emitidos. SPEC 5 separa ocupados de sold, conserva vendas por ID de sessão e proíbe recibos para ocupados configurados. |
| REV-003 | Importante, resolvido | Alterar filme durante pedido poderia alterar valor enviado ao cartão. SPEC 6/7 bloqueia owner/UI e preserva pedido/payload/callbacks existentes. |
| REV-004 | Melhoria | Catálogo local é demonstrativo e não representa programação real ou backend. Proposta e tabela identificam essa limitação; persistência/rede não foram solicitadas. |

Sem ressalva material para implementar o fluxo autorizado. Critérios cobrem configuração automática, ausência de escolha de horário, estoque, valor imutável e integração sintética. Aprovação da SPEC não aprova implementação nem encerramento.
