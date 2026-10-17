package br.com.romulopenha.sistemaprototipoandroid.platform

import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes do transporte atômico do APK de bootstrap até o inbox. */
class BundledLoginPluginInstallerTest {
    @Test
    fun `publica somente arquivo apk completo`() {
        val staging = Files.createTempDirectory("plugin-bootstrap-test").toFile()
        val content = "apk-debug".toByteArray()

        try {
            val installed = BundledLoginPluginInstaller.install(ByteArrayInputStream(content), staging)

            assertTrue(installed.name.startsWith("plugin-login-"))
            assertTrue(installed.name.endsWith(".apk"))
            assertArrayEquals(content, installed.readBytes())
            assertFalse(staging.listFiles().orEmpty().any { it.extension == "upload" })
        } finally {
            staging.deleteRecursively()
        }
    }

    @Test
    fun `entrega repetida preserva o mesmo candidato`() {
        val staging = Files.createTempDirectory("plugin-bootstrap-test").toFile()
        val content = "mesmo-apk".toByteArray()

        try {
            val first = BundledLoginPluginInstaller.install(ByteArrayInputStream(content), staging)
            val second = BundledLoginPluginInstaller.install(ByteArrayInputStream(content), staging)

            assertEquals(first.canonicalFile, second.canonicalFile)
            assertEquals(1, staging.listFiles().orEmpty().count { it.extension == "apk" })
            assertFalse(staging.listFiles().orEmpty().any { it.extension == "upload" })
        } finally {
            staging.deleteRecursively()
        }
    }

    @Test
    fun `prioriza revisao incorporada antes dos candidatos antigos`() {
        val staging = Files.createTempDirectory("plugin-bootstrap-order-test").toFile()
        try {
            val older = File(staging, "older.apk").apply {
                writeText("old")
                setLastModified(1L)
            }
            val bundled = File(staging, "bundled.apk").apply {
                writeText("current")
                setLastModified(3L)
            }
            val newer = File(staging, "newer.apk").apply {
                writeText("new")
                setLastModified(2L)
            }

            val ordered = BundledLoginPluginInstaller.prioritize(
                bundledCandidate = bundled,
                inboxCandidates = listOf(older, bundled, newer),
            )

            assertEquals(listOf(bundled, older, newer), ordered)
        } finally {
            staging.deleteRecursively()
        }
    }
}
