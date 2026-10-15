# Validação — 10016

Status: `REPROVADA` integral por PIN físico, race e auditoria pendentes.
Validação técnica da implementação PASS após IMPLEMENTACAO_APROVADA.
Descoberta física GIX validada em reteste autorizado em14-out22:12.

Baseline10015: GTK000 físico, GOX042 porPIN08ausente. Usuário agora autoriza
GIX8035 e sorteio entre todos os presentes. Ambiente Windows/Go1.26.5/API35;
nenhuma alteração de stack. Registrar comandos/exits e limites aqui.

## Comandos e resultados — 14-out-2026

Ambiente: Windows10/Go1.26.5windows386 para testes, geraçãoAAR Go1.26.5amd64
portátil/JDK17.0.11/NDK28.2.13676358/API29/quatroABIs. Android wrapper9.6.0/
JBR25/alvoJava17/compile37target36min29, emulator-5554/API35. Sem upgrades.

| ID | Diretório/comando | Exit | Resultado |
| --- | --- | --- | --- |
| VAL-001 | Go `go test ./...` | 0 | PASS todos os pacotes. |
| VAL-002 | Go `go vet ./...` | 0 | PASS. |
| VAL-003 | Go `go test -tags integration -coverprofile build/10016-coverage.out ./internal/domain/command ./internal/domain/parser ./internal/application/service ./mobile` | 0 | PASS; command90,0%, parser89,9%, service84,7%, mobile67,4% legado. |
| VAL-004 | Raiz `python specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/validate-versionable-structure.py` | 0 | Projeção13arquivos versionáveis PASS; local.properties preexistente/ignorado fora da projeção como10011. |
| VAL-005 | Raiz `powershell -NoProfile -ExecutionPolicy Bypass -File specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/build-mobile-aar.ps1` | 0 | AAR/sourcejar/proveniência regenerados;69sourcehashes. |
| VAL-006 | Android `gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:lintDebug :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug --console=plain` | 0 | PASS26s;32JVM/0falhas;256/263=97,34% elegível;Kotlin/native/plugin/host recompilados. |
| VAL-007 | Android ANDROID_SERIAL=emulator-5554 `gradlew.bat :plugin-saque-cartao:connectedDebugAndroidTest --console=plain` | 0 | PASS39s;18instrumentados/0falhas, incluindo símboloJNI novo, erro sanitizado sem sessão, sem TCP/serial no teste. |
| VAL-008 | Raiz `python specs/changes/10016-descobrir-indice-pin-gix/evidence/verify-artifacts.py` | 0 final | 69hashesfonte/AAR/11APIs/4ABIs/min29/merged/stripped/APK/pluginasset do host e32/18testes conferidos. |
| VAL-009 | Go `go run ./build/10016-key-discovery` | 0 processo; FALHA funcional | `open_failed SERIAL_UNAVAILABLE`; não chegou aoGIX e não capturou. Ver diagnóstico físico. |
| VAL-010 | ADB5554 `install -r <Android>/app/build/outputs/apk/debug/app-debug.apk`; launcher `-ProvisionOnly -EmulatorSerial emulator-5554`; `am force-stop <host>`; `am start -n <host>/.MainActivity` | 0 | Host atualizado,3snapshotsDES/hashesconfirmed, Activity relançada. Fonteproperties/perfil não alterada nesta Change. |
| VAL-011 | Go `go run ./build/10016-key-discovery`, reteste pedido pelo usuário | 0 | PASS físico22:12:23America/Sao_Paulo:abertura/GIX000,2candidatas,índice02sorteado/cachemesmo02,CLO000/closeconfirmed. Sem captura financeira. |

Go: apps/desktop/libpinpadabecsgo. Android:
apps/frontend/smartphone/sistema-prototipo-android. Host:
br.com.romulopenha.sistemaprototipoandroid. Ver [relatório de artefatos](evidence/artifact-verification-2026-10-14.json)
e [diagnóstico físico](evidence/diagnostico-fisico.md).

Integração44cenários:28regressão +14descoberta com PIN17 viaGIX/fonte08,
nas duasAPIs/GPN/GOX/GTK +2mapa inválido/zero com somenteOPN/GIX/CLO, semGCX.
Core cobre sorteio de cada candidato com entropia determinística, singleton,
zero/falhaentropia, flags0/1/2, DATA excluída, cache/fontealterada/nova sessão,
GCX com índice incorreto bloqueado, cancelamento/close duranteGIX sem publicação.
UIstate preserva erroPIN_KEY_MISSING e não permite formulário/captura.

Cobertura focal2203/2657=82,91%; arquivos pagamento581/631=92,08%.
Novosparser11/11=100%, service52/53=98,11%, mobile11/11=100%.
Não declarar80% no mobile legado inteiro.

AAR SHA256 `383259f6095ca25a079fc0ed388027c47f61b5369f31bbde51485e8501e875eb`;
pluginAPK `c5ed17db928c5465058b829615ae8d7f72eeac3c1a60620bf194a933a38d56b5`;
hostAPK `18b8991432866f0ed2fe62ce365d36a03b0f4c4b1b2ab39d563901392482721e`.
NovaAPI ResolvePaymentPINConfiguration compilada no adapter e exercitada JNI.

## Avisos, limites e retomada

Atualização após relato do usuário “funcionou”: validação funcional da senha
confirmada pelo usuário. Revisão do LogPinpadAbecs.txt (369753bytes/3004linhas)
encontrou liberação da sessão/clientedisconnected/bridge_stopped comcodeOK no
encerramento. Bridge opaco mantém payloads redigidos, portanto isso não comprova
GOXRES/PINblock/KSN. No emulator-5554, perfilDES/logPinResults=true/GTKclaro
confirmados, porém no_backup/saque-cartao/pin-results.jsonl não existe; Logcat
retido sem eventosABECS/pin_key_selected. Destino do logger conferido no código:
contexto host/noBackupFilesDir. Não houve nova captura ou alteração de dados.
Par criptográfico real continua NÃO COMPROVADO; não concluir PINoffline/ausência
de captura online apenas por faltar registro. Distinguir relato funcional do
usuário da confirmação técnica do par; não copiar valores criptográficos para evidência.

Instrumentação mostrou depreciação sun.misc.Unsafe/protobuf3.25.5 existente,
sem falha. Verificador de artefatos chamado durante instrumentação retornouexit1
porque relatório ainda estava incompleto; após término e18/0, verificador final
exit0, sem relaxar asserções. Git diff --check exit0; avisos LF/CRLF somente.
Verificador de documentos UTF8/status/links acompanha evidência10016.

Hardware: primeira aberturaCOM7 falhou; reteste autorizado passou. Listener33664
mantido e nenhum processo externo encerrado. Descoberta/cache/fechamento físicos
agora comprovados. Falta jornada do usuário com cartão/PIN. Não comprovar PINblock/KSN físico ou fechar
aceite financeiro com testes sintéticos. Sorteio não prova associação da chave
à rede; é política DES expressamente solicitada, registrada em ADR001.

Race CGO+gcc e auditoria independente HIGH_ASSURANCE herdados10011 pendentes.
Sem alteração de system, aprovação formal, commit ou archive.
