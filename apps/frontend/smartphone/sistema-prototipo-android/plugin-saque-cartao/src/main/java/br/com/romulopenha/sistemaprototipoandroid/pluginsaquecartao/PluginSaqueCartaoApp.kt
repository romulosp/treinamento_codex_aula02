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
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginNegocioApp
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginRouter
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IUIRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginHostContext
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginManifest
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginRoute
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.SharedApi

/** Plugin independente de homologação; PIN e comandos permanecem no componente Go. */
class PluginSaqueCartaoApp : IPluginNegocioApp {
    private var router: IPluginRouter? = null
    private val owners = ConcurrentHashMap<SaqueJourney, CoroutineScope>()
    override val manifest = PluginManifest(
        pluginId = PluginId,
        displayName = "Pagamento com Cartão",
        pluginVersion = "1.1.0",
        requiredSharedApiMajor = 1,
        requiredSharedApiMinor = 1,
        entryClass = PluginSaqueCartaoApp::class.java.name,
        capabilities = setOf("business-menu"),
    )

    override val businessMenuItems = listOf(BusinessMenuItem("pagamento-cartao", "Pagamento com Cartão", "pagamento-cartao", 100))

    /** Declara a posição da funcionalidade no menu montado pelo host. */
    override fun getCaminhoMenu(): String = "Principal > Outros Serviços > Pagamento com Cartão"

    override fun onLoad(host: PluginHostContext) = Unit
    override fun onAttach(registry: IUIRegistry, router: IPluginRouter) {
        this.router = router
    }
    override fun onActivate() = Unit
    override fun onDetach() {
        owners.forEach { (owner, scope) -> scope.launch { owner.dispose() } }
        owners.clear()
        router = null
    }

    /**
     * Cria proprietário por View; somente attach inicia a preparação EMV deste
     * plugin. Discovery/criação sem exibição não abre hardware; detach fecha
     * sessão e composição apenas observa snapshots, sem alterar a infra do host.
     */
    override fun createBusinessScreen(context: Context): View = ComposeView(context).apply {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val owner = SaqueJourney(GoSaqueRepository(context), scope) {
            router?.navigate(PluginRoute(PluginId, "menu-principal", SharedApi.version))
        }
        owners[owner] = scope
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

    private companion object { const val PluginId = "br.com.romulopenha.sistemaprototipoandroid.saquecartao" }
}
