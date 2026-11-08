# Proposta — layout do plugin de ingressos

## Status
`SPEC_APROVADA` — revisão vigente em [reviews/2026-11-08-spec-review-r4.md](reviews/2026-11-08-spec-review-r4.md).

## Objetivo e autorização
Redesenhar somente a interface do plugin de ingressos com base no documento de UI/UX fornecido em 08/11/2026. O usuário delimitou expressamente: “Apenas o layout, preservando as funções atuais”.

## Escopo
Fidelidade à imagem de protótipo enviada após rejeição visual da primeira entrega: carrossel de cards largos no topo, horários abaixo, mapa com desenhos de poltronas e resumo branco lateral. Preservar cadastro manual por diálogo, 12 lugares existentes, pedido com um assento, regras de pagamento e emissão demonstrativa das Changes 10020 e 10021. Cabeçalho e menu pertencem ao host. Tocar referência de cartaz apenas preenche o cadastro existente, sem cadastrar ou pagar automaticamente.

## Limites
Não adicionar catálogo, múltiplas sessões ou assentos por pedido, meia-entrada, combos, PIX, reserva temporizada, servidor, impressão, nova API ou garantia de autorização financeira. Os elementos do anexo que dependem dessas funções não serão apresentados como controles operacionais. Sem alterações de SDK, dependências, host, componente financeiro ou Go/Bridge.

## Fonte
Anexo `Especificação Técnica de UI/UX: Plugin de Venda de Ingressos (IPluginNegocioApp)` e delimitação posterior do usuário; contrato funcional em [10020](../10020-plugin-venda-ingressos/spec.md) e [10021](../10021-valor-pedido-pagamento/spec.md).
