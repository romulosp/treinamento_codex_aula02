$caminhoScript = Join-Path (Split-Path -Parent $PSScriptRoot) 'validar-codigo.ps1'

Describe 'validar-codigo.ps1' {
    $conteudo = Get-Content -LiteralPath $caminhoScript -Raw

    It 'possui sintaxe PowerShell válida' {
        $tokens = $null
        $erros = $null
        [System.Management.Automation.Language.Parser]::ParseFile($caminhoScript, [ref]$tokens, [ref]$erros) | Out-Null

        $erros.Count | Should Be 0
    }

    It 'expõe as ações operacionais previstas' {
        $conteudo | Should Match "ValidateSet\('Tudo', 'Subir', 'Analisar', 'Status', 'Parar'\)"
        $conteudo | Should Match 'if \(\[string\]::IsNullOrWhiteSpace\(\$DiretorioProjeto\)\)'
    }

    It 'mantém o fallback de infraestrutura identificável' {
        $conteudo | Should Match 'SONAR_FALLBACK_LLM_REQUIRED:'
        $conteudo | Should Match '\$CodigoFallbackLlm = 20'
    }

    It 'não contém senha JDBC fixa' {
        $conteudo | Should Not Match 'SONAR_JDBC_PASSWORD\s*=\s*sonar'
        $conteudo | Should Match 'Nova-SenhaAleatoria'
    }
}
