# Revisão 3 da SPEC — 10017

Skill: spec-review. Resultado: `SPEC_APROVADA`.

Revisados proposta, RF-016..020, DESIGN, tarefas anteriores, ADR-001 e workflow. Pedido do usuário cobre integração do caminho real. Achado REV-3-001: desenho anterior excluía Bridge; resolvido por ADR restrito à observabilidade. REV-3-002: chunks opacos não podem ser expostos por heurística; resolvido por enquadramento limitado, validação CRC, catálogo, checagem de campos e falha fechada. REV-3-003: DETAILED opt-in no executável mas habilitado no BAT de laboratório, com override LEGACY e aviso efetivo. Sem pendências materiais de contrato. Testes devem cobrir descarte, dados sensíveis, chunks, isolamento e execução TCP scripted.