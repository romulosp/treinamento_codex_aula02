package br.com.romulopenha.sistemaprototipoandroid.platform

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.viewinterop.AndroidView
import androidx.test.platform.app.InstrumentationRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Valida a chamada do APK real de ingressos ao componente sintético por meio do host. */
class IngressosCardIntegrationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun aprovacaoVoltaAoSucessoDaBilheteria() = assertCompletion(CardWithdrawalStatus.APPROVED)

    @Test fun capturaVoltaAoSucessoDemonstrativoSemConverterStatus() = assertCompletion(CardWithdrawalStatus.CAPTURED)

    @Test fun capturaPedidoMistoDeShowPreservaTotalEEstoque() = assertCompletion(CardWithdrawalStatus.CAPTURED, mixed = true)

    /** Confere encaminhamento da opção e conclusão local da bilheteria para ambos os estados aceitos. */
    private fun assertCompletion(status: CardWithdrawalStatus, mixed: Boolean = false) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val fake = TestCardComponent()
        val latch = CountDownLatch(1)
        var items = emptyList<BusinessMenuItem>()
        var error: Throwable? = null
        val manager = BusinessPluginManager(context, { items = it; latch.countDown() },
            { error = it; latch.countDown() }, {}, componentOverride = fake)
        try {
            manager.startAfterSession()
            assertTrue(latch.await(30, TimeUnit.SECONDS))
            assertNull(error)
            val item = items.single { it.titulo == "Venda de ingressos" }
            compose.setContent { AndroidView(factory = { ctx -> requireNotNull(manager.createScreen(item.id, ctx)) }) }
            compose.waitUntil(10_000) {
                compose.onAllNodesWithContentDescription("Ilustração de Batman").fetchSemanticsNodes().isNotEmpty()
            }
            val title = if (mixed) "Festival de música" else "Batman"
            if (mixed) compose.onNodeWithText("Shows").performClick()
            compose.onNodeWithContentDescription("Cartaz: $title").performClick()
            compose.onNodeWithText("A1").performScrollTo().performClick()
            if (mixed) {
                repeat(2) { compose.onNodeWithContentDescription("Adicionar Pista").performScrollTo().performClick() }
                compose.onNodeWithContentDescription("Adicionar Pista VIP").performScrollTo().performClick()
            }
            compose.onNodeWithText("Criar pedido").performScrollTo().performClick()
            compose.onNodeWithText("ABRIR PAGAMENTO NO TERMINAL POS").performScrollTo().performClick()
            compose.runOnIdle {
                assertEquals(1, fake.created)
                assertEquals(if (mixed) 31000L else 4400L, fake.request!!.amountCents)
                assertTrue(fake.request!!.orderId.isNotBlank())
                assertEquals(CardPaymentResultPresentation.CALLER, fake.options!!.resultPresentation)
                fake.finish(CardWithdrawalResult(status, true))
            }
            compose.onNodeWithText("Transação Realizada com Sucesso").assertIsDisplayed()
            if (status == CardWithdrawalStatus.CAPTURED) capture("ingressos-captured-sucesso.png")
            compose.waitUntil(10_000) {
                compose.onAllNodesWithText("Selecione um evento para começar.").fetchSemanticsNodes().isNotEmpty()
            }
            if (status == CardWithdrawalStatus.CAPTURED) capture("ingressos-captured-novo-atendimento.png")
            if (mixed) compose.onNodeWithText("Shows").performClick()
            compose.onNodeWithContentDescription("Cartaz: $title").performClick()
            compose.onNodeWithContentDescription("Assento A1").performScrollTo().assertIsNotEnabled()
            if (mixed) {
                compose.onNodeWithText("98 disponíveis · Sem numeração").performScrollTo().assertIsDisplayed()
                compose.onNodeWithText("39 disponíveis · Sem numeração").performScrollTo().assertIsDisplayed()
            }
            compose.runOnIdle { assertEquals(1, fake.created) }
        } finally {
            compose.runOnIdle { manager.close() }
        }
    }

    /** Captura exclusivamente o fluxo sintético do plugin carregado no host. */
    private fun capture(name: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.cacheDir.resolve(name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}

/** Componente sintético sem I/O ou uso de cartão; entrega resultado controlado pelo teste. */
private class TestCardComponent : ICardPaymentPresentationComponent {
    var created = 0
    var request: CardPaymentRequest? = null
    var options: CardPaymentOptions? = null
    private lateinit var callback: (CardWithdrawalResult) -> Unit
    override val manifest = PluginManifest("synthetic", "synthetic", "1.0.0", 1, 5,
        "synthetic.Entry", setOf("card-withdrawal-component"))
    override fun onLoad(host: PluginHostContext) = Unit
    override fun onAttach(registry: IUIRegistry, router: IPluginRouter) = Unit
    override fun onActivate() = Unit
    override fun onDetach() = Unit
    override fun createWithdrawal(context: Context, onComplete: (CardWithdrawalResult) -> Unit): CardWithdrawalSession {
        error("Não deve abrir caminho legado sem valor")
    }
    override fun createPayment(context: Context, request: CardPaymentRequest,
        onComplete: (CardWithdrawalResult) -> Unit): CardWithdrawalSession {
        error("Não deve ignorar opções de apresentação")
    }
    override fun createPayment(context: Context, request: CardPaymentRequest, options: CardPaymentOptions,
        onComplete: (CardWithdrawalResult) -> Unit): CardWithdrawalSession {
        this.request = request
        this.options = options
        created++
        callback = onComplete
        return object : CardWithdrawalSession {
            override val view: View = TextView(context).apply { text = "Pagamento sintético" }
            override fun cancel() = Unit
        }
    }
    /** Conclui a sessão sintética na thread principal. */
    fun finish(result: CardWithdrawalResult) = callback(result)
}
