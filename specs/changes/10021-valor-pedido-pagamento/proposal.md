# Proposta — valor do pedido no pagamento

## Status
`SPEC_APROVADA` — revisão r2.

Corrigir o fluxo relatado pelo usuário: ingressos deve enviar o valor do pedido
ao componente de cartão, que o mostra sem permitir edição. Crédito/débito e
parcelas continuam selecionáveis. Evolução independente da infraestrutura
solicitada pelo usuário, mantendo os contratos legados da 10019. O valor
recebido é a base; tarifa e total seguem as regras de homologação da 10004.
