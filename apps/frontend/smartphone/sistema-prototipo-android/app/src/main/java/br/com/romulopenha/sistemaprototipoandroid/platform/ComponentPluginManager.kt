package br.com.romulopenha.sistemaprototipoandroid.platform

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.util.AtomicFile
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.*
import dalvik.system.DexClassLoader
import java.io.File
import java.util.zip.ZipFile

/**
 * Carrega um único componente verificado e coordena uma sessão visual por vez.
 * Preparação ocorre no executor do host; lançamento e callbacks ocorrem na main.
 * A instância permanece viva entre logins para preservar o classloader das nativas.
 */
internal class ComponentPluginManager(
    private val context: Context,
    private val nativeLibraries: (ZipFile, File) -> File?,
    private val suppliedComponent: ICardWithdrawalComponent? = null,
) {
    @Volatile private var component: ICardWithdrawalComponent? = suppliedComponent
    private var classLoader: DexClassLoader? = null
    private var active: Invocation? = null
    private var blocked = false
    private val main = Handler(Looper.getMainLooper())

    /** Retém owner, contêiner e callback até cleanup; valid impede retorno após detach. */
    private class Invocation(
        val container: FrameLayout,
        val caller: View,
        val callback: (CardWithdrawalResult) -> Unit,
        var valid: Boolean = true,
        var session: CardWithdrawalSession? = null,
        var presentationFailed: Boolean = false,
    )

    /** Prepara uma vez sem abrir hardware, usando o pipeline autenticado do host. */
    fun prepare() {
        if (component != null) return
        runCatching {
            val inbox = File(requireNotNull(context.getExternalFilesDir(null)), "plugins/components/inbox")
                .apply { check(mkdirs() || isDirectory) }
            context.assets.list("plugins/components").orEmpty().sorted().forEach { name ->
                val candidate = File(inbox, name)
                context.assets.open("plugins/components/$name").use { input ->
                    candidate.outputStream().use(input::copyTo)
                }
            }
            val staged = inbox.listFiles { file -> file.isFile && file.extension == "apk" }.orEmpty().map {
                LoginPluginLoader.stageAndVerify(context, inbox, it, {}, LoginPluginLoader.ComponentRequest)
            }.distinctBy { it.digestSha256 }
            val candidates = LoginPluginLoader.findVerified(context, {}, LoginPluginLoader.ComponentRequest)
            val selectionFile = File(context.filesDir, "plugins/components/selected.sha256")
            val selection = AtomicFile(selectionFile)
            val verified = if (staged.isNotEmpty()) {
                staged.single()
            } else if (selectionFile.exists()) {
                val selectedDigest = selection.readFully().toString(Charsets.US_ASCII).trim()
                require(selectedDigest.matches(Regex("[a-f0-9]{64}")))
                candidates.single { it.digestSha256 == selectedDigest }
            } else {
                candidates.single()
            }
            require(verified.descriptor.requiredMinor >= 3)
            ZipFile(verified.apk).use { zip ->
                val service = requireNotNull(zip.getEntry(ServicePath))
                val entries = zip.getInputStream(service).bufferedReader().useLines { lines ->
                    lines.map(String::trim).filter { it.isNotEmpty() && !it.startsWith('#') }.toList()
                }
                require(entries == listOf(verified.descriptor.entryClass))
                val optimized = File(context.codeCacheDir, "plugins/${verified.digestSha256}")
                    .apply { check(mkdirs() || isDirectory) }
                val natives = nativeLibraries(zip, optimized)
                val loader = DexClassLoader(verified.apk.absolutePath, optimized.absolutePath, natives?.absolutePath, context.classLoader)
                val loaded = loader.loadClass(entries.single()).getDeclaredConstructor().newInstance() as ICardWithdrawalComponent
                LoginPluginLoader.validateRuntimeManifest(loaded.manifest, verified.descriptor)
                try {
                    loaded.onLoad(NoopHost)
                    loaded.onAttach(NoopRegistry, NoopRouter)
                    loaded.onActivate()
                    check(selectionFile.parentFile!!.mkdirs() || selectionFile.parentFile!!.isDirectory)
                    val output = selection.startWrite()
                    try {
                        output.write(verified.digestSha256.toByteArray(Charsets.US_ASCII))
                        selection.finishWrite(output)
                    } catch (failure: Exception) {
                        selection.failWrite(output)
                        throw failure
                    }
                } catch (failure: Exception) {
                    runCatching { loaded.onDetach() }
                    throw failure
                }
                classLoader = loader
                component = loaded
            }
        }
    }

    /** Abre a View sobre o consumidor; indisponibilidade e concorrência não criam sessão. */
    fun launch(container: FrameLayout, caller: View, callback: (CardWithdrawalResult) -> Unit) {
        launchInternal(container, caller, null, null, callback)
    }

    /** Encaminha o valor base sem alteração e exige provedor compatível com 1.4. */
    fun launchPayment(container: FrameLayout, caller: View, request: CardPaymentRequest,
                      callback: (CardWithdrawalResult) -> Unit) {
        launchInternal(container, caller, request, null, callback)
    }

    /** Exige faceta 1.5 e encaminha opções visuais sem alterar pedido, status ou cleanup. */
    fun launchPayment(container: FrameLayout, caller: View, request: CardPaymentRequest,
                      options: CardPaymentOptions, callback: (CardWithdrawalResult) -> Unit) {
        launchInternal(container, caller, request, options, callback)
    }

    /** Coordena ambos os contratos com o mesmo lifecycle e sem fallback de valor. */
    private fun launchInternal(container: FrameLayout, caller: View, request: CardPaymentRequest?, options: CardPaymentOptions?,
                               callback: (CardWithdrawalResult) -> Unit) {
        check(Looper.myLooper() == Looper.getMainLooper())
        if (blocked || active != null) {
            callback(CardWithdrawalResult(CardWithdrawalStatus.BUSY, !blocked))
            return
        }
        val loaded = component
        if (loaded == null || !container.isAttachedToWindow || (request != null && loaded !is ICardPaymentComponent) ||
            (options != null && loaded !is ICardPaymentPresentationComponent)) {
            callback(CardWithdrawalResult(CardWithdrawalStatus.UNAVAILABLE, true))
            return
        }
        val invocation = Invocation(container, caller, callback)
        active = invocation
        try {
            val completion: (CardWithdrawalResult) -> Unit = { result ->
                main.post { finish(invocation, result) }
            }
            val session = when {
                request == null -> loaded.createWithdrawal(container.context, completion)
                options == null -> (loaded as ICardPaymentComponent).createPayment(container.context, request, completion)
                else -> (loaded as ICardPaymentPresentationComponent).createPayment(container.context, request, options, completion)
            }
            invocation.session = session
            caller.visibility = View.GONE
            container.addView(session.view, FrameLayout.LayoutParams(-1, -1))
            session.view.requestFocus()
        } catch (_: Exception) {
            invocation.presentationFailed = true
            val session = invocation.session
            if (session == null) {
                // Sem uma sessão retornada, não há como confirmar cleanup.
                finish(invocation, CardWithdrawalResult(CardWithdrawalStatus.ERROR, false))
            } else {
                // Se attach começou, aguardar seu callback de cleanup antes de retornar.
                runCatching { session.cancel() }.onFailure {
                    finish(invocation, CardWithdrawalResult(CardWithdrawalStatus.UNKNOWN, false))
                }
            }
        }
    }

    /** Voltar solicita cleanup e mantém a UI até o componente terminar. */
    fun cancel(): Boolean {
        val invocation = active ?: return false
        invocation.session?.cancel()
        return true
    }

    /** Invalida callback antes de cancelar por logout, detach ou destruição. */
    fun abandon(container: FrameLayout? = null) {
        val invocation = active ?: return
        if (container != null && invocation.container !== container) return
        invocation.valid = false
        invocation.session?.cancel()
    }

    /** Remove overlay somente após retorno e restaura o mesmo consumidor ainda anexado. */
    private fun finish(invocation: Invocation, result: CardWithdrawalResult) {
        if (active !== invocation) return
        active = null
        blocked = blocked || !result.cleanupConfirmed
        invocation.session?.view?.let(invocation.container::removeView)
        invocation.caller.visibility = View.VISIBLE
        if (invocation.valid && invocation.container.isAttachedToWindow) {
            invocation.caller.requestFocus()
            invocation.callback(if (invocation.presentationFailed && result.cleanupConfirmed)
                result.copy(status = CardWithdrawalStatus.ERROR) else result)
        }
    }

    /** Agenda detach do componente e preserva invalidação do consumidor. */
    fun close() {
        abandon()
        val loaded = component
        component = null
        runCatching { loaded?.onDetach() }
    }

    private object NoopHost : PluginHostContext {
        override fun publish(event: PluginEvent) = Unit
    }
    private object NoopRegistry : IUIRegistry {
        override fun registerStartupAuth(factory: PluginScreenFactory): Nothing = error("Capacidade incompatível")
    }
    private object NoopRouter : IPluginRouter {
        override fun navigate(route: PluginRoute) = Unit
    }
    private companion object {
        const val ServicePath = "META-INF/services/br.com.romulopenha.sistemaprototipoandroid.sharedapi.ICardWithdrawalComponent"
    }
}
