[CmdletBinding()]
param(
    [ValidateSet('Tudo', 'Subir', 'Analisar', 'Status', 'Parar')]
    [string]$Acao = 'Tudo',

    [ValidateRange(30, 900)]
    [int]$TempoEsperaSegundos = 240,

    [string]$DiretorioSonar = 'D:\desenvolvimento\sonar',

    [string]$DiretorioPostgreSql = 'D:\desenvolvimento\banco_dados\postgresql',

    [string]$DiretorioProjeto
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($DiretorioProjeto)) {
    $DiretorioProjeto = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
}

$CodigoFallbackLlm = 20
$NomeProjetoSonar = 'sonar-local'
$UrlSonarLocal = 'http://localhost:9000'
$ImagemScanner = 'sonarsource/sonar-scanner-cli:latest'

function Encerrar-Com-Erro {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Mensagem,

        [int]$Codigo = 1
    )

    [Console]::Error.WriteLine("ERRO: $Mensagem")
    exit $Codigo
}

function Solicitar-AuditoriaLlm {
    param([Parameter(Mandatory = $true)][string]$Motivo)

    Write-Output "SONAR_FALLBACK_LLM_REQUIRED: $Motivo"
    Write-Output 'Execute a Auditoria de Qualidade Assistida por LLM e registre escopo, arquivos, comandos, resultados, achados e correções em validation.md.'
    exit $CodigoFallbackLlm
}

function Testar-Comando {
    param([Parameter(Mandatory = $true)][string]$Nome)

    return $null -ne (Get-Command $Nome -ErrorAction SilentlyContinue)
}

function Invocar-Comando {
    param(
        [Parameter(Mandatory = $true)][string]$Arquivo,
        [Parameter(Mandatory = $true)][string[]]$Argumentos,
        [Parameter(Mandatory = $true)][string]$Descricao
    )

    & $Arquivo @Argumentos
    if ($LASTEXITCODE -ne 0) {
        throw "$Descricao falhou com código de saída $LASTEXITCODE."
    }
}

function Confirmar-DockerDisponivel {
    if (-not (Testar-Comando 'docker')) {
        Solicitar-AuditoriaLlm 'o comando Docker não está instalado ou não está disponível no PATH.'
    }

    & docker version --format '{{.Server.Version}}' > $null 2>&1
    if ($LASTEXITCODE -ne 0) {
        Solicitar-AuditoriaLlm 'o daemon Docker não está acessível. Inicie ou corrija o Docker Desktop antes de executar o Sonar.'
    }
}

function Obter-ArquivoCompose {
    param([Parameter(Mandatory = $true)][string]$Diretorio)

    $arquivo = Join-Path $Diretorio 'docker-compose.yml'
    if (-not (Test-Path -LiteralPath $arquivo -PathType Leaf)) {
        Encerrar-Com-Erro "arquivo docker-compose.yml não encontrado em $Diretorio."
    }

    return $arquivo
}

function Invocar-ComposePostgreSql {
    param([Parameter(Mandatory = $true)][string[]]$Argumentos)

    $arquivoCompose = Obter-ArquivoCompose $DiretorioPostgreSql
    Invocar-Comando 'docker' (@('compose', '-f', $arquivoCompose) + $Argumentos) 'A composição PostgreSQL'
}

function Obter-IdContainerPostgreSql {
    $arquivoCompose = Obter-ArquivoCompose $DiretorioPostgreSql
    $identificador = [string](& docker compose -f $arquivoCompose ps -q postgres 2>$null | Select-Object -First 1)
    if ($LASTEXITCODE -ne 0) {
        return $null
    }

    return $identificador.Trim()
}

function Obter-SaudePostgreSql {
    $identificador = Obter-IdContainerPostgreSql
    if ([string]::IsNullOrWhiteSpace($identificador)) {
        return $null
    }

    $estado = [string](& docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' $identificador 2>$null | Select-Object -First 1)
    if ($LASTEXITCODE -ne 0) {
        return $null
    }

    return $estado.Trim()
}

function Aguardar-SaudePostgreSql {
    $limite = (Get-Date).AddSeconds($TempoEsperaSegundos)
    do {
        if ((Obter-SaudePostgreSql) -eq 'healthy') {
            return $true
        }

        Start-Sleep -Seconds 3
    } while ((Get-Date) -lt $limite)

    return $false
}

function Garantir-PostgreSql {
    if ((Obter-SaudePostgreSql) -ne 'healthy') {
        Write-Output 'PostgreSQL não está saudável; iniciando a composição local.'
        try {
            Invocar-ComposePostgreSql @('up', '-d')
        }
        catch {
            Solicitar-AuditoriaLlm 'não foi possível iniciar a composição PostgreSQL necessária ao Sonar.'
        }
    }

    if (-not (Aguardar-SaudePostgreSql)) {
        Solicitar-AuditoriaLlm "o PostgreSQL não atingiu o estado healthy em até $TempoEsperaSegundos segundos."
    }
}

function Nova-SenhaAleatoria {
    $bytes = New-Object byte[] 32
    $gerador = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $gerador.GetBytes($bytes)
    }
    finally {
        $gerador.Dispose()
    }

    return (($bytes | ForEach-Object { $_.ToString('x2') }) -join '')
}

function Obter-CredenciaisBancoSonar {
    if (-not (Test-Path -LiteralPath $DiretorioSonar -PathType Container)) {
        Encerrar-Com-Erro "diretório do Sonar não encontrado em $DiretorioSonar."
    }

    $arquivoEnv = Join-Path $DiretorioSonar '.env'
    if (-not (Test-Path -LiteralPath $arquivoEnv -PathType Leaf)) {
        $senha = Nova-SenhaAleatoria
        $conteudo = "SONAR_DB_USER=sonar`r`nSONAR_DB_PASSWORD=$senha`r`n"
        $utf8SemBom = New-Object -TypeName System.Text.UTF8Encoding -ArgumentList $false
        [System.IO.File]::WriteAllText($arquivoEnv, $conteudo, $utf8SemBom)
        Write-Output "Arquivo local de credenciais criado em $arquivoEnv."
    }

    $valores = @{}
    foreach ($linha in Get-Content -LiteralPath $arquivoEnv) {
        $texto = $linha.Trim()
        if ([string]::IsNullOrWhiteSpace($texto) -or $texto.StartsWith('#')) {
            continue
        }

        $indiceSeparador = $texto.IndexOf('=')
        if ($indiceSeparador -gt 0) {
            $valores[$texto.Substring(0, $indiceSeparador)] = $texto.Substring($indiceSeparador + 1)
        }
    }

    if (-not $valores.ContainsKey('SONAR_DB_USER') -or -not $valores.ContainsKey('SONAR_DB_PASSWORD') -or
        [string]::IsNullOrWhiteSpace($valores['SONAR_DB_USER']) -or [string]::IsNullOrWhiteSpace($valores['SONAR_DB_PASSWORD'])) {
        Encerrar-Com-Erro "o arquivo $arquivoEnv deve conter SONAR_DB_USER e SONAR_DB_PASSWORD sem valores vazios."
    }

    if ($valores['SONAR_DB_USER'] -notmatch '^[a-z][a-z0-9_]{0,62}$') {
        Encerrar-Com-Erro 'SONAR_DB_USER possui formato inválido para o PostgreSQL.'
    }

    return [PSCustomObject]@{
        Arquivo = $arquivoEnv
        Usuario = [string]$valores['SONAR_DB_USER']
        Senha   = [string]$valores['SONAR_DB_PASSWORD']
    }
}

function Garantir-BancoSonar {
    param([Parameter(Mandatory = $true)]$Credenciais)

    $identificador = Obter-IdContainerPostgreSql
    if ([string]::IsNullOrWhiteSpace($identificador)) {
        Solicitar-AuditoriaLlm 'o container PostgreSQL não foi localizado após o healthcheck.'
    }

    $usuario = $Credenciais.Usuario
    $senha = $Credenciais.Senha.Replace("'", "''")
    $sql = @'
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_catalog.pg_roles WHERE rolname = '{0}') THEN
        CREATE ROLE {0} LOGIN PASSWORD '{1}';
    ELSE
        ALTER ROLE {0} WITH LOGIN PASSWORD '{1}';
    END IF;
END
$$;
SELECT 'CREATE DATABASE sonar OWNER {0}'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'sonar')
\gexec
'@ -f $usuario, $senha

    $sql | & docker exec -i $identificador psql -U root -d postgres -v ON_ERROR_STOP=1 2>$null
    if ($LASTEXITCODE -ne 0) {
        Solicitar-AuditoriaLlm 'não foi possível preparar o usuário ou banco PostgreSQL do Sonar.'
    }
}

function Invocar-ComposeSonar {
    param(
        [Parameter(Mandatory = $true)]$Credenciais,
        [Parameter(Mandatory = $true)][string[]]$Argumentos
    )

    $arquivoCompose = Obter-ArquivoCompose $DiretorioSonar
    Invocar-Comando 'docker' (@('compose', '--env-file', $Credenciais.Arquivo, '-f', $arquivoCompose, '--project-name', $NomeProjetoSonar) + $Argumentos) 'A composição SonarQube'
}

function Obter-StatusSonar {
    try {
        $resposta = Invoke-WebRequest -Uri "$UrlSonarLocal/api/system/status" -UseBasicParsing -TimeoutSec 5
        if ($resposta.StatusCode -eq 200) {
            return ([string]((ConvertFrom-Json $resposta.Content).status))
        }
    }
    catch {
        return $null
    }

    return $null
}

function Aguardar-SonarAtivo {
    $limite = (Get-Date).AddSeconds($TempoEsperaSegundos)
    do {
        if ((Obter-StatusSonar) -eq 'UP') {
            return $true
        }

        Start-Sleep -Seconds 5
    } while ((Get-Date) -lt $limite)

    return $false
}

function Garantir-SonarAtivo {
    Garantir-PostgreSql
    $credenciais = Obter-CredenciaisBancoSonar
    Garantir-BancoSonar $credenciais

    if ((Obter-StatusSonar) -ne 'UP') {
        Write-Output 'SonarQube não está disponível; iniciando a composição local.'
        try {
            Invocar-ComposeSonar $credenciais @('up', '-d')
        }
        catch {
            Solicitar-AuditoriaLlm 'não foi possível iniciar a composição SonarQube.'
        }
    }

    if (-not (Aguardar-SonarAtivo)) {
        Solicitar-AuditoriaLlm "o SonarQube não atingiu o estado UP em até $TempoEsperaSegundos segundos."
    }
}

function Confirmar-TokenSonar {
    if ([string]::IsNullOrWhiteSpace($env:SONAR_TOKEN)) {
        Solicitar-AuditoriaLlm 'SONAR_TOKEN não foi informado no ambiente de execução; a análise autenticada não pode ser realizada.'
    }
}

function Obter-ModulosJava {
    $diretorioBackend = Join-Path $DiretorioProjeto 'apps\backend'
    if (-not (Test-Path -LiteralPath $diretorioBackend -PathType Container)) {
        return @()
    }

    return @(Get-ChildItem -LiteralPath $diretorioBackend -Directory | Where-Object {
        Test-Path -LiteralPath (Join-Path $_.FullName 'pom.xml') -PathType Leaf
    })
}

function Obter-ModulosFrontend {
    $diretorioFrontend = Join-Path $DiretorioProjeto 'apps\frontend\web'
    if (-not (Test-Path -LiteralPath $diretorioFrontend -PathType Container)) {
        return @()
    }

    return @(Get-ChildItem -LiteralPath $diretorioFrontend -Directory | Where-Object {
        Test-Path -LiteralPath (Join-Path $_.FullName 'package.json') -PathType Leaf
    })
}

function Invocar-Scanner {
    param(
        [Parameter(Mandatory = $true)][string]$DiretorioModulo,
        [Parameter(Mandatory = $true)][string[]]$Propriedades,
        [Parameter(Mandatory = $true)][string]$Descricao
    )

    $argumentos = @(
        'run', '--rm',
        '--network', "$NomeProjetoSonar`_default",
        '--mount', "type=bind,source=$DiretorioModulo,target=/usr/src",
        '--workdir', '/usr/src',
        '--env', 'SONAR_HOST_URL=http://sonarqube:9000',
        '--env', 'SONAR_TOKEN',
        $ImagemScanner
    ) + $Propriedades

    Invocar-Comando 'docker' $argumentos $Descricao
}

function Invocar-AnaliseJava {
    param([Parameter(Mandatory = $true)]$Modulo)

    if (-not (Testar-Comando 'mvn')) {
        Encerrar-Com-Erro 'Maven não está disponível no PATH; configure Java 17 e Maven antes da análise Java.'
    }

    Push-Location -LiteralPath $Modulo.FullName
    try {
        Invocar-Comando 'mvn' @('-DskipTests', 'package') "O build Java do módulo $($Modulo.Name)"
    }
    finally {
        Pop-Location
    }

    $propriedades = @(
        "-Dsonar.projectKey=aula02-java-$($Modulo.Name)",
        "-Dsonar.projectName=Aula 02 Java - $($Modulo.Name)",
        '-Dsonar.sources=src/main/java,src/main/resources',
        '-Dsonar.tests=src/test/java',
        '-Dsonar.java.binaries=target/classes',
        '-Dsonar.java.test.binaries=target/test-classes',
        '-Dsonar.exclusions=**/target/**',
        '-Dsonar.qualitygate.wait=true',
        '-Dsonar.qualitygate.timeout=300'
    )

    Invocar-Scanner $Modulo.FullName $propriedades "O scanner Sonar do módulo Java $($Modulo.Name)"
}

function Invocar-AnaliseFrontend {
    param([Parameter(Mandatory = $true)]$Modulo)

    if (-not (Testar-Comando 'npm')) {
        Encerrar-Com-Erro 'npm não está disponível no PATH; instale Node.js antes da análise front-end.'
    }

    Push-Location -LiteralPath $Modulo.FullName
    try {
        Invocar-Comando 'npm' @('run', 'build') "O build front-end do módulo $($Modulo.Name)"
    }
    finally {
        Pop-Location
    }

    $propriedades = @(
        "-Dsonar.projectKey=aula02-frontend-$($Modulo.Name)",
        "-Dsonar.projectName=Aula 02 Front-end - $($Modulo.Name)",
        '-Dsonar.sources=.',
        '-Dsonar.test.inclusions=**/*.test.js,**/*.test.jsx,**/*.test.ts,**/*.test.tsx,**/*.spec.js,**/*.spec.jsx,**/*.spec.ts,**/*.spec.tsx',
        '-Dsonar.exclusions=**/node_modules/**,**/dist/**,**/coverage/**',
        '-Dsonar.qualitygate.wait=true',
        '-Dsonar.qualitygate.timeout=300'
    )

    Invocar-Scanner $Modulo.FullName $propriedades "O scanner Sonar do módulo front-end $($Modulo.Name)"
}

function Executar-Analises {
    Confirmar-TokenSonar

    $modulosJava = @(Obter-ModulosJava)
    $modulosFrontend = @(Obter-ModulosFrontend)
    if (($modulosJava.Count + $modulosFrontend.Count) -eq 0) {
        Encerrar-Com-Erro 'nenhum módulo Java Maven ou front-end com package.json foi localizado para análise.'
    }

    foreach ($modulo in $modulosJava) {
        Invocar-AnaliseJava $modulo
    }

    foreach ($modulo in $modulosFrontend) {
        Invocar-AnaliseFrontend $modulo
    }
}

function Exibir-Status {
    $status = Obter-StatusSonar
    if ($status -eq 'UP') {
        Write-Output "SonarQube disponível em $UrlSonarLocal (estado UP)."
        return
    }

    Solicitar-AuditoriaLlm 'o SonarQube local não está disponível para consulta de status.'
}

function Parar-Sonar {
    $arquivoEnv = Join-Path $DiretorioSonar '.env'
    if (-not (Test-Path -LiteralPath $arquivoEnv -PathType Leaf)) {
        Encerrar-Com-Erro "não há arquivo .env em $DiretorioSonar; não foi possível determinar a configuração segura para parar o Sonar."
    }

    $credenciais = [PSCustomObject]@{ Arquivo = $arquivoEnv }
    Invocar-ComposeSonar $credenciais @('down')
    Write-Output 'SonarQube local parado. O PostgreSQL permanece em execução por ser uma dependência compartilhada.'
}

try {
    Confirmar-DockerDisponivel

    switch ($Acao) {
        'Tudo' {
            Garantir-SonarAtivo
            Executar-Analises
            Write-Output 'Análise Sonar concluída para todos os módulos localizados.'
        }
        'Subir' {
            Garantir-SonarAtivo
            Write-Output "SonarQube disponível em $UrlSonarLocal."
        }
        'Analisar' {
            Garantir-SonarAtivo
            Executar-Analises
            Write-Output 'Análise Sonar concluída para todos os módulos localizados.'
        }
        'Status' {
            Exibir-Status
        }
        'Parar' {
            Parar-Sonar
        }
    }
}
catch {
    Encerrar-Com-Erro $_.Exception.Message
}
