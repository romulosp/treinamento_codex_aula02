package br.com.romulopenha.sistemaprototipoandroid

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import br.com.romulopenha.sistemaprototipoandroid.platform.DynamicLoginPluginManager
import br.com.romulopenha.sistemaprototipoandroid.platform.PluginHostState
import br.com.romulopenha.sistemaprototipoandroid.platform.PluginRuntimeStatus
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginEvent
import kotlinx.coroutines.delay

/** Activity host que alterna entre autenticação dinâmica e menu de negócio. */
class MainActivity : ComponentActivity() {
    private var hostState by mutableStateOf(
        PluginHostState(
            status = PluginRuntimeStatus.WAITING,
            message = "Aguardando plugin de autenticação na pasta dinâmica.",
        ),
    )
    private var session by mutableStateOf<PluginEvent.SessionStateChanged?>(null)
    private var logoutInProgress by mutableStateOf(false)
    private lateinit var pluginManager: DynamicLoginPluginManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pluginManager = DynamicLoginPluginManager(applicationContext) { state ->
            hostState = state
        }
        setContent {
            PluginHostContent(
                state = hostState,
                session = session,
                logoutInProgress = logoutInProgress,
                onSessionEstablished = { established -> session = established },
                onLogout = { established ->
                    if (!logoutInProgress) {
                        logoutInProgress = true
                        pluginManager.logout(established.sessionId) {
                            logoutInProgress = false
                            session = null
                        }
                    }
                },
                onRuntimeFailure = pluginManager::reportRuntimeFailure,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    override fun onStart() {
        super.onStart()
        pluginManager.start()
    }

    override fun onStop() {
        pluginManager.stop()
        super.onStop()
    }

    override fun onDestroy() {
        pluginManager.close()
        super.onDestroy()
    }
}

/**
 * Renderiza a capacidade ativa e troca atomicamente para o menu após a sessão.
 *
 * Exceções cooperativas do plugin são convertidas em fallback sem derrubar a
 * Activity. O host recebe somente a sessão opaca.
 */
@Composable
private fun PluginHostContent(
    state: PluginHostState,
    session: PluginEvent.SessionStateChanged?,
    logoutInProgress: Boolean,
    onSessionEstablished: (PluginEvent.SessionStateChanged) -> Unit,
    onLogout: (PluginEvent.SessionStateChanged) -> Unit,
    onRuntimeFailure: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (session != null) {
        val currentOnLogout by rememberUpdatedState(onLogout)
        LaunchedEffect(session.sessionId, session.expiresAtEpochMillis) {
            val remaining = (session.expiresAtEpochMillis - System.currentTimeMillis()).coerceAtLeast(0)
            delay(remaining)
            currentOnLogout(session)
        }
        BusinessMenuScreen(
            items = emptyList(),
            logoutInProgress = logoutInProgress,
            onLogout = { onLogout(session) },
            modifier = modifier,
        )
        return
    }

    val plugin = state.activePlugin
    if (plugin == null) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(state.message)
        }
        return
    }

    key(plugin.digestSha256) {
        AndroidView(
            factory = { context ->
                FrameLayout(context).apply {
                    try {
                        val pluginView = plugin.screenFactory.create(context) { event ->
                            try {
                                onSessionEstablished(event)
                            } catch (error: Exception) {
                                onRuntimeFailure(error)
                            } catch (error: LinkageError) {
                                onRuntimeFailure(error)
                            }
                        }
                        addView(
                            pluginView,
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            ),
                        )
                    } catch (error: Exception) {
                        onRuntimeFailure(error)
                        addView(TextView(context).apply { text = context.getString(R.string.plugin_auth_unavailable) })
                    } catch (error: LinkageError) {
                        onRuntimeFailure(error)
                        addView(TextView(context).apply { text = context.getString(R.string.plugin_auth_unavailable) })
                    }
                }
            },
            modifier = modifier,
        )
    }
}

/**
 * Exibe itens de negócio já validados ou o estado vazio previsto nesta fase.
 *
 * @param items itens fornecidos exclusivamente por [br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginNegocioApp]
 * @param logoutInProgress impede solicitações concorrentes de logout
 * @param onLogout solicita encerramento da sessão atual
 */
@Composable
internal fun BusinessMenuScreen(
    items: List<BusinessMenuItem>,
    logoutInProgress: Boolean,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (items.isEmpty()) {
            Text("Nenhum plugin de negócio carregado.")
        } else {
            items.sortedWith(compareBy(BusinessMenuItem::ordem, BusinessMenuItem::id)).forEach { item ->
                Text(item.titulo)
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onLogout, enabled = !logoutInProgress) {
            Text(if (logoutInProgress) "Saindo..." else "Sair")
        }
    }
}
