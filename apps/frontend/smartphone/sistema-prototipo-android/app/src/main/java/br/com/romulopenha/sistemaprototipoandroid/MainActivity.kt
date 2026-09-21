package br.com.romulopenha.sistemaprototipoandroid

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import br.com.romulopenha.sistemaprototipoandroid.platform.DynamicLoginPluginManager
import br.com.romulopenha.sistemaprototipoandroid.platform.PluginHostState
import br.com.romulopenha.sistemaprototipoandroid.platform.PluginRuntimeStatus
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginEvent

/** Activity host que observa, valida e hospeda a capacidade dinâmica de autenticação. */
class MainActivity : ComponentActivity() {
    private var hostState by mutableStateOf(
        PluginHostState(
            status = PluginRuntimeStatus.WAITING,
            message = "Aguardando plugin de autenticação na pasta dinâmica.",
        ),
    )
    private var session: PluginEvent.SessionStateChanged? = null
    private lateinit var pluginManager: DynamicLoginPluginManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pluginManager = DynamicLoginPluginManager(applicationContext) { state ->
            hostState = state
        }
        setContent {
            PluginHostContent(
                state = hostState,
                onSessionEstablished = { established -> session = established },
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
 * Renderiza somente o estado recebido e encaminha eventos do plugin ao host.
 *
 * A criação da View é uma fronteira cooperativa: exceções são convertidas em
 * fallback e reportadas ao manager, sem derrubar a Activity.
 */
@Composable
private fun PluginHostContent(
    state: PluginHostState,
    onSessionEstablished: (PluginEvent.SessionStateChanged) -> Unit,
    onRuntimeFailure: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                        addView(TextView(context).apply { text = "Plugin de autenticação indisponível." })
                    } catch (error: LinkageError) {
                        onRuntimeFailure(error)
                        addView(TextView(context).apply { text = "Plugin de autenticação indisponível." })
                    }
                }
            },
            modifier = modifier,
        )
    }
}
