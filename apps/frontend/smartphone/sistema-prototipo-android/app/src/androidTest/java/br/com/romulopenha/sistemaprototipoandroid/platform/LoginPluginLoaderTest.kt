package br.com.romulopenha.sistemaprototipoandroid.platform

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

/** Exercita as fronteiras de rejeição e o parser do manifesto no dispositivo. */
class LoginPluginLoaderTest {
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun arquivoVazioGeraRejectedEAposLimpeza() {
        withTemporaryStaging { staging ->
            val candidate = File(staging, "empty.apk").apply { createNewFile() }
            val transitions = mutableListOf<PluginTransition>()

            assertThrows(IllegalArgumentException::class.java) {
                LoginPluginLoader.stageAndVerify(context, staging, candidate, transitions::add)
            }

            assertEquals(PluginRuntimeStatus.REJECTED, transitions.last().status)
            assertFalse(candidate.exists())
        }
    }

    @Test
    fun arquivoQueNaoEhZipGeraRejectedEAposQuarentena() {
        withTemporaryStaging { staging ->
            val candidate = File(staging, "invalid.apk").apply { writeText("not-an-apk") }
            val transitions = mutableListOf<PluginTransition>()

            assertThrows(Exception::class.java) {
                LoginPluginLoader.stageAndVerify(context, staging, candidate, transitions::add)
            }

            assertEquals(PluginRuntimeStatus.REJECTED, transitions.last().status)
            assertFalse(candidate.exists())
        }
    }

    @Test
    fun manifestoExigeJsonEstruturadoEDependencias() {
        val descriptor = LoginPluginLoader.PluginManifestParser.parse(
            """
            {
              "schemaVersion": 1,
              "pluginId": "br.com.romulopenha.sistemaprototipoandroid.login",
              "displayName": "Login",
              "pluginVersion": "1.0.1",
              "requiredSharedApiMajor": 1,
              "requiredSharedApiMinor": 0,
              "entryClass": "br.com.romulopenha.sistemaprototipoandroid.pluginlogin.PluginLoginApp",
              "declaredPackageName": "br.com.romulopenha.sistemaprototipoandroid.pluginlogin",
              "priority": 100,
              "capabilities": ["startup-auth"],
              "dependencies": []
            }
            """.trimIndent(),
        )
        assertEquals("1.0.1", descriptor.pluginVersion)
        assertEquals(emptySet<String>(), descriptor.dependencies)

        assertThrows(Exception::class.java) {
            LoginPluginLoader.PluginManifestParser.parse("{\"pluginId\":\"only-partial\"}")
        }
        assertThrows(Exception::class.java) {
            LoginPluginLoader.PluginManifestParser.parse("texto que nao eh json")
        }
        assertThrows(Exception::class.java) {
            LoginPluginLoader.PluginManifestParser.parse("{\"schemaVersion\":\"1\"}")
        }
    }

    private fun withTemporaryStaging(block: (File) -> Unit) {
        val staging = File(context.cacheDir, "plugin-review-${System.nanoTime()}").apply { mkdirs() }
        try {
            block(staging)
        } finally {
            staging.deleteRecursively()
        }
    }
}
