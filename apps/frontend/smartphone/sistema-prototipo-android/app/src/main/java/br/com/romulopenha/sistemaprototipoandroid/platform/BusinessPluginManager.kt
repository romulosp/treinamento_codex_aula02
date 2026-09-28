package br.com.romulopenha.sistemaprototipoandroid.platform

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginNegocioApp
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginRouter
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IUIRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginEvent
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginHostContext
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginRoute
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginScreenFactory
import dalvik.system.DexClassLoader
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.ZipFile

/** Mantém instância e classloader vivos enquanto a sessão usa o plugin. */
private data class BusinessPluginHandle(
    val leaf: MenuLeaf,
    val plugin: IPluginNegocioApp,
    @Suppress("unused") val classLoader: DexClassLoader,
)

/**
 * Prepara, descobre e hospeda todos os plugins de negócio após o login.
 *
 * O build debug entrega APKs como staging de desenvolvimento; a validação,
 * promoção e carga continuam no pipeline seguro do host.
 */
internal class BusinessPluginManager(
    private val context: Context,
    private val onItems: (List<BusinessMenuItem>) -> Unit,
    private val onFatal: (MenuFatalException) -> Unit,
    private val onNavigateToMenu: () -> Unit,
) : AutoCloseable {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    private val generation = AtomicLong()

    @Volatile
    private var handlesByItemId: Map<String, BusinessPluginHandle> = emptyMap()

    /** Agenda staging debug, descoberta e montagem sem bloquear a main thread. */
    fun startAfterSession() {
        val requestedGeneration = generation.incrementAndGet()
        executor.execute {
            try {
                stageAvailablePlugins()
                val handles = LoginPluginLoader.findVerified(context, {}, LoginPluginLoader.BusinessRequest)
                    .distinctBy { it.descriptor.pluginId }
                    .flatMap(::discoverHandles)
                val items = MenuTreeBuilder.build(handles.map(BusinessPluginHandle::leaf))
                val byId = handles.associateBy { it.leaf.path.itemId }
                mainHandler.post {
                    if (generation.get() == requestedGeneration) {
                        handlesByItemId = byId
                        onItems(items)
                    } else {
                        handles.detachAll()
                    }
                }
            } catch (error: MenuFatalException) {
                mainHandler.post {
                    if (generation.get() == requestedGeneration) onFatal(error)
                }
            }
        }
    }

    /** Cria a tela do item-folha selecionado, mantendo o plugin isolado do host. */
    fun createScreen(itemId: String, activityContext: Context): View? =
        handlesByItemId[itemId]?.plugin?.createBusinessScreen(activityContext)

    /** Invalida resultados, destaca plugins cooperativamente e limpa o menu. */
    fun clear() {
        generation.incrementAndGet()
        val handles = handlesByItemId.values.toList()
        handlesByItemId = emptyMap()
        executor.execute { handles.detachAll() }
        mainHandler.post { onItems(emptyList()) }
    }

    /** Encerra o manager e libera referências cooperativas dos plugins. */
    override fun close() {
        generation.incrementAndGet()
        handlesByItemId.values.toList().detachAll()
        handlesByItemId = emptyMap()
        executor.shutdownNow()
    }

    /** Copia assets debug e processa todos os APKs disponibilizados no inbox de negócio. */
    private fun stageAvailablePlugins() {
        val names = context.assets.list(BusinessAssetDirectory).orEmpty().sorted()
        val inbox = File(requireNotNull(context.getExternalFilesDir(null)), "plugins/business/inbox").apply { mkdirs() }
        names.forEach { name ->
            val candidate = File(inbox, name)
            context.assets.open("$BusinessAssetDirectory/$name").use { input ->
                candidate.outputStream().use(input::copyTo)
            }
        }
        inbox.listFiles { file -> file.isFile && file.extension.equals("apk", ignoreCase = true) }
            .orEmpty()
            .sortedBy(File::getName)
            .forEach { candidate ->
            runCatching {
                LoginPluginLoader.stageAndVerify(context, inbox, candidate, {}, LoginPluginLoader.BusinessRequest)
            }
        }
    }

    /** Instancia somente classes declaradas no serviço e executa o lifecycle do contrato. */
    private fun discoverHandles(verified: VerifiedLoginPlugin): List<BusinessPluginHandle> = try {
        ZipFile(verified.apk).use { zip ->
            val entry = zip.getEntry(ServicePath) ?: return emptyList()
            val classNames = zip.getInputStream(entry).bufferedReader().useLines { lines ->
                lines.map(String::trim).filter { it.isNotEmpty() && !it.startsWith('#') }.toList()
            }
            val optimized = File(context.codeCacheDir, "plugins/${verified.digestSha256}").apply { mkdirs() }
            val loader = DexClassLoader(verified.apk.absolutePath, optimized.absolutePath, null, context.classLoader)
            classNames.mapNotNull { className ->
                runCatching {
                    val plugin = loader.loadClass(className).getDeclaredConstructor().newInstance() as? IPluginNegocioApp
                        ?: return@runCatching null
                    val item = plugin.businessMenuItems.single().also { require(it.filhos.isEmpty()) }
                    val path = MenuPathValidator.validate(plugin.getCaminhoMenu())
                    plugin.onLoad(EmptyHostContext)
                    plugin.onAttach(NoopRegistry, MenuRouter)
                    plugin.onActivate()
                    BusinessPluginHandle(MenuLeaf(path, item, "${verified.apk.name}:$className"), plugin, loader)
                }.getOrNull()
            }
        }
    } catch (_: Exception) {
        emptyList()
    }

    private fun Collection<BusinessPluginHandle>.detachAll() = forEach { handle -> runCatching { handle.plugin.onDetach() } }

    private object EmptyHostContext : PluginHostContext {
        override fun publish(event: PluginEvent) = Unit
    }

    private object NoopRegistry : IUIRegistry {
        override fun registerStartupAuth(factory: PluginScreenFactory): Nothing =
            error("Plugin de negócio não pode registrar startup-auth")
    }

    private val MenuRouter = object : IPluginRouter {
        override fun navigate(route: PluginRoute) {
            if (route.path == MenuRoute) mainHandler.post(onNavigateToMenu)
        }
    }

    private companion object {
        const val BusinessAssetDirectory = "plugins/business"
        const val MenuRoute = "menu-principal"
        const val ServicePath = "META-INF/services/br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginNegocioApp"
    }
}
