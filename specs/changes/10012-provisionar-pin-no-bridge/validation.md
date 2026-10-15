# Validação — 10012

Iniciada em 2026-10-14. Não se declara pinpad/compra física homologados.

VAL-001: Windows/PowerShell, ADB SDK local, emulator-5554. `adb devices` exit 0;
`adb -s emulator-5554 shell run-as br.com.romulopenha.sistemaprototipoandroid ls -l files/saque-cartao`
exit 0: perfil 346 bytes e tabela 17773 bytes, ambos 11-out; PIN ausente.
`... test -f files/saque-cartao/criptografia.properties` exit 1.
`Get-NetTCPConnection -LocalPort 39100 -State Listen`: listener 127.0.0.1 PID33664.
Log de 14-out termina em bridge_listening; sem chamadas de cartão nesse período.

## Resultado do provisionamento

Revisão: [IMPLEMENTACAO_APROVADA](reviews/2026-10-14-implementation-review.md).
Etapa de arquivos: `VALIDADA`; não equivale à aprovação física da 10011.
Ambiente: Windows10/PowerShell5.1, ADB SDK local/emulator5554/API35, host debug
instalado. Fontes externas DES indicadas pelo usuário; nenhum PRD/credencial geral lido.

| ID | Cwd raiz / comando | Resultado | Exit |
| --- | --- | --- | --- |
| VAL-002 | Parser PowerShell AST ParseFile dos dois scripts produtivos e teste | PASS sintaxe. | 0 |
| VAL-003 | `powershell.exe -NoProfile -ExecutionPolicy Bypass -File specs/changes/10011-corrigir-leitura-cartao-pinpad/evidence/test-payment-pin-config.ps1` | 13 casos sintéticos PASS. | 0 |
| VAL-004 | `powershell.exe -NoProfile -ExecutionPolicy Bypass -File apps/frontend/smartphone/sistema-prototipo-android/scripts/iniciar-saque-cartao.ps1 -ProvisionOnly -ValidateOnly -EmulatorSerial emulator-5554` | Fontes/seleção PASS, zero mutação. | 0 |
| VAL-005 | `powershell.exe -NoProfile -ExecutionPolicy Bypass -File specs/changes/10012-provisionar-pin-no-bridge/evidence/test-provisioning.ps1` | 8 casos PASS: inválido/PRD/ValidateOnly, sem Java/COM, reverse-only, helper integrado, invalidação e finally restore. Três snapshots com hash, mesmo PID/serial count antes/depois. | 0 |
| VAL-006 | Launcher `-ValidateOnly -PortaPinpad COM7 -EmulatorSerial emulator-5554` | Modo completo mantém preflight COM/toolchain/AAR, PASS sem iniciar segundo listener. | 0 |

Primeira tentativa de VAL-005 interrompeu na captura do stderr esperado de um
caso negativo por ErrorActionPreference=Stop do harness. Finally restaurou DES;
harness passou a capturar stderr/exit de modo controlado, nova execução PASS.
Nenhuma falha produtiva foi ocultada. Fonte PIN real não aparece em saídas.

Após os testes, host aberto para verificação manual: usuário entrou e executou
jornada às00:59. CargaEMV/GCX passaram, GTK011 falhou e CLO000 fechou; isso
confirma superação de CRYPTO_MISSING e outra falha de configuração/dispositivo.
Não contar essa operação manual no teste de isolamento anterior. Diagnóstico
GIX posterior só consulta mapas, sem GCX/GPN/GOX nem KSN/keys em saída.
