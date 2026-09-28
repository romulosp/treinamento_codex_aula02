# Evidências de implementação — 10006-layout-login-referencia-svg

## Status

`IMPLEMENTADA`

Esta evidência prepara a Change para teste humano. Ela não constitui revisão
da implementação, validação formal, aprovação ou arquivamento.

## Ambiente

- Data: 2026-09-25.
- Sistema: Windows, PowerShell.
- JDK: Eclipse Adoptium 25 localizado no cache Gradle; bytecode do projeto
  continua configurado para Java 17.
- Gradle Wrapper: 9.6.0.
- Emulador: `Medium_Tablet(AVD)`, Android 15, identificado como
  `emulator-5554`.
- Referência: `tela-login.svg`, 1280×800, SHA-256
  `35FD4AFD781897E92E559FED84B626942FCA353EE299EF2967E382B7F016F8EE`.

## Comandos e resultados

| ID | Diretório | Comando | Resultado | Saída |
| --- | --- | --- | --- | --- |
| VAL-001 | raiz do repositório | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | O validador executou e interrompeu ao encontrar `local.properties`, arquivo local preexistente e ignorado pelo Git. Nenhum arquivo foi removido. | 1 |
| VAL-002 | projeto Android | `gradlew.bat :plugin-login:testDebugUnitTest :plugin-login:assembleDebug --stacktrace` | Build concluído; 6 testes JVM passaram, sem falhas. | 0 |
| VAL-003 | projeto Android | `gradlew.bat :plugin-login:lintDebug :plugin-login:assembleDebugAndroidTest :app:assembleDebug --stacktrace` | Plugin, APK de testes e host compilados. Lint: 0 erros e 4 warnings preexistentes/não relacionados ao layout (`OldTargetApi`, `NewerVersionAvailable`, `DataExtractionRules`, `MissingApplicationIcon`). | 0 |
| VAL-004 | projeto Android | `gradlew.bat :plugin-login:connectedDebugAndroidTest --stacktrace` | 4 testes Compose executados no emulador, sem falhas, erros ou skips. | 0 |
| VAL-005 | raiz do repositório | `git diff --check -- apps/frontend/smartphone/sistema-prototipo-android/plugin-login specs/changes/10006-layout-login-referencia-svg` | Nenhum erro de whitespace; somente aviso de conversão futura LF/CRLF. | 0 |

## Inspeção visual técnica

O `app-debug.apk` foi instalado no emulador e a `MainActivity` foi aberta em
paisagem. A inspeção confirmou:

- cabeçalho branco com `Buy More`, `POS - COMPRAS` e `v1.0.0.0`;
- título e campos centralizados, sem corte;
- teclado alfanumérico completo, sem botão `ENTER`;
- `CONFIRMAR` vertical e teclado numérico à direita;
- rodapé branco com a mensagem inicial;
- ausência de sobreposição entre os componentes do aplicativo.

O teclado do sistema apareceu na lateral direita porque o campo de usuário
recebe foco automaticamente. Esse overlay pertence ao Android e não impediu a
inspeção do layout do aplicativo.

## KDoc e escopo

O inventário está em `kdoc-inventory.md`. Somente dois arquivos Kotlin do
`:plugin-login` foram alterados por esta Change. As demais modificações já
presentes no worktree pertencem a trabalhos paralelos e foram preservadas.

## Pendência obrigatória

O teste humano comparativo com o SVG permanece pendente. Conforme o agente
`implementador-para-teste`, nenhuma revisão de implementação, validação formal,
aprovação, atualização de `specs/system/`, arquivamento ou commit foi executado.
