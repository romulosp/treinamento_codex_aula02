# Tasks — 068 diagnosticopinpad

## Especificação e revisão

- [x] Distinguir os anexos de entrada das instruções e contratos do projeto.
- [x] Reconciliar o rascunho v7 com o `PBRG` aprovado na Change 067.
- [x] Confirmar baseline da fachada `mobile` e lacuna de correlação `DATA`.
- [x] Definir escopo mínimo e adiar o laboratório completo.
- [x] Validar a matriz de toolchain em fontes oficiais.
- [x] Avaliar Skills Android oficiais sem instalação automática.
- [x] Definir critérios de aceite e gates P0–6.
- [x] Registrar revisão formal em `reviews/2026-09-27-spec-review.md`.

## Pré-condição 067

- [ ] Concluir a revisão de implementação da Change 067.
- [ ] Resolver achados bloqueantes/importantes que afetem a Change 068.
- [ ] Executar o Gate 1 `gomobile bind` da Change 067.
- [x] Registrar AAR importável e chamada Kotlin → Go.

## Implementação Go/Bridge

- [x] Fixar ferramenta `golang.org/x/mobile` e script de geração do AAR.
- [x] Adicionar `Version()` com proveniência do build.
- [x] Adicionar `Client.Ping(operationID)`.
- [x] Adicionar `Client.GetInfoJSON(operationID)` com schema versionado.
- [x] Propagar `operationID` pelos frames `DATA` sem alterar `PBRG` v1.
- [x] Implementar `ScriptedSerialTransport` de desenvolvimento.
- [x] Garantir que o Bridge físico consuma `PORTA_PINPAD` e reutilize o valor no
  transporte serial e no ownership.
- [x] Cobrir cancelamento, panic boundary, correlação, schema e transcript.
- [x] Adicionar GoDoc em todos os símbolos públicos novos/modificados.

## Implementação Android

- [x] Criar projeto single-module e Gradle Wrapper nas versões aprovadas.
- [x] Configurar AAR local não versionado e falha objetiva quando ausente.
- [x] Implementar adapter/repository do binding e logging sanitizado.
- [x] Implementar `DiagnosticUiState` e `DiagnosticViewModel` com UDF.
- [x] Implementar tela Compose acessível e estados de loading/erro/cancelamento.
- [x] Implementar configuração de host, porta e timeout.
- [x] Adicionar KDoc aos contratos Kotlin.
- [x] Adicionar testes JVM de validação de endpoint e estado do ViewModel.
- [x] Adicionar README com preparação, build, execução e troubleshooting.

## Testes e validação

- [x] Executar validador estrutural da Skill Android.
- [x] Executar testes Go, vet, build e cobertura aplicável.
- [x] Executar `testDebugUnitTest`, `lintDebug` e `assembleDebug`.
- [ ] Executar testes instrumentados/Compose no Emulator API 37.
- [x] Executar Gate 3 com `adb reverse` e `Ping`.
- [ ] Executar Gate 4 com transporte roteirizado.
- [ ] Executar Gate 5 com COM e pinpad real.
- [ ] Executar Gate 6 de erros, lifecycle e redaction.
- [x] Registrar ambiente, comando, código de saída e evidências.
- [ ] Solicitar revisão de implementação, validação e aprovação formal.

## Change posterior

- [ ] Criar Change 069+ para o catálogo completo de comandos do Functional Lab.
