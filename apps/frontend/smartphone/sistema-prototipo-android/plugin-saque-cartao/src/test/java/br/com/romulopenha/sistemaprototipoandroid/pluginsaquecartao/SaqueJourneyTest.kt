package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/** Cenários sintéticos: não representam evidência de hardware. */
@OptIn(ExperimentalCoroutinesApi::class)
class SaqueJourneyTest {
    @Test fun `GIX sem chave PIN bloqueia formulario antes de captura`() = runTest {
        val fake = FakeSaqueRepository(openFailure = SaqueFailure("PIN_KEY_MISSING", "preflight"))
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent()
        assertFalse(owner.state.value.configReady)
        assertEquals("PIN_KEY_MISSING", owner.state.value.failureCode)
        assertEquals("preflight", owner.state.value.failurePhase)
        assertTrue(owner.state.value.message.contains("DUKPT TDES de PIN"))
        assertFalse(fake.calls.contains("capture"))
        assertEquals("close", fake.calls.last())
    }
    @Test fun `falha GTK impede GOX e GPN e preserva erro principal mesmo se close falhar`() = runTest {
        for (type in listOf("00", "03", "05", "06")) {
            val fake = FakeSaqueRepository(type = type, tracksFailure = SaqueFailure("PINPAD_ERROR", "command"), closeFailure = true)
            val owner = SaqueJourney(fake, backgroundScope) {}
            owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("100")); owner.onEvent(SaqueEvent.Begin); runCurrent()
            assertFalse(fake.calls.contains("gox")); assertFalse(fake.calls.contains("pin"))
            assertEquals("PINPAD_ERROR", owner.state.value.failureCode)
            assertEquals("command", owner.state.value.failurePhase)
            assertEquals(SaqueResultado.UNKNOWN, owner.state.value.result)
            assertTrue(owner.state.value.cleanupFailed)
            assertEquals("close", fake.calls.last())
        }
    }

    @Test fun `tipo desconhecido interrompe sem assumir tarja`() = runTest {
        val fake = FakeSaqueRepository(type = "99")
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("100")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertFalse(fake.calls.contains("tracks")); assertFalse(fake.calls.contains("pin")); assertFalse(fake.calls.contains("gox"))
        assertEquals("PROTOCOL_ERROR", owner.state.value.failureCode)
    }

    @Test fun `GOX negado nao envia FCX e close nao confirmado torna resultado indeterminado`() = runTest {
        for (closeFails in listOf(false, true)) {
            val fake = FakeSaqueRepository(type = "06", gox = '1', closeFailure = closeFails)
            val owner = SaqueJourney(fake, backgroundScope) {}
            owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("100")); owner.onEvent(SaqueEvent.Begin); runCurrent()
            assertFalse(fake.calls.contains("fcx")); assertFalse(fake.calls.contains("pin"))
            assertEquals(if (closeFails) SaqueResultado.UNKNOWN else SaqueResultado.DENIED, owner.state.value.result)
            assertEquals(closeFails, owner.state.value.cleanupFailed)
        }
    }

    @Test fun `colagem invalida nao vira compra positiva ou truncada`() = runTest {
        val fake = FakeSaqueRepository()
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent()
        val preparationCalls = fake.calls.toList()
        for (input in listOf("-10000", "abc10000", "1234567890123")) {
            owner.onEvent(SaqueEvent.Value(input))
            assertEquals(input, owner.state.value.valueInput)
            assertNull(owner.state.value.summary)
            owner.onEvent(SaqueEvent.Begin); runCurrent()
            assertEquals(preparationCalls, fake.calls)
        }
        owner.onEvent(SaqueEvent.Value("00100,00"))
        assertEquals("10000", owner.state.value.valueInput)
        assertEquals(10534L, owner.state.value.summary!!.totalCents)
    }

    @Test fun `formulario so recebe edicao apos preparacao e respeita limite externo`() = runTest {
        val fake = FakeSaqueRepository()
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.onEvent(SaqueEvent.Value("10000"))
        owner.onEvent(SaqueEvent.Installments("2"))
        assertNull(owner.state.value.summary)
        assertEquals(0, owner.state.value.step)
        owner.load(); runCurrent()
        owner.onEvent(SaqueEvent.Value("10000"))
        owner.onEvent(SaqueEvent.Installments("2"))
        assertEquals(10614L, owner.state.value.summary!!.totalCents)
        assertEquals("6,14%", owner.state.value.feeLabel)
        owner.onEvent(SaqueEvent.Value("200000"))
        assertNull(owner.state.value.summary)
        owner.onEvent(SaqueEvent.Value("20000"))
        assertEquals(21228L, owner.state.value.summary!!.totalCents)
        assertEquals(listOf("open", "capabilities", "table"), fake.calls)
    }

    @Test fun `modalidade congelada e total seguem para captura com filtro correto`() = runTest {
        for (mode in SaqueModalidade.entries) {
            val fake = FakeSaqueRepository()
            val owner = SaqueJourney(fake, backgroundScope) {}
            owner.load(); runCurrent()
            owner.onEvent(SaqueEvent.Value("10000"))
            owner.onEvent(SaqueEvent.Modalidade(mode))
            owner.onEvent(SaqueEvent.Begin); runCurrent()
            assertEquals(mode, fake.capturedSummary!!.modalidade)
            assertEquals(if (mode == SaqueModalidade.CREDITO) "000000010534" else "000000010100",
                fake.capturedSummary!!.amountN12())
            assertEquals(if (mode == SaqueModalidade.CREDITO) "01" else "02", fake.capturedApplicationTypes)
        }
    }

    @Test fun `tarja captura PIN depois de GTK e nao envia EMV`() = runTest {
        val fake = FakeSaqueRepository()
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent()
        owner.onEvent(SaqueEvent.Value("15000"))
        owner.onEvent(SaqueEvent.Begin); owner.onEvent(SaqueEvent.Begin)
        runCurrent()
        assertEquals(SaqueResultado.CAPTURED, owner.state.value.result)
        assertEquals(15801L, owner.state.value.summary!!.totalCents)
        assertEquals(listOf("open", "capabilities", "table",
            "display", "key",
            "capture", "tracks", "pin", "close"), fake.calls)
        assertEquals(2, fake.ids.distinct().size)
        assertEquals(1, fake.ids.take(3).distinct().size)
        assertEquals(1, fake.ids.drop(3).distinct().size)
        assertTrue(fake.ids.all { it.isNotBlank() })
    }

    @Test fun `EMV online usa FCX e decisao real`() = runTest {
        val fake = FakeSaqueRepository(type = "03", gox = '2', fcx = '1')
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("100")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals(SaqueResultado.DENIED, owner.state.value.result)
        assertFalse(fake.calls.contains("pin"))
        assertEquals(listOf("tracks", "gox", "fcx", "close"), fake.calls.takeLast(4))
    }

    @Test fun `EMV offline nao envia FCX e CTLS simulado usa GPN depois de GTK`() = runTest {
        for (type in listOf("03", "05", "06")) {
            val fake = FakeSaqueRepository(type = type, gox = '0')
            val owner = SaqueJourney(fake, backgroundScope) {}
            owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("100")); owner.onEvent(SaqueEvent.Begin); runCurrent()
            assertFalse(fake.calls.contains("fcx"))
            assertEquals(type == "05", fake.calls.contains("pin"))
            assertEquals(type != "05", fake.calls.contains("gox"))
            assertEquals(if (type == "05") SaqueResultado.CAPTURED else SaqueResultado.APPROVED, owner.state.value.result)
            assertFalse(fake.calls.contains("cancel"))
            if (type == "05") assertTrue(fake.calls.indexOf("tracks") < fake.calls.indexOf("pin"))
        }
    }

    @Test fun `confirmacao usa DEX unico com informacoes e um GKY`() = runTest {
        val body = "\rR$ 105,34\rVALOR TARIFA\rR$ 5,34\rVALOR COMPRA\rR$ 100,00\rQUANTIDADE\r1x\rCANCELA/CONFIRMA"
        for ((display, title) in listOf(
            SaqueDisplay(2, 16) to "  VALOR TOTAL",
            SaqueDisplay(8, 40) to "              VALOR TOTAL",
            SaqueDisplay(0, 0) to "VALOR TOTAL",
            SaqueDisplay(4, 1000) to "VALOR TOTAL",
        )) {
            val fake = FakeSaqueRepository(display = display)
            val owner = SaqueJourney(fake, backgroundScope) {}
            owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("10000")); owner.onEvent(SaqueEvent.Begin); runCurrent()
            assertEquals(1, fake.calls.count { it == "display" })
            assertEquals(1, fake.calls.count { it == "key" })
            assertTrue(fake.calls.indexOf("display") < fake.calls.indexOf("key"))
            assertTrue(fake.calls.indexOf("key") < fake.calls.indexOf("capture"))
            assertFalse(fake.calls.contains("page"))
            assertEquals(SaqueResultado.CAPTURED, owner.state.value.result)
            assertEquals(title + body, fake.displayMessages.single())
            assertTrue(fake.displayMessages.single().toByteArray(Charsets.ISO_8859_1).size <= 160)
        }
    }

    @Test fun `GKY so comeca depois que DEX conclui`() = runTest {
        val displayReady = CompletableDeferred<Unit>()
        val fake = FakeSaqueRepository(pendingDisplay = displayReady)
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("10000")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals(2, owner.state.value.step)
        assertTrue(fake.calls.contains("display"))
        assertFalse(fake.calls.contains("key"))
        assertFalse(fake.calls.contains("capture"))
        displayReady.complete(Unit); runCurrent()
        assertEquals(1, fake.calls.count { it == "key" })
        assertTrue(fake.calls.contains("capture"))
    }

    @Test fun `DEX falha sem GKY nem captura`() = runTest {
        val fake = FakeSaqueRepository(displayFailure = SaqueFailure("DISPLAY_ERROR", "command"))
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("10000")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals("DISPLAY_ERROR", owner.state.value.failureCode)
        assertFalse(fake.calls.contains("key"))
        assertFalse(fake.calls.contains("capture"))
        assertTrue(fake.calls.contains("close"))
    }

    @Test fun `alinhamento opcional nao excede S160 em valores grandes e doze parcelas`() = runTest {
        val fake = FakeSaqueRepository(display = SaqueDisplay(2, 99), maximumBase = MaxAmountCents)
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("500000000000"))
        owner.onEvent(SaqueEvent.Installments("12")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        val message = fake.displayMessages.single()
        assertTrue(message.startsWith("VALOR TOTAL\r"))
        assertTrue(message.contains("QUANTIDADE\r12x"))
        assertTrue(message.toByteArray(Charsets.ISO_8859_1).size <= 160)
        assertEquals(1, fake.calls.count { it == "key" })
        assertTrue(fake.calls.contains("capture"))
    }

    @Test fun `cancelar na confirmacao cancela sem capturar cartao`() = runTest {
        val fake = FakeSaqueRepository(key = 13)
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("10000")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals(1, fake.calls.count { it == "display" })
        assertEquals(1, fake.calls.count { it == "key" })
        assertFalse(fake.calls.contains("capture"))
        assertEquals(SaqueResultado.CANCELED, owner.state.value.result)
        assertEquals("CANCELED", owner.state.value.failureCode)
    }

    @Test fun `tecla inesperada nao captura e cancelamento precoce nao segue a jornada`() = runTest {
        for (key in listOf(4, 5, 6, 7, 8, 13)) {
            val fake = FakeSaqueRepository(key = key)
            val owner = SaqueJourney(fake, backgroundScope) {}
            owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("100")); owner.onEvent(SaqueEvent.Begin); runCurrent()
            assertFalse(fake.calls.contains("capture"))
            assertEquals(5, owner.state.value.step)
            assertFalse(owner.state.value.busy)
            assertEquals(if (key == 13) SaqueResultado.CANCELED else SaqueResultado.ERROR, owner.state.value.result)
            assertEquals(if (key == 13) "CANCELED" else "CONFIRMATION_ERROR", owner.state.value.failureCode)
        }
    }

    @Test fun `falha apos envio e cleanup mantem indeterminado`() = runTest {
        val fake = FakeSaqueRepository(captureFailure = SaqueFailure("TIMEOUT"), closeFailure = true)
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("100")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals(SaqueResultado.UNKNOWN, owner.state.value.result)
        assertTrue(owner.state.value.cleanupFailed)
        assertFalse(fake.calls.contains("tracks"))
        owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals(1, fake.calls.count { it == "capture" })
    }

    @Test fun `cancelar atinge repository bloqueado e detach descarta resposta tardia`() = runTest {
        val fake = FakeSaqueRepository(pendingCapture = CompletableDeferred())
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("100")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals(3, owner.state.value.step)
        owner.onEvent(SaqueEvent.Cancel); runCurrent()
        assertTrue(fake.calls.contains("cancel"))
        owner.dispose(); fake.pendingCapture!!.complete("03"); runCurrent()
        assertEquals(3, owner.state.value.step)
        assertFalse(fake.calls.contains("gox")); assertTrue(fake.calls.contains("close"))
    }

    @Test fun `configuracao ausente ou invalida bloqueia inicio com codigo sanitizado`() = runTest {
        val expected = listOf(
            Triple("MISSING", "Perfil de homologação não provisionado", 10534L),
            Triple("TABLE_MISSING", "Tabela EMV não provisionada", 10534L),
            Triple("CONFIGURATION", "Configuração de homologação inválida", 10534L),
            Triple("CRYPTO_MISSING", "Configuração de criptografia não provisionada", 10534L),
        )
        for ((code, message, total) in expected) {
            val fake = FakeSaqueRepository(profileFailure = code)
            val owner = SaqueJourney(fake, backgroundScope) {}
            owner.load(); runCurrent()
            assertFalse(owner.state.value.configReady)
            assertEquals(code, owner.state.value.failureCode)
            assertEquals("preflight", owner.state.value.failurePhase)
            owner.onEvent(SaqueEvent.Value("10000"))
            owner.onEvent(SaqueEvent.Begin); runCurrent()
            assertTrue("hardware não deve ser alcançado", fake.calls.isEmpty())
            assertNull(owner.state.value.summary)
            assertEquals(0, owner.state.value.step)
            assertTrue(owner.state.value.message, owner.state.value.message.startsWith(message))
        }
        val ready = FakeSaqueRepository()
        val owner = SaqueJourney(ready, backgroundScope) {}
        owner.load(); runCurrent()
        assertTrue(owner.state.value.configReady)
        assertNull(owner.state.value.failureCode)
        assertNull(owner.state.value.failurePhase)
    }

    @Test fun `debito mantem uma parcela com tarifa fixa e retorno e propagado`() = runTest {
        val fake = FakeSaqueRepository()
        var returned = false
        val ready = SaqueJourney(fake, backgroundScope) { returned = true }
        ready.load(); runCurrent()
        ready.onEvent(SaqueEvent.Value("10000")); ready.onEvent(SaqueEvent.Installments("12"))
        ready.onEvent(SaqueEvent.Modalidade(SaqueModalidade.DEBITO))
        assertEquals(1, ready.state.value.summary!!.installments)
        assertEquals(10100L, ready.state.value.summary!!.totalCents)
        assertEquals("R$ 1,00", ready.state.value.feeLabel)
        ready.onEvent(SaqueEvent.Value("0")); assertNull(ready.state.value.summary)
        ready.onEvent(SaqueEvent.Return); runCurrent(); assertTrue(returned)
        assertEquals("close", fake.calls.last())
        assertFalse(fake.calls.contains("cancel"))
    }

    @Test fun `carga acontece na entrada e avancar inicia confirmacao sem repetir preparacao`() = runTest {
        val tableReady = CompletableDeferred<Unit>()
        val fake = FakeSaqueRepository(pendingTable = tableReady)
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); owner.load(); runCurrent()
        assertEquals(listOf("open", "capabilities", "table"), fake.calls)
        assertEquals(0, owner.state.value.step)
        assertTrue(owner.state.value.busy)
        assertFalse(owner.state.value.configReady)
        owner.onEvent(SaqueEvent.Value("10000")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertNull(owner.state.value.summary)
        assertFalse(fake.calls.contains("display"))
        tableReady.complete(Unit); runCurrent()
        assertEquals(1, owner.state.value.step)
        assertTrue(owner.state.value.configReady)
        assertFalse(owner.state.value.busy)
        owner.load(); runCurrent()
        owner.onEvent(SaqueEvent.Value("10000")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals("display", fake.calls[3])
        assertEquals(1, fake.calls.count { it == "open" })
        assertEquals(1, fake.calls.count { it == "table" })
        assertEquals(1, fake.calls.count { it == "capabilities" })
    }

    @Test fun `falha de carga fecha sessao e nao libera formulario ou captura`() = runTest {
        val fake = FakeSaqueRepository(tableFailure = SaqueFailure("PINPAD_ERROR", "command"))
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent()
        assertEquals(listOf("open", "capabilities", "table", "close"), fake.calls)
        assertEquals(0, owner.state.value.step)
        assertFalse(owner.state.value.configReady)
        assertFalse(owner.state.value.busy)
        assertEquals("PINPAD_ERROR", owner.state.value.failureCode)
        owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertFalse(fake.calls.contains("capture"))
    }

    @Test fun `falha de abertura nao carrega tabela e faz cleanup parcial`() = runTest {
        val fake = FakeSaqueRepository(openFailure = SaqueFailure("SERIAL_UNAVAILABLE", "serial"))
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent()
        assertEquals(listOf("open", "close"), fake.calls)
        assertFalse(owner.state.value.configReady)
        assertEquals("SERIAL_UNAVAILABLE", owner.state.value.failureCode)
    }

    @Test fun `cancelamento durante carga impede prontidao e fecha sessao`() = runTest {
        val tableReady = CompletableDeferred<Unit>()
        val fake = FakeSaqueRepository(pendingTable = tableReady)
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent()
        owner.onEvent(SaqueEvent.Cancel); runCurrent()
        assertTrue(fake.calls.contains("cancel"))
        tableReady.complete(Unit); runCurrent()
        assertEquals(0, owner.state.value.step)
        assertFalse(owner.state.value.configReady)
        assertFalse(owner.state.value.busy)
        assertEquals("CANCELED", owner.state.value.failureCode)
        assertTrue(fake.calls.contains("close"))
    }

    @Test fun `detach durante carga descarta resposta tardia e fecha sessao`() = runTest {
        val tableReady = CompletableDeferred<Unit>()
        val fake = FakeSaqueRepository(pendingTable = tableReady)
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent()
        val beforeDetach = owner.state.value
        owner.dispose(); runCurrent(); tableReady.complete(Unit); runCurrent()
        assertEquals(beforeDetach, owner.state.value)
        assertTrue(fake.calls.contains("close"))
        assertFalse(fake.calls.contains("display"))
        owner.load(); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals(1, fake.calls.count { it == "table" })
    }
}

internal fun testProfile() = SaquePerfil("DES", "localhost", 39100, 5000, 5, "01", "TEST000001",
    "01", "02", 100000, "30", 7, SaqueCenario.APPROVE)

/** Fake mantém só metadados e permite pausar uma fase para exercitar concorrência. */
internal class FakeSaqueRepository(
    private val type: String = "00", private val gox: Char = '2', private val fcx: Char = '0',
    private val key: Int = 0, private val display: SaqueDisplay = SaqueDisplay(4, 32),
    private val captureFailure: Exception? = null, private val closeFailure: Boolean = false,
    private val profileFailure: String? = null,
    private val openFailure: Exception? = null,
    private val tableFailure: Exception? = null,
    private val tracksFailure: Exception? = null,
    private val maximumBase: Long = 100000,
    private val displayFailure: Exception? = null,
    val pendingDisplay: CompletableDeferred<Unit>? = null,
    val pendingTable: CompletableDeferred<Unit>? = null,
    val pendingCapture: CompletableDeferred<String>? = null,
) : SaqueRepository {
    val calls = mutableListOf<String>()
    val ids = mutableListOf<String>()
    val pages = mutableListOf<Pair<String, String>>()
    val displayMessages = mutableListOf<String>()
    var capturedSummary: SaqueResumo? = null
    var capturedApplicationTypes: String? = null
    private fun record(name: String, id: String) { calls += name; ids += id }
    override suspend fun loadProfile(): SaquePerfil {
        profileFailure?.let { throw SaqueFailure(it, "preflight") }
        return testProfile().copy(maxBaseCents = maximumBase)
    }
    override suspend fun open(profile: SaquePerfil, operationId: String) { record("open", operationId); openFailure?.let { throw it } }
    override suspend fun displayCapabilities(operationId: String): SaqueDisplay { record("capabilities", operationId); return display }
    override suspend fun loadTable(profile: SaquePerfil, operationId: String) {
        record("table", operationId)
        tableFailure?.let { throw it }
        pendingTable?.await()
    }
    override suspend fun displayText(operationId: String, message: String) {
        displayMessages += message
        record("display", operationId)
        displayFailure?.let { throw it }
        pendingDisplay?.await()
    }
    override suspend fun displayPage(operationId: String, first: String, second: String) {
        pages += first to second
        record("page", operationId)
    }
    override suspend fun waitKey(operationId: String, timeoutSeconds: Int): Int { record("key", operationId); return key }
    override suspend fun capture(profile: SaquePerfil, summary: SaqueResumo, operationId: String): SaqueModoCartao {
        capturedSummary = summary
        capturedApplicationTypes = profile.applicationTypes(summary.modalidade)
        record("capture", operationId); captureFailure?.let { throw it }
        return SaqueModoCartao.fromCardType(pendingCapture?.await() ?: type)
    }
    override suspend fun capturePin(operationId: String) { record("pin", operationId) }
    override suspend fun tracks(profile: SaquePerfil, operationId: String) {
        record("tracks", operationId); tracksFailure?.let { throw it }
    }
    override suspend fun continueEmv(profile: SaquePerfil, summary: SaqueResumo, operationId: String): Char { record("gox", operationId); return gox }
    override suspend fun finalizeEmv(profile: SaquePerfil, operationId: String): Char { record("fcx", operationId); return fcx }
    override suspend fun cancel(operationId: String) { record("cancel", operationId) }
    override suspend fun close(operationId: String) { record("close", operationId); if (closeFailure) throw SaqueFailure("TIMEOUT") }
}
