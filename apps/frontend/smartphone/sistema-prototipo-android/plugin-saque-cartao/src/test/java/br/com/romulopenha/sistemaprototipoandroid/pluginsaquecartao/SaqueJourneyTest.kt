package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import java.math.BigDecimal
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/** Cenários sintéticos: não representam evidência de hardware. */
@OptIn(ExperimentalCoroutinesApi::class)
class SaqueJourneyTest {
    @Test fun `edicao sem perfil recalcula e carregamento aplica limite externo`() = runTest {
        val fake = FakeSaqueRepository()
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.onEvent(SaqueEvent.Value("100"))
        owner.onEvent(SaqueEvent.Installments("2"))
        assertEquals(10614L, owner.state.value.summary!!.totalCents)
        assertEquals("6,14%", owner.state.value.feeLabel)
        owner.onEvent(SaqueEvent.Value("2000"))
        assertNotNull(owner.state.value.summary)
        owner.load(); runCurrent()
        assertNull(owner.state.value.summary)
        owner.onEvent(SaqueEvent.Value("200"))
        assertEquals(21228L, owner.state.value.summary!!.totalCents)
        assertTrue(fake.calls.isEmpty())
    }

    @Test fun `modalidade congelada e total seguem para captura com filtro correto`() = runTest {
        for (mode in SaqueModalidade.entries) {
            val fake = FakeSaqueRepository()
            val owner = SaqueJourney(fake, backgroundScope) {}
            owner.load(); runCurrent()
            owner.onEvent(SaqueEvent.Value("100"))
            owner.onEvent(SaqueEvent.Modalidade(mode))
            owner.onEvent(SaqueEvent.Begin); runCurrent()
            assertEquals(mode, fake.capturedSummary!!.modalidade)
            assertEquals("000000010534", fake.capturedSummary!!.amountN12())
            assertEquals(if (mode == SaqueModalidade.CREDITO) "01" else "02", fake.capturedApplicationTypes)
        }
    }

    @Test fun `tarja captura PIN antes de GTK e nao envia EMV`() = runTest {
        val fake = FakeSaqueRepository()
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent()
        owner.onEvent(SaqueEvent.Value("150,00"))
        owner.onEvent(SaqueEvent.Begin); owner.onEvent(SaqueEvent.Begin)
        runCurrent()
        assertEquals(SaqueResultado.CAPTURED, owner.state.value.result)
        assertEquals(15801L, owner.state.value.summary!!.totalCents)
        assertEquals(listOf("open", "capabilities", "table", "display", "key", "capture", "pin", "tracks", "cancel", "close"), fake.calls)
        assertEquals(1, fake.ids.distinct().size)
        assertTrue(fake.ids.all { it.isNotBlank() })
    }

    @Test fun `EMV online usa FCX e decisao real`() = runTest {
        val fake = FakeSaqueRepository(type = "03", gox = '2', fcx = '1')
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("10")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals(SaqueResultado.DENIED, owner.state.value.result)
        assertFalse(fake.calls.contains("pin"))
        assertEquals(listOf("tracks", "gox", "fcx", "cancel", "close"), fake.calls.takeLast(5))
    }

    @Test fun `EMV offline nao envia FCX e CTLS simulado nao exige PIN`() = runTest {
        for (type in listOf("03", "05", "06")) {
            val fake = FakeSaqueRepository(type = type, gox = '0')
            val owner = SaqueJourney(fake, backgroundScope) {}
            owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("10")); owner.onEvent(SaqueEvent.Begin); runCurrent()
            assertFalse(fake.calls.contains("fcx")); assertFalse(fake.calls.contains("pin"))
            assertEquals(if (type == "06") SaqueResultado.CAPTURED else SaqueResultado.APPROVED, owner.state.value.result)
        }
    }

    @Test fun `display pequeno exige tres confirmacoes reais`() = runTest {
        val fake = FakeSaqueRepository(display = SaqueDisplay(2, 16))
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("10")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals(3, fake.calls.count { it == "page" }); assertEquals(3, fake.calls.count { it == "key" })
        assertEquals(SaqueResultado.CAPTURED, owner.state.value.result)
    }

    @Test fun `tecla inesperada nao captura e cancelamento precoce nao conclui etapas`() = runTest {
        for (key in listOf(4, 13)) {
            val fake = FakeSaqueRepository(key = key)
            val owner = SaqueJourney(fake, backgroundScope) {}
            owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("10")); owner.onEvent(SaqueEvent.Begin); runCurrent()
            assertFalse(fake.calls.contains("capture"))
            assertEquals(setOf(1), owner.state.value.completedSteps)
            assertEquals(if (key == 13) SaqueResultado.CANCELED else SaqueResultado.ERROR, owner.state.value.result)
        }
    }

    @Test fun `falha apos envio e cleanup mantem indeterminado`() = runTest {
        val fake = FakeSaqueRepository(captureFailure = SaqueFailure("TIMEOUT"), closeFailure = true)
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("10")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals(SaqueResultado.UNKNOWN, owner.state.value.result)
        assertTrue(owner.state.value.cleanupFailed)
        assertFalse(fake.calls.contains("tracks"))
        owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals(1, fake.calls.count { it == "capture" })
    }

    @Test fun `cancelar atinge repository bloqueado e detach descarta resposta tardia`() = runTest {
        val fake = FakeSaqueRepository(pendingCapture = CompletableDeferred())
        val owner = SaqueJourney(fake, backgroundScope) {}
        owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("10")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertEquals(3, owner.state.value.step)
        owner.onEvent(SaqueEvent.Cancel); runCurrent()
        assertTrue(fake.calls.contains("cancel"))
        owner.dispose(); fake.pendingCapture!!.complete("03"); runCurrent()
        assertEquals(3, owner.state.value.step)
        assertFalse(fake.calls.contains("gox")); assertTrue(fake.calls.contains("close"))
    }

    @Test fun `perfil ausente bloqueia hardware e debito recalcula parcela`() = runTest {
        val missing = FakeSaqueRepository(profileFailure = true)
        val owner = SaqueJourney(missing, backgroundScope) {}
        owner.load(); runCurrent(); owner.onEvent(SaqueEvent.Value("10")); owner.onEvent(SaqueEvent.Begin); runCurrent()
        assertFalse(owner.state.value.configReady); assertTrue(missing.calls.isEmpty())
        assertEquals(1053L, owner.state.value.summary!!.totalCents)
        val fake = FakeSaqueRepository()
        var returned = false
        val ready = SaqueJourney(fake, backgroundScope) { returned = true }
        ready.load(); runCurrent(); ready.onEvent(SaqueEvent.Value("10")); ready.onEvent(SaqueEvent.Installments("12"))
        ready.onEvent(SaqueEvent.Modalidade(SaqueModalidade.DEBITO))
        assertEquals(1, ready.state.value.summary!!.installments)
        ready.onEvent(SaqueEvent.Value("0")); assertNull(ready.state.value.summary)
        ready.onEvent(SaqueEvent.Return); assertTrue(returned)
    }
}

internal fun testProfile() = SaquePerfil("DES", "localhost", 39100, 5000, 5, "01", "TEST000001",
    "01", "02", 100000, "30", 7, SaqueCenario.APPROVE)

/** Fake mantém só metadados e permite pausar uma fase para exercitar concorrência. */
internal class FakeSaqueRepository(
    private val type: String = "00", private val gox: Char = '2', private val fcx: Char = '0',
    private val key: Int = 0, private val display: SaqueDisplay = SaqueDisplay(4, 32),
    private val captureFailure: Exception? = null, private val closeFailure: Boolean = false,
    private val profileFailure: Boolean = false,
    val pendingCapture: CompletableDeferred<String>? = null,
) : SaqueRepository {
    val calls = mutableListOf<String>()
    val ids = mutableListOf<String>()
    var capturedSummary: SaqueResumo? = null
    var capturedApplicationTypes: String? = null
    private fun record(name: String, id: String) { calls += name; ids += id }
    override suspend fun loadProfile(): SaquePerfil { if (profileFailure) throw SaqueFailure("CONFIGURATION"); return testProfile() }
    override suspend fun open(profile: SaquePerfil, operationId: String) { record("open", operationId) }
    override suspend fun displayCapabilities(operationId: String): SaqueDisplay { record("capabilities", operationId); return display }
    override suspend fun loadTable(profile: SaquePerfil, operationId: String) { record("table", operationId) }
    override suspend fun displayText(operationId: String, message: String) { record("display", operationId) }
    override suspend fun displayPage(operationId: String, first: String, second: String) { record("page", operationId) }
    override suspend fun waitKey(operationId: String, timeoutSeconds: Int): Int { record("key", operationId); return key }
    override suspend fun capture(profile: SaquePerfil, summary: SaqueResumo, operationId: String): String {
        capturedSummary = summary
        capturedApplicationTypes = profile.applicationTypes(summary.modalidade)
        record("capture", operationId); captureFailure?.let { throw it }; return pendingCapture?.await() ?: type
    }
    override suspend fun capturePin(operationId: String) { record("pin", operationId) }
    override suspend fun tracks(profile: SaquePerfil, operationId: String) { record("tracks", operationId) }
    override suspend fun continueEmv(profile: SaquePerfil, summary: SaqueResumo, operationId: String): Char { record("gox", operationId); return gox }
    override suspend fun finalizeEmv(profile: SaquePerfil, operationId: String): Char { record("fcx", operationId); return fcx }
    override suspend fun cancel(operationId: String) { record("cancel", operationId) }
    override suspend fun close(operationId: String) { record("close", operationId); if (closeFailure) throw SaqueFailure("TIMEOUT") }
}
