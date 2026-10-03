package br.com.romulopenha.diagnosticopinpad.ui

import br.com.romulopenha.diagnosticopinpad.data.AppLogger
import br.com.romulopenha.diagnosticopinpad.data.DiagnosticRepository
import br.com.romulopenha.diagnosticopinpad.data.EndpointConfig
import br.com.romulopenha.diagnosticopinpad.data.CatalogAction
import br.com.romulopenha.diagnosticopinpad.data.CatalogInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Testes de estado e fluxo da tela sem AAR, Bridge ou hardware. */
@OptIn(ExperimentalCoroutinesApi::class)
class DiagnosticViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ping publica pong sem abrir sessao`() = runTest {
        val repository = FakeRepository()
        val viewModel = DiagnosticViewModel(repository, NoopLogger, dispatcher)

        viewModel.ping()
        advanceUntilIdle()

        assertEquals("PONG", viewModel.uiState.value.result)
        assertEquals(ActionStatus.SUCCESS, viewModel.uiState.value.actionStatus)
        assertEquals(SessionStatus.CLOSED, viewModel.uiState.value.sessionStatus)
        assertEquals(BridgeStatus.REACHABLE, viewModel.uiState.value.bridgeStatus)
        assertEquals(1, repository.pingCalls)
    }

    @Test
    fun `configuracao invalida nao chama repository`() = runTest {
        val repository = FakeRepository()
        val viewModel = DiagnosticViewModel(repository, NoopLogger, dispatcher)

        viewModel.updatePort("0")
        viewModel.ping()
        advanceUntilIdle()

        assertEquals(ActionStatus.ERROR, viewModel.uiState.value.actionStatus)
        assertEquals(0, repository.pingCalls)
    }

    @Test
    fun `acao do catalogo publica resultado sanitizado`() = runTest {
        val repository = FakeRepository()
        val viewModel = DiagnosticViewModel(repository, NoopLogger, dispatcher)

        viewModel.executeCatalog(CatalogAction.STATE, CatalogInput())
        advanceUntilIdle()

        assertEquals("CLOSED", viewModel.uiState.value.result)
        assertEquals(CatalogAction.STATE.title, viewModel.uiState.value.action)
        assertEquals(1, repository.catalogCalls)
        assertEquals(SessionStatus.CLOSED, viewModel.uiState.value.sessionStatus)
        assertEquals(ActionStatus.SUCCESS, viewModel.uiState.value.actionStatus)
    }

    @Test
    fun `abrir so habilita sessao quando repository confirma`() = runTest {
        val repository = FakeRepository()
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.open()
        advanceUntilIdle()
        assertEquals(SessionStatus.OPEN, vm.uiState.value.sessionStatus)
        assertEquals(ActionStatus.SUCCESS, vm.uiState.value.actionStatus)
        vm.close()
        advanceUntilIdle()
        assertEquals(SessionStatus.CLOSED, vm.uiState.value.sessionStatus)
    }

    @Test
    fun `erro de abrir preserva codigo fase id e limpa resultado antigo`() = runTest {
        val repository = FakeRepository().apply { openError = IllegalStateException("SERIAL_UNAVAILABLE:serial_open") }
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.checkVersion(); advanceUntilIdle()
        vm.open(); advanceUntilIdle()
        assertEquals(ActionStatus.ERROR, vm.uiState.value.actionStatus)
        assertEquals(SessionStatus.CLOSED, vm.uiState.value.sessionStatus)
        assertEquals("SERIAL_UNAVAILABLE", vm.uiState.value.errorCode)
        assertEquals("serial_open", vm.uiState.value.errorPhase)
        assertEquals("", vm.uiState.value.result)
        org.junit.Assert.assertNotNull(vm.uiState.value.lastOperationId)
        org.junit.Assert.assertNotNull(vm.uiState.value.durationMillis)
    }

    @Test
    fun `catalogo DSP nao atravessa fronteira com sessao fechada`() = runTest {
        val repository = FakeRepository()
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.executeCatalog(CatalogAction.DSP, CatalogInput(mapOf("line1" to "TESTE", "line2" to "TESTE")))
        advanceUntilIdle()
        assertEquals(0, repository.catalogCalls)
        assertEquals("PINPAD_CLOSED", vm.uiState.value.errorCode)
    }

    @Test
    fun `version e consulta local nao alteram sessao fechada`() = runTest {
        val vm = DiagnosticViewModel(FakeRepository(), NoopLogger, dispatcher)
        vm.checkVersion(); advanceUntilIdle()
        assertEquals(SessionStatus.CLOSED, vm.uiState.value.sessionStatus)
        vm.executeCatalog(CatalogAction.STATE); advanceUntilIdle()
        assertEquals(SessionStatus.CLOSED, vm.uiState.value.sessionStatus)
    }

    @Test
    fun `QR exige sessao confirmada`() = runTest {
        val repository = FakeRepository()
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.executeCatalog(CatalogAction.QR, CatalogInput(mapOf("data" to "teste")))
        advanceUntilIdle()
        assertEquals(0, repository.catalogCalls)
        assertEquals("PINPAD_CLOSED", vm.uiState.value.errorCode)
    }

    @Test
    fun `ping falho mostra erro sem confirmar sessao ou Bridge`() = runTest {
        val repository = FakeRepository().apply { pingError = Exception("BRIDGE_UNREACHABLE:ping") }
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.ping()
        advanceUntilIdle()
        assertEquals(ActionStatus.ERROR, vm.uiState.value.actionStatus)
        assertEquals(BridgeStatus.UNREACHABLE, vm.uiState.value.bridgeStatus)
        assertEquals(SessionStatus.CLOSED, vm.uiState.value.sessionStatus)
        assertEquals("ping", vm.uiState.value.errorPhase)
    }

    @Test
    fun `editar endpoint libera cliente anterior antes da proxima acao`() = runTest {
        val repository = FakeRepository()
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.open(); advanceUntilIdle()
        vm.updatePort("39101")
        assertEquals(SessionStatus.CLOSED, vm.uiState.value.sessionStatus)
        vm.checkVersion(); advanceUntilIdle()
        assertEquals(1, repository.closeCalls)
        assertEquals("CLOSED", repository.session)
        assertEquals(SessionStatus.CLOSED, vm.uiState.value.sessionStatus)
    }

    @Test
    fun `cancelar operacao ativa chama Go e preserva ID final`() = runTest {
        val repository = FakeRepository().apply { infoGate = CompletableDeferred() }
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.open(); advanceUntilIdle()
        vm.getInfo(); runCurrent()
        val operation = vm.uiState.value.operationId
        vm.cancel(); advanceUntilIdle()
        assertEquals(operation, repository.canceledIds.single())
        assertEquals(operation, vm.uiState.value.lastOperationId)
        assertEquals("CANCELED", vm.uiState.value.errorCode)
        assertEquals(null, vm.uiState.value.operationId)
        assertEquals(SessionStatus.OPEN, vm.uiState.value.sessionStatus)
    }

    @Test
    fun `fechar aguarda cancelamento e libera sessao ativa`() = runTest {
        val repository = FakeRepository().apply { infoGate = CompletableDeferred() }
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.open(); advanceUntilIdle()
        vm.getInfo(); runCurrent()
        vm.close(); advanceUntilIdle()
        assertEquals(listOf("cancel", "close"), repository.cleanupEvents)
        assertEquals(SessionStatus.CLOSED, vm.uiState.value.sessionStatus)
        assertEquals(ActionStatus.SUCCESS, vm.uiState.value.actionStatus)
    }

    @Test
    fun `saida encerra somente depois de cancelar e fechar`() = runTest {
        val repository = FakeRepository().apply { infoGate = CompletableDeferred() }
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.open(); advanceUntilIdle()
        vm.getInfo(); runCurrent()
        vm.exit { repository.cleanupEvents.add("finish") }
        advanceUntilIdle()
        assertEquals(listOf("cancel", "close", "finish"), repository.cleanupEvents)
        assertEquals(SessionStatus.CLOSED, vm.uiState.value.sessionStatus)
    }

    @Test
    fun `saida com falha de Close nao encerra nem fabrica sessao fechada`() = runTest {
        val repository = FakeRepository()
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.open(); advanceUntilIdle()
        repository.closeError = Exception("TIMEOUT:command")
        var finished = false
        vm.exit { finished = true }; advanceUntilIdle()
        assertEquals(false, finished)
        assertEquals("TIMEOUT", vm.uiState.value.errorCode)
        assertEquals(SessionStatus.OPEN, vm.uiState.value.sessionStatus)
    }

    @Test
    fun `limpar ViewModelStore cancela e fecha recursos`() = runTest {
        val repository = FakeRepository().apply { infoGate = CompletableDeferred() }
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        val store = ViewModelStore().apply { put("diagnostico", vm) }
        vm.open(); advanceUntilIdle()
        vm.getInfo(); runCurrent()
        store.clear(); advanceUntilIdle()
        assertEquals(listOf("cancel", "close"), repository.cleanupEvents)
        assertEquals("CLOSED", repository.session)
    }

    @Test
    fun `GetInfo consulta sessao aberta sem alterar disponibilidade`() = runTest {
        val vm = DiagnosticViewModel(FakeRepository(), NoopLogger, dispatcher)
        vm.open(); advanceUntilIdle()
        vm.getInfo(); advanceUntilIdle()
        assertEquals("{}", vm.uiState.value.result)
        assertEquals(SessionStatus.OPEN, vm.uiState.value.sessionStatus)
        assertEquals(ActionStatus.SUCCESS, vm.uiState.value.actionStatus)
    }

    @Test
    fun `acoes desabilitadas e formulario invalido nao atravessam repository`() = runTest {
        val repository = FakeRepository()
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.executeCatalog(CatalogAction.RESERVED); advanceUntilIdle()
        assertEquals(ActionStatus.ERROR, vm.uiState.value.actionStatus)
        vm.open(); advanceUntilIdle()
        vm.executeCatalog(CatalogAction.QR, CatalogInput(mapOf("size" to "nao-numerico")))
        advanceUntilIdle()
        assertEquals(0, repository.catalogCalls)
        assertEquals(ActionStatus.ERROR, vm.uiState.value.actionStatus)
    }
}

private object NoopLogger : AppLogger {
    override fun event(
        level: String,
        event: String,
        operationId: String?,
        fields: Map<String, String>,
    ) = Unit
}

private class FakeRepository : DiagnosticRepository {
    var pingCalls = 0
    var catalogCalls = 0
    var closeCalls = 0
    var session = "CLOSED"
    var openError: Throwable? = null
    var pingError: Throwable? = null
    var closeError: Throwable? = null
    var infoGate: CompletableDeferred<Unit>? = null
    val canceledIds = mutableListOf<String>()
    val cleanupEvents = mutableListOf<String>()

    override suspend fun version(): String = "test"

    override suspend fun ping(config: EndpointConfig, operationId: String) {
        pingCalls += 1
        pingError?.let { throw it }
    }

    override suspend fun open(config: EndpointConfig, operationId: String) {
        openError?.let { throw it }
        session = "OPEN"
    }

    override suspend fun getInfoJson(operationId: String): String {
        infoGate?.await()
        return "{}"
    }

    override suspend fun close(operationId: String) {
        closeCalls += 1
        cleanupEvents.add("close")
        closeError?.let { throw it }
        session = "CLOSED"
    }

    override suspend fun cancel(operationId: String) {
        canceledIds.add(operationId)
        cleanupEvents.add("cancel")
    }

    override fun state(): String = session

    override suspend fun executeCatalog(
        config: EndpointConfig,
        action: CatalogAction,
        input: CatalogInput,
        operationId: String,
    ): String {
        catalogCalls += 1
        return state()
    }
}
