<# .SYNOPSIS Encerra explicitamente somente o PID e a instância registrados pelo launcher. #>
[CmdletBinding()]
param([Parameter(Mandatory=$true)][string]$ProcessRecord)
$ErrorActionPreference='Stop'
$record=Get-Content -Raw -Encoding UTF8 -LiteralPath $ProcessRecord | ConvertFrom-Json
$created=Get-Process -Id $record.pid -ErrorAction SilentlyContinue
if (-not $created) { Write-Output 'A instância registrada já encerrou.'; exit 0 }
if ($created.Path -ne $record.executable -or $created.StartTime.ToUniversalTime().ToString('o') -ne $record.startTime) { throw 'PID reutilizado ou executável divergente; processo preservado.' }
Stop-Process -InputObject $created
Write-Output 'Instância registrada encerrada explicitamente.'
