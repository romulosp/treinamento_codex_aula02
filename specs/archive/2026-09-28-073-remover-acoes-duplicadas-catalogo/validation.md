# Validation — 073 Remover ações duplicadas do catálogo Android

## Status

`VALIDADA`

## Ambiente

- Windows 10, PowerShell, workspace compartilhado.
- Android Studio/emulador `emulator-5554` online.
- JDK `17.0.11` em `C:\Desenvolvimento\jdk-17.0.11`.
- Gradle Wrapper do projeto, versão reportada pelo build 9.6.0.
- Projeto: `apps/frontend/smartphone/diagnosticopinpad`.

## Evidências

| Evidência | Comando/procedimento | Resultado |
|---|---|---|
| VAL-073-01 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/diagnosticopinpad` | Código 1 por `local.properties` já presente no workspace; nenhum problema estrutural adicional reportado. O arquivo é local/ignorado e não foi alterado. |
| VAL-073-02 | `.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` | `BUILD SUCCESSFUL`, código 0. Há apenas warning conhecido da API deprecated `createComposeRule`. |
| VAL-073-03 | Instalação dos APKs debug e androidTest via `adb -s emulator-5554 install -r` | Ambos instalados com sucesso, código 0. |
| VAL-073-04 | `adb shell am instrument -w -e class br.com.romulopenha.diagnosticopinpad.ui.DiagnosticRegressionTest ...` | `OK (4 tests)`, código 0. Inclui a verificação de exatamente um par rápido e ausência dos rótulos duplicados. |
| VAL-073-05 | Teste JVM `CatalogActionTest` executado por `testDebugUnitTest` | 26 ações visíveis, sem OPEN/CLOSE, ordem preservada, código 0. |
| VAL-073-06 | Inspeção de diff/escopo | Somente enum de visibilidade, renderização do catálogo e testes Android foram alterados; nenhum Go/AAR/protocolo foi alterado. |

## Resultado por critério

| Critério | Resultado |
|---|---|
| CA-073-01 | conforme: um `Abrir` e um `Fechar` nos controles superiores |
| CA-073-02 | conforme: catálogo não exibe `Abrir conexão` nem `Fechar conexão` |
| CA-073-03 | conforme: `Estado atual` é a primeira ação visível e a ordem relativa permanece |
| CA-073-04 | conforme: callbacks rápidos preservados; regressão Compose passou |
| CA-073-05 | conforme: tela segue usando `LazyColumn`; teste instrumentado passou |
| CA-073-06 | conforme: Gradle, lint, build e testes passaram |

## Limitação registrada

O validador estrutural da skill Android considera a presença de `local.properties`
uma violação potencialmente sensível. Esse arquivo já existia no projeto local,
está fora da mudança e não foi modificado. O build reproduzível e os testes
foram executados pelo Wrapper com sucesso.
