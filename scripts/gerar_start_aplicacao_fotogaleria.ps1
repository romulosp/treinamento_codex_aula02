[CmdletBinding()]
param(
    [string]$ChavesPath = 'D:\desenvolvimento\chave_des\chave_des.properties',
    [string]$ProjectName = 'fotogaleria',
    [string]$OutputPath = ''
)

$ErrorActionPreference = 'Stop'
$requiredKeys = @(
    'HOSTNAME_DB_POSTGRESQL',
    'PORTA_DB_POSTGRESQL',
    'BANCO_DB_TAREFA',
    'USER_DB_POSTGRESQL',
    'SENHA_DB_POSTGRESQL'
)
$templatePath = Join-Path $PSScriptRoot 'templates\start_aplicacao-fotogaleria.bat.template'
if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $OutputPath = Join-Path $PSScriptRoot '..\apps\backend\fotogaleria\start_aplicacao.bat'
}
$outputFullPath = [System.IO.Path]::GetFullPath($OutputPath)
$temporaryPath = $null

function Fail-Safely([string]$Message) {
    [Console]::Error.WriteLine("ERRO: $Message")
    exit 1
}

function Escape-BatchValue([string]$Key, [string]$Value) {
    if ($Value.Contains("`r") -or $Value.Contains("`n") -or $Value.Contains('%') -or $Value.Contains('!')) {
        Fail-Safely "valor da chave $Key contem caractere nao suportado pelo cmd.exe."
    }
    $escaped = $Value.Replace('^', '^^')
    foreach ($pair in @(@('&', '^&'), @('|', '^|'), @('<', '^<'), @('>', '^>'), @('(', '^('), @(')', '^)'), @('"', '^"'))) {
        $escaped = $escaped.Replace($pair[0], $pair[1])
    }
    return $escaped
}

if (-not (Test-Path -LiteralPath $ChavesPath -PathType Leaf)) {
    Fail-Safely "arquivo de configuracao ausente em $ChavesPath."
}
if (-not (Test-Path -LiteralPath $templatePath -PathType Leaf)) {
    Fail-Safely 'template do start_aplicacao.bat nao encontrado.'
}

$properties = @{}
$targetSection = "DB POSTGRESQL $ProjectName"
$insideTargetSection = $false
$sectionFound = $false
$lineNumber = 0

try {
    foreach ($line in Get-Content -LiteralPath $ChavesPath) {
        $lineNumber++
        $trimmed = $line.Trim()
        if ($trimmed -match '^##\s*(.+?)\s*$') {
            if ($insideTargetSection) { break }
            $insideTargetSection = $Matches[1].Equals($targetSection, [StringComparison]::OrdinalIgnoreCase)
            if ($insideTargetSection) { $sectionFound = $true }
            continue
        }
        if (-not $insideTargetSection -or [string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith('#')) {
            continue
        }
        $separator = $line.IndexOf('=')
        if ($separator -le 0) {
            Fail-Safely "formato invalido na linha $lineNumber da secao $targetSection."
        }
        $key = $line.Substring(0, $separator).Trim()
        $value = $line.Substring($separator + 1).Trim()
        if ($properties.ContainsKey($key)) {
            Fail-Safely "chave duplicada na secao ${targetSection}: $key."
        }
        $properties[$key] = $value
    }
}
catch {
    Fail-Safely "nao foi possivel processar a secao $targetSection."
}

if (-not $sectionFound) {
    Fail-Safely "secao obrigatoria ausente: ##$targetSection."
}
$missingKeys = @($requiredKeys | Where-Object {
    -not $properties.ContainsKey($_) -or [string]::IsNullOrWhiteSpace([string]$properties[$_])
})
if ($missingKeys.Count -gt 0) {
    Fail-Safely "chaves obrigatorias ausentes ou vazias na secao ${targetSection}: $($missingKeys -join ', ')."
}

$values = @{}
foreach ($key in $requiredKeys) {
    $values[$key] = Escape-BatchValue $key ([string]$properties[$key])
}

try {
    $template = [System.IO.File]::ReadAllText($templatePath)
    $replacements = @{
        '${HOSTNAME_DB_POSTGRESQL}' = $values['HOSTNAME_DB_POSTGRESQL']
        '${PORTA_DB_POSTGRESQL}' = $values['PORTA_DB_POSTGRESQL']
        '${BANCO_DB_TAREFA}' = $values['BANCO_DB_TAREFA']
        '${USER_DB_POSTGRESQL}' = $values['USER_DB_POSTGRESQL']
        '${SENHA_DB_POSTGRESQL}' = $values['SENHA_DB_POSTGRESQL']
    }
    $rendered = $template
    foreach ($placeholder in $replacements.Keys) {
        $rendered = $rendered.Replace($placeholder, [string]$replacements[$placeholder])
    }
    if ($rendered -match '\$\{[A-Z0-9_]+\}') {
        Fail-Safely 'template contem placeholder nao resolvido.'
    }
    $outputDirectory = Split-Path -Parent $outputFullPath
    if (-not (Test-Path -LiteralPath $outputDirectory -PathType Container)) {
        New-Item -ItemType Directory -Path $outputDirectory | Out-Null
    }
    $temporaryPath = "$outputFullPath.tmp.$PID"
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($temporaryPath, $rendered, $utf8NoBom)
    Move-Item -LiteralPath $temporaryPath -Destination $outputFullPath -Force
}
catch {
    if ($temporaryPath -and (Test-Path -LiteralPath $temporaryPath)) {
        Remove-Item -LiteralPath $temporaryPath -Force -ErrorAction SilentlyContinue
    }
    Fail-Safely "falha ao gerar start_aplicacao.bat ($($_.Exception.GetType().Name))."
}

Write-Output "BAT da secao $targetSection gerado com sucesso."
