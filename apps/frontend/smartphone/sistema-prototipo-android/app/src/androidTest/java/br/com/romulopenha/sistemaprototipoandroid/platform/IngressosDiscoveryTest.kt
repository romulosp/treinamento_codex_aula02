package br.com.romulopenha.sistemaprototipoandroid.platform

import android.view.View
import androidx.test.platform.app.InstrumentationRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test

/** Confere coexistência dos APKs de negócio no menu gerado pelo host. */
class IngressosDiscoveryTest {
    @Test fun descobreIngressoEOperacoes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val latch = CountDownLatch(1)
        var items = emptyList<BusinessMenuItem>()
        var error: Throwable? = null
        val manager = BusinessPluginManager(context, { items = it; latch.countDown() },
            { error = it; latch.countDown() }, {})
        try {
            manager.startAfterSession()
            assertTrue(latch.await(20, TimeUnit.SECONDS))
            assertNull(error)
            assertTrue(items.any { it.titulo == "Operações" })
            val ingressos = requireNotNull(items.singleOrNull { it.titulo == "Venda de ingressos" })
            var view: View? = null
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                view = manager.createScreen(ingressos.id, context)
            }
            assertNotNull(view)
        } finally {
            manager.close()
        }
    }
}
