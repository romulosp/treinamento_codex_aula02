# Evidências — layout de ingressos

## Estado
`IMPLEMENTADA` para teste humano. Testes técnicos aprovados; gate estrutural preexistente continua reprovado. Não representa revisão de implementação, validação formal ou aprovação final. Não arquivada e sem commit nesta etapa.

**Revisão vigente: r4**, abaixo. Os registros VAL-001 a VAL-009 e suas primeiras capturas representam a composição anterior, rejeitada visualmente pelo usuário. Sua aprovação técnica não constitui aceite visual. A revisão r4 segue a imagem de protótipo enviada posteriormente.

## Baseline e ambiente
08/11/2026, Windows/PowerShell, workspace `D:/desenvolvimento/ia/aula02`. Projeto Android em `apps/frontend/smartphone/sistema-prototipo-android`; Gradle Wrapper 9.6.0; JBR OpenJDK 25.0.3 do Android Studio (`D:/desenvolvimento/ferramentas_android/Nova pasta/AndroidStudio/jbr`), compilação alvo Java 17; Python 3.14.6. Emulador `emulator-5554`, Medium_Tablet/Pixel_Tablet, Android 15/API 35, 2560×1600 pixels, densidade 320.

O workspace já contém alterações de outras Changes, incluindo plugin-ingressos ainda não versionado. Não usar o diff global como prova do escopo desta entrega; limitar inventário aos arquivos efetivamente editados nesta Change. Nenhum comando físico autorizado por esta alteração de layout.

## Comandos e resultados

Comandos Gradle executados no diretório do projeto Android; demais comandos na raiz do workspace. `JAVA_HOME` apontou para o JBR acima somente no processo de execução, sem alteração persistente do ambiente.

| ID | Comando / procedimento | Resultado e evidência | Exit |
| --- | --- | --- | ---: |
| VAL-001 | `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | Reprovado: `local.properties` local preexistente; conteúdo não lido/exposto, arquivo preservado. | 1 |
| VAL-002 | Primeira extração da UI e primeira compilação | Extração local não concluída por erro de caminho/codificação; compilação encontrou declarações duplicadas. Corrigida a extração, mantendo contratos da rota. `evidence/gradle-initial.log`. | 1 |
| VAL-003 | `./gradlew.bat :plugin-ingressos:testDebugUnitTest :plugin-ingressos:ingressosCoverageVerify :plugin-ingressos:lintDebug :plugin-ingressos:assembleDebug :plugin-ingressos:assembleDebugAndroidTest --console=plain` | PASS: 6 JVM, cobertura 57/57 linhas elegíveis; lint sem erros; APK/test APK. `evidence/gradle-build.log`. | 0 |
| VAL-004 | `./gradlew.bat :plugin-ingressos:connectedDebugAndroidTest :app:assembleDebug --console=plain` | PASS: 6 Compose e host compilado. `evidence/gradle-ui.log`. Esse runner desinstalou o pacote de testes ao terminar; primeira extração posterior de imagens falhou por pacote ausente. Não é falha da UI. | 0 (Gradle); 1 (extração posterior) |
| VAL-005 | `./gradlew.bat :plugin-ingressos:testDebugUnitTest :plugin-ingressos:ingressosCoverageVerify :plugin-ingressos:lintDebug :plugin-ingressos:assembleDebugAndroidTest :app:assembleDebug --console=plain` | PASS após integração dos cartazes e teste a 150% de fonte: 6 JVM, 57/57 linhas elegíveis, lint 0 erros/3 avisos, build host/test APK. `evidence/gradle-final.log`. | 0 |
| VAL-006 | `adb -s emulator-5554 install -r <plugin-ingressos-debug.apk>` e `adb -s emulator-5554 install -r <plugin-ingressos-debug-androidTest.apk>`; `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginingressos.test/androidx.test.runner.AndroidJUnitRunner` | PASS: `OK (6 tests)` com APK final. Callback de cartão inteiramente sintético. Imagens extraídas por `adb exec-out run-as <pacote-plugin> cat cache/<arquivo>` com validação da assinatura PNG. `evidence/instrumentation-final.log`. | 0 |
| VAL-007 | Comparação SHA-256 do APK em `assets/plugins/business/plugin-ingressos.apk` dentro do host com APK produzido; inspeção ZIP do plugin | PASS: bytes do plugin correspondem, quatro WebPs, manifesto e descritor de serviço presentes. `evidence/artifacts.json`. | 0 |
| VAL-008 | Comparação SHA-256 com `evidence/baseline.json` | `CinemaJourney.kt` e `shared-api/PluginContracts.kt` idênticos à baseline desta sessão. Nenhum arquivo do host, componente financeiro ou Go foi editado nesta Change. | 0 |
| VAL-009 | `adb -s emulator-5554 install -r apps/frontend/smartphone/sistema-prototipo-android/app/build/outputs/apk/debug/app-debug.apk`; `adb -s emulator-5554 shell am start -W -n br.com.romulopenha.sistemaprototipoandroid/.MainActivity` | `Success`, `Status: ok`; host atualizado e aberto para teste humano. Nenhum login ou pagamento físico automatizado. | 0 |

## Cenários e inspeção visual

- Fluxo existente: cadastrar Exemplo/Sala 1/20h/R$12,50 → A1 → criar pedido → chamada sintética com 1250 centavos e uma única invocação → APPROVED/cleanup → emissão demonstrativa.
- Estado puro: A1 selecionado, B1 vendido/desabilitado, B2 disponível com callback correto; pedido pendente bloqueia mapa e mantém ação de cartão.
- Compacto com 380dp: seleção e pedido acessíveis por rolagem; amplo com 1000dp: resumo lateral e mapa em A/B.
- TAB a partir de Filme move foco para Sala usando controles nativos.
- Título longo com fonte 150% mantém ação acessível e mostra capa Avatar.

Capturas abertas e inspecionadas: [amplo](evidence/ingressos-layout-amplo.png), [compacto/pedido](evidence/ingressos-layout-compacto-pedido.png), [fonte ampliada](evidence/ingressos-fonte-ampliada.png). Tipografia, cartazes, estados e resumo sem sobreposição nessas capturas. A rolagem recorta naturalmente parte do conteúdo fora do viewport; o mapa compacto permite rolagem horizontal. A imagem [compacto inicial](evidence/ingressos-layout-compacto.png) também foi extraída.

## Imagens e segurança

Quatro originais PNG 1024×1536 em `output/imagegen/cinema/`; quatro WebPs 512×768 totalizam 476.962 bytes no módulo. Geração integrada, prompts em [evidence/poster-prompts.md](evidence/poster-prompts.md), metadados/hashes em [evidence/posters.json](evidence/posters.json). As imagens foram inspecionadas durante a geração. Batman usa cidade noturna; Spider-Man e títulos sem capa própria usam cinema genérico. A indisponibilidade da referência específica foi comunicada ao usuário.

Tentativas Gemini: três HTTP 429, sem imagem; execução interrompida por instrução do usuário. Chave não impressa, copiada, registrada, incluída nos assets ou enviada ao gerador integrado; runner temporário removido. Nenhuma nova dependência, permissão ou API de rede no Android. Não foram acessados dados de cartão/PIN, comandos financeiros ou perfis privados.

## Inventário de código e KDoc

| Arquivo | Mudança e documentação |
| --- | --- |
| `plugin-ingressos/src/main/java/.../PluginIngressosApp.kt` | Apresentação extraída; Modifier opcional da rota repassado à tela; lifecycle e callbacks preservados. KDoc da rota atualizado. Classe mantém KDoc existente; overrides herdados não criam contrato novo. |
| `plugin-ingressos/src/main/java/.../CinemaScreen.kt` | Tela, cabeçalho, sessão/editor, cartaz local, mapa, botão de assento, legenda, resumo e rótulo de seção têm KDoc de apresentação/eventos/limites. `formatPrice` documentado e preservado. Cores privadas excluídas por serem constantes visuais triviais. |
| `plugin-ingressos/src/androidTest/java/.../CinemaScreenTest.kt` | Fluxo sintético preservado; cinco cenários focados adicionados, helpers de renderização/captura documentados. Sem alteração de testes JVM ou lógica da jornada. |

Sem tarefa Dokka configurada no módulo; não adicionada ferramenta de documentação. Lint mantém avisos de configuração preexistentes `OldTargetApi`, `GradleDependency` e `MissingApplicationIcon`. Testes mantêm aviso de depreciação `createComposeRule`; runner avisa sobre Unsafe do protobuf. Não impedem os resultados técnicos acima.

## Pendências formais

Gate estrutural não aprovado por arquivo local preexistente. Teste humano pelo login/menu do host, revisão da implementação, validação formal e aprovação final permanecem pendentes. Esta Change não atualiza `specs/system/`, não é arquivada e não produz commit antes desses gates.

## Revisão r4 — correção conforme imagem do protótipo

O usuário rejeitou a primeira composição e forneceu imagem com carrossel horizontal largo, horários abaixo, mapa com ícones de poltrona e resumo branco. Removidos slogan, etapas numeradas, painel único de sessão e cabeçalho azul escuro. O cadastro passou a diálogo acionado por Configurar sessão; selecionar capa preenche somente o rascunho, sem criar sessão/pedido automaticamente. Não houve nova geração de imagens ou acesso à chave/API Google.

### Evidências técnicas r4

Mesmo ambiente registrado acima. Comandos Gradle no projeto Android; comandos Python/adb na raiz. Logs de versões intermediárias preservados; os resultados finais estão identificados abaixo.

| ID | Comando / procedimento | Resultado e evidência | Exit |
| --- | --- | --- | ---: |
| VAL-010 | Mesmo validador estrutural de VAL-001, antes da implementação r4 | Reprovado pelo mesmo `local.properties` preexistente, preservado sem leitura de conteúdo. | 1 |
| VAL-011 | `./gradlew.bat :plugin-ingressos:testDebugUnitTest :plugin-ingressos:ingressosCoverageVerify :plugin-ingressos:lintDebug :plugin-ingressos:assembleDebugAndroidTest :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` | PASS; builds intermediários em `gradle-r4.log` e `gradle-r4-final.log`. | 0 |
| VAL-012 | Instalar APK/test APK do plugin com `adb -s emulator-5554 install -r <apk>` e executar `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginingressos.test/androidx.test.runner.AndroidJUnitRunner` | Primeira execução: 7 testes/1 falha. Teste tentava rolar o botão fixo de confirmação do novo diálogo; corrigido para clique direto. Não houve falha de regra de negócio. `instrumentation-r4.log`. | 0 shell / JUnit FAIL |
| VAL-013 | Mesmo comando Gradle de VAL-011 após correções e recorte superior dos cartazes | PASS: 6 JVM, 57/57 linhas elegíveis (100%), lint 0 erros/3 avisos; host, plugin e test APKs. `gradle-r4-release.log`. | 0 |
| VAL-014 | Reinstalar APK/test APK finais do plugin e repetir instrumentação de VAL-012 | PASS: `OK (7 tests)`. `instrumentation-r4-final.log`. Extração binária das cinco capturas via `adb exec-out run-as <pacote-plugin> cat cache/<nome>` com assinatura PNG verificada. | 0 / JUnit PASS |
| VAL-015 | Instalar APK/test APK do host; `adb -s emulator-5554 shell am instrument -w -e class br.com.romulopenha.sistemaprototipoandroid.platform.IngressosCardIntegrationTest br.com.romulopenha.sistemaprototipoandroid.test/androidx.test.runner.AndroidJUnitRunner` | PASS: `OK (1 test)` com APK real carregado por DexClassLoader; imagem Batman efetivamente carregada no processo host, cadastro/pedido e componente sintético recebem 1250 centavos, retornando APPROVED com cleanup. `host-integration-r4.log`. | 0 / JUnit PASS |
| VAL-016 | Inspeção ZIP e SHA-256 de host/plugin; hashes de CinemaJourney.kt e PluginContracts.kt contra baseline | PASS: host contém APK idêntico ao produzido e quatro WebPs em `cinema/posters/`; núcleo e contrato idênticos à baseline. `artifacts-r4.json`. | 0 |
| VAL-017 | `adb -s emulator-5554 shell am start -W -n br.com.romulopenha.sistemaprototipoandroid/.MainActivity` após instalação do host final em VAL-015 | `Status: ok`, host final aberto para teste humano. Nenhum login ou pagamento físico automatizado. | 0 |

### Inspeção visual r4 e limites

Capturas abertas e verificadas: [composição ampla](evidence/ingressos-prototipo-amplo.png), [compacto](evidence/ingressos-layout-compacto-r4.png), [fonte 150%](evidence/ingressos-fonte-ampliada-r4.png). Verificados cards largos no topo, três referências visíveis na captura ampla, borda azul na sessão atual, gradiente azul claro, horário abaixo, barra cinza TELA, desenho de encosto/braços/base e resumo inteiramente branco. Corrigido recorte central que cortava o rosto no cartaz de Coringa; carrossel usa alinhamento superior. Compacto mantém rolagem vertical e horizontal, acesso ao assento B6 e ao botão de pedido. Título longo/fonte ampliada permanecem legíveis e ações acessíveis. Demais capturas: `ingressos-layout-amplo-r4.png` e `ingressos-layout-compacto-pedido-r4.png`.

Diferenças de dados em relação à imagem do usuário são explícitas e preservam o escopo: somente 12 lugares reais da demonstração, um horário cadastrado e um assento por pedido; sem classificação indicativa/tecnologia inventadas, meia-entrada, novas sessões ou assentos fictícios. Galeria de capas não é catálogo de disponibilidade. Nova capa não substitui sessão com pedido/venda; controles ficam desabilitados e a jornada continua protegida.

### Recursos no host e inventário KDoc r4

Detectado que `BusinessPluginManager.createScreen` recebe Context do host, sem Resources do APK externo. Os WebPs foram movidos para `plugin-ingressos/src/main/resources/cinema/posters/` e carregados pelo classloader da própria entry class; `produceState` coordena decodificação em `Dispatchers.IO`, streams fechados e saída da composição. Isso evita resolver IDs R do plugin contra recursos do host. Teste VAL-015 comprova o cenário real sem instalar ou depender do plugin como aplicativo autônomo no host. Nenhum arquivo de produção do host foi editado nesta revisão.

`CinemaScreen.kt`: KDoc em tela, carrossel, chips, diálogo, mapa, botão, desenho de poltrona, legenda, resumo, resolução de arte, carregamento de imagem e formatter. Constantes visuais triviais continuam excluídas justificadamente. `CinemaScreenTest.kt`: casos anteriores adaptados ao diálogo e rótulo visual do botão; novo caso prova atalho de capa sem cadastro automático e navegação por seta; teste de pedido comprova bloqueio da galeria. `app/src/androidTest/.../IngressosCardIntegrationTest.kt`: apenas teste atualizado para novo diálogo/rótulo e comprovação da imagem carregada. Nenhuma alteração em CinemaJourney, shared-api, host de produção, componente financeiro ou Go/Bridge.

Resultado: **6 testes JVM + 7 testes Compose + 1 integração no host**, todos aprovados na versão final. Lint mantém três avisos de configuração já registrados; sem erros. Validador estrutural e aceite visual/manual do usuário seguem pendentes; a revisão r4 não constitui aprovação formal da implementação.
