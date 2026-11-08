# Design

SharedApi 1.4 acrescenta interfaces especializadas e request imutável,
preservando os métodos existentes. Host verifica a interface do provedor antes
de criar sessão. Ingressos exige 1.4 e não recua para o caminho sem valor.

SaqueJourney recebe fixedAmountCents opcional no construtor; ele inicializa
valueInput, indica amountLocked e ignora Value quando fixo. Reutiliza
calculateSaque para base, tarifa e total em ambos os caminhos, validando limite
do perfil e parcelas. UI usa
readOnly e o owner impõe o bloqueio independentemente da UI.

Sem dependências novas. Evolução com testes de contratos, JVM, UI e integração
do host usando componente sintético. Nenhum teste dispara operação física.

O valor da request é a base e o resultado compartilhado ainda não devolve
tarifa ou total ao consumidor. Não representar preço base como comprovante do
total cobrado; a conciliação financeira permanece fora da homologação.
