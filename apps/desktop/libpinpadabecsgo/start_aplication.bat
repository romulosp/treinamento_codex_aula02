@echo off
setlocal

cd /d "%~dp0"

rem ===== CONFIGURACAO DO PINPAD =====
if "%PORTA_PINPAD%"=="" set "PORTA_PINPAD=COM7"
set PINPAD_BAUDRATE=19200
set PINPAD_TIMEOUT=30
if "%PINPAD_LOG_FILE%"=="" set "PINPAD_LOG_FILE=%~dp0logs\LogPinpadAbecs.txt"

rem ===== CONFIGURACAO DO GO =====
set GOROOT=C:\Desenvolvimento\go1.26.5.windows-386
set PATH=%GOROOT%\bin;%PATH%
set GOCACHE=%~dp0.gocache
set GOMODCACHE=%~dp0.gomodcache
set BIN_DIR=%~dp0.bin
set EXE_PATH=%BIN_DIR%\libpinpadabecsgo.exe

if not exist "%~dp0logs" mkdir "%~dp0logs"
if errorlevel 1 (
    echo ERRO: nao foi possivel criar o diretorio de logs.
    pause
    exit /b 1
)

rem ===== INFORMACOES =====
echo ==========================================
echo Biblioteca Go ABECS - teste local
echo Porta: %PORTA_PINPAD%
echo Baud rate: %PINPAD_BAUDRATE%
echo Timeout: %PINPAD_TIMEOUT% segundos
echo Log serial ativo: %PINPAD_LOG_FILE%
echo Diretorio: %CD%
echo ==========================================
echo.

where go >nul 2>&1
if errorlevel 1 (
    echo ERRO: Go nao foi encontrado no PATH.
    echo Instale/configure o Go antes de executar este arquivo.
    pause
    exit /b 1
)

echo GOROOT: %GOROOT%
go version
echo.

echo Executando o programa de validacao...
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"
"%GOROOT%\bin\go.exe" build -o "%EXE_PATH%" .\cmd\libpinpadabecsgo
if errorlevel 1 (
    echo ERRO: a compilacao falhou.
    set EXIT_CODE=%ERRORLEVEL%
    goto :finish
)

echo Executando o binario local: %EXE_PATH%
"%EXE_PATH%"
set EXIT_CODE=%ERRORLEVEL%

:finish
echo.
if not "%EXIT_CODE%"=="0" (
    echo O teste terminou com codigo %EXIT_CODE%.
    echo Se aparecer bloqueio de politica de grupo, solicite permissao ao administrador do Windows.
) else (
    echo Teste concluido com sucesso.
)

pause
exit /b %EXIT_CODE%
