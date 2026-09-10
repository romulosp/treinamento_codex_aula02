param(
    [string]$ArquivoChaves = 'D:\desenvolvimento\chave_des\chave_des.properties',
    [string]$Template = (Join-Path $PSScriptRoot 'templates\start_aplicacao-dashboardatendimentoservico.bat.template'),
    [string]$Destino = (Join-Path $PSScriptRoot '..\apps\backend\dashboardatendimentoservico\start_aplicacao.bat')
)

$ErrorActionPreference = 'Stop'

function Get-Secao {
    param([string[]]$Linhas, [string]$Cabecalho)
    $inicio = -1
    for ($i = 0; $i -lt $Linhas.Count; $i++) {
        if ($Linhas[$i].Trim() -match $Cabecalho) { $inicio = $i + 1; break }
    }
    if ($inicio -lt 0) { throw "Secao obrigatoria ausente: $Cabecalho" }
    $resultado = @{}
    for ($i = $inicio; $i -lt $Linhas.Count; $i++) {
        $linha = $Linhas[$i].Trim()
        if ($linha -match '^##(?:DB|SSO)\s') { break }
        if ($linha -eq '' -or $linha.StartsWith('#')) { continue }
        if ($linha -match '^([^=]+?)\s*=\s*(.*)$') {
            $resultado[$Matches[1].Trim()] = $Matches[2].Trim()
        }
    }
    return $resultado
}

function Assert-Chaves {
    param([hashtable]$Secao, [string[]]$Chaves)
    $ausentes = @($Chaves | Where-Object { -not $Secao.ContainsKey($_) -or [string]::IsNullOrWhiteSpace($Secao[$_]) })
    if ($ausentes.Count -gt 0) { throw "Chaves obrigatorias ausentes ou vazias: $($ausentes -join ', ')" }
}

function ConvertTo-BatchValue {
    param([string]$Valor)
    if ($Valor -match "[`r`n]") { throw 'Valor de configuracao invalido.' }
    return $Valor.Replace('%', '%%').Replace('"', '^"')
}

if (-not (Test-Path -LiteralPath $ArquivoChaves -PathType Leaf)) {
    Write-Error "Arquivo de chaves ausente: $ArquivoChaves"
    exit 1
}

try {
    $linhas = Get-Content -LiteralPath $ArquivoChaves -Encoding utf8
    $db = Get-Secao $linhas '^##DB\s+POSTGRESQL\s+DASHBOARDCOMPRAS$'
    $sso = Get-Secao $linhas '^##SSO\s+KEYCLOAK\s+Internet\s+local$'
    $dbKeys = @('HOSTNAME_DB_POSTGRESQL','PORTA_DB_POSTGRESQL','BANCO_DB_DASHBOARDCOMPRAS','USER_DB_POSTGRESQL','SENHA_DB_POSTGRESQL')
    $ssoKeys = @('OIDC_AUTH_SERVER_URL_INTERNET','OIDC_CLIENT_ID_INTERNET','OIDC_CLIENT_SECRET_INTERNET')
    Assert-Chaves $db $dbKeys
    Assert-Chaves $sso $ssoKeys
    if (-not (Test-Path -LiteralPath $Template -PathType Leaf)) { throw "Template ausente: $Template" }

    $conteudo = Get-Content -Raw -LiteralPath $Template -Encoding utf8
    $mapa = @{
        '{{POSTGRES_HOST}}' = $db['HOSTNAME_DB_POSTGRESQL'];
        '{{POSTGRES_PORT}}' = $db['PORTA_DB_POSTGRESQL'];
        '{{POSTGRES_DB}}' = $db['BANCO_DB_DASHBOARDCOMPRAS'];
        '{{POSTGRES_USER}}' = $db['USER_DB_POSTGRESQL'];
        '{{POSTGRES_PASSWORD}}' = $db['SENHA_DB_POSTGRESQL'];
        '{{OIDC_AUTH_SERVER_URL}}' = $sso['OIDC_AUTH_SERVER_URL_INTERNET'];
        '{{OIDC_CLIENT_ID}}' = $sso['OIDC_CLIENT_ID_INTERNET'];
        '{{OIDC_CLIENT_SECRET}}' = $sso['OIDC_CLIENT_SECRET_INTERNET'];
    }
    foreach ($placeholder in $mapa.Keys) {
        $conteudo = $conteudo.Replace($placeholder, (ConvertTo-BatchValue $mapa[$placeholder]))
    }
    if ($conteudo -match '{{[^}]+}}') { throw 'O template manteve placeholders sem valor.' }

    $diretorio = Split-Path -Parent $Destino
    New-Item -ItemType Directory -Force -Path $diretorio | Out-Null
    $temporario = "$Destino.tmp"
    Set-Content -LiteralPath $temporario -Value $conteudo -Encoding ascii
    Move-Item -LiteralPath $temporario -Destination $Destino -Force
    Write-Output "BAT local gerado com sucesso em: $Destino"
    exit 0
} catch {
    Write-Error $_.Exception.Message
    exit 1
}
