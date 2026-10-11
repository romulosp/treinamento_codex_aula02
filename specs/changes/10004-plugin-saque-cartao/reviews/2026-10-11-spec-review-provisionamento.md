# Correção do provisionamento — REV-013

Data: 2026-10-11. SPEC_APROVADA para corrigir o launcher dentro do contrato
de provisionamento privado já aprovado, em resposta à falha relatada na tela.
Não altera comandos, índices, cenário ou política financeira.

Reprodução: diretório files/saque-cartao ausente; MISSING/preflight esperado.
Ao provisionar, as aspas duplas passadas via Start-Process são consumidas no
parsing Windows. A shell remota aplica o redirecionamento antes do run-as,
causando falha de gravação. Preservar aspas simples em torno do argumento
de sh -c no comando remoto. Confirmar exit e SHA-256 de cada arquivo privado
contra o arquivo local antes de abrir o host, sem emitir conteúdo dos arquivos.
Na reprodução seguinte, o caminho de stdin normalizou CRLF do perfil, alterando
seu hash. Solução comprovada: Base64 em memória enviado por StandardInput.BaseStream,
`adb shell -T` e decodificação diretamente sob run-as com `base64 -d`.
Sem arquivo intermediário público ou payload no console. Fechar stdin e aguardar
exit remoto antes de conferir o hash.

Correção documental: TAB_ACQ real é 04, conferido nos 36 registros DES.
APPTYPE permanece 01/02, lido no offset 42 do registro AID, não no campo AIDLEN.
Perfil candidato local não é provisionado até a escolha explícita do cenário
FCX e aceitação dos parâmetros propostos. A tabela pode ser preparada isoladamente
sem cliente, sessão ou execução de comandos físicos.

## Parâmetros aceitos — REV-014

O solicitante respondeu à proposta completa do perfil e selecionou APPROVE
para a simulação local atual, reconhecendo negação e erro de comunicação como
cenários futuros do servidor. SPEC_APROVADA para o perfil proposto: adquirente 04,
versão DES873D94F derivada do SHA-256, prazos 30s, limite técnico 876117049236
centavos para respeitar N12 após a maior taxa, método DATA 30/índice 08.
Não constitui limite comercial aprovado ou evidência de chave instalada.

O código mantém a decisão do GOX: 0/1 conclui offline sem forçar FCX; somente
resultado 2 usa APPROVE simulado (opções 0000/ARC 00). Negação do pinpad e falha
real de transporte não são convertidas em sucesso. A integração futura com
servidor precisa de contrato próprio para substituir a simulação.
