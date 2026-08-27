@echo off
setlocal

rem ===== CONFIGURACAO DO AMBIENTE =====
set JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11
set MAVEN_HOME=C:\Desenvolvimento\apache-maven-3.8.8
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

rem ===== CONFIGURACAO DA APLICACAO =====
set "AUTH-SERVER-URL=https://login2des.caixa.gov.br/auth/realms/internet"
set "CLIENT-ID=cli-ser-spl"
set "SECRET=77a26834-9534-4693-8363-264d213b5fc4"
set "CLIENTS-AUTHORIZED=cli-ser-spl"
set "DB2_JDBC_URL=jdbc:db2://10.192.224.76:5021/CSD1"
set "DB2_USERNAME=SSPLDB01"
set "DB2_PASSWORD=SSPLDB01"

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