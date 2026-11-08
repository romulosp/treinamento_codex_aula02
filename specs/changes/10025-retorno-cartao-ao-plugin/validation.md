# Evidências

## Estado e ambiente
`IMPLEMENTADA` para teste humano. Testes técnicos aprovados; não constitui revisão de implementação, validação ou aprovação formal. Sem archive, commit ou atualização de specs/system. Gate estrutural preexistente continua reprovado.

08/11/2026, Windows/PowerShell, workspace `D:/desenvolvimento/ia/aula02`; Gradle no projeto `apps/frontend/smartphone/sistema-prototipo-android`. Wrapper 9.6.0, JBR OpenJDK 25.0.3 em `D:/desenvolvimento/ferramentas_android/Nova pasta/AndroidStudio/jbr`, alvo Java 17; Python 3.14.6. Emulador emulator-5554/Pixel_Tablet, Android15/API35. JAVA_HOME apenas no processo do comando. Não houve compra física, captura de PIN ou comando de protocolo Go/Bridge automático; nenhum perfil/chave externo alterado.

## Comandos e resultados

| ID | Comando / procedimento | Resultado e evidência | Exit |
| --- | --- | --- | ---: |
| VAL-001 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` na raiz | Reprovado pelo mesmo local.properties preexistente, preservado sem exposição. | 1 |
| VAL-002 | `./gradlew.bat :shared-api:testDebugUnitTest :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebugAndroidTest :plugin-ingressos:testDebugUnitTest :plugin-ingressos:ingressosCoverageVerify :plugin-ingressos:lintDebug :plugin-ingressos:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` | Primeira execução: 43 testes do componente/1 falha em asserção de manifesto que ainda esperava minor4. Atualizada para contrato aprovado minor5; teste também verifica construtor público sem argumentos por reflexão. `gradle-build.log`. | 1 |
| VAL-003 | Repetir comando VAL-002 após correção | PASS: 71 JVM, coberturas, lint e builds; `gradle-final.log`. | 0 |
| VAL-004 | Instalar APK/test APK do componente com `adb -s emulator-5554 install -r <apk>`; `adb -s emulator-5554 shell am instrument -w -e class br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao.PluginSaqueReturnTest br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao.test/androidx.test.runner.AndroidJUnitRunner` | PASS: `OK (2 tests)` da fábrica/attach/owner reais, repository sintético. CALLER retorna CAPTURED automaticamente após close único; método1.4 mantém tela terminal e VOLTAR. `component-real-factory.log`. | 0 / JUnit PASS |
| VAL-005 | Repetir VAL-002 após ajuste de KDoc/capturas dos testes do host | PASS final: todos os 71 JVM, coberturas, lint/builds e test APKs. `gradle-release.log`. | 0 |
| VAL-006 | Reinstalar APK/test APK finais do componente; `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao.test/androidx.test.runner.AndroidJUnitRunner` | PASS: `OK (22 tests)`, incluindo fábrica real sintética, UI antiga e fixtures locais. O teste JNI de descoberta opera somente cliente CLOSED, sem TCP/serial/captura. `component-suite.log`. | 0 / JUnit PASS |
| VAL-007 | Instalar APK/test APK de ingressos; `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginingressos.test/androidx.test.runner.AndroidJUnitRunner` | PASS: `OK (10 tests)`; programação fixa, carrossel, sucesso e novo atendimento. `ingressos-suite.log`. | 0 / JUnit PASS |
| VAL-008 | Instalar APK/test APK do host; `adb -s emulator-5554 shell am instrument -w -e class br.com.romulopenha.sistemaprototipoandroid.platform.ComponentPluginManagerTest,br.com.romulopenha.sistemaprototipoandroid.platform.IngressosCardIntegrationTest br.com.romulopenha.sistemaprototipoandroid.test/androidx.test.runner.AndroidJUnitRunner` | PASS: `OK (11 tests)`; lifecycle/concorrência/cleanup, consumidor antigo, rejeição de provedor1.4 para opções novas e duas integrações de ingressos (APPROVED/CAPTURED). `host-suite.log`. | 0 / JUnit PASS |
| VAL-009 | Conferir SHA-256 de baseline, APKs embutidos/produzidos e manifestos JSON | PASS: GoSaqueRepository e AAR idênticos; APKs de ingressos/componente embutidos no host são idênticos aos produzidos, ambos requerem minor5. `baseline.json`, `artifacts.json`, `test-summary.json`. | 0 |
| VAL-010 | `adb -s emulator-5554 shell am start -W -n br.com.romulopenha.sistemaprototipoandroid/.MainActivity` | Host final instalado e aberto: Status ok, LaunchState COLD. Nenhum login/pagamento físico automatizado. | 0 |

## Resumo final

| Suíte | Testes | Falhas |
| --- | ---: | ---: |
| Shared-api JVM | 5 | 0 |
| Componente JVM | 43 | 0 |
| Ingressos JVM | 14 | 0 |
| Host JVM | 9 | 0 |
| Componente instrumentado | 22 | 0 |
| Ingressos instrumentado | 10 | 0 |
| Host instrumentado focal | 11 | 0 |
| **Total final** | **114** | **0** |

Cobertura elegível: componente 293/300 linhas (97,67%); ingressos 80/80 (100%). Lint: componente/ingressos 0 erros e 3 avisos cada; host 0 erros e 8 avisos de configuração preexistentes. Aviso de depreciação createComposeRule mantido. Nenhuma biblioteca, SDK ou versão de ferramenta atualizada; somente contrato interno SharedApi1.5 e versões dos APKs consumidores/provedor.

## O que foi comprovado

- Opção `CardPaymentOptions(CALLER)` pertence à chamada de ingressos. Novas facetas 1.5 transportam configuração sem mexer no DTO monetário; construtor2args/copy de CardPaymentRequest e contratos1.3/1.4 preservados. Código antigo continua disponível e a fábrica real 1.4 mantém VOLTAR.
- Owner com opção ligada espera close, retorna uma vez e não repete close no detach. Pausa artificial de close mantém callback bloqueado até encerramento; recusa, cancelamento, erro, UNKNOWN, cleanup falho e consumidor destruído não geram retorno automático de sucesso.
- Fábrica real PluginSaqueCartaoApp, attach da View, coleta de estado, owner e callback foram executados com ScreenSaqueRepository exclusivamente sintético. CAPTURED retornou CAPTURED, sem mudança de status e sem botão VOLTAR; método antigo exibiu Captura de homologação concluída e esperou VOLTAR. Repository Go/serial não foi usado nesses testes.
- Host encaminha pedido e opções sem mudança e só usa nova faceta quando disponível. Provedor que suporta somente1.4 recebe zero chamadas quando se exige personalização, retornando UNAVAILABLE sem fallback. Consumidor Operações continua no contrato antigo.
- Ingressos aceita CAPTURED como conclusão somente demonstrativa por instrução explícita do usuário; cleanup confirmado e ID de pedido continuam obrigatórios. Não há conversão em APPROVED nem autorização financeira nova. APP/CAP mostram a tela própria do plugin, retornam em3s à seleção inicial e conservam lugar vendido, sem segunda chamada financeira.

## Inspeção visual

Capturas abertas e verificadas no processo do host: [sucesso após CAPTURED](evidence/ingressos-captured-sucesso.png) e [novo atendimento](evidence/ingressos-captured-novo-atendimento.png). A confirmação mostra texto solicitado e aviso de demonstração/sem comprovação financeira; depois o plugin volta sem seleção/pedido anterior. São testes com componente sintético no host, complementados pelo teste independente da fábrica real do componente com repository sintético. Nenhuma validação física foi alegada.

## Inventário e KDoc

- `shared-api/CardWithdrawalComponent.kt`: enum/opções/facetas novas documentadas, incluindo status original e momento pós-cleanup. Tipos/métodos monetários antigos não alterados. `PluginContracts.kt` e publicação local documentam minor1.5; autenticação1.2 permanece compatível.
- `BusinessPluginManager.kt`: contexto do consumidor implementa faceta nova e mantém métodos antigos/identidade ativa. Override herda contrato; helper continua documentado.
- `ComponentPluginManager.kt`: overload de personalização documentado; mesmo Invocation/finish para restauração/callback, sem fallback que descarte opções.
- `PluginSaqueCartaoApp.kt`: KDoc de factory injetada, construtor público, overload e criação por View; manifestos binário/JSON e versão1.5.0/minor5 coerentes.
- `SaqueJourney.kt`: parâmetro default-false documentado; finalização única/closedState/retorno mantêm documentação e guards; alteração só após cleanup, sem mudar sequência Go.
- `PluginIngressosApp.kt`: usa faceta/opção CALLER, minor5/versão1.1.0 coerentes no JSON/binário; ausência de faceta é indisponibilidade. `CinemaJourney.kt` documenta CAPTURED somente demonstrativo e mantém guards por ID.
- Testes SharedApi/owner/manifesto/fábrica real, host e bilheteria atualizados conforme novos contratos. `PluginSaqueReturnTest.kt` documenta fixtures sem I/O físico. Nenhum segredo, conteúdo de PIN/KSN real ou arquivo privado de usuário foi registrado.

Sem Dokka configurado; nenhuma ferramenta de documentação foi adicionada. Cores/helpers privados triviais e overrides com documentação herdada correta mantêm exclusões justificadas.

## Pendências

Gate estrutural local.properties, teste humano físico pelo usuário e revisão/validação/aprovação formais permanecem pendentes. Artefatos estão prontos para esse teste, mas não comprovam autorização financeira real. Não houve archive/commit ou mudança de perfil, chave, protocolo, Go, Bridge ou AAR.
