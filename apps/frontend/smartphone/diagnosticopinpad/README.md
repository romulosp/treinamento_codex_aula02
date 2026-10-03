# diagnosticopinpad

Aplicativo Android nativo da Change 075 para exercitar o caminho:

```text
Compose -> AAR Go -> PBRG/TCP -> Bridge Windows -> transporte serial -> pinpad
```

## Catálogo implementado

O laboratório apresenta as 28 opções do menu desktop, agrupadas por função.
As opções 6 e 25 permanecem visíveis e desabilitadas conforme a SPEC. A opção
28 cancela a operação, fecha o cliente e só então encerra a Activity.

As operações 1-5, 7-24 e 26-27 usam métodos nomeados da fachada gomobile. A
fachada converte listas delimitadas em tipos do core Go; o Kotlin não constrói
comandos ABECS, CRC, framing ou JSON de comando.

GTK, GPN, GCX, GOX e FCX retornam apenas resumo sanitizado. PAN, trilhas, PIN
block, KSN, chaves, dados EMV e bytes raw não são colocados no estado Compose,
na saída ou nos logs.

## Pré-requisitos

- JDK 17 em `C:\Desenvolvimento\jdk-17.0.11`;
- Maven disponível em `C:\Desenvolvimento\apache-maven-3.8.8`; o build Android usa Gradle;
- Android SDK com API 37, Build Tools 36.0.0 e NDK 28.2.13676358;
- Go 1.26 host Windows **amd64** e `gomobile` (Go host 386 não é compatível com o NDK Windows x86_64);
- `adb` e Emulator API 37;
- Bridge compilado a partir de `apps/desktop/libpinpadabecsgo`.

O wrapper usa `gradle-9.6.0-bin.zip`. A URL `gradle-9.6-bin.zip` retorna 404 e
não deve ser usada.

## Gerar o AAR

```powershell
.\scripts\build-mobile-aar.ps1 -AllowDirty
```

O AAR fica em `app/libs/` (ignorado pelo Git). O script registra SHA-256 e
proveniência em `build/mobile-aar-provenance.json`.

## Teste no Emulator

O fluxo abaixo documenta o contrato corrigido da
[Change 075](../../../../specs/changes/075-diagnosticopinpad-consolidado/spec.md)
e a baseline [074](../../../../specs/changes/074-pinpad-android-bridge-stack/spec.md), que consolida a 072.
Não representa declaração de implementação concluída ou de validação física.

No host Windows, selecione explicitamente o emulador quando houver mais de um
online e prepare o reverse da porta TCP efetiva. Exemplo manual no diretório
do app:

```powershell
$sdk = "D:\desenvolvimento\ferramentas_android\Sdk"
$env:Path = "$sdk\platform-tools;$env:Path"
$env:ANDROID_SERIAL = "emulator-5554"
adb -s $env:ANDROID_SERIAL reverse tcp:39100 tcp:39100
adb -s $env:ANDROID_SERIAL reverse --list
.\gradlew.bat :app:installDebug
adb -s $env:ANDROID_SERIAL shell am start -n br.com.romulopenha.diagnosticopinpad/.MainActivity
```

A saída de `reverse --list` deve conter o mapeamento solicitado para o serial
selecionado. Se `PINPAD_BRIDGE_PORT` for diferente de `39100`, use essa porta
nos dois lados do reverse e no campo Porta do app. O default do laboratório é
`localhost`. No Emulator, use `adb reverse` para esse host ou informe
explicitamente `10.0.2.2` como override.
A porta Android é TCP, não COM. Reverse verificado não comprova listener,
disponibilidade da serial ou sessão ABECS aberta.

O helper disponível no módulo Go é `preparar_emulador.ps1`.
Execute no diretório `apps/desktop/libpinpadabecsgo`:

```powershell
.\preparar_emulador.ps1 -EmulatorSerial "emulator-5554" -BridgePort 39100
# Alternativa: seleção por ambiente.
$env:ANDROID_SERIAL = "emulator-5554"
.\preparar_emulador.ps1 -BridgePort 39100
```

O launcher deve localizar ADB no PATH ou SDK configurado, preparar e verificar
o reverse e mostrar o serial usado. Um único emulador online pode ser
selecionado automaticamente; vários exigem `ANDROID_SERIAL` ou
`-EmulatorSerial`. Dispositivos físicos não são selecionados automaticamente.
ADB ausente, emulador ausente/offline ou seleção ambígua devem informar
`emulator_not_prepared` e comando corretivo, mas o Bridge host pode iniciar
para uso Windows. Reverse é opcional nesse uso somente host.

Repita a preparação após reiniciar o emulador. O helper deve limitar-se ao
mapeamento da porta solicitada, sem remover outros reverses, instalar
artefatos ou alterar a política de execução persistente. O app não executa
ADB; conexão recusada isolada não permite distinguir reverse ausente de
Bridge host parado.

Para um teste sem hardware, no Windows Host execute no módulo Go:

```powershell
$env:PINPAD_BRIDGE_TRANSPORT = "scripted"
go run .\cmd\libpinpadabecsgo-bridge
```

O modo `scripted` serve aos cenários sem hardware e não comprova comunicação
física. Para pinpad físico, configure a COM no processo e execute o BAT no
módulo Go:

```powershell
$env:PORTA_PINPAD = "<COM_DO_PINPAD>"
.\testar_bridge_pinpad.bat
```

`PORTA_PINPAD` é consumida exclusivamente pelo processo Windows do Bridge. O
Android recebe apenas o endpoint TCP. O BAT deve herdar essa variável sem
atribuir COM fixa; não edite o BAT para escolher a porta. Ausência, vazio ou
somente espaços devem falhar com código não zero antes de abrir serial.
Valores válidos existentes de baudrate, timeout, porta Bridge e log devem ser
preservados. Somente quando ausentes, os defaults do launcher são `19200`,
`30` segundos, `39100` e `<raiz-do-módulo>/logs/LogPinpadAbecs.txt`.
`PINPAD_BRIDGE_TRANSPORT` deve ser removida apenas no `setlocal` do BAT para
selecionar `physical`, sem alterar variáveis persistentes.

Console e log devem mostrar configuração efetiva, origem `environment` ou
`default`, unidades e modo, dentro da allowlist da SPEC. Terminais/IDEs já
abertos podem ter ambiente antigo; reabra-os após mudanças persistentes e
reinicie o Bridge para carregar nova configuração. Todo entrypoint físico exige
`PORTA_PINPAD`; `config.Load()` não possui fallback para COM fixa.

A indicação Go `bridge_listening` corresponde ao evento do log somente
após bind bem-sucedido em `127.0.0.1`, com porta, PID, transporte, COM e log
ativo. Arquivo criado, configuração impressa e `go run` iniciado não provam
listener. Falha de compilação, configuração, log ou porta ocupada deve ser
visível; o BAT executa em foreground e deve exibir/devolver o código real
após `pause`, sem matar outro processo ou trocar de porta automaticamente.

## Sessão e erros — contrato 072

Abrir com cliente fechado deve executar um Ping controlado do protocolo Bridge
dentro do mesmo prazo total da ação. Ping não adquire COM nem envia ABECS; se
falhar, Abrir não tenta serial e deve orientar a conferir listener, host/porta
e reverse. Não há retry, troca de endpoint ou fallback scripted automático
para operação física.

Estado da ação, conectividade Bridge e sessão confirmada pelo Go devem ser
separados:

| Resultado | Sessão e comandos físicos |
|---|---|
| `PONG` com cliente fechado | Bridge alcançável; sessão continua `CLOSED` |
| Version ou consulta Estado `CLOSED` | Não abre sessão nem habilita DSP/GIX |
| `Service.Open` concluído, incluindo OPN | Sessão `OPEN`; habilita ações dependentes |
| Abrir falha | Não apresenta `OPEN`; permite nova tentativa explícita |
| Operação falha com sessão ainda válida | Ação em erro; sessão segue o estado real Go |
| Disconnect ou timeout que invalida sessão | Fechada/desconhecida; bloqueia comandos físicos |
| Fechar ou trocar endpoint | Fecha o cliente; nova abertura deve ser explícita |

Botões rápidos e catálogo devem obedecer à mesma sessão confirmada. Cancelar
e Fechar devem continuar permitindo liberação de recursos em falha. Recriar o
processo começa com sessão fechada; consulta local de estado não comprova
saúde remota.

A UI deve mostrar código, fase, mensagem segura, duração e ID final da última
operação na área de estado acima do catálogo. Se o erro ocorrer fora da área
visível, deve haver aviso acessível ou rolagem até ele, sem depender apenas de
cor. A mensagem deve persistir após fechar diálogo/teclado; nova ação limpa o
resultado anterior. O ID final deve permanecer separado da operação ativa
para correlação com o log.

As categorias públicas incluem `BRIDGE_UNREACHABLE`, `TIMEOUT`, `PROTOCOL_ERROR`,
`BUSY`, `OWNERSHIP_ERROR`, `SERIAL_UNAVAILABLE`, `CANCELED`, `DISCONNECTED`,
`PINPAD_ERROR`, `PINPAD_CLOSED` e `BINDING_ERROR`. Erros devem preservar código
e correlationId, com mensagem sanitizada e limitada, sem payload ABECS,
stack trace, caminho interno ou erro arbitrário de driver. Falha secundária
de limpeza não deve substituir a causa original.

## Log compartilhado — contrato 072

O único rastro Windows deve permanecer em `PINPAD_LOG_FILE`, por default do
launcher `<raiz-do-módulo>/logs/LogPinpadAbecs.txt`, em append. O mesmo tracer
deve registrar eventos reais de startup/listener, conexão, HELLO/Ping,
ownership, falhas de serial/sessão, liberação e shutdown, além de Open, TX/RX
redigidos e Close. `ping_ok` e `ownership_failed` devem alimentar o arquivo
mesmo sem abrir serial; não se fabricam bytes ou linhas de sucesso ABECS.

Eventos devem incluir timestamp, fase, resultado/código, porta/modo e
sessionId/correlationId quando disponíveis. Como o Bridge trata bytes opacos,
eventos usam contagens, sem parser de negócio. O rastro deve aplicar redação
conservadora também a chunks fragmentados/agregados; payload desconhecido
deve ser omitido e descrito somente por contagem quando necessário. PAN,
trilhas, PIN block, KSN, chaves, EMV sensível e parâmetros de formulários não
devem aparecer em eventos ou erros.

O logger privado Android guarda somente metadados; não cria outro rastro
Windows. Sem Bridge em execução, o erro fica na UI/log privado Android, pois
não há processo host para alimentar o arquivo. Falha ao abrir o log deve
aparecer no stderr/console com código não zero; falha posterior de persistência
deve ser visível e invalidar a sessão afetada quando o rastro obrigatório não
puder ser mantido.

O arquivo Android fica no armazenamento interno privado `files/logs/diagnosticopinpad.jsonl`.
O adapter usa `Mobile.errorCode` e `Mobile.errorPhase`; a UI mantém enums separados
para ação, conectividade e sessão. QR também exige sessão confirmada.

## Gates

```powershell
python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/diagnosticopinpad
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:createDebugUnitTestCoverageReport
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug
```

O relatório JVM fica em `app/build/reports/coverage/test/debug/index.html`.
A interpretação separa lógica JVM de UI/Android/JNI conforme o inventário da
Change 075; o percentual JVM não representa cobertura integrada do aplicativo.
`gradlew.bat` preserva o código de saída real do Gradle, inclusive em falha.

Para testar a implementação consolidada, execute uma operação por vez e registre
evidências no `validation.md` da Change 075. O Gate 5 exige pinpad físico, fluxo
Abrir → GIX → DSP → Fechar e reabertura, log e confirmação visual. Sem essa
evidência, o fluxo físico permanece não comprovado; scripted não substitui
esse gate. Os resultados técnicos de 2026-10-03 estão na Change 075.
Não use valores reais de cartão ou chaves em capturas, logs ou documentação.
