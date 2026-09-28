package br.com.romulopenha.diagnosticopinpad.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.romulopenha.diagnosticopinpad.data.DiagnosticRepository
import br.com.romulopenha.diagnosticopinpad.data.EndpointConfig
import br.com.romulopenha.diagnosticopinpad.data.AppLogger
import br.com.romulopenha.diagnosticopinpad.data.CatalogAction
import br.com.romulopenha.diagnosticopinpad.data.CatalogInput
import br.com.romulopenha.diagnosticopinpad.data.DiagnosticFailure
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Estados observáveis da única tela do laboratório. */
enum class DiagnosticStatus {
    IDLE,
    CONNECTING,
    OPEN,
    RUNNING,
    CANCELING,
    ERROR,
    CLOSED,
}

/** Estado imutável renderizado pela tela Compose. */
data class DiagnosticUiState(
    val host: String = "localhost",
    val port: String = "39100",
    val timeoutMillis: String = "10000",
    val status: DiagnosticStatus = DiagnosticStatus.IDLE,
    val action: String = "",
    val operationId: String? = null,
    val version: String = "",
    val result: String = "",
    val error: String? = null,
    val durationMillis: Long? = null,
    val sessionState: String = "CLOSED",
    val bridgeReachable: Boolean? = null,
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
    fun checkVersion() {
        launchAction("version", DiagnosticStatus.RUNNING) {
            val version = repository.version()
            _uiState.update { it.copy(version = version, result = version) }
        }
    }

    /** Testa o Bridge sem encaminhar bytes ABECS à serial. */
    fun ping() = launchConfigured("ping", DiagnosticStatus.CONNECTING) { config, id ->
        repository.ping(config, id)
        _uiState.update { it.copy(result = "PONG", bridgeReachable = true) }
    }

    /** Abre a sessão ABECS pelo transporte configurado. */
    fun open() = launchConfigured("open", DiagnosticStatus.CONNECTING) { config, id ->
        repository.open(config, id)
        _uiState.update { it.copy(result = "Sessão aberta", bridgeReachable = true) }
    }

    /** Consulta informações tipadas do dispositivo por GIX. */
    fun getInfo() {
        if (!requireSession()) return
        launchConfigured("get_info", DiagnosticStatus.RUNNING) { _, id ->
        val result = repository.getInfoJson(id)
        _uiState.update { it.copy(result = result) }
        }
    }

    /** Executa uma ação nomeada do catálogo, mantendo parâmetros fora dos logs. */
    fun executeCatalog(action: CatalogAction, input: CatalogInput = CatalogInput()) {
        if (!action.enabled || action == CatalogAction.EXIT) {
            _uiState.update { it.copy(status = DiagnosticStatus.ERROR, error = action.disabledReason ?: "Ação inválida") }
            return
        }
        if (requiresSession(action) && !requireSession()) return
        input.validate(action)?.let { validationError ->
            _uiState.update { it.copy(status = DiagnosticStatus.ERROR, error = validationError) }
            return
        }
        launchConfigured(action.title, DiagnosticStatus.RUNNING) { config, id ->
            val result = repository.executeCatalog(config, action, input, id)
            _uiState.update { it.copy(result = result) }
        }
    }

    /** Fecha a sessão antes de entregar o controle de saída à Activity. */
    fun exit(onFinished: () -> Unit) {
        viewModelScope.launch {
            operationJob?.let { current ->
                activeOperationId?.let { id -> runCatching { repository.cancel(id) } }
                current.cancelAndJoin()
            }
            runCatching { repository.close() }
            _uiState.update { it.copy(status = DiagnosticStatus.CLOSED, operationId = null) }
            withContext(Dispatchers.Main.immediate) { onFinished() }
        }
    }

    /** Cancela a operação ativa no Go e na coroutine que a aguarda. */
    fun cancel() {
        val id = activeOperationId ?: return
        _uiState.update { it.copy(status = DiagnosticStatus.CANCELING) }
        viewModelScope.launch(ioDispatcher) {
            runCatching { repository.cancel(id) }
            operationJob?.cancel()
        }
    }

    /** Fecha a sessão e libera a porta do Bridge. */
    fun close() {
        viewModelScope.launch {
            operationJob?.let { current ->
                runCatching { repository.cancel(activeOperationId ?: "") }
                current.cancelAndJoin()
            }
            launchAction("close", DiagnosticStatus.CLOSED) {
                repository.close()
                _uiState.update { it.copy(status = DiagnosticStatus.CLOSED, result = "Sessão fechada") }
            }
        }
    }

    override fun onCleared() {
        operationJob?.cancel()
        val id = activeOperationId
        cleanupScope.launch {
            runCatching {
                if (id != null) {
                    repository.cancel(id)
                }
                repository.close()
            }
        }
        super.onCleared()
    }

    private fun launchConfigured(
        action: String,
        initialStatus: DiagnosticStatus,
        block: suspend (EndpointConfig, String) -> Unit,
    ) {
        val config = endpointOrError() ?: return
        launchAction(action, initialStatus) {
            val id = activeOperationId ?: error("operation id ausente")
            block(config, id)
        }
    }

    private fun launchAction(
        action: String,
        initialStatus: DiagnosticStatus,
        block: suspend () -> Unit,
    ) {
        if (operationJob?.isActive == true) {
            _uiState.update { it.copy(error = "Já existe uma operação em andamento") }
            return
        }
        val id = UUID.randomUUID().toString()
        activeOperationId = id
        operationJob = viewModelScope.launch {
            val startedAt = System.nanoTime()
            _uiState.update {
                it.copy(
                    status = initialStatus,
                    action = action,
                    operationId = id,
                    lastOperationId = id,
                    error = null,
                    errorCode = null,
                    errorPhase = null,
                    result = "",
                    durationMillis = null,
                )
            }
            logger.event("INFO", "operation_started", id)
            try {
                endpointCleanupJob?.join()
                withContext(ioDispatcher) { block() }
                val duration = (System.nanoTime() - startedAt) / 1_000_000
                val session = confirmedSession()
                _uiState.update { it.copy(durationMillis = duration, operationId = null, sessionState = session,
                    status = if (session == "OPEN") DiagnosticStatus.OPEN else DiagnosticStatus.CLOSED) }
                logger.event("INFO", "operation_finished", id, mapOf("durationMillis" to duration.toString()))
            } catch (cancelled: CancellationException) {
                _uiState.update {
                    it.copy(status = DiagnosticStatus.ERROR, error = "Operação cancelada", errorCode = "CANCELED", errorPhase = "command",
                        sessionState = confirmedSession(), durationMillis = (System.nanoTime() - startedAt) / 1_000_000, operationId = null)
                }
                logger.event("INFO", "operation_canceled", id)
                throw cancelled
            } catch (failure: Throwable) {
                val safe = DiagnosticFailure.from(failure)
                _uiState.update {
                    it.copy(status = DiagnosticStatus.ERROR, error = safe.message, errorCode = safe.code, errorPhase = safe.phase,
                        sessionState = confirmedSession(), durationMillis = (System.nanoTime() - startedAt) / 1_000_000, operationId = null,
                        bridgeReachable = if (safe.code in setOf("BRIDGE_UNREACHABLE", "DISCONNECTED")) false else it.bridgeReachable)
                }
                logger.event("ERROR", "operation_failed", id, mapOf("errorCode" to safe.code, "phase" to safe.phase))
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
            _uiState.update { it.copy(status = DiagnosticStatus.ERROR, error = error) }
            return null
        }
        return config
    }

    private fun updateInput(update: DiagnosticUiState.() -> DiagnosticUiState) {
        if (operationJob?.isActive != true) {
            val previous = _uiState.value
            if (previous.update() == previous) return
            _uiState.update { it.update().copy(sessionState = "CLOSED", bridgeReachable = null, status = DiagnosticStatus.IDLE) }
            // Não abre o endpoint novo nem bloqueia a digitação. Toda próxima
            // ação aguarda a liberação do cliente anterior no dispatcher IO.
            if (endpointCleanupJob?.isActive != true) {
                endpointCleanupJob = viewModelScope.launch(ioDispatcher) {
                    runCatching { repository.close() }.onFailure { failure ->
                        val safe = DiagnosticFailure.from(failure)
                        logger.event("ERROR", "endpoint_cleanup_failed", fields = mapOf("errorCode" to safe.code, "phase" to safe.phase))
                    }
                }
            }
        }
    }

    private fun confirmedSession(): String {
        val state = _uiState.value
        return repository.stateFor(EndpointConfig(state.host.trim(), state.port.toIntOrNull() ?: -1, state.timeoutMillis.toLongOrNull() ?: -1))
    }

    private fun requireSession(): Boolean {
        if (_uiState.value.sessionState == "OPEN") return true
        _uiState.update { it.copy(status = DiagnosticStatus.ERROR, result = "", errorCode = "PINPAD_CLOSED", errorPhase = "command",
            error = DiagnosticFailure.messageFor("PINPAD_CLOSED")) }
        return false
    }

    companion object {
        /** Somente ações que dialogam com o pinpad exigem sessão confirmada. */
        fun requiresSession(action: CatalogAction): Boolean = action !in setOf(
            CatalogAction.OPEN, CatalogAction.CLOSE, CatalogAction.STATE, CatalogAction.EXIT,
            CatalogAction.UNAVAILABLE, CatalogAction.RESERVED, CatalogAction.QR,
        )
    }
}
