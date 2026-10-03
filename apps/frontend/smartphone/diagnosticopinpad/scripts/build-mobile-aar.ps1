[CmdletBinding()]
param(
    [switch]$AllowDirty
)

$ErrorActionPreference = "Stop"
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..\..")).Path
$goModule = Join-Path $repoRoot "apps\desktop\libpinpadabecsgo"
$aarOutput = Join-Path $repoRoot "apps\frontend\smartphone\diagnosticopinpad\app\libs\libpinpadabecsgo.aar"
$provenanceOutput = Join-Path $repoRoot "apps\frontend\smartphone\diagnosticopinpad\build\mobile-aar-provenance.json"
$mobileVersion = "v0.0.0-20260908204917-8b95e45f8d3e"

if (-not (Get-Command go -ErrorAction SilentlyContinue)) {
    throw "Go não encontrado no PATH."
}

$goVersion = (& go version)
$goHostArch = (& go env GOHOSTARCH)
if ($goHostArch.Trim() -ne 'amd64') {
    throw 'O NDK Windows instalado requer Go host amd64. Selecione o Go de 64 bits no PATH antes de gerar o AAR.'
}
$dirty = (& git -C $repoRoot status --porcelain)
if ($dirty -and -not $AllowDirty) {
    throw "A árvore está dirty. Use -AllowDirty somente para evidência local explícita."
}
$revision = (& git -C $repoRoot rev-parse --short HEAD)

& go install "golang.org/x/mobile/cmd/gomobile@$mobileVersion"
if ($LASTEXITCODE -ne 0) {
    throw "Falha ao instalar gomobile $mobileVersion."
}

$goBin = (& go env GOBIN)
if ([string]::IsNullOrWhiteSpace($goBin)) {
    $goBin = Join-Path (& go env GOPATH) "bin"
}
$gomobile = Join-Path $goBin "gomobile.exe"
if (-not (Test-Path -LiteralPath $gomobile)) {
    $gomobile = Join-Path $goBin "gomobile"
}
if (-not (Test-Path -LiteralPath $gomobile)) {
    throw "gomobile não foi localizado após a instalação."
}

New-Item -ItemType Directory -Force -Path (Split-Path $aarOutput), (Split-Path $provenanceOutput) | Out-Null
& $gomobile init
if ($LASTEXITCODE -ne 0) {
    throw "gomobile init falhou."
}

$ldflags = "-X br.com.romulopenha/lib-pinpad-abecs-go/mobile.buildVersion=0.1.0 -X br.com.romulopenha/lib-pinpad-abecs-go/mobile.buildRevision=$revision -X br.com.romulopenha/lib-pinpad-abecs-go/mobile.buildDirty=$([bool]$dirty)"
Push-Location $goModule
try {
    # O Windows PowerShell pode dividir argumentos nativos no formato
    # "-nome=valor" quando eles são escritos diretamente na invocação.
    # O gomobile recebe o prefixo Java e os flags de build como argumentos
    # indivisíveis; por isso a lista é montada explicitamente antes da chamada.
    $bindArguments = @(
        'bind'
        '-target=android'
        '-androidapi=26'
        '-javapkg=br.com.romulopenha.libpinpadabecsgo'
        "-o=$aarOutput"
        "-ldflags=$ldflags"
        './mobile'
    )
    & $gomobile @bindArguments
    if ($LASTEXITCODE -ne 0) {
        throw "gomobile bind falhou."
    }
} finally {
    Pop-Location
}

$sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $aarOutput).Hash
[ordered]@{
    generatedAt = (Get-Date).ToUniversalTime().ToString("o")
    goVersion = $goVersion
    gomobileBuildMetadata = (& go version -m $gomobile)
    xMobileVersion = $mobileVersion
    gitRevision = $revision
    dirty = [bool]$dirty
    sha256 = $sha256
    minSdk = 26
    target = 'android'
    goHostArch = $goHostArch.Trim()
    javaHome = $env:JAVA_HOME
    androidHome = $env:ANDROID_HOME
    androidNdkHome = $env:ANDROID_NDK_HOME
    command = "gomobile bind -target=android -androidapi=26 -javapkg=br.com.romulopenha.libpinpadabecsgo ./mobile"
} | ConvertTo-Json | Set-Content -Encoding utf8 -LiteralPath $provenanceOutput

Write-Host "AAR gerado: $aarOutput"
Write-Host "SHA-256: $sha256"
