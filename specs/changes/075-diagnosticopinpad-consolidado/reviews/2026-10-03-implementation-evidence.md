# Registro de implementação — 075

Data: 2026-10-03. Estado: `IMPLEMENTADA`, para teste humano.

Este registro contém evidência do implementador; não é revisão independente,
aprovação ou validação formal.

## Alterações

- `DiagnosticViewModel`: ação, Bridge e sessão independentes; último ID final
  separado; QR exige sessão; Close correlacionado; saída falha de forma visível
  se o fechamento não concluir.
- `DiagnosticRepository`/`DiagnosticFailure`: produção usa os helpers tipados
  `Mobile.errorCode/errorPhase`, com fallback seguro e mensagens locais.
- `DiagnosticScreen`: painel superior com rolagem, foco e live region.
- `AppLogger`: armazenamento interno privado e allowlist de estados/ação.
- Go mobile: UTF-8/128 bytes antes da rede, fase Ping, erro remoto normalizado,
  correlação Close; protocolo marca corpo truncado como `UnexpectedEOF`.
- Script AAR: verifica host amd64 e registra metadata do binário gomobile e
  ambiente; README descreve os caminhos Java/Maven e a baseline 074/075.
- Testes JVM/Compose/Go: regressões de estados, QR, falha Ping, foco, ID inválido,
  sanitização remota; fluxo scripted real e versão JNI com proveniência.

## Resultados

Go integração/vet/build: saída 0. Cobertura Go agregada com `-coverpkg=./...`:
80,1%; cobertura local por pacote agregada sem esse flag: 78,2%.
Android: 20 testes JVM, seis instrumentados, zero falhas/erros/skips; lint e APK
concluídos. AAR regenerado e inspecionado, quatro ABIs e minSdk 26.
Comandos, ambiente, hash e tentativas malsucedidas estão em `../validation.md`.

## Pendências explícitas

Hardware físico, race detector, cobertura elegível Kotlin e testes humanos de
rotação/Cancel/saída durante operação. A aprovação formal e a validação
independente não foram executadas.
