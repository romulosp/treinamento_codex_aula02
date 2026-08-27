@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM ============================================================
REM Limpa artefatos gerados do treinamento
REM
REM Preserva fontes, configuracoes e a estrutura Spec Driven.
REM
REM Remove:
REM   - diretorios target e .quarkus dentro de apps\
REM   - arquivos .log dentro de apps\
REM ============================================================

set "ROOT=%~dp0"
set "APPS=%ROOT%apps"
set /a REMOVIDOS=0
set /a ERROS=0

echo.
echo ============================================================
echo LIMPEZA DOS ARTEFATOS GERADOS
echo ============================================================
echo.
echo Workspace:
echo   %ROOT%
echo.
echo Serao removidos, somente dentro de:
echo   %APPS%
echo.
echo Padroes de artefatos:
echo   target
echo   .quarkus
echo   *.log
echo.
echo Sera preservado:
echo   fontes, recursos, testes, scripts e configuracoes
echo   %ROOT%\.git
echo   %ROOT%\specs
echo   %ROOT%\.gitignore
echo.

choice /C SN /N /M "Deseja continuar? [S/N]: "

if errorlevel 2 (
    echo.
    echo Operacao cancelada.
    exit /b 0
)

if not exist "%APPS%" (
    echo.
    echo Nenhum diretorio apps encontrado.
    exit /b 0
)

REM ------------------------------------------------------------
REM Remove diretorios gerados apenas abaixo de apps\
REM ------------------------------------------------------------

for /d /r "%APPS%" %%D in (target .quarkus) do (
    if exist "%%D" (
        echo.
        echo Removendo diretorio:
        echo   %%D
        rd /S /Q "%%D"
        if exist "%%D" (
            echo ERRO: nao foi possivel remover o diretorio %%D
            set /a ERROS+=1
        ) else (
            set /a REMOVIDOS+=1
        )
    )
)

REM ------------------------------------------------------------
REM Remove arquivos de log apenas abaixo de apps\
REM ------------------------------------------------------------

for /r "%APPS%" %%F in (*.log) do (
    if exist "%%F" (
        echo.
        echo Removendo arquivo:
        echo   %%F
        del /F /Q "%%F"
        if exist "%%F" (
            echo ERRO: nao foi possivel remover o arquivo %%F
            set /a ERROS+=1
        ) else (
            set /a REMOVIDOS+=1
        )
    )
)

if not "!ERROS!"=="0" (
    echo.
    echo ============================================================
    echo LIMPEZA CONCLUIDA COM ERROS
    echo ============================================================
    exit /b 1
)

echo.
echo ============================================================
echo LIMPEZA CONCLUIDA
echo ============================================================
echo.
if "!REMOVIDOS!"=="0" (
    echo Nenhum artefato gerado encontrado.
) else (
    echo Artefatos removidos: !REMOVIDOS!
)
echo Estrutura Spec Driven, fontes e configuracoes preservadas.
echo.
exit /b 0