# Plano — 10016

Criar parser payment_pin_keys e service payment_pin_keys com cache por sessão.
Acoplar cheque opcional de seleção no CapturePaymentCard e limpar cache no ciclo
de vida. API aditiva mobile, atualizar KDoc/godoc e resolver no open Android.
Erro sem chaves tem categoria própria e mensagem testada.

Testes parser tabelados, entropia determinística por readerlocal, sessões fake
comGIX antes de captura, cancelamento e repeat. IntegraçãoBridge aditiva mantém
28cenários anteriores; novo ramo usa única chave conhecida de GIX para verificar
payload de PIN independente do índice doarquivo. Instrumento testa API binding
e preparação sem carregar chave/cartão, com bytes sintéticos quando necessário.
Goall/vet/integration/cobertura, estrutural/lint/JVM/build/emulador, AAR/API/ABI/
hashes. Consulta física resolve só OPN/GIX/CLO; não executar compra.
