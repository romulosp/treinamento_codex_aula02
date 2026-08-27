@echo off
setlocal EnableExtensions

REM ============================================================
REM Limpa artefatos gerados do treinamento
REM
REM Preserva a estrutura Spec Driven e remove somente saídas de compilação.
REM
REM Remove:
REM   - target\ dentro dos módulos Maven
REM ============================================================

set "ROOT=C:\Desenvolvimento\ia\treinamento_codex\aula02"
set "BACKEND=%ROOT%\apps\backend"

echo.
echo ============================================================
echo LIMPEZA DOS ARTEFATOS GERADOS
echo ============================================================
echo.
echo Workspace:
echo   %ROOT%
echo.
echo Sera removido:
echo   %BACKEND%\target
echo.
echo Sera preservado:
echo   %ROOT%\specs
echo   %ROOT%\.gitignore
echo.

choice /C SN /N /M "Deseja continuar? [S/N]: "

if errorlevel 2 (
    echo.
    echo Operacao cancelada.
    exit /b 0
)

REM ------------------------------------------------------------
REM Remove saídas Maven geradas
REM ------------------------------------------------------------

if exist "%BACKEND%\target" (
    echo.
    echo Removendo diretorio:
    echo   %BACKEND%\target

    rd /S /Q "%BACKEND%\target"

    if exist "%BACKEND%\target" (
        echo.
        echo ERRO: nao foi possivel remover o diretorio target.
        exit /b 1
    )
) else (
    echo.
    echo Nenhum diretorio target encontrado.
)

echo.
echo ============================================================
echo LIMPEZA CONCLUIDA
echo ============================================================
echo.
echo Estrutura Spec Driven preservada.
echo.
exit /b 0