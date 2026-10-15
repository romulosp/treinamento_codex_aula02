# Validação — 10011

## Estado

Atualizado em 2026-10-13. Implementação entregue e revisão de código/arquitetura
`IMPLEMENTACAO_APROVADA`: [IMP-REV](reviews/2026-10-13-implementation-review.md).
Gates técnicos disponíveis passaram; **validação integral REPROVADA por gates
materiais ainda pendentes** (homologação física/host, race e auditoria formal).
Não se afirma falha dos testes que passaram. Aprovação final da mudança:
**pendente**, distinta da revisão da SPEC e do código.

Aplicada a skill `implementation-validate`: “Pendências importantes tornam a
validação reprovada.” Registrar esse resultado impede considerar a Change
liberada/arquivável enquanto AC-02/09 e HIGH_ASSURANCE não estiverem completos.

## Ambiente

Windows 10/PowerShell, workspace `D:\desenvolvimento\ia\aula02`. Go 1.26.5
windows/386 para testes/Bridge; Go 1.26.5 windows/amd64 portátil para gomobile.
Python 3.14.6 (`py`), Node 24.16.0. Biblioteca em
`apps/desktop/libpinpadabecsgo`; projeto Android em
`apps/frontend/smartphone/sistema-prototipo-android`.

Gradle Wrapper 9.6.0 com JAVA_HOME
`D:\desenvolvimento\ferramentas_android\Nova pasta\AndroidStudio\jbr` (JBR 25),
alvo Kotlin/Java 17. SDK `D:\desenvolvimento\ferramentas_android\Sdk`,
NDK 28.2.13676358, minSdk 29/compileSdk 37/targetSdk 36.
AAR: JDK 17.0.11 em `C:\Desenvolvimento\jdk-17.0.11`, Go amd64 em
`%TEMP%\saque-cartao-go-1.26.5-amd64\go`, x/mobile fixado no go.mod
`v0.0.0-20260908204917-8b95e45f8d3e`, quatro ABIs, API 29.
Instrumentação: `emulator-5554`, AVD Medium_Tablet Android 15/API 35.

Houve alteração produtiva autorizada, build e testes sintéticos. Nenhuma compra
física, instalação de chave ou leitura da tabela PRD nesta etapa.

## Histórico documental e baseline — antes da implementação

| ID | Diretório/comando | Resultado | Exit |
| --- | --- | --- | --- |
| VAL-001 | Raiz: inventário seguro via `Get-Content` da fonte externa, mostrando só nomes, comprimento/presença e classificação de WK vazia. | Três campos; tipo 3, índice 08, WK logicamente vazia. Sem conteúdo sensível publicado. | 0 |
| VAL-002 | Download HTTPS com `Invoke-WebRequest` para `%TEMP%`; `py -c` com `pypdf.PdfReader` e extração local temporária. | Cópia primária de 283 páginas; seções usadas registradas no diagnóstico. | 0 |
| VAL-003 | Raiz: `py specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/verificar_documentos.py` | PASS: estrutura, UTF-8, links locais, status e AC-01..AC-09. | 0 |
| VAL-004 | Biblioteca Go: `go test ./internal/domain/command ./internal/domain/parser ./internal/application/service ./mobile` | PASS nos quatro pacotes, resultados do cache Go. Baseline sem alteração produtiva; não demonstra a nova sequência. | 0 |
| VAL-005 | Raiz: `git status --short` e `git diff --check` | Somente a nova pasta desta Change aparece como não rastreada. Diff sem erros nos arquivos rastreados; a nova pasta é verificada por VAL-003. | 0 |

Revisão técnica da SPEC: [REV-001](reviews/2026-10-11-spec-review.md), decisão `SPEC_APROVADA`. A aprovação final da implementação continua pendente.

Segurança: auditoria de implementação não se aplica nesta entrega documental; revisão de limites sensíveis incluída na SPEC/ADR. Não reutilizar relatório PDF histórico para aprovar esta correção.

## Plano de validação da implementação

| Grupo | Cenários obrigatórios | Aceite |
| --- | --- | --- |
| Payload e candidatos | Compra 00h em GCX/GOX, APPTYPE por snapshot, AID base/estendido com referências distintas, nenhuma omissão de filtros nas tentativas. | AC-01/02/04 |
| Roteamento | Tipos 00/03/05/06, tipo desconhecido, falha GCX/GTK, GOX sem GPN externo, GPN só no ramo sem EMV. Asserções de ordem e ausência de comandos indevidos. | AC-03/07 |
| Configuração | Três campos, índice sintético variável, WK vazia, BOM/CRLF/comentários; ausência, duplicatas, tipo incompatível, WK não vazia, novo campo, limite excedido; zero OPN/GPN/GOX em falha preflight. | AC-05 |
| Contexto Go | PAN sintético de GCX/trilha, mascarado/ausente/conflitante; correlações e clientes distintos; uso repetido; erro/timeout/cancelamento/desconexão/novo GCX/close invalidam. Verificar comando GPN interno e ausência de PAN na ABI/logs. | AC-06/08 |
| Encerramento | Sem comando pendente: fechamento sem CAN indevido; pendente: cancelar e confirmar; falha principal+cleanup distintos; close não confirmado mantém indeterminado e impede reuso. | AC-07/08/09 |
| Segurança de saída | Capturar JSON público, erro, logs e traces com marcadores sintéticos de dados sensíveis; assegurar ausência inclusive nos caminhos inválidos. Não usar segredo/cartão real nas fixtures. | AC-08 |

Executar `go test ./...` e `go vet ./...` após implementação. O Go instalado é windows/386; teste `-race` requer toolchain/GOARCH suportado e deve ter ambiente explícito, sem declarar race validado nesta configuração.

No Android, executar validador estrutural da skill base antes dos gates; Gradle Wrapper `:plugin-saque-cartao:testDebugUnitTest`, `:plugin-saque-cartao:lintDebug`, `:plugin-saque-cartao:assembleDebug` e `:plugin-saque-cartao:assembleDebugAndroidTest`. Descobrir tarefas existentes se necessário. Testes Compose/instrumentados via host, inventário KDoc, cobertura elegível ≥80% e proveniência AAR fazem parte de AC-09. Registrar comando, cwd, versões, exit e artefatos em cada execução.

## Roteiro físico obrigatório

1. Provisionar tabela DES, perfil e fonte criptográfica privada, validar integridade e confirmar chave/slot no pinpad sem publicar valores de chave.
2. Abrir host `:app`, autenticar, acessar Pagamento com Cartão; escolher explicitamente crédito ou débito na tela 1 e confirmar o total.
3. Repetir com cartões de teste: ICC, tarja, CTLS EMV e CTLS simulando tarja quando disponível; incluir aplicações AID base/estendido de ambas as modalidades. Marcar cenário não disponível como pendente, nunca aprovado por inferência.
4. Observar GCX/GTK/GOX ou GPN pela telemetria permitida. Confirmar entrada de PIN exclusivamente no pinpad e ausência de GOX em 05.
5. Exercitar fallback permitido, cancelamento real, chave ausente e desconexão controlada; não trocar índice/método diante de erro. Preservar falha principal.
6. Confirmar fechamento no pinpad e liberação de ownership; nova compra somente por ação explícita após encerramento confirmado. Registrar comando/status/mode/tentativa/correlação sanitizados.

Revisão de implementação concluída em 2026-10-13; validação física, aprovação
final, atualização de `specs/system/`, commit e archive permanecem pendentes.

## Diagnóstico incremental — 2026-10-12

Evidência: [chip/contactless EMV e interrupção anterior a GOX](evidence/diagnostico-chip-2026-10-12.md). Escopo documental; requisitos aprovados e código produtivo não foram alterados.

Ambiente: Windows/PowerShell, Go 1.26.5 windows/386, Python 3.14.6 e pypdf 6.18.0.

| ID | Diretório/comando | Resultado | Exit |
| --- | --- | --- | --- |
| VAL-006 | Raiz: extração read-only da cópia PDF local com `py -` / `pypdf.PdfReader`, páginas 84–86, 136–138 e 202. | GOX aplica-se a 03/06; GTK usa chave DATA e não deve invalidar o kernel EMV. | 0 |
| VAL-007 | Raiz: cálculo estrutural Python com TLV/framing e parâmetros sintéticos; comparação SHA-256 com proveniência AAR de quatro fontes relevantes. | GCX=80, GTK=30, GOX=63, CLO=42; quatro hashes coincidem. Identificação pelo tamanho é inferência; status ABECS e APK instalado não são comprovados. | 0 |
| VAL-008 | Biblioteca Go: `go test ./internal/domain/command ./internal/domain/parser ./internal/application/service ./mobile` | PASS nos quatro pacotes (cache). Baseline, sem implementação ou homologação física. | 0 |
| VAL-009 | Raiz: `py specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/verificar_documentos.py`; `git diff --check`; `git status --short` | PASS documental; diff sem erros; somente a pasta 10011 permanece não rastreada. | 0 |

## Implementação e revisão — 2026-10-12/13

Os registros VAL-001..009 acima são históricos. As afirmações de entrega somente
documental/ausência de código naquele período não descrevem o estado atual.
Não reutilizar o baseline como prova da implementação.

Nos comandos a seguir, `C` = `specs/changes/10011-corrigir-leitura-cartao-pinpad`,
`G` = diretório da biblioteca Go e `A` = raiz Android definidos no ambiente.
O prefixo em cada linha identifica o cwd; caminhos relativos usam esse cwd.

| ID | Cwd / comando | Resultado / artefato | Exit |
| --- | --- | --- | --- |
| VAL-010 | G: `go test ./...` | PASS em todos os pacotes após as últimas alterações de contexto/close; inclui regressões dos consumidores genéricos. | 0 |
| VAL-011 | G: `go vet ./...` | PASS; sem diagnóstico. | 0 |
| VAL-012 | G: `go test -tags integration -coverprofile build/10011-coverage-integration.out ./internal/domain/command ./internal/domain/parser ./internal/application/service ./mobile` | PASS: command 90,0%, parser 89,6%, service 82,0%, mobile legado 62,3%. Agregado 81,04%; novos arquivos 88,53%. Bridge opaco + serial sintética exercita seis cenários, GTK 042 interrompendo e GOX 0/1/2. | 0 |
| VAL-013 | G, Go amd64 portátil, `CGO_ENABLED=1`: `go test -race ./internal/application/service ./mobile` | Não executável: `gcc` ausente no PATH; sem compiler MinGW nos caminhos comuns. Race não aprovado. | 1 |
| VAL-014 | Raiz: `powershell.exe -NoProfile -ExecutionPolicy Bypass -File C/evidence/test-payment-pin-config.ps1` | PASS: 13 casos sintéticos, incluindo BOM, método/índice variável, WK, duplicata, desconhecido, UTF-8 inválido e limite; não acessa fonte real. | 0 |
| VAL-015 | Raiz: `[System.Management.Automation.Language.Parser]::ParseFile(<A>/scripts/iniciar-saque-cartao.ps1,[ref]$tokens,[ref]$errors)`; exigir `$errors.Count -eq 0` | PASS de sintaxe do launcher. Não executa Bridge/hardware. | 0 |
| VAL-016 | Raiz: `powershell.exe -NoProfile -ExecutionPolicy Bypass -File C/evidence/build-mobile-aar.ps1` | AAR/source jar/proveniência regenerados após últimas mudanças Go. Hash final e fontes conferidos por VAL-021. | 0 |
| VAL-017 | G: `go build -o build/10011/libpinpadabecsgo-bridge.exe ./cmd/libpinpadabecsgo-bridge` | PASS: Bridge windows/386 compilado; processo não iniciado. | 0 |
| VAL-018 | A, JAVA_HOME/SDK definidos acima: `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:lintDebug :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug --console=plain` | BUILD SUCCESSFUL; 30 JVM (24 Journey/5 Models/1 Plugin), zero falhas; lint e cobertura aprovados, APKs finais construídos. | 0 |
| VAL-019 | A, mesmo ambiente, `ANDROID_SERIAL=emulator-5554`: `./gradlew.bat :plugin-saque-cartao:connectedDebugAndroidTest --console=plain` | BUILD SUCCESSFUL em 44s: 15 instrumentados, zero falhas, APK final. Usa target standalone de teste, fontes sintéticas; não abre fluxo financeiro nem comprova carga pelo host/login/menu. | 0 |
| VAL-020 | G: `go run D:/desenvolvimento/ia/aula02/specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/validate-external-des.go D:/desenvolvimento/chave_des/perfil_saque_cartao_des.json D:/desenvolvimento/chave_des/emv_des.json D:/desenvolvimento/chave_des/informacoes_critografia_pinpad.properties` | PASS: PIN integral, 36 registros EMV, candidatos crédito=8/débito=7. Métodos estáticos do core, zero cliente/socket/comando físico. | 0 |
| VAL-021 | Raiz: `py specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/verify-artifacts.py` | PASS: 64 hashes de fonte, AAR, quatro ABIs, min API 29 no manifesto, sete APIs; nativos merged e stripped/empacotados conferem; APK plugin idêntico ao asset do host. [JSON da conferência](evidence/artifact-verification-2026-10-13.json). | 0 |
| VAL-022 | Raiz: `py .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | Falha por `local.properties` local preexistente, ignorado pelo Git; nenhum arquivo removido para silenciar o gate. | 1 |
| VAL-023 | Raiz: `py specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/validate-versionable-structure.py` | PASS de invariantes de Wrapper/settings/build/manifestos sem arquivos locais/builds. Limitação VAL-022 permanece registrada; não representa aprovação do arquivo local ignorado. | 0 |
| VAL-024 | Raiz: `py specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/verificar_documentos.py`; `git diff --check` | PASS de UTF-8, links, status/AC e whitespace após atualização dos documentos. | 0 |

Relatórios Android locais: `plugin-saque-cartao/build/test-results/testDebugUnitTest/`,
`build/reports/lint-results-debug.xml`,
`build/reports/jacoco/saqueCoverageReport/saqueCoverageReport.xml`,
`build/outputs/androidTest-results/connected/debug/` (relativos ao módulo).
Inventário produtivo/KDoc e exclusões: [arquitetura implementada](evidence/arquitetura-implementada.md).

Cobertura Android elegível: 253/260 linhas = **97,31%**. Go agregado dos quatro
pacotes focais: 1.940/2.394 statements = **81,04%**. Cinco arquivos novos:
332/375 = **88,53%**. A fachada mobile antiga inteira não alcança 80%; não se
oculta esse resultado nem se altera a regra para aprovar pacote legado inteiro.

Hash AAR final:
`5cb7f34e6fa2d1bc015015ee808f79049733fc6dc5e8d3340d1f292ebf79d666`.
Demais hashes de APKs/source jar/Bridge no JSON. Proveniência identifica HEAD
`26f17c4de084a773a41d1c87b257125265664379` e `dirty=true`, mais 64 hashes do
fonte efetivo; não equivale a commit das alterações.

Falhas/avisos relevantes durante implementação/verificação:

- Testes iniciais detectaram identificação incorreta das tags de trilhas GCX;
  parser foi corrigido para 8041/8042/8043, com regressão exercitada.
- Testes sintéticos identificaram tratamento cultural de BOM no PowerShell;
  comparação passou a Ordinal e teste de BOM duplo foi incluído.
- Fixture agrupando quatro registros de 284 bytes excedia o bloco TLR; builders
  passaram a ser exercitados em blocos válidos, preservando todos os registros.
- Comando inicial de build Bridge com concatenação de ldflags falhou; comando
  simples de VAL-017 passou. Nenhum flag financeiro/segredo é necessário.
- Primeira conferência Python usou caminho incompleto do asset host e falhou;
  corrigido para `assets/plugins/business/plugin-saque-cartao.apk`. Os .so do
  APK são stripped pelo AGP; comparações corretas são AAR→merged e stripped→APK,
  ambas passaram. Não se espera igualdade binária direta AAR→APK após stripping.
- Gradle instrumentado em JBR 25 emitiu aviso de depreciação `sun.misc.Unsafe`
  pela biblioteca protobuf das ferramentas. Build/testes concluíram com exit 0;
  nenhuma dependência foi atualizada para silenciar o aviso.

## Aceite técnico e pendências materiais

AC-01/03/04/05/06/07/08 têm evidência automatizada da implementação. AC-02
tem fixtures e validação estática de candidatos, mas exige seleção física.
AC-09 tem build/proveniência/empacotamento; host/login/menu, ownership e próxima
operação real ainda exigem o roteiro físico acima. Não declarar esses ACs
integralmente aprovados por inferência.

Auditoria formal independente HIGH_ASSURANCE e race continuam pendentes.
Revisão local dos limites sensíveis está no IMP-REV, sem PAN/trilhas/WK/PIN/KSN
nos DTOs/estado/relatórios, e testes usam somente marcadores sintéticos.
Tabelas/chaves reais não foram incorporadas ao Git/APK.

Conclusão da etapa: **REPROVADA por validação incompleta**, sem aprovação final,
commit, atualização de system ou archive. Retomar pelos gates abertos na
[memória dos agentes](AGENTS-MEMORY.md), preservando o fonte/artefatos já revisados.
