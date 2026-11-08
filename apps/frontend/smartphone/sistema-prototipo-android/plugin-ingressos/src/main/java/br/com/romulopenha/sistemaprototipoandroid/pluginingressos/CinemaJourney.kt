package br.com.romulopenha.sistemaprototipoandroid.pluginingressos

import java.util.UUID
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalStatus

/** Texto da confirmação local solicitada para encerrar o atendimento demonstrativo. */
internal const val TRANSACTION_SUCCESS_MESSAGE = "Transação Realizada com Sucesso"

/** Dados fictícios de uma sessão local; o preço está em centavos. */
internal data class CinemaSession(val film: String, val room: String, val time: String, val price: Int, val date: String = "08/11/2026", val venue: String = "Centro cultural demonstração",
    val layout: VenueLayout = VenueLayout.cinema(), val sectors: List<StandingSector> = emptyList(),
    val kind: EventKind = EventKind.CINEMA)

/**
 * Instantâneo da demonstração, sem informações financeiras ou pessoais.
 * @property movieId evento selecionado no catálogo local
 * @property selectedSeats posições editáveis, incluindo ambos os lugares de cada casal
 * @property standing quantidades editáveis por setor sem numeração
 * @property soldStanding quantidades já vendidas na apresentação atual
 * @property order snapshot do pedido, fonte única de itens e total durante o pagamento
 * @property sessionId sessão fixa associada ao filme e ao estoque local
 * @property sold ingressos emitidos localmente somente na sessão atual
 * @property occupied ocupados da configuração, sem ingresso emitido nesta instância
 * @property completedOrderId pedido concluído localmente, usado para proteger o retorno ao início
 */
internal data class CinemaState(
    val movieId: String? = null,
    val sessionId: String? = null,
    val session: CinemaSession? = null,
    val selectedSeats: Set<String> = emptySet(),
    val standing: Map<String, Int> = emptyMap(),
    val soldStanding: Map<String, Int> = emptyMap(),
    val order: TicketOrder? = null,
    val sold: Set<String> = emptySet(),
    val occupied: Set<String> = emptySet(),
    val paymentInProgress: Boolean = false,
    val paymentStatus: CardWithdrawalStatus? = null,
    val cleanupConfirmed: Boolean = false,
    val completedOrderId: String? = null,
    val message: String = "",
) {
    /** Acessores derivados para integrações internas anteriores; o pedido é a única fonte de verdade. */
    val selected: String? get() = selectedSeats.firstOrNull()
    val pending: String? get() = order?.let { it.seats.firstOrNull() ?: "pista" }
    val pendingOrderId: String? get() = order?.id
}

/**
 * Mantém seleção, pedido e vendas voláteis por sessão configurada no cinema.
 * @property catalog configuração local validada, sem campos editáveis pelo operador
 * @throws IllegalArgumentException se a configuração contém IDs/dados inválidos ou lugares desconhecidos
 */
internal class CinemaJourney(val catalog: CinemaCatalog = DemoCinemaCatalog.catalog) {
    /** Vendas locais conservadas ao trocar de filme; perde-se com a instância, sem persistência. */
    private val soldBySession = mutableMapOf<String, Set<String>>()
    /** Quantidades vendidas em pista por apresentação, confirmadas junto com as posições numeradas. */
    private val standingBySession = mutableMapOf<String, Map<String, Int>>()

    init {
        require(catalog.cinema.isNotBlank() && catalog.movies.isNotEmpty()) { "Configure um catálogo com eventos." }
        require(catalog.movies.map { it.id }.distinct().size == catalog.movies.size) { "Eventos duplicados." }
        require(catalog.movies.map { it.session.id }.distinct().size == catalog.movies.size) { "Sessões duplicadas." }
        catalog.movies.forEach { movie ->
            require(movie.id.isNotBlank() && movie.title.isNotBlank()) { "Evento sem identificação." }
            val session = movie.session
            require(session.id.isNotBlank() && session.room.isNotBlank() && session.time.isNotBlank()) { "Sessão incompleta." }
            require(session.price in 1..99999) { "Preço base fora dos limites demonstrativos." }
            require(session.date.isNotBlank() && session.venue.isNotBlank()) { "Local ou data ausente." }
            val places = session.layout.places
            val ids = places.map { it.id }
            require(ids.distinct().size == ids.size) { "Posições duplicadas." }
            require(places.isNotEmpty() || session.sectors.isNotEmpty()) { "Apresentação sem lugares ou setores." }
            require(places.all { it.row in 'A'..'Z' && it.number > 0 && it.id == "${it.row}${it.number}" }) { "Numeração inválida." }
            require(session.layout.aisleAfter.all { n -> n > 0 && places.any { it.number > n } }) { "Corredor inválido." }
            require(session.occupied.all { it in ids }) { "Lugar configurado desconhecido." }
            places.forEach { place ->
                val partner = places.find { it.id == place.partner }
                when (place.kind) {
                    PlaceKind.STANDARD -> require(place.partner == null)
                    PlaceKind.COUPLE -> {
                        require(partner != null && partner.kind == PlaceKind.COUPLE && partner.partner == place.id && partner.row == place.row && kotlin.math.abs(partner.number - place.number) == 1)
                        require((place.id in session.occupied) == (partner.id in session.occupied)) { "Par parcialmente ocupado." }
                    }
                    PlaceKind.WHEELCHAIR, PlaceKind.COMPANION -> {
                        require(partner != null && partner.partner == place.id && partner.row == place.row)
                        require(partner.kind == if (place.kind == PlaceKind.WHEELCHAIR) PlaceKind.COMPANION else PlaceKind.WHEELCHAIR)
                    }
                }
            }
            require(session.sectors.map { it.id }.distinct().size == session.sectors.size) { "Setores duplicados." }
            require(session.sectors.none { it.id in ids }) { "Setor com ID de posição." }
            require(session.sectors.all { it.id.isNotBlank() && it.name.isNotBlank() && it.capacity > 0 &&
                it.occupied in 0..it.capacity && it.price in 1..99999 }) { "Setor inválido." }
        }
    }
    /** Retrato imutável atual, atualizado somente pelas operações desta jornada. */
    var state: CinemaState = CinemaState()
        private set

    /**
     * Carrega os dados fixos do filme, sem escolha de horário e sem iniciar pedido/pagamento.
     * Preserva vendas por sessão e impede troca durante pedido pendente ou pagamento.
     * Selecionar novamente o filme atual mantém assento e pedido sem reinicialização.
     */
    fun selectMovie(movieId: String) {
        if (state.completedOrderId != null) return
        val movie = catalog.movies.find { it.id == movieId }
        if (movie == null) {
            state = state.copy(message = "Evento indisponível neste local.")
            return
        }
        if (movieId == state.movieId) return
        if (state.pending != null || state.paymentInProgress) {
            state = state.copy(message = "Conclua ou cancele o pedido antes de trocar o evento.")
            return
        }
        val configured = movie.session
        state = CinemaState(
            movieId = movie.id,
            sessionId = configured.id,
            session = CinemaSession(movie.title, configured.room, configured.time, configured.price,
                configured.date, configured.venue, configured.layout, configured.sectors, movie.kind),
            occupied = configured.occupied,
            sold = soldBySession[configured.id].orEmpty(),
            soldStanding = standingBySession[configured.id].orEmpty(),
            message = "Apresentação carregada. Selecione seus ingressos.",
        )
    }

    /** Alterna uma posição livre ou seu par inteiro; não altera um pedido já congelado. */
    fun select(seat: String) {
        if (state.completedOrderId != null || state.pending != null || state.paymentInProgress) return
        val place = state.session?.layout?.places?.find { it.id == seat }
        if (place == null) { state = state.copy(message = "Assento indisponível."); return }
        val group = if (place.kind == PlaceKind.COUPLE) setOf(seat, requireNotNull(place.partner)) else setOf(seat)
        if (group.any { it in state.sold || it in state.occupied }) {
            state = state.copy(message = "Assento indisponível."); return
        }
        val selection = if (seat in state.selectedSeats) state.selectedSeats - group else state.selectedSeats + group
        state = state.copy(selectedSeats = selection, message = "Seleção atualizada.")
    }

    /** Ajusta quantidade absoluta de pista, respeitando estoque da apresentação e pedido congelado. */
    fun setStanding(sectorId: String, quantity: Int) {
        if (state.pending != null || state.paymentInProgress || state.completedOrderId != null) return
        val sector = state.session?.sectors?.find { it.id == sectorId } ?: return
        val available = sector.capacity - sector.occupied - state.soldStanding.getOrDefault(sectorId, 0)
        if (quantity !in 0..available) { state = state.copy(message = "Quantidade indisponível."); return }
        state = state.copy(standing = if (quantity == 0) state.standing - sectorId else state.standing + (sectorId to quantity),
            message = "Quantidade atualizada.")
    }

    /** Retorna itens da seleção ou o snapshot congelado; a UI não calcula preços. */
    fun currentLines(): List<TicketLine> = state.lines()

    /** Congela itens, apresentação, total e identificador antes do pagamento. */
    fun createOrder() {
        if (state.completedOrderId != null || state.pending != null || state.paymentInProgress) return
        val sessionId = state.sessionId ?: return
        val lines = currentLines()
        val total = lines.fold(0L) { sum, line -> (sum + line.total).coerceAtMost(MAX_TICKET_AMOUNT + 1) }
        if (lines.isEmpty() || total !in 1..MAX_TICKET_AMOUNT) {
            state = state.copy(message = "Selecione ingressos dentro do limite de pagamento."); return
        }
        val id = UUID.randomUUID().toString()
        val order = TicketOrder(id, sessionId, state.selectedSeats.toSet(), state.standing.toMap(), lines.toList(), total)
        state = state.copy(order = order, selectedSeats = emptySet(), standing = emptyMap(), message = "Pedido demonstrativo criado.")
    }

    /** Inicia no máximo uma chamada; só falhas sem hardware admitem nova tentativa. */
    fun beginPayment(): Boolean {
        if (state.pending == null || state.paymentInProgress ||
            (state.paymentStatus != null && state.paymentStatus !in setOf(CardWithdrawalStatus.UNAVAILABLE, CardWithdrawalStatus.BUSY))) {
            return false
        }
        state = state.copy(paymentInProgress = true, paymentStatus = null, cleanupConfirmed = false,
            message = "Abrindo pagamento com cartão.")
        return true
    }

    /**
     * Recebe resultado sanitizado; APPROVED ou CAPTURED com cleanup concluem apenas demonstração.
     * Callback repetido ou sem pagamento ativo é ignorado. Não comprova autorização financeira.
     * @param orderId ID capturado pela rota na solicitação original; retorno obsoleto é ignorado
     */
    fun paymentCompleted(orderId: String, status: CardWithdrawalStatus, cleanupConfirmed: Boolean) {
        if (!state.paymentInProgress || state.pending == null || state.pendingOrderId != orderId) return
        state = state.copy(paymentInProgress = false, paymentStatus = status, cleanupConfirmed = cleanupConfirmed,
            message = "Resultado local: ${status.name}. Não comprova autorização financeira.")
        if (status in setOf(CardWithdrawalStatus.APPROVED, CardWithdrawalStatus.CAPTURED) && cleanupConfirmed) completeDemonstration()
    }

    /** Libera o indicador local se o chamador foi abandonado sem callback do host. */
    fun abandonPayment() {
        if (state.paymentInProgress) {
            state = state.copy(paymentInProgress = false, paymentStatus = CardWithdrawalStatus.UNKNOWN,
                cleanupConfirmed = false, message = "Resultado local desconhecido. Verifique a operação.")
        }
    }

    /** Registra emissão demonstrativa identificada, sem converter captura em aprovação financeira. */
    private fun completeDemonstration() {
        val order = requireNotNull(state.order)
        val sessionId = requireNotNull(state.sessionId)
        val orderId = requireNotNull(state.pendingOrderId)
        soldBySession[sessionId] = state.sold + order.seats
        val standingSold = state.soldStanding.toMutableMap()
        order.standing.forEach { (id, quantity) -> standingSold[id] = standingSold.getOrDefault(id, 0) + quantity }
        standingBySession[sessionId] = standingSold.toMap()
        state = state.copy(sold = state.sold + order.seats,
            order = null, soldStanding = standingSold.toMap(),
            completedOrderId = orderId, message = TRANSACTION_SUCCESS_MESSAGE)
    }

    /** Limpa somente a conclusão atual, conservando estoque; ID obsoleto não apaga atendimento posterior. */
    fun startNewAttendance(completedOrderId: String) {
        if (state.completedOrderId != completedOrderId || state.paymentInProgress) return
        state = CinemaState()
    }

    /** Cancela ou recusa o pedido, sem produzir venda. */
    fun discard(refused: Boolean) {
        if (state.completedOrderId != null) return
        if (state.pending == null || state.paymentInProgress) {
            state = state.copy(message = "Não há pedido pendente.")
            return
        }
        state = state.copy(selectedSeats = emptySet(), standing = emptyMap(), order = null, paymentStatus = null, cleanupConfirmed = false,
            message = if (refused) "Pagamento simulado recusado." else "Pedido cancelado.")
    }

    /** Identificadores da sala demonstrativa. */
    companion object {
        val seats: List<String> = VenueLayout.cinema().places.map { it.id }
    }
}

/** Produz linhas imutáveis ordenadas pela configuração, sem tarifa do cartão. */
internal fun CinemaState.lines(): List<TicketLine> {
    order?.let { return it.lines }
    val configured = session ?: return emptyList()
    return configured.layout.places.filter { it.id in selectedSeats }.map {
        TicketLine(it.id, "${it.id} · ${it.kind.label}", 1, configured.price)
    } + configured.sectors.mapNotNull { sector ->
        standing[sector.id]?.takeIf { it > 0 }?.let { TicketLine(sector.id, sector.name, it, sector.price) }
    }
}
