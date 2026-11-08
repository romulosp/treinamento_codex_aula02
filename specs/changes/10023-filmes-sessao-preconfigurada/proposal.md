# Proposta — filme com sessão pré-configurada do cinema

## Status
`SPEC_APROVADA` — [revisão](reviews/2026-11-08-spec-review.md).

## Objetivo e autorização
O usuário rejeitou o formulário de Configurar sessão e esclareceu que horário e demais dados vêm do que já está configurado para aquele cinema. Confirmou expressamente: “o usuario não escolhe horario”. O operador deve selecionar apenas filme e assento.

## Escopo
Substituir o cadastro manual por catálogo demonstrativo local do cinema. Cada filme possui uma sessão programada com sala, horário, preço e lugares ocupados. Selecionar filme carrega imediatamente esses dados, somente para conferência. Preservar o visual da Change 10022, pedido com um assento e integração de pagamento da 10021.

## Limites
Sem formulário, seleção/edição de horário, servidor, CRUD de catálogo, seleção de cinema, persistência, múltiplos assentos, meia-entrada, combos, PIX, novas imagens, autorização financeira ou execução física automática. A origem local demonstrativa pode ser substituída em Change futura com contrato próprio.

Esta Change substitui o cadastro e a restrição de troca após vendas da 10020/10022: vendas permanecem por sessão em memória e não são apagadas ao selecionar outro filme. Pedido pendente continua impedindo troca de filme até cancelamento ou emissão.
