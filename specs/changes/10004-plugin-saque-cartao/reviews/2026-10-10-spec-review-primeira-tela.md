# Revisão da primeira tela — REV-007

Data: 2026-10-10. Decisão: `SPEC_APROVADA`, com base na solicitação explícita
do solicitante sobre a captura da primeira tela. Complementa REV-005/006.

Remover a faixa do host com “Voltar ao menu”; o host mantém retorno pelo
botão/gesto Voltar do Android. A primeira tela oferece “SAIR” vermelho abaixo
de “AVANÇAR”, enviando o evento de retorno ao mesmo menu, sem logout.

Modalidade e valor da compra são editáveis mesmo sem perfil externo.
O cálculo é local; comandos físicos continuam bloqueados sem perfil válido.
A modalidade congelada seleciona o filtro de aplicação crédito/débito do
perfil na integração, sem inventar códigos de domínio ABECS.

Taxa percentual: `5,34 + 0,80 × (parcelas - 1)`, de 1 a 12 parcelas:
1 = 5,34%; 2 = 6,14%; 12 = 14,14%. Débito mantém uma parcela.
Tarifa = compra × taxa / 100, HALF_UP em centavos; total a pagar = compra
+ tarifa. A regra substitui a taxa do perfil externo; `feePercent` legado
é ignorado. O limite externo permanece obrigatório para operação física.
Sem perfil, calcular até o limite de representação N12, incluindo a tarifa.

Aceite: testes da progressão completa de taxas, limites/arredondamento,
recalculo ao editar e carregar perfil, seleção enviada à captura, campos
habilitados sem perfil, Sair/Voltar e ausência da faixa. Não valida hardware.
