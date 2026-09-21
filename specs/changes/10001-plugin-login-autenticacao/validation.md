# Validação

## Status

`PENDENTE_REVISAO_IMPLEMENTACAO`

| ID | Cenário | Evidência esperada |
| --- | --- | --- |
| VAL-01 | Regra `L` | teste unitário de recusa e aceitação |
| VAL-02 | Cursor/foco | teste Compose com seleção/foco após entradas |
| VAL-03 | Ações visuais | semântica encontra `ENTER` e ausência de cancelar |
| VAL-04 | Plugin | build produz APK e host carrega `startup-auth` |
| VAL-05 | Manual | script instala e abre o emulador em paisagem |

## Evidência de implementação e correção de runtime

Ambiente: Windows, JBR do Android Studio `25.0.3`, Gradle `9.6.0`, AGP `9.4.0`, Android SDK em
`D:\desenvolvimento\ferramentas_android\Sdk` (plataforma `android-37.0`).

| ID | Comando | Resultado |
| --- | --- | --- |
| PRE-01 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | código 0; invariantes estruturais atendidos |
| PRE-02 | `gradlew.bat :app:assembleDebug --no-daemon --console=plain` | código 0; host e APK `plugin-login` gerados |
| PRE-03 | `gradlew.bat :plugin-login:testDebugUnitTest --no-daemon --console=plain` | código 0; regra de primeira letra `L` aprovada |
| PRE-04 | `gradlew.bat :app:lintDebug :plugin-login:lintDebug --no-daemon --console=plain` | código 0; lint aprovado |
| PRE-05 | `scripts/gerar-compilar-executar.ps1` | build concluído; instalação não executada porque `adb` não encontrou dispositivo/emulador |
| PRE-06 | `gradlew.bat :plugin-login:assembleDebugAndroidTest --no-daemon --console=plain` | código 0; APK de teste Compose gerado, execução pendente de emulador |
| PRE-07 | `gradlew.bat :app:assembleDebug :plugin-login:testDebugUnitTest :app:lintDebug :plugin-login:lintDebug --no-daemon --console=plain` | código 0 com `JAVA_HOME` apontado ao JBR; host, unitários e lint aprovados após a correção |
| PRE-08 | `gradlew.bat :plugin-login:connectedDebugAndroidTest --no-daemon --console=plain` | código 0 no `Medium_Tablet(AVD) - 15`; os dois testes Compose foram aprovados |
| PRE-09 | `adb install -r app/build/outputs/apk/debug/app-debug.apk` + inicialização do pacote | instalação com sucesso; processo `br.com.romulopenha.sistemaprototipoandroid` ativo (`pid 15432`) |
| PRE-10 | dump XML do `uiautomator` em `/sdcard/login-plugin-window.xml` | encontrou `IDENTIFICAÇÃO`, `USUÁRIO`, `SENHA`, `ENTER` e `CONFIRMAR`; não encontrou `Plugin de autenticação indisponível.` nem `SAIR/CANCELAR` |
| PRE-11 | `adb shell input text A` com usuário vazio e dump XML subsequente | campo de usuário permaneceu vazio; entrada inicial inválida foi ignorada; app reiniciado em seguida para deixar o emulador limpo |
| PRE-12 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | código 1: o validador recusou `local.properties` local criado pelo Android Studio; arquivo preservado por ser configuração do ambiente e não evidência de defeito de código |

O diagnóstico anterior encontrou dois defeitos de runtime: o Android recusava o
APK gravável entregue ao `DexClassLoader` (`SecurityException`) e a instanciação
reflexiva da `LoginViewModel` privada falhava (`IllegalAccessException`). A
correção e sua verificação estão registradas em
`reviews/2026-09-21-runtime-plugin-load-fix.md`.

Esta evidência não substitui revisão independente nem a validação formal. O
status permanece pendente enquanto o quality gate estrutural estiver bloqueado
por `local.properties` e a revisão independente não for concluída.
