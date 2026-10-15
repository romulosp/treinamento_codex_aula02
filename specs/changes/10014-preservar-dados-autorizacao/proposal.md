# Proposta — 10014

Status: `SPEC_APROVADA` pela [REV-001](reviews/2026-10-14-spec-review.md).

O usuário confirmou que GTK é obrigatório nesta aplicação: o objeto da transação
precisa conter as informações para o futuro autorizador. Hoje PaymentTracks
executa GTK e imediatamente sobrescreve sua resposta, perdendo os dados.
Corrigir a retenção privada por operação, sem remover GTK nem contornar GTK011.

Escopo: contexto privado Go, consulta tipada interna para futura integração,
limpeza, testes, evidências, memória e regeneração dos artefatos dependentes.
Fora de escopo: implementar o autorizador, enviar transações, carregar chaves,
trocar parâmetros de criptografia, novas telas ou logar trilhas/PAN.
