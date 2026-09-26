package br.com.romulopenha.sistemaprototipoandroid.pluginlogin

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

/** Resultado seguro da validação, sem tokens nem credenciais. */
internal sealed interface AuthenticationResult {
    /** Sessão opaca criada pela API. */
    data class Success(val sessionId: String, val expiresAtEpochMillis: Long) : AuthenticationResult

    /** O provedor recusou usuário ou senha. */
    data object InvalidCredentials : AuthenticationResult

    /** A API ou o provedor não pôde concluir a tentativa. */
    data object Unavailable : AuthenticationResult
}

/** Porta usada pelo estado de login para autenticar e encerrar uma sessão. */
internal interface CredentialAuthenticator {
    /** Executa autenticação fora da main thread e não persiste os argumentos. */
    suspend fun authenticate(user: String, password: String): AuthenticationResult

    /** Encerra a sessão; deve ser chamado pelo host fora da main thread. */
    fun logout(sessionId: String): Boolean
}

/** Cliente HTTP mínimo para a API de autenticação do protótipo. */
internal class BackendCredentialAuthenticator(
    private val baseUrl: String = BuildConfig.AUTH_API_BASE_URL,
) : CredentialAuthenticator {
    override suspend fun authenticate(user: String, password: String): AuthenticationResult =
        withContext(Dispatchers.IO) {
            val payload = JSONObject()
                .put("usuario", user)
                .put("senha", password)
                .toString()
            try {
                val response = post("/contexto/autenticacao-validacao-credencial", payload)
                parseAuthenticationResponse(response.statusCode, response.body)
            } catch (_: IOException) {
                AuthenticationResult.Unavailable
            } catch (_: JSONException) {
                AuthenticationResult.Unavailable
            } catch (_: IllegalArgumentException) {
                AuthenticationResult.Unavailable
            }
        }

    override fun logout(sessionId: String): Boolean {
        val payload = JSONObject().put("sessaoId", sessionId).toString()
        return try {
            post("/contexto/autenticacao-validacao-credencial/logout", payload).statusCode ==
                HttpURLConnection.HTTP_NO_CONTENT
        } catch (_: IOException) {
            false
        } catch (_: JSONException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    /**
     * Executa POST com timeouts finitos e fecha a conexão em todos os caminhos.
     * O conteúdo da requisição e da resposta nunca é registrado.
     */
    private fun post(path: String, payload: String): HttpResponse {
        val connection = URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
            connection.readTimeout = READ_TIMEOUT_MILLIS
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(payload) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            HttpResponse(status, stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty())
        } finally {
            connection.disconnect()
        }
    }

    private data class HttpResponse(val statusCode: Int, val body: String)

    private companion object {
        const val CONNECT_TIMEOUT_MILLIS = 5_000
        const val READ_TIMEOUT_MILLIS = 10_000
    }
}

/** Converte somente o contrato público da API em estado de autenticação. */
internal fun parseAuthenticationResponse(statusCode: Int, body: String): AuthenticationResult = when (statusCode) {
    HttpURLConnection.HTTP_OK -> {
        val json = JSONObject(body)
        val sessionId = json.optString("sessaoId")
        val expiresAt = json.optLong("expiraEmEpochMillis")
        if (json.optBoolean("autenticado") && sessionId.isNotBlank() && expiresAt > 0) {
            AuthenticationResult.Success(sessionId, expiresAt)
        } else {
            AuthenticationResult.Unavailable
        }
    }
    HttpURLConnection.HTTP_UNAUTHORIZED -> AuthenticationResult.InvalidCredentials
    else -> AuthenticationResult.Unavailable
}
