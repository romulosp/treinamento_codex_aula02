package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Valida arquivos privados sintéticos no adapter real sem cliente, Bridge ou pinpad. */
class SaquePreflightTest {
    private val profile = """{"schemaVersion":1,"environment":"DES","host":"localhost","port":39100,
        "timeoutMillis":5000,"timeoutSeconds":5,"tableAcquirer":"01","tableVersion":"TEST000001",
        "creditApplicationTypes":"01","debitApplicationTypes":"02","maxBaseCents":100000,
        "gtkDataMethod":"30","gtkKeyIndex":7,"scenario":"APPROVE"}"""

    @Test fun arquivosInvalidosFalhamAntesDeCriarCliente() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dir = File(context.filesDir, "saque-cartao").apply { mkdirs() }
        val profileFile = File(dir, "perfil.json")
        val tableFile = File(dir, "emv.json")
        val cryptoFile = File(dir, "criptografia.properties")
        val backupProfile = profileFile.takeIf(File::exists)?.readBytes()
        val backupTable = tableFile.takeIf(File::exists)?.readBytes()
        val backupCrypto = cryptoFile.takeIf(File::exists)?.readBytes()
        try {
            profileFile.writeText(profile)
            cryptoFile.writeText("TIPO_CHAVE=3\nINDICE_CHAVE=17\nWORKING_KEY=\"\"\n")
            for (table in listOf("[]", "invalid", "[\"01110201ABC\"]", "[\"01010101ABC\"]")) {
                tableFile.writeText(table)
                val failed = runCatching { GoSaqueRepository(context).loadProfile() }.exceptionOrNull()
                assertTrue(failed is SaqueFailure)
                assertEquals("CONFIGURATION", (failed as SaqueFailure).code)
                assertEquals("preflight", failed.phase)
            }
            fun record(index: String, app: String, aid: String) =
                "284101" + index + (aid.length / 2).toString().padStart(2, '0') + aid.padEnd(32, '0') + app + "0".repeat(240)
            tableFile.writeText(org.json.JSONArray(listOf(
                record("01", "01", "A0000000990101"), record("02", "02", "A000000099020101"),
            )).toString())
            assertEquals("01", GoSaqueRepository(context).loadProfile().tableAcquirer)
            assertFalse(GoSaqueRepository(context).loadProfile().logPinResults)
            profileFile.writeText(org.json.JSONObject(profile).put("logPinResults", true).toString())
            assertTrue(GoSaqueRepository(context).loadProfile().logPinResults)
            profileFile.writeText(org.json.JSONObject(profile).put("gtkDataMethod", "").toString())
            assertEquals("", GoSaqueRepository(context).loadProfile().gtkDataMethod)
            profileFile.writeText(profile)
            val repository = GoSaqueRepository(context)
            repository.loadProfile()
            for (raw in listOf("TIPO_CHAVE=1\nINDICE_CHAVE=17\nWORKING_KEY=", "TIPO_CHAVE=3\nINDICE_CHAVE=17",
                "TIPO_CHAVE=3\nINDICE_CHAVE=17\nWORKING_KEY=\nEXTRA=synthetic")) {
                cryptoFile.writeText(raw)
                assertEquals("CONFIGURATION", (runCatching { repository.loadProfile() }.exceptionOrNull() as SaqueFailure).code)
            }
            cryptoFile.delete()
            assertEquals("CRYPTO_MISSING", (runCatching { repository.loadProfile() }.exceptionOrNull() as SaqueFailure).code)
            cryptoFile.writeText("TIPO_CHAVE=3\nINDICE_CHAVE=17\nWORKING_KEY=\n")
            tableFile.delete()
            assertEquals("TABLE_MISSING", (runCatching { GoSaqueRepository(context).loadProfile() }.exceptionOrNull() as SaqueFailure).code)
            profileFile.delete()
            assertEquals("MISSING", (runCatching { GoSaqueRepository(context).loadProfile() }.exceptionOrNull() as SaqueFailure).code)
        } finally {
            if (backupProfile == null) profileFile.delete() else profileFile.writeBytes(backupProfile)
            if (backupTable == null) tableFile.delete() else tableFile.writeBytes(backupTable)
            if (backupCrypto == null) cryptoFile.delete() else cryptoFile.writeBytes(backupCrypto)
        }
    }
}
