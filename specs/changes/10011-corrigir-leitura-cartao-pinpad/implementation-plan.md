# Plano de implementação — 10011

Início: 2026-10-12, após confirmação de `SPEC_APROVADA` em proposal/spec e solicitação explícita de implementação. Manter o contrato aprovado, sem upgrade de stack ou alteração de layout.

## Fatias e contratos

1. Parser Go de propriedades PIN: validar os três campos integralmente; método DUKPT TDES, índice N2 e WK vazia. Fachada `ValidatePaymentPINProperties` para preflight e `PaymentPINConfigurationSummaryJSON` para snapshot permitido do perfil debug.
2. Contexto privado no serviço Go, vinculado à instância/sessão/operationID. Fachada aditiva: `CapturePaymentCardSummaryJSON`, `GetPaymentTracksSummaryJSON`, `ContinuePaymentEMVSummaryJSON` e `CapturePaymentPIN`. Nenhuma assinatura recebe PAN ou devolve PIN block/KSN. APIs genéricas mantêm seus contratos.
3. Validar candidatos de modalidade usando registros integrais e identidade AARR; não selecionar AID no Android. Preservar fallback do core, com mensagem para ação externa e telemetria sanitizada.
4. Launcher importa `CriptografiaPath`, valida antes de abrir hardware e provisiona `criptografia.properties` no diretório privado debug com integridade. Falha invalida snapshot anterior. Android lê e valida antes de Open, congela configuração e usa tipo de modo para GCX → GTK → GOX/GPN, com compra `00h`.
5. Core registra comando/status/tentativa/correlação em diagnóstico sanitizado. Cleanup fecha após comando concluído e só cancela operação pendente; resultado principal e cleanup permanecem separados. Falha não confirmada impede reutilização da sessão.
6. Recompilar AAR/source jar/proveniência e Bridge rastreáveis; executar testes Go e gates Android disponíveis. Homologação física e aprovação formal não são inferidas dos testes.

## Inventário e testes

- Go: `internal/domain/command`, `internal/domain/parser`, `internal/application/service` e `mobile`; propriedades, payload compra/filtros, candidatos base/estendido, modos 00/03/05/06, fallback, PAN privado, uso único, isolamento, cancelamento/close/erro e sanitização. `go test ./...`, `go vet ./...`, cobertura; race depende de toolchain suportada.
- Android: `SaqueRepository.kt`, `SaqueJourney.kt`, modelos/configuração e testes existentes; snapshot, preflight, ordem por modo, decisões GOX/FCX, erro principal e cleanup. Validador estrutural antes de Gradle Wrapper, unitários, lint, assembleDebug e assembleDebugAndroidTest. Cobertura elegível e KDoc registrados.
- Launcher: testes PowerShell com fonte criptográfica sintética, incluindo BOM/separadores/comentários, duplicatas, desconhecidos, inválidos e tamanho máximo; nenhuma chave real copiada aos testes.
- Artefatos: usar scripts existentes de AAR/proveniência, confirmar APIs aditivas e vínculo fonte/artefato. Não alterar transporte PBRG.

## Skills e segurança

Aplicadas: spec-implement; android-native-engineering e referências de workflow/quality gates; compose-state-and-effects; compose-ui-testing-patterns; skills Go de estilo, modernização, segurança de tipos/recursos, contexto, erros e testes. Não se altera composição/layout, portanto sub-skill de component design não é acionada nesta fatia. Catálogo Android externo já classificado REJECT no diagnóstico da SPEC; nenhuma incorporação externa.

PAN permanece em buffer mutável privado, descartado no encerramento/falha/cancelamento/troca de captura. Strings transitórias dos builders não têm apagamento garantido pelo runtime. Dados reais não serão usados em fixtures, logs ou relatórios. DATA GTK e PIN GOX/GPN continuam independentes; chave ausente é diagnosticada e não contornada.

## Riscos e término

O log redigido sugere GTK antes de GOX, mas não identifica o status físico. A implementação deve tornar esse status observável com metadados permitidos. Não instalar chaves nem executar compra automaticamente. A etapa termina como IMPLEMENTADA somente após código e artefatos previstos prontos, com resultados/limitações registrados; revisão formal, homologação e aprovação final continuam posteriores.
