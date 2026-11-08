# SPEC — valor do pedido no pagamento

## Status
`SPEC_APROVADA` — revisão r2 em reviews/2026-11-07-spec-review-r2.md.

## Requisitos e aceite
1. API compartilhada 1.4 aditiva: CardPaymentRequest(orderId, amountCents),
   com ID opaco não vazio limitado a 128 caracteres e valor Long positivo até N12.
   ICardPaymentHostContext e ICardPaymentComponent estendem os contratos 1.3
   sem alterar assinaturas antigas; o novo caminho não faz fallback editável.
2. Ingressos cria ID imutável por pedido e envia o valor base exato em centavos
   congelado com a sessão. Sem conversão por ponto flutuante.
3. Host repassa a solicitação sem alteração; componente incompatível retorna
   UNAVAILABLE sem abrir hardware. Preservar cleanup, lifecycle e callbacks.
4. Componente mostra valor base recebido como somente leitura; eventos de edição
   também são ignorados pelo owner. Aplicar a mesma regra da SPEC 10004:
   crédito 5,34% mais 0,80 ponto percentual por parcela adicional, de 1 a 12;
   débito tarifa fixa de R$ 1,00 e uma parcela. Tarifa arredondada HALF_UP
   em centavos; total = base + tarifa. Recalcular ao mudar modalidade/parcelas,
   sem alterar o valor base.
5. Ao avançar, congelar resumo de modalidade, base, tarifa, total e parcelas;
   confirmação, GCX e continuação usam o mesmo total. Valor base ou total que
   exceda os limites aplicáveis bloqueia avanço, sem truncamento.
6. Caminho legado sem pedido preserva cálculo e edição anteriores. API 1.3,
   autenticação 1.2 e consumidores antigos continuam compatíveis.
7. Testar validação, imutabilidade na lógica e UI, modalidade/parcelas, valor
   base e total calculado na captura e propagação do APK pelo host. Registrar build,
   lint, cobertura e testes sintéticos sem executar compra física automaticamente.

Revisão r2 corrige a hipótese anterior de tarifa zero: o usuário confirmou
que a tarifa deve ser calculada conforme a 10004. Para base R$ 10,00:
crédito 1x tarifa R$ 0,53/total R$ 10,53; crédito 7x tarifa R$ 1,01/total
R$ 11,01; débito tarifa R$ 1,00/total R$ 11,00. O contrato de retorno ainda
não transmite valor final ao plugin de ingressos; o ingresso exibido contém
o preço base, e não comprova o total efetivamente pago.

Não inclui autorizador financeiro, mudança Go/Bridge ou chaves. Aprovação local
continua homologação.
