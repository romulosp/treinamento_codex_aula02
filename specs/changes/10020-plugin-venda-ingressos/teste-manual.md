# Teste manual — cinema demonstrativo

1. No emulador, conclua o login existente e escolha **Venda de ingressos** no menu inicial. O host deve construir a entrada a partir do caminho declarado pelo APK.
2. Cadastre filme, sala, horário e preço, por exemplo `12,50`; confira a sessão.
3. Escolha A1, crie o pedido e confira os dados e o valor.
4. Selecione **Abrir pagamento com cartão**. Pela evolução 10021, o host envia o valor base do pedido e o componente o exibe bloqueado. Selecione crédito/parcelas ou débito; a tarifa e o total são calculados conforme a modalidade. A abertura prepara o pinpad e depende da infraestrutura de homologação.
5. Depois de resultado local APPROVED e cleanup confirmado, selecione **Emitir ingresso demonstrativo**. Confira **SIMULAÇÃO — SEM VALIDADE** e A1 vendido. Outros resultados não emitem ingresso.
6. Tente cadastrar preço zero ou inválido antes de criar uma sessão; a interface deve explicar a rejeição.

O resultado local do cartão não comprova autorização financeira. O ingresso não tem validade. Os dados são temporários e podem desaparecer ao sair ou reiniciar. A demonstração usa apenas uma sessão e um terminal, sem estoque compartilhado nem reservas persistentes.

Preparação realizada: emulador `Medium_Tablet`, host aberto na tela de login, arquivos privados de homologação provisionados e Hello/Ping do Bridge confirmados. Não houve leitura física de cartão nem verificação de chave. Use suas credenciais habituais.

O perfil DES deste ambiente foi atualizado explicitamente para
`10.0.2.2:39100`, endereço do Windows no Android Emulator. O binding real no
host passou no teste de Ping lendo esse perfil privado. Essa configuração
evita depender de adb reverse neste emulador. Após atualizar o perfil, abrir
nova jornada; uma tela que já falhou não recarrega configuração automaticamente.
