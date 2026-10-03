@echo off
setlocal

cd /d "%~dp0"
if errorlevel 1 exit /b 1

where go >nul 2>&1
if errorlevel 1 (
    echo ERRO: Go nao encontrado no PATH.
    pause
    exit /b 1
)
if not exist "go.mod" (
    echo ERRO: modulo Go nao encontrado.
    pause
    exit /b 1
)

rem ===== CONFIGURACAO DO PINPAD FISICO =====
rem PORTA_PINPAD vem do processo Windows. Nunca sobrescrever a COM do operador.
powershell -NoProfile -Command "if ($env:PORTA_PINPAD -cnotmatch '^(?i:COM[1-9][0-9]{0,3})$') { exit 1 }"
if errorlevel 1 (
    echo ERRO: defina PORTA_PINPAD como uma porta COM valida no ambiente Windows.
    echo Exemplo no PowerShell: $env:PORTA_PINPAD = '^<COM_DO_PINPAD^>'
    pause
    exit /b 1
)
if not defined PINPAD_BAUDRATE set "PINPAD_BAUDRATE=19200"
if not defined PINPAD_TIMEOUT set "PINPAD_TIMEOUT=30"
if not defined PINPAD_BRIDGE_PORT set "PINPAD_BRIDGE_PORT=39100"
if not defined PINPAD_LOG_FILE set "PINPAD_LOG_FILE=%~dp0logs\LogPinpadAbecs.txt"

rem Transporte scripted e removido: este BAT usa a porta fisica configurada.
set "PINPAD_BRIDGE_TRANSPORT="

echo ==========================================
echo Bridge Windows - teste com pinpad fisico
echo ==========================================
powershell -NoProfile -Command "foreach ($key in @('PORTA_PINPAD','PINPAD_BAUDRATE','PINPAD_TIMEOUT','PINPAD_BRIDGE_PORT','PINPAD_LOG_FILE')) { Write-Host ($key + '=' + [Environment]::GetEnvironmentVariable($key)) }"
echo.

rem Reverse e opcional para iniciar o host; o helper informa indisponibilidade.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0preparar_emulador.ps1"

go run .\cmd\libpinpadabecsgo-bridge
set "BRIDGE_EXIT_CODE=%ERRORLEVEL%"

echo.
echo Bridge encerrado. Codigo de saida: %BRIDGE_EXIT_CODE%
pause
endlocal & exit /b %BRIDGE_EXIT_CODE%
