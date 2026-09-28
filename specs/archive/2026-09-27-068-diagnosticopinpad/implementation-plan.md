# Plano de implementação — 068 diagnosticopinpad

Este plano foi criado após `SPEC_APROVADA`, conforme o workflow. Ele não
autoriza ignorar a pré-condição da Change 067 nem amplia o escopo da SPEC.

## Sequência

1. Revisar formalmente a implementação da 067 e executar seu Gate 1.
2. Fixar o build do AAR e provar `Version()` em um consumidor Kotlin mínimo.
3. Estender a fachada com `Ping`, `GetInfoJSON` e correlação `DATA`, mantendo
   compatibilidade com `PBRG` v1.
4. Implementar e testar o transporte serial roteirizado no host.
5. Confirmar que o fluxo físico usa `PORTA_PINPAD` no processo Windows do
   Bridge, sem transportar configuração de COM para o Android.
6. Criar o projeto Android e validar sua estrutura antes da UI funcional.
7. Implementar repository, ViewModel/UDF e tela Compose.
8. Executar Gates 2–4 e 6 no Emulator.
9. Executar Gate 5 com COM e pinpad real.
10. Registrar revisão da implementação, validação e aprovação.

## Estratégia incremental

- primeiro incremento: projeto vazio, configuração e testes puros de estado;
- segundo incremento: geração do AAR + `Version()` assim que o Gate 1 estiver
  disponível;
- terceiro incremento: `Ping`, lifecycle e cancelamento;
- quarto build: `Open/GetInfo/Close` com transcript;
- último gate: substituir apenas o adapter roteirizado pela serial real.

Cada incremento deve manter testes verdes. Uma falha estrutural no binding ou
no contrato de transporte interrompe a UI funcional e retorna para revisão.

## Qualidade

- Go: `gofmt`, `go test ./...`, cobertura, `go vet ./...`, `go build ./...` e
  race quando suportado pelo host;
- Android: Gradle Wrapper, unit tests, lint, assemble e connected tests;
- documentação: GoDoc/KDoc em português do Brasil;
- segurança: dependências fixadas, loopback, limites, permissões mínimas,
  redaction e nenhum payload raw em logs;
- integração: evidência de `operationID` correlacionada entre Android, Go e
  Bridge.

## Artefatos

APK, AAR, logs completos e capturas de hardware serão artefatos de build e não
fontes versionadas. `validation.md` registrará caminhos, checksums, ambiente,
comandos, códigos de saída e resultados sanitizados.
