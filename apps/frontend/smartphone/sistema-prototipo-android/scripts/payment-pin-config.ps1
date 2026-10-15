# Parser delimitado do perfil PIN DES. Não executa expressões nem ecoa valores inválidos.
function Read-PaymentPinConfiguration([string]$Path) {
    try {
        $file = Get-Item -LiteralPath $Path
        if ($file.PSIsContainer -or $file.Length -lt 1 -or $file.Length -gt 65536) { throw 'Limite inválido.' }
        $bytes = New-Object byte[] 65537
        $length = 0
        $stream = [IO.File]::Open($file.FullName,[IO.FileMode]::Open,[IO.FileAccess]::Read,[IO.FileShare]::Read)
        try {
            while ($length -lt $bytes.Length) {
                $read = $stream.Read($bytes,$length,$bytes.Length-$length)
                if ($read -eq 0) { break }
                $length += $read
            }
        } finally { $stream.Dispose() }
        if ($length -lt 1 -or $length -gt 65536) { throw 'Limite inválido.' }
        $utf8 = New-Object Text.UTF8Encoding($false, $true)
        $raw = $utf8.GetString($bytes,0,$length)
        if ($raw.StartsWith([string][char]0xFEFF,[StringComparison]::Ordinal)) { $raw = $raw.Substring(1) }
        $fields = New-Object 'Collections.Generic.Dictionary[string,string]' ([StringComparer]::Ordinal)
        foreach ($line in ($raw -split "`n")) {
            $trimmed = $line.Trim()
            if (-not $trimmed -or $trimmed.StartsWith('#') -or $trimmed.StartsWith('!')) { continue }
            $separator = $trimmed.IndexOfAny([char[]]'=:')
            if ($separator -lt 1) { throw 'Sintaxe inválida.' }
            $key = $trimmed.Substring(0,$separator).Trim()
            $value = $trimmed.Substring($separator+1).Trim()
            if ($key -cnotin @('TIPO_CHAVE','INDICE_CHAVE','WORKING_KEY') -or $fields.ContainsKey($key)) { throw 'Campo inválido.' }
            $fields.Add($key,$value)
        }
        if ($fields.Count -ne 3 -or $fields['TIPO_CHAVE'] -cne '3' -or $fields['INDICE_CHAVE'] -cnotmatch '^[0-9]{2}$') { throw 'Perfil inválido.' }
        $wk = $fields['WORKING_KEY']
        if ($wk -ceq '""') { $wk = '' }
        if ($wk -cne '') { throw 'WK incompatível.' }
        # A origem inteira foi validada; snapshot canônico mantém todos os campos.
        return [pscustomobject]@{
            Method = $fields['TIPO_CHAVE']
            KeyIndex = [int]$fields['INDICE_CHAVE']
            WorkingKey = $wk
            Snapshot = "TIPO_CHAVE=$($fields['TIPO_CHAVE'])`nINDICE_CHAVE=$($fields['INDICE_CHAVE'])`nWORKING_KEY=`n"
        }
    } catch {
        throw 'Configuração criptográfica PIN ausente ou inválida.'
    }
}
