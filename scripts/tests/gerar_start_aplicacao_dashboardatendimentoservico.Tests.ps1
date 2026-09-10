$ErrorActionPreference = 'Stop'
$raiz = Resolve-Path (Join-Path $PSScriptRoot '..\..')
$gerador = Join-Path $raiz 'scripts\gerar_start_aplicacao_dashboardatendimentoservico.ps1'
$template = Join-Path $raiz 'scripts\templates\start_aplicacao-dashboardatendimentoservico.bat.template'
$temp = Join-Path ([System.IO.Path]::GetTempPath()) ("dashboard-config-" + [Guid]::NewGuid())
New-Item -ItemType Directory -Path $temp | Out-Null
try {
    $arquivo = Join-Path $temp 'chaves.properties'
    $destino = Join-Path $temp 'start.bat'
    @'
##DB POSTGRESQL DASHBOARDCOMPRAS
HOSTNAME_DB_POSTGRESQL = host-sintetico
PORTA_DB_POSTGRESQL = 5432
BANCO_DB_DASHBOARDCOMPRAS = banco-sintetico
USER_DB_POSTGRESQL = usuario-sintetico
SENHA_DB_POSTGRESQL = senha-sintetica
##SSO KEYCLOAK Internet local
OIDC_AUTH_SERVER_URL_INTERNET = http://oidc.invalid/realms/internet
OIDC_CLIENT_ID_INTERNET = cliente-sintetico
OIDC_CLIENT_SECRET_INTERNET = segredo-sintetico
'@ | Set-Content -LiteralPath $arquivo -Encoding utf8

    & $gerador -ArquivoChaves $arquivo -Template $template -Destino $destino
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path $destino)) { throw 'Geracao sintetica deveria concluir com sucesso.' }
    $resultado = Get-Content -Raw -LiteralPath $destino
    if ($resultado -match '{{[^}]+}}') { throw 'O BAT gerado manteve placeholders.' }
    if ($resultado -notmatch '/realms/internet') { throw 'O realm internet nao foi mapeado.' }

    (Get-Content -Raw $arquivo).Replace('OIDC_CLIENT_SECRET_INTERNET = segredo-sintetico','') | Set-Content $arquivo -Encoding utf8
    $errorActionAnterior = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $gerador -ArquivoChaves $arquivo -Template $template -Destino $destino 2>$null
    $codigoFalhaEsperada = $LASTEXITCODE
    $ErrorActionPreference = $errorActionAnterior
    if ($codigoFalhaEsperada -eq 0) { throw 'Chave ausente deveria causar falha.' }
    Write-Output 'Testes do gerador concluidos com sucesso.'
    exit 0
} finally {
    Remove-Item -LiteralPath $temp -Recurse -Force
}
