# Validation — 068 diagnosticopinpad

## Status

`IMPLEMENTADA`

A implementação Android foi compilada e exercitada no Emulator API 37 com o
Bridge em transporte scripted. A change continua deliberadamente neste estado:
a revisão de implementação, a validação formal, a aprovação e o teste com
pinpad físico permanecem pendentes.

## Ambiente da execução

- sistema: Windows 10.0.19045;
- data: 2026-09-27;
- repositório: `D:\desenvolvimento\ia\lib-pinpad-abecs`;
- Go usado para o AAR: `go1.26.5 windows/amd64`, isolado em `.tmp`;
- Java usado pelo Gradle: JBR `25.0.3` do Android Studio;
- Gradle Wrapper: `9.6.0`;
- Android SDK: API 37, Build Tools 36.0.0;
- Android NDK: `28.2.13676358`;
- dispositivo: `emulator-5554`, Emulator API 37, `Medium_Tablet`;
- ADB: SDK `platform-tools`;
- Bridge: `127.0.0.1:39100`, transporte `scripted`;
- AAR: gerado localmente e ignorado pelo Git.

## Evidências

| Comando/procedimento | Código | Resultado |
|---|---:|---|
| `go test -count=1 ./...` | 0 | Testes Go unitários aprovados. |
| `go test -count=1 -tags=integration ./...` | 0 | Fluxo Go/Bridge com transcript serial roteirizado aprovado. |
| `go vet ./...` | 0 | Nenhum diagnóstico. |
| `go build ./...` | 0 | Build dos pacotes desktop, Bridge e fachada aprovado. |
| `go test ./... -coverprofile=coverage.out` | 0 | Testes aprovados; cobertura total registrada em 82,0%. |
| `CGO_ENABLED=1 go test -race ./...` | 1 | Não executado por ausência de compilador C `gcc` no Windows; a limitação não invalida os testes sem race. |
| `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/diagnosticopinpad` | 0 | Invariantes estruturais Android atendidos. |
| `scripts/validate-project.ps1` | 0 | Validador estrutural do projeto aprovado. |
| `scripts/build-mobile-aar.ps1 -AllowDirty` | 0 | AAR gerado; SHA-256 `996A65B9EFB091E22943DA286D37988A3953A7AA3AF0EF834BB658E4BF0F6BD6`. |
| `gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` | 0 | Testes JVM, lint e APK debug aprovados. |
| `adb devices` | 0 | `emulator-5554` online. |
| `adb reverse tcp:39100 tcp:39100` | 0 | `localhost:39100` do app encaminhado ao Bridge do host. |
| `adb install -r app/build/outputs/apk/debug/app-debug.apk` | 0 | APK instalado com sucesso. |
| `adb shell am start -n br.com.romulopenha.diagnosticopinpad/.MainActivity` | 0 | `MainActivity` ficou em primeiro plano. |
| teste manual no Emulator: botão `Ping` | 0 | UI exibiu `Estado: OPEN`, `Ação: ping`, `Duração: 93 ms` e resultado `PONG`. |
| `adb logcat` filtrado por `AndroidRuntime`/`FATAL EXCEPTION` | 0 | Nenhum crash do aplicativo após a abertura e o ping. |
| `git diff --check` | 0 | Nenhum erro de whitespace; permanecem apenas avisos de conversão LF/CRLF. |

## Gates ainda não encerrados

- `VAL-004`: testes instrumentados/Compose e prova explícita de `Version()`;
- `VAL-006`: fluxo completo `Open → GetInfo → Close` no app;
- `VAL-007`: fluxo equivalente com `PORTA_PINPAD` e pinpad físico real;
- `VAL-008`: timeout, cancelamento, `BUSY`, disconnect e lifecycle sob falha;
- `VAL-009`: correlação ponta a ponta e inspeção formal de redaction;
- `VAL-010`: cobertura e revisão formal da implementação.

## Regra de status

A change não poderá ser marcada `VALIDADA` ou `APROVADA` antes da revisão
formal, dos gates restantes e do teste com pinpad físico definido pela SPEC. O
estado `IMPLEMENTADA` registra somente que o incremento de implementação e as
evidências técnicas disponíveis foram concluídos.
