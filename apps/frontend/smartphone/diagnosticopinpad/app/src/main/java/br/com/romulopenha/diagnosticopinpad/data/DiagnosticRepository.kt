package br.com.romulopenha.diagnosticopinpad.data

import br.com.romulopenha.diagnosticopinpad.BuildConfig
import br.com.romulopenha.libpinpadabecsgo.mobile.Client
import br.com.romulopenha.libpinpadabecsgo.mobile.Mobile

/** Endpoint editável usado pelo diagnóstico do Bridge local. */
data class EndpointConfig(
    val host: String = BuildConfig.DEFAULT_BRIDGE_HOST,
    val port: Int = BuildConfig.DEFAULT_BRIDGE_PORT,
    val timeoutMillis: Long = BuildConfig.DEFAULT_TIMEOUT_MILLIS,
) {
    /** Valida os limites antes de atravessar a fronteira gomobile. */
    fun validate(): String? = when {
        host.isBlank() -> "Host obrigatório"
        host !in setOf("localhost", "127.0.0.1", "10.0.2.2") -> "Use localhost ou override local do Emulator"
        port !in 1..65535 -> "Porta deve estar entre 1 e 65535"
        timeoutMillis !in 100..120000 -> "Timeout deve estar entre 100 e 120000 ms"
        else -> null
    }
}

/** Contrato testável da tela, separado da classe gerada pelo gomobile. */
interface DiagnosticRepository : CatalogRepository {
    suspend fun version(): String
    suspend fun ping(config: EndpointConfig, operationId: String)
    suspend fun open(config: EndpointConfig, operationId: String)
    suspend fun getInfoJson(operationId: String): String
    suspend fun close(operationId: String = "")
    suspend fun cancel(operationId: String)
    fun state(): String
    /** Consulta local não confirma sessão de um endpoint diferente. */
    fun stateFor(config: EndpointConfig): String = state()
    /** Classifica uma falha sem expor texto arbitrário à camada de apresentação. */
    fun classifyFailure(failure: Throwable): DiagnosticFailure = DiagnosticFailure.from(failure)
}

/** Adapter fino entre o ViewModel e a fachada Go gerada no AAR. */
class GoMobileRepository(
    private val logger: AppLogger,
) : DiagnosticRepository {
    @Volatile private var client: Client? = null
    @Volatile private var endpoint: EndpointConfig? = null

    override suspend fun version(): String = Mobile.version()

    override suspend fun ping(config: EndpointConfig, operationId: String) {
        clientFor(config).ping(operationId)
    }

    override suspend fun open(config: EndpointConfig, operationId: String) {
        clientFor(config).open(operationId)
    }

    override suspend fun getInfoJson(operationId: String): String =
        requireClient().getInfoJSON(operationId)

    override suspend fun close(operationId: String) {
        val previous = client
        client = null
        endpoint = null
        previous?.close(operationId)
    }

    override suspend fun cancel(operationId: String) {
        requireClient().cancel(operationId)
    }

    override fun state(): String = client?.getState() ?: "CLOSED"

    override fun stateFor(config: EndpointConfig): String =
        if (endpoint == config) state() else "CLOSED"

    override fun classifyFailure(failure: Throwable): DiagnosticFailure {
        val encoded = failure.message.orEmpty()
        return DiagnosticFailure.fromCodes(
            code = Mobile.errorCode(encoded),
            phase = Mobile.errorPhase(encoded),
        )
    }

    /** Executa uma ação nomeada do catálogo e devolve somente texto sanitizado. */
    override suspend fun executeCatalog(
        config: EndpointConfig,
        action: CatalogAction,
        input: CatalogInput,
        operationId: String,
    ): String {
        val bridge = clientFor(config)
        fun int(key: String, default: Int = 0): Int = input.value(key).toIntOrNull() ?: default
        fun bool(key: String): Boolean = input.value(key).trim().equals("true", ignoreCase = true)
        fun csv(key: String): List<String> = input.value(key).split(',', ';').map(String::trim).filter(String::isNotEmpty)
        return when (action) {
            CatalogAction.OPEN -> bridge.open(operationId).let { "Sessão aberta" }
            CatalogAction.CLOSE -> close(operationId).let { "Sessão fechada" }
            CatalogAction.STATE -> bridge.getState()
            CatalogAction.INFO -> bridge.getInfoJSON(operationId)
            CatalogAction.RESET -> bridge.reset(operationId).let { "Reset concluído" }
            CatalogAction.DSP -> bridge.displayDSP(operationId, input.value("line1"), input.value("line2")).let { "DSP concluído" }
            CatalogAction.DEX -> bridge.displayDEX(operationId, input.value("message")).let { "DEX concluído" }
            CatalogAction.MNU -> "Opção selecionada: ${bridge.displayMNU(operationId, int("timeout", 30).toLong(), input.value("title"), input.value("options"))}"
            CatalogAction.GKY -> "Tecla pressionada: 0x${bridge.waitForKeyPress(operationId, int("timeout").toLong()).toString(16).uppercase()}"
            CatalogAction.GCX -> bridge.purchaseGCXSummaryJSON(operationId, input.value("amount"), input.value("date"), input.value("clock"), bool("enableCtls"), bool("hideAmount"))
            CatalogAction.GIX_RAW -> bridge.getInfoRawSummaryJSON(operationId)
            CatalogAction.DISPLAY_CAPABILITIES -> bridge.getDisplayCapabilitiesJSON(operationId)
            CatalogAction.OPEN_SECURE -> bridge.openSecure(operationId).let { "Sessão segura aberta" }
            CatalogAction.CLX -> bridge.closeVisual(operationId, input.value("message"), input.value("mediaName")).let { "CLX concluído" }
            CatalogAction.LOAD_QR_MEDIA -> bridge.loadQRCodeMultimedia(operationId, input.value("name"), input.value("text"), int("size").toLong()).let { "Mídia QR carregada" }
            CatalogAction.DISPLAY_MEDIA -> bridge.displayImage(operationId, input.value("name")).let { "DSI concluído" }
            CatalogAction.LOAD_EMV_TABLE -> bridge.loadCompleteEMVTable(operationId, input.value("acquirer"), input.value("version"), input.value("records")).let { "Tabela EMV carregada" }
            CatalogAction.GTK -> bridge.getTracksSummaryJSON(operationId, input.value("tracks"), input.value("dataMethod"), int("openDigits").toLong(), int("keyIndex", -1).toLong(), input.value("workingKeyHex"), input.value("ivHex"), input.value("publicKeyModHex"), input.value("publicKeyExpHex"))
            CatalogAction.GOX -> bridge.continueEMVSummaryJSON(operationId, input.value("acquirerReference"), input.value("pinMethod"), int("keyIndex").toLong(), input.value("workingKeyHex"), input.value("transactionType"), input.value("amount"), input.value("cashback"), input.value("currencyHex"), input.value("options"), input.value("displayMessage"), input.value("terminalParamsHex"), input.value("emvDataHex"), input.value("tagListHex"), int("timeout").toLong())
            CatalogAction.FCX -> bridge.finalizeEMVSummaryJSON(operationId, input.value("options"), input.value("authorization"), input.value("emvDataHex"), input.value("tagListHex"), int("timeout").toLong())
            CatalogAction.GPN_MKWK -> bridge.capturePINMK(operationId, int("keyIndex").toLong(), input.value("workingKeyHex"), input.value("pan"), input.value("message")).let { "PIN coletado; PIN block e KSN foram redigidos" }
            CatalogAction.GPN_DUKPT -> bridge.capturePINDUKPT(operationId, int("keyIndex").toLong(), input.value("pan"), input.value("message")).let { "PIN coletado; PIN block e KSN foram redigidos" }
            CatalogAction.QR -> bridge.displayQRCodeSummaryJSON(operationId, input.value("data"), int("size", 200).toLong(), int("margin").toLong(), int("xPos").toLong(), int("yPos").toLong())
            CatalogAction.LIST_MEDIA -> bridge.listMultimediaFilesJSON(operationId)
            CatalogAction.DELETE_MEDIA -> bridge.deleteMultimediaFiles(operationId, input.value("names"))
            CatalogAction.UNAVAILABLE, CatalogAction.RESERVED, CatalogAction.EXIT -> error("Ação não executável pelo catálogo")
        }
    }

    private fun clientFor(config: EndpointConfig): Client {
        val existing = client
        if (existing != null && endpoint == config) {
            return existing
        }
        if (existing != null) {
            existing.close("")
        }
        val created = Mobile.newClient(config.host, config.port.toLong(), config.timeoutMillis)
        client = created
        endpoint = config
        logger.event(
            level = "INFO",
            event = "client_created",
            fields = mapOf(
                "host" to config.host,
                "port" to config.port.toString(),
                "timeoutMillis" to config.timeoutMillis.toString(),
                "source" to "ui",
            ),
        )
        return created
    }

    private fun requireClient(): Client = client ?: error("Cliente ainda não conectado")
}
