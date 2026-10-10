# Validação: 10004-plugin-saque-cartao

**Autor:** Rômulo Penha

## Status

`SPEC_APROVADA` — revisão de SPEC em REV-005; implementação e evidências técnicas em andamento.

## Revisão documental de 2026-10-10

Ambiente: workspace Windows, Windows PowerShell, arquivos UTF-8.
Avaliação estrutural/textual de dez SVGs; não foi renderizada UI Android.
Contrato anterior preservado e referências incorporadas com SHA-256.
Diretório de execução: raiz do repositório. Windows PowerShell
`5.1.19041.6456`, em 2026-10-10.

| Comando | Resultado | Código de saída |
| --- | --- | --- |
| `git diff --check` | PASS; sem erro de whitespace; avisos de normalização LF/CRLF. | 0 |
| Verificação PowerShell reproduzível abaixo | PASS: 12 hashes SHA-256 conferidos, 10 SVGs com XML válido e 8 links locais existentes nos documentos revisados. | 0 |

Comando PowerShell executado (quebras de linha adicionadas para leitura):

```powershell
$ErrorActionPreference = 'Stop'
$change = (Resolve-Path 'specs/changes/10004-plugin-saque-cartao').Path
$entries = Get-Content -Raw -Encoding UTF8 -LiteralPath "$change/source-material/sha256.json" | ConvertFrom-Json
foreach ($entry in $entries) {
    $file = Join-Path $change $entry.file
    if ((Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash -ne $entry.sha256) {
        throw "Hash divergente: $($entry.file)"
    }
}
$svgs = Get-ChildItem -LiteralPath "$change/source-material" -Recurse -Filter *.svg
if ($svgs.Count -ne 10) { throw 'Quantidade de SVGs diferente de dez' }
foreach ($svg in $svgs) {
    $xml = [xml](Get-Content -Raw -Encoding UTF8 -LiteralPath $svg.FullName)
    if ($xml.DocumentElement.LocalName -ne 'svg') { throw 'Raiz SVG inválida' }
}
Write-Output "PASS: $($entries.Count) hashes e $($svgs.Count) documentos SVG válidos"
$docs = Get-ChildItem -LiteralPath $change -Filter *.md
$docs += Get-Item -LiteralPath "$change/reviews/2026-10-10-spec-review.md","$change/source-material/README.md"
$count = 0
foreach ($doc in $docs) {
    $content = Get-Content -Raw -Encoding UTF8 -LiteralPath $doc.FullName
    foreach ($match in [regex]::Matches($content, '\]\(([^)]+)\)')) {
        $target = $match.Groups[1].Value
        if ($target -match '^(https?://|#)') { continue }
        $relative = $target.Split('#')[0]
        if (-not (Test-Path -LiteralPath (Join-Path $doc.DirectoryName $relative))) {
            throw "Link ausente em $($doc.Name): $target"
        }
        $count++
    }
}
Write-Output "PASS: $count links locais existentes"
Write-Output $PSVersionTable.PSVersion.ToString()
```

Build, unitários, lint, cobertura, UI e dispositivo da revisão física não
foram executados: não há código novo e existem achados materiais na SPEC.
Nenhuma evidência abaixo de 2026-09-25 valida hardware ou a revisão atual.

### Adendo documental DES — 2026-10-10

Homologação física DES confirmada pelo solicitante. Inspeção somente do
perfil DES: parser JSON estrito PowerShell falhou devido a comentários (exit 1);
análise estrutural Node após remover comentários de linha passou (exit 0).
36 registros, 15 AIDs/21 CAPKs; comprimentos e Latin-1 conferidos.
Comando exato e resultados em [perfil DES](perfil-homologacao-des.md).
Nenhum registro/chave copiado, nenhuma leitura PRD, nenhum comando de hardware.
Revisão atual em [REV-004](reviews/2026-10-10-spec-review-des.md).

Atualização do contrato consumidor: [integração 074](integracao-074.md) registra
as operações existentes e seus argumentos por etapa. Parâmetros confirmados:
DUKPT TDES, método 3, índice 08 (inteiro 8 no binding), workingKey vazia.
`git -c core.safecrlf=false diff --check`: PASS, código 0.
Verificação adicional de links locais do contrato com `Test-Path`: PASS,
código 0. Nenhuma chamada ABECS ou alteração de código de produção nesta etapa.

## Evidências históricas de 2026-09-25

As tabelas e comandos abaixo continuam referentes somente à baseline histórica.

## Ambiente

Windows, JDK 17 em `C:\Desenvolvimento\jdk-17.0.11`; emulador `emulator-5554` (Medium Tablet, API 35), em 2026-09-25.

## Comandos e evidências

| Comando ou cenário | Resultado | Código |
| --- | --- | --- |
| `./gradlew.bat :plugin-saque-cartao:test :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebug` | Teste unitário, lint e APK aprovados. | 0 |
| `./gradlew.bat :app:compileDebugAndroidTestKotlin :app:connectedDebugAndroidTest` | Seis testes instrumentados aprovados; inclui discovery do APK de saque, caminho `Outros Serviços > Saque Cartão` e criação da `View`. | 0 |
| Instalação e abertura de `:app` | A tela de login apareceu antes de qualquer item de negócio. | 0 |
| Login de operador, menu e clique | O menu exibiu `Outros Serviços > Saque Cartão`; o clique abriu a tela "Insira ou aproxime o cartão". | 0 |
| Fluxo manual do saque | Cartão lido → senha local → confirmação → "Transação concluída" → `IPluginRouter` retornou ao menu do host. | 0 |

## Inventário KDoc

- `PluginSaqueCartaoApp`: contrato, isolamento e retorno pelo roteador.
- `SaqueEtapa` e `SaqueCartaoRoute`: estado local e descarte da senha.
- Não existe Activity launcher em nenhuma variante do plugin.
