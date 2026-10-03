[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..\..")).Path
$validator = Join-Path $repoRoot ".agents\skills\android-native-engineering\scripts\validate_android_project.py"
$project = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

if (-not (Test-Path -LiteralPath $validator)) {
    throw "Validador estrutural da Skill Android não encontrado."
}
& python $validator $project
exit $LASTEXITCODE
