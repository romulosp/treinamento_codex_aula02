package br.com.romulopenha.sistemaprototipoandroid.pluginingressos

/**
 * Sessão já programada pelo cinema; os dados não são editáveis na bilheteria.
 * @property id identificador estável e único da sessão dentro do catálogo
 * @property room sala previamente configurada
 * @property time horário informativo previamente configurado
 * @property price preço base em centavos, sem tarifa do componente de cartão
 * @property occupied lugares indisponíveis na configuração, sem ingresso local emitido
 * @property date data informativa fictícia, sem escolha pelo operador
 * @property venue identificação do local previamente configurado
 * @property layout geometria numerada própria desta apresentação
 * @property sectors capacidades, ocupações iniciais e preços dos setores de pista
 */
internal data class CinemaConfiguredSession(
    val id: String,
    val room: String,
    val time: String,
    val price: Int,
    val occupied: Set<String> = emptySet(),
    val date: String = "08/11/2026",
    val venue: String = "Centro cultural demonstração",
    val layout: VenueLayout = VenueLayout.cinema(),
    val sectors: List<StandingSector> = emptyList(),
)

/**
 * Evento e sua apresentação fixa configurada no local demonstrativo.
 * @property id identificador estável usado pela seleção do carrossel
 * @property title título apresentado no cartaz e resumo
 * @property session programação fixa carregada ao selecionar o evento
 * @property kind categoria utilizada pelo filtro e pela identificação de tela/palco
 */
internal data class CinemaMovie(val id: String, val title: String, val session: CinemaConfiguredSession, val kind: EventKind = EventKind.CINEMA)

/**
 * Catálogo local de eventos entregue à jornada sem edição pelo operador.
 * @property cinema identificação demonstrativa do cinema
 * @property movies filmes com suas sessões programadas
 */
internal data class CinemaCatalog(val cinema: String, val movies: List<CinemaMovie>)

/** Fonte local de dados fictícios; não consulta programação comercial, rede ou armazenamento. */
internal object DemoCinemaCatalog {
    /** Configuração demonstrativa única, com preço e ocupação independentes por sessão. */
    val catalog = CinemaCatalog(
        cinema = "Bilheteria demonstração",
        movies = listOf(
            CinemaMovie("batman", "Batman", CinemaConfiguredSession(
                "cinema-demo-batman-1930", "SALA 04", "19:30", 4400, setOf("A4", "A5", "B4", "B5"))),
            CinemaMovie("avatar", "Avatar 3", CinemaConfiguredSession(
                "cinema-demo-avatar-1715", "SALA 02", "17:15", 4800, setOf("A2", "B2", "B3"))),
            CinemaMovie("coringa", "Coringa 2", CinemaConfiguredSession(
                "cinema-demo-coringa-2000", "SALA 03", "20:00", 3600, setOf("A6", "B1", "B6"))),
            CinemaMovie("teatro", "Uma noite no teatro", CinemaConfiguredSession(
                "teatro-demo-2000", "Teatro Central", "20:00", 6000,
                layout = VenueLayout.regular(6, 10)), EventKind.THEATRE),
            CinemaMovie("show", "Festival de música", CinemaConfiguredSession(
                "show-demo-2100", "Arena", "21:00", 9000,
                layout = VenueLayout.regular(4, 10),
                sectors = listOf(StandingSector("pista", "Pista", 100, 5000),
                    StandingSector("vip", "Pista VIP", 40, 12000))), EventKind.SHOW),
            CinemaMovie("spiderman", "Spider-Man", CinemaConfiguredSession(
                "cinema-demo-spiderman-1430", "SALA 01", "14:30", 3200, setOf("A3", "A4", "B5"))),
        ),
    )
}
