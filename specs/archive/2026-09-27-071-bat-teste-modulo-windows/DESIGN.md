# DESIGN — 071 BAT de teste do módulo Windows

O artefato será um BAT simples, editável e autocontido na raiz do módulo Go.
Ele não substituirá `start_aplication.bat`; será um atalho explícito para o
cenário de teste do Bridge com pinpad físico.

O README do módulo deverá apontar para o BAT e diferenciar:

- teste físico Windows: `testar_bridge_pinpad.bat`;
- teste sem hardware: `PINPAD_BRIDGE_TRANSPORT=scripted`;
- teste Android Emulator: Bridge local e `adb reverse`.

## Evolução corretiva na 072

O BAT permanece launcher host em foreground, mas a evolução usa variável
herdada e pode delegar preparação/verificação do reverse a helper PowerShell.
Readiness, seleção de emulador e preservação do exit code seguem o
[DESIGN 072](../../archive/2026-09-27-072-corrigir-abertura-bridge-android/DESIGN.md).
O desenho original autocontido não exige manter sobrescrita da configuração.
