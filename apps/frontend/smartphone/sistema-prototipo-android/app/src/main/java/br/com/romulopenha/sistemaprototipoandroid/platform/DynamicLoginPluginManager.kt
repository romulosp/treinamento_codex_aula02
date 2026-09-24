package br.com.romulopenha.sistemaprototipoandroid.platform

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.FileObserver
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/** Estado imutável consumido pela UI do host. */
internal data class PluginHostState(
    val status: PluginRuntimeStatus,
    val activePlugin: LoadedLoginPlugin? = null,
    val message: String,
)

/**
 * Coordena descoberta, fila serial, ciclo de vida e recuperação do plugin login.
 *
 * O manager recebe somente o contexto da aplicação e publica estados no main
 * looper. O callback do [FileObserver] nunca lê nem carrega o APK.
 */
internal class DynamicLoginPluginManager(
    context: Context,
    private val onStateChanged: (PluginHostState) -> Unit,
) : AutoCloseable {
    private val applicationContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val worker: ScheduledExecutorService =
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor()
    private val stagingDirectory: File by lazy {
        File(requireNotNull(applicationContext.getExternalFilesDir(null)), "plugins/inbox")
    }

    @Volatile
    private var started = false
    private var observer: FileObserver? = null
    private var pendingScan: ScheduledFuture<*>? = null
    private var activePlugin: LoadedLoginPlugin? = null

    /** Inicia observação e agenda recuperação/varredura de boot. */
    @Synchronized
    fun start() {
        if (started) return
        try {
            check(!worker.isShutdown) { "PluginManager já foi encerrado" }
            val directory = stagingDirectory
            check(directory.mkdirs() || directory.isDirectory) {
                "Não foi possível criar a pasta dinâmica"
            }
            started = true
            observer = createObserver(directory).also { it.startWatching() }
            scheduleScan(delayMillis = 0)
        } catch (error: Exception) {
            started = false
            observer?.stopWatching()
            observer = null
            audit(
                PluginTransition(
                    status = PluginRuntimeStatus.ERROR,
                    reason = error.javaClass.simpleName,
                ),
                error,
            )
            publish(
                PluginHostState(
                    status = PluginRuntimeStatus.ERROR,
                    message = "Plugin de autenticação indisponível.",
                ),
            )
        }
    }

    /** Para o watcher e cancela varredura pendente; a instância ativa permanece estável. */
    @Synchronized
    fun stop() {
        if (!started) return
        started = false
        observer?.stopWatching()
        observer = null
        pendingScan?.cancel(false)
        pendingScan = null
    }

    /**
     * Converte exceção de criação de View/callback em recuperação cooperativa.
     * Credenciais e argumentos do plugin não são registrados.
     */
    fun reportRuntimeFailure(error: Throwable) {
        worker.execute {
            val current = activePlugin
            activePlugin = null
            runCatching { current?.detach() }
            val transition = PluginTransition(
                status = PluginRuntimeStatus.ERROR,
                version = current?.pluginVersion,
                digestSha256 = current?.digestSha256,
                reason = error.javaClass.simpleName,
            )
            audit(transition, error)
            publish(
                PluginHostState(
                    status = PluginRuntimeStatus.ERROR,
                    message = "Plugin de autenticação indisponível.",
                ),
            )
        }
    }

    /**
     * Executa logout do plugin na fila serial e sempre conclui no main looper.
     *
     * O resultado informa somente se a API confirmou o logout; o host deve
     * remover o acesso local mesmo quando o retorno for falso.
     */
    fun logout(sessionId: String, onCompleted: (Boolean) -> Unit) {
        worker.execute {
            val completed = try {
                activePlugin?.logout(sessionId) == true
            } catch (error: Exception) {
                audit(
                    PluginTransition(
                        status = PluginRuntimeStatus.ERROR,
                        reason = error.javaClass.simpleName,
                    ),
                    error,
                )
                false
            } catch (error: LinkageError) {
                audit(
                    PluginTransition(
                        status = PluginRuntimeStatus.ERROR,
                        reason = error.javaClass.simpleName,
                    ),
                    error,
                )
                false
            }
            mainHandler.post { onCompleted(completed) }
        }
    }

    /** Libera observer, fila e referência cooperativa da instância ativa. */
    override fun close() {
        stop()
        val current = activePlugin
        activePlugin = null
        runCatching { current?.detach() }
        worker.shutdownNow()
    }

    /** Cria o watcher que apenas filtra eventos finais de APK e agenda trabalho. */
    private fun createObserver(directory: File): FileObserver =
        object : FileObserver(directory, CLOSE_WRITE or MOVED_TO) {
            override fun onEvent(event: Int, path: String?) {
                if (event and (CLOSE_WRITE or MOVED_TO) == 0) return
                if (path?.endsWith(".apk", ignoreCase = true) != true) return
                scheduleScan()
            }
        }

    /** Aplica debounce para serializar eventos repetidos do sistema de arquivos. */
    @Synchronized
    private fun scheduleScan(delayMillis: Long = DebounceMillis) {
        if (!started || worker.isShutdown) return
        pendingScan?.cancel(false)
        pendingScan = worker.schedule(::scan, delayMillis, TimeUnit.MILLISECONDS)
    }

    /** Prioriza o bootstrap debug atual e usa o repositório anterior como fallback. */
    private fun scan() {
        val bundledCandidate = if (activePlugin == null) {
            try {
                BundledLoginPluginInstaller.installIfPresent(applicationContext, stagingDirectory)
            } catch (error: Exception) {
                audit(
                    PluginTransition(
                        status = PluginRuntimeStatus.ERROR,
                        reason = error.javaClass.simpleName,
                    ),
                    error,
                )
                publish(
                    PluginHostState(
                        status = PluginRuntimeStatus.ERROR,
                        message = "Plugin de autenticação indisponível.",
                    ),
                )
                return
            }
        } else {
            null
        }

        val candidates = BundledLoginPluginInstaller.prioritize(
            bundledCandidate = bundledCandidate,
            inboxCandidates = stagingDirectory.listFiles { file ->
            file.isFile && file.extension.equals("apk", ignoreCase = true)
            }.orEmpty().asList(),
        )

        if (activePlugin == null && bundledCandidate != null) {
            processCandidate(bundledCandidate)
        }

        if (activePlugin == null) {
            LoginPluginLoader.findVerified(applicationContext, ::onTransition)
                .firstOrNull()
                ?.let(::activate)
        }

        if (activePlugin == null && candidates.isEmpty()) {
            publish(
                PluginHostState(
                    status = PluginRuntimeStatus.WAITING,
                    message = "Aguardando plugin de autenticação na pasta dinâmica.",
                ),
            )
        }
        candidates
            .filterNot { candidate -> candidate.absoluteFile == bundledCandidate?.absoluteFile }
            .forEach(::processCandidate)
    }

    /** Executa o pipeline de um candidato sem afetar uma instância ativa em rejeição. */
    private fun processCandidate(candidate: File) {
        try {
            val verified = LoginPluginLoader.stageAndVerify(
                context = applicationContext,
                stagingRoot = stagingDirectory,
                candidate = candidate,
                onTransition = ::onTransition,
            )
            val current = activePlugin
            when {
                current == null -> activate(verified)
                current.digestSha256 == verified.digestSha256 -> Unit
                else -> {
                    val transition = verified.transition(PluginRuntimeStatus.PENDING_RESTART)
                    audit(transition)
                    publish(
                        PluginHostState(
                            status = PluginRuntimeStatus.PENDING_RESTART,
                            activePlugin = current,
                            message = "Atualização validada e pendente de reinício.",
                        ),
                    )
                }
            }
        } catch (error: Exception) {
            if (activePlugin == null) {
                publish(
                    PluginHostState(
                        status = PluginRuntimeStatus.REJECTED,
                        message = "Plugin de autenticação rejeitado.",
                    ),
                )
            }
        } catch (error: LinkageError) {
            if (activePlugin == null) {
                publish(
                    PluginHostState(
                        status = PluginRuntimeStatus.REJECTED,
                        message = "Plugin de autenticação rejeitado.",
                    ),
                )
            }
        }
    }

    /** Carrega uma revisão verificada e só a publica após `onActivate` concluído. */
    private fun activate(verified: VerifiedLoginPlugin) {
        try {
            val loaded = LoginPluginLoader.load(applicationContext, verified, ::onTransition)
            activePlugin = loaded
            publish(
                PluginHostState(
                    status = PluginRuntimeStatus.ACTIVE,
                    activePlugin = loaded,
                    message = "Plugin de autenticação ativo.",
                ),
            )
        } catch (error: Exception) {
            activePlugin = null
            publish(
                PluginHostState(
                    status = PluginRuntimeStatus.ERROR,
                    message = "Plugin de autenticação indisponível.",
                ),
            )
        } catch (error: LinkageError) {
            activePlugin = null
            publish(
                PluginHostState(
                    status = PluginRuntimeStatus.ERROR,
                    message = "Plugin de autenticação indisponível.",
                ),
            )
        }
    }

    /** Audita cada transição e publica progresso enquanto ainda não há plugin ativo. */
    private fun onTransition(transition: PluginTransition) {
        audit(transition)
        if (transition.status in TransitionalStatuses && activePlugin == null) {
            publish(
                PluginHostState(
                    status = transition.status,
                    message = "Preparando plugin de autenticação.",
                ),
            )
        }
    }

    /** Registra apenas identidade, versão, digest abreviado, estado e classe da falha. */
    private fun audit(transition: PluginTransition, error: Throwable? = null) {
        val digest = transition.digestSha256?.take(DigestLogLength)
        val message = buildString {
            append("status=${transition.status}")
            transition.pluginId?.let { append(" pluginId=$it") }
            transition.version?.let { append(" version=$it") }
            digest?.let { append(" digest=$it") }
            transition.reason?.let { append(" reason=$it") }
        }
        if (error != null && isDebuggable()) {
            Log.e(LogTag, message, error)
        } else {
            Log.i(LogTag, message)
        }
    }

    /** Transfere toda alteração de estado observável para o main looper. */
    private fun publish(state: PluginHostState) {
        mainHandler.post { onStateChanged(state) }
    }

    /** Limita stack traces ao build explicitamente marcado como debuggable. */
    private fun isDebuggable(): Boolean =
        applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    private fun VerifiedLoginPlugin.transition(status: PluginRuntimeStatus) = PluginTransition(
        status = status,
        pluginId = descriptor.pluginId,
        version = descriptor.pluginVersion,
        digestSha256 = digestSha256,
    )

    private companion object {
        const val LogTag = "DynamicPluginManager"
        const val DebounceMillis = 300L
        const val DigestLogLength = 12
        val TransitionalStatuses = setOf(
            PluginRuntimeStatus.DISCOVERED,
            PluginRuntimeStatus.STAGED,
            PluginRuntimeStatus.VERIFIED,
            PluginRuntimeStatus.LOADED,
            PluginRuntimeStatus.ATTACHED,
        )
    }
}
