package br.com.romulopenha.diagnosticopinpad.ui

import br.com.romulopenha.diagnosticopinpad.data.AppLogger
import br.com.romulopenha.diagnosticopinpad.data.DiagnosticRepository
import br.com.romulopenha.diagnosticopinpad.data.EndpointConfig
import br.com.romulopenha.diagnosticopinpad.data.CatalogAction
import br.com.romulopenha.diagnosticopinpad.data.CatalogInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
        assertEquals(DiagnosticStatus.CLOSED, viewModel.uiState.value.status)
        assertEquals("CLOSED", viewModel.uiState.value.sessionState)
        assertEquals(true, viewModel.uiState.value.bridgeReachable)
        assertEquals(1, repository.pingCalls)
    }

    @Test
    fun `configuracao invalida nao chama repository`() = runTest {
        val repository = FakeRepository()
        val viewModel = DiagnosticViewModel(repository, NoopLogger, dispatcher)

        viewModel.updatePort("0")
        viewModel.ping()
        advanceUntilIdle()

        assertEquals(DiagnosticStatus.ERROR, viewModel.uiState.value.status)
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
        assertEquals("CLOSED", viewModel.uiState.value.sessionState)
        assertEquals(DiagnosticStatus.CLOSED, viewModel.uiState.value.status)
    }

    @Test
    fun `abrir so habilita sessao quando repository confirma`() = runTest {
        val repository = FakeRepository()
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.open()
        advanceUntilIdle()
        assertEquals("OPEN", vm.uiState.value.sessionState)
        assertEquals(DiagnosticStatus.OPEN, vm.uiState.value.status)
        vm.close()
        advanceUntilIdle()
        assertEquals("CLOSED", vm.uiState.value.sessionState)
    }

    @Test
    fun `erro de abrir preserva codigo fase id e limpa resultado antigo`() = runTest {
        val repository = FakeRepository().apply { openError = IllegalStateException("SERIAL_UNAVAILABLE:serial_open") }
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.checkVersion(); advanceUntilIdle()
        vm.open(); advanceUntilIdle()
        assertEquals(DiagnosticStatus.ERROR, vm.uiState.value.status)
        assertEquals("CLOSED", vm.uiState.value.sessionState)
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
        assertEquals("CLOSED", vm.uiState.value.sessionState)
        vm.executeCatalog(CatalogAction.STATE); advanceUntilIdle()
        assertEquals("CLOSED", vm.uiState.value.sessionState)
    }

    @Test
    fun `editar endpoint libera cliente anterior antes da proxima acao`() = runTest {
        val repository = FakeRepository()
        val vm = DiagnosticViewModel(repository, NoopLogger, dispatcher)
        vm.open(); advanceUntilIdle()
        vm.updatePort("39101")
        assertEquals("CLOSED", vm.uiState.value.sessionState)
        vm.checkVersion(); advanceUntilIdle()
        assertEquals(1, repository.closeCalls)
        assertEquals("CLOSED", repository.session)
        assertEquals("CLOSED", vm.uiState.value.sessionState)
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

    override suspend fun version(): String = "test"

    override suspend fun ping(config: EndpointConfig, operationId: String) {
        pingCalls += 1
    }

    override suspend fun open(config: EndpointConfig, operationId: String) {
        openError?.let { throw it }
        session = "OPEN"
    }

    override suspend fun getInfoJson(operationId: String): String = "{}"

    override suspend fun close(operationId: String) { closeCalls += 1; session = "CLOSED" }

    override suspend fun cancel(operationId: String) = Unit

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
