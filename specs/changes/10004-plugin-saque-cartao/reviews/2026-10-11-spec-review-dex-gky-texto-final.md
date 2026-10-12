# REV-019 — DEX informativo e decisão GKY

Status: `SPEC_APROVADA` pela solicitação explícita recebida em 2026-10-11.

Escopo: mensagem física de confirmação no plugin 10004 e testes correspondentes.
Preservar tela Android/SVG, Go/Bridge/AAR, captura/PIN e parâmetros do perfil.
Ao final, encerrar o emulador atual, reiniciar sem snapshot e instalar/abrir o
host atualizado, preservando dados privados e repondo reverse do Bridge.

Mensagem DEX única: VALOR TOTAL e total; VALOR TARIFA e tarifa; VALOR COMPRA
e base; QUANTIDADE e número de parcelas no formato Nx; CANCELA/CONFIRMA.
Cada rótulo/valor fica em linha própria com CR. Centralizar apenas o primeiro
rótulo VALOR TOTAL por espaços iniciais usando colunas GIX disponíveis, somente
se a mensagem inteira continuar em Latin-1 até 160 bytes. Colunas ausentes ou
inválidas e orçamento insuficiente usam mensagem sem alinhamento; nunca bloquear
a confirmação por número de linhas da grade textual. Não adicionar CR iniciais,
espaços finais ou concatenar campos para alterar o layout.

Um único GKY após DEX concluído. Manual ABECS 2.12, página 80:
RSP_STAT 000 = ST_OK; 013 = ST_CANCEL; 008 = ST_BACKSP; 004..007 = F1..F4.
O core ParseGKYStatus já converte para 0x00/0x0D/0x08/0x04..0x07.
Só 0x00 segue para captura; 0x0D encerra cancelado; demais teclas/falhas não
iniciam captura. Não trocar por CEX nesta correção, embora o manual classifique
GKY como obsoleto. Reutilizar fachadas displayDEX/waitForKeyPress existentes.

GIX/PP_DSPTXTSZ fornece LLCC (página 40); DSI exibe arquivo de imagem.
DEX 2.12 (página 70) não permite configurar fonte/alinhamento nativo. Espaços
iniciais são tentativa opcional de layout, sem garantir pixels ou alinhamento
visual do firmware. Não converter dimensões gráficas em largura de caracteres.

Motivo da correção: preencher todas as nove linhas com a largura GIX pode
exceder 160 bytes (por exemplo, 9 linhas de 40 colunas já exigem 368 bytes
com os CR), fazendo DEX falhar antes de GKY. Remover esse preenchimento global.
Testar ordem, payload, confirmação/cancelamento e teclas inesperadas; conferir
hardware separadamente. Nenhuma autorização de saque real ou PIN no Android.
