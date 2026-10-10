package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.math.BigDecimal
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** UI com dados sintéticos; captura e semântica não validam pinpad ou autorizador. */
class SaqueCartaoScreenTest {
    @get:Rule val compose = createComposeRule()
    private val summary = calculateSaque("150,00", SaqueModalidade.CREDITO, "1")

    @Test fun formularioEditavelSemPerfilESairRetornaAoMenu() {
        var last: SaqueEvent? = null
        var state by mutableStateOf(SaqueState(message = "Configuração de homologação incompleta."))
        compose.setContent {
            SaqueCartaoScreen(state, { event ->
                last = event
                state = when (event) {
                    is SaqueEvent.Modalidade -> state.copy(modalidade = event.value)
                    is SaqueEvent.Value -> state.copy(valueInput = event.value,
                        summary = calculateSaque(event.value, state.modalidade, "1"))
                    else -> state
                }
            })
        }
        compose.onNodeWithText("Débito").assertIsEnabled().performClick()
        compose.onNodeWithText("Débito").assertIsSelected()
        compose.onNodeWithText("Crédito").performClick()
        compose.onNodeWithText("Crédito").assertIsSelected()
        compose.onNode(hasSetTextAction() and hasText("Valor da compra")).assertIsEnabled().performTextInput("100")
        compose.onNodeWithText("R$ 105,34").assertExists()
        compose.onNodeWithText("AVANÇAR").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("SAIR").performScrollTo().assertIsEnabled().performClick()
        assertEquals(SaqueEvent.Return, last)
        capture("07-primeira-tela-sem-perfil")
    }

    @Test fun cincoEtapasSeguemReferenciaVisualETransicao() {
        var state by mutableStateOf(SaqueState(configReady = true, valueInput = "150,00", summary = summary,
            message = "Confira o resumo antes de avançar."))
        compose.setContent { SaqueCartaoScreen(state, {}) }
        compose.onNodeWithText("Resumo da transação").assertIsDisplayed()
        compose.onNodeWithText("TOTAL A PAGAR").assertIsDisplayed()
        capture("01-valores")
        for (step in 2..5) {
            compose.mainClock.autoAdvance = false
            compose.runOnIdle { state = state.copy(step = step, completedSteps = (1 until step).toSet(), busy = step < 5,
                result = if (step == 5) SaqueResultado.APPROVED else null,
                message = when (step) { 2 -> "Aguardando confirmação do cliente..."; 3 -> "Aguardando leitura do cartão...";
                    4 -> "Siga as instruções no pinpad; digite a senha se solicitada."; else -> "Homologação de teste. Não autoriza entrega de dinheiro." }) }
            compose.mainClock.advanceTimeBy(1000)
            compose.mainClock.autoAdvance = true
            compose.onNodeWithText("Etapa $step de 5").assertIsDisplayed()
            compose.onNodeWithText("VALOR DA COMPRA").assertDoesNotExist()
            if (step == 3) compose.onNodeWithText("CHIP").assertIsDisplayed()
            if (step == 5) compose.onNodeWithText("Homologação aprovada").assertIsDisplayed()
            capture("0$step-etapa")
        }
    }

    @Test fun modalidadeAvancoCancelamentoERetornoEmitemEventos() {
        var last: SaqueEvent? = null
        var state by mutableStateOf(SaqueState(configReady = true, summary = summary))
        compose.setContent { SaqueCartaoScreen(state, { last = it }) }
        compose.onNodeWithText("Débito").performClick()
        assertEquals(SaqueEvent.Modalidade(SaqueModalidade.DEBITO), last)
        compose.onNodeWithText("SAIR").performScrollTo().performClick()
        assertEquals(SaqueEvent.Return, last)
        compose.onNodeWithText("AVANÇAR").performScrollTo().performClick()
        assertEquals(SaqueEvent.Begin, last)
        compose.runOnIdle { state = state.copy(step = 4, busy = true) }
        compose.onNodeWithText("CANCELAR").performScrollTo().performClick()
        assertEquals(SaqueEvent.Cancel, last)
        compose.runOnIdle { state = state.copy(step = 5, busy = false, result = SaqueResultado.UNKNOWN) }
        compose.onNodeWithText("VOLTAR AO MENU INICIAL").performScrollTo().performClick()
        assertEquals(SaqueEvent.Return, last)
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(0)
    }

    @Test fun compactoFonteAmpliadaPreservaAcoesEResumo() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                Box(Modifier.width(420.dp).fillMaxHeight()) {
                    SaqueCartaoScreen(SaqueState(configReady = true, summary = summary,
                        message = "Confira o resumo antes de avançar."), {})
                }
            }
        }
        compose.onNodeWithText("AVANÇAR").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("R$ 158,01").assertExists()
        capture("06-compacto-fonte150")
    }

    @Test fun spinnerAnimaSemAlterarEstadoOuEmitirEventos() {
        var events = 0
        val state = SaqueState(step = 4, busy = true, message = "Aguardando processamento...")
        compose.mainClock.autoAdvance = false
        compose.setContent { SaqueCartaoScreen(state, { events++ }) }
        compose.mainClock.advanceTimeBy(100)
        val first = compose.onRoot().captureToImage().asAndroidBitmap()
        compose.mainClock.advanceTimeBy(350)
        val next = compose.onRoot().captureToImage().asAndroidBitmap()
        assertFalse("Spinner deve alterar somente a aparência", first.sameAs(next))
        compose.onNodeWithText("Aguardando processamento...").assertIsDisplayed()
        assertEquals(0, events)
        compose.mainClock.autoAdvance = true
    }

    /** Artefatos salvos em armazenamento externo privado do APK de teste. */
    private fun capture(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "layout-10004").apply { mkdirs() }
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
