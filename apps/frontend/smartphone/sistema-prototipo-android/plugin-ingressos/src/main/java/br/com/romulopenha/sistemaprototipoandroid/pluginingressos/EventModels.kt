package br.com.romulopenha.sistemaprototipoandroid.pluginingressos

/** Categoria informativa e filtro do catálogo local. */
internal enum class EventKind(val label: String) { CINEMA("Cinema"), THEATRE("Teatro"), SHOW("Shows") }

/** Tipo físico da posição; acompanhante não obriga compra conjunta. */
internal enum class PlaceKind(val label: String) {
    STANDARD("Comum"), WHEELCHAIR("PCD"), COMPANION("Acompanhante"), COUPLE("Namoro")
}

/** Posição numerada e seus vínculos opcionais de acessibilidade ou par inseparável. */
internal data class VenuePlace(
    val id: String, val row: Char, val number: Int,
    val kind: PlaceKind = PlaceKind.STANDARD, val partner: String? = null,
)

/** Geometria configurável; corredores ficam depois dos números indicados e não consomem estoque. */
internal data class VenueLayout(val places: List<VenuePlace>, val aisleAfter: Set<Int>) {
    companion object {
        /** Gera uma plateia regular menor para exemplos de teatro e show. */
        fun regular(rows: Int, columns: Int): VenueLayout {
            require(rows in 1..26 && columns in 1..30)
            return VenueLayout((0 until rows).flatMap { offset ->
                val row = 'A' + offset
                (1..columns).map { VenuePlace("$row$it", row, it) }
            }, setOf(columns / 2).filter { it > 0 }.toSet())
        }

        /** Gera exatamente 180 posições comerciais, incluindo PCD, acompanhantes e quatro lugares namoro. */
        fun cinema(): VenueLayout = VenueLayout(('A'..'L').flatMap { row ->
            (1..15).map { number ->
                val id = "$row$number"
                val partner = when (id) {
                    "A1" -> "A2"; "A2" -> "A1"; "A3" -> "A4"; "A4" -> "A3"
                    "A13" -> "A12"; "A12" -> "A13"; "A15" -> "A14"; "A14" -> "A15"
                    "L1" -> "L2"; "L2" -> "L1"; "L14" -> "L15"; "L15" -> "L14"
                    else -> null
                }
                val kind = when {
                    row == 'A' && number in setOf(1, 3, 13, 15) -> PlaceKind.WHEELCHAIR
                    row == 'A' && number in setOf(2, 4, 12, 14) -> PlaceKind.COMPANION
                    row == 'L' && number in setOf(1, 2, 14, 15) -> PlaceKind.COUPLE
                    else -> PlaceKind.STANDARD
                }
                VenuePlace(id, row, number, kind, partner)
            }
        }, setOf(5, 10))
    }
}

/** Estoque inicial de um setor sem numeração, preço em centavos e ocupação previamente configurada. */
internal data class StandingSector(
    val id: String, val name: String, val capacity: Int, val price: Int, val occupied: Int = 0,
)

/** Item congelado: lugares individuais ou quantidade de um setor; valores sempre em centavos. */
internal data class TicketLine(val id: String, val label: String, val quantity: Int, val unitPrice: Int) {
    val total: Long get() = quantity.toLong() * unitPrice
}

/** Pedido imutável de uma única apresentação, capturado antes de abrir o componente de pagamento. */
internal data class TicketOrder(
    val id: String, val presentationId: String, val seats: Set<String>,
    val standing: Map<String, Int>, val lines: List<TicketLine>, val total: Long,
)

/** Limite do contrato CardPaymentRequest, sem alteração na API compartilhada. */
internal const val MAX_TICKET_AMOUNT = 999999999999L
