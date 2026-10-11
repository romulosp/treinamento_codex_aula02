# Espera sem botão de cancelamento — REV-016

Data: 2026-10-11. SPEC_APROVADA pela solicitação explícita de remover a
possibilidade de cancelar na tela Aguarde, processando da preparação do plugin.

Etapa 0 enquanto busy: somente instrução/progresso/spinner, sem CANCELAR,
CANCELANDO ou SAIR. Supersede a ação visual de cancelamento da REV-015.
Falha de preparação continua oferecendo SAIR. Confirmação/cancelamento das
etapas transacionais 2..4 permanece no contrato existente.

Cleanup automático por detach/dispose continua necessário para não deixar sessão
ou operação órfã; é lifecycle interno, não ação oferecida na tela de espera.
Não alterar infraestrutura do host ou outros plugins. Verificar ausência dos
botões na espera, transição para formulário e cancelamento transacional existente.
