@echo off
setlocal EnableExtensions
cd /d "%~dp0"

rem ===== CONFIGURACAO DO AMBIENTE =====
set JAVA_HOME=C:\Desenvolvimento\jdk-17.0.11
set MAVEN_HOME=C:\Desenvolvimento\apache-maven-3.8.8
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%
set POSTGRESQL_JDBC_URL=jdbc:postgresql://localhost:5432/tarefa
set POSTGRES_COMPOSE_DIR=D:\desenvolvimento\banco_dados\postgresql

if "%POSTGRESQL_USERNAME%"=="" (
  echo ERRO: defina POSTGRESQL_USERNAME antes de iniciar a aplicacao.
  exit /b 1
)
if "%POSTGRESQL_PASSWORD%"=="" (
  echo ERRO: defina POSTGRESQL_PASSWORD antes de iniciar a aplicacao.
  exit /b 1
)

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo ERRO: Java 17 nao encontrado em %JAVA_HOME%
  exit /b 1
)
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo ERRO: Maven nao encontrado em %MAVEN_HOME%
  exit /b 1
)
if not exist "%POSTGRES_COMPOSE_DIR%\docker-compose.yml" (
  echo ERRO: Compose nao encontrado em %POSTGRES_COMPOSE_DIR%
  exit /b 1
)

echo Subindo PostgreSQL pelo Docker Compose...
cd /d "%POSTGRES_COMPOSE_DIR%"
docker compose version >nul 2>&1
if not errorlevel 1 goto compose_v2
  docker-compose version >nul 2>&1
  if errorlevel 1 (
    echo ERRO: Docker Compose nao esta disponivel.
    exit /b 1
  )
  docker-compose up -d
if errorlevel 1 (
  echo ERRO: nao foi possivel iniciar o PostgreSQL.
  exit /b 1
)
goto aguardar_banco

:compose_v2
docker compose up -d
if errorlevel 1 (
  echo ERRO: nao foi possivel iniciar o PostgreSQL.
  exit /b 1
)

echo Aguardando o container postgres_db ficar saudavel...
:aguardar_banco
set /a TENTATIVAS=0
docker inspect --format="{{.State.Health.Status}}" postgres_db 2>nul | findstr /i "healthy" >nul
if not errorlevel 1 goto banco_ok
set /a TENTATIVAS+=1
if %TENTATIVAS% GEQ 30 (
  echo ERRO: postgres_db nao ficou saudavel no tempo esperado.
  exit /b 1
)
timeout /t 2 /nobreak >nul
goto aguardar_banco

:banco_ok
cd /d "%~dp0"
echo ==========================================
echo Java em uso:
"%JAVA_HOME%\bin\java.exe" -version
echo ==========================================
echo PostgreSQL: %POSTGRESQL_JDBC_URL%
echo Swagger: http://127.0.0.1:8080/q/swagger-ui/
echo API: http://127.0.0.1:8080/tarefas
echo.
mvn quarkus:dev
set CODIGO=%errorlevel%
pause
endlocal & exit /b %CODIGO%
