# DESIGN — 10016

Parser puro extrai []int dos flagsA100 da tagPIN existente8035. Serviço privado
ResolvePaymentPINConfiguration valida fonte, sessão e paymentStarted sob locks,
consulta GIX uma vez, limpa resposta raw, sorteia com crypto/rand e publica sob
s.mu conferindo epoch/contexto/estado. Cache contém fonte e configuração resolvida;
Open novo/Close/Shutdown removem cache; paymentMu serializa resolve/captura.
LoadTable pode invalidar captura, mas não deve sortear outra chave nesta sessão.

Função auxiliar recebe io.Reader somente para testar falha/índices com entropia
determinística; produção usa crypto/rand.Reader, sem DI global mutável/dependência.
Configuração retornada é valor/cópia; nenhuma referência mutável ao cache.

API mobile ResolvePaymentPINConfiguration(operationID,method,configuredIndex,WK)
devolve PaymentPINConfiguration existente. Método/WK preservados; índice resolvido.
Adapter substitui snapshot somente após resolve bem-sucedido em open e reutiliza
em captura/continuação. Sem acesso a valor de chave. Novo erro de domínio
ErrPaymentPINKeyMissing mapeia categoria pública PIN_KEY_MISSING:preflight.
Mensagem da jornada explica carga de chave, sem paths/valores arbitrários.

Decisão de sorteio restrita à homologaçãoDES no [ADR-001](adr/001-indice-pin-sorteado.md).
Não incorporar skill externa/stack/upgrade, topologia HIGH_ASSURANCE herdada.
