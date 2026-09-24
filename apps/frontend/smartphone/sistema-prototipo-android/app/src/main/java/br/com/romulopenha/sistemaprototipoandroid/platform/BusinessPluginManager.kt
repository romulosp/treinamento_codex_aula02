package br.com.romulopenha.sistemaprototipoandroid.platform

import android.content.Context
import android.os.Handler
import android.os.Looper
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem
import java.io.File

/** Descobre e ativa o menu demonstrativo sem conceder acesso a credenciais. */
internal class BusinessPluginManager(
    private val context: Context,
    private val onItems: (List<BusinessMenuItem>) -> Unit,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    /** Busca a revisão privada mais recente depois de autenticação bem-sucedida. */
    fun startAfterSession() {
        Thread {
            val request = PluginLoadingRequest(
                pluginId = "br.com.romulopenha.sistemaprototipoandroid.negocio",
                requiredCapability = "business-menu",
            )
            val inbox = File(requireNotNull(context.getExternalFilesDir(null)), "plugins/business/inbox").apply { mkdirs() }
            val candidate = File(inbox, "plugin-negocio-bootstrap.apk")
            if (!candidate.exists()) {
                runCatching {
                    context.assets.open("plugins/plugin-negocio.apk").use { input ->
                        candidate.outputStream().use(input::copyTo)
                    }
                }.getOrElse { return@Thread }
            }
            runCatching {
                LoginPluginLoader.stageAndVerify(context, inbox, candidate, {}, request)
            }.getOrNull()?.let { verified ->
                val loaded = LoginPluginLoader.loadBusiness(context, verified, {})
                mainHandler.post { onItems(loaded.items) }
            }
        }.start()
    }

    /** Limpa o menu quando a sessão expira ou é encerrada. */
    fun clear() = mainHandler.post { onItems(emptyList()) }
}
