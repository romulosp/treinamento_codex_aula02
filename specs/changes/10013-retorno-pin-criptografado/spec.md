# SPEC — 10013

Status: `SPEC_APROVADA` pela [REV-001](reviews/2026-10-14-spec-review.md).
Complementa a 10011 após instrução explícita do usuário.

## Requisitos REQUIRED

RF-01: novas APIs gomobile proprietárias retornam objeto privado com decisão
EMV e bytes PIN block/KSN. GPN DUKPT TDES exige 8 e 10 bytes respectivamente;
GOX só retorna o par quando PP_GOXRES indica PIN online (terceiro dígito 2).
PIN offline/sem PIN não deve inventar par. Erro, cancelamento ou contexto inválido
não retornam payload. APIs de resumo/void antigas mantêm descarte e ABI.

RF-02: resultado por chamada, sem singleton: getters copiam buffers; Clear
sobrescreve os buffers controlados, com acesso sincronizado. Adapter consome,
usa/loga conforme RF-03 e limpa resultado e cópias em finally. Nenhum par em
Compose/StateFlow/SavedState/JSON público, exceções ou logs gerais/serial.

RF-03: por autorização expressa do usuário, permitir valores criptografados em
log local DES debug da aplicação. Destino exclusivamente
`context.noBackupFilesDir/saque-cartao/pin-results.jsonl`, protegido pelo sandbox,
fora de backup. Opt-in `logPinResults: true` no perfil privado; ausência=false,
tipo não booleano bloqueia preflight. Habilitar no perfil DES fornecido pelo
usuário para atender ao pedido. Registrar command GPN/GOX, operationID, timestamp,
PIN block hexadecimal e KSN hexadecimal. Nunca PIN digitado, PAN, WK ou resposta
bruta. Limitar arquivo a 64 KiB; ao exceder, reiniciar o log diagnóstico. Falha
de escrita registra só categoria sanitizada e não altera decisão/repete compra.
Esta exceção não se aplica a PRD, Logcat ou LogPinpadAbecs.txt do Bridge.

RF-04: importar TIPO_CHAVE/INDICE_CHAVE/WORKING_KEY integralmente por parser Go
existente. API tipada privada da configuração retorna os três valores validados,
incluindo WK normalizada. Kotlin não constrói WK literal/fallback nem índice/método
de operação fixos. Perfil desta homologação continua DUKPT TDES (3), WK vazia;
validar limites não significa substituir valores. DATA GTK permanece configurada
separadamente no perfil; não derivar índice/método DATA do arquivo PIN.

RF-05: preservar GCX→GTK→GOX para 03/06 e GCX→GTK→GPN para 00/05; FCX só GOX=2.
Não remover GTK, trocar método/índice ou repetir captura para compensar status.
Documentar fatos físicos: GTK011=ST_INVPARM, não ST_INVCALL010; fechamento000;
GIX slot08 PIN/DATA ausente, DATA sem chave carregada. Não afirmar qual campo
causou011 unicamente nem fabricar par para simular dispositivo funcionando.

## Plataforma e aceite

Stack/SharedApi/SDKs/topologia da 10011 mantidos. Mudança de fronteira sensível
registrada no ADR-001; instrução do usuário prevalece sobre descarte anterior
apenas para esse retorno/log privado DES.

- AC-01: GPN/GOX online retornam par exato com dados sintéticos; offline vazio;
  getters sem alias, Clear e concorrência; APIs antigas não vazam valores.
- AC-02: erro GTK, correlação/mode inválidos, cancelamento e uso repetido não
  devolvem par; payload incompleto é rejeitado. Sem GPN em EMV.
- AC-03: instrumentação prova log opt-in no noBackupFilesDir, conteúdo sintético
  exato, disabled/offline sem registro, tamanho limitado e configurações inválidas.
- AC-04: índice sintético distinto de08 e três getters provam origem da fonte;
  WK literal removida do Kotlin; parser continua rejeitando fonte inválida.
- AC-05: Go/JVM/lint/build/instrumentação, KDoc e AAR/API/ABI/proveniência registrados.
- AC-06: arquivo externo DES opt-in provisionado com hash, memória atualizada;
  captura física real pendente até confirmação de chaves habilitadas/carregadas.
