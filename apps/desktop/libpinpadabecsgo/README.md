# lib-pinpad-abecs-go

Biblioteca Go para comunicação serial com dispositivos compatíveis com ABECS 2.12.

## Escopo

Este módulo contém a biblioteca serial ABECS e o adaptador RESTful em `internal/api`, servido por `cmd/libpinpadabecsgo-api`. O executável `cmd/libpinpadabecsgo` permanece como ferramenta local interativa de diagnóstico; WebSocket e UI não fazem parte do contrato.

## Configuração

- `PORTA_PINPAD`: obrigatória; porta serial operacional fornecida pelo ambiente
  do processo, por exemplo `COM7` ou `/dev/ttyUSB0`. Não existe fallback de
  COM em `config.Load()`, no launcher ou no modelo padrão.
- `PINPAD_BAUDRATE`: opcional, padrão `19200`.
- `PINPAD_TIMEOUT`: opcional em segundos, padrão `30`.
- `PINPAD_HTTP_HOST`: host do servidor RESTful (padrão `127.0.0.1`).
- `PINPAD_HTTP_PORT` ou `PORT`: porta do servidor RESTful (padrão `8080`).
- `PINPAD_BRIDGE_PORT`: porta TCP local do Transport Bridge (padrão `39100`).
- `PINPAD_BRIDGE_HOST`: host do Bridge para o consumidor mobile (padrão
  `localhost`; no Android Emulator prefira `adb reverse` e use `10.0.2.2`
  somente como override explícito).
- `PINPAD_LOG_FILE`: habilita o rastro serial SPE/PP/RSP no arquivo indicado.
  Caminhos relativos são resolvidos contra a raiz deste módulo, nunca contra o
  diretório de trabalho. `start_aplication.bat` e o `executar projeto.bat` da
  raiz criam `logs/` e definem o caminho absoluto
  `<raiz-do-módulo>/logs/LogPinpadAbecs.txt` quando a variável estiver vazia.

O comando `cmd/libpinpadabecsgo-bridge`, usado pelo `diagnosticopinpad`,
configura o mesmo tracer e o mesmo arquivo `logs/LogPinpadAbecs.txt` antes de
aceitar conexões Android. O Android não cria uma segunda cópia do rastro ABECS
nem acessa diretamente o filesystem do Windows.

### Contrato corrigido do launcher físico — Change 072

O contrato abaixo segue a [SPEC da Change 072](../../../specs/changes/2026-09-27-072-corrigir-abertura-bridge-android/spec.md)
e seu [DESIGN](../../../specs/changes/2026-09-27-072-corrigir-abertura-bridge-android/DESIGN.md).
Ele descreve o comportamento exigido, sem atestar implementação concluída ou
sucesso de testes físicos.

Para testar um pinpad físico Windows, configure `PORTA_PINPAD` no ambiente do
processo e execute [`testar_bridge_pinpad.bat`](testar_bridge_pinpad.bat) na
raiz deste módulo. Não edite o BAT para atribuir uma COM: o valor deve vir da
variável do ambiente do processo, sem default universal.

```powershell
$env:PORTA_PINPAD = "<COM_DO_PINPAD>"
.\testar_bridge_pinpad.bat
```

O BAT deve herdar a COM sem sobrescrevê-la. Ausência, valor vazio ou somente
espaços devem interromper o teste com mensagem clara e código não zero antes
de abrir a serial. Valor presente inválido deve ser rejeitado. Os demais
valores válidos do processo devem ser preservados; apenas quando ausentes,
o launcher aplica estes defaults:

| Variável | Default do launcher | Unidade/finalidade |
|---|---|---|
| `PINPAD_BAUDRATE` | `19200` | baudrate serial |
| `PINPAD_TIMEOUT` | `30` | segundos |
| `PINPAD_BRIDGE_PORT` | `39100` | porta TCP local |
| `PINPAD_LOG_FILE` | `<raiz-do-módulo>/logs/LogPinpadAbecs.txt` | log compartilhado |

O launcher deve remover `PINPAD_BRIDGE_TRANSPORT` apenas no seu escopo
`setlocal`, selecionar o modo `physical` e usar a mesma COM no adaptador e no
ownership. Console e log devem informar configuração efetiva, unidades,
origem `environment`/`default` e modo, somente para as variáveis permitidas
pela SPEC. Terminais e IDEs já abertos podem ter ambiente antigo: reabra-os
após mudar variáveis persistentes. Um Bridge em execução não recarrega essas
alterações; encerre-o e inicie-o novamente.

O BAT pode atender o Android Emulator com pinpad físico. Para teste sem
hardware, inicie o entrypoint diretamente com `PINPAD_BRIDGE_TRANSPORT=scripted`;
o BAT físico remove essa seleção. Não há troca automática de COM, endpoint,
transporte ou retry de operação física.

### Preparação do Android Emulator

O helper `preparar_emulador.ps1` usa `-EmulatorSerial` e `-BridgePort`, com
seleção também por `ANDROID_SERIAL`. Sem outra opção, prepara somente reverse:

```powershell
.\preparar_emulador.ps1 -EmulatorSerial "emulator-5554" -BridgePort 39100
# Alternativa de seleção:
$env:ANDROID_SERIAL = "emulator-5554"
.\preparar_emulador.ps1 -BridgePort 39100
```

O retorno criptográfico da Change10013 é aditivo: `CapturePaymentPINResult`
e `ContinuePaymentEMVResult` devolvem `PaymentResult` privado com getters
`PINBlock`, `KSN`, `Result` e `Clear`. PIN online DUKPT exige8/10bytes; GOX
sem PIN online não fabrica par. APIs antigas de resumo/void mantêm descarte.
Integração deve limpar cópias e handle em finally; nenhum PAN na assinatura.
`ParsePaymentPINConfiguration` importa integralmente método/índice/WK da fonte,
incluindo normalização de aspas vazias; operação não recebe valores fixos.

Log solicitado em homologação: perfil externo `logPinResults: true`, apenas
DES/debug, arquivo privado Android `no_backup/saque-cartao/pin-results.jsonl`,
máximo64KiB. Contém PIN block/KSN criptografados após captura válida, nunca PIN
digitado/PAN/WK; não é Logcat nem log serial Bridge. Configuração de slot não
instala chave no pinpad. Diagnóstico GIX/GTK em14-out consta na Change10013.

O launcher deve procurar ADB no PATH ou SDK configurado. Com um único emulador
online, pode selecioná-lo; com vários, deve exigir seleção explícita. Não
seleciona dispositivos físicos automaticamente. A preparação deve executar
e verificar o reverse para a porta TCP efetiva, mostrando o serial usado:

```powershell
adb -s emulator-5554 reverse tcp:39100 tcp:39100
adb -s emulator-5554 reverse --list
```

Se a porta configurada for outra, substitua `39100` nos dois lados do
mapeamento e no cliente Android. A listagem deve conter esse mapeamento.
Repita a preparação após reiniciar o emulador; o helper deve limitar-se a essa
porta, sem remover outros reverses, instalar artefatos ou alterar a política
de execução persistente.

ADB ausente, emulador ausente/offline ou seleção ambígua devem produzir
`emulator_not_prepared` com orientação corretiva, mas não impedir o Bridge
host de iniciar. Reverse é opcional para o uso somente Windows. No Emulator,
prefira reverse com host `localhost`; `10.0.2.2` é apenas override explícito.

Desde a Change 10012, `testar_bridge_pinpad.bat` também solicita o provisionamento
de pagamento no host debug já instalado. O helper faz isso com
`-ProvisionarPagamentoCartao`. Exige as três fontes DES em
`D:\desenvolvimento\chave_des`: `perfil_saque_cartao_des.json`, `emv_des.json`
e `informacoes_critografia_pinpad.properties`. Overrides de origem:
`PINPAD_PERFIL_PATH`, `PINPAD_TABELA_PATH`, `PINPAD_CRIPTOGRAFIA_PATH`.
`chave_des.properties` contém credenciais de outros serviços e não substitui a fonte PIN.

Para recuperar `CRYPTO_MISSING` com Bridge já em execução, execute:

```powershell
.\preparar_emulador.ps1 -EmulatorSerial emulator-5554 -BridgePort 39100 -ProvisionarPagamentoCartao
```

Esse comando prepara reverse, perfil, tabela e PIN com hash; não inicia segundo
Bridge, instala APK/chaves ou abre compra. O launcher Android também aceita
`-ProvisionOnly`, que prepara os arquivos sem exigir COM, Java ou Go.
`-ProvisionOnly -ValidateOnly` verifica fontes/emulador sem escrita.
Depois de provisionar, saia da tela com falha e entre novamente no menu de
pagamento para novo preflight. Listener TCP e reverse prontos não comprovam
configuração privada, OPN ou chave DUKPT instalada no pinpad.
Reverse verificado não comprova listener, COM disponível ou sessão ABECS.

Antes de mostrar o menu ou iniciar o servidor, o executável imprime
`Log serial ativo: <caminho-absoluto>`. Esse é o único arquivo que deve ser
aberto para acompanhar a execução; um arquivo homônimo dentro de `cmd/` é um
artefato antigo e não é usado pela aplicação.

O rastro é desabilitado por padrão na biblioteca. O arquivo é criado em append
UTF-8, com uma linha de ativação imediatamente visível, não deve ser versionado
e só pode ser compartilhado após confirmar a
ausência de PAN, trilhas, PIN, PIN block, KSN, chaves e dados pessoais. OPN
seguro, MLR, TLR, GCX, GTK, GOX, FCX e GPN devem ser redigidos integralmente.
O contrato 072 exige redação conservadora também para chunks fragmentados ou
agregados: payloads desconhecidos devem ser omitidos, usando somente contagens
quando a redação não puder ser garantida. Bytes de controle isolados podem
permanecer visíveis; não se adiciona parser de negócio ao Bridge.

Cada retorno não vazio de `Adapter.Read` gera uma linha `PP`, inclusive ACK,
NAK, EOT e fragmentos de frame. Cada `Adapter.Write` confirmado gera uma linha
`SPE`; ambas informam o ponto de I/O e a data/hora, por exemplo:

```text
[COM7#001] SPE 16 47 49 58 30 30 30 17 12 34 CMD=GIX FUNC=serial.Adapter.Write DATA_HORA=2026-09-11T12:30:00-03:00
[COM7#001] PP  06 FUNC=serial.Adapter.Read DATA_HORA=2026-09-11T12:30:01-03:00
```

Na 072, o mesmo tracer e arquivo devem receber eventos reais de infraestrutura
e sessão, além de Open, TX/RX redigidos e Close: `bridge_starting`,
`bridge_listening`, `bridge_start_failed`, `client_connected`, `hello_ok`,
`ping_ok`, `acquire_requested`, `ownership_acquired`, `ownership_failed`,
`serial_open_failed`, `session_acquired`, `session_error`, `session_released`,
`client_disconnected` e `bridge_stopped`, conforme o fluxo. Eventos incluem
timestamp, fase, resultado/código, porta/modo e IDs disponíveis; bytes opacos
são descritos por contagens. Ping e falha de ownership devem alimentar o log
mesmo sem abrir serial, sem fabricar linhas TX/RX ou sucesso ABECS.

Eventos e erros devem ser limitados, sanitizados e sem dados sensíveis,
parâmetros de formulários ou payloads desconhecidos. O log privado Android
contém apenas metadados; não há segundo arquivo Windows. Sem processo Bridge,
a falha Android aparece na UI/log privado e não pode alimentar o arquivo do
host. Falha ao abrir o log deve aparecer no stderr com código não zero; falha
de persistência posterior deve ser visível e invalidar a sessão afetada quando
o rastro obrigatório não puder ser mantido.

## Execução da API RESTful

Para iniciar o servidor HTTP da API REST:

```bash
cd apps/desktop/libpinpadabecsgo
go run ./cmd/libpinpadabecsgo-api
```

O servidor escutará por padrão em `http://127.0.0.1:8080/`.

### Testes manuais e scripts auxiliares

Estão disponíveis dois utilitários de teste em `D:\desenvolvimento\ia\estudo\pinpad-abecs\`:
1. `run_pinpad_tests.bat`: menu interativo em lote para envio rápido de requisições `curl` para os principais endpoints (OPN, GIX, DSP, GCX e execução completa).
2. `tutorial_teste_restfull.txt`: catálogo completo com exemplos de requisições `curl` para os 25 comandos da biblioteca.

Exemplo de execução via curl:
```bash
# Obter informações do dispositivo
curl -v -X GET "http://127.0.0.1:8080/api/gix"

# Abrir conexão
curl -v -X POST "http://127.0.0.1:8080/api/opn" -H "Content-Type: application/json" -d '{"secure":false}'
```

## Multimídia no menu local

- A opção 16 carrega o arquivo com `MLI/MLR/MLE`; o prazo de transferência
  começa depois que o caminho e o nome forem digitados. O nome A8 tem oito
  caracteres ASCII alfanuméricos.
- A opção 17 envia `DSI` para uma mídia já carregada. O status `000` confirma
  que o firmware aceitou o comando; o menu lembra que a imagem precisa ser
  confirmada visualmente no display.
- A opção 26 lista as mídias com `LMF`; a opção 27 exclui nomes com `DMF`
  (separe vários nomes por ponto e vírgula).
- O fluxo ABECS envia os bytes originais do arquivo; Base64 não é usado e
  aumentaria o volume em cerca de um terço. `MLI`/`DSI` não recebem largura ou
  altura. Consulte as dimensões e formatos informados por `GIX` (opção 4 ou
  13); o próprio pinpad valida suporte e dimensões ao finalizar/exibir.
- Se uma resposta expirar ou não corresponder ao comando, o serviço tenta
  ressincronizar com `CAN/EOT`. Depois de três tentativas sem `EOT`, ele fecha e
  abre a porta uma única vez, exige um novo CAN/EOT e executa OPN antes de
  liberar a fila. O comando que expirou não é reenviado automaticamente. Se a
  reconexão também falhar, feche e abra explicitamente a conexão.

## Verificação

No diretório do módulo:

```text
go test ./...
go test -coverprofile="coverage.out" ./...
go tool cover -func="coverage.out"
go vet ./...
```

## Transport Bridge local

O Bridge pode ser iniciado em foreground com:

```text
go run ./cmd/libpinpadabecsgo-bridge
```

Antes de iniciar o Bridge físico, configure a porta do pinpad no processo
Windows:

```powershell
$env:PORTA_PINPAD = "COM7"
go run ./cmd/libpinpadabecsgo-bridge
```

O comando lê `PORTA_PINPAD` por `config.Load()` e usa o mesmo valor validado no
adaptador serial e no ownership. O bind permanece somente em `127.0.0.1`, na
porta `PINPAD_BRIDGE_PORT` (default `39100`). Ownership e serial são adquiridos
na abertura da sessão, não no Ping. O Bridge transporta bytes opacos para uma
sessão Emulator, não interpreta comandos ABECS e não substitui a API REST.

Pelo contrato 072, a indicação `bridge_listening` no console corresponde ao
evento de readiness do log, emitido somente após `net.Listen`/bind bem-sucedido,
com bind, porta, PID, transporte, COM efetiva e log ativo. Criar o log,
imprimir configuração ou chamar `go run` não comprova readiness. O launcher
deve validar Go/diretório, suportar caminhos com espaços e executar em
foreground. Porta ocupada ou erro de startup deve ser identificável, sem
matar processos nem escolher outra porta. O BAT deve capturar o código real
imediatamente, exibi-lo e devolvê-lo após `pause`, sem sucesso genérico em falha.

Listener disponível e `PONG` comprovam, respectivamente, servidor iniciado e
conectividade Bridge; nenhum deles equivale a `Open` ABECS. No Android, Abrir
com cliente fechado deve fazer Ping controlado no mesmo orçamento de timeout,
sem adquirir COM ou enviar ABECS. Somente `Service.Open` concluído, incluindo
OPN, confirma sessão aberta. Estado da ação, conectividade e sessão Go devem
ser distintos; Version/Estado `CLOSED` não habilitam comandos físicos.

Erros devem preservar código e correlationId, com mensagem pública segura.
A UI deve mostrar fase, código, mensagem, duração e ID final acima do catálogo,
com aviso acessível ou rolagem quando necessário. Falha da ação não define
sozinha o estado da sessão; queda ou timeout que a invalide bloqueia comandos
e exige nova abertura explícita. Consulte o
[README Android](../../frontend/smartphone/diagnosticopinpad/README.md) para
o fluxo de diagnóstico. Teste scripted não comprova abertura física;
CA-072-13 exige hardware, log e confirmação visual.

## Jornada de compra com chip, contactless e tarja

A Change [10011](../../../specs/changes/10011-corrigir-leitura-cartao-pinpad/spec.md)
adiciona APIs de pagamento que mantêm o contexto de cartão no core Go.
`CapturePaymentCardSummaryJSON` envia compra `00h`, habilita as três interfaces
e valida candidatos da modalidade na tabela carregada. O resultado contém
somente tipo, status ICC e referências de aplicação permitidos.

Depois da captura, `GetPaymentTracksSummaryJSON` consome GTK uma
única vez. Para chip `03` e contactless EMV `06`, use
`ContinuePaymentEMVSummaryJSON`: o kernel controla CVM e eventual PIN. FCX é
permitido quando GOX exige autorização online. Para tarja `00` ou contactless
simulando tarja `05`, use `CapturePaymentPIN`; o PAN explícito necessário após
GTK é extraído e mantido apenas no Go, sem argumento PAN no binding.

Todas as fases usam o mesmo operationID e snapshot de configuração. Nova
captura, uso repetido, troca de modalidade/valor/chave, correlação alheia,
cancelamento ou perda da sessão interrompem a jornada. Uma nova operação exige
encerramento e abertura explícita; falha de GTK impede a continuação.

A Change 10014 mantém GTK obrigatório pelo contrato da aplicação, apesar de
opcional no fluxo geral ABECS. O contexto privado Go retém a resposta GTK e
PIN block/KSN/resultado EMV até invalidação ou fechamento. A futura integração
Go com autorizador poderá usar `Service.PaymentAuthorizationSnapshot(ctx, id)`
após GPN/GOX e antes de FCX/close. O snapshot tem getters por cópia e `Clear`;
o consumidor deve limpar também as cópias. Não há chamada ao autorizador
implementada, nova API JSON/gomobile ou log de trilhas/PAN. GTK011 continua
dependente de diagnóstico/configuração física; retenção não troca método/chave.

Antes de abrir o dispositivo, `ValidatePaymentPINProperties` valida os campos
obrigatórios `TIPO_CHAVE`, `INDICE_CHAVE` e `WORKING_KEY`, com limite de 64 KiB.
O perfil aceita método `3`, índice N2 e WK vazia, inclusive o marcador `""`.
`PaymentPINConfigurationSummaryJSON` retorna somente método e índice após
validar a fonte inteira. A criptografia DATA de GTK usa configuração separada;
provisionar esse arquivo não instala nem comprova chaves no pinpad.

Na Change10015, o perfil DES externo escolhe GTK: `"gtkDataMethod":""` retorna
trilhas em claro, omitindo método/índice/WK do comando; `"gtkDataMethod":"30"`
seleciona DUKPT TDES DATA no `gtkKeyIndex` da fonte. O índice fica reservado no
perfil quando em claro. `00` significa MK/WK DES, não trilhas em claro.
Campo ausente/null/método inválido não ativa fallback. Reprovisione o perfil e
inicie nova jornada para mudar o modo. O snapshot interno identifica DATAMethod
e DATAKeyIndex efetivamente enviado (-1 quando ausente), sem expor cartão em JSON.
PIN continua seguindo o properties e é criptografado em ambos os modos GTK.

A Change10016 adiciona `Client.ResolvePaymentPINConfiguration` após Open para
a jornada DES. Consulta GIX8035(PIN DUKPT TDES), valida mapaA100 e sorteia
uniformemente entre todas as posições carregadas. A escolha é congelada por
sessão, sem nova tentativa em outro índice se GOX/GPN falhar. Ausência de chaves
retorna `PIN_KEY_MISSING:preflight`; mapa ausente/malformado é `PROTOCOL_ERROR`.
TIPO_CHAVE e WORKING_KEY continuam da fonte; INDICE_CHAVE é validado como campo
legado e o índice efetivo passa a vir do GIX nesta API. O arquivo não é alterado.
APIs antigas conservam o comportamento sem descoberta implícita. GIX não fornece
valores secretos ou associação de chave à rede; política de sorteio é de homologação
solicitada pelo usuário, não autorização financeira. GTK DATA é independente.

O launcher Android `scripts/iniciar-saque-cartao.ps1` aceita `CriptografiaPath`
e provisiona `criptografia.properties` apenas no armazenamento privado debug,
com conferência de integridade. Falha no preflight invalida o snapshot PIN
anterior no emulador selecionado; `ValidateOnly` não modifica o dispositivo.
O launcher continua exigindo os caminhos do perfil e da tabela DES.

O diagnóstico Go registra comando permitido, status N3, fase, tentativa e
correlação, mantendo frames redigidos e a ABI de erro `CODE:phase`.
As APIs genéricas conservam seu contrato; o uso de PAN vazio em GPN genérico
depende das pré-condições anteriores a GTK descritas pelo protocolo.
