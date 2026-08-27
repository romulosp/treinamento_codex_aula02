@echo off
setlocal

rem ===== CONFIGURACAO DO AMBIENTE =====
set JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11
set MAVEN_HOME=C:\Desenvolvimento\apache-maven-3.8.8
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

rem Configuracao exclusiva da maquina local (arquivo ignorado pelo Git).
if exist "%~dp0start_aplicacao.local.bat" call "%~dp0start_aplicacao.local.bat"

set "CONFIGURACAO_INVALIDA="
if not defined AUTH-SERVER-URL (
	echo ERRO: a variavel AUTH-SERVER-URL deve estar configurada.
	set "CONFIGURACAO_INVALIDA=1"
)
if not defined CLIENT-ID (
	echo ERRO: a variavel CLIENT-ID deve estar configurada.
	set "CONFIGURACAO_INVALIDA=1"
)
if not defined SECRET (
	echo ERRO: a variavel SECRET deve estar configurada.
	set "CONFIGURACAO_INVALIDA=1"
)
if not defined CLIENTS-AUTHORIZED (
	echo ERRO: a variavel CLIENTS-AUTHORIZED deve estar configurada.
	set "CONFIGURACAO_INVALIDA=1"
)
if not defined DB2_JDBC_URL (
	echo ERRO: a variavel DB2_JDBC_URL deve estar configurada.
	set "CONFIGURACAO_INVALIDA=1"
)
if not defined DB2_USERNAME (
	echo ERRO: a variavel DB2_USERNAME deve estar configurada.
	set "CONFIGURACAO_INVALIDA=1"
)
if not defined DB2_PASSWORD (
	echo ERRO: a variavel DB2_PASSWORD deve estar configurada.
	set "CONFIGURACAO_INVALIDA=1"
)

if defined CONFIGURACAO_INVALIDA (
	echo.
	echo Configure as variaveis no ambiente ou em start_aplicacao.local.bat.
	exit /b 1
)

echo ==========================================
echo Java em uso:
"%JAVA_HOME%\bin\java.exe" -version
echo ==========================================
echo.

pushd "%~dp0"
mvn quarkus:dev
popd

pause
endlocal