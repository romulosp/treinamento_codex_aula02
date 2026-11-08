package br.com.romulopenha.sistemaprototipoandroid.pluginingressos

import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalStatus
import org.junit.Assert.*
import org.junit.Test

/** Contratos de geometria, compra múltipla e estoque das apresentações demonstrativas. */
class EventJourneyTest {
    @Test fun cinemaTem180PosicoesEspeciaisECorredoresSemConsumirLugares() {
        val layout = VenueLayout.cinema()
        assertEquals(180, layout.places.size)
        assertEquals(180, layout.places.map { it.id }.distinct().size)
        assertEquals(('A'..'L').toList(), layout.places.map { it.row }.distinct())
        assertTrue(layout.places.groupBy { it.row }.values.all { it.map { p -> p.number } == (1..15).toList() })
        assertEquals(setOf(5, 10), layout.aisleAfter)
        assertEquals(setOf("A1", "A3", "A13", "A15"), layout.places.filter { it.kind == PlaceKind.WHEELCHAIR }.map { it.id }.toSet())
        assertEquals(setOf("A2", "A4", "A12", "A14"), layout.places.filter { it.kind == PlaceKind.COMPANION }.map { it.id }.toSet())
        assertEquals(setOf("L1", "L2", "L14", "L15"), layout.places.filter { it.kind == PlaceKind.COUPLE }.map { it.id }.toSet())
    }

    @Test fun namoroAlternaParInteiroECobraDoisIngressos() {
        val j = cinema()
        j.select("L1")
        assertEquals(setOf("L1", "L2"), j.state.selectedSeats)
        assertEquals(8800L, j.currentLines().sumOf { it.total })
        j.select("L2")
        assertTrue(j.state.selectedSeats.isEmpty())
        j.select("L15")
        j.createOrder()
        assertEquals(setOf("L14", "L15"), j.state.order!!.seats)
        assertEquals(8800L, j.state.order!!.total)
    }

    @Test fun pcdEAcompanhanteSaoCompradosIndependentemente() {
        val j = cinema()
        j.select("A1")
        assertEquals(setOf("A1"), j.state.selectedSeats)
        j.select("A2")
        assertEquals(setOf("A1", "A2"), j.state.selectedSeats)
        j.select("A1")
        assertEquals(setOf("A2"), j.state.selectedSeats)
    }

    @Test fun pedidoMistoCongelaItensEValorEPreservaEstoqueNoNovoAtendimento() {
        val j = show()
        j.select("A1"); j.select("B3")
        j.setStanding("pista", 3); j.setStanding("vip", 2)
        j.createOrder()
        val order = requireNotNull(j.state.order)
        assertEquals(57000L, order.total)
        assertEquals(7, order.lines.sumOf { it.quantity })
        assertEquals("show-demo-2100", order.presentationId)
        j.setStanding("pista", 99); j.select("C1"); j.selectMovie("batman"); j.createOrder()
        assertEquals(order, j.state.order)
        assertTrue(j.beginPayment())
        j.paymentCompleted("stale", CardWithdrawalStatus.CAPTURED, true)
        assertTrue(j.state.paymentInProgress)
        j.paymentCompleted(order.id, CardWithdrawalStatus.CAPTURED, true)
        val completed = j.state
        j.paymentCompleted(order.id, CardWithdrawalStatus.CAPTURED, true)
        assertEquals(completed, j.state)
        assertEquals(order.seats, j.state.sold)
        assertEquals(mapOf("pista" to 3, "vip" to 2), j.state.soldStanding)
        j.startNewAttendance(order.id); j.selectMovie("batman")
        assertTrue(j.state.sold.isEmpty()); assertTrue(j.state.soldStanding.isEmpty())
        j.selectMovie("show")
        assertEquals(order.seats, j.state.sold)
        assertEquals(mapOf("pista" to 3, "vip" to 2), j.state.soldStanding)
    }

    @Test fun pistaRespeitaCapacidadeReduzidaPorConfiguracaoEVendas() {
        val j = show()
        j.setStanding("pista", -1); j.setStanding("missing", 1); j.setStanding("pista", 101)
        assertTrue(j.state.standing.isEmpty())
        j.setStanding("pista", 100); j.createOrder(); complete(j)
        j.startNewAttendance(requireNotNull(j.state.completedOrderId)); j.selectMovie("show")
        j.setStanding("pista", 1)
        assertTrue(j.state.standing.isEmpty())
        j.setStanding("vip", 2); j.setStanding("vip", 0)
        assertTrue(j.state.standing.isEmpty())
    }

    @Test fun cancelamentoEFalhasNaoConsomemEstoqueMisto() {
        CardWithdrawalStatus.entries.forEach { status ->
            listOf(false, true).forEach { cleanup ->
                val j = show()
                j.select("A1"); j.setStanding("pista", 3); j.createOrder()
                val order = j.state.order!!
                j.beginPayment(); j.paymentCompleted(order.id, status, cleanup)
                val success = cleanup && status in setOf(CardWithdrawalStatus.APPROVED, CardWithdrawalStatus.CAPTURED)
                assertEquals(success, j.state.completedOrderId != null)
                if (!success) {
                    assertTrue(j.state.sold.isEmpty()); assertTrue(j.state.soldStanding.isEmpty())
                    assertEquals(order, j.state.order)
                    j.discard(false)
                    assertNull(j.state.order)
                }
            }
        }
    }

    @Test fun casalVendidoContinuaIndisponivelNoNovoAtendimento() {
        val j = cinema(); j.select("L1"); j.createOrder(); complete(j)
        j.startNewAttendance(requireNotNull(j.state.completedOrderId)); j.selectMovie("batman"); j.select("L2")
        assertTrue(j.state.selectedSeats.isEmpty())
        assertEquals(setOf("L1", "L2"), j.state.sold)
    }

    @Test fun pistaPuraFuncionaSemMapaNumerado() {
        val session = DemoCinemaCatalog.catalog.movies.first().session.copy(layout = VenueLayout(emptyList(), emptySet()), occupied = emptySet(),
            sectors = listOf(StandingSector("geral", "Geral", 10, 1550, 2)))
        val j = configured(session)
        j.select("A1"); j.createOrder(); assertNull(j.state.order)
        j.setStanding("geral", 9); assertTrue(j.state.standing.isEmpty())
        j.setStanding("geral", 8); j.createOrder()
        assertEquals(12400L, j.state.order!!.total)
        complete(j)
        assertEquals(mapOf("geral" to 8), j.state.soldStanding)
    }

    @Test fun limiteFinanceiroECalculadoEmLongERejeitaPedidoExcessivo() {
        val sector = StandingSector("large", "Grande", Int.MAX_VALUE, 99999)
        val j = configured(DemoCinemaCatalog.catalog.movies.first().session.copy(sectors = listOf(sector)))
        j.setStanding("large", 10_000_000); j.createOrder()
        assertEquals(999990000000L, j.state.order!!.total)
        assertEquals("R$ 9999900000,00", formatPrice(j.state.order!!.total))
        j.discard(false); j.setStanding("large", Int.MAX_VALUE); j.createOrder()
        assertNull(j.state.order); assertTrue(j.state.soldStanding.isEmpty())
    }

    @Test fun configuracaoRejeitaMapasParesESetoresInvalidos() {
        val base = DemoCinemaCatalog.catalog.movies.first().session
        val places = base.layout.places
        val invalid = listOf(
            base.copy(date = ""), base.copy(venue = ""),
            base.copy(layout = VenueLayout(emptyList(), emptySet())),
            base.copy(layout = base.layout.copy(places = places + places.first())),
            base.copy(layout = base.layout.copy(aisleAfter = setOf(15))),
            base.copy(layout = base.layout.copy(places = places.map { if (it.id == "A1") it.copy(id = "wrong") else it })),
            base.copy(layout = base.layout.copy(places = places.map { if (it.id == "L1") it.copy(partner = "L15") else it })),
            base.copy(layout = base.layout.copy(places = places.map { if (it.id == "A1") it.copy(partner = "A4") else it })),
            base.copy(layout = base.layout.copy(places = places.map { if (it.id == "B1") it.copy(partner = "B2") else it })),
            base.copy(occupied = setOf("L1")),
            base.copy(sectors = listOf(StandingSector("s", "S", 0, 100))),
            base.copy(sectors = listOf(StandingSector("s", "S", 2, 0))),
            base.copy(sectors = listOf(StandingSector("s", "S", 2, 100, 3))),
            base.copy(sectors = listOf(StandingSector("", "S", 2, 100))),
            base.copy(sectors = listOf(StandingSector("s", "", 2, 100))),
            base.copy(sectors = listOf(StandingSector("A1", "S", 2, 100))),
            base.copy(sectors = listOf(StandingSector("s", "S", 2, 100), StandingSector("s", "S", 2, 100)))
        )
        invalid.forEach { session -> assertThrows(IllegalArgumentException::class.java) { configured(session) } }
        val occupiedPair = configured(base.copy(occupied = setOf("L1", "L2")))
        occupiedPair.select("L2"); assertTrue(occupiedPair.state.selectedSeats.isEmpty())
    }

    @Test fun demosTemCinemaTeatroEShowComCapacidadesProprias() {
        val movies = DemoCinemaCatalog.catalog.movies
        assertEquals(EventKind.entries.toSet(), movies.map { it.kind }.toSet())
        assertEquals(60, movies.single { it.kind == EventKind.THEATRE }.session.layout.places.size)
        assertEquals(40, movies.single { it.kind == EventKind.SHOW }.session.layout.places.size)
        assertThrows(IllegalArgumentException::class.java) { VenueLayout.regular(0, 15) }
        assertThrows(IllegalArgumentException::class.java) { VenueLayout.regular(2, 0) }
    }

    private fun cinema() = CinemaJourney().apply { selectMovie("batman") }
    private fun show() = CinemaJourney().apply { selectMovie("show") }
    private fun configured(session: CinemaConfiguredSession) = CinemaJourney(CinemaCatalog("Teste", listOf(CinemaMovie("test", "Teste", session)))).apply { selectMovie("test") }
    private fun complete(j: CinemaJourney) {
        j.beginPayment(); j.paymentCompleted(requireNotNull(j.state.pendingOrderId), CardWithdrawalStatus.CAPTURED, true)
    }
}
