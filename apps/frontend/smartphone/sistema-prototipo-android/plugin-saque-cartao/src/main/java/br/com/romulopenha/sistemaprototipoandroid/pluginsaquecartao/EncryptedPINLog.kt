package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import android.content.Context
import android.content.pm.ApplicationInfo
import java.io.File
import java.time.Instant
import org.json.JSONObject

/**
 * Registra somente o par criptografado autorizado em DES debug, mediante opt-in.
 * Destino no sandbox/noBackup: saque-cartao/pin-results.jsonl, máximo 64 KiB.
 * Ao exceder, reinicia o log diagnóstico. Não escreve Logcat/Bridge/UI, PAN, WK
 * ou PIN em claro. Chamador executa em IO, limpa seus buffers e trata falha sem replay.
 * Par vazio representa ausência de PIN online e não gera registro; parcial é inválido.
 */
internal fun writeEncryptedPINLog(
    context: Context, enabled: Boolean, environment: String, operationId: String,
    command: String, pinBlock: ByteArray, ksn: ByteArray,
) {
    if (!enabled || (pinBlock.isEmpty() && ksn.isEmpty())) return
    require(context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0 && environment == "DES")
    require(command in setOf("GPN", "GOX") && operationId.matches(Regex("[A-Za-z0-9-]{1,128}")))
    require(pinBlock.size == 8 && ksn.size == 10)
    val entry = JSONObject().put("schemaVersion", 1).put("timestamp", Instant.now().toString())
        .put("operationId", operationId).put("command", command)
        .put("pinBlock", encryptedHex(pinBlock)).put("ksn", encryptedHex(ksn)).toString() + "\n"
    val directory = File(context.noBackupFilesDir, "saque-cartao")
    check(directory.mkdirs() || directory.isDirectory)
    val file = File(directory, "pin-results.jsonl")
    if (file.length() + entry.toByteArray(Charsets.UTF_8).size > 65536) file.writeText("", Charsets.UTF_8)
    file.appendText(entry, Charsets.UTF_8)
}

/** Representa bytes criptografados em hexadecimal; jamais recebe PIN digitado ou chave. */
private fun encryptedHex(value: ByteArray): String =
    value.joinToString("") { "%02X".format(it.toInt() and 0xff) }
