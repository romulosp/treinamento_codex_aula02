package br.com.romulopenha.sistemaprototipoandroid.platform

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.viewinterop.AndroidView
import androidx.test.platform.app.InstrumentationRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Exercita contrato de lifecycle com componente sintético, sem acesso a hardware. */
class ComponentPluginManagerTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun personalizacaoNaoRecuaParaProvedor14QueIgnoraOpcoes() {
        val fake = OldCardPaymentComponent()
        val manager = ComponentPluginManager(context, { _, _ -> null }, fake)
        lateinit var container: FrameLayout
        compose.setContent {
            AndroidView(factory = { ctx -> FrameLayout(ctx).apply { addView(View(ctx)); container = this } })
        }
        compose.runOnIdle {
            var result: CardWithdrawalResult? = null
            manager.launchPayment(container, container.getChildAt(0), CardPaymentRequest("options-test", 4800),
                CardPaymentOptions(CardPaymentResultPresentation.CALLER)) { result = it }
            assertEquals(CardWithdrawalStatus.UNAVAILABLE, result!!.status)
            assertEquals(0, fake.payments)
            assertEquals(1, container.childCount)
            manager.close()
        }
    }

    @Test fun pedidoComValorNaoRecuaParaComponenteLegadoEditavel() {
        val fake = SyntheticCardComponent()
        val manager = ComponentPluginManager(context, { _, _ -> null }, fake)
        lateinit var container: FrameLayout
        compose.setContent {
            AndroidView(factory = { ctx -> FrameLayout(ctx).apply { addView(View(ctx)); container = this } })
        }
        compose.runOnIdle {
            var result: CardWithdrawalResult? = null
            manager.launchPayment(container, container.getChildAt(0), CardPaymentRequest("pedido", 3000)) { result = it }
            assertEquals(CardWithdrawalStatus.UNAVAILABLE, result!!.status)
            assertEquals(0, fake.created)
            manager.close()
        }
    }

    @Test fun falhaDaFabricaNaoSimulaSucessoEBloqueiaNovaChamada() {
        val fake = SyntheticCardComponent(failCreation = true)
        val manager = ComponentPluginManager(context, { _, _ -> null }, fake)
        lateinit var container: FrameLayout
        val results = mutableListOf<CardWithdrawalResult>()
        compose.setContent {
            AndroidView(factory = { ctx -> FrameLayout(ctx).apply { addView(View(ctx)); container = this } })
        }
        compose.runOnIdle {
            manager.launch(container, container.getChildAt(0)) { results += it }
            manager.launch(container, container.getChildAt(0)) { results += it }
            assertEquals(listOf(CardWithdrawalStatus.ERROR, CardWithdrawalStatus.BUSY), results.map { it.status })
            assertTrue(results.none { it.cleanupConfirmed })
            assertEquals(1, fake.created)
            assertEquals(View.VISIBLE, container.getChildAt(0).visibility)
        }
    }

    @Test fun retornaUmaVezERestauraMesmaView() {
        val fake = SyntheticCardComponent()
        val manager = ComponentPluginManager(context, { _, _ -> null }, fake)
        lateinit var container: FrameLayout
        lateinit var caller: View
        var calls = 0
        compose.setContent {
            AndroidView(factory = { ctx ->
                FrameLayout(ctx).also {
                    caller = TextView(ctx).apply { text = "Consumidor" }
                    it.addView(caller)
                    container = it
                }
            })
        }
        compose.runOnIdle {
            manager.launch(container, caller) { calls++ }
            assertEquals(View.GONE, caller.visibility)
            assertEquals(2, container.childCount)
            fake.complete(CardWithdrawalResult(CardWithdrawalStatus.CAPTURED, true))
            fake.complete(CardWithdrawalResult(CardWithdrawalStatus.CAPTURED, true))
        }
        compose.runOnIdle {
            assertEquals(1, calls)
            assertEquals(1, container.childCount)
            assertSame(caller, container.getChildAt(0))
            assertEquals(View.VISIBLE, caller.visibility)
        }
    }

    @Test fun concorrenciaRetornaBusyEAguardaCancelamento() {
        val fake = SyntheticCardComponent()
        val manager = ComponentPluginManager(context, { _, _ -> null }, fake)
        lateinit var container: FrameLayout
        val results = mutableListOf<CardWithdrawalStatus>()
        compose.setContent {
            AndroidView(factory = { ctx -> FrameLayout(ctx).apply { addView(View(ctx)); container = this } })
        }
        compose.runOnIdle {
            manager.launch(container, container.getChildAt(0)) { results += it.status }
            manager.launch(container, container.getChildAt(0)) { results += it.status }
            assertEquals(listOf(CardWithdrawalStatus.BUSY), results)
            assertEquals(1, fake.created)
            assertTrue(manager.cancel())
            assertEquals(1, fake.canceled)
            assertEquals(2, container.childCount)
            fake.complete(CardWithdrawalResult(CardWithdrawalStatus.CANCELED, true))
        }
        compose.runOnIdle {
            assertEquals(listOf(CardWithdrawalStatus.BUSY, CardWithdrawalStatus.CANCELED), results)
            assertFalse(manager.cancel())
        }
    }

    @Test fun abandonoSuprimeCallbackEEsperaCleanupAntesDeNovaSessao() {
        val fake = SyntheticCardComponent()
        val manager = ComponentPluginManager(context, { _, _ -> null }, fake)
        lateinit var container: FrameLayout
        var calls = 0
        compose.setContent {
            AndroidView(factory = { ctx -> FrameLayout(ctx).apply { addView(View(ctx)); container = this } })
        }
        compose.runOnIdle {
            manager.launch(container, container.getChildAt(0)) { calls++ }
            manager.abandon(container)
            assertEquals(1, fake.canceled)
            fake.complete(CardWithdrawalResult(CardWithdrawalStatus.CANCELED, true))
        }
        compose.runOnIdle {
            assertEquals(0, calls)
            assertEquals(1, container.childCount)
            manager.launch(container, container.getChildAt(0)) { calls++ }
            assertEquals(2, fake.created)
        }
    }

    @Test fun cleanupFalhoBloqueiaReabertura() {
        val fake = SyntheticCardComponent()
        val manager = ComponentPluginManager(context, { _, _ -> null }, fake)
        lateinit var container: FrameLayout
        val results = mutableListOf<CardWithdrawalResult>()
        compose.setContent {
            AndroidView(factory = { ctx -> FrameLayout(ctx).apply { addView(View(ctx)); container = this } })
        }
        compose.runOnIdle {
            manager.launch(container, container.getChildAt(0)) { results += it }
            fake.complete(CardWithdrawalResult(CardWithdrawalStatus.UNKNOWN, false))
        }
        compose.runOnIdle {
            manager.launch(container, container.getChildAt(0)) { results += it }
            assertEquals(listOf(CardWithdrawalStatus.UNKNOWN, CardWithdrawalStatus.BUSY), results.map { it.status })
            assertFalse(results.last().cleanupConfirmed)
            assertEquals(1, fake.created)
        }
    }

    @Test fun componenteAusenteRetornaIndisponivelSemCriarView() {
        val manager = ComponentPluginManager(context, { _, _ -> null })
        lateinit var container: FrameLayout
        compose.setContent {
            AndroidView(factory = { ctx -> FrameLayout(ctx).apply { addView(View(ctx)); container = this } })
        }
        compose.runOnIdle {
            var result: CardWithdrawalResult? = null
            manager.launch(container, container.getChildAt(0)) { result = it }
            assertEquals(CardWithdrawalStatus.UNAVAILABLE, result!!.status)
            assertEquals(1, container.childCount)
        }
    }

    @Test fun consumidorRealChamaComponenteEResultadoVoltaNaMesmaTela() {
        val fake = SyntheticCardComponent()
        val loaded = CountDownLatch(1)
        var items = emptyList<BusinessMenuItem>()
        var fatal: Throwable? = null
        val manager = BusinessPluginManager(context,
            onItems = { items = it; loaded.countDown() },
            onFatal = { fatal = it; loaded.countDown() },
            onNavigateToMenu = {},
            componentOverride = fake)
        try {
            manager.startAfterSession()
            assertTrue(loaded.await(30, TimeUnit.SECONDS))
            assertNull(fatal)
            val consumer = items.single { it.titulo == "Operações" }
            compose.setContent {
                AndroidView(factory = { ctx -> requireNotNull(manager.createScreen(consumer.id, ctx)) })
            }
            compose.onNodeWithText("Abrir pagamento com cartão").performClick()
            compose.runOnIdle {
                assertEquals(1, fake.created)
                assertTrue(manager.cancelComponent())
                fake.complete(CardWithdrawalResult(CardWithdrawalStatus.CANCELED, true))
            }
            compose.onNodeWithText("Resultado local: CANCELED").assertIsDisplayed()
            compose.onNodeWithText("Abrir pagamento com cartão").assertIsDisplayed()
        } finally {
            compose.runOnIdle { manager.close() }
        }
    }
}

/** Fixture de sessão manualmente concluída; nunca abre pinpad nem usa o adapter Go. */
private class SyntheticCardComponent(private val failCreation: Boolean = false) : ICardWithdrawalComponent {
    var created = 0
    var canceled = 0
    private lateinit var callback: (CardWithdrawalResult) -> Unit
    override val manifest = PluginManifest("synthetic", "synthetic", "1.0.0", 1, 3,
        "synthetic.Entry", setOf("card-withdrawal-component"))
    override fun onLoad(host: PluginHostContext) = Unit
    override fun onAttach(registry: IUIRegistry, router: IPluginRouter) = Unit
    override fun onActivate() = Unit
    override fun onDetach() = Unit
    override fun createWithdrawal(context: Context, onComplete: (CardWithdrawalResult) -> Unit): CardWithdrawalSession {
        created++
        if (failCreation) error("Falha sintética de criação")
        callback = onComplete
        return object : CardWithdrawalSession {
            override val view = TextView(context).apply { text = "Componente sintético" }
            override fun cancel() { canceled++ }
        }
    }
    fun complete(result: CardWithdrawalResult) = callback(result)
}

/** Provedor 1.4 conhece valor fixo, mas não conhece a personalização 1.5. */
private class OldCardPaymentComponent : ICardPaymentComponent {
    var payments = 0
    override val manifest = PluginManifest("old-payment", "old", "1.4.0", 1, 4,
        "synthetic.Old", setOf("card-withdrawal-component"))
    override fun onLoad(host: PluginHostContext) = Unit
    override fun onAttach(registry: IUIRegistry, router: IPluginRouter) = Unit
    override fun onActivate() = Unit
    override fun onDetach() = Unit
    override fun createWithdrawal(context: Context, onComplete: (CardWithdrawalResult) -> Unit): CardWithdrawalSession =
        error("Não deve abrir caminho legado")
    override fun createPayment(context: Context, request: CardPaymentRequest,
        onComplete: (CardWithdrawalResult) -> Unit): CardWithdrawalSession {
        payments++
        return object : CardWithdrawalSession {
            override val view = TextView(context)
            override fun cancel() = Unit
        }
    }
}
