# SPEC — 072 Corrigir abertura, estado e log Android/Bridge

## Status

`SPEC_APROVADA`

Revisão técnica: [2026-09-27-spec-review.md](reviews/2026-09-27-spec-review.md).
Revisão da porta por ambiente:
[2026-09-28-spec-review-porta-ambiente.md](reviews/2026-09-28-spec-review-porta-ambiente.md).

## 1. Precedência e invariantes

Esta SPEC complementa 067–070 e substitui, na 071, a sobrescrita de
`PORTA_PINPAD` e a indicação ambígua de sucesso do launcher. A implementação
existente é baseline de diagnóstico, não evidência de conformidade com 072.

O Windows consome a configuração COM; o Android usa host/porta TCP. O core Go
decide o estado ABECS. O Bridge mantém os bytes opacos e o layout `PBRG` v1.
Listener permanece `127.0.0.1`; default do cliente permanece `localhost`.
`10.0.2.2` continua somente override explícito. Nenhuma operação física recebe
retry, troca de endpoint ou fallback scripted automático.

## 2. RF-072-01 — Configuração efetiva Windows

O BAT `testar_bridge_pinpad.bat` deve usar `PORTA_PINPAD` herdada do processo,
sem atribuir qualquer valor fixo. Exemplos de COM são apenas
documentação temporal do ambiente, não defaults. Na execução física por esse BAT,
variável ausente, vazia ou só com espaços deve interromper a execução com
mensagem clara e código não zero, antes de abrir a serial.

O entrypoint direto continua usando a precedência de `config.Load()` da 067
(inclusive fallback existente quando a variável está ausente). O requisito
mais estrito acima pertence ao launcher de teste físico. Valor presente porém
inválido deve ser rejeitado, não convertido silenciosamente no fallback.

Baudrate, timeout, porta Bridge e destino de log definidos no processo serão
preservados quando válidos. Quando ausentes, defaults do launcher são `19200`,
`30` segundos, `39100` e `<raiz-do-módulo>/logs/LogPinpadAbecs.txt`.
`PINPAD_BRIDGE_TRANSPORT` será removida somente no escopo local do BAT, para
selecionar serial física. `setlocal` não modifica as variáveis persistentes do
Windows. A mesma COM efetiva será usada pelo adaptador e pelo ownership.

Console e log devem mostrar configuração efetiva, origem `environment` ou
`default`, unidades e modo `physical`/`scripted`. Só variáveis da allowlist
acima podem ser registradas. O README explicará que terminais/IDE já abertos
podem ter ambiente antigo e que a configuração não muda no processo em execução.

## 3. RF-072-02 — Inicialização e saída verificáveis

O launcher deve validar Go e diretório do módulo, usar caminhos entre aspas,
suportar caminhos com espaços e executar o Bridge em foreground. Não mata
processos existentes nem escolhe outra porta se houver conflito.

O Go só deve emitir `bridge_listening` após `net.Listen` ter sucesso. Esse
evento informa bind, porta, PID, transporte, COM efetiva e log ativo. Criar o
arquivo, chamar `go run` ou imprimir configuração não significa que o servidor
está pronto. `bridge_listening` significa apenas disponibilidade do servidor,
nunca sucesso de abertura ABECS.

Configuração inválida, falta de ferramenta, diretório inacessível, destino de
log inválido e porta TCP ocupada devem produzir erro identificável. O BAT
captura o código de saída imediatamente após o processo, o informa, mantém a
janela legível e devolve o mesmo código depois do `pause`. Não pode imprimir
sucesso genérico após falha de compilação ou bind.

Falhas Go após ativar o log também são gravadas nele. Se o próprio arquivo não
puder ser aberto, a causa será informada no stderr/console com código não zero;
não se promete registrar erro em um destino indisponível. Shutdown solicitado
fecha sessões/serial e registra `bridge_stopped` antes de fechar o tracer.

## 4. RF-072-03 — Preparação do Android Emulator

O launcher deve tentar localizar ADB no PATH ou SDK configurado. Quando houver
um único emulador online, configura `adb -s <serial> reverse` para a porta TCP
efetiva, verifica o resultado por `reverse --list` e mostra o serial usado.
Dispositivos físicos não são selecionados automaticamente.

Com múltiplos emuladores, exige seleção explícita por `ANDROID_SERIAL` ou por
parâmetro do helper documentado, sem escolher o primeiro. ADB ausente, nenhum
emulador, offline ou seleção ambígua devem informar `emulator_not_prepared` e
o comando corretivo; isso não impede o Bridge host de iniciar. O operador
poderá usar o módulo Windows sem Android.

Preparação poderá estar em helper PowerShell versionado, invocado pelo BAT,
com interface `-EmulatorSerial` e `-BridgePort`. Deve poder ser repetida após
reiniciar o emulador; limita-se ao mapeamento da porta configurada, não remove
outros reverses nem instala artefatos automaticamente. Scripts não alteram
política de execução persistente ou abrem janelas adicionais.

Reverse configurado não é prova de listener, COM ou sessão ABECS. Antes de
uma tentativa explícita de Abrir com cliente fechado, o app executa um Ping
controlado do protocolo Bridge, dentro do orçamento de timeout da ação, para
identificar falhas de conectividade. Ping não adquire COM nem envia ABECS.
Se falhar, a ação não tenta abrir serial e mostra orientação sobre Bridge,
host/porta e reverse. O app não executa ADB nem consegue afirmar, por uma
conexão recusada isolada, se faltou reverse ou processo host.

## 5. RF-072-04 — Log compartilhado alimentado por eventos reais

O único arquivo de diagnóstico Windows permanece o destino da 070, em append.
O mesmo tracer recebe eventos de infraestrutura e de transporte, com escrita
segura para concorrência. Não se cria arquivo alternativo Android para o
rastro Windows. O logger privado Android continua somente com metadados.

Eventos mínimos: `bridge_starting`, `bridge_listening`, `bridge_start_failed`,
`client_connected`, `hello_ok`, `ping_ok`, `acquire_requested`,
`ownership_acquired`, `ownership_failed`, `serial_open_failed`,
`session_acquired`, `session_error`, `session_released`, `client_disconnected`
e `bridge_stopped`. Use apenas os eventos aplicáveis a cada fluxo.

Cada evento possui timestamp, componente, fase, resultado/código, porta/modo,
sessionId quando criado e correlationId quando recebido. Falhas antes de
receber um ID indicam correlação indisponível, sem inventar ID de operação do
Android. Erros não podem ser descartados em `_ = s.handle(...)`, callbacks de
release, fechamento ou goroutines de encaminhamento sem diagnóstico.

O rastro serial preserva linhas de Open, TX/RX redigidos e Close existentes.
Falha de ownership deve deixar evento mesmo sem chamar `Transport.Open()`.
Ping gera evento técnico mesmo sem abrir serial. Não devem existir linhas
serial/TX/RX artificiais para satisfazer o teste de arquivo alimentado.
Teste de persistência verifica crescimento e conteúdo após cada evento, sem
depender de encerrar o processo. Mensagens de log devem limitar tamanho e
remover controles para impedir injeção de linhas.

PAN, trilhas, PIN block, KSN, chaves, EMV sensível, parâmetros de formulários e
payloads desconhecidos não aparecem em eventos ou erros. Como o Bridge é
opaco, eventos usam contagens de bytes. O rastro compartilhado deve preservar
ou reforçar a redação conservadora para chunks fragmentados/agregados sem
adicionar parser de negócio ao servidor ou confiar em strings de erro remotas.

Sem Bridge em execução, uma tentativa Android não pode alimentar um arquivo
de processo Windows ausente: erro aparece na UI/log privado Android. Falha de
persistência após startup deve aparecer no stderr, invalidar a sessão afetada
quando o rastro obrigatório não puder ser mantido e não resultar em sucesso
silencioso. Não registrar conteúdo potencialmente sensível ao relatar a falha.

## 6. RF-072-05 — Erros rastreáveis e sanitizados

Preservar o formato `ERROR code:message` do envelope atual, com códigos de
infraestrutura já suportados. Mensagem ao cliente será estável, limitada e
sem stack trace, caminho interno ou erro arbitrário de driver. Causa técnica
sanitizada permanece no log Windows. `ERROR` deve preservar o correlationId
recebido na solicitação; nunca transportar payload ABECS na mensagem.

O binding deve classificar erros locais e do Bridge sem perder o código na
fronteira gomobile; Kotlin usa categoria/mensagem segura, não apenas regex
sobre `Throwable.message`. Pode-se usar representação estável documentada de
erro na fachada, sem expor tipos internos ou alterar assinaturas operacionais
existentes. Mensagem desconhecida vira falha controlada e não é ecoada livremente.

| Fase | Categoria apresentada | Orientação ao operador |
|---|---|---|
| Conexão recusada/host sem resposta | `BRIDGE_UNREACHABLE` ou `TIMEOUT` | conferir BAT/listener, endpoint e reverse |
| Handshake inválido | `PROTOCOL_ERROR` | conferir versão/processo na porta |
| Ownership ocupado | `BUSY` | fechar a sessão concorrente, não trocar a COM |
| Falha do mecanismo de lock | `OWNERSHIP_ERROR` | consultar evento Windows correlacionado |
| Driver/Open/Read/Write | `SERIAL_UNAVAILABLE` | conferir COM efetiva/driver e causa local |
| Prazo expirado | `TIMEOUT` | informar fase e resultado não confirmado |
| Interrupção solicitada | `CANCELED` | informar cancelamento e estado confirmado |
| Queda de conexão | `DISCONNECTED` | exigir nova abertura explícita |
| OPN/ABECS rejeitado | `PINPAD_ERROR` | apresentar categoria/status público seguro |
| Uso de comando sem sessão | `PINPAD_CLOSED` | solicitar Abrir antes da ação |
| Erro não reconhecido | `BINDING_ERROR` | registrar metadados seguros para diagnóstico |

Essas categorias de apresentação não são novos messageTypes do PBRG. A causa
original da falha não deve ser substituída por falha secundária de cleanup.
Não há retry automático para erro ou resultado indeterminado.

## 7. RF-072-06 — Estado fiel e erro visível

Separar estado transitório da ação (`RUNNING`, `ERROR`, etc.), conectividade do
Bridge e estado confirmado da sessão Go (`CLOSED`, `OPEN`, `BUSY` ou equivalente
documentado). Disponibilidade de comandos depende da sessão confirmada, não
do sucesso de Ping, Version ou de uma consulta local de estado.

| Evento | Estado da sessão e efeito |
|---|---|
| Cliente novo / recriação do processo | fechada; sem retomada automática |
| Version | não modifica sessão/conectividade confirmada |
| Ping bem-sucedido com cliente fechado | Bridge alcançável; sessão continua fechada |
| Consulta Estado retorna `CLOSED` | UI apresenta fechado; não habilita DSP/GIX |
| Abrir conclui `Service.Open`, inclusive OPN | sessão aberta; ações dependentes habilitadas |
| Abrir falha | não apresenta aberta; preserva causa e permite nova tentativa explícita |
| Operação falha, sessão ainda válida | estado da ação é erro; sessão segue o estado real Go |
| Disconnect / timeout que invalida sessão | fechada ou desconhecida; bloqueia comandos físicos |
| Fechar | sessão fechada; resultado antigo não aparenta sucesso da próxima ação |

Tanto botões rápidos quanto ações do catálogo devem obedecer ao mesmo contrato.
Consulta de estado não abre conexão. Troca de endpoint fecha cliente anterior,
invalida conectividade e exige nova abertura. Cancelar/Fechar não são
desabilitados de modo a impedir liberação de recurso em uma falha.

Código, fase, mensagem segura, duração e ID da última operação devem aparecer
na área de estado anterior ao catálogo. A falha deve ser visível sem percorrer
28 opções; se o usuário estiver no fim, a UI deve apresentar aviso visível ou
rolar até o erro quando a ação termina. Preservar mensagem no fechamento de
diálogo/teclado e permitir acessibilidade sem depender somente da cor.
Limpar resultado anterior ao iniciar uma nova ação. Guardar ID final para
correlacionar o log, separado do ID de operação ainda em andamento.

## 8. RF-072-07 — Ciclo de vida, ownership e reabertura

O Bridge deve notificar falha fatal de leitura serial mesmo se o peer estiver
ocioso em ReadFrame. Fechar socket, serial e ownership, cancelar workers e
aguardar sua finalização antes de reutilizar o mesmo transporte em outra
sessão. Serial/TCP writes concorrentes devem preservar frames completos.
Cleanup será idempotente, documentado e não perderá erros de liberação.

O adaptador Windows de mutex nomeado deve adquirir e liberar no contexto de
thread exigido pela API Windows. A implementação não pode presumir que uma
goroutine mantém a mesma thread do SO. Resultado de mutex abandonado deve ser
tratado explicitamente: registrar abandono, não retornar falso `BUSY` nem
iniciar replay; liberar a posse adquirida e falhar de forma controlada para
uma nova tentativa explícita. A serial permanece sujeita ao resultado real
do driver. Detalhes e referências estão em `DESIGN.md`.

Teste entre dois processos Windows deve provar exclusividade, liberação por
Close/disconnect e reabertura. Não afirmar que CLI/API já participam do mesmo
lock sem evidência; concorrência com consumidor sem esse lock ainda deve gerar
falha real de driver bem diagnosticada.

## 9. Qualidade e critérios de aceite

GoDoc/KDoc em português para contratos alterados; DI simples e arquitetura
existente. Testes devem exercitar o defeito relatado, não apenas criação de
arquivo ou presença de strings no BAT. Cobertura mínima de 80% do código Go
novo aplicável, com inventário, escopo e medição; meta 90%. Não atualizar
toolchain em função desta correção.

| Critério | Comportamento verificável | Evidência prevista |
|---|---|---|
| CA-072-01 | BAT preserva qualquer valor válido herdado de `PORTA_PINPAD`; ausência/vazio falha | teste do launcher com ambiente controlado |
| CA-072-02 | defaults/unidades corretos, modo físico e config segura | testes de ambiente/configuração |
| CA-072-03 | readiness após bind; conflito/erro retorna código real | subprocesso + listener conflitante |
| CA-072-04 | reverse usa porta e emulador selecionado; ambiguidades são explícitas | ADB fake + ADB real |
| CA-072-05 | preflight Ping não adquire COM; falha deixa sessão fechada | Go/Kotlin + Emulator |
| CA-072-06 | mesmo log cresce em Ping, ownership/falha e Open/Close | integração Bridge/tracer real |
| CA-072-07 | ERROR mantém código/ID, UI não ecoa segredo e não esconde causa | erros injetados por fase |
| CA-072-08 | Ping/Version/Estado CLOSED nunca apresentam sessão OPEN | ViewModel e teste Compose |
| CA-072-09 | comandos físicos bloqueados sem Open; UI mostra erro acima do catálogo | botões rápidos e catálogo |
| CA-072-10 | read fatal, disconnect, cancel/close liberam workers/serial/lock | integração e reabertura |
| CA-072-11 | mutex Windows correto em duas execuções; abandono controlado | teste específico Windows multiprocesso |
| CA-072-12 | scripted prova Abrir → GIX → DSP → Fechar e reabrir | AAR/APK/Bridge e log compartilhado |
| CA-072-13 | porta física indicada por `PORTA_PINPAD` prova Abrir → GIX → DSP → Fechar e reabrir | ambiente efetivo, hardware, log e confirmação visual |
| CA-072-14 | build/testes Go e Android passam, artefatos instalados correspondem ao build | comandos, hashes e versões |
| CA-072-15 | anexos não viram instruções; docs e SPECs relacionadas são coerentes | revisão documental e rastreabilidade |

Sem CA-072-13 executado, a correção física permanece não comprovada. Falta de
hardware é pendência registrada, não sucesso scripted nem motivo para fechar
a change como resolvida. Gates formais seguem o workflow do repositório.
