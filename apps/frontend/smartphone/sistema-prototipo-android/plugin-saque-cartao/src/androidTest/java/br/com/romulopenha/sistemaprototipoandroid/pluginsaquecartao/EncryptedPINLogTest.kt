package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.File
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/** Usa somente pares sintéticos no target de teste, sem Bridge ou operação financeira. */
class EncryptedPINLogTest {
    @Test fun logPrivadoOptInLimiteEValidacao() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.noBackupFilesDir, "saque-cartao/pin-results.jsonl")
        val backup = file.takeIf(File::exists)?.readBytes()
        val block = ByteArray(8) { 0x11 }
        val ksn = ByteArray(10) { 0x22 }
        try {
            file.delete()
            writeEncryptedPINLog(context, false, "DES", "test-id", "GPN", block, ksn)
            assertFalse(file.exists())
            writeEncryptedPINLog(context, true, "DES", "test-id", "GOX", byteArrayOf(), byteArrayOf())
            assertFalse(file.exists())
            writeEncryptedPINLog(context, true, "DES", "test-id", "GPN", block, ksn)
            val json = JSONObject(file.readText())
            assertEquals("1111111111111111", json.getString("pinBlock"))
            assertEquals("22222222222222222222", json.getString("ksn"))
            assertEquals("GPN", json.getString("command"))
            assertFalse(json.has("pan")); assertFalse(json.has("workingKey"))
            val previous = file.readBytes()
            for (action in listOf(
                { writeEncryptedPINLog(context, true, "PRD", "test-id", "GPN", block, ksn) },
                { writeEncryptedPINLog(context, true, "DES", "test-id", "GTK", block, ksn) },
                { writeEncryptedPINLog(context, true, "DES", "test-id", "GPN", ByteArray(7), ksn) },
                { writeEncryptedPINLog(context, true, "DES", "injected\nvalue", "GPN", block, ksn) },
            )) { assertTrue(runCatching(action).isFailure); assertArrayEquals(previous, file.readBytes()) }
            file.writeText("x".repeat(65530))
            writeEncryptedPINLog(context, true, "DES", "test-id", "GOX", block, ksn)
            assertTrue(file.length() <= 65536)
            assertEquals("GOX", JSONObject(file.readText()).getString("command"))
            for (invalid in listOf("\"true\"", "1", "null")) {
                val profile = """{"schemaVersion":1,"logPinResults":$invalid}"""
                assertTrue(runCatching { parseSaqueProfile(profile) }.isFailure)
            }
        } finally {
            block.fill(0); ksn.fill(0)
            if (backup == null) file.delete() else file.writeBytes(backup)
        }
    }
}
