# Validação — 10013

Iniciada em 2026-10-14. GIX/GTK físicos foram lidos sem desativar redaction;
nenhuma captura financeira é repetida automaticamente. Hardware para PIN online
permanece pendente: configuração em arquivo não comprova chave carregada.

## Estado e ambiente

Revisão local: [IMPLEMENTACAO_APROVADA](reviews/2026-10-14-implementation-review.md).
Gates disponíveis PASS; validação integral `REPROVADA` por AC-06 físico pendente,
race sem compiladorC e auditoria formal independente HIGH_ASSURANCE pendente.
Conforme implementation-validate, pendências importantes impedem VALIDADA.

Windows10/PowerShell5.1, workspace D:\desenvolvimento\ia\aula02. Go1.26.5
windows/386 para teste; Go1.26.5 amd64 portátil/JDK17.0.11/NDK28.2.13676358
para AAR API29/quatroABIs. GradleWrapper9.6.0 em JBR25, targetJava17,
SDK37/target36/min29. SDK D:\desenvolvimento\ferramentas_android\Sdk,
emulator5554 Android15/API35. Ambiente integral anterior descrito na10011.

| ID | Cwd/comando | Resultado | Exit |
| --- | --- | --- | --- |
| VAL-001 | Raiz: coleta de Logcat, seleção exclusiva de JSON msg=comando ABECS concluido e campos command/status/phase/attempt/correlation | GTK011 confirmado, GCX000 e CLO000; não há GOX/GPN nessa tentativa manual. | 0 |
| VAL-002 | Biblioteca Go: diagnóstico temporário `go run ./build/10012-diagnostic` (OPN/GIX/CLO apenas; fonte depois movida para evidence/query-key-metadata.go.txt, fora dos pacotes produtivos) | GIX000; mapas8035/36 slot08 ausente; PIN02/07 carregados, nenhumaDATAcarregada; close confirmado. Sem cartão/PIN/KSN/key values. [Diagnóstico](evidence/diagnostico-gtk-2026-10-14.md). | 0 |
| VAL-003 | Go: `go test ./...` | PASS após retorno proprietário; diagnóstico temporário compilado mas main não executado pelo test. | 0 |
| VAL-004 | Go: `go vet ./...` | PASS. | 0 |
| VAL-005 | Go: `go test -tags integration -coverprofile build/10013-coverage.out ./internal/domain/command ./internal/domain/parser ./internal/application/service ./mobile` | PASS: sete cenários por API antiga/nova (14), inclusiveGTK011/042, GPN e GOX0/1/2; par8/10 exato, sem par offline, FCX condicionado. Unit getters/cópias/Clear/concorrência e config17/63. | 0 |
| VAL-006 | Raiz: `powershell.exe -NoProfile -ExecutionPolicy Bypass -File specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/build-mobile-aar.ps1` | Bind AAR/sourcejar/proveniência PASS; classes PaymentResult e PaymentPINConfiguration e três APIs adicionais conferidas. | 0 |
| VAL-007 | Raiz: `py specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/validate-versionable-structure.py` | PASS da projeção estrutural; local.properties ignorado permanece limitação já registrada na10011. | 0 |
| VAL-008 | Android, JAVA_HOME JBR/SDK: `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:lintDebug :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug --console=plain` | BUILD SUCCESSFUL38s;30JVM sem falhas, lint/cobertura/builds PASS. Sem alteração de layout. | 0 |
| VAL-009 | Android, ANDROID_SERIAL=emulator-5554: `./gradlew.bat :plugin-saque-cartao:connectedDebugAndroidTest --console=plain` | BUILD SUCCESSFUL33s;16 instrumentados sem falhas. Logger sintético opt-in/disabled/offline/PRD/partial/injection/bound e profileboolean; arquivos anteriores restaurados. | 0 |
| VAL-010 | Raiz: `py specs/changes/10013-retorno-pin-criptografado/evidence/verify-artifacts.py` | 65 hashes de fonte/AAR/API/ABI/nativos/APK e assethost PASS. [JSON](evidence/artifact-verification-2026-10-14.json). | 0 |
| VAL-011 | Alteração JSON externa apenas logPinResults=true; `adb -s emulator-5554 install -r <Android>/app/build/outputs/apk/debug/app-debug.apk`; launcher10012 `-ProvisionOnly -EmulatorSerial emulator-5554` | Host novo instalado; três snapshots com hashes. PIN source e parâmetrosDATA não alterados. Perfil anterior salvo TEMP/codex-10013-perfil-before.json. | 0 |
| VAL-012 | `adb -s emulator-5554 shell am force-stop <pacotehost>` e `... am start -n <pacotehost>/.MainActivity`; ls privado/Get-NetTCPConnection39100 | Host relançado sem compra; perfil375/tabela17773/PIN42 bytes canônicos privados. Listener PID33664 mantido. Fonte perfil atual338 bytes após JSON/flag; tamanhos anteriores são históricos. | 0 |
| VAL-013 | Raiz: `git diff --check` e verificador documental das Changes | PASS de whitespace/UTF8/links/status/AC. Git apenas avisa futura conversão LF→CRLF. | 0 |

Aviso das ferramentas Android: protobuf usa sun.misc.Unsafe depreciado em JBR25;
não impediu testes/build e nenhuma dependência foi atualizada para silenciá-lo.
Não rerodar race conhecido inviável sem gcc e não alegar aprovação desse gate.

Cobertura: command90,0%; parser89,6%; service81,6%; mobile legado66,7%.
Agregado quatro pacotes2008/2471=81,26%; seis arquivospayment400/452=88,50%;
novo mobile/payment_result.go64/67=95,52%. JaCoCo elegível255/262=97,33%.
Adapter/logger Android dependem de framework/binding e são exercitados em
instrumentação, não excluídos para fingir cobertura JVM de todo o módulo.
O resultado sensível usa Clear/copias/singleuse; testes de pares são sintéticos,
não prova de PIN real nem auditoria independente.

AAR final SHA256:3fd298841c1cffdfff7b595bb65051108a593dd9d8da115f373da65bf747bc79,
HEAD26f17c4de084a773a41d1c87b257125265664379/dirty=true/65 sourceHashes.
Bridge executável antigo opaco não mudou, não foi reiniciado ou substituído;
hash histórico está no JSON, não é alegação de recompilação por esta Change.

## Log autorizado e limite físico

Habilitado no perfil externo DES fornecido pelo usuário, preservando todos os
outros campos. Fonte PIN continua TIPO_CHAVE3/INDICE_CHAVE08/WK normalizada
vazia, importada integralmente; nenhum valor de operação foi fixado no adapter.
Destino Android: `no_backup/saque-cartao/pin-results.jsonl` relativo ao diretório
privado do host `br.com.romulopenha.sistemaprototipoandroid`.
Só será criado quando houver par real válido; erroGTK/semPIN não fabrica log.
Valores reais desse arquivo não foram extraídos para ferramentas/chat/relatórios.

Validação integral não concluída: responsabilidade pelo pinpad/rede deve
confirmar/habilitar/cargar chaves e parâmetros aprovados para o slot configurado.
Não alterar para02/07, inventar chave, trocar método30 ou removerGTK. Arquivo de
configuração não injeta DUKPT no equipamento. Nenhum commit/system/archive ou
aprovação financeira nesta etapa.
