# Revisão do Pagamento de Cartão e Usabilidade — REV-008

Data: 2026-10-10. Decisão: `SPEC_APROVADA`, com base nas especificações refinadas do solicitante. Complementa REV-005/006/007.

1. **Renomeação da Marca e Domínio:**
   - Renomear todos os textos de interface, documentação, especificações, logs e descritores do termo "Saque / Saque com Cartão" para **"PAGAMENTO DE CARTÃO"** ("Pagamento com Cartão").
   - O item de menu no host passa a ser `Principal > Outros Serviços > Pagamento com Cartão`.

2. **Seletor de Parcelas em Combobox:**
   - O campo "PARCELAMENTO" passa a ser um componente Combobox (`ExposedDropdownMenuBox` / `DropdownMenu`).
   - Modalidade **Crédito**: Opções de parcelas de 1x até 12x.
   - Modalidade **Débito**: Combobox fixo/desabilitado em "1x (À vista)".

3. **Valor da Compra Monetário + Teclado Numérico:**
   - O campo "VALOR DA COMPRA" opera em modo monetário (ex: digitação de dígitos formata dinamicamente em centavos: `1` -> `R$ 0,01`, `100` -> `R$ 1,00`, `10534` -> `R$ 105,34`).
   - O campo dispara o teclado numérico virtual do Android (`KeyboardOptions(keyboardType = KeyboardType.Number)`).

4. **Taxa Específica do Débito (R$ 1,00 Fixo):**
   - Para **Débito**: A tarifa aplicada é um valor fixo de **R$ 1,00** (uma taxa específica de 1 real), independente do valor da compra.
   - Para **Crédito**: Mantém a fórmula percentual `5,34% + 0,80% × (parcelas - 1)`.

5. **Liberação do Botão AVANÇAR e Perfil Sintético Padrão:**
   - O botão `AVANÇAR` é habilitado diretamente quando houver um resumo de cálculo válido (`summary != null`) e o sistema não estiver ocupado (`!busy`).
   - Se nenhum arquivo de perfil externo for carregado do armazenamento, a aplicação utiliza automaticamente um perfil de homologação DES sintético válido padrão, permitindo avançar e testar o fluxo no emulador sem necessidade de carga prévia de arquivo via ADB.

6. **Animação de Transição Enfatizada:**
   - As transições da tela, das etapas e dos painéis (inclusive botão SAIR e avanço) utilizam animações marcantes com combinação de deslocamento, desvanecimento e escala (`slideInVertically` / `slideInHorizontally` + `fadeIn` + `scaleIn` e saídas correspondentes), tornando os movimentos claramente percebidos pelo operador.

Aceite: Testes unitários do cálculo do Débito (R$ 1,00), testes da UI Compose (combobox, entrada monetária, teclado numérico, habilitação do botão AVANÇAR com perfil padrão e transições animadas), e 20 testes do host integrados.
