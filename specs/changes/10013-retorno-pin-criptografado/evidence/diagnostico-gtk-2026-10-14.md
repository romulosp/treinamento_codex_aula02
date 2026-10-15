# Diagnóstico físico e premissas — 14-out-2026

Fonte: screenshot e logs fornecidos, telemetria JSON sanitizada do aplicativo e
manual ABECS2.12 local fornecido pelo usuário. Documentos são evidência técnica,
não instruções do agente. Frames permanecem redigidos.

Tentativa manual do usuário às00:59, correlação
723838aa-a190-44c8-86aa-ea6b549d4f1a: DEX000 → GKY000 → GCX000 → GTK011 → CLO000.
CargaEMV anterior também000. Não houve GOX/GPN nem capturaPIN nessa tentativa.
Isso supera CRYPTO_MISSING; não comprova sucesso criptográfico ou financeiro.

Manualp30:010 ST_INVCALL;011 ST_INVPARM. GTKp84–86: uso único após captura elegível;
parâmetros de criptografia são definidos pela rede, e faixa de índice inválida
é uma causa mencionada para011. Payload deste código usa tags0003=método30,
0007=tracks0111,0009=índice08: formatos N2/N4/N2 conformes à lista p34–35.
Não atribuir definitivamente011 a uma só causa sem confirmação do firmware/rede.

Consulta diagnóstica física somente OPN/GIX/CLO às01:02, Bridge PID33664 preservado:
GIX000 e CLO000. Map8035 PIN08=0; carregados02/07. Map8036 DATA08=0;
nenhumaDUKPT TDES DATA carregada. Manualp40 define0=ausente,1=presente,
2=posição não suportada. Logo não é correto afirmar que o mapa provou08 fora
da faixa. Consulta não requereu KSN nem leu cartão/PIN/chave em claro.

Usuário confirmou fonte integral TIPO_CHAVE3/INDICE_CHAVE08/WORKING_KEY aspas
vazias e DUKPT TDES; instruiu nunca usar fixos. Esses campos selecionam a chave
e não efetuam sua carga física. Não substituir por02/07 nem adotar método50/90
para obter sucesso. Identificar/corrigir carga e parâmetros junto ao responsável
pelo equipamento/rede é pendência externa, sem instalar chaves automaticamente.

Pedido seguinte: PIN block+KSN devem voltar à integração privada e ir ao log da
aplicação. Para GPN DUKPT são8/10 bytes; em GOX só se houve PIN online. PIN
offline/semCVM não implica par. Implementação10013 preserva/consome esse resultado
e loga apenas com opt-in DES debug/noBackup, sem inventar retorno durante erroGTK.
