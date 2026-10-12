# Recuperação da versão anterior — 2026-10-11

Solicitação: recuperar a versão que chegava à digitação da senha, após relato
de aplicação não abrir. Nenhuma nova mudança de código nesta recuperação.

Host e plugin gerados após reversão têm exatamente os hashes registrados para
REV-017. O host foi reinstalado com `install -r`, preservando os dados privados.

Diagnóstico de execução: tentativas `am start -W` e `dumpsys` ficaram bloqueadas
antes do reinício; o crash buffer contém falhas nativas FORTIFY/mutex destruído
em hwui do processo host. Isso não demonstra que a formatação DEX causou as
falhas nativas. Reinício frio do Medium_Tablet, sem wipe-data, recuperou os
serviços; abertura posterior terminou com Status ok em 1308 ms e UI de login.

Bridge existente preservado; Hello/Ping PBRG aprovado e reverse 39100 reposto.
Perfil/tabela privados continuam presentes; conteúdo não lido nem registrado.
Não houve OPN, leitura de cartão ou captura de PIN nesta recuperação. O relato
do solicitante de estabilidade até a senha não é convertido em nova evidência
física. Evidências de abertura e identidade dos APKs em validation.md.
