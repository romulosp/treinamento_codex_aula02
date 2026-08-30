[CmdletBinding()]
param(
    [string]$ChavesPath = 'D:\desenvolvimento\chave_des\chave_des.properties'
)

$ErrorActionPreference = 'Stop'
$requiredKeys = @(
    'HOSTNAME_DB_POSTGRESQL',
    'PORTA_DB_POSTGRESQL',
    'BANCO_DB',
    'USER_DB_POSTGRESQL',
    'SENHA_DB_POSTGRESQL',
    'OIDC_AUTH_SERVER_URL',
    'OIDC_CLIENT_ID',
    'OIDC_CLIENT_SECRET'
)
$templatePath = Join-Path $PSScriptRoot 'templates\start_aplicacao-gerenciartarefas.bat.template'
$outputPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\apps\backend\gerenciartarefas\start_aplicacao.bat'))
$temporaryPath = $null

function Fail-Safely([string]$Message) {
    [Console]::Error.WriteLine("ERRO: $Message")
    exit 1
}

if (-not (Test-Path -LiteralPath $ChavesPath -PathType Leaf)) {
    Fail-Safely "arquivo de chaves ausente em $ChavesPath. Crie-o e configure as chaves necessarias."
}
if (-not (Test-Path -LiteralPath $templatePath -PathType Leaf)) {
    Fail-Safely 'template do start_aplicacao.bat nao encontrado.'
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
}
catch {
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
    $escaped = $escaped.Replace('&', '^&')
    $escaped = $escaped.Replace('|', '^|')
    $escaped = $escaped.Replace('<', '^<')
    $escaped = $escaped.Replace('>', '^>')
    $escaped = $escaped.Replace('(', '^(')
    $escaped = $escaped.Replace(')', '^)')
    $escaped = $escaped.Replace('"', '^"')
    return $escaped
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
        '${BANCO_DB}' = $values['BANCO_DB']
        '${USER_DB_POSTGRESQL}' = $values['USER_DB_POSTGRESQL']
        '${SENHA_DB_POSTGRESQL}' = $values['SENHA_DB_POSTGRESQL']
        '${OIDC_AUTH_SERVER_URL}' = $values['OIDC_AUTH_SERVER_URL']
        '${OIDC_CLIENT_ID}' = $values['OIDC_CLIENT_ID']
        '${OIDC_CLIENT_SECRET}' = $values['OIDC_CLIENT_SECRET']
    }
    $rendered = $template
    foreach ($placeholder in $replacements.Keys) {
        $rendered = $rendered.Replace($placeholder, [string]$replacements[$placeholder])
    }
    if ($rendered -match '\$\{[A-Z0-9_]+\}') {
        Fail-Safely 'template contem placeholder nao resolvido.'
    }
    $temporaryPath = "$outputPath.tmp.$PID"
    $utf8NoBom = New-Object -TypeName System.Text.UTF8Encoding -ArgumentList $false
    [System.IO.File]::WriteAllText($temporaryPath, $rendered, $utf8NoBom)
    Move-Item -LiteralPath $temporaryPath -Destination $outputPath -Force
}
catch {
    if ($temporaryPath -and (Test-Path -LiteralPath $temporaryPath)) {
        Remove-Item -LiteralPath $temporaryPath -Force -ErrorAction SilentlyContinue
    }
    Fail-Safely "falha ao renderizar ou substituir o BAT final ($($_.Exception.GetType().Name))."
}

Write-Output "BAT final gerado em $outputPath."
