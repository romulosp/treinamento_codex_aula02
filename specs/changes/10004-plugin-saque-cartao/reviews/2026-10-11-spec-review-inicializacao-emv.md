# Carga EMV na entrada do plugin — REV-015

Data: 2026-10-11. SPEC_APROVADA pela correção explícita do solicitante:
ao entrar em Pagamento com Cartão, apresentar Aguarde, processando enquanto
carrega a tabela EMV; somente depois mostrar o formulário. AVANÇAR deve iniciar
a função existente de confirmação/cancelamento, sem carregar novamente a tabela.
Supersede a ordem anterior da carga vinculada a Begin.

Sequência: criação de proprietário por View → estado de preparação → leitura
e validação privada do perfil/tabela → Ping/Open → capacidades do display →
TLI/TLR/TLE → prontidão e tela 1. A confirmação de TLE é o gate do formulário,
não leitura local nem Ping. Uma inicialização por proprietário, sem replay por
recomposição, duplo clique ou segunda chamada de load.

Esclarecimento explícito do solicitante: esse comportamento é exclusivo do
plugin, não da infraestrutura da aplicação. Iniciar em attach da View do
Pagamento com Cartão; discovery/criação sem exibição não inicia hardware.
Não alterar host, carregador genérico de APKs ou ciclo de outros plugins.

Preservar o cliente/sessão aberta enquanto o usuário informa valores. Begin
congela o resumo e chama DSP/GKY de confirmação/cancelamento com as capacidades
já consultadas; então segue GCX/GTK/GOX/GPN/FCX conforme contrato vigente.
Novo operationID para a transação, sem recriar cliente nem repetir a carga.

Falha de configuração, Open ou carga impede exposição do formulário, encerra
tentativa parcial quando aplicável e mostra código/fase sanitizados e SAIR.
Cancelamento na preparação chega ao Go e impede prontidão; saída/detach fecha
a sessão inclusive quando o usuário ainda está na tela 1. Respostas tardias
não alteram proprietário descartado. Carga permanece na fachada Go da 074.

UI: etapa técnica 0 anterior às cinco telas, spinner Compose já existente,
texto Aguarde, processando e transição animada para valores; formulário ausente
durante preparação. Erro permanece na preparação sem simular processamento.

Testes: pausar carga e provar formulário bloqueado; ordem Open/capacidades/carga
antes de Begin; exatamente uma carga; primeiro comando de Begin = confirmação;
falha e cancelamento da carga sem GCX; detach durante preparação e saída durante
formulário fecham sessão; UI da espera e transição para valores. Atualizar
evidências, APK do plugin e host consumidor; hardware exige teste humano separado.
