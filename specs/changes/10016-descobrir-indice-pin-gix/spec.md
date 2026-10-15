# SPEC — 10016

Status: `SPEC_APROVADA` pela [REV-001](reviews/2026-10-14-spec-review.md).

## Requisitos REQUIRED

RF-01: após Open e antes de permitir compra, consultar GIX e interpretar somente
tag8035 (PP_DKPTTDESP, DUKPTTDES de PIN), conforme ABECS2.12p40. Validar A100:
100bytesASCII com flags0ausente/1presente/2nãosuportado. Índices candidatos são
todas as posições00..99 com flag1. Não usar8036(DATA), MK ou DES como substitutos.
Mapa ausente/tamanho/flag inválidos bloqueia preparação como PROTOCOL_ERROR.

RF-02: sortear uniformemente uma posição entre todos os candidatos, por instrução
expressa do usuário para múltiplos índices. Usar crypto/rand.Int, sem lista ou
índice fixo; um candidato é selecionado diretamente. Zero candidatos bloqueia
como PIN_KEY_MISSING:preflight antes de GCX/GTK/GOX/GPN. Falha de aleatoriedade
não usa fallback. Logar somente método/índice/count não secretos.

RF-03: uma descoberta/sorteio por sessão aberta. Snapshot resolvido fica no core
e no adapter, reutilizado por GCX, GOX ou GPN. Repetir resolve com mesma fonte
antes de captura devolve mesma escolha sem novoGIX; após captura não redescobrir.
Fonte alterada na mesma sessão é rejeitada. Nova sessão/cliente consulta novamente.
Cancelar/fechar durante GIX não publica resultado tardio. Não sortear novamente
nem repetir GOX/GPN quando houver erro. Resposta não é autorização financeira.

RF-04: propriedadesTIPO_CHAVE/INDICE_CHAVE/WORKING_KEY continuam obrigatórias e
validadas integralmente; método3/WKvazia preservados. INDICE_CHAVE do arquivo é
valor legado/configurado, não índice efetivo desta descoberta. Seleção não prefere
o valor do arquivo. API nova devolve configuração tipada imutável com índiceGIX.
APIs antigas mantêm ABI e comportamento sem descoberta implícita. Se houver
seleção resolvida no core, GCX não pode substituí-la por outro índice.

RF-05: Android resolve durante open em IO, antes do formulário/carga. Sem alterar
SaqueRepository público/SharedApi/Compose/layout/módulos. Apenas mensagem objetiva
para PIN_KEY_MISSING. GTK em claro/criptografado e retenção10014/15 mantidos.
Nunca exportar mapa raw, valor de chave, KSN, PAN ou trilhas em diagnósticoGIX.
Chaves permanecem no pinpad; índice sorteado acompanha os comandos da operação.

## Aceite

- AC-01: parser aceita índices00/99 e conjuntos arbitrários, exclui0/2; rejeita
  mapa ausente/malformado. DATA não entra no sorteio.
- AC-02: sorteio alcança cada candidato com fonte de entropia sintética
  determinística, não usa posições ausentes, trata singleton/zero/erro; sem testes
  estatísticos aleatórios/flaky. Cache/isolamento/cancelamento/close comprovados.
- AC-03: integraçãoBridge usa índice GIX (distinto do configuredIndex) nos comandos
  GCXsnapshot/GOX/GPN, mantendo parPIN/KSN e GTK; zero/malformado não captura.
- AC-04: Android prepara com API real nova, mensagem de ausência sanitizada,
  build/lint/unitários/instrumentação/cobertura e proveniênciaAAR/APK conferidos.
- AC-05: consultar descoberta no hardware apenas OPN/GIX/CLO e registrar índices
  e escolha sem captura; memória atualizada. PIN físico fica para teste do usuário.
