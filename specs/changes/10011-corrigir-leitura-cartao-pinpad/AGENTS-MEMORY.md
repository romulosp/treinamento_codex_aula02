# Memória operacional para agentes — Change 10011

Atualizada em 2026-10-14. Leia este arquivo antes de retomar trabalho de leitura,
PIN, criptografia, Go/ABECS ou plugin `:plugin-saque-cartao` nesta Change.

## Pedido vigente

MARCO MAIS RECENTE: usuário testou aplicação e confirmou “funcionou!!!”. Enviou
LogPinpadAbecs.txt para conferir. Bridge terminou comsession_released/
client_disconnected/bridge_stopped(codeOK), payloads redigidos. Emulador5554online,
perfilDES/logPinResults=true/GTKclaro; arquivo privadoPINresults não encontrado e
Logcat atual semeventosABECS. Contexto logger foi conferido(host.noBackupFilesDir).
Fluxo funcional de senha confirmado PELO USUÁRIO; parPINblock+KSN real ainda
não comprovado por evidência disponível. Não afirmarPINoffline como causa nem
simular retorno, refazer compra automaticamente ou copiar criptogramas parachat.
Sem alteração de produto nesta conferência; detalhes emvalidation10016.

ATIVO AGORA10016: usuário retomou após cancelamento e autorizou consultar GIX
para índicesDUKPT; respondeu explicitamente sortear entre todos os carregados,
incluindo quando2oumais. Nova SPEC10016/ADR001 aprova índicePIN efetivo dinâmico;
TIPO_CHAVE/WK continuam fonteproperties, INDICE_CHAVE obrigatório legado não é
preferência. Consultar8035 somentePIN DUKPTTDES, flags0/1/2A100. Uma seleção
uniforme crypto/rand por sessão, congelada em GCX/GOX/GPN; sem retry/re-sorteio
após erro. Zero candidatas bloqueiaPIN_KEY_MISSING antes da captura. Mapa inválido
PROTOCOL_ERROR. GIX não retorna valor secreto de chave. Não usar8036DATA paraPIN.
10016 IMPLEMENTADA/revisãoIMPLEMENTACAO_APROVADA; Goall/vet/integrationPASS,
44cenáriosBridge,32JVM/18instrumentados/0falhas,lint/cobertura97,34%/buildPASS26s/39s.
AAR SHA256383259f6095ca25a079fc0ed388027c47f61b5369f31bbde51485e8501e875eb,
69sourcehashes/11APIs/4ABIs/nativos/plugin/host conferidos. Hostinstall-r,
ProvisionOnly3snapshots/hashesconfirmed, Activity relançada. Fonteexterna inalterada.
Primeira tentativa física novaAPI falhouSERIAL_UNAVAILABLE. Usuário pediu reteste;
14-out22:12:23America/Sao_PauloPASS: abertura/GIX000,2índicesPINpresentes,
sorteado02(método3/fontelegada08), cacheRepeatSame=true, CLO000/close_confirmed.
Consulta não capturou cartão/PIN/compra. Listener33664mantido. Descoberta física
agora comprovada; próxima sessão sorteia novamente, não fixar02 a partir deste teste.
Ler diagnóstico-físico/VAL-01110016. Próximo: jornada física do usuário para obter
PINblock/KSN. Validação integralREPROVADA: PINfísico/race/auditoria. Sem commit/archive.

PONTO MAIS RECENTE14-out08:31: GTK em claro passou no hardware; tentativa real
R$21,07/correlaçãoc93e59ea-1d77-40a8-978b-753c65bf95a6: GCX000→GTK000→GOX042→CLO000.
GOX042=ST_ERRKEY, nãoGTK011. SnapshotAndroid confirmadoPINmétodo3/índice08/WKvazia.
Nova consulta física OPN/GIX/CLO08:31:57 confirmouPIN08ausente, PIN02/07presentes;
DATA08ausente/nenhumaDATApresente; CLO000 confirmado, nenhuma captura automática.
Índice consultado obtido pelo parser do arquivo externo. Usuário perguntou qual
chave foi referenciada: PIN DUKPTTDES08, nãoDATA. Ler evidence/diagnostico-gox042
da10015. Para concluir: responsável carregar/confirmarPIN08 ou informar índice
PINautorizado para editarproperties/provisionar; NÃO escolher02/07 por presença.
Ainda não há PINblock+KSN real. Fonteproperties seleciona registro, não injeta chave.

ATIVO AGORA: [10015 — GTK em claro configurável](../10015-configurar-gtk-claro/spec.md).
Resposta do usuário em14-out esclarece modo ATUAL em claro, não confirma carga
da chaveDATA08. GTK continua obrigatório. Perfil externo gtkDataMethod=""
significa em claro,30 significa DUKPTTDESDATA; gtkKeyIndex08 é preservado e só
enviado em30. Não usar00 para em claro (é MK/WKDES), não fallback automático.
PIN3/08/WK vazia continua do properties, independente de GTK; chavePIN física
é necessária quando GOX/GPN pedir PIN. Trilhas nunca em log/UI/persistência;
retenção privada10014 identifica modoDATA/índice operativo (-1 em claro).
10015 SPEC_APROVADA/ADR001; IMPLEMENTADA/revisãoIMPLEMENTACAO_APROVADA. Perfil
externo alterado somente gtkDataMethod30→vazio; backupTEMPcodex-10015-perfil-before.json.
Goall/vet/integrationPASS,28cenáriosBridge;9perfísPS/8provisionamentoPASS.
Android31JVM/17instrumentados/0falhas, lint/cobertura97,33%/buildPASS32s/36s.
AARregeneradoSHA25617a4beba4005f65f7dd4ddfb9654f79e46e49c6b39719fcc1cbeb92d1dc74c3c,
66sourcehashes; APIs/ABIs/nativos/plugin/host conferidos. Hostinstall-r,3snapshots
ProvisionOnly/hashesconfirmed, perfilprivado GTKvazio/índice08 confirmado; Activity
relançada. Listener33664mantido. Validação integral10015REPROVADA físicos/race/auditoria.
Próximo: teste físico do usuário de nova jornada em claro; se erro, ler comando/status
real do log. Não dizerPIN físico validado; GIX históricoPIN08ausente ainda relevante.
Não executar compra automaticamente nem encerrar Bridge33664.
As seções abaixo são histórico; esta resposta supersede a exigência DATA30 atual.

HISTÓRICO: [10014 — preservar dados para autorização](../10014-preservar-dados-autorizacao/spec.md).
Usuário esclareceu em 14-out que GTK/objeto com informações é OBRIGATÓRIO para
o futuro autorizador. Hipótese de retirar GTK foi rejeitada antes de qualquer
alteração de fluxo. Manter 03/06 GCX→GTK→GOX e 00/05 GCX→GTK→GPN.
Lacuna comprovada: PaymentTracks limpava toda resposta GTK imediatamente.
10014 retém GTK/PIN block/KSN/GOX EMV no contexto privado da operação e oferece
PaymentAuthorizationSnapshot no pacote interno Go, getters por cópia/Clear.
Futuro consumidor solicita após GPN/GOX e antes de FCX/close, limpa snapshot e
cópias; nenhuma chamada ao autorizador ou nova API pública gomobile nesta Change.
Implementação concluída e revisão IMPLEMENTACAO_APROVADA. Goall/vet/integration
exit0; cinco cenários modos/decisão e14cenários Bridge sem perdaGTK. Arquivo novo
98,72%, focal82,07%, arquivos pagamento90,51%. AAR regeneradoSHA256
a9c9fc6e7c6b5300bda61d8a0c2072967bf3ea5eb8a593c294fb8ba9ec200b47,66sourcehashes.
Android gatesPASS6s (30JVM UP-TO-DATE/Kotlin sem alterações); instrumentação
PASS33s/16executados/0falhas. Plugin/host/nativos conferidos. Host atualizado
install-r, ProvisionOnly3fontes/hashesconfirmed, Activity relançada. Bridge33664
mantido. Validação integral10014REPROVADA físico/race/auditoria. Ler validation.md.
Último teste do usuário: correlação3b46a9fb-027c-4959-ac42-0b595e5dbafd, R$223,33,
GCX000→GTK011→CLO000, nenhum GOX/GPN. Arquivos externos mantidos, métodoDATA30/
índice08 e PIN3/08/WK vazia não alterados; GIX anterior ausência de chaves08.
Pergunta pendente: confirmação da carga DUKPT DATA08 pelo responsável pelo pinpad.
Não alegar que retenção resolva GTK011. Não executar nova compra física automaticamente.
As seções 10013 abaixo registram a entrega anterior, não o ponto atual.

HISTÓRICO: [10013 — retorno PIN criptografado](../10013-retorno-pin-criptografado/spec.md),
SPEC_APROVADA em 14-out. Usuário quer PIN block+KSN na integração Go→Android
privada e log da aplicação; autorizou explicitamente valores criptografados em
log DES privado. ADR da10013 limita a noBackupFilesDir/debug/opt-in, nunca
Logcat/Bridge/UI/PAN/PIN em claro/WK. Reforçou três campos sempre do arquivo,
nunca fixos; retirar WK literal do adapter via objeto tipado do parser Go.
Código da10013 implementado: mobile/payment_result.go (PaymentResult, APIs
CapturePaymentPINResult/ContinuePaymentEMVResult, ParsePaymentPINConfiguration);
core CapturePaymentPINResult aditivo; wrapper antigo limpa; Kotlin recebe três
getters da fonte (WK literal removida), logPinResults boolean opt-in e logger
noBackup64KiB. TestsGo all/vet/integration PASS; sete cenários x APIs novas/antigas,
incluindoGTK011. AAR regenerado com SHA256
3fd298841c1cffdfff7b595bb65051108a593dd9d8da115f373da65bf747bc79.
PONTO ATUAL: entrega10013 concluída e revisada IMPLEMENTACAO_APROVADA; Gradle
session30681 terminou PASS38s e instrumentação43862 PASS33s:30JVM/16instrumentados,
lint/build/cobertura97,33%. Sourcehashes65/nativos/API/hostplugin conferidos.
Perfil externo logPinResults:true habilitado (agora338bytes); só flag/formatação
mudaram, nenhum método/índice/PIN source alterado. Host final instalado -r,
três snapshots provisionados com hash e Activity relançada. Listener33664 mantido.
Log da aplicação: no_backup/saque-cartao/pin-results.jsonl relativo ao sandbox
host; debug/DES/opt-in,64KiB. Só escreve par válido real, não existe captura
real bem-sucedida neste marco. Não imprimir log criptográfico real em tools/chat.
Validação integral10013 REPROVADA por gate físico/race/auditoria independente.
Ler validation.md/reviews da10013 para comandos/exits, não repetir gates sem motivo.
Não repetir compra física; chaves08 ainda não confirmadas/carregadas por operador.

Em 14-out, usuário relatou regressão na preparação após a modificação e autorizou
criar/alterar SPECs e corrigir. Nova [Change 10012](../10012-provisionar-pin-no-bridge/spec.md)
trata o caminho BAT/reverse/provisionamento; consultar suas revisões e validação
antes de retomar. A revisão anterior da 10011 não cobriu esse entrypoint legado.

O usuário autorizou implementar as correções da SPEC aprovada e, em seguida,
revisar todas as implementações e a arquitetura envolvidas, documentar na SPEC
e registrar o diretório externo usado pela aplicação. Continuar do ponto atual;
não reiniciar a investigação nem tratar os testes sintéticos como hardware.

## Regras e contrato

- Seguir AGENTS.md, workflow e skill base Android. proposal/spec permanecem
  `SPEC_APROVADA` pela REV-001. tasks registra implementação entregue.
- 03/06: GCX → GTK → GOX; PIN/CVM pertence ao kernel EMV. FCX só quando GOX exige online.
- 00/05: GCX → GTK → GPN; PAN explícito privado no Go, não no Android.
- Compra usa byte 00h; crédito/débito DES usa filtro 01/02 e candidatos completos.
- DATA GTK é independente de PIN; não trocar método/índice para contornar chave ausente.
- Não instalar chaves, acessar conteúdo PRD, executar compra física automaticamente,
  desativar redaction, expor PAN/trilhas/WK/PIN/KSN ou arquivar sem aprovação formal.

## Diretório fornecido pelo usuário

`D:\desenvolvimento\chave_des` contém, conforme inventário reafirmado em 2026-10-14:

| Arquivo | Papel conhecido / limite |
| --- | --- |
| `informacoes_critografia_pinpad.properties` | Fonte PIN da 10011: TIPO_CHAVE, INDICE_CHAVE, WORKING_KEY; validar sem publicar conteúdo. |
| `perfil_saque_cartao_des.json` | Perfil DES de endpoint/prazos/tabela/DATA/limite/cenário; contrato e provisionamento documentados em evidence/configuracao-externa-des.md. |
| `emv_des.json` | Tabela externa DES de homologação: 36 registros; candidatos crédito=8/débito=7; validação estática Go PASS; preservar registros/AIDs/identidades. |
| `chave_des.properties` | Credenciais gerais PostgreSQL/OIDC/API. Referenciado pelos geradores de launcher gerenciartarefas, fotogaleria e autenticadorsso; não é a fonte PIN desta jornada. Não copiar valores. |
| `emv_prd.json` | Presença informada pelo usuário; conteúdo fora do escopo de homologação DES e ainda não acessado. |

Tamanhos informados: chave_des.properties 2639; emv_des.json 17773;
emv_prd.json 11603; informacoes_critografia_pinpad.properties 53;
perfil_saque_cartao_des.json 346 bytes. Não fixar esses tamanhos como contrato.

## Incidente de preparação — 2026-10-14

- Tela: CRYPTO_MISSING, fase preflight. Arquivo PIN ausente no host debug,
  comprovado por ADB; perfil/tabela privados eram de 11-out. O core não chegou
  a chamar pinpad, portanto não era evidência de falha GOX/GTK/COM.
- Entry point usado: apps/desktop/libpinpadabecsgo/testar_bridge_pinpad.bat;
  helper preparar_emulador.ps1 fazia somente reverse. 10011 tornou PIN obrigatório
  sem integrar esse caminho antigo ao provisionamento: lacuna corrigida na 10012.
- Ambiente informado e observado: COM7, baud 19200, timeout 30s, TCP39100,
  emulator-5554/API35, log apps/desktop/libpinpadabecsgo/logs/LogPinpadAbecs.txt.
  Listener 127.0.0.1:39100 PID33664 observado em 14-out; PID é histórico.
- Novo modo `iniciar-saque-cartao.ps1 -ProvisionOnly` valida/provisiona três
  fontes no host já instalado sem COM/Java/Go, APK/Bridge/Activity/OPN/compra.
  `-ProvisionOnly -ValidateOnly` não escreve. Fontes default são as externas
  acima; overrides PINPAD_PERFIL_PATH/TABELA_PATH/CRIPTOGRAFIA_PATH.
- BAT agora chama `preparar_emulador.ps1 -ProvisionarPagamentoCartao`;
  helper default continua reverse-only. Falha avisa pagamento não preparado,
  mas permite Bridge somente Windows. Não iniciar novo listener se já ativo.
- Recuperação atual: três snapshots provisionados no host com hashes;
  oito cenários de teste passaram, fonte inválida invalida PIN e finally restaura
  fonte DES válida. Listener PID e contagem serial permaneceram iguais.
- UI atual ao abrir host: login. Não ler/usar credenciais de chave_des.properties;
  usuário deve entrar e abrir Pagamento com Cartão. Em tela já com falha, usar SAIR
  e entrar novamente para novo owner/preflight; não existe recarga/replay automático.
  Usuário entrou e chegou à captura: OPN/cargaEMV/GCX passaram, GTK011 falhou,
  CLO000 fechou, correlação723838aa-a190-44c8-86aa-ea6b549d4f1a às00:59.
- Status normativo:010 ST_INVCALL,011 ST_INVPARM (PDFp30). Não confundir termos.
  PayloadGTK usa método30, tracks0111, DATAidx08 do perfil; nenhuma GOX/GPN
  nessa tentativa. Sequência e formato conformes não provam configuração válida
  da rede/dispositivo; não ignorar GTK ou escolher índice por tentativa.
- GIX diagnóstico físico OPN/GIX/CLO às01:02: map8035 PIN08=0 (ausente),
  PIN carregados02/07; map8036 DATA08=0, nenhumaDATAcarregada; statusGIX000,
  close confirmado. 0 significa ausente,2 significaria posição não suportada.
  Não afirmar que08 esteja fora da faixa com base nesse mapa. Causa exata011
  ainda precisa confirmação do fornecedor; chaves ausentes são pré-condição
  criptográfica não atendida. Arquivo3/08/WK vazia define seleção, não injeta chave.
- Usuário reafirmou TIPO_CHAVE3/INDICE_CHAVE08/WK aspasvazias e DUKPTTDES.
  Manter snapshot; não trocar para02/07 nem carregar chaves automaticamente.

## Trabalho implementado

- Go: parser PIN integral, candidatos AID/modalidade, PAN de trilhas incompletas,
  contexto por serviço/sessão/operação, APIs aditivas de pagamento, diagnóstico
  sanitizado, fallback e fechamento confirmado. GCX passou a ler tags 8041/8042/8043.
- APIs mobile em `mobile/payment.go`: ValidatePaymentPINProperties,
  PaymentPINConfigurationSummaryJSON (método/índice apenas), ValidatePaymentCandidatesJSON,
  CapturePaymentCardSummaryJSON, GetPaymentTracksSummaryJSON,
  ContinuePaymentEMVSummaryJSON, CapturePaymentPIN.
- Android: modo tipado, GTK antes de PIN/EMV, configuração PIN privada importada,
  leitura UTF-8 limitada/estrita, erro principal preservado e close sem Cancel incondicional.
- Launcher: CriptografiaPath, parser compartilhado `payment-pin-config.ps1`,
  provisionamento privado com hash e invalidação de snapshot anterior em falha.
- Testes Go, integração Bridge sintética, JVM e preflight Android atualizados.
- AAR/source jar/proveniência, plugin/host APK e Bridge local recompilados.
- README Go, implementation-plan e handoff para revisão registrados.

## Evidências já obtidas

As evidências e hash desta seção são históricos da10011/13-out. Consulte o
bloco ATIVO acima e validation.md da10013 para o artefato atual e gates novos.

- `go test ./...` e `go vet ./...`: PASS após as últimas alterações Go.
- Integração com Bridge opaco: modos 00/03/05/06, GOX 0/1/2 com PIN online dentro
  de GOX e GTK 042 interrompendo continuação: PASS.
- Cobertura dos quatro pacotes focais com integração: command 90,0%; parser 89,6%;
  service 82,0%; mobile 62,3% (fachada inteira legada). Agregado final 81,04%;
  cinco arquivos Go novos 88,53%; não afirmar 80% no pacote mobile legado inteiro.
- Android: 30 unitários, lint, cobertura JVM 97,31% e builds: PASS. 15 instrumentados
  no emulator-5554: PASS no APK final em 2026-10-13, connectedDebugAndroidTest exit 0.
  Testes standalone com dados sintéticos; host/login/menu/pinpad físico pendentes.
- PowerShell: 13 casos sintéticos do parser e sintaxe do launcher: PASS.
- Race: tentativa Go amd64/CGO falhou porque gcc não existe no PATH; nenhum dos
  caminhos MinGW comuns existe. Não declarar race aprovado nem instalar upgrade.
- Validador estrutural direto identifica local.properties preexistente e ignorado
  pelo Git. Projeção dos arquivos estruturais versionáveis sem esse arquivo: PASS.
- AAR mais recente: SHA256 `5cb7f34e6fa2d1bc015015ee808f79049733fc6dc5e8d3340d1f292ebf79d666`.
  Gerado pelo script da evidência usando Go 1.26.5 amd64 portátil, JDK 17.0.11,
  NDK 28.2.13676358, API 29 e quatro ABIs. 64 sourceHashes conferidos; nativos
  AAR→merged e stripped→APK conferidos; plugin igual ao asset embutido no host.
- Bridge: `apps/desktop/libpinpadabecsgo/build/10011/libpinpadabecsgo-bridge.exe`.
  Não foi iniciado no hardware; processos Bridge existentes não foram encerrados.

## Ponto exato de retomada

O bloco ATIVO AGORA10016 no início supersede esta lista histórica10011/10013.
Host atual usa descoberta GIX8035/sorteio único de índicePIN por sessão; GTK em claro.
Próxima ação: teste físico de jornadaPIN pelo usuário. Reteste14-out22:12 já validou
abertura/GIX/sorteio/cache/CLO; não repetir consulta sem novo motivo ou fixar02.
Não voltar a fixarPIN08: índice do arquivo é legado validado nesta API; método/WK
continuam da fonte. GIX retorna índices carregados, não valores secretos de chaves.
Não removerGTK, escolher02/07 ou desativar redaction. LogsPIN privados autorizados
permanecem como10013; não registrar trilhas/PAN. Revisões/evidências atuais10016.

Lista abaixo é histórica da10011, não instrução para repetir os gates atuais:

1. Trabalho de código, revisão de arquitetura, documentação e memória concluído.
   Ler reviews/2026-10-13-implementation-review.md: IMPLEMENTACAO_APROVADA para
   seguir à validação; nenhuma divergência adicional de código encontrada.
2. validation.md atualizado com VAL-010..024, comandos/ambiente/exits, cobertura e
   artefatos. Estado integral REPROVADA por gates materiais pendentes, não por
   falha dos testes técnicos que passaram. Não promover para VALIDADA/APROVADA.
3. Próximos gates: host/login/menu e homologação física DES (incluindo AIDs,
   chip/CTLS/tarja, PIN, fallback, close/ownership); race em ambiente CGO+C;
   auditoria formal independente HIGH_ASSURANCE. Não repetir baseline/build
   sem novo fonte/falha/artefato. Revalidar hashes após qualquer alteração Go.
4. Inventário externo e arquitetura estão ligados na SPEC. Dois geradores de
   backend têm divergência de nomes perante chave_des.properties; observar em
   evidence/configuracao-externa-des.md e tratar em Change própria, sem ampliar 10011.
5. Aprovação formal, atualização de system, commit e archive continuam pendentes.
   Atualizar esta memória a cada marco e continuar daqui.

## Ambiente e cautelas práticas

- Workspace `D:\desenvolvimento\ia\aula02`, PowerShell/Windows.
- Go padrão: 1.26.5 windows/386. Para AAR, Go amd64 em
  `%TEMP%\saque-cartao-go-1.26.5-amd64\go`; JDK `C:\Desenvolvimento\jdk-17.0.11`.
- Gradle: JAVA_HOME JBR em `D:\desenvolvimento\ferramentas_android\Nova pasta\AndroidStudio\jbr`;
  SDK `D:\desenvolvimento\ferramentas_android\Sdk`. Wrapper compila alvo Java 17.
- Go build/test em `apps/desktop/libpinpadabecsgo`; Gradle na raiz Android.
- PowerShell não expande glob de arquivos para gofmt: passar array de caminhos.
- Montar argumentos nativos em variáveis/arrays; uma concatenação inline de ldflags
  já causou erro de build e foi corrigida. Bridge é compilado sem esse flag desnecessário.
- BOM em PowerShell exige StringComparison.Ordinal; comparação cultural ignorava
  BOM e foi corrigida com testes. Nenhuma fonte real foi copiada para fixtures.
- Árvore contém alterações desta tarefa e a Change 10011 não rastreada desde antes;
  não descartar arquivos, fazer commit/archive ou sobrescrever instruções do projeto.
