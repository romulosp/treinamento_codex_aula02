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
    fun `ping publica pong e estado aberto`() = runTest {
        val repository = FakeRepository()
        val viewModel = DiagnosticViewModel(repository, NoopLogger, dispatcher)

        viewModel.ping()
        advanceUntilIdle()

        assertEquals("PONG", viewModel.uiState.value.result)
        assertEquals(DiagnosticStatus.OPEN, viewModel.uiState.value.status)
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

    override suspend fun version(): String = "test"

    override suspend fun ping(config: EndpointConfig, operationId: String) {
        pingCalls += 1
    }

    override suspend fun open(config: EndpointConfig, operationId: String) = Unit

    override suspend fun getInfoJson(operationId: String): String = "{}"

    override suspend fun close(operationId: String) = Unit

    override suspend fun cancel(operationId: String) = Unit

    override fun state(): String = "CLOSED"

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
