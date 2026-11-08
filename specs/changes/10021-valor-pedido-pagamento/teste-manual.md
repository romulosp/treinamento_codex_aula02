# Teste manual

1. Entre no host, abra Venda de ingressos e crie pedido, por exemplo R$ 30,00.
2. Abra pagamento com cartão; após preparar o terminal, o campo Valor do pedido
   deve mostrar R$ 30,00 e não aceitar digitação/colagem.
3. Selecione crédito em 1x: tarifa R$ 1,60 e total R$ 31,60. Para 7x,
   tarifa R$ 3,04 e total R$ 33,04. Ao selecionar débito, fica uma parcela,
   tarifa R$ 1,00 e total R$ 31,00.
4. Confira o resumo e o valor total exibido no pinpad antes de prosseguir manualmente.

A confirmação/captura usa base do pedido mais tarifa calculada pelo componente.
O caminho sem pedido de Operações mantém comportamento legado. Homologação não comprova
autorização financeira real. A sessão anterior em memória foi encerrada para
instalar esta versão; crie novo pedido para testar.
