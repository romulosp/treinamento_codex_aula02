# Validação — 10010-estilo-menu-aplicacao

**Fase:** IMPLEMENTADA para teste humano. As evidências abaixo são verificações técnicas, sem revisão de implementação, validação formal ou aprovação.
**Ambiente:** Windows, PowerShell, workspace D:/desenvolvimento/ia/aula02, data 2026-10-03.

## Evidências documentais
Leitura de workflow.md, convenções de evidência, skill Android base e referências de workflow/arquitetura/quality gates. Inspeção de MainActivity.kt e AuthenticationHeader.kt confirmou Compose, callbacks, agrupamento, vazio, fatal e dados atuais. Comandos Get-Content/rg/git status terminaram com código 0.

Verificação documental por script: arquivos obrigatórios da fase, status SPEC_APROVADA em proposal/spec, revisão e origem íntegra. Resultado e comando registrados após execução abaixo.

## Ambiente técnico

Windows 10 amd64, PowerShell; Gradle Wrapper 9.6.0, launcher Java 17.0.11, daemon Java 25 conforme configuração preexistente. SDK em D:/desenvolvimento/ferramentas_android/Sdk; compile 37/target 36/min 29, versões preservadas. Emulador emulator-5554, Pixel Tablet/Medium_Tablet(AVD), Android 15/API 35, resolução física 2560×1600 e densidade 320 dpi (2 px/dp).

## Comandos de implementação

Gradle executado em `apps/frontend/smartphone/sistema-prototipo-android`; validador e Git na raiz, salvo check-ignore no diretório Android.

| Comando | Exit code | Resultado |
| --- | ---: | --- |
| `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | 1 | Única ocorrência: local.properties preexistente. O script trata qualquer presença local como violação, sem distinguir tracking. Arquivo ignorado pelo Git e preservado. |
| `git check-ignore local.properties` | 0 | Configuração local do SDK não versionada. |
| `./gradlew.bat :app:tasks --all` | 0 | Descoberta das tarefas reais; sem Dokka/cobertura configurados. |
| `./gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:compileDebugAndroidTestKotlin` | 0 | Build, JVM app, lint e compilação instrumentada passaram. |
| `./gradlew.bat :app:connectedDebugAndroidTest` (após correções) | 0 | 19 testes passaram, 0 falhas; 39 s. |
| `./gradlew.bat :app:assembleDebug testDebugUnitTest lintDebug :app:connectedDebugAndroidTest :plugin-login:connectedDebugAndroidTest` | 0 | 21 JVM (app 9, login 7, shared-api 3, negócio 1, saque 1); 24 instrumentados (app 19, login 5); lint de todos os módulos sem erros. 1 min 11 s. |
| `./gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` (após insets da Activity) | 0 | Build/JVM/lint app repetidos em 5 s. Nenhum composable instrumentado alterado por esse ajuste de wiring. |
| `git diff --check` | 0 | Sem erro de whitespace. Avisos Git de LF/CRLF. |
| `adb pull /data/local/tmp/menu10010-*.png` (dez arquivos enumerados individualmente) | 0 | Capturas preservadas em evidence/. |
| `adb install -r apps/frontend/smartphone/sistema-prototipo-android/app/build/outputs/apk/debug/app-debug.apk` (raiz) | 0 | Success: APK final instalado no emulator-5554 para teste humano, sem abrir sessão ou executar logout. |

Logs em `evidence/gradle-final.log`, `evidence/gradle-insets.log`; XML em `evidence/app-instrumented.xml`, `evidence/plugin-login-instrumented.xml`. HTML de lint/JVM/instrumentados nos build/reports dos módulos. Lint: 15 avisos (8 app, 4 login, 2 negócio, 1 saque), sem erros; incluem versões/target, backup/ícone e código existente de Views. Não ampliado escopo para suprimir avisos.

Diagnósticos corrigidos nesta implementação: duplicidade temporária de função durante extração (compile exit 1); teste de teclado iniciado em modo touch (instrumentados exit 1); overlay ripple alterando tokens hover (instrumentados exit 1). Teste prepara modo Keyboard/foco antes das teclas. Botão mantém semântica nativa e feedback visual próprio, com ripple local desativado. Rodadas finais incluem as correções. Primeiro pull das capturas após desinstalação automática falhou (adb exit 1); testes agora preservam cópias via UiAutomation em /data/local/tmp.

## Cenários e capturas

BusinessMenuScreenTest: vazio, saída/callback, disabled durante logout, fatal/reconhecimento, agrupadores aninhados não acionáveis, ordenação por ordem/id, Tab/Enter/Space acionando uma vez. MenuActionButtonTest: cores hover/press/disabled, ausência de escala com movimento reduzido, foco dourado externo. Testes existentes de header, login e descoberta/criação de tela de saque passaram, sem alterar arquivos de outras Changes.

MenuLayoutTest: cards 240×64 dp, breakpoints do título, dados longos integrais na semântica, saída fixa a 72 dp da base e 24/16 dp da direita, último item acessível por scroll. Fixtures de área útil/densidade/fontScale: 1024×768/1/1; 1366×768/1/1; 1920×1080/1/1; 1366×768/0,75/1; 360×640/2/1,3. Dez imagens inicial/rolada em `evidence/menu10010-*.png`; dados de teste ausentes da produção. Imagem 1366×768 inicial inspecionada tecnicamente; aceite humano pendente.

Essas densidades são locais ao fixture Compose, não cinco dispositivos físicos. Capturas recortam o fixture e excluem barras do sistema. MainActivity aplica safeDrawingPadding uma vez somente no menu. Os testes de fixture não comprovam insets reais da Activity; conferência permanece no teste humano.

## Inventário KDoc, cobertura e segurança

| Arquivo de produção | Documentação e responsabilidade |
| --- | --- |
| MainActivity.kt | Wiring da variante e insets. KDoc existente de Activity/host preservado; onCreate herda contrato da Activity, sem nova API; logout intacto. |
| AuthenticationHeader.kt | KDoc de CoreShell/AuthenticationHeader atualizado; MenuAuthenticationHeader/MenuProfileInfo/displayRole documentados. Variante anterior preservada por default. |
| BusinessMenuScreen.kt | KDoc de BusinessMenuScreen/BusinessMenuEntries/MenuActionButton/rememberMenuMotionAllowed. Observador ANIMATOR_DURATION_SCALE limitado à composição, removido no dispose. Cores/sombras 150 ms, escala 100 ms; movimento desativado aplica snap e escala 1. |
| MenuDesignTokens.kt | Object e todas as propriedades com KDoc; tokens normativos. |

Declarações locais triviais e override ContentObserver.onChange seguem contrato do helper documentado, sem API externa. app/build.gradle.kts apenas define AndroidJUnitRunner. Novos testes MenuActionButtonTest/MenuLayoutTest e atualização de BusinessMenuScreenTest. Sem nova dependência de produção, fonte de dados, permissão, persistência ou log pessoal. LoginPlugin/LoginScreenTest staged e Change 10008 preexistentes foram preservados.

Cobertura percentual não configurada no projeto: nenhum percentual foi inventado. Os 21 JVM e 24 instrumentados comprovam cenários, sem demonstrar mínimo 80%/alvo 90%. Instrumentação de cobertura/inventário quantitativo elegível ficam pendentes do gate formal, sem adicionar dependência nesta Change visual. Não declarar qualidade formal concluída.

## Conferência estática de SAIR

SAIR → BusinessMenuScreen.onLogout → MainActivity envia established.sessionId → DynamicLoginPluginManager.logout (fila serial) → LoadedLoginPlugin.logout → LoginPlugin.logout → BackendCredentialAuthenticator. O cliente POSTa `/contexto/autenticacao-validacao-credencial/logout` com JSON `sessaoId` e aceita HTTP 204. Base debug preexistente `http://10.0.2.2:8180`, configurável por authApiBaseUrl; localhost no Android aponta ao próprio dispositivo. Sem chamada live/logout real, sem comprovação live de idempotência. O host preserva limpeza local ao concluir a tentativa.

## Entrega e limite

APK final: `apps/frontend/smartphone/sistema-prototipo-android/app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `4339B80AF85902574C21C16E11B9E3552761FFB48B4BD7E168FC87E2CC982EDD`.

Correções editoriais dos documentos consolidados substituíram `é` indevido em títulos/medidas por traço, × ou à, preservando contrato/status SPEC_APROVADA. Origem/rascunhos preservados; plano criado antes do código.

Não executados: implementation-review, implementation-validate, change-approve, git-commit, atualização system, archive ou staging. Pendentes: teste humano, insets/densidade reais, alternância de preferência de movimento no sistema, cobertura e gates formais. Continuação somente após teste humano/autorização explícita, pelo prompt executar-mudanca-spec-driven na mesma pasta.

VAL-DOC-001: script Python via PowerShell (`python -`), verificando existencia dos seis documentos da fase, status e SHA-256 da origem. Resultado PASS; codigo de saida 0.
