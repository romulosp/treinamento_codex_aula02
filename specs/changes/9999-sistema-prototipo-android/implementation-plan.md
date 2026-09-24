# Plano de implementação: 9999-sistema-prototipo-android

**Autor:** Rômulo Penha

## Status

`SPEC_APROVADA`

## Plano de continuação de 2026-09-24

1. Aprovar a revisão que preserva o microkernel e cria `:plugin-negocio`.
2. Criar o APK de negócio, seu manifesto e o bootstrap debug.
3. Carregar o plugin após a sessão, preservando falha isolada e logout atual.
4. Implementar menu, seleção, feedback e testes no plugin.

## Atualização prompt2 — compatibility review e execução

A combinação aprovada é `minSdk = 26`, `compileSdk = 37`, `targetSdk = 36`,
Compose BOM `2026.09.00`, Compose 1.12.x stable, AGP 9.4.0, Gradle 9.6.0,
KGP 2.2.10 e JDK 17. A Compatibility Review terminou em `PASS`.

A fundação foi criada e o bloqueio de ambiente foi resolvido no Android Studio,
com o SDK configurado em `D:\desenvolvimento\ferramentas_android\Sdk`. O
primeiro smoke test revelou uma falha de estado salvável em Compose; ela foi
corrigida na `MainActivity` e o APK passou por build, instalação e abertura no
emulador. As demais tarefas da implementação integral continuam pendentes.

## Fatos observados

- `proposal.md` e `spec.md` estão em `SPEC_APROVADA`, com ressubmissão formal sem pendência material.
- O projeto Android foi criado em `apps/frontend/smartphone/sistema-prototipo-android/`.
- A raiz ignora aplicações smartphone e arquivos não textuais; será necessária exceção explícita e restrita ao novo projeto, preservando a exclusão de builds, caches, `local.properties` e credenciais.
- Java 17.0.11 está disponível em `C:\Desenvolvimento\jdk-17.0.11`, mas não está no `PATH` da sessão.
- Android SDK e `adb` estão disponíveis em `D:\desenvolvimento\ferramentas_android\Sdk`; o projeto possui Gradle Wrapper 9.6.0.
- A configuração aprovada é: perfil `SIMPLE`, módulo `app`, `minSdk = 26`, `compileSdk = 37`, `targetSdk = 36`, Java 17, AGP 9.4.0, KGP 2.2.10, Gradle 9.6.0, Compose BOM estável `2026.09.00`, Activity Compose 1.13.0 e Lifecycle 2.11.0.
- O projeto não possui Sonar configurado para este módulo; aplica-se a Auditoria de Qualidade Assistida por LLM.
- A auditoria de segurança é aplicável por haver frontend Android, manifesto, dependências, entrada de senha e configuração de backup/captura. O gerador `docs/security-audit/gerar_relatorio.py` está disponível.

## Impactos prováveis

- Criar o projeto em `apps/frontend/smartphone/sistema-prototipo-android/`, incluindo Gradle Wrapper, catálogo de versões, módulo `app`, manifesto e recursos textuais.
- Ajustar `.gitignore` somente para versionar o novo projeto e seu Wrapper, mantendo artefatos gerados e dados locais ignorados.
- Criar tema e catálogo tipado com 212 variantes de destino neutras e únicas.
- Criar componentes por família: botões, rótulos/painéis/imagem neutra, entradas, senha, seleção, dados, feedback e teclados.
- Criar `CatalogUiState`, eventos, redutor/ViewModel, tela-catálogo adaptativa e menu demonstrativo local.
- Criar testes JVM para catálogo, edição Unicode, seleção, calculadora, estado e menu; testes Compose instrumentados para semântica, callbacks, foco, desabilitação, acessibilidade e capturas.
- Não haverá banco, rede, autenticação real, permissões perigosas, serviços, receivers, providers, deep links ou recursos binários legados.

## Estratégia de implementação

1. Incorporar e conferir o material de origem necessário em `source-material/`, sem usar caminhos externos durante a implementação.
2. Criar a fundação Gradle/Android e validar identidade, SDKs, módulo, Java 17 e ausência de caminhos absolutos.
3. Implementar modelos imutáveis, catálogo das 212 variantes e regras puras de edição, seleção, calculadora e menu.
4. Implementar tema neutro e componentes Compose stateless em pastas próprias, com KDoc em português do Brasil.
5. Implementar ViewModel e tela-catálogo em vertical slice: composição expandida 800 x 600, recomposição compacta/média, insets, rolagem e preservação de estado.
6. Implementar o menu demonstrativo e integrar o fluxo local de confirmação/retorno.
7. Implementar testes junto a cada comportamento e atualizar `tasks.md` somente após evidência de conclusão.
8. Confirmar por hash que nenhum PNG/JPG/GIF/TTF de `source-material/` foi copiado ao projeto.

## Testes, cobertura e qualidade

- Validador estrutural: `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android`.
- Descoberta de tarefas: `gradlew.bat tasks`.
- Build: `gradlew.bat assembleDebug`.
- Testes JVM: `gradlew.bat testDebugUnitTest`.
- Lint: `gradlew.bat lintDebug`.
- Testes instrumentados/UI: `gradlew.bat connectedDebugAndroidTest`, condicionados a emuladores API 26 e API 37 disponíveis; `targetSdk = 36` deve permanecer verificável na configuração.
- Cobertura: JaCoCo sobre lógica Kotlin elegível, mínimo 80% de linhas e branches; Composables declarativos, Activity e código gerado só podem ser excluídos com justificativa em `validation.md`.
- Screenshots: teste instrumentado captura estados determinísticos e valida dimensões/conteúdo; a validação executa janelas mínimas e registra comparação humana em 800 x 600.
- KDoc: inventariar todo `.kt` de produção, documentar declarações públicas/protegidas e regras internas não óbvias; Dokka não será adicionado porque não é exigido nem preexistente.
- Sem Sonar disponível, executar build, compilação, lint, testes, cobertura, inventário teste-produção e revisão assistida de bugs, vulnerabilidades, hotspots, tratamento de erro, duplicação, código morto, complexidade e documentação.
- Registrar ambiente, versões, comandos, códigos de saída e artefatos em `validation.md`.

## Auditoria de segurança

- Inspecionar manifesto, exportação da Activity, permissões, backup, cleartext, captura de tela, logs, entrada de senha, armazenamento, fixtures, recursos, dependências e segredos.
- Verificar que a senha é mascarada, não persistida nem registrada e que `FLAG_SECURE` protege a tela demonstrativa.
- Verificar por hashes e extensão que nenhum binário legado entra no módulo ou APK.
- Executar a Skill `security-audit`, registrar evidências em `validation.md` e gerar relatório PDF atual identificado pela Change em `docs/security-audit/`.
- Se houver achado confirmado dentro da SPEC, corrigir, repetir revisão da implementação, validação e auditoria. Achado que exigir nova decisão ou ação externa bloqueia a Change.

## Riscos, dúvidas e decisões necessárias

- Risco operacional: Android SDK e emuladores não estão instalados nos caminhos verificados. O SDK poderá ser preparado localmente para a validação; indisponibilidade persistente bloqueia apenas os gates que dependem de dispositivo e deve ser registrada, sem declarar sucesso fictício.
- Risco de dependência: downloads Gradle/Google Maven exigem rede. Falha transitória será distinguida de incompatibilidade real.
- Risco visual: a ausência deliberada dos binários legados reduz fidelidade pixel a pixel; o contrato aceita substituição neutra e exige preservar hierarquia, paleta, forma e comportamento.
- Risco de escopo: 212 variantes são rastreadas no catálogo tipado, mas a tela demonstra as famílias e estados relevantes, sem criar 212 componentes visuais distintos.
- Não há decisão de produto pendente. Mudança de SDK, canal público, incorporação de ativo legado, API experimental ou integração externa exige retorno à SPEC.

## Skills por etapa

- Fundação, processo, arquitetura, segurança, KDoc e quality gates: `android-native-engineering`.
- Componentes Compose: `compose-component-design`.
- Estado da tela, ViewModel e efeitos: `compose-state-and-effects`.
- Foco, teclado virtual, acessibilidade e navegação local: `compose-focus-navigation`.
- Transições ou gestos previstos pela SPEC: `compose-animations`.
- Testes Compose, semântica, tags e instrumentação: `compose-ui-testing-patterns`.
- ADRs, README e documentação técnica extensa: `grounded-writing`.

Cada sub-skill deve ser lida antes da etapa correspondente. Os quality gates
continuam sendo os da skill base `android-native-engineering`.
