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

## Pagamento com Cartão e usabilidade — REV-008 (2026-10-10)

Estado: IMPLEMENTADA para avaliação humana. O teste físico do pinpad continua
pendente; todos os resultados abaixo usam dados sintéticos e perfil DES sintético.

Contrato entregue: nome de exibição, item e caminho de menu, rótulo do APK,
`plugin-manifest.json` e log do launcher passam a "Pagamento com Cartão"
(`Principal > Outros Serviços > Pagamento com Cartão`, `itemId`
`principal-outros-serviços-pagamento-com-cartão`). O valor da compra é digitado
em centavos e formatado dinamicamente (`1` → `R$ 0,01`, `100` → `R$ 1,00`,
`10534` → `R$ 105,34`) com teclado numérico. O parcelamento é combobox de 1x a
12x no crédito e fica fixo em 1x (à vista) no débito. Tarifa: crédito
`5,34% + 0,80 × (parcelas − 1)`; débito `R$ 1,00` fixo. Total a pagar = compra +
tarifa, HALF_UP em centavos. AVANÇAR habilita com `summary != null && !busy`.
Sem arquivo de perfil, a jornada adota `defaultDesProfile()` (DES,
`10.0.2.2:39100`, limite sintético de R$ 5.000,00) e informa "Perfil DES padrão
em uso...". Perfil externo presente e inválido devolve `CONFIGURATION`, não é
substituído e mantém o início físico bloqueado com mensagem explícita.
Transições de etapa/painel e a entrada do grupo AVANÇAR/SAIR combinam
deslocamento, desvanecimento e escala.

Escopo preservado como identificador: módulo `:plugin-saque-cartao`, diretório,
pacote `...pluginsaquecartao`, `applicationId`, `pluginId`, classes `Saque*`,
diretório privado `files/saque-cartao` e o id da Change 10004. A renomeação cobre
textos de interface, descritores publicados, logs e a documentação vigente
(`proposal.md` e `spec.md` inclusive); registros históricos (REV-005/006/007, a
evidência de 2026-09-25 e a baseline) não foram reescritos.

Ambiente: Windows PowerShell 5.1, Gradle Wrapper 9.6.0/AGP 9.4, JBR 25.0.3 em
`D:\desenvolvimento\ferramentas_android\Nova pasta\AndroidStudio\jbr`, Kotlin com
bytecode Java 17, SDK em `D:\desenvolvimento\ferramentas_android\Sdk`,
emulator-5554/Medium_Tablet API 35, 2560×1600, densidade 320. Diretório dos
comandos Gradle: `apps/frontend/smartphone/sistema-prototipo-android`.
Logs desta etapa em `evidence/rev-008/`.

| Comando | Resultado | Exit |
| --- | --- | --- |
| `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:saqueCoverageVerify --console=plain` | PASS: 18 testes (11 jornada, 6 modelos, 1 contrato), zero falhas; cobertura de linhas 212/224 = 94,6% (gate 80%). Instruções 87,4% e branches 79,0%. | 0 |
| `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` | PASS (`BUILD SUCCESSFUL`); lint do plugin com zero erros e três avisos (OldTargetApi, GradleDependency, NewerVersionAvailable); APKs debug de plugin, host e testes gerados. | 0 |
| `adb -s emulator-5554 install -r` para plugin, teste do plugin, host e teste do host | Quatro instalações `Success`. | 0 |
| `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao.test/androidx.test.runner.AndroidJUnitRunner` | `OK (8 tests)`: combobox de crédito e débito fixo, entrada monetária, tarifa fixa, AVANÇAR/SAIR, animação do grupo de ações, cinco etapas, spinner e parser de perfil. | 0 |
| `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.test/androidx.test.runner.AndroidJUnitRunner` | `OK (20 tests)`, incluindo discovery do APK real com o novo título e `itemId`, ausência da faixa e retorno pelo Voltar Android. | 0 |
| `adb -s emulator-5554 pull /sdcard/Android/data/.../files/layout-10004 evidence/layout` | Sete capturas atuais; conjunto REV-007 preservado em `evidence/rev-007/layout-10004/`. | 0 |
| Conferência SHA-256 das 9 capturas atuais e das 7 preservadas | PASS: hashes atuais conferem com `evidence/capturas-sha256.md` e o conjunto REV-007 confere byte a byte com o registro anterior. | 0 |
| `git -c core.safecrlf=false diff --check` | PASS, sem erro de whitespace. | 0 |
| `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | Rejeita apenas `local.properties` local (caminho do SDK, ignorado pelo Git); arquivo preservado sem versionamento. | 1 |
| Mesmo validador sobre snapshot dos arquivos versionados (sem `build/`, `.gradle/` e `local.properties`) | `OK: invariantes estruturais Android atendidos`, 73 arquivos. | 0 |

Uma primeira execução da instrumentação do plugin falhou em `assertExists` porque
`R$ 1,00` aparece duas vezes (rótulo TAXA APLICADA e valor "Taxa aplicada" do
resumo); a asserção passou a exigir exatamente dois nós. Nenhuma linha de
produção foi alterada para contornar teste.

### Inventário KDoc REV-008

| Arquivo | Contratos atualizados |
| --- | --- |
| `SaqueModels.kt` | Dígitos monetários, tarifa fixa do débito, rótulo da taxa, cálculo por centavos e perfil DES padrão. |
| `SaqueJourney.kt` | Ausência de arquivo adota o perfil padrão; arquivo inválido bloqueia o início com mensagem explícita. |
| `SaqueRepository.kt` | Porta distingue `MISSING` de `CONFIGURATION` na leitura do perfil privado. |
| `SaqueCartaoScreen.kt` | Entrada monetária, combobox de parcelas, habilitação do avanço e movimento do grupo de ações. |
| `PluginSaqueCartaoApp.kt` | Nome de exibição, item e caminho de menu renomeados. |

Limitações: sem OPN físico, cartão, PIN ou autorização financeira. O perfil DES
padrão é sintético e não substitui o perfil do laboratório; com ele, AVANÇAR
tenta o Bridge local e falha de forma visível quando o Bridge não está ativo.
A cobertura JVM não mede Compose nem o adapter Android/gomobile. As capturas
REV-007 mais antigas da montagem `cinco-etapas.png` continuam históricas da
REV-006. Não há aprovação formal, atualização de `specs/system/`, archive ou
commit nesta etapa.

## Remoção do indicador de etapas — REV-009 (2026-10-10)

Estado: IMPLEMENTADA para avaliação humana. O teste físico do pinpad continua
pendente; os resultados abaixo usam dados sintéticos e perfil DES sintético.

Contrato entregue: o indicador não interativo de cinco etapas (círculos `1..5`,
rótulos e o texto "Etapa X de 5") foi removido de todas as telas do plugin,
conforme [REV-009](reviews/2026-10-10-spec-review-remocao-indicador.md) e
solicitação direta do solicitante sobre a captura hospedada no host. Motivo: sob
o cabeçalho global, o elemento consumia altura e forçava rolagem já na primeira
tela. Não foi criado indicador substituto; a etapa atual é identificada pelo
conteúdo do painel e pela transição animada. Painéis, cartão central,
ilustrações, total destacado, cores, ações sólidas e movimento permanecem como na
REV-006. O estado `completedSteps` foi removido do estado público e do
proprietário da jornada por não ter mais consumidor na UI.

Ambiente: o mesmo da REV-008 (Windows PowerShell 5.1, Gradle Wrapper 9.6.0/AGP 9.4,
JBR 25.0.3, Kotlin com bytecode Java 17, emulator-5554/Medium_Tablet API 35,
2560×1600, densidade 320). Logs desta etapa em `evidence/rev-009/`.

| Comando | Resultado | Exit |
| --- | --- | --- |
| `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` | PASS (`BUILD SUCCESSFUL`); 18 testes unitários sem falha; cobertura de linhas 210/222 = 94,6% (gate 80%), instruções 88,6% e branches 79,9%; lint do plugin com zero erros e três avisos. | 0 |
| `adb -s emulator-5554 install -r` para plugin, teste do plugin, host e teste do host | Quatro instalações `Success`. | 0 |
| `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao.test/androidx.test.runner.AndroidJUnitRunner` | `OK (9 tests)`: inclui ausência do indicador nas cinco telas e ausência de rolagem na primeira tela. | 0 |
| `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.test/androidx.test.runner.AndroidJUnitRunner` | `OK (20 tests)`, sem regressão no host. | 0 |
| `adb -s emulator-5554 pull /sdcard/Android/data/.../files/layout-10004 evidence/layout` | Sete capturas atuais; conjunto REV-008 preservado em `evidence/rev-008/layout-10004/`. | 0 |
| Conferência SHA-256 do conjunto atual e dos conjuntos REV-007/REV-008 preservados | PASS: os 21 hashes registrados em `evidence/capturas-sha256.md` conferem. | 0 |
| `git -c core.safecrlf=false diff --check` | PASS, sem erro de whitespace. | 0 |

O teste `primeiraTelaNaoExigeRolagemParaAsAcoesEssenciais` compara os limites de
`AVANÇAR` e `SAIR` com a altura da janela raiz (`getUnclippedBoundsInRoot`), além
de exigir ambos visíveis sem gesto de rolagem. A captura
[01-valores](evidence/layout/layout-10004/01-valores.png) confirma os dois botões
inteiros na janela expandida.

Limitações: a remoção do indicador elimina o contador textual de progresso da
jornada; o operador acompanha a fase pelo conteúdo do painel e pelo resultado.
Esse ponto foi verificado apenas com dados sintéticos; a avaliação com pinpad
físico continua pendente. Sem OPN físico, cartão, PIN ou autorização financeira.
Não há aprovação formal, atualização de `specs/system/`, archive ou commit nesta
etapa.

## Integração física no padrão 075 — REV-010 (2026-10-10)

Estado: IMPLEMENTADA para avaliação humana. A jornada com pinpad físico volta a
ser o próximo teste; os resultados abaixo são técnicos e não substituem hardware.

### Causa apurada do erro na etapa 2

Com o Bridge reativado pelo solicitante, a verificação do ambiente encontrou
`libpinpadabecsgo-bridge` em execução (PID 2544) escutando em `127.0.0.1:39100`
e **nenhum arquivo provisionado** em `files/saque-cartao` do app host. Como a
REV-008 liberava AVANÇAR com perfil DES sintético sem tabela EMV, a jornada
abria sessão e falhava em seguida na carga da tabela, exibindo apenas
"Erro na homologação". A verificação confirmou ainda que o modo `scripted` do
Bridge cobre apenas `OPN/GIX/DSP/CLO`, não servindo para provar compra sem
hardware — razão pela qual a validação end-to-end permanece no laboratório.

A tabela do laboratório `D:\desenvolvimento\chave_des\emv_des.json`
(SHA-256 `873d94fa1ec3be17d5a58d130f1ab07b82367eee01c05b4745218c14ebad0ab2`)
foi inspecionada localmente, sem copiar registros: 36 registros (15 TAB_ID 1 e
21 TAB_ID 2), um único adquirente, todos os comprimentos `TAB_LEN` coerentes com
o corpo. A versão A10 não consta da tabela e continua vindo do perfil do
laboratório.

### Contrato entregue

Perfil e tabela passam a ser obrigatórios para AVANÇAR; o perfil DES sintético e
o estado `defaultProfileInUse` foram removidos. A leitura privada distingue
`MISSING` (perfil ausente), `TABLE_MISSING` (tabela ausente) e `CONFIGURATION`
(conteúdo inválido), cada um com mensagem própria. Falhas expõem código e fase
sanitizados no padrão do diagnóstico 075 (`Mobile.errorCode`/`errorPhase`), com
os códigos locais `MISSING`, `TABLE_MISSING`, `CONFIGURATION` e
`CONFIRMATION_ERROR`; nenhum texto livre do driver chega à tela. A confirmação no
pinpad usa `DSP032` (duas linhas S16) e `GKY` em quatro páginas — valor total,
parcelas, tarifa e confirmação/cancelamento — conforme seções 3.3.5 e 3.3.10 do
manual ABECS 2.12; OK avança, CANCELA cancela e outra tecla encerra com
`CONFIRMATION_ERROR`, sem truncamento silencioso. O caminho DEX deixou de ser
usado nesta etapa. GCX filtrado pela modalidade (AID base/estendido e fallback
preservados no core), GTK resumido, GPN DUKPT método 3/índice 8 com PAN vazio na
tarja, GOX no ICC/CTLS EMV e FCX por cenário permanecem com os contratos vigentes.

### Ambiente e comandos

Windows PowerShell 5.1, Gradle Wrapper 9.6.0/AGP 9.4, JBR 25.0.3, Kotlin com
bytecode Java 17, emulator-5554/Medium_Tablet API 35, Bridge Go ativo em
`127.0.0.1:39100`. Diretório dos comandos Gradle:
`apps/frontend/smartphone/sistema-prototipo-android`. Logs em `evidence/rev-010/`.

| Comando | Resultado | Exit |
| --- | --- | --- |
| `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` | PASS (`BUILD SUCCESSFUL`); 18 testes unitários sem falha; cobertura de linhas 210/223 = 94,2% (gate 80%), instruções 87,4% e branches 79,3%; lint do plugin com zero erros e três avisos. | 0 |
| `adb -s emulator-5554 install -r` para plugin, teste do plugin, host e teste do host | Quatro instalações `Success`. | 0 |
| `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao.test/androidx.test.runner.AndroidJUnitRunner` | `OK (11 tests)`: quatro páginas DSP por GKY, bloqueio sem configuração, código da falha exibido, combobox, monetário, tarifa fixa, animação e spinner. | 0 |
| `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.test/androidx.test.runner.AndroidJUnitRunner` | `OK (20 tests)`, sem regressão no host. | 0 |
| Inspeção local da tabela DES do laboratório (Python, sem copiar registros) | PASS: 36 registros, um adquirente, `TAB_LEN` coerente em 36/36, TAB_ID 1/2 conforme o contrato. | 0 |
| `go test ./internal/application/service ./internal/domain/command ./mobile` | PASS nos três pacotes, incluindo `TestFilteredWithdrawalPreservesCandidatesAcrossFallback` (GCX filtrado preserva candidatos no fallback), `TestPurchaseGCXAppliesContactlessFallback`, `TestFilteredGCXEncodesWithdrawalAndCandidates` e `TestTableBuildersMatchABECS212`. | 0 |
| `git -c core.safecrlf=false diff --check` | PASS, sem erro de whitespace. | 0 |

Os testes de jornada provam: bloqueio do início nos três códigos de
provisionamento com o core intocado; quatro páginas DSP (rótulos e valores
exatos) com quatro GKY em display pequeno e amplo; cancelamento na primeira
tecla sem capturar cartão; propagação de `CANCELED` e `CONFIRMATION_ERROR` ao
estado. Os testes de UI provam AVANÇAR desabilitado sem configuração e a
exibição de `Falha: <código> · fase <fase>`.

### Pendências e limites

A jornada humana com pinpad físico e o perfil/tabela do laboratório precisa ser
repetida: o app agora recusa avançar sem provisionamento e mostra o código real
da falha. O perfil aprovado (com `tableAcquirer` e `tableVersion` do laboratório)
não está no repositório e deve ser informado ao launcher. Nenhuma chave, PAN,
trilha, PIN block ou registro EMV foi copiado para a Change, para os testes ou
para o APK. Sem OPN físico, cartão, PIN ou autorização financeira; sem aprovação
formal, atualização de `specs/system/`, archive ou commit nesta etapa.


## Revisão das últimas alterações — 2026-10-11

Revisão concluída como REPROVADA; achados e comandos em
[relatório de implementação](reviews/2026-10-11-implementation-review.md).
Nova execução: 18 unitários PASS, 210/223 linhas (94,2%), lint zero erros/3 avisos,
build PASS e instrumentação OK (11 tests), todos exit 0. Validador estrutural
rejeitou local.properties ignorado (exit 1); arquivo preservado.
Reverse e listener conferidos; probe PBRG local hello/ping PASS. Diretório privado
saque-cartao ausente. Tabela DES indicada tem 36 registros e hash igual ao já
registrado. PORTA_PINPAD será consumida do ambiente. Sem provisionamento/OPN.
Logs locais em evidence/review-20261011/. Não reexecutados os 20 testes do host.


### Arquivo de parâmetros criptográficos indicado em 2026-10-11

Inspeção somente leitura de `D:\desenvolvimento\chave_des\informacoes_critografia_pinpad.properties`,
Python, exit 0. Arquivo com 3 propriedades: TIPO_CHAVE, INDICE_CHAVE e
WORKING_KEY. Referências não secretas conferidas: método 3, índice 08,
working key vazia, coerentes com PIN já aprovado. Não há versão da tabela,
índice DATA separado ou cenário FCX. O nome genérico INDICE_CHAVE não resolve
por si só se 08 também identifica DATA; confirmação solicitada. Arquivo bruto
não copiado nem valores criptográficos publicados. Perfil não gerado e nenhum
provisionamento/OPN/GTK/GOX/FCX realizado; PORTA_PINPAD permanece fonte da COM.

## Entrega das correções — REV-011/012 — 2026-10-11

Estado IMPLEMENTADA, entregue para nova revisão formal. A reprovação anterior
permanece histórica; estes testes técnicos não aprovam a própria entrega nem
comprovam hardware. Correções autorizadas por REV-011/012 antes do código:
validação integral local Go antes de configReady; entrada monetária inválida
preservada e bloqueada; contrato/KDoc de tarifa corrigidos; modalidade da tela
gera APPTYPE 01/02 sem filtros exigidos no perfil externo.

Ambiente: Windows/PowerShell, Go 1.26.5 windows/386 para testes; Go 1.26.5
windows/amd64 portátil para gomobile, Java 17.0.11 no bind, NDK 28.2.13676358,
Android API 29 no AAR. Gradle executado com JBR 25.0.3, compilação alvo Java 17;
SDK local D:/desenvolvimento/ferramentas_android/Sdk; emulator-5554.

| Diretório / comando | Resultado | Exit |
| --- | --- | ---: |
| Módulo Go: `go test ./mobile ./internal/application/service ./internal/domain/command` | PASS; inclui validador local sem Client e rejeições de tabela. | 0 |
| Módulo Go: `go test ./internal/domain/command -run 'TestFilteredGCX' -count=1` | PASS; campos GCX conferidos byte a byte após consulta do manual. | 0 |
| Projeto Android: `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` | BUILD SUCCESSFUL; 19 unitários; 214/227 linhas = 94,3%; lint 0 erros e 3 avisos preexistentes. | 0 |
| `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao.test/androidx.test.runner.AndroidJUnitRunner` | OK (13 tests), 112,863s; inclui preflight pela função real Go, entrada inválida na UI e modalidade sem filtros externos. | 0 |
| Projeto Android: `./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` após AAR com metadados finais | BUILD SUCCESSFUL; host atualizado instalado no emulador. | 0 |
| `adb -s emulator-5554 shell am instrument -w -e class br.com.romulopenha.sistemaprototipoandroid.platform.BusinessPluginManagerTest br.com.romulopenha.sistemaprototipoandroid.test/androidx.test.runner.AndroidJUnitRunner` | OK (1 test), 3,69s; APK dinâmico, View, inicialização nativa e exportação do validador real no AAR. Não reexecutados os 20 do host. | 0 |
| Raiz: `python .agents/skills/android-native-engineering/scripts/validate_android_project.py <cópia temporária dos arquivos versionados do projeto Android>` | PASS sem local.properties; arquivo original preservado. Execução direta anterior continua registrada como exit 1. | 0 |
| Parser PowerShell: ParseFile de scripts/iniciar-saque-cartao.ps1 | Zero erros de sintaxe; não executa launcher físico. | 0 |
| `go run <TEMP>/codex-10004-offline-<guid>.go` | 36 registros DES reais aceitos pelo validador local; sem Client/rede/hardware. A10 TEST000001 usado exclusivamente nessa verificação offline, não como versão aprovada do laboratório. | 0 |

O teste instrumentado de preflight utiliza arquivos sintéticos no Context do
plugin isolado, com backup/restauração, sem provisionar o armazenamento do host.
Nenhum registro real, chave ou dado de cartão foi incluído em fixtures/APK/logs.

AAR final: quatro ABIs (arm64-v8a, armeabi-v7a, x86, x86_64); versão 1.1.0,
revision 131a0af, dirty=true. SHA-256:
`02a8c356b979a838a39f40271bd0df55feeb2a098eae9d0c4be2e5eb3800a9be`.
Proveniência inclui comando gomobile, toolchain e hashes atuais das fontes Go.
Logs REV-012 e proveniência copiados para `evidence/rev-011-012/` (artefatos locais).

### Referências e pendência física

Consulta direta do PDF local, pypdf, exit 0: SPE_APPTYPE na página 35 e seções
3.7.1/6.9.1 (páginas 129/243). Valores 01/02 confirmados pelo solicitante e
coerentes com a tabela DES. [Mapeamento para a stack 074](integracao-074.md)
atualizado após a orientação de reutilizar seus parâmetros/contratos aprovados.

Listener, reverse e Bridge preservados. Não reiniciado Bridge nem realizado OPN
físico. Perfil não provisionado: versão A10 da tabela externa, limite operacional
e cenário FCX da jornada não foram encontrados na SPEC 074 nem nos dois arquivos
externos recebidos. Timeout serial normativo 30s e timeout de cliente do
diagnóstico 10000 ms identificados, distinguindo os prazos explícitos de comandos
bloqueantes. DATA 08 permanece indicação provisória do solicitante. Modelo em
[perfil-laboratorio-pendente.md](perfil-laboratorio-pendente.md). Revisão formal,
teste humano físico, aprovação, archive e commit continuam pendentes.

## Correção de MISSING e provisionamento — REV-013/014 — 2026-10-11

Relato humano: tela inicia com MISSING/preflight e AVANÇAR bloqueado.
`adb -s emulator-5554 shell run-as br.com.romulopenha.sistemaprototipoandroid ls -l files/saque-cartao`
confirmou diretório ausente, exit 1. Não houve crash evidenciado pela captura.

Correção do launcher: aspas remotas preservadas, Base64 em memória enviado
via `adb shell -T`, decodificação direta sob run-as no destino privado e
hash obrigatório antes de abertura do host. Evita redirecionamento sob a shell
externa e normalização CRLF no stdin Windows. Reprodução inicial de gravação
falhou; tentativas sem preservação exata foram recusadas pelo hash. Versão final
preserva bytes dos dois arquivos. Nenhuma alteração Kotlin/Go nesta correção;
testes Android anteriores não repetidos, pois a correção é no provisionamento.

Ambiente Windows/PowerShell e emulator-5554, mesmos SDK/Go da seção anterior.

| Comando/procedimento | Resultado | Exit |
| --- | --- | ---: |
| ParseFile do launcher via System.Management.Automation.Language.Parser | Zero erros; função Write-PrivateFile obtida do AST do próprio script para execução isolada. | 0 |
| Função Write-PrivateFile para perfil.json e emv.json; `adb ... run-as ... sha256sum files/saque-cartao/<arquivo>` comparado a Get-FileHash | PASS para ambos; bytes idênticos à origem; perfil 346 bytes, tabela 17773 bytes. | 0 |
| `scripts/iniciar-saque-cartao.ps1 -PerfilPath D:/desenvolvimento/chave_des/perfil_saque_cartao_des.json -TabelaPath D:/desenvolvimento/chave_des/emv_des.json -ValidateOnly` | PASS; sem iniciar/reiniciar Bridge ou executar comandos físicos. | 0 |
| `go run <TEMP>/codex-10004-provisionamento/validar-candidato.go <perfil> <tabela>` no módulo Go | PASS para tabela real, 36 registros, adquirente 04, versão derivada DES873D94F; nenhuma chamada de Client/I/O de pinpad. | 0 |
| `Get-NetTCPConnection -State Listen -LocalPort 39100` | 127.0.0.1:39100, PID 2544 preservado. | 0 |
| `adb ... uiautomator dump` e pull do XML | Tela atual do emulador: login. A reentrada no plugin após autenticação humana ainda não foi observada. | 0 |

O adquirente 01 anotado anteriormente em integracao-074/perfil pendente estava
incorreto. Conferência direta: TAB_ACQ no offset 4 dos 36 registros = 04;
T1_APPTYPE no offset 42 dos registros AID = 01 (8) / 02 (7). Referências
documentais corrigidas. Hash da tabela permanece
873d94fa1ec3be17d5a58d130f1ab07b82367eee01c05b4745218c14ebad0ab2.
Hash do perfil provisionado:
54ec1a3113c3d604c0343236968a6ef212a2d7ce798836e034895e0d106bef12.

Solicitante aceitou proposta do perfil e escolheu APPROVE para simulação local
até a futura integração com servidor. Arquivo reutilizável:
`D:\desenvolvimento\chave_des\perfil_saque_cartao_des.json`; parâmetros e origem
em [perfil efetivo](perfil-laboratorio-pendente.md) e REV-014. Não há obrigação
de fornecer outro perfil para esta execução. PIN/chave instalada, reentrada da
tela e sequência EMV física continuam sem comprovação; nenhum OPN/GCX/GOX/FCX
foi executado nesta preparação. Teste humano pode agora usar o provisionamento.

## Inicialização EMV exclusiva do plugin — REV-015 — 2026-10-11

SPEC_APROVADA pela orientação explícita do solicitante. Estado IMPLEMENTADA,
entregue para revisão formal/teste humano. Não altera código da infraestrutura
do host, carregador genérico, Go, Bridge ou outros APKs. Alterações desta revisão
limitadas ao plugin Pagamento com Cartão, testes do plugin e documentos da Change.

Attach da View inicia uma preparação única por proprietário, antes do formulário:
validar perfil/tabela → Ping/Open → capacidades → carga EMV real confirmada.
Espera técnica na etapa 0 exibe Aguarde, processando e spinner Compose. Falha
mantém formulário ausente e disponibiliza SAIR. configReady só é publicado após
carga concluída; sessão/display ficam no proprietário. Begin reutiliza ambos e
inicia a confirmação DSP/GKY existente sem repetir abertura ou carga. Saída da
tela 1 e detach fecham sessão; cancelamento/resposta tardia não liberam formulário.
Discovery/criação de View sem attach não chama preparação nem hardware.

Ambiente: Windows/PowerShell, JBR 25.0.3 e target Java 17, Gradle Wrapper do
projeto, SDK D:/desenvolvimento/ferramentas_android/Sdk, emulator-5554 API 35.
AAR da REV-012 preservado, sem novo gomobile bind. Arquivos privados do host
preservados com os mesmos hashes de REV-014.

| Diretório / comando | Resultado | Exit |
| --- | --- | ---: |
| Raiz: `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | Rejeitou local.properties preexistente; preservado. | 1 |
| Mesmo validador na cópia temporária dos arquivos versionados + não ignorados do projeto (sem configuração local) | OK: invariantes estruturais atendidas. | 0 |
| Projeto Android: `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:lintDebug :plugin-saque-cartao:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` | BUILD SUCCESSFUL; 24 unitários, zero falhas; 239/245 linhas = 97,6%; lint 0 erros/3 avisos preexistentes. | 0 |
| `adb -s emulator-5554 install -r <APK>` para plugin, teste plugin, host e teste host | Quatro Success; storage privado não removido. | 0 |
| `adb -s emulator-5554 shell am instrument -w br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao.test/androidx.test.runner.AndroidJUnitRunner` | OK (15 tests), 131,535s; espera/formulário, falha de preparação/SAIR e regressões da UI, spinner e parser. | 0 |
| `adb -s emulator-5554 shell am instrument -w -e class br.com.romulopenha.sistemaprototipoandroid.platform.BusinessPluginManagerTest br.com.romulopenha.sistemaprototipoandroid.test/androidx.test.runner.AndroidJUnitRunner` | OK (1 test), 2,406s; discovery/View não anexada e Go nativo. Não reexecutados os 20 do host. | 0 |
| Projeto Android: `./gradlew.bat :app:assembleDebug --console=plain` após comentários KDoc finais | BUILD SUCCESSFUL; host final instalado e MainActivity aberta para login. | 0 |
| `adb ... pull .../layout-10004/00-preparacao-emv.png evidence/rev-015/` | PNG inspecionado visualmente; espera/spinner/CANCELAR, sem formulário. A ação CANCELAR desta captura foi removida por REV-016 em `evidence/rev-016/`. | 0 |
| `adb ... run-as ... sha256sum files/saque-cartao/perfil.json files/saque-cartao/emv.json` | Perfil 54ec1a3113c3d604c0343236968a6ef212a2d7ce798836e034895e0d106bef12 e tabela 873d94fa1ec3be17d5a58d130f1ab07b82367eee01c05b4745218c14ebad0ab2 preservados. | 0 |

Unitários pausam a carga com CompletableDeferred: etapa 0 e edição/Begin bloqueados
até conclusão; chamada repetida de load não repete I/O; primeiro comando após
Begin é page, com uma única abertura/consulta/carga em toda a jornada. Cobrem
falha de abertura/carga, cancelamento durante carga, detach e resposta tardia,
saída na tela 1 e os caminhos existentes de confirmação, tarja, EMV e cleanup.
Instrumentação usa estados/tabelas sintéticos; não executa Open/carga física.

Hashes dos APKs finais:

- Plugin: cb2c53454268233ac489afd23900b4e4e5d7e5bfdcbda971270c1fb138c6d147.
- Host: 1a215a718bd87dfd14de655f757cc56b10a619d84a93ed7b6475ea4ac72755e6.
- PNG da espera: ff077b8155e16fbefee8ff2d77ed8379d7d405d95816ffa8329f4f5031fbe804.

Logs/XML/PNG locais em evidence/rev-015/. Carga física ao entrar no plugin e
confirmação/cancelamento com cartão/PIN requerem avaliação humana. Nenhuma
simulação constitui evidência de TLE/GCX físicos aceitos; revisão formal,
validação formal, aprovação final, archive e commit permanecem pendentes.

## Espera sem ação de cancelamento — REV-016 — 2026-10-11

SPEC_APROVADA pela solicitação explícita do solicitante de não existir
possibilidade de cancelar na tela "Aguarde, processando" da preparação.
Estado IMPLEMENTADA, entregue para revisão formal/teste humano. Escopo
limitado ao plugin Pagamento com Cartão, seus testes e documentos da Change;
nenhuma alteração em host, carregador, Go, Bridge ou outros plugins.

A etapa técnica 0 em `busy` renderiza somente título, instrução, spinner e
mensagem real: a condição de `SaqueCartaoScreen` que exibia CANCELAR/CANCELANDO
nessa fase foi restringida às etapas transacionais 2..4, onde existe operação
confirmada no pinpad. A falha de preparação continua oferecendo SAIR e segue
sem formulário. O cleanup por detach/dispose permanece interno ao proprietário
e não é exposto como ação na espera.

Arquivos alterados: `SaqueCartaoScreen.kt` (condição de renderização e KDoc do
`StagePanel`), `SaqueCartaoScreenTest.kt` (asserções de ausência de
CANCELAR/CANCELANDO/SAIR na espera e na falha) e documentos da Change
(`spec.md`, `DESIGN.md`, `tasks.md`, `implementation-plan.md`, esta validação).

Ambiente: Windows/PowerShell, JBR 25.0.3 e target Java 17, Gradle Wrapper do
projeto, SDK `D:/desenvolvimento/ferramentas_android/Sdk`, emulator-5554 API 35
(Medium_Tablet AVD). AAR da REV-012 preservado, sem novo gomobile bind.
Arquivos privados do host preservados; nenhum hardware físico executado.

| Diretório / comando | Resultado | Exit |
| --- | --- | ---: |
| Raiz: `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | Rejeitou `local.properties` preexistente (mesma limitação registrada em REV-015); nenhum artefato local foi removido ou alterado. | 1 |
| Projeto Android: `./gradlew.bat :plugin-saque-cartao:compileDebugAndroidTestKotlin --console=plain` | BUILD SUCCESSFUL; somente avisos preexistentes de depreciação (`MenuAnchorType`, `createComposeRule`). | 0 |
| Projeto Android: `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:lintDebug --console=plain` | BUILD SUCCESSFUL; 24 unitários, zero falhas; lint sem erro novo. | 0 |
| Projeto Android: `./gradlew.bat :plugin-saque-cartao:saqueCoverageVerify :plugin-saque-cartao:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest --console=plain` | BUILD SUCCESSFUL; cobertura 239/245 linhas = 97,6% (LINE missed=6, covered=239), sem regressão frente a REV-015. | 0 |
| Projeto Android: `./gradlew.bat :plugin-saque-cartao:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=...SaqueCartaoScreenTest" --console=plain` | BUILD SUCCESSFUL; 13/13 testes instrumentados, `failures="0" errors="0"`. Primeira execução expôs asserção minha incorreta (SAIR do formulário já entrando durante a transição animada); corrigida antes do resultado final. | 0 |
| `adb -s emulator-5554 shell am instrument -w -e class ...SaqueCartaoScreenTest br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao.test/androidx.test.runner.AndroidJUnitRunner` | OK (13 tests), 135,168s; espera sem ações, falha de preparação com SAIR e cancelamento transacional das etapas 2..4 preservado. | 0 |
| `adb -s emulator-5554 pull .../layout-10004/00-preparacao-emv.png evidence/rev-016/00-preparacao-emv-REV016.png` | PNG inspecionado visualmente: "Aguarde, processando", instrução, spinner e "Carregando tabela EMV...", sem qualquer botão de ação. | 0 |
| Projeto Android: `./gradlew.bat :app:assembleDebug --console=plain` e `adb -s emulator-5554 install -r app-debug.apk` | BUILD SUCCESSFUL e `Success`; host reinstalado com o plugin atualizado embutido para o teste humano. | 0 |

O asset embutido no host é idêntico ao APK do plugin corrigido, com SHA-256
`BB4BBDFFD374F5A9B4184F42DA64E0BC453686A7104627D463AD792DEF578EA4`.

Hashes dos artefatos desta revisão:

- Plugin (`plugin-saque-cartao-debug.apk`): BB4BBDFFD374F5A9B4184F42DA64E0BC453686A7104627D463AD792DEF578EA4.
- Host (`app-debug.apk`): 7610FEDEEC1F82CAE07F06B9A24525ABB1D15768D7590A1BD816B5C55BB73300.
- PNG da espera: 2BF404A0914578D3CAEAF7466AB4A8C3FE874722203131FE4C81E9A6EB68BEC6.

Evidência local em `evidence/rev-016/`: captura 2560x1600 (Medium_Tablet API 35)
e relatório XML dos 13 testes instrumentados (SHA-256
`069335992592E4240FABFD46B88C1F58791E0595C639918166E1DF73F2B5D122`). A imagem
confirma o comportamento observado no emulador, que não substitui a avaliação
humana da carga física ao entrar no plugin. Limitação herdada: o validador
estrutural continua reprovando por `local.properties` preexistente, condição de
ambiente já registrada em REV-015 e não causada por esta mudança. Revisão
formal, validação formal, aprovação final, atualização de `specs/system/`,
archive e commit permanecem pendentes.

## Confirmação com DEX único + GKY — REV-017 — 2026-10-11

SPEC_APROVADA pela solicitação explícita do solicitante: a confirmação no pinpad
deixa de ser "DSP032 em quatro páginas + GKY por página" e passa a **um único
DEX com as quatro informações + um único GKY**. Estado IMPLEMENTADA, entregue
para teste humano. Escopo limitado ao plugin Pagamento com Cartão, seus testes e
documentos da Change; nenhuma alteração em host, carregador, Go, Bridge, AAR ou
outros plugins.

Mensagem DEX (português, latin1, ≤160 bytes) com quebra de linha `\r` e **todas
as linhas centralizadas** na largura de `SaqueDisplay.columns`:

```text
  VALOR TOTAL  \r  R$ 105,34  \r VALOR COMPRA \r  R$ 100,00  \r VALOR TARIFA \r  R$ 5,34  \r QUANTIDADE \r     1x     \r CANCELA/CONFIRMA
```

- `VALOR TOTAL` → `totalCents`; `VALOR COMPRA` → `baseCents`;
  `VALOR TARIFA` → `feeCents`; `QUANTIDADE` → `installments`;
  `CANCELA/CONFIRMA` → GKY. Todas as linhas centralizadas em `columns`.
- Tecla 0x00 = CONFIRMA, 0x0D = CANCELA; demais teclas = `CONFIRMATION_ERROR`.
- Timeout do GKY permanece `config.timeoutSeconds` do perfil.

Alterações: `SaqueJourney.kt` (`confirm()` agora chama `displayText` + um único
`waitKey`; adicionados `confirmationMessage()` e `center()`; removido o loop DSP
e o `require` de 16 colunas), `SaqueJourneyTest.kt` (fake registra a mensagem
DEX; testes atualizados para DEX único + um GKY com centralização), `spec.md`,
`DESIGN.md` e a revisão
`reviews/2026-10-11-spec-review-confirmacao-dex-gky.md`. `SaqueRepository.kt`
não mudou: `displayText` (DEX) já existia; `displayPage` (DSP) permanece
disponível na fachada sem chamador na jornada.

Ambiente: Windows/PowerShell, JBR 25.0.3 e target Java 17, Gradle Wrapper do
projeto, SDK `D:/desenvolvimento/ferramentas_android/Sdk`, emulator-5554 API 35.

| Diretório / comando | Resultado | Exit |
| --- | --- | ---: |
| Projeto Android: `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:lintDebug :plugin-saque-cartao:saqueCoverageVerify --console=plain` | BUILD SUCCESSFUL; 24 unitários, zero falhas; lint sem erro; cobertura 242/248 linhas = 97,6%. | 0 |
| Projeto Android: `./gradlew.bat :app:assembleDebug :plugin-saque-cartao:assembleDebugAndroidTest --console=plain` | BUILD SUCCESSFUL; host regerado com o plugin atualizado embutido. | 0 |
| Projeto Android: `./gradlew.bat :plugin-saque-cartao:connectedDebugAndroidTest --console=plain` | BUILD SUCCESSFUL; 13/13 testes instrumentados, `failures="0"`, 2m29s no emulador. | 0 |

Unitários que cobrem a nova confirmação: `confirmacao usa DEX unico com
informacoes e um GKY` (ordem, conteúdo e centralização da mensagem em dois
displays), `cancelar na confirmacao cancela sem capturar cartao` (0x0D) e as
regressões de captura/EMV/cleanup. A mensagem DEX é verificada com os valores
congelados (`R$ 105,34`, `R$ 100,00`, `R$ 5,34`, `1x`) e `VALOR TOTAL` centralizado.

Hashes dos artefatos desta revisão:

- Plugin (`plugin-saque-cartao-debug.apk`): BE6ED33A86590BA96E39CFAEEAF6237EE12EC3803AA3EA231718DC21E34DD0AA.
- Host (`app-debug.apk`): 79F4E092ABD746A933F477BF1CAD484C8FBD9BC9379B24DDDBE9A5FE0501D3A5.
- Asset embutido no host (`plugins/business/plugin-saque-cartao.apk`): BE6ED33A86590BA96E39CFAEEAF6237EE12EC3803AA3EA231718DC21E34DD0AA (idêntico ao APK do plugin).

Pendência física obrigatória: confirmar no pinpad real que o DEX apresenta as
informações na ordem pedida e que as teclas 0x00 (CONFIRMA) e 0x0D (CANCELA)
chegam com a ordem DEX→GKY registrada no log serial. A separação por `\r` e a
centralização de todas as linhas (espaços na largura de `SaqueDisplay.columns`)
dependem de validação no hardware; nenhum emulador/fake substitui essa
evidência. Revisão formal, validação formal, aprovação final, atualização de
`specs/system/`, archive e commit permanecem pendentes.

## Consolidação documental da centralização no pinpad — 2026-10-11

RF-002 reúne o contrato vigente REV-017 para centralização dos rótulos, valores
 e CANCELA/CONFIRMA. O contrato complementar referencia essa seção e remove a
instrução conflitante DSP/GKY. Revisão: `reviews/2026-10-11-spec-review-centralizacao-pinpad.md`.

Ambiente: Windows, PowerShell, Python, raiz `D:/desenvolvimento/ia/aula02`.

| Comando | Resultado | Exit |
| --- | --- | ---: |
| `git diff --check` | Sem erros de whitespace; avisos de normalização LF/CRLF em arquivos já modificados. | 0 |
| `python -c` com leitura UTF-8 e assertions de RF-002/contrato complementar | PASS: cinco rótulos, largura, limite DEX e GKY único presentes; instruções antigas removidas. | 0 |

Atualização exclusivamente documental; testes Android não se aplicam a esta
edição. Evidências técnicas da REV-017 permanecem registradas acima; a
centralização e as teclas no pinpad físico continuam pendentes de validação.

## Ajuste pontual do SVG de confirmação — 2026-10-11

Solicitante esclareceu que o alvo é somente o bloco de informações em
`source-material/gemini/2_CONFIRMACAO_COMPRA_PINPAD.svg`. Centralizados os seis
textos em x=640, com rótulos e valores em linhas distintas, dentro do painel
existente. Inventário SHA-256 atualizado. Revisão:
`reviews/2026-10-11-spec-review-svg-confirmacao-centralizado.md`.

Ambiente: Windows/PowerShell, Python 3.14, raiz do workspace.

| Comando | Resultado | Exit |
| --- | --- | ---: |
| `python -` (ElementTree, assertions de quantidade, x, text-anchor e conteúdo, SHA-256) | PASS: XML válido, seis textos centralizados, valores preservados, hash atualizado. | 0 |
| `git diff --check -- specs/changes/10004-plugin-saque-cartao/source-material` | Sem erros. | 0 |

Primeira execução da atualização do inventário encontrou BOM UTF-8 (exit 1);
leitura corrigida para utf-8-sig, preservando o BOM. Testes Android não se
aplicam ao ajuste de SVG. Sem nova evidência de hardware.

## Retificação do alvo — display físico do pinpad — 2026-10-11

O solicitante esclareceu que deseja centralização na tela física do pinpad.
Desfeito integralmente o ajuste do SVG e restaurado seu hash original.
Ambiente: Windows/PowerShell, Python 3.14. Comando: `python -` com restauração
pontual e assertion SHA-256. Resultado PASS, exit 0.

Inspeção de `SaqueJourney.confirmationMessage()` mostra centralização por
espaços em `SaqueDisplay.columns` antes de `displayText()` (DEX). Essa inspeção
não comprova alinhamento no hardware. Modelo do dispositivo e aparência do
texto solicitados para identificar a causa antes de alterar o envio.

## REV-018 — Layout da confirmação DEX/GIX — 2026-10-11

Estado: IMPLEMENTADA, revisão técnica em
`reviews/2026-10-11-implementation-review-layout-dex-gix.md`.
Implementado somente no consumidor Android da confirmação física. SVG Gemini
continua com o SHA-256 original; nenhuma nova alteração de UI nesta revisão.

Ambiente: Windows/PowerShell; Python 3.14; JBR em
`D:/desenvolvimento/ferramentas_android/Nova pasta/AndroidStudio/jbr`, target
Java 17; SDK local já configurado. Diretório Gradle:
`apps/frontend/smartphone/sistema-prototipo-android`.

| Diretório/comando | Resultado | Exit |
| --- | --- | ---: |
| Raiz: `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | Reprovado por local.properties preexistente, conforme limitação já registrada. | 1 |
| Projeto Android: `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:lintDebug :plugin-saque-cartao:saqueCoverageVerify :app:assembleDebug --console=plain` com JAVA_HOME acima | BUILD SUCCESSFUL; 25 testes, zero falhas; lint passou; cobertura 250/256 linhas (97,66%); host e plugin compilados. | 0 |
| Raiz: `python -` lendo XML de testes e JaCoCo | Confirmou contagem, falhas e cobertura. | 0 |
| Raiz: `git diff --check -- specs/changes/10004-plugin-saque-cartao apps/frontend/smartphone/sistema-prototipo-android/plugin-saque-cartao` | Sem erros de whitespace; avisos LF/CRLF. | 0 |

Vetores de confirmação para grades sintéticas 16x16 e 16x40: 117 e 154 bytes,
respectivamente. Mesmo tamanho gráfico 320x240 nos dois cenários prova que
pixels não substituem colunas. Grade inválida, altura insuficiente, largura
insuficiente ou excesso de 160 bytes falham antes de DEX/GKY/captura.

Fontes: manual ABECS 2.12 local, páginas 40 e 70, texto extraído com PyMuPDF.
Consulta oficial externa identificou DEX_OPTIONS na versão 2.20
(https://abecs.org.br/storage/pages/01JP0P3T089VNH5HW6HA81NEAY.pdf);
RESEARCH_CANDIDATE, não incorporado pois o contrato vigente é 2.12.

Limitações: nenhum teste instrumentado adicional (UI não alterada); nenhum
comando físico executado nesta revisão. Conferir no PPP100 real as dimensões
GIX, centralização visual e CONFIRMA/CANCELA. Sem aprovação final, archive,
atualização de specs/system ou commit nesta entrega.

## Reversão da REV-018 por regressão — 2026-10-11

Autorização explícita do solicitante: voltar ao funcionamento anterior após
CONFIRMATION_ERROR no pinpad. REV-018 revogada; REV-017 restaurada no código,
adapter, testes e contrato vigente. Removidos agrupamento de campos, CR
iniciais e bloqueios novos por dimensões/altura/tamanho; restaurada a mensagem
original e seu preenchimento. Alterações preexistentes de UI preservadas.
A imagem enviada evidencia erro de confirmação; não se afirma diagnóstico
físico definitivo nem funcionamento validado no hardware após esta reversão.

Ambiente: Windows/PowerShell; JBR em
`D:/desenvolvimento/ferramentas_android/Nova pasta/AndroidStudio/jbr`, Java
17 target; projeto `apps/frontend/smartphone/sistema-prototipo-android`.

| Comando | Resultado | Exit |
| --- | --- | ---: |
| `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:lintDebug :plugin-saque-cartao:saqueCoverageVerify :app:assembleDebug --console=plain` | BUILD SUCCESSFUL; 24 testes, zero falhas; lint e cobertura passaram; host recompilado com plugin revertido. | 0 |
| `git diff --check -- specs/changes/10004-plugin-saque-cartao apps/frontend/smartphone/sistema-prototipo-android/plugin-saque-cartao` | Sem erros, avisos LF/CRLF. | 0 |
| `python -` lendo XML de testes | 24 testes, zero falhas. | 0 |

APK gerado em `app/build/outputs/apk/debug/app-debug.apk`. Não instalado nem
executado no dispositivo nesta revisão. Evidências REV-018 acima são históricas
e não representam o contrato ou a implementação vigente.

## Recuperação operacional da versão anterior — 2026-10-11

Solicitação do usuário: recuperar a versão estável que chegava à senha.
Código REV-017 já restaurado; nenhuma nova mudança de produção. Identidade
binária confirmada contra os hashes da REV-017 registrados nesta Change:

- Host SHA-256: 79F4E092ABD746A933F477BF1CAD484C8FBD9BC9379B24DDDBE9A5FE0501D3A5.
- Plugin SHA-256: BE6ED33A86590BA96E39CFAEEAF6237EE12EC3803AA3EA231718DC21E34DD0AA.

Ambiente: Windows/PowerShell, Python 3.14, Android SDK local,
emulator-5554/Medium_Tablet API 35. Revisão:
`reviews/2026-10-11-recuperacao-versao-estavel.md`.

| Comando/ação | Resultado | Exit |
| --- | --- | ---: |
| `Get-FileHash` dos APKs host/plugin | Hashes idênticos aos da REV-017. | 0 |
| `adb -s emulator-5554 emu kill` | Encerramento do emulador travado. | 0 |
| `Start-Process emulator.exe` com `-avd Medium_Tablet -no-snapshot-load -qt-hide-window -grpc-use-token -idle-grpc-timeout 300`, WindowStyle Hidden | Reinício frio sem wipe-data; BOOT_COMPLETED confirmado. | 0 |
| `adb -s emulator-5554 install -r apps/frontend/smartphone/sistema-prototipo-android/app/build/outputs/apk/debug/app-debug.apk` | Success. | 0 |
| `python -` Hello/Ping PBRG em 127.0.0.1:39100 | PASS, sem comando físico. | 0 |
| `adb -s emulator-5554 reverse tcp:39100 tcp:39100` | Reverse criado. | 0 |
| `adb -s emulator-5554 shell am start -W -n br.com.romulopenha.sistemaprototipoandroid/.MainActivity` | Status ok; COLD; TotalTime 1308 ms. | 0 |
| `adb -s emulator-5554 shell uiautomator dump /sdcard/recovery-window.xml` | UI real de login presente. | 0 |
| `python -` com SHA-256 do APK instalado, XML da UI, pidof e test -s dos arquivos privados | PASS: APK correto instalado, login visível, PID 3648, perfil/tabela preservados. | 0 |

Primeira leitura Python da UI usou encoding local CP1252 e falhou (exit 1);
repetição UTF-8 passou. Antes do reinício, am start/dumpsys/reboot não responderam;
recovery usou encerramento do emulador e boot sem snapshot. Aplicação deixada
aberta no login. Nenhum teste físico de cartão/PIN feito nesta execução.

## REV-019 — DEX/GKY, texto final e entrega no emulador — 2026-10-11

Estado: IMPLEMENTADA, revisão técnica aprovada em
`reviews/2026-10-11-implementation-review-dex-gky-texto-final.md`.
Contrato vigente: `reviews/2026-10-11-spec-review-dex-gky-texto-final.md`.

Mensagem DEX em nove linhas: VALOR TOTAL/total, VALOR TARIFA/tarifa,
VALOR COMPRA/base, QUANTIDADE/Nx e CANCELA/CONFIRMA. Somente o primeiro rótulo
recebe espaços iniciais opcionais conforme colunas GIX, com fallback simples
quando não couber em 160 bytes. Sem padding global, CR iniciais ou validação de
altura que impeça GKY. Captura segue somente após 0x00; 0x0D cancela; LIMPA/F1..F4
falham sem captura. Go, Bridge, AAR, tela Android e SVGs não alterados.

Fontes verificadas: manual local ABECS 2.12 páginas 40 (GIX) e 80 (GKY),
extraído por PyMuPDF; exemplo CLI main.go opções 8/10; core WaitForKeyPress
com ParseGKYStatus. Retornos GKY 000 e 013 mapeiam 0x00/0x0D. DSI exibe imagens,
não consulta dimensões. GIX existente basta para tentativa opcional de alinhamento;
fonte/pixels do DEX não são configuráveis no contrato 2.12. Não usada versão 2.20.

Ambiente: Windows/PowerShell, Python 3.14; Gradle Wrapper, JAVA_HOME em
`D:/desenvolvimento/ferramentas_android/Nova pasta/AndroidStudio/jbr`, target Java
17; Go 1.26.5; Medium_Tablet/emulator-5554 API 35, SDK local.

| Diretório/comando | Resultado | Exit |
| --- | --- | ---: |
| Raiz: `python .agents/skills/android-native-engineering/scripts/validate_android_project.py apps/frontend/smartphone/sistema-prototipo-android` | Reprovado por local.properties preexistente, limitação herdada. | 1 |
| Projeto Android: `./gradlew.bat :plugin-saque-cartao:testDebugUnitTest :plugin-saque-cartao:lintDebug :plugin-saque-cartao:saqueCoverageVerify :app:assembleDebug --console=plain` | BUILD SUCCESSFUL; 27 testes, zero falhas; lint passou; cobertura 243/249 linhas = 97,59%. | 0 |
| Módulo Go: `go test ./internal/domain/parser ./internal/application/service -run 'GKY\|MenuAnd' -count=1` | Pacotes parser e service passaram nos testes filtrados GKY. | 0 |
| Raiz: `python -` comparando hashes do AAR e fontes service/parser/fachada com provenance.json | PASS; AAR e implementação GKY correspondem à proveniência. | 0 |
| Raiz: `python -` conferindo XML unitários/JaCoCo e asset ZIP do host | PASS; contagens/cobertura acima, asset idêntico ao APK plugin novo. | 0 |
| `adb -s emulator-5554 shell am force-stop br.com.romulopenha.sistemaprototipoandroid` e `adb -s emulator-5554 emu kill` | Versão atual encerrada; emulador fechado. | 0 |
| `Start-Process emulator.exe` com `-avd Medium_Tablet -no-snapshot-load -qt-hide-window -grpc-use-token -idle-grpc-timeout 300`, WindowStyle Hidden | Reinício sem snapshot e sem wipe-data; BOOT_COMPLETED. | 0 |
| `python -` Hello/Ping PBRG para 127.0.0.1:39100 | PASS; Bridge existente pronto, sem OPN ou outro comando físico. | 0 |
| `adb -s emulator-5554 install -r apps/frontend/smartphone/sistema-prototipo-android/app/build/outputs/apk/debug/app-debug.apk` | Success; dados preservados. | 0 |
| `adb -s emulator-5554 reverse tcp:39100 tcp:39100` | Reverse reposto e conferido. | 0 |
| `adb -s emulator-5554 shell am start -W -n br.com.romulopenha.sistemaprototipoandroid/.MainActivity` | Status ok; COLD; TotalTime 3947 ms. | 0 |
| `python -` conferindo SHA-256 do APK instalado, UI via uiautomator, pidof, reverse e test -s dos arquivos privados | PASS; versão nova, login visível, PID 2836, perfil/tabela preservados sem leitura de conteúdo. | 0 |
| `git diff --check -- specs/changes/10004-plugin-saque-cartao apps/frontend/smartphone/sistema-prototipo-android/plugin-saque-cartao` | Sem erros; avisos de normalização LF/CRLF. | 0 |

Hashes finais:

- Plugin: 2b83cb12b728788557a387601c5a717645616a1212704e9f91777199c71ef8c9.
- Host instalado: 80c60e52638e83c8bc8513ea40cc9b781a27615fb19b917c8b4c11ac73b0f7bf.
- Asset plugin embutido no host: idêntico ao hash do plugin acima.

Novas evidências JVM cobrem DEX pendente antes de GKY, DEX falhando sem tecla ou
captura, mensagens em 16/40 colunas e dimensões inválidas, 12 parcelas/valores
altos com fallback S160, cancelar e todos os retornos LIMPA/F1..F4. Não há teste
instrumentado adicional de UI porque ela não foi alterada nesta revisão; abertura
real foi conferida no emulador. Sessão serial histórica redigida não comprova
comandos novos. Verificação física de layout/CONFIRMA/CANCELA e jornada até PIN
permanece para operador; nenhum cartão ou PIN usado nesta execução. Sem aprovação
final, archive, specs/system ou commit nesta entrega.
