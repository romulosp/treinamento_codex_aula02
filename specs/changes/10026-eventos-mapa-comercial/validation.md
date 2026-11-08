# Validação — Change 10026

Status: **VALIDADA**, com ressalva ambiental do validador estrutural registrada em [ADR-001](ADR-001-ambiente-local.md). Aprovação humana final permanece pendente; sem archive ou commit.

## Ambiente
2026-11-08, Windows 10 amd64, PowerShell, Gradle Wrapper 9.6.0; launcher Java 17.0.11, daemon Java 25 conforme gradle-daemon-jvm.properties; Kotlin do Gradle 2.3.21. Stack Android preservada: compileSdk 37, minSdk 29, targetSdk 36. Emulador emulator-5554 Medium Tablet API 35, 2560×1600. UI compacta controlada com 380dp e fonte 150%.

Diretório Gradle: `apps/frontend/smartphone/sistema-prototipo-android`. Comandos abaixo usam `.\gradlew.bat` desse diretório. Nenhuma operação física de pinpad/cartão executada; pagamentos dos testes usam componentes sintéticos.

## Evidências finais
- VAL-001: `:plugin-ingressos:ingressosCoverageVerify :plugin-ingressos:lintDebug :plugin-ingressos:connectedDebugAndroidTest :app:assembleDebug :app:assembleDebugAndroidTest :shared-api:testDebugUnitTest :plugin-saque-cartao:testDebugUnitTest :app:testDebugUnitTest --console=plain`, exit **0**. Log: [validation-final.log](validation-final.log). Plugin: **25 JVM** (14 anteriores + 11 novos) e **13 Compose** sem falha. Resultados JVM dependentes preservados/cached: shared-api 5, cartão 43, host 9; total JVM disponível 82. [UI completa](evidence/plugin-ui-full.xml), [resumo](evidence/results.json).
- VAL-002: após inspeção visual, corrigida largura da linha de total e revisada a implementação. `:plugin-ingressos:connectedDebugAndroidTest :plugin-ingressos:ingressosCoverageVerify :plugin-ingressos:lintDebug :app:assembleDebug -Pandroid.testInstrumentationRunnerArguments.class=br.com.romulopenha.sistemaprototipoandroid.pluginingressos.CinemaScreenTest#mapaAmpliadoSelecionaCasaisComNumeracaoAteL15,br.com.romulopenha.sistemaprototipoandroid.pluginingressos.CinemaScreenTest#showCombinaAssentoEPistasComPagamentoUnico --console=plain`, exit **0**. O runner Gradle executou apenas o primeiro filtro: **1 teste**, não dois; [log](ui-focal.log), [XML](evidence/plugin-ui-focal.xml).
- VAL-003: ambos os métodos finais executados individualmente por `adb shell am instrument -w -r -e class <classe>#<método> br.com.romulopenha.sistemaprototipoandroid.pluginingressos.test/androidx.test.runner.AndroidJUnitRunner`, após `adb install -r -t` dos APKs plugin/debug e androidTest. Cada comando exit **0**, **OK (1 test)**. Evidências: [mapa](evidence/mapaAmpliadoSelecionaCasaisComNumeracaoAteL15.log), [pedido misto e largura](evidence/showCombinaAssentoEPistasComPagamentoUnico.log). Capturas finais inspecionadas: [mapa ampliado e corações](evidence/eventos-mapa-ampliado.png), [pedido de casais](evidence/eventos-casais-pedido.png), [show e total completo](evidence/eventos-show-misto.png). [Compacto](evidence/ingressos-catalogo-compacto-pedido.png) e [fonte ampliada](evidence/ingressos-catalogo-fonte-ampliada.png) também inspecionados.
- VAL-004: `:app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=br.com.romulopenha.sistemaprototipoandroid.platform.IngressosCardIntegrationTest,br.com.romulopenha.sistemaprototipoandroid.platform.ComponentPluginManagerTest --console=plain`, exit **0**. Runner executou **3 casos de IngressosCardIntegrationTest**, não a segunda classe. APPROVED, CAPTURED e show misto R$ 310 com estoque de pista 98/VIP 39 no novo atendimento: aprovados. [Log](host-integration.log), [XML](evidence/host-integration.xml).
- VAL-005: cobertura elegível **149/149 linhas = 100%**, mínimo 80% atendido. Inclui CinemaJourney/CinemaJourneyKt, gerador VenueLayout.Companion e cálculo TicketLine; exclui DTOs declarativos/Compose e wiring sem regra própria. [XML](evidence/coverage.xml). Lint plugin **0 erros e 3 avisos** de configuração já existentes; advertências de createComposeRule legado e Unsafe/protobuf da toolchain mantidas, sem upgrade fora de escopo.
- VAL-006: `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android`, da raiz, exit **1**: somente local.properties preexistente. `git check-ignore apps/frontend/smartphone/sistema-prototipo-android/local.properties`, exit **0**. [Log](structural-validator.log). Ressalva ambiental [ADR-001](ADR-001-ambiente-local.md); o validador não foi alterado nem declarado aprovado.
- VAL-007: SHA-256 do APK embutido no host corresponde exatamente ao plugin final 1.2.0/versionCode 3; contrato requerido 1.5 inalterado. [Hashes e manifesto](evidence/artifacts.json). Sem nova dependência, rede, credencial ou mudança em componente financeiro/nativos. Inspeção de arquivos Kotlin alterados sem espaços finais.
- VAL-008: `adb install -r app/build/outputs/apk/debug/app-debug.apk`, exit **0/Success**; `adb shell am start -S -W -n br.com.romulopenha.sistemaprototipoandroid/.MainActivity`, exit **0**, Status ok, LaunchState COLD, TotalTime 2909ms. Host final aberto no emulador para teste humano.

## Falhas intermediárias e correções
- O primeiro teste compacto foi interrompido após detectar sobreposição de semântica nos eixos de rolagem: separados em contêineres distintos. A suíte completa posterior passou.
- Compilação do novo teste exigiu import da extensão assert; corrigido antes dos resultados finais.
- Ampliação inicialmente mantinha dois mapas acessíveis: removida composição do mapa de fundo enquanto o modal está aberto. Suite completa e cenário focal passaram.
- Inspeção de captura encontrou corte no total do resumo do show: rótulo passou a ocupar largura flexível, com asserção de limites e nova captura final aprovada.

## Aceite e limites
CA-01 a CA-08 cobertos pelos testes e inspeções acima. Estoque permanece apenas em memória, por apresentação. Captura de homologação conclui somente venda demonstrativa; aviso de simulação e ingresso sem validade preservados. Aprovação financeira real, backend, concorrência entre terminais, persistência, meia-entrada e descontos não integram esta Change.

## Incidente pós-entrega — perfil MISSING no preflight

Usuário apresentou erro MISSING/preflight ao chamar cartão. ADB confirmou ausência de files/saque-cartao no sandbox do host. A validação sintética/instalação não preservou o provisionamento externo e a entrega 10026 deixou de restaurá-lo. Não é evidência de falha na passagem do valor nem validação do terminal físico.

Recuperação operacional pela SPEC 10012 aprovada, sem alterar código:
- `powershell.exe -NoProfile -ExecutionPolicy Bypass -File apps/frontend/smartphone/sistema-prototipo-android/scripts/iniciar-saque-cartao.ps1 -ProvisionOnly -ValidateOnly -EmulatorSerial emulator-5554`: exit 0/PASS.
- Mesmo comando com `-ProvisionOnly`, sem `-ValidateOnly`: exit 0; três arquivos privados restaurados e hashes confirmados.
- ADB confirmou perfil.json, emv.json e criptografia.properties presentes. Nenhum conteúdo ou valor criptográfico publicado. Nenhum arquivo PRD/credencial geral acessado, chave instalada no dispositivo ou operação física iniciada.
- Não havia processo Bridge nem listener TCP 39100 durante a recuperação. O usuário precisa iniciar o launcher do Bridge antes de sair da tela de erro e reabrir pagamento. A recuperação não comprova listener, OPN ou pagamento físico.

Correção para próximas entregas: após terminar TODOS os testes instrumentados e a instalação final do host, executar ProvisionOnly e conferir os três arquivos/hashes antes de anunciar prontidão para teste humano. Não reinstalar/testar o host depois desse passo sem novo provisionamento. Não abrir cartão automaticamente para validar, porque o attach pode iniciar preparação física.
