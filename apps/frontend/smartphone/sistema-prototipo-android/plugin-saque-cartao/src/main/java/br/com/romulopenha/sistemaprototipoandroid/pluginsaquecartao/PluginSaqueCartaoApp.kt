package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginNegocioApp
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginRouter
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IUIRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginHostContext
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginManifest
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginRoute
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.SharedApi

/** Plugin independente que demonstra o fluxo local de saque por cartão. */
class PluginSaqueCartaoApp : IPluginNegocioApp {
    private var router: IPluginRouter? = null
    override val manifest = PluginManifest(
        pluginId = PluginId,
        displayName = "Saque Cartão",
        pluginVersion = "1.1.0",
        requiredSharedApiMajor = 1,
        requiredSharedApiMinor = 1,
        entryClass = PluginSaqueCartaoApp::class.java.name,
        capabilities = setOf("business-menu"),
    )

    override val businessMenuItems = listOf(BusinessMenuItem("saque-cartao", "Saque Cartão", "saque-cartao", 100))

    /** Declara a posição da funcionalidade no menu montado pelo host. */
    override fun getCaminhoMenu(): String = "Principal > Outros Serviços > Saque Cartão"

    override fun onLoad(host: PluginHostContext) = Unit
    override fun onAttach(registry: IUIRegistry, router: IPluginRouter) {
        this.router = router
    }
    override fun onActivate() = Unit
    override fun onDetach() {
        router = null
    }

    /** Cria a View Compose isolada, sem compartilhar a senha com o host. */
    override fun createBusinessScreen(context: Context): View = ComposeView(context).apply {
        setContent {
            SaqueCartaoRoute(
                onReturnToMenu = {
                    router?.navigate(PluginRoute(PluginId, "menu-principal", SharedApi.version))
                },
            )
        }
    }

    private companion object { const val PluginId = "br.com.romulopenha.sistemaprototipoandroid.saquecartao" }
}

/** Etapas locais do fluxo demonstrativo; nenhuma etapa representa autorização real. */
internal enum class SaqueEtapa { CARTAO, SENHA, CONCLUIDO }

/** Renderiza as telas de cartão, senha e conclusão; senha só vive nesta composição. */
@Composable
internal fun SaqueCartaoRoute(
    onReturnToMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var etapa by remember { mutableStateOf(SaqueEtapa.CARTAO) }
    var senha by remember { mutableStateOf("") }
    Surface(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when (etapa) {
                SaqueEtapa.CARTAO -> {
                    Text("Insira ou aproxime o cartão")
                    Button(onClick = { etapa = SaqueEtapa.SENHA }) { Text("Cartão lido") }
                }
                SaqueEtapa.SENHA -> {
                    Text("Digite a senha do cartão")
                    OutlinedTextField(
                        value = senha,
                        onValueChange = { senha = it.take(8) },
                        label = { Text("Senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().semantics { password() },
                    )
                    Button(onClick = { senha = ""; etapa = SaqueEtapa.CONCLUIDO }, enabled = senha.isNotEmpty()) { Text("Confirmar saque") }
                }
                SaqueEtapa.CONCLUIDO -> {
                    Text("Transação concluída")
                    Button(onClick = onReturnToMenu) { Text("Voltar ao menu inicial") }
                }
            }
        }
    }
}
