@echo off
setlocal

rem ===== CONFIGURACAO DO AMBIENTE =====
set JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11
set MAVEN_HOME=C:\Desenvolvimento\apache-maven-3.8.8
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo ==========================================
echo Java em uso:
"%JAVA_HOME%\bin\java.exe" -version
echo ==========================================
echo.


mvn quarkus:dev

pause
endlocal