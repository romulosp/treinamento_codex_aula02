package br.com.romulopenha.sistemaprototipoandroid.pluginnegocio

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginNegocioApp
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginRouter
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IUIRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginHostContext
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginManifest
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IComponentHostContext
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalResult

/** Fornece uma folha demonstrativa de menu sem regra transacional ou dado sensível. */
class PluginNegocioApp : IPluginNegocioApp {
    private var componentHost: IComponentHostContext? = null
    override val manifest = PluginManifest(
        pluginId = PluginId,
        displayName = "Menu demonstrativo",
        pluginVersion = "1.2.0",
        requiredSharedApiMajor = 1,
        requiredSharedApiMinor = 3,
        entryClass = PluginNegocioApp::class.java.name,
        capabilities = setOf("business-menu"),
    )

    override val businessMenuItems: List<BusinessMenuItem> = listOf(
        BusinessMenuItem("operacoes", "Operações", "operacoes", 10),
    )

    /** Declara a posição estável da folha demonstrativa no menu do host. */
    override fun getCaminhoMenu(): String = "Principal > Operações"

    override fun onLoad(host: PluginHostContext) { componentHost = host as? IComponentHostContext }

    override fun onAttach(registry: IUIRegistry, router: IPluginRouter) = Unit

    override fun onActivate() = Unit

    override fun onDetach() { componentHost = null }

    override fun createBusinessScreen(context: Context): View = ComposeView(context).apply {
        setContent {
            DemonstrationMenu(businessMenuItems, onLaunch = { callback ->
                componentHost?.launchCardWithdrawal(callback)
            })
        }
    }

    private companion object {
        const val PluginId = "br.com.romulopenha.sistemaprototipoandroid.negocio"
    }
}

/** Exibe uma ação local para a única folha publicada pelo plugin. */
@Composable
private fun DemonstrationMenu(
    items: List<BusinessMenuItem>,
    onLaunch: ((CardWithdrawalResult) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Menu demonstrativo")
        items.forEach { item ->
            Button(onClick = { selected = item.titulo }) { Text(item.titulo) }
        }
        Button(onClick = {
            onLaunch { outcome -> result = outcome.status.name +
                if (!outcome.cleanupConfirmed) " — verifique o terminal antes de reiniciar" else "" }
        }) { Text("Abrir pagamento com cartão") }
        Text("Homologação de teste. Não autoriza entrega de dinheiro.")
        result?.let { Text("Resultado local: $it") }
    }
    selected?.let { title ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(title) },
            text = { Text("Ação demonstrativa local.") },
            confirmButton = { Button(onClick = { selected = null }) { Text("Fechar") } },
        )
    }
}
