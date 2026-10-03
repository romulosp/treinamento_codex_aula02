package br.com.romulopenha.diagnosticopinpad.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.romulopenha.diagnosticopinpad.BuildConfig
import br.com.romulopenha.diagnosticopinpad.data.AppLogger
import br.com.romulopenha.diagnosticopinpad.data.CatalogAction
import br.com.romulopenha.diagnosticopinpad.data.CatalogInput
import br.com.romulopenha.diagnosticopinpad.data.DiagnosticFailure
import br.com.romulopenha.diagnosticopinpad.data.DiagnosticRepository
import br.com.romulopenha.diagnosticopinpad.data.EndpointConfig
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Estado da ação solicitada pelo operador, independente da conectividade e da sessão. */
enum class ActionStatus { IDLE, RUNNING, SUCCESS, CANCELING, ERROR }

/** Estado observado do transporte até o Bridge Windows. */
enum class BridgeStatus { UNKNOWN, REACHABLE, UNREACHABLE }

/** Estado confirmado da sessão ABECS para o endpoint atualmente editado. */
enum class SessionStatus {
    CLOSED,
    OPEN,
    BUSY,
    UNKNOWN;

    companion object {
        /** Converte somente estados conhecidos publicados pelo binding. */
        fun fromBinding(value: String): SessionStatus = entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}

/** Estado imutável renderizado pela tela Compose. */
data class DiagnosticUiState(
    val host: String = BuildConfig.DEFAULT_BRIDGE_HOST,
    val port: String = BuildConfig.DEFAULT_BRIDGE_PORT.toString(),
    val timeoutMillis: String = BuildConfig.DEFAULT_TIMEOUT_MILLIS.toString(),
    val actionStatus: ActionStatus = ActionStatus.IDLE,
    val bridgeStatus: BridgeStatus = BridgeStatus.UNKNOWN,
    val sessionStatus: SessionStatus = SessionStatus.CLOSED,
    val action: String = "",
    val operationId: String? = null,
    val version: String = "",
    val result: String = "",
    val error: String? = null,
    val durationMillis: Long? = null,
    val lastOperationId: String? = null,
    val errorCode: String? = null,
    val errorPhase: String? = null,
)

/**
 * State holder da tela. Operações bloqueantes são deslocadas para IO e toda
 * alteração chega à UI por um StateFlow imutável.
 */
class DiagnosticViewModel(
    private val repository: DiagnosticRepository,
    private val logger: AppLogger,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiagnosticUiState())

    /** Estado que a tela deve observar com coleta lifecycle-aware. */
    val uiState: StateFlow<DiagnosticUiState> = _uiState.asStateFlow()

    private var operationJob: Job? = null
    private var endpointCleanupJob: Job? = null
    private var activeOperationId: String? = null
    private val cleanupScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    /** Atualiza o host editado enquanto não há operação em execução. */
    fun updateHost(value: String) = updateInput { copy(host = value) }

    /** Atualiza a porta editada enquanto não há operação em execução. */
    fun updatePort(value: String) = updateInput { copy(port = value) }

    /** Atualiza o timeout editado enquanto não há operação em execução. */
    fun updateTimeout(value: String) = updateInput { copy(timeoutMillis = value) }

    /** Consulta a versão de build exposta pelo binding Go. */
    fun checkVersion() = launchAction("version") {
        val version = repository.version()
        _uiState.update { it.copy(version = version, result = version) }
    }

    /** Testa o Bridge sem encaminhar bytes ABECS à serial. */
    fun ping() = launchConfigured("ping") { config, id ->
        repository.ping(config, id)
        _uiState.update { it.copy(result = "PONG", bridgeStatus = BridgeStatus.REACHABLE) }
    }

    /** Abre a sessão ABECS pelo transporte configurado. */
    fun open() = launchConfigured("open") { config, id ->
        repository.open(config, id)
        _uiState.update { it.copy(result = "Sessão aberta", bridgeStatus = BridgeStatus.REACHABLE) }
    }

    /** Consulta informações tipadas do dispositivo por GIX. */
    fun getInfo() {
        if (!requireSession()) return
        launchConfigured("get_info") { _, id ->
            val result = repository.getInfoJson(id)
            _uiState.update { it.copy(result = result) }
        }
    }

    /** Executa uma ação nomeada do catálogo, mantendo parâmetros fora dos logs. */
    fun executeCatalog(action: CatalogAction, input: CatalogInput = CatalogInput()) {
        if (!action.enabled || action == CatalogAction.EXIT) {
            setLocalError(action.disabledReason ?: "Ação inválida")
            return
        }
        if (requiresSession(action) && !requireSession()) return
        input.validate(action)?.let { validationError ->
            setLocalError(validationError)
            return
        }
        launchConfigured(action.title) { config, id ->
            val result = repository.executeCatalog(config, action, input, id)
            _uiState.update { it.copy(result = result) }
        }
    }

    /** Fecha a sessão antes de entregar o controle de saída à Activity. */
    fun exit(onFinished: () -> Unit) {
        viewModelScope.launch {
            operationJob?.let { current ->
                activeOperationId?.let { id -> withContext(ioDispatcher) { runCatching { repository.cancel(id) } } }
                current.cancelAndJoin()
            }
            val cleanup = withContext(ioDispatcher) { runCatching { repository.close(UUID.randomUUID().toString()) } }
            cleanup.exceptionOrNull()?.let { failure ->
                val safe = repository.classifyFailure(failure)
                _uiState.update {
                    it.copy(actionStatus = ActionStatus.ERROR, sessionStatus = confirmedSession(),
                        error = safe.message, errorCode = safe.code, errorPhase = safe.phase)
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    actionStatus = ActionStatus.SUCCESS,
                    sessionStatus = SessionStatus.CLOSED,
                    operationId = null,
                )
            }
            withContext(Dispatchers.Main.immediate) { onFinished() }
        }
    }

    /** Cancela a operação ativa no Go e na coroutine que a aguarda. */
    fun cancel() {
        val id = activeOperationId ?: return
        _uiState.update { it.copy(actionStatus = ActionStatus.CANCELING) }
        viewModelScope.launch(ioDispatcher) {
            runCatching { repository.cancel(id) }
            operationJob?.cancel()
        }
    }

    /** Fecha a sessão e libera a porta do Bridge. */
    fun close() {
        val current = operationJob
        if (current?.isActive == true) {
            viewModelScope.launch {
                _uiState.update { it.copy(actionStatus = ActionStatus.CANCELING) }
                activeOperationId?.let { id -> withContext(ioDispatcher) { runCatching { repository.cancel(id) } } }
                current.cancelAndJoin()
                launchClose()
            }
        } else {
            launchClose()
        }
    }

    override fun onCleared() {
        operationJob?.cancel()
        val id = activeOperationId
        cleanupScope.launch {
            runCatching {
                if (id != null) repository.cancel(id)
                repository.close()
            }
        }
        super.onCleared()
    }

    private fun launchClose() = launchAction("close") {
        repository.close(activeOperationId.orEmpty())
        _uiState.update { it.copy(result = "Sessão fechada", sessionStatus = SessionStatus.CLOSED) }
    }

    private fun launchConfigured(
        action: String,
        block: suspend (EndpointConfig, String) -> Unit,
    ) {
        val config = endpointOrError() ?: return
        launchAction(action) {
            val id = activeOperationId ?: error("operation id ausente")
            block(config, id)
        }
    }

    private fun launchAction(action: String, block: suspend () -> Unit) {
        if (operationJob?.isActive == true) {
            setLocalError("Já existe uma operação em andamento")
            return
        }
        val id = UUID.randomUUID().toString()
        activeOperationId = id
        operationJob = viewModelScope.launch {
            val startedAt = System.nanoTime()
            _uiState.update {
                it.copy(
                    actionStatus = ActionStatus.RUNNING,
                    action = action,
                    operationId = id,
                    error = null,
                    errorCode = null,
                    errorPhase = null,
                    result = "",
                    durationMillis = null,
                )
            }
            logEvent("INFO", "operation_started", id)
            try {
                endpointCleanupJob?.join()
                withContext(ioDispatcher) { block() }
                val duration = elapsedMillis(startedAt)
                _uiState.update {
                    it.copy(
                        actionStatus = ActionStatus.SUCCESS,
                        durationMillis = duration,
                        operationId = null,
                        lastOperationId = id,
                        sessionStatus = confirmedSession(),
                    )
                }
                logEvent("INFO", "operation_finished", id, mapOf("durationMillis" to duration.toString()))
            } catch (cancelled: CancellationException) {
                val duration = elapsedMillis(startedAt)
                _uiState.update {
                    it.copy(
                        actionStatus = ActionStatus.ERROR,
                        error = DiagnosticFailure.messageFor("CANCELED"),
                        errorCode = "CANCELED",
                        errorPhase = "command",
                        sessionStatus = confirmedSession(),
                        durationMillis = duration,
                        operationId = null,
                        lastOperationId = id,
                    )
                }
                logEvent("INFO", "operation_canceled", id, mapOf("durationMillis" to duration.toString()))
                throw cancelled
            } catch (failure: Throwable) {
                val safe = repository.classifyFailure(failure)
                val duration = elapsedMillis(startedAt)
                _uiState.update {
                    it.copy(
                        actionStatus = ActionStatus.ERROR,
                        error = safe.message,
                        errorCode = safe.code,
                        errorPhase = safe.phase,
                        sessionStatus = confirmedSession(),
                        durationMillis = duration,
                        operationId = null,
                        lastOperationId = id,
                        bridgeStatus = if (safe.code in setOf("BRIDGE_UNREACHABLE", "DISCONNECTED")) {
                            BridgeStatus.UNREACHABLE
                        } else {
                            it.bridgeStatus
                        },
                    )
                }
                logEvent(
                    "ERROR",
                    "operation_failed",
                    id,
                    mapOf("errorCode" to safe.code, "phase" to safe.phase, "durationMillis" to duration.toString()),
                )
            } finally {
                activeOperationId = null
                operationJob = null
            }
        }
    }

    private fun endpointOrError(): EndpointConfig? {
        val state = _uiState.value
        val config = EndpointConfig(
            host = state.host.trim(),
            port = state.port.toIntOrNull() ?: -1,
            timeoutMillis = state.timeoutMillis.toLongOrNull() ?: -1,
        )
        val error = config.validate()
        if (error != null) {
            setLocalError(error)
            return null
        }
        return config
    }

    private fun updateInput(update: DiagnosticUiState.() -> DiagnosticUiState) {
        if (operationJob?.isActive == true) return
        val previous = _uiState.value
        val updated = previous.update()
        if (updated == previous) return
        _uiState.value = updated.copy(
            actionStatus = ActionStatus.IDLE,
            bridgeStatus = BridgeStatus.UNKNOWN,
            sessionStatus = SessionStatus.CLOSED,
            result = "",
            error = null,
            errorCode = null,
            errorPhase = null,
        )
        if (endpointCleanupJob?.isActive != true) {
            endpointCleanupJob = viewModelScope.launch(ioDispatcher) {
                runCatching { repository.close() }.onFailure { failure ->
                    val safe = repository.classifyFailure(failure)
                    logger.event(
                        "ERROR",
                        "endpoint_cleanup_failed",
                        fields = mapOf("errorCode" to safe.code, "phase" to safe.phase),
                    )
                }
            }
        }
    }

    private fun confirmedSession(): SessionStatus {
        val state = _uiState.value
        val endpoint = EndpointConfig(
            state.host.trim(),
            state.port.toIntOrNull() ?: -1,
            state.timeoutMillis.toLongOrNull() ?: -1,
        )
        return SessionStatus.fromBinding(repository.stateFor(endpoint))
    }

    private fun setLocalError(message: String) {
        _uiState.update {
            it.copy(
                actionStatus = ActionStatus.ERROR,
                result = "",
                error = message,
                errorCode = "BINDING_ERROR",
                errorPhase = "configuration",
            )
        }
    }

    /** Registra eventos fora da main thread, preservando cancelamentos observáveis. */
    private suspend fun logEvent(
        level: String,
        event: String,
        operationId: String,
        fields: Map<String, String> = emptyMap(),
    ) = withContext(NonCancellable + ioDispatcher) {
        val state = _uiState.value
        logger.event(
            level,
            event,
            operationId,
            fields + mapOf(
                "action" to state.action,
                "actionState" to state.actionStatus.name,
                "bridgeState" to state.bridgeStatus.name,
                "sessionState" to state.sessionStatus.name,
            ),
        )
    }

    private fun requireSession(): Boolean {
        if (_uiState.value.sessionStatus == SessionStatus.OPEN) return true
        _uiState.update {
            it.copy(
                actionStatus = ActionStatus.ERROR,
                result = "",
                errorCode = "PINPAD_CLOSED",
                errorPhase = "command",
                error = DiagnosticFailure.messageFor("PINPAD_CLOSED"),
            )
        }
        return false
    }

    private fun elapsedMillis(startedAt: Long): Long = (System.nanoTime() - startedAt) / 1_000_000

    companion object {
        /** Somente ações que não dialogam com o pinpad dispensam sessão confirmada. */
        fun requiresSession(action: CatalogAction): Boolean = action !in setOf(
            CatalogAction.OPEN,
            CatalogAction.CLOSE,
            CatalogAction.STATE,
            CatalogAction.EXIT,
            CatalogAction.UNAVAILABLE,
            CatalogAction.RESERVED,
        )
    }
}
