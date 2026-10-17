[CmdletBinding()]
param(
    [string]$ChavesPath = 'D:\desenvolvimento\chave_des\chave_des.properties'
)

$ErrorActionPreference = 'Stop'
$requiredKeys = @(
    'OIDC_AUTH_SERVER_URL_MOBILE_INTRANET',
    'OIDC_CLIENT_ID_MOBILE_SERVICE_INTRANET',
    'OIDC_CLIENT_SECRET_MOBILE_SERVICE_INTRANET'
)
$templatePath = Join-Path $PSScriptRoot 'templates\start_aplicacao-autenticadorsso.bat.template'
$outputPath = [System.IO.Path]::GetFullPath(
    (Join-Path $PSScriptRoot '..\apps\backend\autenticadorsso\start_aplicacao.bat')
)
$temporaryPath = $null

function Fail-Safely([string]$Message) {
    [Console]::Error.WriteLine("ERRO: $Message")
    exit 1
}

if (-not (Test-Path -LiteralPath $ChavesPath -PathType Leaf)) {
    Fail-Safely "arquivo de chaves ausente em $ChavesPath."
}
if (-not (Test-Path -LiteralPath $templatePath -PathType Leaf)) {
    Fail-Safely 'template do autenticador SSO nao encontrado.'
}

$properties = @{}
try {
    $lineNumber = 0
    foreach ($line in Get-Content -LiteralPath $ChavesPath) {
        $lineNumber++
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith('#')) {
            continue
        }
        $separator = $line.IndexOf('=')
        if ($separator -le 0) {
            Fail-Safely "formato invalido na linha $lineNumber do arquivo de chaves."
        }
        $key = $line.Substring(0, $separator).Trim()
        $value = $line.Substring($separator + 1).Trim()
        $properties[$key] = $value
    }
} catch {
    Fail-Safely "nao foi possivel ler o arquivo de chaves em $ChavesPath."
}

$missingKeys = @($requiredKeys | Where-Object {
    -not $properties.ContainsKey($_) -or [string]::IsNullOrWhiteSpace([string]$properties[$_])
})
if ($missingKeys.Count -gt 0) {
    Fail-Safely "chaves obrigatorias ausentes ou vazias: $($missingKeys -join ', ')."
}

function Escape-BatchValue([string]$Key, [string]$Value) {
    if ($Value.Contains("`r") -or $Value.Contains("`n") -or $Value.Contains('%') -or $Value.Contains('!')) {
        Fail-Safely "valor da chave $Key contem caractere nao suportado pelo cmd.exe."
    }
    $escaped = $Value.Replace('^', '^^')
    $escaped = $escaped.Replace('&', '^&').Replace('|', '^|')
    $escaped = $escaped.Replace('<', '^<').Replace('>', '^>')
    $escaped = $escaped.Replace('(', '^(').Replace(')', '^)')
    return $escaped.Replace('"', '^"')
}

$rendered = [System.IO.File]::ReadAllText($templatePath)
foreach ($key in $requiredKeys) {
    $safeValue = Escape-BatchValue $key ([string]$properties[$key])
    $rendered = $rendered.Replace("`${$key}", $safeValue)
}
if ($rendered -match '\$\{[A-Z0-9_]+\}') {
    Fail-Safely 'template contem placeholder nao resolvido.'
}

try {
    $outputDirectory = Split-Path -Parent $outputPath
    if (-not (Test-Path -LiteralPath $outputDirectory -PathType Container)) {
        Fail-Safely "diretorio do backend ausente em $outputDirectory."
    }
    $temporaryPath = "$outputPath.tmp.$PID"
    $utf8NoBom = New-Object -TypeName System.Text.UTF8Encoding -ArgumentList $false
    [System.IO.File]::WriteAllText($temporaryPath, $rendered, $utf8NoBom)
    Move-Item -LiteralPath $temporaryPath -Destination $outputPath -Force
} catch {
    if ($temporaryPath -and (Test-Path -LiteralPath $temporaryPath)) {
        Remove-Item -LiteralPath $temporaryPath -Force -ErrorAction SilentlyContinue
    }
    Fail-Safely "falha ao gerar o launcher ($($_.Exception.GetType().Name))."
}

Write-Output "Launcher local gerado em $outputPath."
