@echo off
setlocal

cd /d "%~dp0"

rem ===== CONFIGURACAO DO PINPAD FISICO =====
set "PORTA_PINPAD=COM14"
set "PINPAD_BAUDRATE=19200"
set "PINPAD_TIMEOUT=30"
set "PINPAD_BRIDGE_PORT=39100"
set "PINPAD_LOG_FILE=%~dp0logs\LogPinpadAbecs.txt"

rem Transporte scripted e removido: este BAT usa a porta fisica configurada.
set "PINPAD_BRIDGE_TRANSPORT="

echo ==========================================
echo Bridge Windows - teste com pinpad fisico
echo ==========================================
echo PORTA_PINPAD=%PORTA_PINPAD%
echo PINPAD_BAUDRATE=%PINPAD_BAUDRATE%
echo PINPAD_TIMEOUT=%PINPAD_TIMEOUT%
echo PINPAD_BRIDGE_PORT=%PINPAD_BRIDGE_PORT%
echo PINPAD_LOG_FILE=%PINPAD_LOG_FILE%
echo.

go run .\cmd\libpinpadabecsgo-bridge

echo.
echo Bridge encerrado. Consulte o log acima.
pause
endlocal
