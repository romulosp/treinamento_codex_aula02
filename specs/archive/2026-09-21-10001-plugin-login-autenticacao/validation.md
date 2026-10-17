# Validação

## Status

`VALIDADA`

| ID | Cenário | Evidência esperada |
| --- | --- | --- |
| VAL-01 | Regra `L` | teste unitário de recusa e aceitação |
| VAL-02 | Cursor/foco | teste Compose com seleção/foco após entradas |
| VAL-03 | Ações visuais | semântica encontra `ENTER` e ausência de cancelar |
| VAL-04 | Plugin | build produz APK e host carrega `startup-auth` |
| VAL-05 | Manual | script instala e abre o emulador em paisagem |

## Validação formal — 2026-09-21

Revisão de implementação aprovada em
`reviews/2026-09-21-implementation-review.md`.

| ID | Comando/ação | Resultado | Código |
| --- | --- | --- | --- |
| VAL-001 | `gradlew.bat :app:assembleDebug :plugin-login:assembleDebug :app:testDebugUnitTest :plugin-login:testDebugUnitTest --no-daemon --console=plain` | Build dos APKs e testes JVM concluídos | `0` |
| VAL-002 | `gradlew.bat :app:lintDebug :plugin-login:lintDebug --no-daemon --console=plain` | Lint dos módulos aprovado | `0` |
| VAL-003 | `gradlew.bat :app:connectedDebugAndroidTest :plugin-login:connectedDebugAndroidTest --no-daemon --console=plain` | Testes instrumentados aprovados no `Medium_Tablet(AVD) - 15` | `0` |
| VAL-004 | `validate_android_project.py`, com `local.properties` temporariamente separado e restaurado | Invariantes estruturais atendidos | `0` |
| VAL-005 | `scripts/gerar-compilar-executar.ps1` | Host instalado, iniciado em paisagem e plugin entregue separadamente à pasta dinâmica | `0` |
| VAL-006 | `uiautomator dump` após a ativação dinâmica | Encontrados `IDENTIFICAÇÃO`, `USUÁRIO`, `SENHA`, `ENTER` e `CONFIRMAR`; ausentes indisponibilidade, `SAIR` e `CANCELAR` | `0` |
| VAL-007 | Auditoria de segurança e geração de PDF | Nenhum achado confirmado; categorias de backend/rede/banco permanecem fora do escopo | `0` |

## Ambiente atual

Windows; JBR do Android Studio em `D:\desenvolvimento\ferramentas_android\Nova pasta\AndroidStudio\jbr`; Gradle `9.6.0`; AGP `9.4.0`; Android SDK em `D:\desenvolvimento\ferramentas_android\Sdk`; API 37; emulador `Medium_Tablet(AVD) - 15`.

## Evidências

- APKs: `app/build/outputs/apk/debug/app-debug.apk` e
  `plugin-login/build/outputs/apk/debug/plugin-login-debug.apk`.
- Auditoria: `docs/security-audit/relatorio-10001-plugin-login-autenticacao.pdf`.
- A validação manual pode ser repetida pelo script oficial; o host permanece
  aberto no emulador para inspeção no Android Studio.

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
