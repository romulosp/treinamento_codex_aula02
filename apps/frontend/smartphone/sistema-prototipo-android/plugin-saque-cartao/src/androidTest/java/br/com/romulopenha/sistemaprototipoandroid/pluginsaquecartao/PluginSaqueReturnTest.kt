package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import androidx.compose.material3.Text
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.viewinterop.AndroidView
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardPaymentOptions
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardPaymentRequest
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardPaymentResultPresentation
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalResult
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalStatus
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Exercita fábrica, attach e owner reais do componente, com repository exclusivamente sintético. */
class PluginSaqueReturnTest {
    @get:Rule val compose = createComposeRule()

    @Test fun apresentacaoNoChamadorRetornaCapturedSemVoltarDepoisDeClose() {
        val fixture = render(returnToCaller = true)
        compose.onNodeWithText("AVANÇAR").performScrollTo().performClick()
        compose.onNodeWithText("Consumidor recebeu CAPTURED").assertIsDisplayed()
        compose.onNodeWithText("VOLTAR").assertDoesNotExist()
        compose.runOnIdle {
            assertEquals(CardWithdrawalStatus.CAPTURED, fixture.result.value!!.status)
            assertTrue(fixture.result.value!!.cleanupConfirmed)
            assertEquals(1, fixture.repository.closes)
        }
    }

    @Test fun contratoAnteriorMantemTelaTerminalEVOLTAR() {
        val fixture = render(returnToCaller = false)
        compose.onNodeWithText("AVANÇAR").performScrollTo().performClick()
        compose.onNodeWithText("Captura de homologação concluída").assertIsDisplayed()
        compose.runOnIdle { assertNull(fixture.result.value) }
        compose.onNodeWithText("VOLTAR").performScrollTo().performClick()
        compose.onNodeWithText("Consumidor recebeu CAPTURED").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, fixture.repository.closes) }
    }

    /** Usa a mesma fábrica de sessão de produção; somente a porta de I/O é substituída. */
    private fun render(returnToCaller: Boolean): Fixture {
        val repository = ScreenSaqueRepository()
        val component = PluginSaqueCartaoApp { repository }
        val result = mutableStateOf<CardWithdrawalResult?>(null)
        compose.setContent {
            val terminal = result.value
            if (terminal != null) Text("Consumidor recebeu ${terminal.status.name}")
            else AndroidView(factory = { context ->
                val request = CardPaymentRequest("synthetic-view", 4800)
                val completion: (CardWithdrawalResult) -> Unit = { result.value = it }
                if (returnToCaller) component.createPayment(context, request,
                    CardPaymentOptions(CardPaymentResultPresentation.CALLER), completion).view
                else component.createPayment(context, request, completion).view
            })
        }
        return Fixture(repository, result)
    }

    private data class Fixture(val repository: ScreenSaqueRepository, val result: State<CardWithdrawalResult?>)
}

/** Fonte fictícia sem arquivos, socket, driver, PAN, PIN ou ABECS; captura local tipada 00. */
private class ScreenSaqueRepository : SaqueRepository {
    var closes = 0
    override suspend fun loadProfile() = SaquePerfil("DES", "localhost", 39100, 5000, 5,
        "01", "TEST000001", "01", "02", 100000, "30", 7, SaqueCenario.APPROVE)
    override suspend fun open(profile: SaquePerfil, operationId: String) = Unit
    override suspend fun displayCapabilities(operationId: String) = SaqueDisplay(4, 32)
    override suspend fun loadTable(profile: SaquePerfil, operationId: String) = Unit
    override suspend fun displayText(operationId: String, message: String) = Unit
    override suspend fun displayPage(operationId: String, first: String, second: String) = Unit
    override suspend fun waitKey(operationId: String, timeoutSeconds: Int) = 0
    override suspend fun capture(profile: SaquePerfil, summary: SaqueResumo, operationId: String) = SaqueModoCartao.fromCardType("00")
    override suspend fun capturePin(operationId: String) = Unit
    override suspend fun tracks(profile: SaquePerfil, operationId: String) = Unit
    override suspend fun continueEmv(profile: SaquePerfil, summary: SaqueResumo, operationId: String) = '0'
    override suspend fun finalizeEmv(profile: SaquePerfil, operationId: String) = '0'
    override suspend fun cancel(operationId: String) = Unit
    override suspend fun close(operationId: String) { closes++ }
}
