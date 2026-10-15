# Validação — 10015

Status: `REPROVADA` integral por gates físicos/race/auditoria pendentes;
validação técnica desta implementação PASS, sem afirmar GTK/PIN físicos.

Ambiente e gates herdados10014; instrução atual troca GTK para em claro via
perfil. A ausência da chaveDATA08 não deve bloquear esse modo; seleçãoPIN08
é independente e continua exigindo a chave física quando kernel/GPN pedir PIN.

## Evidências — 2026-10-14

Após [IMPLEMENTACAO_APROVADA](reviews/2026-10-14-implementation-review.md),
Windows10/Go1.26.5windows386; geração AAR Go1.26.5amd64/JDK17.0.11/NDK28.2.13676358/
API29/quatroABIs. Android wrapper9.6.0/JBR25/alvoJava17/SDKcompile37target36min29,
emulator-5554/API35. Stack e dependências existentes, sem upgrades.

| ID | Diretório/comando | Exit | Resultado |
| --- | --- | --- | --- |
| VAL-001 | Raiz: `python specs/changes/10015-configurar-gtk-claro/evidence/set-des-gtk-clear.py` | 0 | Só gtkDataMethod do perfil externo mudou30→vazio; índice08/demais campos iguais; backup `%TEMP%/codex-10015-perfil-before.json`. Script histórico de aplicação única, não rerodar para desfazer configuração. |
| VAL-002 | Go: `go test ./...` | 0 | PASS todos os pacotes. |
| VAL-003 | Go: `go vet ./...` | 0 | PASS. |
| VAL-004 | Go: `go test -tags integration -coverprofile build/10015-coverage.out ./internal/domain/command ./internal/domain/parser ./internal/application/service ./mobile` | 0 | PASS; command90,0%, parser89,6%, service83,9%, mobilelegado66,6%. |
| VAL-005 | Raiz: `powershell -NoProfile -ExecutionPolicy Bypass -File specs/changes/10015-configurar-gtk-claro/evidence/test-gtk-profile.ps1` | 0 | 9casos sintéticos: vazio/30 válidos;00/null/numérico/booleano/ausente/índice100/índiceString rejeitados. ValidateOnly não escreve nem opera serial. |
| VAL-006 | Raiz: `powershell -NoProfile -ExecutionPolicy Bypass -File specs/changes/10012-provisionar-pin-no-bridge/evidence/test-provisioning.ps1` | 0 | 8casos negativos/provisionamento/isolation PASS; snapshot DES em claro restaurado com3hashes; listener/contagemserial inalterados. |
| VAL-007 | Raiz: `python specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/validate-versionable-structure.py` | 0 | Projeção13arquivos versionáveis PASS; exclui local.properties preexistente/ignorado, como10011. |
| VAL-008 | Raiz: `powershell -NoProfile -ExecutionPolicy Bypass -File specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/build-mobile-aar.ps1` | 0 | AAR/sourcejar/proveniência regenerados. |
| VAL-009 | Android: `gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:lintDebug :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug --console=plain` | 0 | PASS32s;31JVM/0falhas, cobertura255/262=97,33%; Kotlin recompilado/testado, plugin/host repacotados. |
| VAL-010 | Android ANDROID_SERIAL=emulator-5554: `gradlew.bat :plugin-saque-cartao:connectedDebugAndroidTest --console=plain` | 0 | PASS36s;17instrumentados executados/0falhas, parser/preflight em claro, negativos, logPIN privado mantido. |
| VAL-011 | Raiz: `python specs/changes/10015-configurar-gtk-claro/evidence/verify-artifacts.py` | 0 | 66sourcehashes/AAR/APIs10/ABIs/merged/stripped/APK/asset host conferidos;31JVM/17instrumentados. |
| VAL-012 | ADB5554 `install -r <Android>/app/build/outputs/apk/debug/app-debug.apk`; launcher `-ProvisionOnly -EmulatorSerial emulator-5554`; ler perfilprivado e conferir modo/index; `am force-stop <host>`; `am start -n <host>/.MainActivity` | 0 | Host atualizado e3fontes provisionadas com hashes; gtkDataMethod vazio/gtkKeyIndex08 confirmados; Activity relançada. Sem OPN/compra/chaves. Bridge127.0.0.1:39100PID33664 preservado. |

Go executado em apps/desktop/libpinpadabecsgo; Android em
apps/frontend/smartphone/sistema-prototipo-android; packagehost
br.com.romulopenha.sistemaprototipoandroid. Script/artifact
[relatório](evidence/artifact-verification-2026-10-14.json) contém hashes e cobertura.
Gates de documentação: `python specs/changes/10015-configurar-gtk-claro/evidence/verify-documents.py`
e `git diff --check`, exit0; avisos LF/CRLF somente.

Integração28cenários=7cenários×2APIs×2modosGTK. Vetor literal GTK008 somente
SPE_TRACKS, índicePIN17/DATA7 sintéticos distintos, resumos sem conteúdo, GTK011/042
bloqueiam continuação em ambos os modos. Core verifica00/03/05/06 com GTK em claro
e PINonline criptografado independente, semKSNDATA fabricado; escolhas inválidas
não enviam I/O. Snapshot retém dados e metadados por cópia, mantém limpeza10014.
Coberturafocal2119/2574=82,32%; arquivos pagamento503/552=91,12%; authorization95/96=98,96%.

AAR SHA256 `17a4beba4005f65f7dd4ddfb9654f79e46e49c6b39719fcc1cbeb92d1dc74c3c`;
pluginAPK `169a5c00ac8c28b4453bb02fdaf7e38bf719b862c3f8981d8abd7c6d1db3169e`;
hostAPK `dbf4b077cbb4a9805eb8beb3ce37ac0c75e957c6d17fe1f29c12c5ce2412ea56`.
ABI/binding mantidos; sourcejar mudou por godoc atualizado. Carregador de negócio
copia assets a cada sessão e isola nativos/cache por digest, confirmado em leitura
de BusinessPluginManager; não foi necessário apagar caches/dados do usuário.

## Limites e retomada

Atualização física08:29/08:31 de14-out: GTK000 comprovado no teste do usuário,
GOX042 ST_ERRKEY, CLO000. Consulta GIX atual confirmouPIN DUKPTTDES08 ausente,
PIN02/07 presentes; snapshot Android seleciona3/08/WKvazia do properties.
Ver [diagnóstico específico](evidence/diagnostico-gox042-2026-10-14.md).
Dependência concreta: carga PIN08 pelo responsável ou indicação de outro índice
autorizado para alterar a fonte; nenhuma seleção automática foi feita.

Instrumentação reportou depreciação sun.misc.Unsafe/protobuf3.25.5 existente,
sem falha. Primeira leitura do relatório durante instrumentação em andamento
mostrou0testes; relatório final gerado após término confirma17. Checker final
exige31/17/zerofalhas, não aceita relatórios vazios. Nenhuma versão atualizada.

Nova escolha explícita do usuário substitui dependência DATA08 do GTK ATUAL;
não confirma carga DATA08. Ativar30 depois depende da carga/configuração DATA
autorizada e nova jornada/provisionamento. PIN3/08 permanece fonteproperties;
GIX histórico informavaPIN08ausente, portanto PIN físico ainda precisa evidência.
Nenhuma compra foi executada automaticamente. Próximo gate: usuário testar nova
jornada em claro e enviar log se falhar; interpretar o comando/status real.
Race CGO+gcc e auditoria independente HIGH_ASSURANCE herdados10011 ainda pendentes.
Nenhuma aprovação formal, system, commit ou archive.
