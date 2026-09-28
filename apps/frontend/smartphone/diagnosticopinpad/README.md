# diagnosticopinpad

Aplicativo Android nativo da Change 069 para exercitar o caminho:

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

- JDK 17;
- Android SDK com API 37, Build Tools 36.0.0 e NDK 28.2.13676358;
- Go 1.26 e `gomobile`;
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

```powershell
$sdk = "D:\desenvolvimento\ferramentas_android\Sdk"
$env:Path = "$sdk\platform-tools;$env:Path"
adb reverse tcp:39100 tcp:39100
.\gradlew.bat :app:installDebug
adb shell am start -n br.com.romulopenha.diagnosticopinpad/.MainActivity
```

Com `adb reverse`, mantenha `localhost` no campo Host. Sem reverse, use
`10.0.2.2`. A porta informada no Android (`39100`) é a porta TCP do Bridge, não
é a porta COM.

Para um teste sem hardware, no Windows Host execute no módulo Go:

```powershell
$env:PINPAD_BRIDGE_TRANSPORT = "scripted"
go run .\cmd\libpinpadabecsgo-bridge
```

Esse transcript cobre os fluxos básicos sem fingir sucesso para operações que
dependem de ABECS real. Para o pinpad físico, o Bridge deve ser iniciado assim:

```powershell
$env:PORTA_PINPAD = "COM14"
$env:PINPAD_BAUDRATE = "19200"
$env:PINPAD_TIMEOUT = "30"
Remove-Item Env:PINPAD_BRIDGE_TRANSPORT -ErrorAction SilentlyContinue
go run .\cmd\libpinpadabecsgo-bridge
```

`PORTA_PINPAD` é consumida exclusivamente pelo processo Windows do Bridge. O
Android recebe apenas o endpoint TCP.

## Gates

```powershell
python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/diagnosticopinpad
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug
```

Para hardware, execute uma operação por vez e registre o resultado no
`validation.md` da Change 069. Não use valores reais de cartão ou chaves em
capturas, logs ou documentação.
