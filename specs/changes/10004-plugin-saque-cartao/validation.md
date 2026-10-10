# Validação: 10004-plugin-saque-cartao

**Autor:** Rômulo Penha

## Status

`IMPLEMENTADA` — testes técnicos registrados para avaliação humana, conforme
REV-005/REV-006. Este registro não representa validação formal nem homologação
do pinpad físico.

## Retomada técnica e correção visual — 2026-10-10

A retomada encontrou checkboxes concluídos sem evidências atuais e uma tela
desconectada do repository. Foram implementados proprietário da jornada,
callbacks, integração das APIs filtradas Go, carregamento nativo por ABI no
host, layout dos cinco SVGs Gemini e animações solicitadas. Registros de
hardware/revisão formal voltaram a ficar pendentes em tasks.md.

Ambiente: Windows PowerShell 5.1, Go 1.26.5 windows/386 para testes/Bridge;
Go 1.26.5 windows/amd64 portátil para gomobile, x/mobile
v0.0.0-20260908204917-8b95e45f8d3e, JDK 17.0.11 no AAR, NDK 28.2.13676358.
Gradle Wrapper 9.6.0 com JBR 25.0.3; bytecode Android Java/Kotlin 17.
SDK em `D:\desenvolvimento\ferramentas_android\Sdk`;
Medium_Tablet API 35, emulator-5554, 2560×1600, densidade 320 (1280×800 dp).
Emulador iniciado oculto, sem snapshot e com GPU swiftshader na repetição.

Diretórios: comandos Go em `apps/desktop/libpinpadabecsgo`; Gradle em
`apps/frontend/smartphone/sistema-prototipo-android`; demais na raiz.

| Comando/cenário | Resultado | Exit |
| --- | --- | --- |
| `go test ./...` | PASS em todos os pacotes Go. | 0 |
| `go test ./mobile ./internal/domain/command ./internal/application/service -coverprofile "$env:TEMP/codex-10004-go-coverage.out"` | PASS; mobile total 45,2%, command 90,1%, service 80,0%. Nas três extensões `journey.go` da 10004: 104/122 statements, 85,2%. Código preexistente fora dessas extensões não está declarado coberto pela Change. | 0 |
| `gomobile bind` registrado na proveniência do AAR | PASS; APIs filtradas/carga formatada e quatro ABIs. | 0 |
| `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest` | PASS: 12 testes, incluindo dinheiro, perfil, tarja/EMV, duplicidade, cancelamento, detach, resposta tardia e cleanup. | 0 |
| `./gradlew.bat :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug --console=plain` | PASS; 181/188 linhas elegíveis Android, 96,3%. Lint: zero erros e três avisos (OldTargetApi, GradleDependency, NewerVersionAvailable); stack vigente mantida por SPEC. Host e plugin debug gerados. | 0 |
| `./gradlew.bat :plugin-saque-cartao:connectedDebugAndroidTest` | PASS na repetição: quatro testes antes de acrescentar a verificação específica do spinner. | 0 |
| `./gradlew.bat :app:connectedDebugAndroidTest --console=plain` | PASS: 19 testes; discovery/menu/View e inicialização real de Go via DexClassLoader do processo host, libgojni x86_64 extraída. | 0 |
| `./gradlew.bat :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug --console=plain` | PASS no código visual final (altura de painéis e fixture compacta ajustadas). | 0 |
| Mesmo build após endurecer parser de perfil | PASS; fração/string em campo inteiro é rejeitada sem conversão implícita. | 0 |
| `adb ... am instrument -w -e class br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao.SaqueProfileTest ...` | `OK (1 test)` após endurecimento: taxa decimal válida e rejeição de ausência, PRD, fração e string nos inteiros. | 0 |
| `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao.test/androidx.test.runner.AndroidJUnitRunner` | `OK (5 tests)` no APK final; cinco etapas/transições, callbacks, compacto/fonte 150%, spinner com relógio controlado e parser de perfil sem defaults. | 0 |
| Parser PowerShell dos dois scripts de launcher | PASS, nenhum erro sintático. | 0 |
| Launcher `-ValidateOnly` com COM inválida, PRD e COM9999 ausente, perfil sintético | Três rejeições esperadas, host/Bridge não iniciados; PRD rejeitado pelo nome antes de qualquer leitura. | 1 por cenário (esperado) |
| `go build -o "$env:TEMP/codex-10004-launcher-checks/bridge-scripted.exe" ./cmd/libpinpadabecsgo-bridge` + `Test-BridgePing 39119` | PASS; Bridge scripted, HELLO/HELLO_OK + PING/PONG e correlação conferidos. Sem aquisição serial ou DATA/ABECS. Processo criado encerrado pelo teste. | 0 |
| Validador estrutural Android sobre snapshot dos arquivos versionados | PASS; local.properties local preservado. Execução direta sobre workspace acusa somente local.properties (exit 1), arquivo de SDK ignorado pelo Git; não foi removido nem versionado. | 0 no snapshot |
| Hash dos cinco anexos comparado a `source-material/gemini/` | PASS: os cinco SVGs correspondem byte a byte. | 0 |
| `git -c core.safecrlf=false diff --check` | PASS, sem erro de whitespace. | 0 |

### Artefatos e inspeção visual

[Painel das cinco etapas e adaptação compacta](evidence/layout/cinco-etapas.png).
Capturas individuais em `evidence/layout/layout-10004/`. Captura da referência
SVG em `evidence/layout/referencia-valores.png`, renderizada por Chrome
headless em 1280×800. Inspeção das capturas confirmou cartões, modalidades,
stepper, azul #003366, total alinhado horizontalmente em janela expandida,
chip/tarja/aproximação e pontos/cadeado estáticos. A captura compacta é
realizada após rolar até AVANÇAR e não mostra o topo inteiro.

As transições usam AnimatedContent com snapshot da própria lambda e identidade
da etapa; atualização do formulário não troca a tela. Fade/deslocamento de
280 ms e spinner Material seguem o movimento do sistema. Teste dedicado
avança o relógio em 100/350 ms, verifica mudança visual, preservação do texto
e zero eventos emitidos. Não foi declarada uma auditoria de todas as escalas
de animação do sistema nem validação em API mínimo/alvo.

Logs finais, XML dos testes host e relatórios de cobertura em
`evidence/technical/`. Dados monetários e perfil das capturas são sintéticos.
A execução de instrumentação via ADB preservou capturas após os testes;
Gradle UTP desinstala APKs de teste e remove seus arquivos privados.

AAR: SHA-256 `93545aea6c3e2d6711f78975947795d8cfa6124f697eb673953e79ff52b9d0aa`.
Origem, comando, hashes das fontes de produção e ABIs em
`apps/frontend/smartphone/sistema-prototipo-android/plugin-saque-cartao/libs/libpinpadabecsgo.provenance.json`.
Artefato local dirty da revisão db23e7d; não é release publicado.

### Correções de ambiente/teste nesta retomada

- Primeira instrumentação: emulador encerrou durante o terceiro teste; exit 1.
  Repetição sem snapshot passou; não foi atribuída causa de código sem prova.
- Teste de integração nativo inicial: lista de APKs verificados continha cópias
  do mesmo plugin. A seleção no teste passou a usar `distinctBy(pluginId)`,
  igual ao discovery de produção; os 19 testes passaram na repetição.
- Configuração inicial de cobertura tinha fileTree sobre todo build e gerava
  dependência implícita Gradle. Corrigida para arquivo `.exec` específico;
  relatório e gate passaram. Nenhuma exclusão foi adicionada para elevar índice.
- AAR: Go do PATH era 386. Toolchain amd64 portátil de mesma versão baixada do
  catálogo oficial, SHA-256 conferido; argumentos gomobile montados em array
  PowerShell e javac 17 adicionado ao PATH local. O PATH do sistema não mudou.

### Inventário KDoc e elegibilidade

| Arquivo | Contratos documentados / exclusões |
| --- | --- |
| `PluginSaqueCartaoApp.kt` | Isolamento, criação por View e cleanup no detach; propriedades e lifecycle sem contrato adicional herdam shared-api. |
| `SaqueModels.kt` | Modalidade, cenário, perfil/validação, dinheiro exato, N12, resumo, estado e eventos. |
| `SaqueRepository.kt` | Porta main-safe, arquivos privados debug, operações tipadas, descarte de dados brutos e categorias sanitizadas. Overrides herdam a porta; parser/helper documentados. |
| `SaqueJourney.kt` | Ownership/main, estado seguro, congelamento, sequência, confirmação real, cancelamento, detach, resposta tardia e cleanup limitado. |
| `SaqueCartaoScreen.kt` | UI pura, identidade de transição, stepper, painéis, resumo/resultado real, spinner, ações e ilustrações decorativas sem dados de PIN. Tokens privados triviais excluídos. |
| `BusinessPluginManager.kt` | Extração de nativas somente de APK verificado, ABI, nomes/tamanhos limitados e diretório privado por digest. |

JaCoCo JVM mede SaqueModelsKt, SaquePerfil, SaqueResumo e SaqueJourney (incluindo
coroutines geradas). Compose, integração Android/gomobile, carregador host e
código gerado BuildConfig exigem instrumentação/integração e não são medidos
por esse agente JVM. Enum/eventos/estado são portadores de dados, sem regras
independentes. Go mede as três extensões journey.go; totais dos pacotes também
estão registrados acima. Dokka não está configurado, não foi instalado.

### Limites para avaliação humana

Não houve OPN físico, carga da tabela DES, cartão/PIN ou GOX/FCX em dispositivo
real. Login humano e jornada financeira completa não foram executados.
Perfil runtime aprovado (taxa/limite/filtros/índice DATA/cenário) e PORTA_PINPAD
continuam sem identificação; resposta "sim" não fornece esses parâmetros.
Launcher físico existe, mas provisionamento/reverse completo com esse perfil,
porta ocupada e seleção ADB ambígua ainda precisam de execução de laboratório.
Não há autorização financeira, aprovação formal, archive, atualização system
ou commit nesta entrega para teste.

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


## Correção da primeira tela — REV-007 (2026-10-10)

Estado: IMPLEMENTADA para avaliação humana. Perfil/tabela reais e teste físico
continuam pendentes; os testes abaixo usam dados sintéticos.

Contrato: faixa Voltar ao menu removida do host; Voltar Android retorna ao menu.
SAIR vermelho abaixo de AVANÇAR usa o mesmo SaqueEvent.Return/IPluginRouter,
sem logout. Compra/modalidade/parcelas são editáveis sem perfil; AVANÇAR exige
perfil válido. Taxa percentual = 5,34 + 0,80 × (parcelas - 1), crédito 1..12,
débito uma parcela. Total = compra + tarifa HALF_UP em centavos. `feePercent`
legado não modifica mais a regra. Perfil limita a compra e fornece filtros
crédito/débito, DATA e cenário. Carregar perfil recalcula entradas já digitadas.
Animações existentes e sequência física preservadas.

Ambiente: Windows PowerShell 5.1, Python local, Gradle Wrapper 9.6.0/AGP 9.4,
JBR 25.0.3 (JAVA_HOME Android Studio), Kotlin bytecode Java 17,
emulator-5554/Medium_Tablet API 35, 2560×1600, densidade 320.
Comandos Gradle executados no projeto Android.

| Comando | Resultado | Exit |
| --- | --- | --- |
| `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | Rejeita local.properties ignorado pelo Git, contendo caminho SDK; arquivo local preservado | 1 |
| Mesmo validador em snapshot dos arquivos versionados do projeto, sem configuração local | Invariantes atendidos | 0 |
| `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` | Build OK; 15 testes, zero falhas; cobertura 187/194 linhas = 96,4%; lint plugin zero erros/3 avisos | 0 |
| `./gradlew.bat :app:assembleDebugAndroidTest :app:lintDebug --console=plain` | Primeira compilação do novo teste rejeitou import de método membro assertDoesNotExist; import removido | 1 |
| `./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug --console=plain` | Build final OK; lint host zero erros/8 avisos | 0 |
| `adb -s emulator-5554 install -r <APK>` para plugin, teste do plugin, host e teste do host | Quatro instalações Success | 0 |
| `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao.test/androidx.test.runner.AndroidJUnitRunner` | OK (6 tests) | 0 |
| `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.test/androidx.test.runner.AndroidJUnitRunner` | OK (20 tests), incluindo ausência da faixa e Voltar Android | 0 |
| Parser PowerShell de `scripts/iniciar-saque-cartao.ps1` | Zero erros | 0 |
| `powershell.exe -NoProfile -File scripts/iniciar-saque-cartao.ps1 -PerfilPath <perfil-sintético-sem-feePercent> -TabelaPath <tabela-sintética-emv_des.json> -PortaPinpad <COM-enumerada> -EmulatorSerial emulator-5554 -ValidateOnly` | Pré-condições OK, nenhum comando físico/provisionamento/abertura Android | 0 |
| `adb -s emulator-5554 pull /sdcard/Android/data/br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao/files/layout-10004 specs/changes/10004-plugin-saque-cartao/evidence/layout` | Sete capturas locais | 0 |

Os testes unitários demonstram taxas de todas as 12 parcelas, cálculo sem
perfil, HALF_UP/overflow/limite externo, recálculo após carga e seleção de
modalidade com total N12/filtro correto na captura. O adapter Go continua
passando `profile.applicationTypes(summary.modalidade)` ao GCX filtrado.
Testes UI demonstram crédito/débito selecionáveis, compra digitável sem perfil,
R$ 100,00 + R$ 5,34 = R$ 105,34, AVANÇAR bloqueado e SAIR ativo, além das cinco
etapas, fonte ampliada e spinner sem eventos artificiais.

Capturas atuais em `evidence/layout/layout-10004/`; `01-valores.png` mostra
primeira tela configurada; `07-primeira-tela-sem-perfil.png` mostra edição local.
A montagem `cinco-etapas.png` da revisão anterior permanece histórica.
SHA-256 atualizado em `evidence/capturas-sha256.md`. Logs locais preservados
em `evidence/primeira-tela/`. Binários seguem política de ignore do repositório.

Avisos lint: plugin OldTargetApi, GradleDependency, NewerVersionAvailable;
host OldTargetApi, AndroidGradlePluginVersion, GradleDependency, DiscouragedApi,
DataExtractionRules, MissingApplicationIcon e dois SetTextI18n. Não houve
upgrade de toolchain nem mudança de perfil de segurança nesta correção.
KDoc atualizado para taxa de domínio, cálculo independente do hardware,
parser de perfil e hospedagem/retorno da View. Não há evidência física nova.
