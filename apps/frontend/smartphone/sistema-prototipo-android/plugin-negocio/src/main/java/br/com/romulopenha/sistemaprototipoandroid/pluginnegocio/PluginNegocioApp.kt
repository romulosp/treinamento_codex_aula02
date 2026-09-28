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

/** Fornece uma folha demonstrativa de menu sem regra transacional ou dado sensível. */
class PluginNegocioApp : IPluginNegocioApp {
    override val manifest = PluginManifest(
        pluginId = PluginId,
        displayName = "Menu demonstrativo",
        pluginVersion = "1.1.0",
        requiredSharedApiMajor = 1,
        requiredSharedApiMinor = 1,
        entryClass = PluginNegocioApp::class.java.name,
        capabilities = setOf("business-menu"),
    )

    override val businessMenuItems: List<BusinessMenuItem> = listOf(
        BusinessMenuItem("operacoes", "Operações", "operacoes", 10),
    )

    /** Declara a posição estável da folha demonstrativa no menu do host. */
    override fun getCaminhoMenu(): String = "Principal > Operações"

    override fun onLoad(host: PluginHostContext) = Unit

    override fun onAttach(registry: IUIRegistry, router: IPluginRouter) = Unit

    override fun onActivate() = Unit

    override fun onDetach() = Unit

    override fun createBusinessScreen(context: Context): View = ComposeView(context).apply {
        setContent { DemonstrationMenu(businessMenuItems) }
    }

    private companion object {
        const val PluginId = "br.com.romulopenha.sistemaprototipoandroid.negocio"
    }
}

/** Exibe uma ação local para a única folha publicada pelo plugin. */
@Composable
private fun DemonstrationMenu(items: List<BusinessMenuItem>) {
    var selected by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Menu demonstrativo")
        items.forEach { item ->
            Button(onClick = { selected = item.titulo }) { Text(item.titulo) }
        }
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
