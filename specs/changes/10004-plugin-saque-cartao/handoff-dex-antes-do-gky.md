# Handoff — DEX informativo antes do GKY na confirmação no pinpad

> **STATUS: RESOLVIDO — REV-017.** O solicitante confirmou o fluxo final:
> DEX único com as quatro informações + um único GKY. A decisão foi registrada
> em `reviews/2026-10-11-spec-review-confirmacao-dex-gky.md`, o código
> implementado em `SaqueJourney.kt` e a validação registrada em `validation.md`
> (seção "Confirmação com DEX único + GKY — REV-017"). Este documento permanece
> como registro histórico do diagnóstico.

**Destino:** DeepSeek-V4-Pro (executor). Este texto substitui as imagens: o
executor não recebeu os prints, mas todo o conteúdo útil deles está descrito em
texto abaixo, junto com o log da sessão serial.

**Data do episódio:** 2026-10-11, 09:02–09:04 (horário local, GMT-03).
**Autor do handoff:** agente DSH, a partir de código, specs e log verificados.

---

## 1. Pedido original do solicitante (literal)

> Ao tentar chamar para confirmar no pinpad dá problema, seria algo similar
> utilizando a spec 074 Pinpad Android Bridge Stack, spec 066-lib-pinpad-abecs-go,
> na parte de confirmar no pinpad.
>
> No executável de teste `D:\desenvolvimento\ia\aula02\apps\desktop\libpinpadabecsgo\bin\libpinpadabecsgo.exe`:
>
> 10) Aguardar tecla pressionada (GKY) — ele já faz isso. Só pro nosso caso,
> antes de fazer GKY, fazer essas sequências:
>
> 8) Exibir mensagem estendida (DEX) — MENSAGEM INFORMATIVA PARA O USUÁRIO ACEITAR AS CONDIÇÕES
> 10) Aguardar tecla pressionada (GKY) — EVENTO CANCELA E CONFIRMA
>
> Também tem a spec 068 diagnosticopinpad, que mostra a integração com os dois
> comandos.

**Refinamento do solicitante (conteúdo concreto a exibir no pinpad):**

> MOSTRAR NA TELA DO PINPAD AS INFORMAÇÕES:
>
> 1. VALOR TOTAL COMPRA
> 2. QUANTIDADE DE VEZES (1 ATÉ 12)
> 3. VALOR TOTAL TARIFA
> 4. CANCELA/CONFIRMA

Esses quatro itens são o conteúdo que a etapa de confirmação deve apresentar no
pinpad. Os três primeiros são dados da transação; o quarto é o par ação/evento
do GKY. **Restrição relevante:** os rótulos pedidos excedem as 16 colunas do
DSP — "VALOR TOTAL COMPRA" tem 18, "QUANTIDADE DE VEZES (1 ATÉ 12)" tem 30 e
"VALOR TOTAL TARIFA" tem 18 caracteres; só "CANCELA/CONFIRMA" cabe exatamente
em 16. Isso reforça o uso do DEX (até 160 bytes) para o texto informativo.

**Interpretação do agente:** hoje a confirmação da etapa 2 da jornada de saque
faz DSP032 em quatro páginas (rótulos abreviados "VALOR TOTAL", "PARCELAS",
"TARIFA", "CONFIRMA?") + GKY e falha. O objetivo é que o caminho real usado pela
jornada apresente no pinpad os quatro itens acima, usando **DEX para a mensagem
informativa** imediatamente antes do **GKY** que lê as teclas CONFIRMA/CANCELA.

---

## 2. O que as duas imagens mostram (descrição textual)

**Imagem 1 — tela Android, etapa 2 (Medium Tablet API 35, 1366x768):**
cabeçalho do host com "Buy More", "POS - COMPRAS", operador `1099264 - Operador Teste001`, `TER-MINAL`, `v1.0.0`. Corpo: título "Pagamento com cartão", cartão branco central com "Confirmação no terminal" e a instrução "Peça ao cliente que confira os valores e confirme no pinpad.". Resumo: `Valor total R$ 105,34`, `Parcelas 1x`, `Tarifa R$ 5,34`. Abaixo, spinner indeterminado e a mensagem azul "Aguardando confirmação do cliente...". Botão azul-escuro "CANCELAR" habilitado na base do cartão.

**Imagem 2 — tela Android, etapa 5 (resultado), mesma sessão:** ícone de exclamação em círculo vermelho claro, título vermelho "Erro na homologação", texto "Não foi possível confirmar o encerramento da sessão. Verifique o terminal antes de nova operação.". Mesmo resumo (`R$ 105,34`, `1x`, `R$ 5,34`). Botão "VOLTAR AO MENU INICIAL". Sem "Falha: CODE · fase" — ou seja, nenhum `failureCode` foi publicado, só o `cleanupFailed`.

Leitura do agente: o texto da imagem 2 é exatamente a mensagem de
`SaqueJourney.kt` quando `cleanup()` retorna falha (`cleanupFailed = true`) e
não existe `failureCode`/`failurePhase`. Isso é **falha de cleanup**, não erro
de comando de confirmação.

---

## 3. Evidência serial (log real, 2026-10-11 09:02–09:04)

Fonte: `D:\desenvolvimento\ia\aula02\apps\desktop\libpinpadabecsgo\logs\LogPinpadAbecs.txt`
(151 linhas; o conteúdo dos payloads já vem redigido pelo próprio core).

Fatos verificáveis do log:

| Linha | Fato |
| --- | --- |
| 3–4 | Bridge `physical`, `COM7`, endpoint `127.0.0.1:39100`, `PORTA_PINPAD` do ambiente |
| 15–18 | `acquire_requested` → `ownership_acquired` → `open(COM7,19200,8,N,1)=>OK` → `session_acquired` (`sessionId=7860-1`) |
| 19–20 | `SPE 18` (7 bytes: provável GIX/`GetDisplayCapabilities`) → `PP 04` |
| 21–24 | `SPE` 7 bytes → `PP 06` + 1 + 9 bytes (handshake/`OPN`) |
| 25–30 | `SPE` 10 bytes → `PP 06` + 1 + **256 + 256 + 251** bytes (carga da tabela EMV, TLI/TLR/TLE) |
| 31–144 | sequência de **13 ciclos** `SPE ... / PP 06 / 1 / 9`: são as páginas de display DSP (`SPE` de 692, 976, 864, 623 bytes) e as respostas |
| 141 | `SPE` de **7 bytes** às 09:03:41.979 → payload exatamente do tamanho do comando literal `GKY`; é o GKY da confirmação |
| 143–144 | resposta do GKY só chega às **09:03:42.293–42.295** (≈310 ms) |
| **145** | **salto de ≈24,7 s**: às **09:04:06.992** o host manda `SPE` de 42 bytes — payload compatível com **DSP** de duas linhas de 16 colunas (limpeza visual), não com DEX |
| 146–148 | `PP 06` + 1 + 9 bytes |
| 149–151 | `close()`, `session_released`, `client_disconnected` |

Observações que importam para o diagnóstico:

1. **Não há nenhum `SPE` de tamanho compatível com DEX** (DEX de, por exemplo, 60–100 caracteres daria payload ≈ 66–106 bytes). O log desta sessão **não contém DEX**.
2. O intervalo de ≈24,7 s entre a resposta do GKY e o próximo comando é o tempo até o usuário desistir/voltar; o comando seguinte é DSP de limpeza, não GKY novo.
3. Não houve `CANCEL`/`CAN` como comando próprio antes do `close()`; o `close()` é o encerramento do cliente.
4. Os únicos payloads não redigidos são os pequenos (`SPE 18`, `SPE` 7 bytes); todo o resto é `REDACTED(n bytes)`, com `n` preservado.
5. A sessão foi liberada corretamente no Bridge (`session_released`), mas o app mostrou **`cleanupFailed`** (imagem 2), logo o cleanup do lado do core não terminou dentro do orçamento.

**Conclusão do agente (hipótese principal, a confirmar):** a falha não é o DEX
em si (ele nem foi enviado). O caminho atual de confirmação tem dois problemas
plausíveis e independentes:
(a) **GKY com deadline curto** — `timeoutSeconds` do perfil (1..120 s) fecha o
`context` e o operador não tem tempo de ler/decidir; a exceção resultante leva
ao caminho de erro/cleanup;
(b) **cleanup frágil** — `cleanup()` em `SaqueJourney.kt` soma `cancel`+`close`
com `withTimeout((timeoutMillis ?: 5000) * 2)`; se o `close` não conclui, o
resultado é `cleanupFailed = true` e a tela de erro sem código (imagem 2).
O pedido de DEX antes do GKY é requisito de produto (aceite das condições) e
também tende a reduzir o problema (a) se o GKY for chamado com timeout
suficiente ou sem deadline.

---

## 4. Estado atual do código (o que existe hoje)

### 4.1 Caminho real da jornada de saque (Kotlin/Compose)

`apps/frontend/smartphone/sistema-prototipo-android/plugin-saque-cartao/src/main/java/br/com/romulopenha/sistemaprototipoandroid/pluginsaquecartao/`

- **`SaqueJourney.kt`** — orquestra a jornada. Trecho relevante (linhas ~205–238):

```kotlin
private suspend fun confirm(display: SaqueDisplay, config: SaquePerfil, summary: SaqueResumo, id: String) {
    require(display.lines >= 2 && display.columns >= 16)
    val pages = listOf(
        "VALOR TOTAL" to "R$ ${formatCents(summary.totalCents)}",
        "PARCELAS" to "${summary.installments}x",
        "TARIFA" to "R$ ${formatCents(summary.feeCents)}",
        "CONFIRMA?" to "CANCELA?",
    )
    require(pages.all { (label, value) -> label.length <= 16 && value.length <= 16 })
    for ((label, value) in pages) {
        checkpoint()
        repository.displayPage(id, label, value)          // → DSP
        confirmed(repository.waitKey(id, config.timeoutSeconds))  // → GKY
    }
}

private fun confirmed(key: Int) {
    if (key == 13) throw SaqueFailure("CANCELED")          // 13 = CANCEL
    if (key != 0) throw SaqueFailure("CONFIRMATION_ERROR", "command")  // 0 = OK
}

private fun checkpoint() {
    if (cancelSent || disposed) throw SaqueFailure("CANCELED")
    if (state.value.cancelRequested) throw SaqueFailure("CANCEL_UNCONFIRMED")
}
```

  Ou seja: **4 páginas DSP, cada uma seguida de um GKY**. Leitura literal do log: 13 ciclos de display antes do GKY final — compatível com páginas adicionais (GKY por página) e com o `require(pages.all { ... })`.

- **`SaqueRepository.kt`** — ponte tipada para a fachada Go. Interfaces e
  implementações já existentes (linhas ~36–129):

```kotlin
/** Exibe texto DEX que já cabe nas capacidades reais. */
suspend fun displayText(operationId: String, message: String)      // ⚠ EXISTE E NÃO É CHAMADO
suspend fun displayPage(operationId: String, first: String, second: String)  // DSP
suspend fun waitKey(operationId: String, timeoutSeconds: Int): Int           // GKY
...
override suspend fun displayText(operationId: String, message: String) = goCall {
    requireClient().displayDEX(operationId, message)                // binding gomobile
}
override suspend fun displayPage(operationId: String, first: String, second: String) = goCall {
    require(first.length <= 16 && second.length <= 16)
    requireClient().displayDSP(operationId, first, second)
}
override suspend fun waitKey(operationId: String, timeoutSeconds: Int): Int = goCall {
    requireClient().waitForKeyPress(operationId, timeoutSeconds.toLong()).toInt()
}
```

  **Achado relevante:** `displayText` (DEX) já está pronto no repository e
  **não tem nenhum chamador** em todo o plugin (verificado por busca no
  diretório `src/main`). Toda a integração DEX pode ser feita na jornada, sem
  tocar no binding.

- **`SaqueModels.kt`** — `SaquePerfil` valida
  `timeoutMillis in 100..120000` e `timeoutSeconds in 1..120`. Logo o GKY nunca
  é chamado com 0 (indefinido) hoje.

### 4.2 Executável de teste (menu interativo Go)

`apps/desktop/libpinpadabecsgo/cmd/libpinpadabecsgo/main.go`

- Menu (`printMenu`, linhas ~431–454): `7) DSP`, `8) Exibir mensagem estendida (DEX)`, `9) MNU`, `10) Aguardar tecla pressionada (GKY)`.
- `case "8"` (linhas 231–234):

```go
case "8":
    message := readLine(reader, "Mensagem: ")
    _, err := svc.DisplayDEX(ctx, message)
    handleErr(logger, "DisplayDEX (DEX)", err)
```

- `case "10"` (linhas 243–248):

```go
case "10":
    timeoutSec := readInt(reader, "Timeout em segundos (0 = aguardar indefinidamente): ")
    key, err := svc.WaitForKeyPress(ctx, timeoutSec)
    if handleErr(logger, "WaitForKeyPress (GKY)", err) {
        fmt.Printf("Tecla pressionada: 0x%02X\n", key)
    }
```

- Cada opção roda sob `context.WithTimeout(context.Background(), menuTimeout)`;
  `menuTimeout` é a constante citada no comentário das linhas ~35 ("pinpad
  (ex.: MNU, GKY, GCX aguardando cartao), evitando que o timeout curto…").
  **Conferir o valor de `menuTimeout` antes de testar DEX+GKY no executável**,
  porque um deadline de menu menor que o tempo de leitura da mensagem reproduz
  o mesmo sintoma do app.

### 4.3 Núcleo Go (builder, serviço e binding)

`apps/desktop/libpinpadabecsgo/`

- **`internal/domain/command/advanced.go`** — `BuildDEXCommand`:

```go
// BuildDEXCommand monta DEX com DEX_MSGLEN N3 e mensagem S1..160.
func BuildDEXCommand(message string) ([]byte, error) {
    encoded, err := latin1(message, 160, "DEX message")
    ...
    if len(encoded) == 0 { return nil, fmt.Errorf("DEX message is required") }
    return BuildPacketPayload(CommandDEX, fmt.Sprintf("%03d%s", len(encoded), encoded)), nil
}
```

  Consequências: mensagem **1..160 bytes**, máximo validado **antes** do envio,
  `DEX_MSGLEN` em 3 dígitos decimais, codificação latin1. Testes em
  `advanced_test.go` cobrem `"Feliz Natal\re um\rPróspero\rAno Novo!"`, rejeição
  de vazio e de 161 caracteres.

- **`internal/application/service/service.go`** — `DisplayDEX` (linha 695) usa
  `sendPayload` (**não bloqueante**); `WaitForKeyPress` (linha 717) usa
  `sendBlockingPayload` com `context.WithTimeout(ctx, timeout*time.Second)`
  **somente se `timeout > 0`** (0 = aguarda indefinidamente, respeitando o
  contexto pai), e converte `RSP_STAT` por `parser.ParseGKYStatus`.

- **`mobile/catalog.go`** — fachada gomobile:

```go
// DisplayDEX envia uma mensagem estendida ao display.
func (c *Client) DisplayDEX(operationID, message string) (err error)
// WaitForKeyPress aguarda GKY e devolve a tecla como inteiro sem bytes raw.
func (c *Client) WaitForKeyPress(operationID string, timeoutSeconds int) (key int, err error)
```

  Ambas entram em `c.run`/`c.runInt` com `operationID` obrigatório: **a
  correlação e o cancelamento já estão resolvidos** para os dois comandos.

### 4.4 Exemplo de integração dos dois comandos (o que a 068 faz)

`apps/frontend/smartphone/diagnosticopinpad/app/src/main/java/br/com/romulopenha/diagnosticopinpad/data/DiagnosticRepository.kt`
(linhas ~100–102) — catálogo de ações do app de diagnóstico, sessão única,
cada ação chamada por `operationID`:

```kotlin
CatalogAction.DEX -> bridge.displayDEX(operationId, input.value("message")).let { "DEX concluído" }
CatalogAction.GKY -> "Tecla pressionada: 0x${bridge.waitForKeyPress(operationId, int("timeout").toLong()).toString(16).uppercase()}"
```

Esse é o precedente aprovado: **DEX e GKY são ações consecutivas na mesma
sessão aberta**, cada uma com seu `operationID`, sem reabrir a sessão entre elas.

---

## 5. Contratos vigentes aplicáveis (citar no plano)

- **066 — `specs/archive/2026-09-09-066-lib-pinpad-abecs-go/spec-command-dex.md`**
  (`SPEC_APROVADA`): builder calcula e serializa `DEX_MSGLEN`; valida tamanho
  máximo antes do envio; enviar pelo worker único, interpretar ACK/status e
  preservar resposta bruta; rastro `SPE CMD=DEX`, `PP`, `RSP CMD=DEX STATUS=...`;
  redigir conteúdo sensível. Aceite: mensagem válida exibida em pinpad físico;
  vazia/excedente/codificação inválida produz erro de validação documentado;
  timeout, NAK, cancelamento e status ABECS distinguíveis.
- **066 — `spec-command-gky.md`** (`SPEC_APROVADA`): payload literal `GKY`, sem
  modo ou timeout; tecla no `RSP_STAT` (000 OK, 004..007 F1..F4, 008 CLEAR,
  013 CANCEL); timeout e cancelamento distintos de tecla não pressionada;
  estado volta a `OPEN` após a operação. **Correção normativa de 2026-09-13**
  sobrepõe a redação anterior.
- **066 — `spec.md` RF-012.3**: `BuildDEXCommand`: mensagem de até 160
  caracteres prefixada por `DEX_MSGLEN` (N3), **sem campos de alinhamento ou
  tipo de mensagem**. RF-012.6: GKY com parser dos códigos e timeout distinguível.
- **074 — `specs/changes/074-pinpad-android-bridge-stack/spec.md`**: contrato
  PBRG v1 binário, ownership/serial no Bridge, payload opaco; o Bridge **não**
  executa regra ABECS. Kotlin não monta comando ABECS.
- **068 — `specs/archive/2026-09-27-068-diagnosticopinpad/spec.md`**: precedente
  de app consumindo a fachada com ações tipadas (sem executor genérico) e
  `operationID` ponta a ponta; RF-003/RF-005/RF-006 são o modelo de integração.
- **10004 — `specs/changes/10004-plugin-saque-cartao/spec.md`** (o consumidor):
  - **REV-010** (linha ~290): a confirmação no pinpad usa `DSP032` (duas linhas
    S16) **com `GKY` em quatro páginas** — valor total, parcelas, tarifa e
    confirmação/cancelamento, seções 3.3.5 e 3.3.10 do manual ABECS 2.12.
  - **RF-002**: não avançar por clique no Android, por envio bem-sucedido ao
    display ou por tecla/índice inesperado.
  - **RF-006**: cancelamento alcança o core; cleanup não substitui a causa
    principal; chamadas gomobile bloqueantes fora da main thread.
  - **REV-011**: `MISSING`, `TABLE_MISSING`, `CONFIGURATION` e
    `CONFIRMATION_ERROR` são os códigos locais sanitizados.
  - **RNF**: "Aguardo descreve estado real; spinner indeterminado…"; "Estado
    sempre textual, não apenas por cor".

⚠ **Conflito a resolver antes de codificar:** REV-010 diz "DSP032 + GKY em
quatro páginas"; o pedido atual insere DEX no caminho de confirmação. Isso exige
**revisão de SPEC em `specs/changes/10004-plugin-saque-cartao/reviews/` e
atualização de `spec.md`/`DESIGN.md` antes da implementação** (regra do
`AGENTS.md`: não implementar mudança sem `SPEC_APROVADA`).

---

## 6. Invariantes que o executor NÃO pode violar

1. O core Go é a única fonte de verdade do protocolo ABECS; Kotlin **não** monta
   comando, não calcula CRC, não interpreta frame.
2. Não criar comando novo, parâmetro novo ou regra ABECS em Kotlin; usar
   `displayDEX`/`waitForKeyPress` já expostos pela fachada.
3. Não expor PAN, trilhas, PIN block, KSN, chaves, EMV sensível, conteúdo de
   tabela ou payload bruto em estado Compose, JSON público ou logs.
4. `operationID` não vazio e não reutilizado para retry automático.
5. Chamadas gomobile bloqueantes **fora** da main thread.
6. Não alterar contrato PBRG, o Bridge, o AAR, o executável de teste ou outros
   plugins, salvo o mínimo necessário e justificado.
7. Uma operação com resultado indeterminado **não** é repetida automaticamente.
8. Mensagem de DEX: 1..160 bytes, latin1, validada antes do envio.

---

## 7. Sequência-alvo (proposta para o executor validar)

```
[sessão já aberta: Ping → Open → capacidades → carga EMV confirmada]   (etapa 0, REV-015)
        │
        ▼
etapa 1: formulário; AVANÇAR congela o resumo
        │
        ▼
etapa 2: confirmação
   DEX  mensagem informativa com os dados da transação                 ← NOVO
         1) VALOR TOTAL COMPRA        (totalCents: R$ 105,34)
         2) QUANTIDADE DE VEZES       (1..12: "1x")
         3) VALOR TOTAL TARIFA        (feeCents: R$ 5,34)
   GKY  aguarda tecla   →  0x00 = CONFIRMA | 0x0D = CANCELA             ← EVENTO
        │
        ├── CONFIRMA → segue GCX/GOX/FCX (etapas 3..5)
        └── CANCELA  → SaqueFailure("CANCELED") → resultado cancelado
```

Os quatro itens do solicitante mapeiam para os campos já congelados em
`SaqueResumo`: `VALOR TOTAL COMPRA` → `totalCents`, `QUANTIDADE DE VEZES (1 ATÉ
12)` → `installments`, `VALOR TOTAL TARIFA` → `feeCents`, e `CANCELA/CONFIRMA` →
o par ação/evento do GKY. **Confirmar com o solicitante** se "VALOR TOTAL COMPRA"
é o total (base+tarifa = `totalCents`) ou o valor base (`baseCents`); o texto
não é ambíguo, então registrar a interpretação na revisão.

Como os rótulos excedem 16 colunas, a transportadora natural é o **DEX** (até
160 bytes, latin1). As quatro páginas DSP abreviadas de REV-010 podem ser
substituídas pelo DEX único ou mantidas para os dados — decidir em revisão (ver
§8 D1/D2/D6).

---

## 8. Decisões abertas (com recomendação)

| # | Pergunta | Recomendação |
| --- | --- | --- |
| D1 | DEX **substitui** as 4 páginas DSP ou entra **antes** da página final `CANCELA/CONFIRMA`? | O DEX carrega os três dados (valor/parcelas/tarifa); as páginas DSP podem ser removidas ou mantidas só para o resumo curto. Decidir em revisão; **recomendação: DEX único com os 4 itens, seguido de um GKY.** |
| D2 | Quem chama o GKY de decisão: o GKY por página existente ou **um** GKY após o DEX? | **Um** GKY após o DEX, para que a tecla lida corresponda semanticamente ao aceite das condições. |
| D3 | Timeout do GKY de decisão | `config.timeoutSeconds` do perfil, mas com valor que permita leitura da mensagem; documentar o orçamento por fase (RF-006 já pede orçamento por fase). Não usar 0 (indefinido) sem revisão explícita, porque `SaquePerfil` valida 1..120. |
| D4 | Evento CONFIRMA/CANCELA: aceitar as teclas `0x00` e `0x0D` (já implementado) | Manter; `0x13`/outros continuam `CONFIRMATION_ERROR`. Confirmar no pinpad físico quais códigos chegam. |
| D5 | Texto exato da mensagem | **Resolvido pelo solicitante:** os 4 itens acima. Definir o texto literal final (maiúsculas, acentuação latin1) e o separador entre campos na SPEC; não inventar valores. |
| D6 | Layout da mensagem DEX: uma linha longa vs. quebra `\r` por campo | Validar em hardware; `BuildDEXCommand` não tem campo de alinhamento (RF-012.3), então a quebra é `\r` (0x0D) no conteúdo, como nos testes (`"Feliz Natal\re um\rPróspero\rAno Novo!"`). |
| D7 | O `cleanupFailed` (imagem 2) precisa de correção independente? | Investigar em paralelo: orçamento de `cleanup()` e o que faz o `close` estourar; a tela deve continuar mostrando o código sanitizado quando houver causa real. |
| D8 | "VALOR TOTAL COMPRA" = total (base+tarifa) ou base? | Confirmar com o solicitante; recomendação `totalCents` (coerente com o resumo atual da tela, que mostra "Valor total R$ 105,34"). |

---

## 9. Pontos de alteração previstos (para o plano)

| Arquivo | O que muda |
| --- | --- |
| `specs/changes/10004-plugin-saque-cartao/spec.md`, `DESIGN.md`, `reviews/…` | Registrar a decisão D1–D6 e sobrepor/ajustar REV-010. **Fazer antes do código.** |
| `.../pluginsaquecartao/SaqueJourney.kt` (`confirm`) | Inserir `repository.displayText(id, mensagem)` antes do GKY de decisão; ajustar o ciclo de páginas conforme D1/D2. |
| `.../pluginsaquecartao/SaqueRepository.kt` | Provavelmente **nenhuma mudança**: `displayText` já existe. Só mexer se precisar de validação adicional de tamanho/persistência. |
| `.../pluginsaquecartao/SaqueCartaoScreen.kt` | Só se o texto/estado da etapa 2 mudar (a tela já mostra a mensagem real de estado). |
| Testes JVM/instrumentados do plugin | Cobrir DEX→CONFIRMA e DEX→CANCELA, ordem dos comandos, ausência de DEX com mensagem inválida e manutenção do cleanup. |
| `apps/desktop/libpinpadabecsgo/…` | **Não alterar** se o problema se resolver no consumidor; mexer só com causa comprovada e nova revisão. |

---

## 10. Evidência exigida ao final (comando, exit code, resultado)

1. Testes JVM do plugin com contagem e `exit 0`.
2. `:plugin-saque-cartao:lintDebug` e `saqueCoverageVerify` (cobertura ≥ 80% no código elegível).
3. Teste instrumentado focado em espera/confirmação no emulador, `exit 0`.
4. **Log serial novo** (`logs/LogPinpadAbecs.txt`) provando a ordem: DEX (`SPE` de tamanho compatível com a mensagem) **imediatamente antes** do `SPE` de 7 bytes do GKY, na mesma `sessionId`.
5. Confirmação no pinpad físico das teclas `0x00` (CONFIRMA) e `0x0D` (CANCELA) → duas execuções, resultados distintos.
6. Se o `cleanupFailed` persistir, log/handling que mostre a causa e o código sanitizado exibido.

---

## 11. Erros a evitar (aprendizados deste repositório)

- **Não** implementar antes de `SPEC_APROVADA`; o `AGENTS.md` exige revisão e
  validação registradas em `reviews/` e `validation.md`.
- **Não** tratar envio bem-sucedido ao display como confirmação (RF-002).
- **Não** concluir que o DEX é a causa do erro atual: o log mostra que o DEX
  **não foi enviado** na sessão que falhou.
- **Não** pedir timeout infinito sem revisão: `SaquePerfil` valida 1..120 s.
- **Não** adicionar comando ABECS novo em Kotlin nem alterar o Bridge.
- **Não** registrar conteúdo de cartão/chave/tabela em log ou UI.
