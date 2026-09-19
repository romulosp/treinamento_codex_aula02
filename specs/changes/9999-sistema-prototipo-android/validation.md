# Validação: 9999-sistema-prototipo-android

**Autor:** Rômulo Penha

## Status

`PENDENTE_IMPLEMENTACAO`

## Ambiente

- Sistema operacional: Windows 10.
- Shell: PowerShell.
- Workspace: raiz do repositório atual.
- Data da validação documental inicial: 2026-09-19.

## Comandos e códigos de saída

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
- Resultado: 315 arquivos fornecidos conferidos, 0 ausência e 0 divergência de hash; a pasta possui 316 arquivos ao incluir seu README local.
- Código de saída: `0`.

### VAL-DOC-004 — Inventário lógico

- Diretório: raiz do repositório.
- Comando: leitura PowerShell dos XMLs e contagem do material local.
- Resultado: 212 definições de tema, 337 estados, 350 nós de menu, 154 fontes Java, 144 imagens e 8 fontes TTF.
- Código de saída: `0`.

### VAL-DOC-005 — Consistência Git

- Diretório: raiz do repositório.
- Comando: `git diff --check` e `git status --short -- specs/changes/9999-sistema-prototipo-android`.
- Resultado: nenhuma inconsistência reportada; a nova Change aparece como não rastreada, conforme esperado antes do commit.
- Código de saída: `0`.

Build, testes Android, lint, cobertura e emulador permanecem não aplicáveis até existir implementação aprovada.

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

- Ferramenta e versão: `PENDENTE`.
- Escopo de classes aplicáveis: lógica de catálogo, edição de texto, seleção, menu e estado de tela.
- Classes excluídas e justificativas: `PENDENTE`.
- Cobertura de linhas: `PENDENTE`.
- Cobertura de branches: `PENDENTE`.
- Comando executado: `PENDENTE`.
- Resultado: `PENDENTE`.
- Código de saída: `PENDENTE`.

## Cenários executados

- Validação documental inicial: `APROVADA` para criação e submissão à revisão de SPEC.
- Cenários Android: não executados; não há código autorizado nem projeto criado nesta Change.

## Evidências

- Inventário estático registrado em `inventario-origem.md`.
- Decisões temporais e candidatas oficiais registradas em `sources-and-decisions.md`.
- Material necessário à migração incorporado e verificado em `source-material/`.
- Evidências de implementação: `PENDENTE`.

## Veredito

`PENDENTE`
