package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import android.content.Context
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalResult
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalSession
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalStatus
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.ICardWithdrawalComponent
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardPaymentRequest
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardPaymentOptions
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardPaymentResultPresentation
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.ICardPaymentPresentationComponent
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginRouter
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IUIRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginHostContext
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginManifest

/**
 * Componente independente com retorno opt-in ao consumidor; PIN/comandos permanecem no Go.
 * @param repositoryFactory cria a porta main-safe por sessão; o construtor público usa o adapter Go
 */
class PluginSaqueCartaoApp internal constructor(
    private val repositoryFactory: (Context) -> SaqueRepository,
) : ICardPaymentPresentationComponent {
    /** Entrada pública de produção preservada para carga dinâmica pelo host. */
    constructor() : this({ GoSaqueRepository(it) })
    private val owners = ConcurrentHashMap<SaqueJourney, CoroutineScope>()
    override val manifest = PluginManifest(
        pluginId = PluginId,
        displayName = "Pagamento com Cartão",
        pluginVersion = "1.5.0",
        requiredSharedApiMajor = 1,
        requiredSharedApiMinor = 5,
        entryClass = PluginSaqueCartaoApp::class.java.name,
        capabilities = setOf("card-withdrawal-component"),
    )

    override fun onLoad(host: PluginHostContext) = Unit
    override fun onAttach(registry: IUIRegistry, router: IPluginRouter) = Unit
    override fun onActivate() = Unit

    /** Agenda cleanup na main thread para todas as Views ainda conhecidas. */
    override fun onDetach() {
        owners.forEach { (owner, scope) -> scope.launch { owner.dispose() } }
        owners.clear()
    }

    /**
     * Cria owner por View sem abrir hardware; attach prepara a sessão.
     * Retorno explícito só publica resultado após cleanup. Detach sem retorno
     * encerra cooperativamente e não chama consumidor destruído.
     */
    override fun createWithdrawal(
        context: Context,
        onComplete: (CardWithdrawalResult) -> Unit,
    ): CardWithdrawalSession = createSession(context, null, false, onComplete)

    override fun createPayment(context: Context, request: CardPaymentRequest,
        onComplete: (CardWithdrawalResult) -> Unit): CardWithdrawalSession =
        createSession(context, request.amountCents, false, onComplete)

    /** Personaliza apenas apresentação; status e cleanup continuam integralmente preservados. */
    override fun createPayment(context: Context, request: CardPaymentRequest, options: CardPaymentOptions,
        onComplete: (CardWithdrawalResult) -> Unit): CardWithdrawalSession =
        createSession(context, request.amountCents,
            options.resultPresentation == CardPaymentResultPresentation.CALLER, onComplete)

    /** Cria owner com base fixa e opção de retorno pós-cleanup, sem conhecer o plugin consumidor. */
    private fun createSession(context: Context, fixedAmountCents: Long?, returnCompletedToCaller: Boolean,
        onComplete: (CardWithdrawalResult) -> Unit): CardWithdrawalSession {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        lateinit var owner: SaqueJourney
        owner = SaqueJourney(repositoryFactory(context), scope, fixedAmountCents, returnCompletedToCaller) {
            val terminal = requireNotNull(owner.closedState)
            owners.remove(owner)
            onComplete(CardWithdrawalResult(
                CardWithdrawalStatus.valueOf((terminal.result ?: SaqueResultado.CANCELED).name),
                cleanupConfirmed = !terminal.cleanupFailed,
            ))
        }
        owners[owner] = scope
        val content = ComposeView(context).apply {
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(view: View) { owner.load() }
                override fun onViewDetachedFromWindow(view: View) {
                    owner.dispose()
                    owners.remove(owner)
                }
            })
            setContent {
                val state by owner.state.collectAsStateWithLifecycle()
                SaqueCartaoScreen(state, owner::onEvent)
            }
        }
        return object : CardWithdrawalSession {
            override val view: View = content
            override fun cancel() = owner.requestReturn()
        }
    }

    private companion object { const val PluginId = "br.com.romulopenha.sistemaprototipoandroid.saquecartao" }
}
