# Revisão da SPEC — revisão 3

Resultado: `SPEC_APROVADA` para correção da seleção de revisão.

Evidência: execução final encontrou 27 testes de host, com uma falha no teste de descoberta por duas revisões verificadas. A alteração do rótulo gerou novo digest do componente, preservando o antigo como previsto. A regra anterior de exigir um único APK no repositório bloqueava atualizações normais.

REV-005 — alta, encerrado pelo contrato corrigido: DESIGN e ADR-001 especificam seleção de um único digest recém-promovido e persistência atômica após lifecycle bem-sucedido. Cada boot revalida a revisão selecionada. Histórico não é apagado, múltiplos digests no mesmo staging são rejeitados e não há escolha por mtime. A instrumentação deve confirmar que a seleção corresponde ao APK embutido atual mesmo com revisões antigas presentes.

Rechecados os achados r2, sem alterações de API, dados sensíveis ou negócio. Esta revisão aprova somente o contrato para implementação; não representa validação ou aprovação final da Change.
