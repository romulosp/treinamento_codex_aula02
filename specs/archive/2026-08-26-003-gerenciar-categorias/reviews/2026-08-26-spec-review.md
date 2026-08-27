# Revisão da SPEC: 003-gerenciar-categorias

## Data e escopo

- Data: 2026-08-26
- Artefatos revisados: `proposal.md`, `spec.md`, `DESIGN.md` e `tasks.md`.
- Referências verificadas: convenções REST, arquitetura backend Java e estratégia de testes compartilhadas.

## Achados

### REV-001 — Crítico — Contrato de atualização sem identificador autoritativo

- Evidência: o requisito funcional 7 estabelece `PUT /categorias/add` com corpo contendo somente `nome_categoria` e `quantidade_produtos`; a pendência bloqueante 1 confirma que não há identificador da categoria-alvo.
- Impacto: não é possível determinar qual categoria deve ser atualizada, nem criar um cenário de integração determinístico para o requisito funcional 8 e o respectivo critério de aceite.
- Recomendação: definir um único contrato de atualização com o identificador autoritativo, na rota ou no corpo, e atualizar os requisitos, cenários, DESIGN e tarefas de forma consistente.

### REV-002 — Crítico — Contrato de exclusão ambíguo quanto ao identificador e corpo

- Evidência: o requisito funcional 9 define `DELETE /categorias/deletar/{id_categoria}`, enquanto a pendência bloqueante 2 registra conflito entre o identificador da rota e o de um corpo de exemplo, sem definir se o corpo existe ou é obrigatório.
- Impacto: a operação pode excluir categorias diferentes para a mesma requisição e seu contrato HTTP não é implementável ou testável de forma inequívoca.
- Recomendação: definir se a exclusão aceita corpo e estabelecer explicitamente o identificador autoritativo; adequar requisitos, cenários, DESIGN e tarefas à decisão.

### REV-003 — Crítico — Respostas para entradas inválidas e recurso inexistente não definidas

- Evidência: a pendência bloqueante 3 declara que não foram definidos status HTTP nem corpos de resposta para categoria inexistente ou entrada inválida.
- Impacto: os contratos públicos não estão completos, contrariando as convenções REST de códigos coerentes e respostas de erro consistentes; os critérios de aceite não podem validar esses comportamentos.
- Recomendação: especificar para cada operação aplicável os códigos HTTP e contratos de erro para identificador inexistente e entrada inválida, incluindo cenários verificáveis.

## Conclusão

`REPROVADA`

Os achados críticos impedem a aprovação da SPEC e, consequentemente, qualquer implementação. A mudança deve retornar à fase de especificação para resolver integralmente `REV-001`, `REV-002` e `REV-003`.