package br.com.romulopenha.diagnosticopinpad.data

/** Campo de entrada de uma ação funcional do laboratório. */
data class CatalogField(
    val key: String,
    val label: String,
    val secret: Boolean = false,
)

/** Ação nomeada correspondente ao menu local da biblioteca Go. */
enum class CatalogAction(
    val number: Int,
    val title: String,
    val group: String,
    val enabled: Boolean = true,
    val disabledReason: String? = null,
    val fields: List<CatalogField> = emptyList(),
    val visibleInCatalog: Boolean = true,
) {
    OPEN(1, "Abrir conexão", "Conexão", visibleInCatalog = false),
    CLOSE(2, "Fechar conexão", "Conexão", visibleInCatalog = false),
    STATE(3, "Estado atual", "Conexão"),
    INFO(4, "Informações do pinpad (GIX)", "Conexão"),
    RESET(5, "Reset rápido (CAN)", "Conexão"),
    UNAVAILABLE(6, "Indisponível no ABECS 2.12", "Conexão", false, "Use Reset rápido (CAN)."),
    DSP(7, "Mensagem fixa (DSP)", "Display", fields = listOf(
        CatalogField("line1", "Linha 1"), CatalogField("line2", "Linha 2"),
    )),
    DEX(8, "Mensagem estendida (DEX)", "Display", fields = listOf(CatalogField("message", "Mensagem"))),
    MNU(9, "Menu interativo (MNU)", "Display", fields = listOf(
        CatalogField("timeout", "Timeout em segundos"), CatalogField("title", "Título"),
        CatalogField("options", "Opções separadas por vírgula"),
    )),
    GKY(10, "Aguardar tecla (GKY)", "Display", fields = listOf(CatalogField("timeout", "Timeout em segundos"))),
    GCX(11, "Transação de compra (GCX)", "Transação", fields = listOf(
        CatalogField("amount", "Valor em centavos"), CatalogField("date", "Data AAMMDD"),
        CatalogField("clock", "Hora HHMMSS"), CatalogField("enableCtls", "Habilitar contactless: true/false"),
        CatalogField("hideAmount", "Ocultar valor: true/false"),
    )),
    GIX_RAW(12, "Resposta GIX resumida", "Informações"),
    DISPLAY_CAPABILITIES(13, "Capacidades do display", "Informações"),
    OPEN_SECURE(14, "Abrir sessão segura (OPN RSA/AES)", "Segurança"),
    CLX(15, "Atualizar/limpar display (CLX)", "Display", fields = listOf(
        CatalogField("message", "Mensagem visual"), CatalogField("mediaName", "Nome da mídia"),
    )),
    LOAD_QR_MEDIA(16, "Carregar QR/mídia (MLI/MLR/MLE)", "Mídia", fields = listOf(
        CatalogField("name", "Nome A8 da mídia"), CatalogField("text", "Texto do QR"), CatalogField("size", "Tamanho em pixels"),
    )),
    DISPLAY_MEDIA(17, "Exibir mídia (DSI)", "Mídia", fields = listOf(CatalogField("name", "Nome A8 da mídia"))),
    LOAD_EMV_TABLE(18, "Carregar tabela EMV (TLI/TLR/TLE)", "EMV", fields = listOf(
        CatalogField("acquirer", "Índice do adquirente"), CatalogField("version", "Versão da tabela"),
        CatalogField("records", "Registros separados por ponto e vírgula"),
    )),
    GTK(19, "Obter trilhas (GTK)", "Transação", fields = listOf(
        CatalogField("tracks", "Seleção de trilhas"), CatalogField("dataMethod", "Método de dados"),
        CatalogField("openDigits", "Dígitos abertos"), CatalogField("keyIndex", "Índice da chave"),
        CatalogField("workingKeyHex", "WK/TDES em hexadecimal", true), CatalogField("ivHex", "IV em hexadecimal", true),
        CatalogField("publicKeyModHex", "Módulo RSA em hexadecimal", true), CatalogField("publicKeyExpHex", "Expoente RSA em hexadecimal", true),
    )),
    GOX(20, "Continuar transação EMV (GOX)", "EMV", fields = listOf(
        CatalogField("acquirerReference", "Referência do adquirente"), CatalogField("pinMethod", "Método de PIN"),
        CatalogField("keyIndex", "Índice da chave"), CatalogField("workingKeyHex", "WK em hexadecimal", true),
        CatalogField("transactionType", "Tipo da transação"), CatalogField("amount", "Valor"), CatalogField("cashback", "Cashback"),
        CatalogField("currencyHex", "Moeda em hexadecimal"), CatalogField("options", "Opções"), CatalogField("displayMessage", "Mensagem"),
        CatalogField("terminalParamsHex", "Parâmetros do terminal em hexadecimal", true), CatalogField("emvDataHex", "Dados EMV em hexadecimal", true),
        CatalogField("tagListHex", "Lista de tags em hexadecimal", true), CatalogField("timeout", "Timeout em segundos"),
    )),
    FCX(21, "Finalizar transação EMV (FCX)", "EMV", fields = listOf(
        CatalogField("options", "Opções"), CatalogField("authorization", "Autorização"),
        CatalogField("emvDataHex", "Dados EMV em hexadecimal", true), CatalogField("tagListHex", "Lista de tags em hexadecimal", true),
        CatalogField("timeout", "Timeout em segundos"),
    )),
    GPN_MKWK(22, "Capturar PIN MK/WK (GPN)", "PIN", fields = listOf(
        CatalogField("keyIndex", "Índice da chave"), CatalogField("workingKeyHex", "WKENC em hexadecimal", true),
        CatalogField("pan", "PAN", true), CatalogField("message", "Mensagem de PIN"),
    )),
    GPN_DUKPT(23, "Capturar PIN DUKPT (GPN)", "PIN", fields = listOf(
        CatalogField("keyIndex", "Índice da chave"), CatalogField("pan", "PAN", true), CatalogField("message", "Mensagem de PIN"),
    )),
    QR(24, "Exibir QR Code", "Mídia", fields = listOf(
        CatalogField("data", "Texto do QR"), CatalogField("size", "Tamanho em pixels"), CatalogField("margin", "Margem"),
        CatalogField("xPos", "Posição X"), CatalogField("yPos", "Posição Y"),
    )),
    RESERVED(25, "Transação GCX completa", "Transação", false, "Reservada na SPEC vigente."),
    LIST_MEDIA(26, "Listar mídias (LMF)", "Mídia"),
    DELETE_MEDIA(27, "Excluir mídias (DMF)", "Mídia", fields = listOf(CatalogField("names", "Nomes separados por ponto e vírgula"))),
    EXIT(28, "Sair", "Aplicativo"),
}

/** Parâmetros efêmeros de uma ação; nunca são persistidos nem logados. */
data class CatalogInput(val values: Map<String, String> = emptyMap()) {
    /** Lê um campo ausente como texto vazio. */
    fun value(key: String): String = values[key].orEmpty()

    /** Valida somente formatos básicos antes da chamada ao AAR. */
    fun validate(action: CatalogAction): String? {
        val numericFields = setOf("timeout", "size", "margin", "xPos", "yPos", "keyIndex", "openDigits")
        for (field in action.fields) {
            val raw = value(field.key).trim()
            if (raw.isNotEmpty() && field.key in numericFields && raw.toLongOrNull() == null) {
                return "${field.label} deve ser numérico"
            }
            if (raw.isNotEmpty() && field.key.endsWith("Hex") &&
                (raw.length % 2 != 0 || raw.any { it !in "0123456789abcdefABCDEF" })
            ) {
                return "${field.label} deve ser hexadecimal par"
            }
        }
        return null
    }
}

/** Contrato de operações do catálogo para desacoplar a tela do binding. */
interface CatalogRepository {
    suspend fun executeCatalog(config: EndpointConfig, action: CatalogAction, input: CatalogInput, operationId: String): String
}
