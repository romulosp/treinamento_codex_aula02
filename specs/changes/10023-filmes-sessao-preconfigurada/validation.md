# Evidências — sessão pré-configurada

## Estado
`IMPLEMENTADA` para teste humano, com testes técnicos aprovados. Gate estrutural preexistente reprovado; revisão da implementação, validação e aprovação formais permanecem pendentes. Sem archive, alteração de specs/system ou commit nesta etapa.

## Ambiente
08/11/2026, Windows/PowerShell, workspace `D:/desenvolvimento/ia/aula02`; projeto em `apps/frontend/smartphone/sistema-prototipo-android`. Gradle Wrapper 9.6.0, JBR OpenJDK 25.0.3 em `D:/desenvolvimento/ferramentas_android/Nova pasta/AndroidStudio/jbr`, alvo Java 17; Python 3.14.6. Emulador `emulator-5554`, Medium_Tablet/Pixel_Tablet, Android 15/API 35, configuração da 10022. JAVA_HOME definido apenas no processo de cada comando Gradle. Dados e componente financeiro sintéticos; nenhuma operação física automática.

## Comandos e resultados

Gradle no diretório Android; demais comandos na raiz. Arquivos e mudanças alheias preexistentes foram preservados. A alteração de 10023 substitui o cadastro manual anteriormente solicitado na 10020/10022 conforme instrução mais recente do usuário.

| ID | Comando / procedimento | Resultado e evidência | Exit |
| --- | --- | --- | ---: |
| VAL-001 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | Reprovado por local.properties local preexistente, preservado sem expor conteúdo. | 1 |
| VAL-002 | `./gradlew.bat :plugin-ingressos:testDebugUnitTest :plugin-ingressos:ingressosCoverageVerify :plugin-ingressos:lintDebug :plugin-ingressos:assembleDebugAndroidTest :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` | Primeira execução interrompida por import indevido de assertDoesNotExist, método de interação sem import top-level; import removido. `gradle-build.log`. | 1 |
| VAL-003 | Mesmo comando Gradle após correção | PASS: 11 JVM, 75/75 linhas elegíveis (100%), lint sem erros; build de plugin, host e test APKs. `gradle-final.log`. | 0 |
| VAL-004 | `adb -s emulator-5554 install -r <plugin-apk>` e `<plugin-test-apk>`; `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginingressos.test/androidx.test.runner.AndroidJUnitRunner` | Primeira execução: 8 testes/1 falha de preparação do foco. Segunda execução após foco semântico explícito: mesma falha porque InputMode permanecia Touch. Logs `instrumentation.log` e `instrumentation-final.log`; fluxo por toque e outros sete cenários passaram. Nenhuma correção de foco em produção foi feita. | 0 shell / JUnit FAIL |
| VAL-005 | `./gradlew.bat :plugin-ingressos:assembleDebugAndroidTest :plugin-ingressos:lintDebug --console=plain` | PASS das atualizações de teste. Logs `gradle-ui-final.log` e `gradle-keyboard-final.log`. Consulta oficial comprovou necessidade de InputMode.Keyboard no teste sem teclado físico; preparação adicionada somente ao teste. | 0 |
| VAL-006 | Instalar test APK atualizado; `adb -s emulator-5554 shell am instrument -w -e class br.com.romulopenha.sistemaprototipoandroid.pluginingressos.CinemaScreenTest#tecladoSelecionaFilmeComProgramacaoFixa br.com.romulopenha.sistemaprototipoandroid.pluginingressos.test/androidx.test.runner.AndroidJUnitRunner` | PASS: `OK (1 test)` após correção fundamentada do ambiente. `keyboard.log`. A partir de foco nativo conhecido, TAB foca Avatar 3 e ENTER seleciona o filme com seus dados fixos. | 0 / JUnit PASS |
| VAL-007 | Repetir suíte completa de instrumentação de VAL-004 com APK final | PASS: `OK (8 tests)`. `instrumentation-verified.log`. Quatro capturas extraídas com `adb exec-out run-as <pacote-plugin> cat cache/<arquivo>` e assinatura PNG verificada. | 0 / JUnit PASS |
| VAL-008 | Instalar host/test APK com `adb -s emulator-5554 install -r <apk>`; `adb -s emulator-5554 shell am instrument -w -e class br.com.romulopenha.sistemaprototipoandroid.platform.IngressosCardIntegrationTest br.com.romulopenha.sistemaprototipoandroid.test/androidx.test.runner.AndroidJUnitRunner` | PASS: `OK (1 test)`. APK real carregado no processo host, imagem carregada, Batman selecionado sem cadastro; componente sintético recebe exatamente 4400 centavos e ID não vazio, uma chamada, retorno APPROVED/cleanup habilita emissão. `host-integration.log`. | 0 / JUnit PASS |
| VAL-009 | Comparações SHA-256 contra baseline, inspeção ZIP do host/plugin e leitura dos XML de testes/cobertura | PASS: contrato compartilhado, BusinessPluginManager e SaqueJourney idênticos; APK embutido corresponde ao produzido; quatro WebPs por classloader. `baseline.json` e `artifacts.json`. | 0 |
| VAL-010 | `adb -s emulator-5554 shell am start -W -n br.com.romulopenha.sistemaprototipoandroid/.MainActivity` | Host final já instalado por VAL-008; `Status: ok`, aberto para teste humano. Nenhum login ou uso físico de cartão/PIN automatizado. | 0 |

## Cenários comprovados

- Filme → configuração automática: carrossel carrega sala, horário, preço e ocupados próprios; não há formulário nem callback/controle de escolha de horário. Repetir filme atual não apaga seleção ou pedido.
- Modelo: configuração inválida rejeitada, filme desconhecido não substitui dados, ocupado/vendido não selecionável, cancelamento não vende, pedido congela filme/assento/preço/ID, resultados locais e cleanup continuam protegendo emissão e nova tentativa.
- Estoque: vendas locais conservadas ao voltar ao filme, sem contaminar sessões diferentes; ocupados configurados não geram recibos locais.
- Interface: ausência de Configurar/Cadastrar sessão, campos de horário/preço e ação de clique no texto da sessão; pedido desabilita troca de filme; largura de 380dp permite acessar B6 e pedido por rolagem; título longo com fonte 150% preserva ações.
- Host: descoberta/arte do APK independente e chamada sintética real mediada pelo host, sem alterar shared-api ou componente financeiro.

Total final: **11 JVM + 8 Compose + 1 integração no host**, zero falhas. Cobertura elegível de CinemaJourney: **75/75 linhas**, regra mínima 80% aprovada. Lint: **0 erros/3 avisos** de configuração preexistentes (OldTargetApi, GradleDependency, MissingApplicationIcon). Aviso de depreciação de createComposeRule mantido; sem migração ou nova dependência.

## Inspeção visual

Capturas abertas e verificadas: [amplo](evidence/ingressos-catalogo-amplo.png), [compacto](evidence/ingressos-catalogo-compacto.png), [fonte ampliada](evidence/ingressos-catalogo-fonte-ampliada.png). O horário aparece com rótulo SESSÃO PROGRAMADA, acompanhado de sala e preço; não há Configurar sessão no topo ou diálogo. Mapa apresenta ocupados configurados em cinza e pedido selecionado em azul; resumo branco e carrossel preservam a referência 10022 r4. Compacto/fonte ampliada permitem rolagem; nada depende de campos editáveis. Captura adicional de ação: `ingressos-catalogo-compacto-pedido.png`.

O catálogo é local e demonstrativo, não uma conexão com configuração real de cinema ou servidor. Quatro filmes, uma sessão fixa por filme, 12 lugares e um assento por pedido conforme SPEC; sem escolha/edição de horário. Dados e vendas se perdem ao encerrar instância/processo. Não constitui venda ou autorização financeira real.

## Inventário KDoc e fronteiras

| Arquivo | Mudança / documentação |
| --- | --- |
| `plugin-ingressos/src/main/java/.../CinemaCatalog.kt` | Novos modelos CinemaConfiguredSession, CinemaMovie, CinemaCatalog e objeto DemoCinemaCatalog; KDoc de responsabilidades/propriedades, preço em centavos, IDs e ocupação. |
| `plugin-ingressos/src/main/java/.../CinemaJourney.kt` | KDoc do catálogo injetado e exceções de configuração; seleção automática sem horário editável; campos movieId/sessionId/occupied/sold documentados; mapa privado de vendas por sessão documentado. Operações financeiras existentes e suas condições preservadas. |
| `plugin-ingressos/src/main/java/.../PluginIngressosApp.kt` | KDoc da rota atualizado; removidos campos de cadastro e evento createSession, substituídos por selectMovie. Lifecycle, integração e contrato externo preservados; overrides continuam com contrato herdado. |
| `plugin-ingressos/src/main/java/.../CinemaScreen.kt` | KDoc da tela, carrossel e SessionDetails atualizado para catálogo e leitura; removido SessionEditor e estado de edição. Demais componentes mantêm documentação. Constantes visuais triviais excluídas justificadamente. |
| `plugin-ingressos/src/test/.../CinemaJourneyTest.kt` | Casos de negócio atualizados para configuração fixa, estoque por sessão e proteção do pedido. |
| `plugin-ingressos/src/androidTest/.../CinemaScreenTest.kt` | Contratos UI e captura documentados; teclado preparado conforme fonte oficial no DESIGN, sem modificar foco de produção. |
| `app/src/androidTest/.../IngressosCardIntegrationTest.kt` | Teste seleciona Batman e confere valor configurado em vez de preencher formulário. Nenhum host de produção modificado. |

Não há tarefa Dokka configurada; nenhuma foi adicionada. Sem permissões, dependências, rede, API de geração, logs financeiros, chaves ou dados sensíveis novos. Os arquivos financeiros da baseline permanecem idênticos; mudanças de outras Changes no workspace não foram revertidas.

## Pendências

Validador estrutural continua reprovado por local.properties preexistente. Aceite humano pelo menu/login do host, revisão de implementação, validação e aprovação final seguem os gates formais. A execução técnica acima não substitui esses gates.
