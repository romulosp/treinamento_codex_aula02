# Tarefas: 066-lib-pinpad-abecs-go

Autor: RÃ´mulo Penha

## Regra de leitura deste arquivo

Os checks abaixo representam tarefas documentadas, nÃ£o uma prova automÃ¡tica de que o comportamento estÃ¡ completo. A integraÃ§Ã£o completa ABECS v2.12 somente poderÃ¡ ser marcada como concluÃ­da depois da matriz protocolo â†’ Go â†’ SPEC â†’ evidÃªncia, da revisÃ£o de implementaÃ§Ã£o e da validaÃ§Ã£o com pinpad fÃ­sico real.

## SPECs individuais

Cada comando possui agora uma SPEC prÃ³pria ou uma SPEC complementar prÃ³pria:

- Ciclo de vida: `spec-command-can.md`, `spec-command-opn.md`, `spec-command-clo.md`, `spec-command-clx.md`, `spec-command-rst.md`.
- InformaÃ§Ãµes/display: `spec-command-gix.md`, `spec-command-dsp.md`, `spec-command-dex.md`, `spec-command-mnu.md`, `spec-command-dsi.md`, `spec-command-qrcode.md`.
- MultimÃ­dia: `spec-command-mli.md`, `spec-command-mlr.md`, `spec-command-mle.md`,
  `spec-command-lmf.md` e `spec-command-dmf.md`.
- Tabelas EMV: `spec-command-tli.md`, `spec-command-tlr.md`, `spec-command-tle.md`.
- Teclas: `spec-command-gky.md`.
- TransaÃ§Ã£o/cartÃ£o: `spec-command-gcx.md`, `spec-command-gtk.md`, `spec-command-gox.md`, `spec-command-fcx.md`.
- PIN: `spec-command-gpn.md`.
- Transversal: `spec-logging.md`, `spec-infra-serial-cancel.md`, `spec-protocolo-seguro.md`.

Todas as SPECs individuais permanecem sujeitas a revisÃ£o formal. A existÃªncia do arquivo nÃ£o autoriza implementaÃ§Ã£o antes de `SPEC_APROVADA`.

## PrÃ©-condiÃ§Ãµes

- [x] Receber definiÃ§Ã£o funcional da Change 066.
- [x] Definir `groupId`, `artifactId`, mÃ³dulo, diretÃ³rio e executÃ¡vel.
- [x] Remover REST, WebSocket, UI e servidores do escopo.
- [x] Aprovar `go.bug.st/serial` como adaptador serial.
- [x] Revisar e aprovar formalmente a SPEC apÃ³s resoluÃ§Ã£o dos achados REV-001 a REV-006.

## ImplementaÃ§Ã£o

- [x] Criar `apps/desktop/libpinpadabecsgo/go.mod` com o mÃ³dulo aprovado.
- [x] Criar README e executÃ¡vel `cmd/libpinpadabecsgo` para validaÃ§Ã£o local.
- [x] Expor no menu local as fachadas implementadas de ciclo de vida, GIX, display, sessÃ£o segura, multimÃ­dia, tabelas EMV, GCX, GTK, GOX, FCX e GPN, preservando a redaction de dados sensÃ­veis e as indisponibilidades contratuais de QR e `TransactionGCX` completo.
- [x] Criar pacotes de domÃ­nio conforme o DESIGN.
- [x] Implementar `PinpadConfig` e carregamento/validaÃ§Ã£o de ambiente.
- [x] Implementar modelos de estado, resposta, dispositivo, display e GCX.
- [x] Implementar catÃ¡logo de comandos e status ABECS.
- [x] Gerar constantes de protocolo, `RSP_DATID`, mÃ­dia e limites definidos na SPEC.
- [x] Gerar catÃ¡logo completo de `RSP_STAT`, parÃ¢metros `SPE_xxx`, cÃ³digos `GKY`, `GPN` e `GCX`.
- [x] Implementar erros sentinela e tipos de erro.
- [x] Implementar CRC-16-CCITT.
- [x] Implementar ApplySubstitution e RemoveSubstitution.
- [x] Implementar BuildPacket e ReadFullResponse.
- [x] Implementar parser ABECS e tags obrigatÃ³rias.
- [x] Implementar parser BER-TLV.
- [x] Implementar porta `SerialPort`, adaptador real e fake.
- [x] Implementar `SessionManager` com relÃ³gio injetÃ¡vel.
- [x] Implementar `CommandQueue` FIFO, capacidade 100, `Enqueue` nÃ£o bloqueante e `ErrQueueFull`.
- [x] Implementar `Submit` bloqueante com espera por resultado ou cancelamento de contexto.
- [x] Implementar worker Ãºnico, contexto, cancelamento e shutdown.
- [x] Implementar `PinpadService` e comandos CAN/OPN/GIX/CLO.
- [x] Implementar builders, parsers e fachada de display DSP/DEX/MNU.
- [x] Cobrir a resposta fÃ­sica MNU com comprimento `006` e seleÃ§Ã£o no campo
  TLV `0x804D`, preservando Ã­ndices de dois dÃ­gitos.
- [x] Implementar fluxo serial de multimÃ­dia DSI/MLI/MLR/MLE.
- [x] Revisar formalmente as SPECs aditivas de LMF, DMF e recuperaÃ§Ã£o de timeout antes de implementar (`reviews/2026-09-14-spec-review.md`).
- [x] Implementar LMF/DMF na camada de domÃ­nio, parser, serviÃ§o e menu local.
- [x] Preservar todos os `PP_MFNAME` repetidos na resposta LMF e devolvÃª-los em maiÃºsculas.
- [x] Recuperar timeout e resposta incompatÃ­vel com CAN/EOT; bloquear comandos comuns se a recuperaÃ§Ã£o falhar.
- [x] Implementar fluxo serial de tabelas EMV TLI/TLR/TLE.
- [x] Implementar GKY, incluindo parser de teclas e timeout. (builder, parser e fachada WaitForKeyPress implementados)
- [x] Corrigir o builder GCX para parÃ¢metros ABECS `0013`, `0015`, `0016` e
  `0017`, com valores N12/N6/N6/N5 e validaÃ§Ã£o antes da serial.
- [x] Remover o GCX preliminar com data/hora zeradas, sua configuraÃ§Ã£o e sua
  opÃ§Ã£o de menu; `PurchaseGCX` deve emitir exatamente um GCX.
- [x] Criar regressÃ£o unitÃ¡ria byte a byte para CA-GCX-008 a CA-GCX-010.
- [x] Implementar o consumo de zero ou mais notificaÃ§Ãµes `NTM` antes da resposta
  final GCX, sem escrita de ACK e sem interpretaÃ§Ã£o TLV da mensagem.
- [x] Perguntar na opÃ§Ã£o 11 se o GCX aceita chip/tarja ou
  chip/tarja/contactless, se mostra ou oculta o valor, e validar as escolhas.
- [x] Criar as regressÃµes automatizadas CA-GCX-011 e CA-GCX-012.
- [x] Iniciar o prazo da opÃ§Ã£o 11 apÃ³s a coleta das entradas e preservar o
  contexto do consumidor no GCX, sem aplicar o timeout genÃ©rico da configuraÃ§Ã£o.
- [x] Criar a regressÃ£o automatizada CA-GCX-013 para propagaÃ§Ã£o do prazo.
- [x] Implementar stub `ErrNotImplemented` para `TransactionGCX` com parÃ¢metros completos.
- [x] Implementar GPN MK/WK e DUKPT, validaÃ§Ãµes e parser binÃ¡rio.
- [x] Remover RST por ausÃªncia no manual ABECS 2.12.
- [x] Especificar e implementar fachada de ciclo de vida e configuraÃ§Ã£o.
- [x] Implementar fachada de comandos bÃ¡sicos, display e capabilities.
- [x] Implementar fachada de imagens, QR Code (via `QRCodeGenerator` injetÃ¡vel) e progresso.
- [x] Implementar `ErrQRCodeGeneratorNotConfigured` e validaÃ§Ã£o de tamanho/margem do QR Code.
- [x] Implementar fachada de carga completa de tabelas EMV.
- [x] Implementar fachada transacional GCX (subconjunto especificado) sem expor `SendRawCommand`.
- [x] Implementar fachadas GKY e GPN; reset usa CAN/EOT e RST foi excluÃ­do.
- [x] Testar concorrÃªncia, cancelamento, shutdown e redaction na fachada.
- [x] Revisar e completar o contrato da fila FIFO, cancelamento e shutdown.
- [x] Revisar e completar o contrato do `SessionManager` e expiraÃ§Ã£o de 300 segundos.
- [x] Confirmar que a bridge HTTP nÃ£o serÃ¡ convertida nem adicionada ao mÃ³dulo Go.
- [x] Criar `start_aplication.bat` para execuÃ§Ã£o local sem privilÃ©gios administrativos, com `COM7` temporÃ¡rio.
- [x] Confirmar que `.gocache`, `.gomodcache`, `.bin` e executÃ¡veis locais nÃ£o serÃ£o versionados.
- [x] Implementar logging `slog` com redaction e tracer SPE/PP/RSP conforme
  `spec-logging.md`, incluindo destino absoluto canÃ´nico
  `<raiz-do-mÃ³dulo>/logs/LogPinpadAbecs.txt`, `PINPAD_LOG_FILE`, marcador de
  ativaÃ§Ã£o, caminho ativo exibido, eventos de ciclo de vida, cobertura de todos
  os comandos tipados e coleta segura de evidÃªncias.
- [x] Remover a dependÃªncia do diretÃ³rio de trabalho na resoluÃ§Ã£o do arquivo de
  rastro e impedir a criaÃ§Ã£o de `cmd/libpinpadabecsgo/logs/LogPinpadAbecs.txt`
  ou de qualquer outro arquivo homÃ´nimo acidental.
- [x] Fazer o adaptador serial real emitir uma Ãºnica linha `SPE` apÃ³s escrita
  integral confirmada e uma linha `PP` por leitura nÃ£o vazia; manter `RSP` no
  serviÃ§o apÃ³s interpretaÃ§Ã£o do status, sem duplicaÃ§Ã£o entre camadas.
- [x] Criar teste de integraÃ§Ã£o do adaptador com pacote GIX conhecido que
  compare byte a byte o hexadecimal de `SPE` com o buffer escrito e cada `PP`
  com o retorno de leitura, incluindo ACK isolado, resposta completa e resposta
  fragmentada.
- [x] Propagar ou tornar observÃ¡vel qualquer falha de escrita/flush/fechamento
  do tracer; nenhuma falha de persistÃªncia poderÃ¡ ser descartada silenciosamente.

## DocumentaÃ§Ã£o obrigatÃ³ria do cÃ³digo

- [x] Ler e aplicar `.agents/skills/golang-documentation/SKILL.md` antes de gerar ou alterar cÃ³digo Go.
- [x] Adicionar comentÃ¡rio de pacote a todos os pacotes Go entregues.
- [x] Adicionar comentÃ¡rios Go a todas as funÃ§Ãµes, mÃ©todos, tipos, interfaces, constantes e variÃ¡veis exportadas.
- [x] Documentar funÃ§Ãµes internas complexas de protocolo, framing, parser, serial, concorrÃªncia, seguranÃ§a, redaction, fila, worker e cancelamento.
- [x] Documentar builders, parsers, modelos, erros e fachadas conforme as SPECs individuais dos comandos.
- [x] Avaliar `ExampleXxx`: nÃ£o aplicÃ¡vel aos pacotes `internal`; o CLI e os fluxos sÃ£o exercitados por testes executÃ¡veis.
- [x] Verificar cÃ³digo gerado: nÃ£o hÃ¡ arquivos Go gerados por template ou ferramenta no mÃ³dulo.
- [x] Verificar ausÃªncia de segredos e dados sensÃ­veis em comentÃ¡rios, exemplos, fixtures e documentaÃ§Ã£o.
- [x] Registrar a inspeÃ§Ã£o documental na revisÃ£o da implementaÃ§Ã£o e em `validation.md`.

## IntegraÃ§Ã£o completa do protocolo ABECS v2.12

- [x] Inventariar todas as funÃ§Ãµes e fluxos do protocolo ABECS v2.12 relevantes ao escopo.
- [x] Criar matriz de rastreabilidade protocolo â†’ pacote Go â†’ SPEC â†’ teste â†’ evidÃªncia fÃ­sica.
- [x] Implementar OPN/CLO e o protocolo seguro RSA/AES conforme `spec-protocolo-seguro.md`; implementar CLX como fluxo visual independente conforme `spec-command-clx.md`.
- [x] Implementar GTK, GOX e FCX em modelos prÃ³prios, sem misturar dados com GCX.
- [ ] Implementar `TransactionGCX` somente apÃ³s aprovaÃ§Ã£o da SPEC de seus parÃ¢metros completos.
- [ ] Confirmar que cada comando possui builder, parser, fluxo serial, timeout, cancelamento, logging e validaÃ§Ã£o fÃ­sica.
- [x] Confirmar que cada pacote e sÃ­mbolo implementado atende RF-017 e CA-021/CA-023.

## RevisÃ£o e validaÃ§Ã£o

- [x] Inventariar arquivos `.go` e testes aplicÃ¡veis.
- [x] Criar testes orientados a tabela para CRC, bytes, substitution e parsers.
- [x] Criar testes dos builders avanÃ§ados e fake serial.
- [x] Criar testes de sessÃ£o, estados, erros, fila e shutdown.
- [x] Executar `gofmt`.
- [x] Executar `go vet ./...`.
- [x] Executar `go test ./...` (todos os pacotes aprovados com cÃ³digo 0 em 2026-09-13).

## Estado da implementaÃ§Ã£o

`IMPLEMENTADA`. O contrato de logging revisado em 2026-09-12 foi implementado,
testado e exercitado no pinpad fÃ­sico para o fluxo `Open â†’ GIX â†’ Close`. A
serializaÃ§Ã£o GCX foi corrigida conforme a seÃ§Ã£o 3.7.1 do manual ABECS v2.12 e
possui regressÃ£o unitÃ¡ria byte a byte. O fluxo tambÃ©m consome notificaÃ§Ãµes NTM
atÃ© a resposta final e o CLI expÃµe as quatro combinaÃ§Ãµes vÃ¡lidas de GCXOPT. A
opÃ§Ã£o 11 tambÃ©m inicia seu prazo somente apÃ³s as entradas, e o GCX preserva o
contexto do consumidor sem ser limitado pelo timeout genÃ©rico. A
Change aguarda revisÃ£o independente da implementaÃ§Ã£o e validaÃ§Ã£o formal; os
demais fluxos fÃ­sicos completos continuam pendentes conforme suas SPECs.
- [x] Executar `go test ./... -coverprofile=coverage.out`.
- [x] Executar `go tool cover -func=coverage.out` (perfil `coverage` no PowerShell; cobertura total aferida: 81,7%).
- [x] Executar `go test -race ./...` (nÃ£o suportado pela distribuiÃ§Ã£o Go `windows/386`; limitaÃ§Ã£o de ambiente registrada).
- [x] Validar build Windows/Linux ou registrar limitaÃ§Ã£o objetiva.
- [x] Executar auditoria de seguranÃ§a aplicÃ¡vel e revisar dependÃªncias (`govulncheck`: zero vulnerabilidades alcanÃ§Ã¡veis).
- [x] Testar a resoluÃ§Ã£o do mesmo caminho absoluto a partir da raiz do mÃ³dulo e
  de `cmd/libpinpadabecsgo`, comprovando que somente um arquivo Ã© criado.
- [ ] Testar a matriz de `CAN`, `OPN`, `CLO`, `CLX`, `GIX`, `DSP`, `DEX`, `MNU`,
  `DSI`, `MLI`, `MLR`, `MLE`, `LMF`, `DMF`, `TLI`, `TLR`, `TLE`, `GKY`, `GCX`,
  `GTK`, `GOX`, `FCX` e `GPN` quanto a `CMD=`, ordem e redaction aplicÃ¡vel.
- [x] Executar revisÃ£o de documentaÃ§Ã£o Go conforme `golang-documentation`; nenhuma lacuna de comentÃ¡rio exportado permaneceu.
- [x] Registrar evidÃªncias em `validation.md`.
- [ ] Executar revisÃ£o da implementaÃ§Ã£o, validaÃ§Ã£o, aprovaÃ§Ã£o e encerramento conforme workflow.


## CorreÃ§Ã£o de conformidade ABECS 2.12 â€” 2026-09-13

- [x] Revisar o manual 2.12 e registrar divergÃªncias em revisÃ£o formal.
- [x] Corrigir a fonte de verdade em `spec-conformidade-abecs-v212.md`.
- [x] Corrigir transporte ACK/NAK, retransmissÃ£o, prazos e CAN/EOT.
- [x] Corrigir OPN clÃ¡ssico/seguro, CLO e proteÃ§Ã£o de CLO/CLX.
- [x] Corrigir builders e parsers de display, MNU, GKY e GPN.
- [x] Corrigir multimÃ­dia MLI/MLR/MLE/DSI e CRC16.
- [x] Corrigir TLI/TLR/TLE e semÃ¢ntica dos status 000/020.
- [x] Corrigir data GCX, contratos GTK/GOX/FCX e validaÃ§Ãµes condicionais.
- [x] Corrigir PP_MFSUP, PP_DSPGRSZ e catÃ¡logo de status.
- [x] Remover RST do cÃ³digo, fachada e CLI.
- [x] Substituir falsos orÃ¡culos por testes byte a byte derivados do manual.
- [x] Executar revisÃ£o de implementaÃ§Ã£o; resultado `IMPLEMENTACAO_APROVADA`.
- [x] Executar validaÃ§Ã£o automatizada; resultado `VALIDADA` com evidÃªncias VAL-ABECS-001 a VAL-ABECS-010.
- [ ] Executar validaÃ§Ã£o fÃ­sica integral dos comandos aplicÃ¡veis.

## Estado atual da implementaÃ§Ã£o

`IMPLEMENTACAO_APROVADA`. Os achados IMP-REV-016 a IMP-REV-026 foram corrigidos,
os critÃ©rios automatizados da SPEC de conformidade ABECS 2.12 foram executados e
a revisÃ£o de 2026-09-13 nÃ£o encontrou nova divergÃªncia material. A matriz fÃ­sica
completa permanece `PENDENTE_VALIDACAO_FISICA`, sem reduzir a evidÃªncia
automatizada a uma alegaÃ§Ã£o de hardware.

## CorreÃ§Ã£o da opÃ§Ã£o 19 GTK â€” 2026-09-13

- [x] Diagnosticar no rastro fÃ­sico `GTK042` apÃ³s envio DUKPT com Ã­ndice 02.
- [x] Especificar escolha explÃ­cita entre trilhas em claro e criptografadas.
- [x] Implementar `GTK000` para o modo em claro, sem Ã­ndice de chave.
- [x] Preservar DUKPT mÃ©todo 50 como opÃ§Ã£o criptografada com Ã­ndice 00..99.
- [x] Criar testes unitÃ¡rios do prompt, das escolhas e das entradas invÃ¡lidas.
- [x] Executar revisÃ£o e validaÃ§Ã£o automatizada da correÃ§Ã£o.

## CorreÃ§Ã£o da opÃ§Ã£o 21 FCX e diagnÃ³stico GOX â€” 2026-09-13

- [x] Diagnosticar o envio fixo de aprovaÃ§Ã£o/ARC apÃ³s GOX online e confrontar a
  seÃ§Ã£o 3.7.4 do manual ABECS 2.12.
- [x] Especificar escolha explÃ­cita do resultado da comunicaÃ§Ã£o com a rede e
  obrigatoriedade condicional de `SPE_ARC`.
- [x] Conservar `PP_GOXRES`, coletar os campos FCX e remover aprovaÃ§Ã£o fixa.
- [x] Exibir `PP_FCXRES` somente como resultado do pinpad.
- [x] Registrar os parÃ¢metros nÃ£o sensÃ­veis do GOX para diagnosticar o retorno
  fÃ­sico `047` sem expor PIN, chaves, trilhas ou EMV.
- [x] Criar testes unitÃ¡rios byte a byte e casos invÃ¡lidos.
- [x] Executar revisÃ£o e validaÃ§Ã£o automatizada da correÃ§Ã£o.
- [x] Confirmar fisicamente `GCX000 â†’ GTK000 â†’ GOX000 â†’ FCX000` na COM7.

## DecodificaÃ§Ã£o das trilhas GTK em claro â€” 2026-09-13

- [x] Confirmar a codificaÃ§Ã£o de PAN/trilhas 2 e 3 na seÃ§Ã£o 5.4.2.2 do manual.
- [x] Especificar conversÃ£o dos nibbles `0`â€“`9`, `D` e filler `F`.
- [x] Implementar a conversÃ£o antes da linha `GTK_CLEAR`.
- [x] Testar vetor fÃ­sico, separador, filler e nibbles invÃ¡lidos.
- [x] Executar revisÃ£o e validaÃ§Ã£o automatizada.

## CorreÃ§Ã£o da opÃ§Ã£o 20 GOX â€” 2026-09-13

- [x] Diagnosticar `GOX011` no rastro fÃ­sico e confrontar a seÃ§Ã£o 3.7.3.
- [x] Especificar seleÃ§Ã£o de `SPE_ACQREF` a partir de `PP_AIDTABINFO`.
- [x] Conservar o valor do GCX e coletar os parÃ¢metros obrigatÃ³rios de PIN.
- [x] Remover adquirente, mÃ©todo e Ã­ndice fixos do menu.
- [x] Criar testes unitÃ¡rios byte a byte e casos invÃ¡lidos.
- [x] Executar revisÃ£o e validaÃ§Ã£o automatizada da correÃ§Ã£o.

## Registro local das trilhas GTK em claro â€” 2026-09-13

- [x] Atualizar os contratos GTK e de logging com a exceÃ§Ã£o do CLI local.
- [x] Implementar linha `GTK_CLEAR` apÃ³s resposta vÃ¡lida no modo em claro.
- [x] Preservar redaction dos frames GTK e do modo criptografado.
- [x] Testar conteÃºdo, escaping, campos vazios e ausÃªncia no modo criptografado.
- [x] Executar revisÃ£o e validaÃ§Ã£o automatizada da correÃ§Ã£o.

## CorreÃ§Ã£o multimÃ­dia da opÃ§Ã£o 16

- [x] Iniciar prazo depois das perguntas e explicar nome A8.
- [x] Comunicar progresso total somente apÃ³s MLE000.
- [x] Testar digitaÃ§Ã£o lenta, limites de blocos, erros e pacotes normativos.
- [x] Registrar testes e build; validaÃ§Ã£o fÃ­sica permanece pendente em
  `validation.md` (VAL-MM-001 a VAL-MM-008).

## Compatibilidade de formato MLI â€” 2026-09-14

- [x] Reaprovar a regra de transferÃªncia de tipo desconhecido como RUF.
- [x] Implementar MLI com tipo RUF sem rejeiÃ§Ã£o local.
- [x] Testar MLI/MLR/MLE com assinatura desconhecida; o formato no DSI Ã© decidido pelo pinpad.

Resultado anterior deste aditivo: `IMPLEMENTACAO_APROVADA`; validaÃ§Ã£o
automatizada `VALIDADA`; integraÃ§Ã£o fÃ­sica multimÃ­dia
`PENDENTE_VALIDACAO_FISICA`. O gate formal permanece `REPROVADA` em
`reviews/2026-09-14-approval.md` e foi atualizado pelas evidÃªncias VAL-MM-011 a
VAL-MM-013 e pelo aditivo abaixo.

## ReconexÃ£o automÃ¡tica e evidÃªncia visual â€” 2026-09-14

- [x] Registrar o rastro fÃ­sico de MLE sem resposta, trÃªs CAN sem EOT, Reset sem
  EOT e recuperaÃ§Ã£o manual por Close/Open com CAN/EOT e OPN000.
- [x] Corrigir as SPECs para distinguir `DSI000` de confirmaÃ§Ã£o visual.
- [x] Definir reconexÃ£o serial controlada como fallback de CAN/EOT, sem reenvio
  automÃ¡tico do comando de resultado indeterminado.
- [x] Revisar formalmente o aditivo em
  `reviews/2026-09-14-spec-review-4.md`.
- [ ] Implementar a reconexÃ£o automÃ¡tica na recuperaÃ§Ã£o pÃ³s-timeout e em Reset.
- [ ] Atualizar o CLI para orientar a confirmaÃ§Ã£o visual de DSI.
- [ ] Criar testes unitÃ¡rios da reconexÃ£o, falhas parciais, tentativa Ãºnica,
  ausÃªncia de reenvio e proteÃ§Ã£o da fila.
- [ ] Executar nova revisÃ£o de implementaÃ§Ã£o e validaÃ§Ã£o automatizada.
- [ ] Validar na COM7 a reconexÃ£o automÃ¡tica apÃ³s timeout de MLE e confirmar
  visualmente a imagem esperada apÃ³s `DSI000`.

Resultado deste aditivo de SPEC: `SPEC_APROVADA`; implementaÃ§Ã£o e validaÃ§Ã£o
permanecem pendentes.`r`n
- [x] Alterar a opção 16 para gerar PNG de QR Code a partir de texto, validar capacidades via GIX e rejeitar pinpad incompatível antes de MLI.
