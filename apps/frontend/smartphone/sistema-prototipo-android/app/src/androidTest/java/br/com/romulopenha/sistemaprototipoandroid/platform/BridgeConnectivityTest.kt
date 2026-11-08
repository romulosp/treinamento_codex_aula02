package br.com.romulopenha.sistemaprototipoandroid.platform

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import dalvik.system.DexClassLoader
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.json.JSONObject

/**
 * Diagnóstico opt-in do binding real no host; Ping não adquire COM nem envia ABECS.
 * Requer argumento realBridge=true e bridgeHost local explicitamente selecionado.
 */
class BridgeConnectivityTest {
    @Test fun pingRealMantemSessaoFisicaFechada() {
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue(arguments.getString("realBridge") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val selectedHost = requireNotNull(arguments.getString("bridgeHost"))
        val host = if (selectedHost == "profile") {
            JSONObject(File(context.filesDir, "saque-cartao/perfil.json").readText()).getString("host")
        } else selectedHost
        require(host in setOf("localhost", "127.0.0.1", "10.0.2.2"))
        val latch = CountDownLatch(1)
        var failure: Throwable? = null
        val manager = BusinessPluginManager(context, { latch.countDown() },
            { failure = it; latch.countDown() }, {})
        try {
            manager.startAfterSession()
            assertTrue("Discovery não concluiu", latch.await(30, TimeUnit.SECONDS))
            assertNull(failure)
            val selected = File(context.filesDir, "plugins/components/selected.sha256").readText().trim()
            val verified = LoginPluginLoader.findVerified(context, {}, LoginPluginLoader.ComponentRequest)
                .single { it.digestSha256 == selected }
            val optimized = File(context.codeCacheDir, "plugins/${verified.digestSha256}")
            val native = Build.SUPPORTED_ABIS.map { File(optimized, "native/$it") }.first { it.isDirectory }
            val loader = DexClassLoader(verified.apk.absolutePath, optimized.absolutePath, native.absolutePath, context.classLoader)
            val mobile = loader.loadClass("br.com.romulopenha.libpinpadabecsgo.mobile.Mobile")
            val client = mobile.getMethod("newClient", String::class.java, Long::class.javaPrimitiveType,
                Long::class.javaPrimitiveType).invoke(null, host, 39100L, 3000L)
            try {
                client.javaClass.getMethod("ping", String::class.java).invoke(client, "android-binding-check")
                assertEquals("CLOSED", client.javaClass.getMethod("getState").invoke(client))
            } catch (error: InvocationTargetException) {
                val encoded = error.targetException.message.orEmpty()
                val code = mobile.getMethod("errorCode", String::class.java).invoke(null, encoded)
                val phase = mobile.getMethod("errorPhase", String::class.java).invoke(null, encoded)
                fail("Binding real: host=$host code=$code phase=$phase")
            } finally {
                client.javaClass.getMethod("close", String::class.java).invoke(client, "android-binding-cleanup")
            }
        } finally {
            manager.close()
        }
    }
}
