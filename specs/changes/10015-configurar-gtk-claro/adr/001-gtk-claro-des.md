# ADR-001 — GTK em claro por escolha explícita DES

Status: aceito na SPEC10015.

Decisão autorizada pelo usuário: GTK em claro neste momento, configurável para
DUKPT TDES DATA30/08 depois. ABECS permite ausência SPE_MTHDDAT (p85).
Isso substitui a exigência anterior de GTK sempre criptografado apenas nesta
jornada DES. Nunca realizar downgrade automático quando GTK30 falhar.

Trilhas em claro trafegam na comunicação privada com o pinpad e permanecem no
contexto Go efêmero, obrigatório para o futuro autorizador. Continua proibido
logar/persistir/exibir PAN/trilhas; comandos e frames GTK permanecem redigidos.
PIN permanece criptografado independentemente de GTK; fonte properties mantida.
Nenhum PRD, alteração de enlace, chave ou autorizador implementado.
