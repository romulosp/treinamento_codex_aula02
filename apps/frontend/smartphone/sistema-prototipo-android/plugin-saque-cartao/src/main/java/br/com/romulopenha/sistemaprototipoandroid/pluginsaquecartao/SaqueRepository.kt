package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import android.content.Context
import android.content.pm.ApplicationInfo
import br.com.romulopenha.libpinpadabecsgo.mobile.Client
import br.com.romulopenha.libpinpadabecsgo.mobile.Mobile
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Dimensões reais consultadas no pinpad, não inferidas pelo modelo do aparelho. */
internal data class SaqueDisplay(val lines: Int, val columns: Int)

/**
 * Falha sanitizada da jornada; mensagens arbitrárias do driver nunca chegam à UI.
 *
 * [code] e [phase] pertencem às whitelists do core 074 (`Mobile.errorCode` e
 * `Mobile.errorPhase`) ou aos códigos locais de provisionamento; nenhum texto
 * livre, caminho, parâmetro ou conteúdo de cartão é propagado.
 */
internal class SaqueFailure(val code: String, val phase: String = "binding") : Exception(code)

/** Porta main-safe para a jornada; protocolo, parsers e cartão ficam no Go. */
internal interface SaqueRepository {
    /** Lê e valida somente configuração privada DES do host debug, sem hardware. */
    suspend fun loadProfile(): SaquePerfil
    /** Cria cliente, confirma Bridge e abre sessão real; pode falhar sem avançar. */
    suspend fun open(profile: SaquePerfil, operationId: String)
    /** Consulta dimensões de display antes de preparar a confirmação. */
    suspend fun displayCapabilities(operationId: String): SaqueDisplay
    /** Encaminha o array externo para validação e carga completas no Go. */
    suspend fun loadTable(profile: SaquePerfil, operationId: String)
    /** Exibe texto DEX que já cabe nas capacidades reais. */
    suspend fun displayText(operationId: String, message: String)
    /** Exibe duas linhas de no máximo 16 caracteres sem truncamento. */
    suspend fun displayPage(operationId: String, first: String, second: String)
    /** Aguarda tecla real; 0 confirma e 13 cancela. */
    suspend fun waitKey(operationId: String, timeoutSeconds: Int): Int
    /** Inicia GCX filtrado; devolve apenas o tipo físico de cartão. */
    suspend fun capture(profile: SaquePerfil, summary: SaqueResumo, operationId: String): String
    /** Captura PIN DUKPT tipo 3/08 com PAN retido no dispositivo. */
    suspend fun capturePin(operationId: String)
    /** Solicita GTK criptografado e descarta o summary que contém apenas comprimentos. */
    suspend fun tracks(profile: SaquePerfil, operationId: String)
    /** Continua EMV; devolve a decisão segura 0, 1 ou 2. */
    suspend fun continueEmv(profile: SaquePerfil, summary: SaqueResumo, operationId: String): Char
    /** Finaliza somente online no cenário explícito; devolve decisão 0 ou 1. */
    suspend fun finalizeEmv(profile: SaquePerfil, operationId: String): Char
    /** Cancela explicitamente a operação no core; coroutine sozinha não cancela hardware. */
    suspend fun cancel(operationId: String)
    /** Encerra sessão mesmo após falha, com o timeout limitado do cliente. */
    suspend fun close(operationId: String)
}

/** Adapter fino gomobile. JSON EMV fica nesta camada e nunca integra estado Compose. */
internal class GoSaqueRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SaqueRepository {
    @Volatile private var client: Client? = null
    private var tableJson: String = ""

    /**
     * Lê perfil e tabela privados do host debug, sem hardware.
     *
     * Distingue ausência de perfil (`MISSING`), ausência de tabela
     * (`TABLE_MISSING`) e conteúdo inválido (`CONFIGURATION`); nenhum desses
     * casos é substituído por configuração sintética.
     */
    override suspend fun loadProfile(): SaquePerfil = withContext(ioDispatcher) {
        try {
            require(context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)
            val dir = File(context.filesDir, "saque-cartao")
            val profileFile = File(dir, "perfil.json")
            if (!profileFile.isFile) throw SaqueFailure("MISSING", "preflight")
            val tableFile = File(dir, "emv.json")
            if (!tableFile.isFile) throw SaqueFailure("TABLE_MISSING", "preflight")
            val profileJson = readPrivateFile(profileFile, 65536)
            tableJson = readPrivateFile(tableFile, 1048576)
            parseSaqueProfile(profileJson).also { profile ->
                Mobile.validateFormattedEMVTableJSON(profile.tableAcquirer, profile.tableVersion, tableJson)
            }
        } catch (failure: SaqueFailure) {
            tableJson = ""
            throw failure
        } catch (_: LinkageError) {
            tableJson = ""
            throw SaqueFailure("BINDING_ERROR", "preflight")
        } catch (_: Exception) {
            tableJson = ""
            throw SaqueFailure("CONFIGURATION", "preflight")
        }
    }

    override suspend fun open(profile: SaquePerfil, operationId: String) = goCall {
        profile.validate()
        val created = Mobile.newClient(profile.host, profile.port.toLong(), profile.timeoutMillis)
        client = created
        created.ping(operationId)
        created.open(operationId)
    }

    override suspend fun displayCapabilities(operationId: String): SaqueDisplay = goCall {
        val json = JSONObject(requireClient().getDisplayCapabilitiesJSON(operationId))
        SaqueDisplay(json.getInt("TextLines"), json.getInt("TextCols"))
    }

    override suspend fun loadTable(profile: SaquePerfil, operationId: String) = goCall {
        require(tableJson.isNotEmpty())
        requireClient().loadFormattedEMVTableJSON(operationId, profile.tableAcquirer, profile.tableVersion, tableJson)
    }

    override suspend fun displayText(operationId: String, message: String) = goCall {
        requireClient().displayDEX(operationId, message)
    }

    override suspend fun displayPage(operationId: String, first: String, second: String) = goCall {
        require(first.length <= 16 && second.length <= 16)
        requireClient().displayDSP(operationId, first, second)
    }

    override suspend fun waitKey(operationId: String, timeoutSeconds: Int): Int = goCall {
        requireClient().waitForKeyPress(operationId, timeoutSeconds.toLong()).toInt()
    }

    override suspend fun capture(profile: SaquePerfil, summary: SaqueResumo, operationId: String): String = goCall {
        val now = LocalDateTime.now()
        val json = requireClient().purchaseGCXFilteredSummaryJSON(
            operationId, summary.amountN12(), now.format(DateTimeFormatter.ofPattern("yyMMdd")),
            now.format(DateTimeFormatter.ofPattern("HHmmss")), true, false, 1L,
            profile.tableAcquirer, profile.applicationTypes(summary.modalidade), "", profile.timeoutSeconds.toLong(),
        )
        val jsonObj = JSONObject(json)
        require(jsonObj.getString("schemaVersion") == "1")
        val type = jsonObj.getString("cardType")
        require(type in setOf("00", "03", "05", "06"))
        type
    }

    override suspend fun capturePin(operationId: String) = goCall {
        requireClient().capturePINDUKPT(operationId, 8L, "", "DIGITE SUA SENHA")
    }

    override suspend fun tracks(profile: SaquePerfil, operationId: String) = goCall {
        requireClient().getTracksSummaryJSON(operationId, "0111", "30", 0L, profile.gtkKeyIndex.toLong(), "", "", "", "")
        Unit
    }

    override suspend fun continueEmv(profile: SaquePerfil, summary: SaqueResumo, operationId: String): Char = goCall {
        val json = requireClient().continueEMVSummaryJSON(
            operationId, profile.tableAcquirer, "3", 8L, "", "\u0001", summary.amountN12(), "", "", "01000",
            "", "", "", "", profile.timeoutSeconds.toLong(),
        )
        decision(json, 6, "012")
    }

    override suspend fun finalizeEmv(profile: SaquePerfil, operationId: String): Char = goCall {
        val json = requireClient().finalizeEMVSummaryJSON(
            operationId, profile.scenario.options, profile.scenario.arc, "", "", profile.timeoutSeconds.toLong(),
        )
        decision(json, 3, "01")
    }

    override suspend fun cancel(operationId: String) = goCall { client?.cancel(operationId); Unit }

    override suspend fun close(operationId: String) = goCall {
        val closing = client
        client = null
        tableJson = ""
        closing?.close(operationId)
        Unit
    }

    /** Executa binding bloqueante fora da main e expõe só categoria e fase públicas. */
    private suspend fun <T> goCall(action: () -> T): T = withContext(ioDispatcher) {
        try {
            action()
        } catch (canceled: CancellationException) {
            throw canceled
        } catch (error: Exception) {
            val message = error.message.orEmpty()
            throw SaqueFailure(Mobile.errorCode(message), Mobile.errorPhase(message))
        } catch (_: LinkageError) {
            throw SaqueFailure("BINDING_ERROR", "binding")
        }
    }

    /** Exige cliente da sessão atual, sem criar sessão implícita ou replay. */
    private fun requireClient(): Client = client ?: throw SaqueFailure("PINPAD_CLOSED")
}

/** Lê arquivo limitado; não inclui conteúdo, caminhos ou erros de parser nas mensagens. */
private fun readPrivateFile(file: File, maxBytes: Long): String {
    require(file.isFile && file.length() in 1..maxBytes)
    return file.readText(Charsets.UTF_8)
}

/** Perfil físico; taxa/modalidade são domínio e seus campos legados são ignorados. */
internal fun parseSaqueProfile(raw: String): SaquePerfil {
    val json = JSONObject(raw)
    require(json.wholeNumber("schemaVersion") == 1L)
    return SaquePerfil(
        json.getString("environment"), json.getString("host"), Math.toIntExact(json.wholeNumber("port")),
        json.wholeNumber("timeoutMillis"), Math.toIntExact(json.wholeNumber("timeoutSeconds")), json.getString("tableAcquirer"),
        json.getString("tableVersion"), SaqueModalidade.CREDITO.applicationType, SaqueModalidade.DEBITO.applicationType,
        json.wholeNumber("maxBaseCents"), json.getString("gtkDataMethod"), Math.toIntExact(json.wholeNumber("gtkKeyIndex")),
        SaqueCenario.valueOf(json.getString("scenario")),
    ).also(SaquePerfil::validate)
}

/** Rejeita coerção JSON de fração/string/booleano para inteiros de configuração. */
private fun JSONObject.wholeNumber(name: String): Long {
    val value = get(name)
    require(value is Number && value.toString().matches(Regex("[0-9]+")))
    return value.toString().toLongOrNull() ?: throw IllegalArgumentException()
}

/** Confirma o tamanho/decisão da resposta resumida; JSON incompleto não implica aprovação. */
private fun decision(raw: String, length: Int, allowed: String): Char {
    val json = JSONObject(raw)
    require(json.getString("schemaVersion") == "1")
    val result = json.getString("result")
    require(result.length == length && result.first() in allowed)
    return result.first()
}
