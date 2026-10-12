# Confirmação com DEX único seguido de GKY — REV-017

Data: 2026-10-11. SPEC_APROVADA pela solicitação explícita do solicitante de
exibir no pinpad, na etapa de confirmação, um único DEX com as informações e um
único GKY para o evento CANCELA/CONFIRMA.

O contrato da confirmação deixa de ser "DSP032 em quatro páginas + GKY por
página" (REV-010) e passa a: **DEX único** com

1. VALOR TOTAL
2. VALOR COMPRA
3. VALOR TARIFA
4. QUANTIDADE
5. CANCELA/CONFIRMA

seguido de **um único GKY**. As páginas DSP abreviadas são removidas.
Tecla 0x00 = CONFIRMA; 0x0D = CANCELA; demais teclas continuam
CONFIRMATION_ERROR (sem captura).

O texto permanece em português. DEX aceita até 160 bytes em latin1. A separação
de linhas usa `\r` (0x0D) e **todas as linhas são centralizadas** na largura
informada por `SaqueDisplay.columns`; ambos dependem de confirmação no pinpad
físico.

VALOR TOTAL usa `totalCents` (base + tarifa), coerente com o resumo já exibido
no Android; VALOR COMPRA usa `baseCents`; VALOR TARIFA usa `feeCents`;
QUANTIDADE usa `installments` (1..12).

Timeout do GKY permanece `config.timeoutSeconds` do perfil; registrar o
orçamento por fase. Cleanup continua interno e não substitui a causa principal.

Não alterar infraestrutura do host, Bridge, AAR ou outros plugins. Verificar a
ordem DEX→GKY no log serial, CONFIRMA e CANCELA no pinpad físico e a manutenção
do cancelamento transacional das etapas 3..4 existente.
