# Evidências

## Estado e ambiente
`IMPLEMENTADA` para teste humano, sem aprovação final. Windows/PowerShell,
Gradle Wrapper 9.6.0, JBR do Android Studio, alvo Java 17; emulador
Medium_Tablet API 35, emulator-5554. Diretório Gradle:
`apps/frontend/smartphone/sistema-prototipo-android`.

**Histórico REV-001:** VAL-001 a VAL-005 abaixo comprovam a versão anterior,
que indevidamente zerava a tarifa. Não representam o contrato vigente REV-002.

| ID | Comando / verificação | Resultado | Exit |
| --- | --- | --- | ---: |
| VAL-001 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | Gate estrutural detecta local.properties preexistente. Não removido nem exposto. | 1 |
| VAL-002 | `./gradlew.bat :shared-api:testDebugUnitTest :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebugAndroidTest :plugin-ingressos:testDebugUnitTest :plugin-ingressos:ingressosCoverageVerify :plugin-ingressos:lintDebug :plugin-ingressos:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` | PASS; 57 JVM, zero falhas. Cobertura elegível: componente 287/294 linhas, ingressos 57/57. Lint sem erros. | 0 |
| VAL-003 | `./gradlew.bat :plugin-saque-cartao:connectedDebugAndroidTest :plugin-ingressos:connectedDebugAndroidTest :app:connectedDebugAndroidTest --console=plain` | PASS; componente 19 testes, ingressos 1; suíte completa do host passou. Diagnóstico realBridge é opt-in e permanece ignorado nessa execução. | 0 |
| VAL-004 | `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=br.com.romulopenha.sistemaprototipoandroid.platform.ComponentPluginManagerTest#pedidoComValorNaoRecuaParaComponenteLegadoEditavel --console=plain` | PASS; teste adicional prova ausência de fallback para provedor antigo/editável. Esse relatório substitui o XML anterior da suíte do host. | 0 |
| VAL-005 | `adb install -r app/build/outputs/apk/debug/app-debug.apk`; `scripts/iniciar-saque-cartao.ps1 -ProvisionOnly -EmulatorSerial emulator-5554`; `adb shell am start -W -n br.com.romulopenha.sistemaprototipoandroid/.MainActivity` | Host instalado, três snapshots privados com hashes confirmados, aplicativo aberto em login. | 0 |

Na versão histórica, testes demonstraram valor imutável na lógica e UI,
modalidade/parcelas sem alteração de total, limite e captura com valor exato. Integração
carrega APK real de ingressos e confere request de 1250 centavos no componente
sintético pelo host. Caminho legado continua coberto pela suíte anterior.
Sem compra, cartão ou PIN automáticos e sem alteração Go/Bridge/chaves.

Avisos: depreciação createComposeRule/MenuAnchorType e Unsafe do runner,
além de avisos lint de SDK/configuração existentes. O gate estrutural VAL-001
continua pendente; testes técnicos não constituem validação formal.
Resumo dos XML, cobertura e hashes em evidence/test-summary.json.

## REV-002 — tarifa com valor base bloqueado

O usuário identificou que REV-001 eliminou indevidamente a tarifa. A correção
reutiliza calculateSaque da 10004 para ambos os caminhos. A base permanece
bloqueada; tarifa e total são recalculados por modalidade e parcelas.

| ID | Comando / verificação | Resultado | Exit |
| --- | --- | --- | ---: |
| VAL-006 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` na raiz | Detecta local.properties preexistente; não removido nem exposto. | 1 |
| VAL-007 | `./gradlew.bat :shared-api:testDebugUnitTest :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug --console=plain` | PASS: 43 testes JVM; cobertura elegível 283/290 linhas; lint sem erros e build. | 0 |
| VAL-008 | `./gradlew.bat :plugin-saque-cartao:connectedDebugAndroidTest --console=plain` | PASS: 20 testes instrumentados, inclusive leitura de valor bloqueado, taxa 10,14%, tarifa R$ 1,01 e total R$ 11,01. | 0 |
| VAL-009 | `adb install -r app/build/outputs/apk/debug/app-debug.apk`; `scripts/iniciar-saque-cartao.ps1 -ProvisionOnly -EmulatorSerial emulator-5554`; abertura do host via adb | PASS: host instalado, três snapshots privados com hashes confirmados, tela de login aberta. | 0 |

Testes sintéticos conferem base R$ 10,00, crédito 1x/7x, débito 1x,
arredondamento, resumo, mensagem de confirmação e total enviado à captura.
Nenhuma compra ou leitura de cartão foi automatizada. O validador estrutural
e o teste físico permanecem pendentes; esta evidência não é aprovação final.
Resultados e hashes do APK estão em evidence/tarifa-r2.json.

## Teste humano — resultado indeterminado

Em 2026-11-07, usuário informou `BINDING_ERROR:command` após iniciar a
captura e imagem com base R$80,00, tarifa R$4,27 e total R$84,27. Soma confere;
não há prova de aprovação/recusa ou de que a alteração de tarifa causou o erro.
O último trecho do log registra aquisição e liberação da sessão, mas não
identifica o comando/status da falha. O Bridge executado não apresenta o
observador semântico que consta no binário da 10017; conteúdo dos pacotes não
foi exposto. AAR corresponde à baseline 10016/10019 e o host instalado ao APK
local r2. Resultado desconhecido: não repetir automaticamente e conciliar antes
de nova operação. A inspeção é diagnóstica, não valida pagamento físico.
