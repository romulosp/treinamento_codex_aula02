package br.com.romulopenha.sistemaprototipoandroid.platform

import android.view.View
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
    fun descobreSaqueCartaoECriaSuaView() {
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
        val outrosServicos = items.singleOrNull { it.titulo == "Outros Serviços" }
        assertNotNull("Caminho declarado pelo plugin não gerou o agrupador", outrosServicos)
        val saque = requireNotNull(outrosServicos).filhos.singleOrNull { it.titulo == "Saque Cartão" }
        assertNotNull("Saque Cartão não encontrado no menu", saque)
        assertEquals("principal-outros-serviços-saque-cartão", requireNotNull(saque).id)
        var view: View? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            view = manager!!.createScreen(saque.id, context)
        }
        assertNotNull("View do plugin não foi criada", view)
    }
}
