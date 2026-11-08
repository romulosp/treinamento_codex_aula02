# Proposta: plugin de venda de ingressos

## Status
`SPEC_APROVADA` — revisão r4 em reviews/2026-11-07-spec-review-r4.md.

## Objetivo
Testar a ideia de venda de ingressos em uma jornada de cinema com assentos, a partir de um plugin independente descoberto pelo host. O usuário confirmou pagamento simulado e ingresso sem validade real para este primeiro teste.

## Escopo inicial
Cadastro demonstrativo de filme e sessão, preço e disponibilidade, seleção de assento, pedido, simulação explícita de pagamento, emissão demonstrativa e consulta local. O plugin declara `Principal > Venda de ingressos`; o host organiza o menu. Dados são temporários e limitados a um terminal para o teste da ideia.

## Dependências e limites
A 10004 e a 10019 são referências e o componente existente é invocado explicitamente pelo host. A API atual não recebe o preço nem fornece autorização financeira. Esta Change não modifica a 10004, não executa operação física automaticamente, não emite ingresso válido e não aprova uma evolução financeira. Shows permanecem fora deste primeiro incremento.
