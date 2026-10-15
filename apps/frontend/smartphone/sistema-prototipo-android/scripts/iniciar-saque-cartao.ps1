<#
.SYNOPSIS
Compila e prepara homologação DES; Bridge físico pronto precede abertura do host.
.DESCRIPTION
Perfil e tabela externos são provisionados somente no armazenamento privado debug.
Não instala chaves, não acessa PRD, não encerra processos concorrentes. ValidateOnly
verifica configuração e ambiente sem Bridge, provisionamento ou abertura Android.
ProvisionOnly prepara arquivos no host debug já instalado, sem COM, build,
instalação, Bridge ou abertura Android; pode ser usado com Bridge já em execução.
#>
[CmdletBinding()]
param(
    [string]$PerfilPath = $(if ($env:PINPAD_PERFIL_PATH) { $env:PINPAD_PERFIL_PATH } else { 'D:\desenvolvimento\chave_des\perfil_saque_cartao_des.json' }),
    [string]$TabelaPath = $(if ($env:PINPAD_TABELA_PATH) { $env:PINPAD_TABELA_PATH } else { 'D:\desenvolvimento\chave_des\emv_des.json' }),
    [string]$CriptografiaPath = $(if ($env:PINPAD_CRIPTOGRAFIA_PATH) { $env:PINPAD_CRIPTOGRAFIA_PATH } else { 'D:\desenvolvimento\chave_des\informacoes_critografia_pinpad.properties' }),
    [string]$PortaPinpad = $env:PORTA_PINPAD,
    [string]$EmulatorSerial = $env:ANDROID_SERIAL,
    [string]$JavaHome = 'D:\desenvolvimento\ferramentas_android\Nova pasta\AndroidStudio\jbr',
    [string]$AndroidSdk = 'D:\desenvolvimento\ferramentas_android\Sdk',
    [switch]$ValidateOnly,
    [switch]$ProvisionOnly
)
$ErrorActionPreference = 'Stop'
$project = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$repo = (Resolve-Path (Join-Path $project '..\..\..\..')).Path
$goModule = Join-Path $repo 'apps\desktop\libpinpadabecsgo'
$package = 'br.com.romulopenha.sistemaprototipoandroid'
$adb = Join-Path $AndroidSdk 'platform-tools\adb.exe'
. (Join-Path $PSScriptRoot 'payment-pin-config.ps1')
$cryptoTemp = $null
$profileTemp = $null
$preparationPhase = 'configuration'

function Assert-Exit([string]$Phase) { if ($LASTEXITCODE -ne 0) { throw "Falha na fase $Phase (exit $LASTEXITCODE)." } }

# Base64 evita normalização CRLF no stdin Windows do ADB; decodifica sob run-as.
function Write-PrivateFile([string]$Source, [string]$Name) {
    if ($Name -notin @('perfil.json','emv.json','criptografia.properties')) { throw 'Nome privado inválido.' }
    $info = New-Object Diagnostics.ProcessStartInfo
    $info.FileName = $adb
    $info.Arguments = '-s "'+$EmulatorSerial+'" shell -T run-as '+$package+" sh -c 'base64 -d > files/saque-cartao/"+$Name+"'"
    $info.UseShellExecute = $false
    $info.CreateNoWindow = $true
    $info.WindowStyle = [Diagnostics.ProcessWindowStyle]::Hidden
    $info.RedirectStandardInput = $true
    $process = New-Object Diagnostics.Process
    $process.StartInfo = $info
    try {
        if (-not $process.Start()) { throw 'Falha ao iniciar provisionamento privado.' }
        $encodedBytes = [Text.Encoding]::ASCII.GetBytes([Convert]::ToBase64String([IO.File]::ReadAllBytes($Source)))
        try { $process.StandardInput.BaseStream.Write($encodedBytes,0,$encodedBytes.Length) }
        finally { $process.StandardInput.BaseStream.Close() }
        $process.WaitForExit()
        if ($process.ExitCode -ne 0) { throw 'Provisionamento privado falhou.' }
    } finally { $process.Dispose() }
}

# Leitura de PBRG limitada; não lê nem monta payload ABECS.
function Read-Exact($Stream, [int]$Length) {
    $buffer = New-Object byte[] $Length
    $offset = 0
    while ($offset -lt $Length) {
        $read = $Stream.Read($buffer, $offset, $Length-$offset)
        if ($read -eq 0) { throw 'Bridge encerrou a conexão.' }
        $offset += $read
    }
    return ,$buffer
}

function Test-BridgePing([int]$Port) {
    $client = New-Object Net.Sockets.TcpClient
    try {
        $connecting = $client.ConnectAsync('127.0.0.1', $Port)
        if (-not $connecting.Wait(1000)) { throw 'Timeout de conexão Bridge.' }
        $stream = $client.GetStream(); $stream.ReadTimeout=1000; $stream.WriteTimeout=1000
        $correlation = [Text.Encoding]::UTF8.GetBytes('launcher-' + [guid]::NewGuid().ToString('N'))
        foreach ($pair in @(@(1,2),@(7,8))) {
            $frame = New-Object byte[] (14 + $correlation.Length)
            [Text.Encoding]::ASCII.GetBytes('PBRG').CopyTo($frame,0)
            $frame[4]=1; $frame[5]=$pair[0]; $frame[9]=$correlation.Length
            $correlation.CopyTo($frame,14); $stream.Write($frame,0,$frame.Length)
            $header = Read-Exact $stream 14
            if ([Text.Encoding]::ASCII.GetString($header,0,4) -ne 'PBRG' -or $header[4] -ne 1 -or $header[5] -ne $pair[1]) { throw 'Prontidão PBRG inválida.' }
            $length = [int]$header[8]*256 + $header[9]
            if ($length -ne $correlation.Length -or @($header[6..7] + $header[10..13] | Where-Object { $_ -ne 0 }).Count -ne 0) { throw 'Prontidão PBRG inválida.' }
            $reply = Read-Exact $stream $length
            if ([Text.Encoding]::UTF8.GetString($reply) -ne [Text.Encoding]::UTF8.GetString($correlation)) { throw 'Correlação PBRG inválida.' }
        }
    } finally { $client.Dispose() }
}

try {
    $crypto = Read-PaymentPinConfiguration $CriptografiaPath
    if (-not $ProvisionOnly -and $PortaPinpad -notmatch '^(?i:COM)[1-9][0-9]{0,3}$') { throw 'Informe PORTA_PINPAD válida, sem COM padrão.' }
    if (-not (Test-Path -LiteralPath $adb)) { throw 'ADB ausente.' }
    if (-not $ProvisionOnly) {
        if (-not (Test-Path -LiteralPath "$JavaHome\bin\java.exe") -or -not (Get-Command go -ErrorAction SilentlyContinue)) { throw 'Toolchain Java/Go incompleta.' }
        $aar=Join-Path $project 'plugin-saque-cartao\libs\libpinpadabecsgo.aar'
        $provenance=Get-Content -Raw -Encoding UTF8 -LiteralPath ([IO.Path]::ChangeExtension($aar,'.provenance.json')) | ConvertFrom-Json
        if ((Get-FileHash -LiteralPath $aar -Algorithm SHA256).Hash.ToLower() -ne $provenance.sha256) { throw 'AAR diverge da proveniência; regenere antes do teste.' }
    }
    if ((Split-Path $TabelaPath -Leaf) -ne 'emv_des.json') { throw 'Selecione exclusivamente a tabela emv_des.json.' }
    $profile = Get-Content -Raw -Encoding UTF8 -LiteralPath $PerfilPath | ConvertFrom-Json
    $required = 'schemaVersion','environment','host','port','timeoutMillis','timeoutSeconds','tableAcquirer','tableVersion','maxBaseCents','gtkDataMethod','gtkKeyIndex','scenario'
    if (@($required | Where-Object { $_ -notin $profile.PSObject.Properties.Name }).Count -ne 0) { throw 'Perfil incompleto.' }
    foreach ($name in @('schemaVersion','port','timeoutMillis','timeoutSeconds','maxBaseCents','gtkKeyIndex')) {
        if ($profile.$name -isnot [int] -and $profile.$name -isnot [long]) { throw 'Perfil exige campos inteiros sem coerção.' }
    }
    if ($profile.schemaVersion -ne 1 -or $profile.environment -ne 'DES' -or $profile.host -notin @('localhost','127.0.0.1','10.0.2.2')) { throw 'Perfil não pertence à homologação local DES.' }
    if ($profile.port -lt 1 -or $profile.port -gt 65535 -or $profile.timeoutMillis -lt 100 -or $profile.timeoutMillis -gt 120000 -or $profile.timeoutSeconds -lt 1 -or $profile.timeoutSeconds -gt 120) { throw 'Prazos/endpoint inválidos.' }
    if ($profile.tableAcquirer -notmatch '^(0[1-9]|[1-9][0-9])$' -or $profile.tableVersion -notmatch '^[A-Za-z0-9]{10}$') { throw 'Identificação da tabela inválida.' }
    if ($profile.gtkDataMethod -isnot [string] -or $profile.gtkDataMethod -notin @('','30') -or $profile.gtkKeyIndex -lt 0 -or $profile.gtkKeyIndex -gt 99 -or $profile.scenario -notin @('APPROVE','DENY','COMMUNICATION_ERROR')) { throw 'Perfil DATA/cenário inválido.' }
    if ($profile.maxBaseCents -lt 1 -or $profile.maxBaseCents -gt 999999999999) { throw 'Parâmetros monetários inválidos.' }
    $table = Get-Item -LiteralPath $TabelaPath
    if ($table.Length -lt 1 -or $table.Length -gt 1048576) { throw 'Tabela ausente ou fora do limite.' }
    $port = [int]$profile.port
    if ($env:PINPAD_BRIDGE_PORT) {
        $port=0
        if (-not [int]::TryParse($env:PINPAD_BRIDGE_PORT,[ref]$port) -or $port -lt 1 -or $port -gt 65535) { throw 'Override PINPAD_BRIDGE_PORT inválido.' }
        $profile.port = $port
    }
    $preparationPhase = 'emulator'
    $devices = & $adb devices; Assert-Exit 'adb devices'
    $online = @($devices | ForEach-Object { if ($_ -match '^(emulator-\d+)\s+device\s*$') { $Matches[1] } })
    if (-not $EmulatorSerial) { if ($online.Count -ne 1) { throw 'Selecione um único emulador online.' }; $EmulatorSerial=$online[0] }
    if ($EmulatorSerial -notin $online) { throw 'Emulador selecionado não está online.' }
    if (-not $ProvisionOnly -and $PortaPinpad -notin [IO.Ports.SerialPort]::GetPortNames()) { throw 'COM física ausente.' }
    if ($ValidateOnly) { Write-Output 'PASS: pré-condições locais; nenhum comando físico ou abertura Android.'; exit 0 }
    $preparationPhase = 'build'
    if (-not $ProvisionOnly) {
        $env:JAVA_HOME=$JavaHome; $env:ANDROID_HOME=$AndroidSdk
        Push-Location $project
        try { & '.\gradlew.bat' :app:assembleDebug :plugin-saque-cartao:assembleDebug --console=plain; Assert-Exit 'build Android' } finally { Pop-Location }
        $build=Join-Path $project 'build\saque-cartao'; New-Item -ItemType Directory -Force $build | Out-Null
        $bridge=Join-Path $build 'libpinpadabecsgo-bridge.exe'
        Push-Location $goModule
        try { & go build -o $bridge './cmd/libpinpadabecsgo-bridge'; Assert-Exit 'build Bridge' } finally { Pop-Location }
        $preparationPhase = 'bridge'
        $probe=New-Object Net.Sockets.TcpListener([Net.IPAddress]::Loopback,$port)
        try { $probe.Start() } finally { $probe.Stop() }
        $stamp=[guid]::NewGuid().ToString('N')
        $env:PORTA_PINPAD=$PortaPinpad; $env:PINPAD_BRIDGE_PORT=[string]$port; $env:PINPAD_BRIDGE_TRANSPORT='physical'
        $env:PINPAD_LOG_FILE=Join-Path $build "bridge-$stamp.jsonl"
        $created=Start-Process -FilePath $bridge -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $build "stdout-$stamp.log") -RedirectStandardError (Join-Path $build "stderr-$stamp.log")
        $processRecord=Join-Path $build "process-$stamp.json"
        @{pid=$created.Id;startTime=$created.StartTime.ToUniversalTime().ToString('o');executable=$bridge;log=$env:PINPAD_LOG_FILE} | ConvertTo-Json | Set-Content -Encoding UTF8 $processRecord
        Write-Output "Bridge criado: PID=$($created.Id); registro=$processRecord"
        $ready=$false; $deadline=[DateTime]::UtcNow.AddSeconds(15)
        while ([DateTime]::UtcNow -lt $deadline -and -not $created.HasExited) {
            try { Test-BridgePing $port; $ready=$true; break } catch { Start-Sleep -Milliseconds 200 }
        }
        if (-not $ready) { throw 'Bridge sem prontidão PBRG/Ping; host não aberto.' }
        & $adb -s $EmulatorSerial reverse "tcp:$port" "tcp:$port"; Assert-Exit 'reverse'
        $reverse=& $adb -s $EmulatorSerial reverse --list; Assert-Exit 'reverse list'
        if (-not ($reverse | Where-Object { $_ -match "tcp:$port\s+tcp:$port(\s|$)" })) { throw 'Reverse não confirmado.' }
        & $adb -s $EmulatorSerial install -r (Join-Path $project 'app\build\outputs\apk\debug\app-debug.apk'); Assert-Exit 'install'
    }
    $preparationPhase = 'provision'
    $build=Join-Path $project 'build\saque-cartao'; New-Item -ItemType Directory -Force $build | Out-Null
    $stamp=[guid]::NewGuid().ToString('N')
    & $adb -s $EmulatorSerial shell run-as $package mkdir -p files/saque-cartao; Assert-Exit 'private directory'
    $profileTemp=Join-Path $build "profile-$stamp.tmp"
    $cryptoTemp=Join-Path $build "crypto-$stamp.tmp"
    [IO.File]::WriteAllText($profileTemp,($profile | ConvertTo-Json), (New-Object Text.UTF8Encoding($false)))
    [IO.File]::WriteAllText($cryptoTemp,$crypto.Snapshot, (New-Object Text.UTF8Encoding($false)))
    try {
        foreach ($item in @(@($profileTemp,'perfil.json'),@($table.FullName,'emv.json'),@($cryptoTemp,'criptografia.properties'))) {
            Write-PrivateFile $item[0] $item[1]
            $remoteHashOutput = & $adb -s $EmulatorSerial shell run-as $package sha256sum ("files/saque-cartao/"+$item[1])
            Assert-Exit 'private hash'
            $remoteHash = (($remoteHashOutput -join "`n") -split '\s+')[0]
            $localHash = (Get-FileHash -LiteralPath $item[0] -Algorithm SHA256).Hash
            if ($remoteHash -notmatch '^[A-Fa-f0-9]{64}$' -or $remoteHash -ine $localHash) { throw 'Hash do arquivo privado diverge da origem.' }
        }
    } finally {
        Remove-Item -LiteralPath $profileTemp -ErrorAction SilentlyContinue
        $profileTemp = $null
        Remove-Item -LiteralPath $cryptoTemp -ErrorAction SilentlyContinue
        $cryptoTemp = $null
    }
    if ($ProvisionOnly) {
        Write-Output "payment_provisioned serial=$EmulatorSerial files=3 hashes=confirmed (nao comprova listener, OPN ou chaves no pinpad)"
    } else {
        $preparationPhase = 'host'
        & $adb -s $EmulatorSerial shell am start -n "$package/.MainActivity"; Assert-Exit 'host start'
        Write-Output 'Host aberto após Ping. Faça login e selecione Outros Serviços > Pagamento com Cartão. Ping não comprova OPN/hardware.'
    }
} catch {
    # Falha de preflight/provisionamento não deixa um snapshot PIN antigo disponível.
    # Só usa o emulador explicitamente selecionado; ValidateOnly nunca modifica o host.
    if (-not $ValidateOnly -and (Test-Path -LiteralPath $adb)) {
        if (-not $EmulatorSerial) {
            $available = @(& $adb devices | ForEach-Object { if ($_ -match '^(emulator-\d+)\s+device\s*$') { $Matches[1] } })
            if ($available.Count -eq 1) { $EmulatorSerial = $available[0] }
        }
        if ($EmulatorSerial -match '^emulator-[0-9]+$') {
            & $adb -s $EmulatorSerial shell run-as $package rm -f files/saque-cartao/criptografia.properties 2>$null | Out-Null
        }
    }
    if ($cryptoTemp -and (Test-Path -LiteralPath $cryptoTemp)) { Remove-Item -LiteralPath $cryptoTemp }
    if ($profileTemp -and (Test-Path -LiteralPath $profileTemp)) { Remove-Item -LiteralPath $profileTemp }
    Write-Error "Preparação interrompida na fase $preparationPhase. Revise fontes DES, seleção do emulador e host debug instalado." -ErrorAction Continue
    exit 1
}
