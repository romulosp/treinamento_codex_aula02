# Validação — 10014

Status: `REPROVADA` integralmente por gates físicos/race/auditoria pendentes.
Retenção e empacotamento aprovados nos testes técnicos abaixo; o status não
significa falha nesses testes nem comprova resolução de GTK011.

Ambiente: Windows 10, Go 1.26.5 windows/386, emulator-5554/API35;
bridge histórico PID33664/COM7/19200/TCP39100 permanece ativo.

Última tentativa informada pelo usuário: correlação
3b46a9fb-027c-4959-ac42-0b595e5dbafd, total R$223,33, GCX000→GTK011→CLO000.
GOX/GPN não executados. Retenção não altera os parâmetros físicos rejeitados.

## Ambiente e execução

Validação após [IMPLEMENTACAO_APROVADA](reviews/2026-10-14-implementation-review.md).
Go 1.26.5 windows/386 para testes; geração gomobile usa Go amd64 portátil,
JDK17.0.11, NDK28.2.13676358, API29/quatro ABIs. Android usa wrapper9.6.0,
JBR25 existente, alvo Java17, SDK existente compile37/target36/min29.
Nenhuma dependência/plataforma atualizada.

| ID | Diretório/comando | Exit | Resultado |
| --- | --- | --- | --- |
| VAL-001 | Go: `go test ./...` | 0 | PASS todos os pacotes. |
| VAL-002 | Go: `go vet ./...` | 0 | PASS. |
| VAL-003 | Go: `go test -tags integration -coverprofile build/10014-coverage.out ./internal/domain/command ./internal/domain/parser ./internal/application/service ./mobile` | 0 | PASS; command90,0%, parser89,6%, service83,3%, mobile66,7% legado. |
| VAL-004 | Raiz: `powershell -NoProfile -ExecutionPolicy Bypass -File specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/build-mobile-aar.ps1` | 0 | AAR/sourcejar regenerados. |
| VAL-005 | Raiz: `python specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/validate-versionable-structure.py` | 0 | Projeção versionável13arquivos PASS; local.properties preexistente/ignorado excluído conforme10011. |
| VAL-006 | Android: `gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:lintDebug :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug --console=plain` | 0 | PASS6s;30JVM/0falhas e cobertura255/262=97,33% reutilizados UP-TO-DATE, Kotlin sem alterações; nativos/plugin/host repacotados. |
| VAL-007 | Android, ANDROID_SERIAL=emulator-5554: `gradlew.bat :plugin-saque-cartao:connectedDebugAndroidTest --console=plain` | 0 | PASS33s;16 testes instrumentados executados/0falhas. |
| VAL-008 | Raiz: `python specs/changes/10014-preservar-dados-autorizacao/evidence/verify-artifacts.py` | 0 | Fonte66hashes/AAR/API/ABI/merged/stripped/APK/asset do host conferidos. |
| VAL-009 | ADB5554: `install -r <Android>/app/build/outputs/apk/debug/app-debug.apk`; launcher `-ProvisionOnly -EmulatorSerial emulator-5554`; `am force-stop <host>`; `am start -n <host>/.MainActivity` | 0 | Host instalado,3fontesDES/hashesconfirmed, Activity relançada. Sem compra/OPN/chaves; Bridge33664 permanece listener39100. |

Os comandos Go usam apps/desktop/libpinpadabecsgo; Android usa
apps/frontend/smartphone/sistema-prototipo-android. Ver relatório
[artifact-verification-2026-10-14.json](evidence/artifact-verification-2026-10-14.json).

Cobertura agregada focal2092/2549=82,07%; arquivos de pagamento477/527=90,51%;
arquivo novo payment_authorization.go77/78=98,72%. Não declarar80% em todo
mobile legado. Suites novas verificam as quatro modalidades, GTK completo,
GOX offline/online/negado, GPN, cópias, clear, cancelamento/epoch tardio,
GTK011/042 e falha FCX. Integração opaca14cenários APIs novas/antigas sem perda GTK.

AAR SHA256 `a9c9fc6e7c6b5300bda61d8a0c2072967bf3ea5eb8a593c294fb8ba9ec200b47`;
pluginAPK `4a1bc0d0138384f82c2be72891c9a4a0ce7ecddb29b874afeeb7a355d5e6b8df`;
hostAPK `7dd460dd3a4512983aa59565362a76dfb5aac93b186e2a7befd75f2ea5d9f0a2`.
Sourcejar manteve hash/API anterior, pois não houve nova API gomobile.

## Avisos e limites

Uma primeira cópia/adaptação do script de evidência via `python -c` terminou
exit1 por aspas PowerShell. Corrigida usando leitura/substituição PowerShell;
script final executou exit0. Nenhum arquivo de produto afetado pelo erro.
Gradle instrumental reportou depreciação sun.misc.Unsafe/protobuf3.25.5 existente;
sem falha ou atualização de dependência. Git diff --check exit0, avisos LF/CRLF.

GIX do diagnóstico10013 é histórico, não nova consulta: DATA08 e PIN08 ausentes,
PIN02/07 presentes e nenhuma DATA presente. Manualp166 diferencia domíniosPIN/DAT;
GTK usa somenteDAT. Status011 significa ST_INVPARM, status010 ST_INVCALL (p30).
O manualp86 relaciona GTK011 a índice fora da faixa; a informação GIX08=0
isoladamente não comprova faixa não suportada (seria2). Solicitar confirmação
do responsável pela carga/modelo, mantendo seleção das fontes externas.

PENDENTE: homologação física que ultrapasse GTK011 e demonstre GOX/PIN real,
confirmação/carga autorizada de chaves pelo responsável, race em ambiente
CGO+compiladorC (gcc ausente comprovado10011), auditoria independente
HIGH_ASSURANCE. Não executar compras, alterar slots ou injetar chaves automaticamente.
Aprovação formal, system, commit e archive não realizados.
