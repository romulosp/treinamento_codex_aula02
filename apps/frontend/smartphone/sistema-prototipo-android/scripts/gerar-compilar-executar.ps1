param(
    [string]$JavaHome = "D:\desenvolvimento\ferramentas_android\Nova pasta\AndroidStudio\jbr",
    [string]$AndroidSdk = "D:\desenvolvimento\ferramentas_android\Sdk"
)

$ErrorActionPreference = "Stop"
$projectDir = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$gradleWrapper = Join-Path $projectDir "gradlew.bat"
$adb = Join-Path $AndroidSdk "platform-tools\adb.exe"
$hostApk = Join-Path $projectDir "app\build\outputs\apk\debug\app-debug.apk"
$pluginApk = Join-Path $projectDir "plugin-login\build\outputs\apk\debug\plugin-login-debug.apk"
$packageName = "br.com.romulopenha.sistemaprototipoandroid"
$remoteDirectory = "/sdcard/Android/data/$packageName/files/plugins/inbox"
$remoteTemporary = "$remoteDirectory/plugin-login.apk.upload"
$remotePlugin = "$remoteDirectory/plugin-login.apk"

if (-not (Test-Path -LiteralPath "$JavaHome\bin\java.exe")) { throw "JBR/JDK não encontrado em: $JavaHome" }
if (-not (Test-Path -LiteralPath $adb)) { throw "adb não encontrado em: $adb" }

$env:JAVA_HOME = $JavaHome
$env:ANDROID_SDK_ROOT = $AndroidSdk
$env:ANDROID_HOME = $AndroidSdk

Write-Host "[1/5] Gerando host e plugin como APKs separados..."
Push-Location $projectDir
try {
    & $gradleWrapper :app:assembleDebug :plugin-login:assembleDebug --no-daemon --console=plain
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
} finally {
    Pop-Location
}

Write-Host "[2/5] Instalando somente o host..."
& $adb install -r $hostApk
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "[3/5] Iniciando o microkernel e o observador da pasta dinâmica..."
& $adb shell am force-stop $packageName
& $adb shell am start -n "$packageName/.MainActivity"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Start-Sleep -Seconds 1

Write-Host "[4/5] Entregando plugin-login na pasta observada..."
& $adb shell mkdir -p $remoteDirectory
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& $adb push $pluginApk $remoteTemporary
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& $adb shell mv $remoteTemporary $remotePlugin
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "[5/5] Aguardando descoberta, validação e ativação dinâmica..."
Start-Sleep -Seconds 2
Write-Host "Host aberto. O plugin foi entregue separadamente para validação manual."
