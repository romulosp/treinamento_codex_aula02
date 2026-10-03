package br.com.romulopenha.diagnosticopinpad.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.romulopenha.diagnosticopinpad.MainActivity
import br.com.romulopenha.diagnosticopinpad.data.AppLogger
import br.com.romulopenha.diagnosticopinpad.data.CatalogAction
import br.com.romulopenha.diagnosticopinpad.data.CatalogInput
import br.com.romulopenha.diagnosticopinpad.data.DiagnosticRepository
import br.com.romulopenha.diagnosticopinpad.data.EndpointConfig
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Exercita lifecycle real com I/O controlado, sem acessar COM ou alterar DI de produção. */
@RunWith(AndroidJUnit4::class)
class DiagnosticLifecycleTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun recreationKeepsPendingOperationAndCancelButtonReachesRepository() {
        val (vm, repository) = pendingOperation()
        val operationId = vm.uiState.value.operationId
        compose.activityRule.scenario.recreate()
        compose.activityRule.scenario.onActivity { activity ->
            assertSame(vm, ViewModelProvider(activity)[DiagnosticViewModel::class.java])
        }
        assertEquals(operationId, vm.uiState.value.operationId)
        assertEquals(1, repository.infoCalls.get())
        compose.onNodeWithText("Cancelar").performScrollTo().performClick()
        compose.waitUntil(10000) { vm.uiState.value.errorCode == "CANCELED" }
        assertEquals(1, repository.cancelCalls.get())
        compose.onNodeWithText("Fechar").performScrollTo().performClick()
        compose.waitUntil(10000) { vm.uiState.value.sessionStatus == SessionStatus.CLOSED }
        assertEquals(1, repository.closeCalls.get())
    }

    @Test fun exitDuringOperationCancelsAndClosesBeforeFinishingActivity() {
        val (vm, repository) = pendingOperation()
        compose.activityRule.scenario.recreate()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("28. Sair"))
        compose.onNodeWithText("28. Sair").performClick()
        compose.waitUntil(10000) { compose.activityRule.scenario.state == Lifecycle.State.DESTROYED }
        assertEquals(1, repository.cancelCalls.get())
        assertEquals(SessionStatus.CLOSED, vm.uiState.value.sessionStatus)
        assertEquals("CLOSED", repository.session)
    }

    private fun pendingOperation(): Pair<DiagnosticViewModel, PendingRepository> {
        val repository = PendingRepository()
        lateinit var vm: DiagnosticViewModel
        compose.activityRule.scenario.onActivity { activity ->
            vm = DiagnosticViewModel(repository, SilentLogger)
            val key = "androidx.lifecycle.ViewModelProvider.DefaultKey:${DiagnosticViewModel::class.java.canonicalName}"
            activity.viewModelStore.put(key, vm)
            vm.open()
        }
        compose.waitUntil(10000) { vm.uiState.value.sessionStatus == SessionStatus.OPEN }
        compose.activityRule.scenario.onActivity { vm.getInfo() }
        compose.waitUntil(10000) { repository.infoCalls.get() == 1 }
        return vm to repository
    }
}

private class PendingRepository : DiagnosticRepository {
    @Volatile var session = "CLOSED"
    val infoCalls = AtomicInteger()
    val cancelCalls = AtomicInteger()
    val closeCalls = AtomicInteger()
    private val pending = CompletableDeferred<Unit>()

    override suspend fun version(): String = "lifecycle-test"
    override suspend fun ping(config: EndpointConfig, operationId: String) = Unit
    override suspend fun open(config: EndpointConfig, operationId: String) { session = "OPEN" }
    override suspend fun getInfoJson(operationId: String): String {
        infoCalls.incrementAndGet()
        pending.await()
        return "{}"
    }
    override suspend fun close(operationId: String) { closeCalls.incrementAndGet(); session = "CLOSED" }
    override suspend fun cancel(operationId: String) { cancelCalls.incrementAndGet() }
    override fun state(): String = session
    override suspend fun executeCatalog(config: EndpointConfig, action: CatalogAction, input: CatalogInput, operationId: String): String = ""
}

private object SilentLogger : AppLogger {
    override fun event(level: String, event: String, operationId: String?, fields: Map<String, String>) = Unit
}
