@echo off
setlocal
set DIR=%~dp0
if not exist "%DIR%gradle\wrapper\gradle-wrapper.jar" (
  echo Gradle Wrapper JAR ausente. Instale Gradle 9.6.0 e execute "gradle wrapper" neste projeto.
  exit /b 1
)
java -classpath "%DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
set "GRADLE_EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %GRADLE_EXIT_CODE%
