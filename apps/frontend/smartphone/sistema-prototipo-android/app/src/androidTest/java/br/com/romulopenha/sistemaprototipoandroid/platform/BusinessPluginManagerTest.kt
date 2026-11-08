package br.com.romulopenha.sistemaprototipoandroid.platform

import android.view.View
import android.os.Build
import dalvik.system.DexClassLoader
import java.io.File
import androidx.test.platform.app.InstrumentationRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Valida discovery e criação de tela usando os APKs reais empacotados no host debug. */
class BusinessPluginManagerTest {
    private var manager: BusinessPluginManager? = null

    @After
    fun tearDown() {
        manager?.close()
    }

    @Test
    fun descobreConsumidorESaqueSomenteComoComponente() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val loaded = CountDownLatch(1)
        var items = emptyList<BusinessMenuItem>()
        var fatal: Throwable? = null
        manager = BusinessPluginManager(
            context = context,
            onItems = { discovered -> items = discovered; loaded.countDown() },
            onFatal = { error -> fatal = error; loaded.countDown() },
            onNavigateToMenu = {},
        )

        manager!!.startAfterSession()

        assertTrue("Discovery não terminou", loaded.await(20, TimeUnit.SECONDS))
        assertTrue("Falha fatal: $fatal", fatal == null)
        assertTrue("Saque não pode aparecer no menu", items.none { it.titulo == "Outros Serviços" })
        val consumer = requireNotNull(items.singleOrNull { it.titulo == "Operações" })
        var view: View? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            view = manager!!.createScreen(consumer.id, context)
        }
        assertNotNull("View do plugin não foi criada", view)
        val expectedDigest = context.assets.open("plugins/components/plugin-saque-cartao.apk").use {
            java.security.MessageDigest.getInstance("SHA-256").digest(it.readBytes())
                .joinToString("") { byte -> "%02x".format(byte) }
        }
        assertEquals(expectedDigest, File(context.filesDir, "plugins/components/selected.sha256").readText())
        val verified = LoginPluginLoader.findVerified(context, {}, LoginPluginLoader.ComponentRequest)
            .single { it.digestSha256 == expectedDigest }
        val optimized = File(context.codeCacheDir, "plugins/${verified.digestSha256}")
        java.util.zip.ZipFile(verified.apk).use { zip ->
            assertNotNull(zip.getEntry("META-INF/services/br.com.romulopenha.sistemaprototipoandroid.sharedapi.ICardWithdrawalComponent"))
            assertTrue(zip.getEntry("META-INF/services/br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginNegocioApp") == null)
        }
        val native = Build.SUPPORTED_ABIS.map { File(optimized, "native/$it") }.first { it.isDirectory }
        assertTrue("libgojni não extraída", File(native, "libgojni.so").isFile)
        val loader = DexClassLoader(verified.apk.absolutePath, optimized.absolutePath, native.absolutePath, context.classLoader)
        val mobile = loader.loadClass("br.com.romulopenha.libpinpadabecsgo.mobile.Mobile")
        val version = mobile.getMethod("version").invoke(null) as String
        assertTrue("Go não inicializou com proveniência na ABI do host",
            version.contains("revision=") && !version.contains("revision=unknown"))
        mobile.getMethod("validateFormattedEMVTableJSON", String::class.java, String::class.java, String::class.java)
            .invoke(null, "01", "TEST000001", "[\"01110101ABC\"]")
    }
}
