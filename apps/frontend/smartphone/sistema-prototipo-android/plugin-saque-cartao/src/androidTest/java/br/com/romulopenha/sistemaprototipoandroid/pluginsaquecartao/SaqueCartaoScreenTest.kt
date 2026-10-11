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
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** UI com dados sintéticos; captura e semântica não validam pinpad ou autorizador. */
class SaqueCartaoScreenTest {
    @get:Rule val compose = createComposeRule()
    private val summary = calculateSaque(15000, SaqueModalidade.CREDITO, 1)

    @Test fun preparacaoExibeEsperaESoDepoisMostraFormulario() {
        var events = 0
        var state by mutableStateOf(SaqueState(step = 0, busy = true, message = "Carregando tabela EMV..."))
        compose.setContent { SaqueCartaoScreen(state, { events++ }) }
        compose.onNodeWithText("Aguarde, processando").assertIsDisplayed()
        compose.onNodeWithText("Carregando tabela EMV...").assertIsDisplayed()
        compose.onNodeWithText("MODALIDADE").assertDoesNotExist()
        compose.onNodeWithText("AVANÇAR").assertDoesNotExist()
        compose.mainClock.autoAdvance = false
        compose.mainClock.advanceTimeBy(500)
        assertEquals(0, events)
        capture("00-preparacao-emv")
        compose.runOnIdle { state = state.copy(step = 1, busy = false, configReady = true,
            message = "Informe valores válidos para continuar.") }
        compose.mainClock.advanceTimeBy(1000)
        compose.mainClock.autoAdvance = true
        compose.onNodeWithText("Aguarde, processando").assertDoesNotExist()
        compose.onNodeWithText("MODALIDADE").assertIsDisplayed()
        compose.onNodeWithText("AVANÇAR").assertExists()
        assertEquals(0, events)
    }

    @Test fun falhaDePreparacaoMantemFormularioAusenteEPermiteSair() {
        var last: SaqueEvent? = null
        compose.setContent { SaqueCartaoScreen(SaqueState(step = 0, busy = false,
            message = "Não foi possível preparar o pinpad. Saia e tente novamente.",
            failureCode = "PINPAD_ERROR", failurePhase = "command"), { last = it }) }
        compose.onNodeWithText("Não foi possível preparar o terminal").assertIsDisplayed()
        compose.onNodeWithText("Falha: PINPAD_ERROR · fase command").assertIsDisplayed()
        compose.onNodeWithText("MODALIDADE").assertDoesNotExist()
        compose.onNodeWithText("AVANÇAR").assertDoesNotExist()
        compose.onNodeWithText("SAIR").performClick()
        assertEquals(SaqueEvent.Return, last)
    }

    @Test fun colagemInvalidaBloqueiaAvancoSemAlterarValor() {
        var state by mutableStateOf(SaqueState(configReady = true))
        compose.setContent {
            SaqueCartaoScreen(state, { event ->
                if (event is SaqueEvent.Value) {
                    val digits = normalizePurchaseInput(event.value)
                    state = state.copy(valueInput = digits, summary = runCatching {
                        calculateSaque(purchaseCents(digits), state.modalidade, 1)
                    }.getOrNull())
                }
            })
        }
        val field = compose.onNode(hasSetTextAction() and hasText("Valor da compra"))
        for (input in listOf("-10000", "1234567890123")) {
            field.performTextReplacement(input)
            compose.runOnIdle { assertEquals(input, state.valueInput); assertNull(state.summary) }
            compose.onNodeWithText("AVANÇAR").performScrollTo().assertIsNotEnabled()
        }
        field.performTextReplacement("10000")
        compose.onNodeWithText("R$ 105,34").assertExists()
        compose.onNodeWithText("AVANÇAR").performScrollTo().assertIsEnabled()
    }

    @Test fun formularioEditavelComConfiguracaoHabilitaAvancoESair() {
        var last: SaqueEvent? = null
        var state by mutableStateOf(SaqueState(configReady = true,
            message = "Confira o resumo antes de avançar."))
        compose.setContent {
            SaqueCartaoScreen(state, { event ->
                last = event
                state = when (event) {
                    is SaqueEvent.Modalidade -> state.copy(modalidade = event.value)
                    is SaqueEvent.Installments -> state.copy(installmentsInput = event.value)
                    is SaqueEvent.Value -> {
                        val digits = event.value.filter(Char::isDigit).take(12)
                        state.copy(valueInput = digits, summary = runCatching {
                            calculateSaque(purchaseCents(digits), state.modalidade, state.installmentsInput.toInt())
                        }.getOrNull())
                    }
                    else -> state
                }
            })
        }
        compose.onNodeWithText("Débito").assertIsEnabled().performClick()
        compose.onNodeWithText("Débito").assertIsSelected()
        compose.onNodeWithText("Crédito").performClick()
        compose.onNodeWithText("Crédito").assertIsSelected()
        compose.onNode(hasSetTextAction() and hasText("Valor da compra")).assertIsEnabled().performTextInput("10000")
        compose.onNode(hasSetTextAction() and hasText("100,00")).assertExists()
        compose.onNodeWithText("R$ 105,34").assertExists()
        compose.onNodeWithText("AVANÇAR").performScrollTo().assertIsEnabled()
        compose.onNodeWithText("SAIR").performScrollTo().assertIsEnabled().performClick()
        assertEquals(SaqueEvent.Return, last)
        capture("07-primeira-tela-sem-perfil")
    }

    @Test fun semConfiguracaoAvancarFicaBloqueadoEMostraCodigo() {
        compose.setContent { SaqueCartaoScreen(SaqueState(configReady = false, valueInput = "10000",
            summary = calculateSaque(10000, SaqueModalidade.CREDITO, 1),
            message = "Perfil de homologação não provisionado. Execute o launcher para provisionar perfil e tabela.",
            failureCode = "MISSING", failurePhase = "preflight"), {}) }
        compose.onNodeWithText("AVANÇAR").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Falha: MISSING · fase preflight").assertIsDisplayed()
        compose.onNodeWithText("SAIR").performScrollTo().assertIsEnabled()
    }

    @Test fun resultadoDeFalhaExibeCodigoSanitizado() {
        compose.setContent { SaqueCartaoScreen(SaqueState(step = 5, result = SaqueResultado.ERROR,
            message = "Homologação de teste. Não autoriza entrega de dinheiro.",
            failureCode = "BRIDGE_UNREACHABLE", failurePhase = "preflight"), {}) }
        compose.onNodeWithText("Erro na homologação").assertIsDisplayed()
        compose.onNodeWithText("Falha: BRIDGE_UNREACHABLE · fase preflight").assertIsDisplayed()
    }

    @Test fun comboboxSelecionaParcelasDoCreditoEDebitoFicaFixo() {
        var state by mutableStateOf(SaqueState(configReady = true, valueInput = "10000", summary = calculateSaque(10000, SaqueModalidade.CREDITO, 1),
            message = "Confira o resumo antes de avançar."))
        compose.setContent {
            SaqueCartaoScreen(state, { event ->
                state = when (event) {
                    is SaqueEvent.Installments -> state.copy(installmentsInput = event.value, feeLabel = feeLabelFor(state.modalidade, event.value.toInt()),
                        summary = runCatching { calculateSaque(purchaseCents(state.valueInput), state.modalidade, event.value.toInt()) }.getOrNull())
                    is SaqueEvent.Modalidade -> state.copy(modalidade = event.value,
                        installmentsInput = if (event.value == SaqueModalidade.DEBITO) "1" else state.installmentsInput,
                        feeLabel = feeLabelFor(event.value, 1),
                        summary = runCatching { calculateSaque(purchaseCents(state.valueInput), event.value, 1) }.getOrNull())
                    else -> state
                }
            })
        }
        compose.onNodeWithText("Parcelas (1 a 12)").performScrollTo().performClick()
        compose.onNodeWithText("3x").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("3", state.installmentsInput) }
        compose.onNodeWithText("6,94%").assertExists()
        compose.onNodeWithText("R$ 106,94").assertExists()
        compose.onNodeWithText("Débito").performClick()
        compose.onNodeWithText("Parcelas (débito)").performScrollTo().assertExists()
        compose.onNodeWithText("1x (À vista)").assertExists()
        compose.onAllNodesWithText("R$ 1,00").assertCountEquals(2)
        compose.onNodeWithText("R$ 101,00").assertExists()
        compose.onNodeWithText("1x (À vista)").performClick()
        compose.onNodeWithText("12x").assertDoesNotExist()
    }

    @Test fun cincoEtapasSeguemReferenciaVisualETransicao() {
        var state by mutableStateOf(SaqueState(configReady = true, valueInput = "15000", summary = summary,
            message = "Confira o resumo antes de avançar."))
        compose.setContent { SaqueCartaoScreen(state, {}) }
        compose.onNodeWithText("Pagamento com cartão").assertIsDisplayed()
        compose.onNodeWithText("Resumo da transação").assertIsDisplayed()
        compose.onNodeWithText("TOTAL A PAGAR").assertIsDisplayed()
        assertSemIndicadorDeEtapas()
        capture("01-valores")
        for (step in 2..5) {
            compose.mainClock.autoAdvance = false
            compose.runOnIdle { state = state.copy(step = step, busy = step < 5,
                result = if (step == 5) SaqueResultado.APPROVED else null,
                message = when (step) { 2 -> "Aguardando confirmação do cliente..."; 3 -> "Aguardando leitura do cartão...";
                    4 -> "Siga as instruções no pinpad; digite a senha se solicitada."; else -> "Homologação de teste. Não autoriza entrega de dinheiro." }) }
            compose.mainClock.advanceTimeBy(1000)
            compose.mainClock.autoAdvance = true
            assertSemIndicadorDeEtapas()
            compose.onNodeWithText("VALOR DA COMPRA").assertDoesNotExist()
            if (step == 3) compose.onNodeWithText("CHIP").assertIsDisplayed()
            if (step == 5) compose.onNodeWithText("Homologação aprovada").assertIsDisplayed()
            capture("0$step-etapa")
        }
    }

    @Test fun primeiraTelaNaoExigeRolagemParaAsAcoesEssenciais() {
        compose.setContent { SaqueCartaoScreen(SaqueState(configReady = true, valueInput = "15000", summary = summary,
            message = "Confira o resumo antes de avançar."), {}) }
        val root = compose.onRoot().getUnclippedBoundsInRoot()
        val avancar = compose.onNodeWithText("AVANÇAR").getUnclippedBoundsInRoot()
        val sair = compose.onNodeWithText("SAIR").getUnclippedBoundsInRoot()
        assertTrue("AVANÇAR fora da janela: $avancar em $root", avancar.bottom <= root.bottom)
        assertTrue("SAIR fora da janela: $sair em $root", sair.bottom <= root.bottom)
        compose.onNodeWithText("AVANÇAR").assertIsDisplayed()
        compose.onNodeWithText("SAIR").assertIsDisplayed()
    }

    /** O indicador de etapas foi removido por REV-009 em todas as telas. */
    private fun assertSemIndicadorDeEtapas() {
        for (step in 1..5) compose.onNodeWithText("Etapa $step de 5").assertDoesNotExist()
        listOf("Valores", "Confirmação", "Leitura", "Senha", "Fim").forEach {
            compose.onNodeWithText(it).assertDoesNotExist()
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
                    SaqueCartaoScreen(SaqueState(configReady = true, valueInput = "15000", summary = summary,
                        message = "Confira o resumo antes de avançar."), {})
                }
            }
        }
        compose.onNodeWithText("AVANÇAR").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("R$ 158,01").assertExists()
        capture("06-compacto-fonte150")
    }

    @Test fun grupoDeAcoesEntraComDeslocamentoEscalaEFade() {
        compose.mainClock.autoAdvance = false
        compose.setContent { SaqueCartaoScreen(SaqueState(configReady = true, valueInput = "15000", summary = summary,
            message = "Confira o resumo antes de avançar."), {}) }
        compose.mainClock.advanceTimeBy(40)
        val start = compose.onRoot().captureToImage().asAndroidBitmap()
        compose.mainClock.advanceTimeBy(600)
        val end = compose.onRoot().captureToImage().asAndroidBitmap()
        assertFalse("Ações devem entrar com movimento percebível", start.sameAs(end))
        compose.mainClock.autoAdvance = true
        compose.onNodeWithText("AVANÇAR").performScrollTo().assertIsEnabled()
        compose.onNodeWithText("SAIR").performScrollTo().assertIsEnabled()
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
