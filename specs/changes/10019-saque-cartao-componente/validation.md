# Evidências técnicas — Change 10019

## Estado
`IMPLEMENTADA` — disponível para teste humano. Este registro contém verificações da fase de implementação; não representa validação formal, aprovação final nem homologação física. Seguido o agente implementador-para-teste; revisões e validação formais posteriores aguardam o teste humano.

## Ambiente — 2026-10-30
Windows, PowerShell, workspace D:/desenvolvimento/ia/aula02. Gradle Wrapper 9.6.0, JBR OpenJDK 25.0.3; alvo de bytecode Java 17. Módulos mantêm compileSdk 37, minSdk 29, targetSdk 36. AVD Medium_Tablet, emulator-5554, Android 15/API 35, x86_64. Nenhuma dependência ou AAR Go foi atualizada.

Diretório Gradle: apps/frontend/smartphone/sistema-prototipo-android. JAVA_HOME: D:/desenvolvimento/ferramentas_android/Nova pasta/AndroidStudio/jbr. SDK: D:/desenvolvimento/ferramentas_android/Sdk.

## Comandos e resultados

| ID | Comando / verificação | Resultado | Exit |
| --- | --- | --- | --- |
| VAL-001 | python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android | Detectou local.properties local e ignorado pelo Git; arquivo não foi exposto ou removido. | 1 |
| VAL-002 | Mesmo validador sobre projeção temporária dos 13 arquivos estruturais (Wrapper/settings/builds/manifests), sem local.properties | PASS. Projeção não mede código nem substitui build. | 0 |
| VAL-003 | .\\gradlew.bat :shared-api:testDebugUnitTest :plugin-negocio:testDebugUnitTest :plugin-saque-cartao:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :plugin-saque-cartao:assembleDebugAndroidTest --console=plain | PASS, 35s; primeira compilação e testes. | 0 |
| VAL-004 | .\\gradlew.bat :shared-api:testDebugUnitTest :plugin-negocio:testDebugUnitTest :plugin-saque-cartao:testDebugUnitTest :app:testDebugUnitTest :app:lintDebug :plugin-saque-cartao:lintDebug :plugin-negocio:lintDebug :plugin-saque-cartao:saqueCoverageVerify :app:assembleDebug :app:assembleDebugAndroidTest :plugin-saque-cartao:assembleDebugAndroidTest --console=plain | PASS, 46s; lint sem erros, com avisos descritos abaixo. | 0 |
| VAL-005 | .\\gradlew.bat :app:connectedDebugAndroidTest :plugin-saque-cartao:connectedDebugAndroidTest :plugin-login:testDebugUnitTest --console=plain | PASS, 1m22s; 26 testes host e 18 plugin, zero falhas. | 0 |
| VAL-006 | .\\gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:saqueCoverageVerify :app:lintDebug :app:assembleDebug :app:connectedDebugAndroidTest --console=plain | PASS, 46s; inclui correção de invalidação no início do logout e teste de cancelamento após detach. | 0 |
| VAL-007 | python specs/changes/10019-saque-cartao-componente/evidence/check-artifacts.py | Primeira execução detectou asset legado no output incremental do staging. Corrigido StagePluginApkTask para remover apenas esse arquivo gerado. | 1 |
| VAL-008 | .\\gradlew.bat :app:assembleDebug --console=plain; depois executar check-artifacts.py novamente | Build PASS em 8s; manifesto/serviço/versão, hashes dos APKs embutidos, ausência do asset antigo, resultados de testes/lint e cobertura PASS. | 0 |
| VAL-009 | scripts/iniciar-saque-cartao.ps1 -ProvisionOnly -ValidateOnly -EmulatorSerial emulator-5554 | Fontes externas válidas; sem abertura de hardware. | 0 |
| VAL-010 | scripts/iniciar-saque-cartao.ps1 -ProvisionOnly -EmulatorSerial emulator-5554 | Primeira tentativa encontrou host removido pela instrumentação. Instalação final -r corrigiu a condição. | 1 |
| VAL-011 | adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk; depois ProvisionOnly -EmulatorSerial emulator-5554 | PASS, três snapshots privados com hashes confirmados; fontes externas preservadas. | 0 |
| VAL-012 | adb -s emulator-5554 reverse tcp:39100 tcp:39100; adb -s emulator-5554 shell am start -n br.com.romulopenha.sistemaprototipoandroid/.MainActivity | PASS; aplicativo instalado e login aberto. Hierarquia UI confirmou pacote do host e botão CONFIRMAR. | 0 |
| VAL-013 | adb shell am instrument -w -e class br.com.romulopenha.sistemaprototipoandroid.platform.BusinessPluginManagerTest,br.com.romulopenha.sistemaprototipoandroid.platform.ComponentPluginManagerTest br.com.romulopenha.sistemaprototipoandroid.test/androidx.test.runner.AndroidJUnitRunner | Rechecagem após remoção do asset legado: OK (7 tests), 10s. | 0 |
| VAL-014 | .\\gradlew.bat :app:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest :plugin-saque-cartao:lintDebug --console=plain; depois am instrument com classe SaqueCartaoScreenTest | Rótulo VOLTAR: build/lint PASS, 12s; instrumentação OK (13 tests), 47,5s. | 0 |
| VAL-015 | .\\gradlew.bat :app:lintDebug :app:assembleDebug :app:connectedDebugAndroidTest --console=plain | 27 testes, uma falha: teste de descoberta assumia uma só revisão no repositório. Também evidenciou bloqueio de atualização pela política estrita; corrigida na SPEC r3 e no seletor por digest persistido, sem apagar histórico. | 1 |
| VAL-016 | Mesmo comando de VAL-015 após revisão r3 e correção | PASS, 1m09s; 27 testes host, zero falhas. Seleção persistida corresponde ao asset atual, mesmo com APKs históricos. | 0 |

Relatório reproduzível de artefatos e resultados: [2026-10-30-artifacts.json](evidence/2026-10-30-artifacts.json). O script de conferência fica em evidence/check-artifacts.py. Logs de execução estão em evidence/, sem conteúdo de perfis ou material criptográfico.

## Cenários comprovados
- Total final JVM: 56 testes (shared-api 3, consumidor 1, login 7, saque 36, host 9), zero falhas.
- Instrumentação final: 45 testes distintos (host 27 e plugin 18), zero falhas. Inclui descoberta de APKs reais, capacidade/serviço do componente, ausência do saque no menu, seleção do digest atual após atualização, extração e inicialização de nativas. Os 13 testes de UI também foram repetidos após o rótulo final VOLTAR.
- Sete novos cenários instrumentados verificam retorno único e restauração da mesma View, BUSY, cancelamento aguardando término, abandono sem callback, bloqueio após cleanup falho, indisponibilidade, falha da fábrica e chamada pelo consumidor real carregado de APK.
- Novos testes JVM verificam retorno após CLO sintético, detach sem callback, cancelamento durante captura com cleanup falho e pedido de cancelamento do host depois de detach.
- Cobertura do escopo JVM elegível existente: 278/285 linhas, 97,54%. UI, loader/host Android e adapter gomobile dependem de instrumentação e não estão incluídos nesse percentual; não há alegação de cobertura numérica total do sistema.

## Avisos e limites
Lint terminou sem erros. Avisos: OldTargetApi, MissingApplicationIcon, GradleDependency, NewerVersionAvailable, AndroidGradlePluginVersion, DiscouragedApi, DataExtractionRules e SetTextI18n, conforme módulos no JSON. JBR/runner emitiram aviso de sun.misc.Unsafe em protobuf; execução terminou com exit 0.

A inspeção direta do validador estrutural continua reportando local.properties ignorado, necessário ao ambiente local. A projeção estrutural passou e o arquivo não foi versionado.

O AAR Go preservou SHA-256 383259f6095ca25a079fc0ed388027c47f61b5369f31bbde51485e8501e875eb, correspondente à integração vigente. Não houve alteração de protocolo, criptografia, perfis externos ou execução automática de leitura/PIN/compra.

Na preparação final, PORTA_PINPAD=COM7, mas COM7 não está presente. COM17 foi identificada por PnP como QuickScan-QW2500, um leitor diferente. Não iniciar Bridge em COM17. O teste físico depende de reconectar o pinpad e iniciar o Bridge na porta correta. Revisão independente HIGH_ASSURANCE, homologação física e gates formais posteriores permanecem pendentes.

## Inventário KDoc
- shared-api/PluginContracts.kt: documentação da versão 1.3 e compatibilidade do evento.
- shared-api/CardWithdrawalComponent.kt: enum e valores, resultado/propriedades, contexto de chamada, sessão/View/cancel e entrada do componente documentados em português.
- app/MainActivity.kt: override de lifecycle mantém contrato Android; mudança interna invalida a sessão no início do logout e delega Voltar ao componente.
- app/platform/LoginPluginLoader.kt: consulta exata do componente e invariantes do manifesto documentados; comparação runtime preservada.
- app/platform/BusinessPluginManager.kt: hospedagem, cancelamento e contexto vinculado ao consumidor documentados.
- app/platform/ComponentPluginManager.kt: carga, invariantes de thread, retenção, sessão, cancelamento, abandono, restauração e encerramento documentados. Seleção de digest é persistida por AtomicFile após lifecycle bem-sucedido, conforme DESIGN/r3.
- plugin-negocio/PluginNegocioApp.kt: UI demonstrativa e contrato de folha herdados; função privada recebe estado local/callback e Modifier. Overrides sem novo contrato herdam a shared-api.
- plugin-saque-cartao/PluginSaqueCartaoApp.kt: criação sem hardware, attach, detach e retorno após cleanup documentados.
- plugin-saque-cartao/SaqueJourney.kt: closedState, requestReturn, finishDisposal e entrega única documentados. Propriedades privadas triviais são cobertas pelos invariantes do owner; sem KDoc repetitivo.
- plugin-saque-cartao/SaqueCartaoScreen.kt: rótulo final passa a VOLTAR, coerente com retorno ao chamador; sem alteração do contrato de callbacks ou do layout, KDoc existente permanece aplicável.
- Objetos privados Noop não adicionam contrato; testes/fixture não são APIs de produção. Não foi adicionada ferramenta Dokka: não há tarefa configurada exigida pela Change.
