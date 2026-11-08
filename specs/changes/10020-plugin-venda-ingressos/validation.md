# Validação

## Estado
`IMPLEMENTADA` para teste manual; não representa validação formal, aprovação final ou venda real.

## Ambiente
Windows, PowerShell, Gradle Wrapper 9.6.0, JBR do Android Studio, bytecode Java 17, emulador Medium_Tablet Android 15/API 35. Diretório dos comandos Gradle: `apps/frontend/smartphone/sistema-prototipo-android`.

| ID | Comando | Resultado | Exit |
| --- | --- | --- | ---: |
| VAL-001 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` na raiz | Detecta `local.properties` local preexistente; arquivo não foi exposto nem removido. | 1 |
| VAL-002 | `./gradlew.bat :plugin-ingressos:testDebugUnitTest :plugin-ingressos:ingressosCoverageVerify :plugin-ingressos:lintDebug :plugin-ingressos:assembleDebugAndroidTest :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` | PASS: 4 testes JVM; núcleo elegível 41/41 linhas; lint sem erros, dois avisos OldTargetApi/MissingApplicationIcon; build host e testes. | 0 |
| VAL-003 | `./gradlew.bat :plugin-ingressos:connectedDebugAndroidTest --console=plain` | PASS: 1 teste Compose de cadastro, pedido e emissão simulada no emulador. | 0 |
| VAL-004 | `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=br.com.romulopenha.sistemaprototipoandroid.platform.IngressosDiscoveryTest --console=plain` | PASS: 1 teste; descobre ingressos e Operações e cria View do plugin pelo host. Não realiza login real. | 0 |
| VAL-005 | `adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk` e `adb -s emulator-5554 shell am start -W -n br.com.romulopenha.sistemaprototipoandroid/.MainActivity` | Instalação e abertura do host em login. | 0 |
| VAL-006 | `./gradlew.bat :app:connectedDebugAndroidTest --console=plain`, primeira tentativa | Falhou porque emulador ficou offline durante inicialização da infraestrutura Docker; nenhuma conclusão de regressão funcional. | 1 |
| VAL-007 | Mesmo comando após reiniciar o emulador | PASS: 28 testes instrumentados do host, zero falhas. | 0 |
| VAL-008 | Instalação e abertura repetidas conforme VAL-005; `adb shell toybox nc -z -w 3 10.0.2.2 8180` | Host aberto em login; emulador alcança a porta do autenticador. Login com credenciais reais ainda não foi testado. | 0 |

Para disponibilizar a autenticação local, Docker Desktop e os serviços do compose já existente foram iniciados. Keycloak reiniciava por ausência do banco `keycloak`; o PostgreSQL já continha `keyclock`, mas não `keycloak`. Criado o banco faltante conforme `D:/desenvolvimento/keyclock/init-scripts/01-create-keycloak-db.sql`, preservando os bancos anteriores. Keycloak importou a configuração local prevista no compose e respondeu HTTP 200; o autenticador Quarkus existente iniciou na porta 8180. Não foram consultadas ou impressas credenciais neste registro. A criação do banco altera persistentemente o ambiente local e não foi revertida.

O compilador avisou sobre depreciação de createComposeRule usado no teste; não impediu execução. O runner registrou aviso de Unsafe do protobuf. O verificador estrutural de VAL-001 não passou; resultado de testes não substitui esse gate. Teste humano, autenticação real, revisão e validação formais ainda devem ser registrados. Nenhum hardware financeiro foi usado. KDoc foi incluído nos tipos e operações adicionados; o inventário completo deve ser avaliado na revisão formal.

## Correção r3 — componente de cartão e visual

O teste humano encontrou ausência de chamada do componente e visual insatisfatório; a entrega anterior foi substituída. A SPEC r3 exige a chamada real mediada pelo host, sem transmitir valor ou supor autorização. A tela foi redesenhada com mapa de assentos e layout adaptativo; captura de teste sintético foi inspecionada visualmente.

| ID | Comando / ação | Resultado | Exit |
| --- | --- | --- | ---: |
| VAL-009 | Validador estrutural Android na raiz | Reprovado por `local.properties` preexistente, sem removê-lo. | 1 |
| VAL-010 | `./gradlew.bat :plugin-ingressos:testDebugUnitTest :plugin-ingressos:ingressosCoverageVerify :plugin-ingressos:lintDebug :app:assembleDebug :plugin-ingressos:connectedDebugAndroidTest --console=plain` | PASS: 6 testes JVM, núcleo elegível 56/56 linhas; teste Compose sintético e build passaram; lint 0 erros, 3 avisos. | 0 |
| VAL-011 | `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=br.com.romulopenha.sistemaprototipoandroid.platform.IngressosCardIntegrationTest --console=plain` | PASS: APK real de ingressos chama componente sintético através do host e recebe retorno sem acesso a hardware. | 0 |
| VAL-012 | Primeira execução do teste Compose r3 | Falhou por asserção com texto ambíguo entre recibo e mensagem; seletor corrigido, execução posterior PASS em VAL-010. | 1 |
| VAL-013 | `scripts/iniciar-saque-cartao.ps1 -ProvisionOnly -EmulatorSerial emulator-5554`, `adb reverse tcp:39100 tcp:39100` e abertura do host | Três snapshots privados provisionados com hashes confirmados; host aberto em login. Hello/Ping PBRG passaram separadamente, sem comando físico. | 0 |

O Bridge responder a Ping não comprova abertura de porta serial, chaves ou funcionamento do pinpad. Nenhum cartão ou PIN foi utilizado automaticamente. Teste manual do usuário e gates formais seguem pendentes.

## Diagnóstico de BRIDGE_UNREACHABLE — 2026-11-07

| ID | Comando / ação | Resultado | Exit |
| --- | --- | --- | ---: |
| VAL-014 | `adb -s emulator-5554 reverse --list`; TCP do emulador a localhost:39100 | Reverse ausente e conexão recusada; Bridge físico do usuário já escutava em 127.0.0.1:39100. | 1 no teste TCP |
| VAL-015 | `adb -s emulator-5554 reverse tcp:39100 tcp:39100`; listagem e teste TCP | Reverse restaurado; TCP do emulador passou. | 0 |
| VAL-016 | Hello/Ping PBRG em listener local e pelo caminho adb forward temporário → emulador → reverse → Bridge | PASS; forward temporário removido, listener e processo do usuário preservados. Sem comando físico. | 0 |
| VAL-017 | `adb exec-out toybox nc` como teste alternativo de PBRG | Timeout do processo de teste; substituído pela verificação bem-sucedida em VAL-016. | 1 |
| VAL-018 | Leitura restrita das condições do perfil privado | Host local e porta 39100 corretos; perfil, tabela e propriedades presentes; conteúdo sensível não registrado. | 0 |

Imagem e saída fornecidas pelo usuário confirmam configuração COM7 no Bridge.
Nenhuma hipótese de falha de chave ou resultado de pagamento pode ser concluída
da falha de ping. Não houve OPN, captura, PIN ou compra automatizada; usuário
precisa sair da tela de erro e iniciar nova jornada manualmente.

## Comparação 074 / 069 / 068 e diagnóstico no processo Android

- 074: Ping é preflight sem COM; Open executa handshake e OPN; localhost depende
  de adb reverse, e 10.0.2.2 é override explícito suportado.
- 068: GoMobileRepository cria Mobile.newClient e chama ping/open no mesmo
  contrato, com host default localhost e override editável 10.0.2.2.
- 069: laboratório funcional consome o catálogo por esse repository; não
  acrescenta inicialização alternativa que substitua Open.
- Componente de cartão: attach chama owner.load; a jornada chama loadProfile,
  repository.open, displayCapabilities e loadTable. Repository cria cliente,
  chama Ping, Open e resolução PIN por GIX. Portanto há inicialização; a falha
  de ping impede alcançar Open. Não foi removida a sequência existente.

| ID | Comando / ação | Resultado | Exit |
| --- | --- | --- | ---: |
| VAL-019 | `./gradlew.bat :app:assembleDebugAndroidTest --console=plain` | Teste opt-in BridgeConnectivityTest compilado; não participa do fluxo comum sem realBridge=true. | 0 |
| VAL-020 | `adb shell am instrument -w -e class br.com.romulopenha.sistemaprototipoandroid.platform.BridgeConnectivityTest -e realBridge true -e bridgeHost localhost br.com.romulopenha.sistemaprototipoandroid.test/androidx.test.runner.AndroidJUnitRunner`, sem reverse | JUnit: 1 falha, BRIDGE_UNREACHABLE/ping; reproduz o erro da UI dentro do processo host usando AAR real. O shell am instrument retorna 0 apesar da falha JUnit. | 0 shell / JUnit FAIL |
| VAL-021 | Mesmo teste com bridgeHost=10.0.2.2 | OK, 1 teste; Ping real e estado CLOSED, sem ACQUIRE/OPN. | 0 / JUnit PASS |
| VAL-022 | Mesmo teste com localhost após restaurar reverse | OK, 1 teste; confirma dependência de encaminhamento para localhost. | 0 / JUnit PASS |
| VAL-023 | Alteração controlada do perfil DES externo e comparação com backup | Somente campo host mudou para 10.0.2.2; backup externo preservado, demais campos idênticos. | 0 |
| VAL-024 | Launcher ProvisionOnly ValidateOnly e depois ProvisionOnly | Três snapshots privados provisionados com hashes confirmados. | 0 |
| VAL-025 | BridgeConnectivityTest com bridgeHost=profile | OK, 1 teste lendo endpoint do perfil privado do host e usando o binding real. Nenhum comando físico. | 0 / JUnit PASS |

Perfil alterado: `D:/desenvolvimento/chave_des/perfil_saque_cartao_des.json`.
Backup: mesmo diretório, sufixo `.before-10020-endpoint-20261107`.
Decisão documentada na ADR-002. Isso corrige a dependência do reverse neste
emulador; não prova a finalização física do pagamento. O processo Bridge do
usuário foi mantido e os parâmetros de COM/PIN/EMV não foram alterados.
