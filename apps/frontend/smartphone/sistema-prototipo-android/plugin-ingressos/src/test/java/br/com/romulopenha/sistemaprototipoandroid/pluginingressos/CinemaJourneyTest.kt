package br.com.romulopenha.sistemaprototipoandroid.pluginingressos

import org.junit.Assert.*
import org.junit.Test
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalStatus

/** Exercita programação fixa, estoque por sessão e limites do pagamento demonstrativo. */
class CinemaJourneyTest {
    private val first = CinemaMovie("first", "Exemplo", CinemaConfiguredSession(
        "session-first", "Sala 1", "20h", 1250, setOf("A2")))
    private val second = CinemaMovie("second", "Outro filme", CinemaConfiguredSession(
        "session-second", "Sala 2", "21h", 3000, setOf("B1")))
    private val catalog = CinemaCatalog("Cinema de teste", listOf(first, second))

    @Test fun selecionarFilmeCarregaConfiguracaoSemCadastroOuEscolhaDeHorario() {
        val j = CinemaJourney(catalog)
        assertNull(j.state.session)
        j.selectMovie("first")
        assertEquals("first", j.state.movieId)
        assertEquals("session-first", j.state.sessionId)
        assertEquals(CinemaSession("Exemplo", "Sala 1", "20h", 1250), j.state.session)
        assertEquals(setOf("A2"), j.state.occupied)
        assertTrue(j.state.sold.isEmpty())
        assertNull(j.state.pending)
        assertFalse(j.state.paymentInProgress)
    }

    @Test fun filmeDesconhecidoNaoSubstituiSessaoOuSelecao() {
        val j = CinemaJourney(catalog)
        j.selectMovie("unknown")
        assertNull(j.state.session)
        j.selectMovie("first")
        j.select("A1")
        j.selectMovie("unknown")
        assertEquals("first", j.state.movieId)
        assertEquals("A1", j.state.selected)
    }

    @Test fun selecionarFilmeAtualNaoApagaAssentoNemPedido() {
        val j = preparedJourney()
        j.select("A1")
        val selection = j.state
        j.selectMovie("first")
        assertEquals(selection, j.state)
        j.createOrder()
        val pending = j.state
        j.selectMovie("first")
        assertEquals(pending, j.state)
    }

    @Test fun ocupadosDesconhecidosESemFilmeNaoPermitemPedido() {
        val j = CinemaJourney(catalog)
        j.select("A1")
        j.createOrder()
        j.discard(false)
        assertNull(j.state.pending)
        j.selectMovie("first")
        listOf("A2", "Z9").forEach {
            j.select(it)
            assertNull(j.state.selected)
            j.createOrder()
            assertNull(j.state.pending)
        }
        assertTrue(j.state.sold.isEmpty())
        assertEquals(setOf("A2"), j.state.occupied)
    }

    @Test fun pedidoCongelaFilmeAssentoPrecoEId() {
        val j = preparedJourney()
        j.select("A1")
        j.createOrder()
        val id = requireNotNull(j.state.pendingOrderId)
        j.selectMovie("second")
        j.select("B2")
        j.createOrder()
        assertEquals(id, j.state.pendingOrderId)
        assertEquals("A1", j.state.pending)
        assertEquals("first", j.state.movieId)
        assertEquals(1250, j.state.session!!.price)
        assertTrue(j.beginPayment())
        assertFalse(j.beginPayment())
        j.selectMovie("second")
        j.discard(false)
        assertEquals(id, j.state.pendingOrderId)
        assertEquals(1250, j.state.session!!.price)
        j.paymentCompleted(id, CardWithdrawalStatus.APPROVED, true)
        assertEquals(setOf("A1"), j.state.sold)
        assertEquals(id, j.state.completedOrderId)
        val completed = j.state
        j.paymentCompleted(id, CardWithdrawalStatus.APPROVED, true)
        assertEquals(completed, j.state)
    }

    @Test fun trocarFilmePreservaVendasIsoladasPorSessao() {
        val j = preparedJourney()
        j.select("A1")
        j.createOrder()
        j.beginPayment()
        j.paymentCompleted(requireNotNull(j.state.pendingOrderId), CardWithdrawalStatus.APPROVED, true)
        j.startNewAttendance(requireNotNull(j.state.completedOrderId))
        j.selectMovie("second")
        assertEquals(3000, j.state.session!!.price)
        assertEquals(setOf("B1"), j.state.occupied)
        assertTrue(j.state.sold.isEmpty())
        j.select("A1")
        assertEquals("A1", j.state.selected)
        j.selectMovie("first")
        assertNull(j.state.selected)
        assertEquals(setOf("A1"), j.state.sold)
        assertEquals(setOf("A2"), j.state.occupied)
        j.select("A1")
        assertNull(j.state.selected)
        j.selectMovie("second")
        assertTrue(j.state.sold.isEmpty())
    }

    @Test fun cancelamentoMantemProgramacaoSemVenderELiberaTroca() {
        val j = preparedJourney()
        j.select("A1")
        j.createOrder()
        j.discard(false)
        assertNull(j.state.pending)
        assertNull(j.state.pendingOrderId)
        assertEquals("first", j.state.movieId)
        assertTrue(j.state.sold.isEmpty())
        j.selectMovie("second")
        assertEquals("second", j.state.movieId)
        j.select("B2")
        j.createOrder()
        j.discard(true)
        assertTrue(j.state.sold.isEmpty())
    }

    @Test fun somenteAprovacaoOuCapturaComCleanupConcluemDemonstracao() {
        CardWithdrawalStatus.entries.forEach { status ->
            val j = preparedJourney()
            j.select("B2")
            j.createOrder()
            j.paymentCompleted(requireNotNull(j.state.pendingOrderId), CardWithdrawalStatus.APPROVED, true)
            assertTrue(j.state.sold.isEmpty())
            assertTrue(j.beginPayment())
            j.paymentCompleted(requireNotNull(j.state.pendingOrderId), status, true)
            val concludesDemo = status in setOf(CardWithdrawalStatus.APPROVED, CardWithdrawalStatus.CAPTURED)
            assertEquals(concludesDemo, "B2" in j.state.sold)
            assertEquals(concludesDemo, j.state.completedOrderId != null)
        }
        listOf(CardWithdrawalStatus.APPROVED, CardWithdrawalStatus.CAPTURED).forEach { status ->
            val j = preparedJourney()
            j.select("B2")
            j.createOrder()
            j.beginPayment()
            j.paymentCompleted(requireNotNull(j.state.pendingOrderId), status, false)
            assertTrue(j.state.sold.isEmpty())
            assertNull(j.state.completedOrderId)
        }
    }

    @Test fun sucessoBloqueiaAcoesEResetaSomenteConclusaoAtualPreservandoVendas() {
        val j = preparedJourney()
        j.select("A1")
        j.createOrder()
        val id = requireNotNull(j.state.pendingOrderId)
        j.beginPayment()
        j.paymentCompleted(requireNotNull(j.state.pendingOrderId), CardWithdrawalStatus.APPROVED, true)
        assertEquals(TRANSACTION_SUCCESS_MESSAGE, j.state.message)
        val success = j.state
        j.selectMovie("second")
        j.select("B2")
        j.createOrder()
        j.discard(false)
        j.abandonPayment()
        assertFalse(j.beginPayment())
        j.startNewAttendance("stale")
        assertEquals(success, j.state)
        j.startNewAttendance(id)
        assertEquals(CinemaState(), j.state)
        j.startNewAttendance(id)
        j.selectMovie("first")
        assertEquals(setOf("A1"), j.state.sold)
        assertEquals(setOf("A2"), j.state.occupied)
        j.select("B2")
        j.createOrder()
        val newId = requireNotNull(j.state.pendingOrderId)
        assertNotEquals(id, newId)
        j.startNewAttendance(id)
        assertEquals(newId, j.state.pendingOrderId)
        assertEquals("B2", j.state.pending)
    }

    @Test fun resultadoNegadoNaoAutorizaResetComIdDoPedido() {
        val j = preparedJourney()
        j.select("A1")
        j.createOrder()
        val id = requireNotNull(j.state.pendingOrderId)
        j.beginPayment()
        j.paymentCompleted(requireNotNull(j.state.pendingOrderId), CardWithdrawalStatus.DENIED, true)
        j.startNewAttendance(id)
        assertEquals(id, j.state.pendingOrderId)
        assertEquals("first", j.state.movieId)
        assertNull(j.state.completedOrderId)
        assertTrue(j.state.sold.isEmpty())
    }

    @Test fun callbackAntigoNaoConcluiPagamentoDoNovoAtendimento() {
        val j = preparedJourney()
        j.select("A1")
        j.createOrder()
        val oldId = requireNotNull(j.state.pendingOrderId)
        j.beginPayment()
        j.paymentCompleted(oldId, CardWithdrawalStatus.APPROVED, true)
        j.startNewAttendance(oldId)
        j.selectMovie("first")
        j.select("B2")
        j.createOrder()
        val newId = requireNotNull(j.state.pendingOrderId)
        j.beginPayment()
        val pending = j.state
        j.paymentCompleted(oldId, CardWithdrawalStatus.APPROVED, true)
        assertEquals(pending, j.state)
        j.paymentCompleted(newId, CardWithdrawalStatus.APPROVED, true)
        assertEquals(newId, j.state.completedOrderId)
        assertEquals(setOf("A1", "B2"), j.state.sold)
    }

    @Test fun indisponibilidadePermiteNovaTentativaMasDesconhecidoNao() {
        val j = preparedJourney()
        j.abandonPayment()
        assertFalse(j.beginPayment())
        j.select("A1")
        j.createOrder()
        assertTrue(j.beginPayment())
        j.paymentCompleted(requireNotNull(j.state.pendingOrderId), CardWithdrawalStatus.UNAVAILABLE, true)
        assertTrue(j.beginPayment())
        j.abandonPayment()
        assertEquals(CardWithdrawalStatus.UNKNOWN, j.state.paymentStatus)
        assertFalse(j.beginPayment())
        j.selectMovie("second")
        assertEquals("first", j.state.movieId)
    }

    @Test fun configuracaoInconsistenteERejeitada() {
        val invalid = listOf(
            catalog.copy(cinema = ""),
            catalog.copy(movies = emptyList()),
            catalog.copy(movies = listOf(first, first)),
            catalog.copy(movies = listOf(first, second.copy(session = second.session.copy(id = first.session.id)))),
            catalog.copy(movies = listOf(first.copy(id = ""))),
            catalog.copy(movies = listOf(first.copy(title = ""))),
            catalog.copy(movies = listOf(first.copy(session = first.session.copy(id = "")))),
            catalog.copy(movies = listOf(first.copy(session = first.session.copy(room = "")))),
            catalog.copy(movies = listOf(first.copy(session = first.session.copy(time = "")))),
            catalog.copy(movies = listOf(first.copy(session = first.session.copy(price = 0)))),
            catalog.copy(movies = listOf(first.copy(session = first.session.copy(price = 100000)))),
            catalog.copy(movies = listOf(first.copy(session = first.session.copy(occupied = setOf("Z9"))))),
        )
        invalid.forEach { config -> assertThrows(IllegalArgumentException::class.java) { CinemaJourney(config) } }
    }

    @Test fun pluginMantemFolhaEFormatacaoExata() {
        val plugin = PluginIngressosApp()
        assertEquals("Principal > Venda de ingressos", plugin.getCaminhoMenu())
        assertEquals("ingressos", plugin.businessMenuItems.single().id)
        assertEquals(setOf("business-menu"), plugin.manifest.capabilities)
        assertEquals("R$ 999,99", formatPrice(99999))
    }

    private fun preparedJourney() = CinemaJourney(catalog).apply { selectMovie("first") }
}
