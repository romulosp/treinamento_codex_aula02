package br.com.romulopenha.diagnosticopinpad.data

import android.content.Context
import java.io.File
import java.time.Instant

/**
 * Persiste eventos técnicos sanitizados em JSON Lines no diretório privado do
 * aplicativo. Payload ABECS e dados financeiros nunca são aceitos pelo
 * contrato do logger.
 */
interface AppLogger {
    fun event(
        level: String,
        event: String,
        operationId: String? = null,
        fields: Map<String, String> = emptyMap(),
    )
}

/** Logger limitado para diagnóstico local, sem permissão de armazenamento amplo. */
class JsonLineLogger(context: Context) : AppLogger {
    private val logFile: File = File(
        context.getExternalFilesDir("logs") ?: File(context.filesDir, "logs"),
        "diagnosticopinpad.jsonl",
    )

    @Synchronized
    override fun event(
        level: String,
        event: String,
        operationId: String?,
        fields: Map<String, String>,
    ) {
        val safeFields = fields.entries
            .filter { it.key in ALLOWED_FIELDS }
            .associate { it.key to sanitize(it.value) }
        val values = linkedMapOf<String, String>(
            "timestamp" to Instant.now().toString(),
            "level" to sanitize(level),
            "event" to sanitize(event),
        )
        operationId?.let { values["operationId"] = sanitize(it) }
        values.putAll(safeFields)
        val json = values.entries.joinToString(",", prefix = "{", postfix = "}") {
            "\"${escape(it.key)}\":\"${escape(it.value)}\""
        }
        logFile.parentFile?.mkdirs()
        if (logFile.exists() && logFile.length() > MAX_BYTES) {
            logFile.delete()
        }
        logFile.appendText("$json\n")
    }

    private fun sanitize(value: String): String = value
        .replace(Regex("[\\u0000-\\u001F\\u007F]"), " ")
        .take(MAX_FIELD_LENGTH)

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")

    private companion object {
        const val MAX_BYTES = 1024L * 1024L
        const val MAX_FIELD_LENGTH = 160
        val ALLOWED_FIELDS = setOf(
            "host",
            "port",
            "timeoutMillis",
            "state",
            "durationMillis",
            "errorCode",
            "bytesTx",
            "bytesRx",
            "source",
        )
    }
}
