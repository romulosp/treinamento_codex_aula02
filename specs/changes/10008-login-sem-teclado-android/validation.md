# Evidências — 10008

## Resultado técnico — 2026-10-03

Implementação disponível para teste humano. Não representa aprovação formal final ou autorização para arquivar.

Ambiente: Windows/PowerShell; Java `C:\Desenvolvimento\jdk-17.0.11`; Maven configurado em `C:\Desenvolvimento\apache-maven-3.8.8` (não utilizado pelo build Android); Gradle Wrapper do projeto; SDK `D:\desenvolvimento\ferramentas_android\Sdk`; emulador `emulator-5554`, Medium Tablet, API 35.

## Inventário

- Produção: `plugin-login/src/main/java/br/com/romulopenha/sistemaprototipoandroid/pluginlogin/LoginPlugin.kt`: `InterceptPlatformTextInput` impede iniciar a sessão da IME somente nos campos do login; `LocalSoftwareKeyboardController.hide()` oculta IME residual ao entrar na tela. Campos continuam editáveis e focáveis; KDoc atualizado. Nenhuma dependência ou permissão adicionada.
- Teste: `plugin-login/src/androidTest/java/br/com/romulopenha/sistemaprototipoandroid/pluginlogin/LoginScreenTest.kt`: novo cenário verifica foco, letras/números físicos, Backspace, teclado do aplicativo e invisibilidade da IME via WindowInsets. Os quatro cenários existentes permanecem.
- Host recompilado para incluir o APK atualizado do plugin. Sem mudanças na autenticação ou na configuração global do dispositivo.

## Comandos e resultados

Diretório do build: `apps/frontend/smartphone/sistema-prototipo-android`, relativo à raiz do repositório.

```powershell
$env:JAVA_HOME='C:\Desenvolvimento\jdk-17.0.11'
$env:MAVEN_HOME='C:\Desenvolvimento\apache-maven-3.8.8'
$env:PATH="$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"
.\gradlew.bat :plugin-login:testDebugUnitTest :plugin-login:lintDebug :app:assembleDebug :plugin-login:connectedDebugAndroidTest
exit $LASTEXITCODE
```

Saída 0, `BUILD SUCCESSFUL`: 7 testes unitários, 5 instrumentados, sem falhas; lint do plugin aprovado; APK debug do host gerado. Relatórios: `plugin-login/build/test-results/testDebugUnitTest/TEST-*.xml`, `plugin-login/build/outputs/androidTest-results/connected/debug/TEST-Medium_Tablet(AVD) - 15.xml` e `plugin-login/build/reports/lint-results-debug.html`.

Na raiz do repositório, validação estrutural de cópia temporária dos fontes, preservando `local.properties` original e excluindo artefatos locais:

```powershell
$taskSources = Join-Path $env:TEMP ('login10008-sources-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $taskSources | Out-Null
robocopy 'apps/frontend/smartphone/sistema-prototipo-android' $taskSources /E /XD build .gradle .idea /XF local.properties /NFL /NDL /NJH /NJS /NP | Out-Null
python .agents/skills/android-native-engineering/scripts/validate_android_project.py $taskSources
exit $LASTEXITCODE
```

Saída 0: `OK: invariantes estruturais Android atendidos`. Esta checagem foi realizada após o build, e não na ordem recomendada pela skill; não foi omitida.

Instalação local preservando os dados do aplicativo:

```powershell
& 'D:\desenvolvimento\ferramentas_android\Sdk\platform-tools\adb.exe' -s emulator-5554 install -r 'apps/frontend/smartphone/sistema-prototipo-android/app/build/outputs/apk/debug/app-debug.apk'
& 'D:\desenvolvimento\ferramentas_android\Sdk\platform-tools\adb.exe' -s emulator-5554 shell am start -n br.com.romulopenha.sistemaprototipoandroid/.MainActivity
```

Instalação: saída 0, `Success`. Inicialização: saída 0. Não houve limpeza de dados, commit ou arquivamento.

## Limitações e teste humano

- Eventos de teclado físico foram simulados pelo teste Compose no emulador. Confirmar também teclado USB/Bluetooth real: tocar Usuário/Senha, digitar letras/números, apagar, alternar campos e usar o teclado da tela. A barra de sugestões/clipboard e o teclado Android não devem aparecer.
- A asserção WindowInsets exige API 30+; cenário executado na API 35. Não foi validado em API 29 ou em firmwares de outros fabricantes.
- Sem medição nova de cobertura: teste de comportamento Compose/framework, sem alteração de regras do reducer. Não declarar percentual ou aprovação de cobertura.
- Durante preparação do teste, chamadas AndroidX indisponíveis e anotação SdkSuppress ausente causaram falhas de compilação. Corrigidas sem novas dependências usando API nativa WindowInsets e TargetApi/Assume; o comando final acima passou.

Referência da solução: [InterceptPlatformTextInput — documentação oficial Compose](https://developer.android.com/reference/kotlin/androidx/compose/ui/platform/InterceptPlatformTextInput.composable).
