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
        val backupProfile = profileFile.takeIf(File::exists)?.readBytes()
        val backupTable = tableFile.takeIf(File::exists)?.readBytes()
        try {
            profileFile.writeText(profile)
            for (table in listOf("[]", "invalid", "[\"01110201ABC\"]", "[\"01010101ABC\"]")) {
                tableFile.writeText(table)
                val failed = runCatching { GoSaqueRepository(context).loadProfile() }.exceptionOrNull()
                assertTrue(failed is SaqueFailure)
                assertEquals("CONFIGURATION", (failed as SaqueFailure).code)
                assertEquals("preflight", failed.phase)
            }
            tableFile.writeText("[\"01110101ABC\",\"01220101ABCD\"]")
            assertEquals("01", GoSaqueRepository(context).loadProfile().tableAcquirer)
            tableFile.delete()
            assertEquals("TABLE_MISSING", (runCatching { GoSaqueRepository(context).loadProfile() }.exceptionOrNull() as SaqueFailure).code)
            profileFile.delete()
            assertEquals("MISSING", (runCatching { GoSaqueRepository(context).loadProfile() }.exceptionOrNull() as SaqueFailure).code)
        } finally {
            if (backupProfile == null) profileFile.delete() else profileFile.writeBytes(backupProfile)
            if (backupTable == null) tableFile.delete() else tableFile.writeBytes(backupTable)
        }
    }
}
