# Evidências

## Estado e ambiente
`IMPLEMENTADA` para teste humano; não representa revisão de implementação, validação ou aprovação formal. Gate estrutural local preexistente continua reprovado. Sem archive, commit ou atualização de specs/system.

08/11/2026, Windows/PowerShell, workspace `D:/desenvolvimento/ia/aula02`; projeto Android em `apps/frontend/smartphone/sistema-prototipo-android`. Gradle Wrapper 9.6.0, JBR OpenJDK 25.0.3 em `D:/desenvolvimento/ferramentas_android/Nova pasta/AndroidStudio/jbr`, alvo Java 17; Python 3.14.6. Emulador emulator-5554, Medium_Tablet/Pixel_Tablet, Android 15/API 35. JAVA_HOME apenas no processo de execução. Sem login/cartão/PIN ou comandos financeiros físicos automáticos.

## Comandos e resultados

Gradle no diretório Android; Python/adb na raiz. Mudanças alheias preexistentes preservadas. Logs intermediários representam a versão anterior ao guard de callback por ID; a versão final está explicitamente indicada.

| ID | Comando / procedimento | Resultado e evidência | Exit |
| --- | --- | --- | ---: |
| VAL-001 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | Reprovado por local.properties local preexistente, preservado sem leitura/exposição de conteúdo. | 1 |
| VAL-002 | `./gradlew.bat :plugin-ingressos:testDebugUnitTest :plugin-ingressos:ingressosCoverageVerify :plugin-ingressos:lintDebug :plugin-ingressos:assembleDebugAndroidTest :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` | PASS da implementação inicial: 13 JVM, 80/80 linhas elegíveis, lint/builds. `gradle-build.log`. | 0 |
| VAL-003 | `./gradlew.bat :plugin-ingressos:assembleDebugAndroidTest :plugin-ingressos:lintDebug --console=plain` | PASS das evidências de movimento/sucesso/início. `gradle-ui.log`. | 0 |
| VAL-004 | `adb -s emulator-5554 install -r <plugin-apk>` e `<plugin-test-apk>`; `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginingressos.test/androidx.test.runner.AndroidJUnitRunner`; depois integração do host indicada em VAL-007 | PASS: 10 UI e 1 host na versão inicial, sem hardware. `instrumentation.log`, `host-integration.log`. | 0 / JUnit PASS |
| VAL-005 | Mesmo comando Gradle de VAL-002 após proteção contra callback atrasado pelo ID do pedido | PASS final: 14 JVM, zero falhas, cobertura 80/80 linhas elegíveis (100%), lint 0 erros/3 avisos, host/plugin/test APKs. `gradle-final.log`. | 0 |
| VAL-006 | Reinstalar APK/test APK finais do plugin e repetir instrumentação de VAL-004 | PASS: `OK (10 tests)`. `instrumentation-final.log`. Capturas e JSON extraídos por `adb exec-out run-as <pacote-plugin> cat cache/<arquivo>`. | 0 / JUnit PASS |
| VAL-007 | Instalar APK/test APK do host; `adb -s emulator-5554 shell am instrument -w -e class br.com.romulopenha.sistemaprototipoandroid.platform.IngressosCardIntegrationTest br.com.romulopenha.sistemaprototipoandroid.test/androidx.test.runner.AndroidJUnitRunner` | PASS final: `OK (1 test)`. APK independente real no processo host; componente sintético recebe 4400 centavos, retorna APPROVED/cleanup, confirmação aparece, início retorna e A1 segue bloqueado. Nenhuma segunda chamada financeira automática. `host-integration-final.log`. | 0 / JUnit PASS |
| VAL-008 | Comparar hashes baseline e ZIP do host/plugin | PASS: shared-api/BusinessPluginManager/SaqueJourney idênticos; APK embutido corresponde ao produzido. `baseline.json`, `artifacts.json`. | 0 |
| VAL-009 | `adb -s emulator-5554 shell am start -W -n br.com.romulopenha.sistemaprototipoandroid/.MainActivity` | Host final instalado por VAL-007 e aberto para teste humano: Status ok, LaunchState COLD. | 0 |

## Cenários finais

- Carrossel: deslocamento suave pelas setas e seleção, snap por toque preservado e realce breve de escala. Teste com relógio congelado mede posição antes, em 64ms e após conclusão: **332,8dp → 169,8dp → 98,8dp**, comprovando estado intermediário em vez de salto. Evidência em `carrossel-movimento.json` (valores da execução final prevalecem se diferenças de arredondamento ocorrerem).
- Conclusão: somente APPROVED + cleanup em pedido ativo e ID correspondente vende uma vez. Não há botão manual de emissão. Demais status e cleanup falso não mostram sucesso/reset. Callback duplicado e retorno antigo após novo pagamento não modificam/vendem o pedido novo.
- Sucesso: texto exato Transação Realizada com Sucesso, confirmação visual e aviso demonstrativo/resultado local; após 3000ms volta à seleção inicial sem filme/assento/pedido anterior. Não há retorno ao login/menu, alteração de Host ou perda de estoque.
- Próximo atendimento: seleção inicial é limpa, o carrossel volta ao início, A1 já emitido continua indisponível ao reabrir Batman; outro assento pode iniciar pedido com ID novo. Timer obsoleto não apaga esse pedido. Comandos durante confirmação são ignorados.
- Interface/host: mantidos catálogo fixo sem escolha de horário, teclado, compacto/fonte ampliada e carregamento de imagens por classloader. Teste do host usa componente sintético e uma única chamada explícita, sem I/O financeiro físico.

Total final: **14 JVM + 10 Compose + 1 integração no host**, zero falhas. Cobertura elegível de CinemaJourney **80/80 linhas**. Lint mantém três avisos de configuração preexistentes (OldTargetApi, GradleDependency, MissingApplicationIcon); createComposeRule mantém aviso de depreciação. Sem novas dependências, versões ou permissões.

## Inspeção visual

Foram abertas e verificadas as capturas do fluxo [sucesso](evidence/ingressos-sucesso-fluxo.png) e [novo atendimento](evidence/ingressos-novo-atendimento.png). Confirmação central legível, aviso demonstrativo, sem resumo antigo sobreposto; início retorna sem seleção ou pedido. [Sucesso controlado](evidence/ingressos-sucesso.png) também foi extraído. A revisão final do guard alterou apenas identificação de callbacks, sem mudar essa apresentação; mesmas capturas são renovadas na execução final.

## Inventário KDoc e fronteiras

- `CinemaJourney.kt`: constante da mensagem documentada, completedOrderId descrito, callback identificado e conclusão automática documentados; helper privado de venda local e startNewAttendance explicam unicidade/estoque/IDs obsoletos. Mapa de vendidos preserva documentação da 10023.
- `PluginIngressosApp.kt`: KDoc da rota descreve conclusão por três segundos e efeito identificado/cancelável. Callback captura request.orderId; contrato externo, lifecycle e dados do pagamento permanecem iguais.
- `CinemaScreen.kt`: SuccessPanel documentado, mensagem com liveRegion; carrossel usa animação nativa e escala em graphicsLayer. Removidos callback/botão de emissão e recibo antigo no resumo. Cores privadas triviais mantêm exclusão justificável de KDoc.
- Testes JVM/Compose e `app/src/androidTest/.../IngressosCardIntegrationTest.kt` atualizados para o novo fluxo. Nenhum host de produção, shared-api, componente financeiro ou Go/Bridge editado nesta Change. Não há Dokka configurado; não foi adicionado.

## Limitações e pendências

Pagamento/ingresso continuam demonstrativos e locais, sem prova de autorização financeira real ou validade de ingresso. Não houve cartão/PIN ou pagamento físico automático. Estoque continua volátil na instância. Gate estrutural local.properties, aceite humano e revisão/validação/aprovação formais seguem pendentes. Testes técnicos não substituem esses gates.
