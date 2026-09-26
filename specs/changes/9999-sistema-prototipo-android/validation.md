# Validação: 9999-sistema-prototipo-android

**Autor:** Rômulo Penha

## Status

`PENDENTE_IMPLEMENTACAO`

## Resultado da Compatibility Review do prompt2

- Resultado: `PASS` documental.
- Stack: AGP 9.4.0, Gradle 9.6.0, KGP 2.2.10, JDK 17, Compose BOM `2026.09.00`, Compose 1.12.x, `compileSdk = 37`, `targetSdk = 36` e `minSdk = 26`.
- Evidência principal: AGP 9.4.0 suporta API 37 e publica a combinação mínima/default acima.
- Limitação: build, resolução real das dependências e testes permanecem pendentes até a implementação.

### VAL-IMP-001 — Build inicial bloqueado

- Diretório: `apps/frontend/smartphone/sistema-prototipo-android/`.
- Comando: `gradlew.bat assembleDebug --no-daemon --console=plain`.
- Resultado: `BUILD FAILED`; o Android SDK não foi localizado por `ANDROID_HOME`, `ANDROID_SDK_ROOT` ou `local.properties`.
- Código de saída: `1`.
- Retorno: fase de implementação após disponibilizar o SDK Android.

### VAL-IMP-002 — Validador estrutural

- Comando: `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android`.
- Resultado: invariantes estruturais Android atendidos.
- Código de saída: `0`.

### VAL-IMP-003 — Build após correção do crash de estado

- Diretório: `apps/frontend/smartphone/sistema-prototipo-android/`.
- Comando: `gradlew.bat assembleDebug --no-daemon --console=plain`, com Java 17 e Android SDK configurados.
- Resultado: `BUILD SUCCESSFUL`; o estado editável foi separado em primitivas salvas por `rememberSaveable`, evitando a exceção do `SaveableStateRegistry`.
- Código de saída: `0`.

### VAL-IMP-004 — Smoke test no emulador

- Comandos: instalação do `app-debug.apk` via `adb install -r` e abertura de `MainActivity` via `adb shell am start`.
- Resultado: instalação `Success`, processo iniciado e nenhum `FATAL EXCEPTION`, `AndroidRuntime` ou encerramento forçado no logcat recente.
- Código de saída: `0`.

### VAL-IMP-005 — Testes JVM

- Comando: `gradlew.bat testDebugUnitTest --no-daemon --console=plain`.
- Resultado: `BUILD SUCCESSFUL`; a tarefa foi executada como `NO-SOURCE`, pois os testes unitários ainda não foram criados.
- Código de saída: `0`.

### VAL-IMP-006 — Lint Android

- Comando: `gradlew.bat lintDebug --no-daemon --console=plain`.
- Resultado: `BUILD SUCCESSFUL`; relatórios HTML e SARIF gerados em `app/build/reports/`.
- Código de saída: `0`.

### VAL-IMP-007 — Primeira vertical slice visual da tela-catálogo

- Ambiente: emulador `Medium_Tablet`, Android API 35, orientação paisagem.
- Resultado: o primeiro enquadramento contém cabeçalho, identificação, usuário e senha, teclado numérico à direita, teclado QWERTY inferior, ações cancelar/confirmar nos extremos e barra de mensagem.
- Evidência local gerada: `app/build/catalog-screen.png` (artefato de build não versionado).
- Limitação: as famílias adicionais abaixo da primeira dobra e o teclado de calculadora continuam pendentes.

### VAL-IMP-008 — Testes após reconstrução da tela

- Comando: `gradlew.bat assembleDebug testDebugUnitTest lintDebug --no-daemon --console=plain`.
- Resultado: `BUILD SUCCESSFUL`; três testes JVM executados e lint aprovado.
- Código de saída: `0`.

### VAL-IMP-009 — Testes Compose no emulador

- Comando: `gradlew.bat connectedDebugAndroidTest --no-daemon --console=plain`.
- Resultado: `BUILD SUCCESSFUL` no `Medium_Tablet(AVD) - 15`; hierarquia visual e emissão de evento por tecla aprovadas.
- Código de saída: `0`.

### VAL-IMP-010 — Geração, compilação, instalação e execução manual

- Diretório: raiz do repositório.
- Comando: `& '.\apps\frontend\smartphone\sistema-prototipo-android\scripts\gerar-compilar-executar.ps1'`.
- Etapas: validador estrutural, `clean assembleDebug`, instalação do APK por `adb` e abertura da `MainActivity`.
- Resultado: estrutura aprovada, `BUILD SUCCESSFUL`, instalação `Success` e Activity iniciada no emulador conectado.
- Código de saída: `0`.
- Evidência visual local: `app/build/component-gallery.png`, mostrando seleção, tabela, mensagens, diálogos e progresso na tela rolável.
- Aceite visual definitivo: manual pelo usuário no Android Studio.

### VAL-IMP-011 — Quality gates após geração dos componentes

- Comando: `gradlew.bat testDebugUnitTest lintDebug --no-daemon --console=plain`.
- Resultado: `BUILD SUCCESSFUL`; testes JVM e lint aprovados.
- Código de saída: `0`.

### VAL-IMP-012 — Ajuste manual de foco e proporção dos teclados

- Mudança: removidos os botões intermediários de seleção de campo; usuário e senha passam a selecionar automaticamente o alvo do teclado ao receber foco.
- Mudança visual: teclas numéricas e alfabéticas ampliadas para 58 dp e fileiras QWERTY distribuídas pela largura útil.
- Comando: `& '.\apps\frontend\smartphone\sistema-prototipo-android\scripts\gerar-compilar-executar.ps1'`.
- Resultado: validador estrutural aprovado, `BUILD SUCCESSFUL`, instalação `Success` e Activity iniciada no emulador.
- Quality gates adicionais: `gradlew.bat testDebugUnitTest lintDebug --no-daemon --console=plain`, código de saída `0`.
- Evidência visual local: `app/build/keyboard-layout.png`.
- Aceite visual definitivo: manual pelo usuário no Android Studio.

### VAL-IMP-013 — Reprodução da nova interface de referência

- Mudança: substituição da tela-catálogo visível pela interface operacional anexada em 2026-09-20.
- Composição: cabeçalho institucional desenhado em Compose, identificação central, teclado alfanumérico retangular, teclado numérico lateral, ações operacionais azul-escuras, sair/cancelar vermelho e indicadores de conectividade verdes.
- Restrição atendida: nenhuma ação usa verde-escuro; verde permanece somente em Gateway, DNS e HTTP.
- Comando: `gradlew.bat assembleDebug testDebugUnitTest connectedDebugAndroidTest lintDebug --no-daemon --console=plain`.
- Resultado: `BUILD SUCCESSFUL`, testes JVM, testes Compose no `Medium_Tablet(AVD) - 15` e lint aprovados.
- Código de saída: `0`.
- Evidência visual local: `app/build/final-interface.png`.
- Aceite visual definitivo: manual pelo usuário no Android Studio.

## Ambiente

- Sistema operacional: Windows 10.
- Shell: PowerShell.
- Workspace: raiz do repositório atual.
- Data da validação documental inicial: 2026-09-19.

## Comandos e códigos de saída

### VAL-DOC-006 — Consistência do baseline prompt2

- Diretório: raiz do repositório.
- Comando: busca por status, versões substituídas e links Markdown locais nos artefatos vigentes da Change.
- Resultado: statuses coerentes; baseline único AGP 9.4.0/Gradle 9.6.0/KGP 2.2.10/JDK 17/Compose BOM 2026.09.00; 0 link Markdown local quebrado.
- Código de saída: `0`.
- Limitação: não comprova resolução de dependências nem build Android.

### VAL-DOC-001 — Estrutura e links locais

- Diretório: raiz do repositório.
- Comando: script PowerShell que verifica os sete itens obrigatórios da Change e resolve todos os links Markdown locais.
- Resultado: 7 itens presentes, 23 arquivos Markdown, 0 link local quebrado.
- Código de saída: `0`.

### VAL-DOC-002 — Nomenclatura, caminhos e whitespace

- Diretório: raiz do repositório.
- Comando: script PowerShell que inspeciona os 23 documentos Markdown autorais.
- Resultado: 0 ocorrência dos termos proibidos, 0 caminho local absoluto e 0 linha com whitespace final.
- Código de saída: `0`.

### VAL-DOC-003 — Integridade do material incorporado

- Diretório: raiz do repositório.
- Comando: comparação SHA-256 de cada arquivo incorporado com o conjunto fornecido.
- Resultado histórico: declarado anteriormente como aprovado, mas invalidado pela inspeção física atual; os 315 arquivos não estão presentes em `source-material/`.
- Estado atual: `REPROVADO`; requer nova incorporação e nova conferência de hashes.

### VAL-DOC-004 — Inventário lógico

- Diretório: raiz do repositório.
- Comando: leitura PowerShell dos XMLs e contagem do material local.
- Resultado histórico: contagens derivadas durante a descoberta, sem os arquivos correspondentes atualmente presentes na Change.
- Estado atual: inventário documental preservado; integridade física pendente.
- Código de saída: `0`.

### VAL-DOC-005 — Consistência Git

- Diretório: raiz do repositório.
- Comando: `git diff --check` e `git status --short -- specs/changes/9999-sistema-prototipo-android`.
- Resultado: nenhuma inconsistência reportada; a nova Change aparece como não rastreada, conforme esperado antes do commit.
- Código de saída: `0`.

Build, testes JVM, lint e execução no emulador são aplicáveis e foram executados. Cobertura, matriz completa de dispositivos e aceite visual humano permanecem pendentes.

## Quality gates previstos

```text
python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android
apps/frontend/smartphone/sistema-prototipo-android/gradlew.bat assembleDebug
apps/frontend/smartphone/sistema-prototipo-android/gradlew.bat testDebugUnitTest
apps/frontend/smartphone/sistema-prototipo-android/gradlew.bat lintDebug
apps/frontend/smartphone/sistema-prototipo-android/gradlew.bat connectedDebugAndroidTest
```

Os comandos finais deverão ser executados a partir do diretório do projeto com o Gradle Wrapper e atualizados caso os nomes reais das tarefas de screenshot ou cobertura sejam diferentes.

## Testes unitários e cobertura

- Ferramenta: JUnit 4.13.2 pelo Gradle Wrapper 9.6.0.
- Escopo de classes aplicáveis: lógica de catálogo, edição de texto, seleção, menu e estado de tela.
- Classes excluídas e justificativas: `PENDENTE`.
- Cobertura de linhas: `PENDENTE`.
- Cobertura de branches: `PENDENTE`.
- Comando executado: `gradlew.bat testDebugUnitTest --no-daemon --console=plain`.
- Resultado: `APROVADO` para os testes implementados; cobertura ainda não medida.
- Código de saída: `0`.

## Cenários executados

- Validação documental inicial: `APROVADA` para criação e submissão à revisão de SPEC.
- Cenários Android: aplicativo compilado, instalado e aberto no emulador; tela principal e galeria rolável inspecionadas localmente. Aceite manual final permanece com o usuário.

## Evidências

- Inventário estático registrado em `inventario-origem.md`.
- Decisões temporais e candidatas oficiais registradas em `sources-and-decisions.md`.
- Entradas textuais incorporadas; código, configurações, imagens, fontes e referência visual permanecem pendentes em `source-material/`.
- Evidências de implementação: build, lint, validador estrutural, smoke test e geração completa registrados em `VAL-IMP-003` a `VAL-IMP-011`.

## Veredito

`PENDENTE`
