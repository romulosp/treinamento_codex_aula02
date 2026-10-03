# Análise de consolidação — 066 + 067 + 070 + 071 + 072

## Status

`CONSOLIDAÇÃO DOCUMENTAL — EM_REVISAO_SPEC`

Esta análise foi feita antes da redação da SPEC consolidada. Ela não altera,
reabre nem reescreve os artefatos arquivados.

## 1. Responsabilidade de cada Change

| Change | Responsabilidade original | Conteúdo absorvido pela baseline |
|---|---|---|
| `066-lib-pinpad-abecs-go` | Biblioteca Go ABECS v2.12, serial, protocolo, comandos, estados, fila, sessão, logging, segurança, fachada e REST local. | Core completo, contratos de comandos, framing/CRC/TLV, segurança ABECS, lifecycle, testes físicos e adaptador REST. |
| `067-android-emulator-transport-bridge` | Separar o core do transporte e criar a ponte TCP local entre Android Emulator e serial Windows. | `Transport`, `EmulatorTransport`, PBRG v1, Bridge loopback, ownership, configuração TCP, fachada gomobile e gate AAR. |
| `070-bridge-log-android` | Fazer o Bridge usar o mesmo arquivo/tracer do transporte serial e do desktop. | Destino único, append, inicialização antecipada, erro de preparação e compartilhamento com transporte físico/scripted. |
| `071-bat-teste-modulo-windows` | Fornecer BAT manual de teste do Bridge físico Windows. | `setlocal`, diretório do módulo, `go run` do Bridge, ambiente e log visíveis, janela legível e exit code preservado; COM fixa original foi corrigida pela 072. |
| `072-corrigir-abertura-bridge-android` | Corrigir configuração efetiva, readiness, reverse, estado Android, erros, logging real, cleanup, mutex Windows e reabertura. | Precedência final para `PORTA_PINPAD`, readiness após bind, Ping sem COM, estados fiéis, categorias de erro, cleanup aguardado e reabertura segura. |

## 2. Sobreposições

| Tema | Changes sobrepostas | Síntese |
|---|---|---|
| Transporte | 066 e 067 | 066 define a porta serial e o protocolo; 067 introduz uma porta neutra e o transporte TCP. A fronteira final é `Transport`, com ABECS opaco fora do Bridge. |
| Configuração | 066, 067 e 072 | 066 define defaults de serial; 067 acrescenta host/porta TCP; 072 define a origem efetiva e impede sobrescrita física pelo launcher. |
| Lifecycle | 066, 067 e 072 | 066 define `CLOSED/OPEN/BUSY/DESYNCHRONIZED` e CAN/EOT; 067 define sessão PBRG; 072 exige encerramento ordenado antes da reabertura. |
| Logging | 066, 070 e 072 | 066 define `SPE/PP/RSP` e redaction; 070 centraliza o tracer; 072 exige eventos reais de Bridge e falhas observáveis. |
| Erros | 066, 067 e 072 | 066 tipa erros Go; 067 define erros de infraestrutura do Bridge; 072 fixa categorias públicas, fases e correlação. |
| Testes | todas | Fakes cobrem componentes puros; integração cobre processo/stream; Android prova binding/lifecycle; hardware prova comunicação e display. |
| Launcher | 066, 071 e 072 | 071 define o BAT manual; 072 substitui a atribuição de COM fixa, disciplina ADB opcional, readiness e código de saída. |

## 3. Requisitos corrigidos posteriormente

### 3.1 Configuração da COM

067 permitia que o entrypoint consumisse `PORTA_PINPAD` conforme a configuração
do core. 072 fixa o comportamento operacional posterior:

- o processo Windows é a única origem da COM;
- o BAT físico herda `PORTA_PINPAD` e falha se ela estiver ausente, vazia ou
  composta apenas por espaços;
- a 074 supersede explicitamente o fallback direto da 066/067/072 e exige a
  variável também no entrypoint direto;
- ausência, vazio, espaços ou valor inválido falham, sem fallback silencioso;
- nenhum launcher, modelo ou entrypoint escolhe COM fixa;
- a mesma porta efetiva alimenta serial e ownership;
- Android recebe host/porta TCP, nunca a COM.

A primeira versão da 071 atribuía COM fixa no BAT. Sua própria retificação
histórica aponta a 072 como contrato final: a atribuição está superseded;
`setlocal`, lançamento no módulo, modo físico, configuração visível e janela
legível permanecem válidos na seção 4.3 da 074.

### 3.2 Readiness

070 tratava a criação/ativação do arquivo como parte relevante do startup.
072 corrige a inferência: `bridge_listening` só pode ocorrer depois de
`net.Listen` bem-sucedido e significa apenas que o servidor está disponível.
Não significa COM aberta, OPN aceito ou sessão ABECS aberta.

### 3.3 Ping e estado

072 corrige a interpretação de Ping, Version e Estado: Ping comprova apenas
alcançabilidade do Bridge; não adquire ownership, não abre COM, não envia ABECS
e não muda a sessão para `OPEN`. A UI e a fachada devem exibir o estado
confirmado pelo core.

### 3.4 Logging real

070 exigia um destino comum. 072 substitui a inferência de sucesso baseada em
arquivo criado por eventos reais: startup, readiness, Ping, ownership, falha de
serial, sessão, TX/RX redigidos, fechamento e parada. Erros de callbacks,
cleanup e goroutines não podem ser descartados.

### 3.5 Cleanup e reabertura

067 definia a liberação da sessão; 072 acrescenta a obrigação de fechar socket,
serial e ownership, cancelar e aguardar workers e somente depois permitir
reutilização. A falha fatal de leitura serial deve ser notificada mesmo com o
peer ocioso em `ReadFrame`.

### 3.6 Mutex Windows

072 corrige a suposição de afinidade de goroutine: adquirir e liberar mutex
nomeado deve ocorrer no contexto de thread exigido pela API Windows. Mutex
abandonado não é `BUSY`, não gera replay e deve resultar em falha controlada,
com a posse liberada quando aplicável.

## 4. Requisitos superseded ou obsoletos

| Requisito antigo | Estado final consolidado | Motivo |
|---|---|---|
| 066 inicial: biblioteca sem qualquer servidor/rede | Superseded pela inclusão do adaptador REST local e, separadamente, do Bridge PBRG loopback. O domínio continua sem depender de rede. | O aditivo REST de 066 e 067/072 introduzem adaptadores externos sem contaminar o core. |
| `RST` como comando, DTO ou operação ABECS | Obsoleto como comando. Reset físico/lógico usa CAN/EOT. A rota REST histórica `/connections/current/resets`, se mantida por compatibilidade, é apenas alias de CAN/EOT e nunca serializa `RST`. A fachada mobile não expõe RST. | A conformidade ABECS v2.12 de 066 remove RST. |
| `SendGCXInitialization`, `skipInit`, `UseGCXInitialization` e `GCXInitTimeout` | Removidos. `PurchaseGCX` envia exatamente um GCX parametrizado. | Correção normativa de 066: o manual não define GCX preliminar. |
| Arquivo criado como prova de fluxo Android/Bridge | Removido como critério suficiente. | 072 exige eventos reais e correlação de sessão. |
| `OPEN` após consulta local, Version ou Ping | Removido. Só `Open` concluído com OPN confirma `OPEN`. | Correção de estado de 072. |
| Fallback automático de outra COM, host, porta ou transporte scripted durante fluxo físico | Removido. | 072 exige falha explícita e preservação da configuração efetiva. |
| 071 original: atribuição de `PORTA_PINPAD` com COM literal no BAT | Removida. O BAT herda, valida e preserva a variável do operador. | 072 é correção posterior explícita. |
| Log relativo ao diretório de trabalho como default | Removido. Default é absoluto, derivado da raiz do módulo Go. | 066/070/072 convergem para um único destino canônico. |
| UI Android como responsabilidade de 067 | Continua fora do núcleo de transporte; o laboratório Android existente pode consumir o binding, mas sua UI não redefine o contrato Go/Bridge. | Escopo original de 067. |

## 5. Contradições encontradas e resolução

### C-001 — “Sem rede” versus REST e Bridge

066 começa descrevendo uma biblioteca sem servidor, mas o aditivo REST da mesma
Change exige `cmd/libpinpadabecsgo-api`; 067 exige o Bridge TCP. A resolução é
arquitetural: domínio, aplicação ABECS e porta `Transport` não conhecem HTTP,
TCP, Android ou COM. REST e Bridge são adaptadores de entrada/infraestrutura
separados. Android usa Bridge, não REST.

### C-002 — RST presente na matriz REST versus exclusão normativa

Partes de 066 ainda listam DTO/rota de RST, enquanto a conformidade posterior
remove RST do catálogo e da fachada. A resolução canônica é não possuir
comando RST. Uma eventual rota REST de reset só pode ser compatibilidade
semântica para CAN/EOT e deve ser marcada como alias, nunca como comando.

### C-003 — `PORTA_PINPAD` obrigatória em todos os entrypoints físicos

A 072 exigia ambiente no BAT físico e ainda preservava fallback no entrypoint
direto. A 074 adotou uma regra mais estrita e ela foi aprovada explicitamente
pelo usuário em `reviews/2026-09-29-approval.md`: não existe COM fixa nem
fallback em launcher, `DefaultConfig`, `config.Load()` ou entrypoint físico.
Essa é supersessão consciente da fonte, não uma atribuição incorreta à 072.

### C-004 — Log criado versus log alimentado

070 define o destino comum; 072 acrescenta a condição de alimentação por
eventos. A criação é pré-condição, não critério de sucesso da comunicação.

### C-005 — Reconexão versus retry

O canal pode executar a recuperação CAN/EOT e, se necessário, uma reconexão
controlada. Isso não autoriza reenviar a operação cujo resultado ficou
indeterminado. O comando original nunca é repetido automaticamente.

## 6. Comportamento final escolhido

O comportamento final é o contrato que está reproduzido em `spec.md`:

1. o core Go tipado é a única fonte de verdade ABECS;
2. `Transport` é uma porta de bytes com lifecycle e contexto;
3. o Bridge apenas enquadra e encaminha bytes opacos;
4. ownership é exclusivo por sessão e por COM;
5. `Ping` testa somente o Bridge;
6. `Open` executa handshake Bridge, aquisição, abertura serial e OPN;
7. a sessão só fica `OPEN` após OPN confirmado;
8. `Close`, cancelamento, desconexão e falha liberam recursos em ordem e de
   modo idempotente;
9. `operationID` acompanha a operação Android e `correlationId` acompanha cada
   frame PBRG/log correlato;
10. o destino de log é único, append, absoluto e redigido;
11. erros públicos são categorias estáveis, com causa detalhada apenas no Go/
   log seguro;
12. nenhuma operação física possui retry automático;
13. recovery do canal não equivale a sucesso da operação interrompida;
14. hardware físico é obrigatório para declarar comunicação/display validados.

## 7. Divergências entre SPECs e código atual

As divergências abaixo são observações do workspace em 2026-09-29. Nenhuma foi
corrigida nesta Change.

| ID | Evidência atual | Divergência/impacto | Classificação |
|---|---|---|---|
| D-001 | A implementação observada em 2026-09-30 removeu a porta padrão de `DefaultConfig()` e fez `config.Load()` rejeitar ausência de `PORTA_PINPAD`. | Conforma-se à decisão C-003 aprovada na 074. Manter testes de ausência/vazio/inválido e mesma porta em serial/ownership. | Resolvida na implementação; validação formal pendente. |
| D-002 | `internal/api/handler.go` registra `/api/v1/connections/current/resets`; `internal/api/dto/dto.go` possui `Rst*DTO`. | A superfície HTTP ainda chama a operação de RST, embora o protocolo/fachada consolidada só permita CAN/EOT. Precisa ser explicitamente alias ou removida em implementação futura. | Importante. |
| D-003 | `internal/application/service/service.go:189` retorna `ErrNotImplemented` em `TransactionGCX`. | A implementação atual não cobre a transação GCX completa; a própria 066 reserva esse contrato para Change futura. | Lacuna conhecida, não resolvida nesta baseline. |
| D-004 | O módulo contém `mobile.aar`, mas o artefato é local/untracked e o build do laboratório usa `app/libs/libpinpadabecsgo.aar` gerado por script. | Há artefato AAR no workspace, mas a proveniência reprodutível não é parte do código versionado observado. | Importante para implementação/validação. |
| D-005 | O app `apps/frontend/smartphone/diagnosticopinpad` exige AAR local em `app/libs` e possui `local.properties`. | O validador estrutural Android falha antes dos quality gates por arquivo local potencialmente sensível; a execução do build não deve ser declarada a partir dessa checagem. | Limitação de ambiente. |
| D-006 | `go test -tags=integration ./...` falhou em `TestCloseAckVariants/legacy_eof` com `set bridge read deadline: io: read/write on closed pipe`. | O estado atual tem uma regressão de fechamento no transporte Emulator que precisa de correção/revisão antes de uma futura implementação ser considerada validada. | Bloqueante para validação atual. |
| D-007 | `go test -race ./...` informa `-race is not supported on windows/386`. | O detector de corridas não foi executado neste ambiente; a limitação deve permanecer explícita. | Limitação de ambiente. |
| D-008 | O código atual expõe uma fachada mobile ampliada, incluindo operações de catálogo e resumos sanitizados. | O contrato final deve preservar a fronteira simples e sem tipos internos/raw; qualquer ampliação precisa continuar compatível com gomobile e com redaction. | Requisito de revisão, não falha automática. |

## 8. Lacunas que exigem decisão

| ID | Lacuna | Decisão necessária |
|---|---|---|
| L-001 | A baseline inclui a integração do laboratório Android porque o objetivo é reconstruir o caminho Android→AAR, mas 067 originalmente exclui telas/UX. | Confirmar na aprovação humana que “Android” significa fachada/AAR e o laboratório consumidor, não um novo produto de UI. O contrato 074 mantém a UI consumidora fora do núcleo. |
| L-002 | REST 066 contém alias histórico de reset em tensão com a remoção de RST. | Aprovar explicitamente a regra “alias HTTP CAN/EOT, sem RST no domínio” ou decidir sua remoção em implementação. |
| L-003 | Comunicação segura ABECS depende de manual e confirmação física do perfil, embora o contrato fixe RSA 2048, expoente 65537, KSEC temporária e AES-CBC. | Implementação futura deve confirmar no dispositivo a revisão normativa, padding, IV e sequência antes de declarar o gate seguro. |
| L-004 | `TransactionGCX` completo permanece reservado em 066. | Confirmar que a baseline deve preservar o stub explícito e não inventar campos, em vez de ampliar o escopo desta consolidação. |

## Conclusão da análise

As cinco Changes podem ser representadas por uma única baseline autocontida,
desde que a SPEC consolidada incorpore as regras finais acima e não trate links
para o archive como passos de execução. A baseline é suficiente para o
subsistema de transporte e integração Android/AAR; ela não transforma o
laboratório Android nem a transação GCX completa em requisitos implícitos que
as fontes não aprovaram.
