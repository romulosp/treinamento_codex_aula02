<#
.SYNOPSIS
Prepara reverse; opcionalmente provisiona as três fontes DES no host debug instalado.
.DESCRIPTION
Nao instala APK, nao inicia/encerra Bridge/Activity e nao modifica outros reverses.
ProvisionarPagamentoCartao usa o launcher de arquivos e exige host debug instalado.
ADB ausente ou selecao ambigua devolve codigo 1; o BAT ainda pode iniciar o host.
.PARAMETER EmulatorSerial
Serial online explicito; por padrao usa ANDROID_SERIAL ou unico emulador online.
.PARAMETER BridgePort
Porta TCP; por padrao PINPAD_BRIDGE_PORT ou 39100.
.PARAMETER ProvisionarPagamentoCartao
Provisiona perfil/tabela/PIN DES com hashes; sem a opcao, prepara somente reverse.
#>
[CmdletBinding()]
param(
    [string]$EmulatorSerial = $env:ANDROID_SERIAL,
    [string]$BridgePort = $env:PINPAD_BRIDGE_PORT,
    [switch]$ProvisionarPagamentoCartao
)
$ErrorActionPreference = 'Stop'
try {
    if ([string]::IsNullOrWhiteSpace($BridgePort)) { $BridgePort = '39100' }
    $portNumber = 0
    if (-not [int]::TryParse($BridgePort, [ref]$portNumber) -or $portNumber -lt 1 -or $portNumber -gt 65535) { throw 'Porta TCP invalida.' }
    $adbCommand = Get-Command adb -ErrorAction SilentlyContinue
    $adbPath = if ($adbCommand) { $adbCommand.Source } else { $null }
    if (-not $adbPath) {
        foreach ($sdkRoot in @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT)) {
            if (-not [string]::IsNullOrWhiteSpace($sdkRoot)) {
                $candidate = Join-Path $sdkRoot 'platform-tools\adb.exe'
                if (Test-Path -LiteralPath $candidate) { $adbPath = $candidate; break }
            }
        }
    }
    if (-not $adbPath) { throw 'ADB ausente. Configure PATH ou ANDROID_HOME.' }
    $deviceOutput = & $adbPath devices
    if ($LASTEXITCODE -ne 0) { throw 'ADB devices falhou.' }
    $online = @($deviceOutput | ForEach-Object { if ($_ -match '^(emulator-\d+)\s+device\s*$') { $Matches[1] } })
    if ([string]::IsNullOrWhiteSpace($EmulatorSerial)) {
        if ($online.Count -ne 1) { throw 'Selecione emulador online com ANDROID_SERIAL ou -EmulatorSerial.' }
        $EmulatorSerial = $online[0]
    }
    if ($online -notcontains $EmulatorSerial) { throw 'Emulador selecionado nao esta online.' }
    $mapping = "tcp:$portNumber"
    & $adbPath -s $EmulatorSerial reverse $mapping $mapping
    if ($LASTEXITCODE -ne 0) { throw 'ADB reverse falhou.' }
    $reverseOutput = & $adbPath -s $EmulatorSerial reverse --list
    if ($LASTEXITCODE -ne 0 -or -not ($reverseOutput | Where-Object { $_ -match "(^|\s)$mapping\s+$mapping(\s|$)" })) { throw 'Mapeamento reverse nao confirmado.' }
    Write-Host "emulator_prepared serial=$EmulatorSerial port=$portNumber (nao comprova listener ou OPN)"
    if ($ProvisionarPagamentoCartao) {
        $launcher = Join-Path $PSScriptRoot '..\..\frontend\smartphone\sistema-prototipo-android\scripts\iniciar-saque-cartao.ps1'
        if (-not (Test-Path -LiteralPath $launcher)) { throw 'Launcher de provisionamento ausente.' }
        $sdkRoot = Split-Path -Parent (Split-Path -Parent $adbPath)
        $env:PINPAD_BRIDGE_PORT = [string]$portNumber
        & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $launcher -ProvisionOnly -EmulatorSerial $EmulatorSerial -AndroidSdk $sdkRoot
        if ($LASTEXITCODE -ne 0) { throw 'Reverse pronto; pagamento nao provisionado. Revise fontes DES e host debug instalado.' }
    }
    exit 0
} catch {
    Write-Host "emulator_not_prepared: $($_.Exception.Message)"
    Write-Host 'Correcao: preparar_emulador.ps1 -EmulatorSerial emulator-5554 -BridgePort 39100 -ProvisionarPagamentoCartao'
    exit 1
}
