package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import android.content.Context
import android.content.pm.ApplicationInfo
import br.com.romulopenha.libpinpadabecsgo.mobile.Client
import br.com.romulopenha.libpinpadabecsgo.mobile.Mobile
import br.com.romulopenha.libpinpadabecsgo.mobile.PaymentResult
import android.util.Log
import java.io.File
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
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
    /** Inicia compra GCX filtrada; devolve modo tipado e mantém PAN exclusivamente no Go. */
    suspend fun capture(profile: SaquePerfil, summary: SaqueResumo, operationId: String): SaqueModoCartao
    /** Captura GPN somente para 00/05 após GTK, usando configuração e PAN privados do core. */
    suspend fun capturePin(operationId: String)
    /** Consome GTK em claro/criptografado conforme perfil; falha impede GOX/GPN. Trilhas ficam no Go. */
    suspend fun tracks(profile: SaquePerfil, operationId: String)
    /** Continua GOX somente para 03/06; kernel controla PIN/CVM e retorna decisão 0, 1 ou 2. */
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
    /** Snapshot privado: fonte validada no preflight; índice GIX resolvido após Open. Fora da UI. */
    @Volatile private var pinConfiguration: PaymentPinConfiguration? = null
    /** Opt-in e ambiente congelados no preflight, fora do estado Compose. */
    @Volatile private var logPinResults = false
    /** Ambiente validado da fonte de perfil; não presume DES para habilitar diagnóstico. */
    @Volatile private var logEnvironment = ""

    /**
     * Lê perfil, tabela e três propriedades PIN privados do host debug, sem hardware.
     *
     * Distingue ausência de perfil (`MISSING`), ausência de tabela
     * (`TABLE_MISSING`), ausência de criptografia (`CRYPTO_MISSING`) e conteúdo
     * inválido (`CONFIGURATION`). Valida candidatos de ambas as modalidades e
     * fonte PIN integral no Go antes de Open; nenhum desses
     * casos é substituído por configuração sintética.
     */
    override suspend fun loadProfile(): SaquePerfil = withContext(ioDispatcher) {
        pinConfiguration = null
        tableJson = ""
        logPinResults = false
        logEnvironment = ""
        try {
            require(context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)
            val dir = File(context.filesDir, "saque-cartao")
            val profileFile = File(dir, "perfil.json")
            if (!profileFile.isFile) throw SaqueFailure("MISSING", "preflight")
            val tableFile = File(dir, "emv.json")
            if (!tableFile.isFile) throw SaqueFailure("TABLE_MISSING", "preflight")
            val pinFile = File(dir, "criptografia.properties")
            if (!pinFile.isFile) throw SaqueFailure("CRYPTO_MISSING", "preflight")
            val sourcePIN = Mobile.parsePaymentPINConfiguration(readPrivateFile(pinFile, 65536))
            val pin = PaymentPinConfiguration(sourcePIN.method(),
                Math.toIntExact(sourcePIN.keyIndex()), sourcePIN.workingKey())
            val profileJson = readPrivateFile(profileFile, 65536)
            tableJson = readPrivateFile(tableFile, 1048576)
            parseSaqueProfile(profileJson).also { profile ->
                Mobile.validateFormattedEMVTableJSON(profile.tableAcquirer, profile.tableVersion, tableJson)
                Mobile.validatePaymentCandidatesJSON(profile.tableAcquirer, profile.creditApplicationTypes, tableJson)
                Mobile.validatePaymentCandidatesJSON(profile.tableAcquirer, profile.debitApplicationTypes, tableJson)
                pinConfiguration = pin
                logPinResults = profile.logPinResults
                logEnvironment = profile.environment
            }
        } catch (failure: SaqueFailure) {
            pinConfiguration = null
            tableJson = ""
            throw failure
        } catch (_: LinkageError) {
            pinConfiguration = null
            tableJson = ""
            throw SaqueFailure("BINDING_ERROR", "preflight")
        } catch (_: Exception) {
            pinConfiguration = null
            tableJson = ""
            throw SaqueFailure("CONFIGURATION", "preflight")
        }
    }

    /** Abre e resolve uma vez o índice PIN por GIX; mesma escolha será usada até o fechamento. */
    override suspend fun open(profile: SaquePerfil, operationId: String) = goCall {
        profile.validate()
        val source = requirePIN()
        val created = Mobile.newClient(profile.host, profile.port.toLong(), profile.timeoutMillis)
        client = created
        created.ping(operationId)
        created.open(operationId)
        val selected = created.resolvePaymentPINConfiguration(operationId, source.method,
            source.keyIndex.toLong(), source.workingKey)
        pinConfiguration = PaymentPinConfiguration(selected.method(),
            Math.toIntExact(selected.keyIndex()), selected.workingKey())
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

    override suspend fun capture(profile: SaquePerfil, summary: SaqueResumo, operationId: String): SaqueModoCartao = goCall {
        val now = LocalDateTime.now()
        val pin = requirePIN()
        val json = requireClient().capturePaymentCardSummaryJSON(
            operationId, summary.amountN12(), now.format(DateTimeFormatter.ofPattern("yyMMdd")),
            now.format(DateTimeFormatter.ofPattern("HHmmss")),
            profile.tableAcquirer, profile.applicationTypes(summary.modalidade), pin.method,
            pin.keyIndex.toLong(), pin.workingKey, profile.timeoutSeconds.toLong(),
        )
        val jsonObj = JSONObject(json)
        require(jsonObj.getString("schemaVersion") == "1")
        SaqueModoCartao.fromCardType(jsonObj.getString("cardType"))
    }

    /** Consome par GPN privado, registra somente com opt-in DES e limpa buffers/handle. */
    override suspend fun capturePin(operationId: String) = goCall {
        val pin = requirePIN()
        val result = requireClient().capturePaymentPINResult(operationId, pin.method, pin.keyIndex.toLong(), pin.workingKey, "DIGITE SUA SENHA")
        try { consumePINResult(result, operationId, "GPN") } finally { result.clear() }
    }

    /** Encaminha a escolha explícita do perfil; modo em claro omite índice no core. */
    override suspend fun tracks(profile: SaquePerfil, operationId: String) = goCall {
        requireClient().getPaymentTracksSummaryJSON(operationId, profile.gtkDataMethod, profile.gtkKeyIndex.toLong())
        Unit
    }

    /** Devolve decisão do kernel; par online é consumido/logado privadamente e limpo em finally. */
    override suspend fun continueEmv(profile: SaquePerfil, summary: SaqueResumo, operationId: String): Char = goCall {
        val pin = requirePIN()
        val result = requireClient().continuePaymentEMVResult(
            operationId, profile.tableAcquirer, summary.amountN12(), pin.method,
            pin.keyIndex.toLong(), pin.workingKey, profile.timeoutSeconds.toLong(),
        )
        try {
            val decision = result.result()
            require(decision.length == 6 && decision.first() in "012")
            consumePINResult(result, operationId, "GOX")
            decision.first()
        } finally { result.clear() }
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
        pinConfiguration = null
        logPinResults = false
        logEnvironment = ""
        closing?.close(operationId)
        Unit
    }

    /** Executa binding bloqueante fora da main e expõe só categoria e fase públicas. */
    private suspend fun <T> goCall(action: () -> T): T = withContext(ioDispatcher) {
        try {
            action()
        } catch (canceled: CancellationException) {
            throw canceled
        } catch (failure: SaqueFailure) {
            throw failure
        } catch (error: Exception) {
            val message = error.message.orEmpty()
            throw SaqueFailure(Mobile.errorCode(message), Mobile.errorPhase(message))
        } catch (_: LinkageError) {
            throw SaqueFailure("BINDING_ERROR", "binding")
        }
    }

    /** Exige cliente da sessão atual, sem criar sessão implícita ou replay. */
    private fun requireClient(): Client = client ?: throw SaqueFailure("PINPAD_CLOSED")

    /** Snapshot só existe após preflight completo; falha não reutiliza configuração anterior. */
    private fun requirePIN(): PaymentPinConfiguration = pinConfiguration ?: throw SaqueFailure("CONFIGURATION", "preflight")

    /** Consome o par privado; diagnóstico autorizado nunca altera resultado ou causa replay. */
    private fun consumePINResult(result: PaymentResult, operationId: String, command: String) {
        val block = result.pinBlock() ?: byteArrayOf()
        val ksn = result.ksn() ?: byteArrayOf()
        try {
            if (block.isEmpty() && ksn.isEmpty()) return
            if (block.size != 8 || ksn.size != 10) throw SaqueFailure("PROTOCOL_ERROR", "command")
            runCatching { writeEncryptedPINLog(context, logPinResults, logEnvironment, operationId, command, block, ksn) }
                .onFailure { Log.w("SaquePIN", "encrypted_pin_log_failed") }
        } finally { block.fill(0); ksn.fill(0) }
    }
}

/**
 * Configuração PIN importada e validada no core, fora de SaqueState.
 * @property method Método DUKPT TDES obtido da fonte, sem conversão automática.
 * @property keyIndex Índice legado no preflight, substituído pelo sorteio GIX ao abrir. Independente de DATA.
 * @property workingKey WORKING_KEY importada e normalizada pelo parser da fonte inteira.
 */
private class PaymentPinConfiguration(val method: String, val keyIndex: Int, val workingKey: String) {
    init { require(method == "3" && keyIndex in 0..99 && workingKey.isEmpty()) }
}

/** Lê UTF-8 estrito com limite durante o streaming; não publica conteúdo ou caminhos. */
private fun readPrivateFile(file: File, maxBytes: Long): String {
    require(file.isFile && file.length() in 1..maxBytes)
    val bytes = file.inputStream().use { input ->
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            require(output.size().toLong() + count <= maxBytes)
            output.write(buffer, 0, count)
        }
        output.toByteArray()
    }
    return Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
}

/**
 * Perfil físico; taxa/modalidade são domínio e seus campos legados são ignorados.
 * GTK exige string explícita vazia/30; ausência/null/número não escolhem em claro.
 */
internal fun parseSaqueProfile(raw: String): SaquePerfil {
    val json = JSONObject(raw)
    require(json.wholeNumber("schemaVersion") == 1L)
    val gtkDataMethod = json.get("gtkDataMethod")
    require(gtkDataMethod is String)
    val logPinResults = if (json.has("logPinResults")) {
        val flag = json.get("logPinResults")
        require(flag is Boolean)
        flag
    } else false
    return SaquePerfil(
        json.getString("environment"), json.getString("host"), Math.toIntExact(json.wholeNumber("port")),
        json.wholeNumber("timeoutMillis"), Math.toIntExact(json.wholeNumber("timeoutSeconds")), json.getString("tableAcquirer"),
        json.getString("tableVersion"), SaqueModalidade.CREDITO.applicationType, SaqueModalidade.DEBITO.applicationType,
        json.wholeNumber("maxBaseCents"), gtkDataMethod, Math.toIntExact(json.wholeNumber("gtkKeyIndex")),
        SaqueCenario.valueOf(json.getString("scenario")), logPinResults,
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
