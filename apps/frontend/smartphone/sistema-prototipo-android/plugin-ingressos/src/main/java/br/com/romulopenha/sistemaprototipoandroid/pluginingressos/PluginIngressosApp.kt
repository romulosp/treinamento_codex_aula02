package br.com.romulopenha.sistemaprototipoandroid.pluginingressos

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalResult
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalStatus
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.ICardPaymentPresentationHostContext
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardPaymentOptions
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardPaymentResultPresentation
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardPaymentRequest
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginNegocioApp
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginRouter
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IUIRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginHostContext
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginManifest
import kotlinx.coroutines.delay

/** Plugin de bilheteria para cinema, teatro e shows, com estoque demonstrativo local. */
class PluginIngressosApp : IPluginNegocioApp {
    private val journey = CinemaJourney()
    private var componentHost: ICardPaymentPresentationHostContext? = null
    override val manifest = PluginManifest(
        pluginId = "br.com.romulopenha.sistemaprototipoandroid.ingressos",
        displayName = "Venda de ingressos",
        pluginVersion = "1.2.1",
        requiredSharedApiMajor = 1,
        requiredSharedApiMinor = 5,
        entryClass = PluginIngressosApp::class.java.name,
        capabilities = setOf("business-menu"),
    )
    override val businessMenuItems = listOf(BusinessMenuItem("ingressos", "Venda de ingressos", "ingressos", 20))
    override fun getCaminhoMenu() = "Principal > Venda de ingressos"
    override fun onLoad(host: PluginHostContext) { componentHost = host as? ICardPaymentPresentationHostContext }
    override fun onAttach(registry: IUIRegistry, router: IPluginRouter) = Unit
    override fun onActivate() = Unit
    override fun onDetach() { componentHost = null; journey.abandonPayment() }
    override fun createBusinessScreen(context: Context): View {
        journey.abandonPayment()
        return ComposeView(context).apply {
            setContent {
                CinemaRoute(journey) { request, callback ->
                    val host = componentHost
                    if (host == null) callback(CardWithdrawalResult(CardWithdrawalStatus.UNAVAILABLE, true))
                    else host.launchCardPayment(request,
                        CardPaymentOptions(CardPaymentResultPresentation.CALLER), callback)
                }
            }
        }
    }
}

/**
 * Encaminha catálogo/pagamento e exibe conclusão por três segundos antes de novo atendimento.
 * O timer pertence à composição e ao ID concluído; cancelamento/reentrada não repete emissão.
 */
@Composable
internal fun CinemaRoute(
    journey: CinemaJourney,
    modifier: Modifier = Modifier,
    launchPayment: (CardPaymentRequest, (CardWithdrawalResult) -> Unit) -> Unit,
) {
    var state by remember { mutableStateOf(journey.state) }
    LaunchedEffect(state.completedOrderId) {
        val completedOrderId = state.completedOrderId ?: return@LaunchedEffect
        delay(3_000)
        journey.startNewAttendance(completedOrderId)
        state = journey.state
    }
    CinemaScreen(
        modifier = modifier,
        state = state, catalog = journey.catalog,
        onMovie = { journey.selectMovie(it); state = journey.state },
        onSelect = { journey.select(it); state = journey.state },
        onStanding = { id, quantity -> journey.setStanding(id, quantity); state = journey.state },
        onOrder = { journey.createOrder(); state = journey.state },
        onPayment = {
            if (journey.beginPayment()) {
                state = journey.state
                val request = CardPaymentRequest(requireNotNull(state.pendingOrderId),
                    requireNotNull(state.order).total)
                launchPayment(request) { result ->
                    journey.paymentCompleted(request.orderId, result.status, result.cleanupConfirmed)
                    state = journey.state
                }
            }
        },
        onCancel = { journey.discard(false); state = journey.state },
    )
}
