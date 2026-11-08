package br.com.romulopenha.sistemaprototipoandroid.pluginingressos

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onAncestors
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalResult
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Exercita filme com programação fixa, apresentação e pagamento inteiramente sintético. */
class CinemaScreenTest {
    @get:Rule val compose = createComposeRule()

    /** Pagamento aprovado mostra sucesso e reinicia sem perder a venda nem disparar outra chamada. */
    @Test fun selecionaFilmeEEmiteIngressoSemFormulario() {
        var calls = 0
        compose.setContent {
            CinemaRoute(CinemaJourney(), modifier = Modifier.testTag("cinema-preview")) { request, callback ->
                calls++
                assertEquals(4400L, request.amountCents)
                callback(CardWithdrawalResult(CardWithdrawalStatus.APPROVED, true))
            }
        }
        compose.onNodeWithContentDescription("Cartaz: Batman").performClick()
        compose.onNodeWithText("Configurar sessão").assertDoesNotExist()
        compose.onNodeWithText("Ingresso: R$ 44,00").assertIsDisplayed()
        compose.onNodeWithContentDescription("Assento A1").reveal().performClick()
        compose.onNodeWithText("Criar pedido").reveal().performClick()
        compose.onNodeWithText("ABRIR PAGAMENTO NO TERMINAL POS").reveal().performClick()
        assertEquals(1, calls)
        compose.onNodeWithText("Transação Realizada com Sucesso").assertIsDisplayed()
        savePreview("ingressos-sucesso-fluxo.png")
        compose.onNodeWithText("Emitir ingresso demonstrativo").assertDoesNotExist()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Selecione um evento para começar.").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Transação Realizada com Sucesso").assertDoesNotExist()
        savePreview("ingressos-novo-atendimento.png")
        compose.onNodeWithContentDescription("Cartaz: Batman").performClick()
        compose.onNodeWithContentDescription("Assento A1").reveal().assertIsNotEnabled()
        compose.onNodeWithContentDescription("Assento A3").reveal().performClick()
        compose.onNodeWithText("Criar pedido").reveal().assertIsEnabled()
        assertEquals(1, calls)
    }

    /** Verifica seleção, ocupados configurados, vendido local e callback por semântica acessível. */
    @Test fun mapaPreservaEstadosESelecaoUnica() {
        var selectedSeat: String? = null
        showScreen(previewState.copy(selectedSeats = setOf("A1"), sold = setOf("B1")), 1000.dp) { selectedSeat = it }
        compose.onNodeWithContentDescription("Assento A1").reveal().assertIsSelected()
        compose.onNodeWithContentDescription("Assento A4").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Assento B1").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Assento B2").reveal().assertIsEnabled().performClick()
        assertEquals("B2", selectedSeat)
    }

    /** Pedido bloqueia mapa e troca de filme, mantendo somente ações financeiras explícitas. */
    @Test fun pedidoPendenteBloqueiaMapaETrocaDeFilme() {
        showScreen(previewState.copy(order = TicketOrder("catalog-test", "cinema-demo-batman-1930", setOf("A1"), emptyMap(), listOf(TicketLine("A1", "A1", 1, 4400)), 4400L)), 1000.dp)
        compose.onNodeWithContentDescription("Assento A1").reveal().assertIsSelected().assertIsNotEnabled()
        compose.onNodeWithContentDescription("Assento B2").assertIsNotEnabled()
        compose.onNodeWithText("ABRIR PAGAMENTO NO TERMINAL POS").reveal().assertIsEnabled()
        compose.onNodeWithContentDescription("Cartaz: Avatar 3").assertIsNotEnabled()
        savePreview("ingressos-catalogo-amplo.png")
    }

    /** Largura interna compacta mantém acesso aos lugares finais e à ação do pedido. */
    @Test fun layoutCompactoPermiteRolarAtePedido() {
        showScreen(previewState.copy(selectedSeats = setOf("B3")), 380.dp)
        savePreview("ingressos-catalogo-compacto.png")
        compose.onNodeWithContentDescription("Assento B3").reveal().assertIsSelected()
        compose.onNodeWithContentDescription("Assento B6").reveal().assertIsDisplayed()
        compose.onNodeWithText("Criar pedido").reveal().assertIsEnabled()
        savePreview("ingressos-catalogo-compacto-pedido.png")
    }

    /** A partir de foco conhecido, TAB e ENTER selecionam o próximo filme pelo controle nativo. */
    @Test fun tecladoSelecionaFilmeComProgramacaoFixa() {
        lateinit var inputMode: InputModeManager
        compose.setContent {
            inputMode = LocalInputModeManager.current
            CinemaRoute(CinemaJourney()) { _, _ -> error("Pagamento não solicitado") }
        }
        compose.runOnIdle { inputMode.requestInputMode(InputMode.Keyboard) }
        compose.onNodeWithContentDescription("Cartaz: Batman").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        compose.onNodeWithContentDescription("Cartaz: Batman").assertIsFocused()
        compose.onRoot().performKeyInput { pressKey(Key.Tab) }
        compose.onNodeWithContentDescription("Cartaz: Avatar 3").assertIsFocused()
        compose.onRoot().performKeyInput { pressKey(Key.Enter) }
        compose.onNodeWithContentDescription("Cartaz: Avatar 3").assertIsSelected()
        compose.onNodeWithText("Ingresso: R$ 48,00").assertIsDisplayed()
    }

    /** Título configurado longo e fonte 150% não impedem acesso à ação do pedido. */
    @Test fun tituloLongoNaoImpedeCriarPedido() {
        val title = "Avatar 3: Uma viagem extraordinária através das estrelas e do tempo"
        val catalog = DemoCinemaCatalog.catalog.copy(movies = listOf(DemoCinemaCatalog.catalog.movies.first().copy(title = title)))
        showScreen(previewState.copy(session = previewState.session!!.copy(film = title), selectedSeats = setOf("A6")),
            380.dp, fontScale = 1.5f, catalog = catalog)
        compose.onNodeWithText("Criar pedido").reveal().assertIsEnabled()
        savePreview("ingressos-catalogo-fonte-ampliada.png")
    }

    /** Trocar filme substitui automaticamente sala, horário, preço e ocupação, sem controles de horário. */
    @Test fun carrosselCarregaDadosDoCinemaSemEscolhaDeHorario() {
        compose.setContent { CinemaRoute(CinemaJourney()) { _, _ -> error("Pagamento não solicitado") } }
        compose.onNodeWithContentDescription("Cartaz: Avatar 3").performClick()
        compose.onAllNodesWithText("17:15 - SALA 02")[0].assertIsDisplayed().assertHasNoClickAction()
        compose.onNodeWithText("Ingresso: R$ 48,00").assertIsDisplayed()
        compose.onNodeWithContentDescription("Assento A2").reveal().assertIsNotEnabled()
        listOf("Configurar sessão", "Cadastrar sessão", "Preço em reais", "Horário").forEach {
            compose.onNodeWithText(it).assertDoesNotExist()
        }
        compose.onNodeWithContentDescription("Cartazes anteriores").performClick()
        compose.onNodeWithContentDescription("Cartaz: Batman").performClick()
        compose.onNodeWithText("Ingresso: R$ 44,00").assertIsDisplayed()
        compose.onNodeWithContentDescription("Assento A2").assertIsEnabled()
        compose.onNodeWithText("Cinema", useUnmergedTree = true).performClick()
        compose.onNodeWithContentDescription("Próximos cartazes").performClick()
        compose.onNodeWithContentDescription("Cartaz: Spider-Man").reveal().assertIsDisplayed()
    }

    /** Ocupados da configuração não aparecem como ingressos emitidos neste terminal. */
    @Test fun ocupacaoConfiguradaNaoCriaRecibosLocais() {
        showScreen(previewState, 1000.dp)
        compose.onNodeWithContentDescription("Assento A4").reveal().assertIsNotEnabled()
        compose.onNodeWithText("SIMULAÇÃO — SEM VALIDADE", substring = true).assertDoesNotExist()
    }

    /** Mede posições intermediárias e finais para comprovar movimento, em vez de salto imediato. */
    @Test fun setasDoCarrosselAnimamDeslocamento() {
        showScreen(previewState, 1000.dp)
        val card = compose.onNodeWithContentDescription("Cartaz: Avatar 3")
        val before = card.getUnclippedBoundsInRoot().left
        compose.mainClock.autoAdvance = false
        try {
            compose.onNodeWithContentDescription("Próximos cartazes").performClick()
            compose.mainClock.advanceTimeBy(64)
            val during = card.getUnclippedBoundsInRoot().left
            compose.mainClock.advanceTimeBy(1_500)
            val after = card.getUnclippedBoundsInRoot().left
            assertTrue("O cartaz deve se mover ao longo dos frames", during < before)
            assertTrue("A posição intermediária deve anteceder o destino", during > after)
            InstrumentationRegistry.getInstrumentation().targetContext.cacheDir.resolve("carrossel-movimento.json")
                .writeText("""{"before_dp":${before.value},"during_64ms_dp":${during.value},"after_dp":${after.value}}""")
        } finally {
            compose.mainClock.autoAdvance = true
        }
    }

    /** Captura confirmação sem dados pessoais ou pedido do cliente anterior. */
    @Test fun telaDeSucessoMostraTextoExatoSemResumoAnterior() {
        showScreen(previewState.copy(completedOrderId = "success-preview"), 1000.dp)
        compose.onNodeWithText("Transação Realizada com Sucesso").assertIsDisplayed()
        compose.onNodeWithText("Resumo da compra").assertDoesNotExist()
        savePreview("ingressos-sucesso.png")
    }

    /** Ampliação permite chegar à última fileira, selecionar pares e conservar a seleção ao fechar. */
    @Test fun mapaAmpliadoSelecionaCasaisComNumeracaoAteL15() {
        compose.setContent { CinemaRoute(CinemaJourney(), modifier = Modifier.testTag("cinema-preview")) { _, _ -> error("Sem pagamento") } }
        compose.onNodeWithContentDescription("Cartaz: Batman").performClick()
        compose.onNodeWithText("Ampliar mapa").reveal().performClick()
        compose.onNodeWithText("Mapa de lugares").assertIsDisplayed()
        compose.onNodeWithContentDescription("Assento L1").reveal().performClick()
        compose.onNodeWithContentDescription("Assento L2").assertIsSelected()
        compose.onNodeWithContentDescription("Assento L15").reveal().performClick().assertIsSelected()
        compose.onNodeWithContentDescription("Assento L14").assertIsSelected()
        compose.onNodeWithText("4 ingressos · R$ 176,00").assertIsDisplayed()
        val mapBitmap = compose.onNodeWithText("Mapa de lugares").onParent().onParent().captureToImage().asAndroidBitmap()
        InstrumentationRegistry.getInstrumentation().targetContext.cacheDir.resolve("eventos-mapa-ampliado.png").outputStream().use {
            mapBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText("Fechar mapa").performClick()
        compose.onNodeWithText("Mapa de lugares").assertDoesNotExist()
        compose.onNodeWithText("Criar pedido").reveal().performClick()
        compose.onNodeWithText("R$ 176,00").reveal().assertIsDisplayed()
        savePreview("eventos-casais-pedido.png")
    }

    /** Filtro de shows e pedido misto usam um único pagamento pelo total dos ingressos. */
    @Test fun showCombinaAssentoEPistasComPagamentoUnico() {
        var calls = 0
        compose.setContent {
            CinemaRoute(CinemaJourney(), modifier = Modifier.testTag("cinema-preview")) { request, callback ->
                calls++
                assertEquals(31000L, request.amountCents)
                callback(CardWithdrawalResult(CardWithdrawalStatus.UNAVAILABLE, true))
            }
        }
        compose.onNodeWithText("Shows").performClick()
        compose.onNodeWithContentDescription("Cartaz: Batman").assertDoesNotExist()
        compose.onNodeWithContentDescription("Cartaz: Festival de música").performClick()
        compose.onNodeWithText("PALCO").assertExists()
        compose.onNodeWithContentDescription("Assento A1").reveal().performClick()
        repeat(2) { compose.onNodeWithContentDescription("Adicionar Pista").reveal().performClick() }
        compose.onNodeWithContentDescription("Adicionar Pista VIP").reveal().performClick()
        compose.onNodeWithText("Criar pedido").reveal().performClick()
        compose.onNodeWithText("R$ 310,00").reveal().assertIsDisplayed()
        val amountBounds = compose.onNodeWithText("R$ 310,00").getUnclippedBoundsInRoot()
        val screenBounds = compose.onNodeWithTag("cinema-preview").getUnclippedBoundsInRoot()
        assertTrue("O valor total deve caber no resumo", amountBounds.right <= screenBounds.right)
        savePreview("eventos-show-misto.png")
        compose.onNodeWithText("ABRIR PAGAMENTO NO TERMINAL POS").reveal().performClick()
        assertEquals(1, calls)
        compose.onNodeWithContentDescription("Adicionar Pista").reveal().assertIsNotEnabled()
    }

    /** Espaços especiais expõem tipo e vínculo por semântica, sem depender somente do símbolo. */
    @Test fun mapaIdentificaPcdEAcompanhantesSemObrigarCompraConjunta() {
        compose.setContent { CinemaRoute(CinemaJourney()) { _, _ -> error("Sem pagamento") } }
        compose.onNodeWithContentDescription("Cartaz: Batman").performClick()
        compose.onNodeWithContentDescription("Assento A1").reveal().performClick().assertIsSelected()
        compose.onNodeWithContentDescription("Assento A2").assertIsNotSelected()
        compose.onNodeWithContentDescription("Assento A1").assert(
            androidx.compose.ui.test.SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription,
                "PCD, vinculado a A2, Selecionado"))
    }

    /** O cabeçalho do host reduz a altura, mas total/ação não saem da área visível em pedido grande. */
    @Test fun resumoFicaNoTopoComAcaoVisivelAoRolarMapaEPedidoLongo() {
        val journey = CinemaJourney().apply {
            selectMovie("batman")
            ('C'..'D').forEach { row -> (1..15).forEach { select("$row$it") } }
            createOrder()
        }
        showScreen(journey.state, 1100.dp, height = 540.dp)
        compose.onNodeWithText("Resumo da compra").assertIsDisplayed()
        val title = compose.onNodeWithText("Resumo da compra").getUnclippedBoundsInRoot()
        assertTrue("Resumo deve começar no topo", title.top < 40.dp)
        val pay = compose.onNodeWithText("ABRIR PAGAMENTO NO TERMINAL POS")
        pay.assertIsDisplayed().assertIsEnabled()
        compose.onNodeWithText("R$ 1320,00").assertIsDisplayed()
        val before = pay.getUnclippedBoundsInRoot()
        assertTrue("Pagamento deve caber sem rolar o painel", before.bottom <= 540.dp)
        compose.onNodeWithContentDescription("Assento L15").reveal().assertIsDisplayed()
        assertEquals(before, pay.getUnclippedBoundsInRoot())
        compose.onNodeWithText("D15 · Comum · 1 × R$ 44,00 = R$ 44,00").reveal().assertIsDisplayed()
        assertEquals(before, pay.getUnclippedBoundsInRoot())
        compose.onNodeWithText("R$ 1320,00").assertIsDisplayed()
        savePreview("resumo-tablet-altura-limitada.png")
    }

    /** Cartazes menores após seleção deixam espaço para trabalhar no mapa sem perder catálogo. */
    @Test fun selecionarEventoCompactaCarrosselEPreservaResumoVisivel() {
        compose.setContent {
            Box(Modifier.width(1100.dp).height(540.dp)) {
                CinemaRoute(CinemaJourney(), modifier = Modifier.testTag("cinema-preview")) { _, _ -> error("Sem pagamento") }
            }
        }
        val poster = compose.onNodeWithContentDescription("Cartaz: Batman")
        val initial = poster.getUnclippedBoundsInRoot().let { it.bottom - it.top }
        poster.performClick()
        assertTrue(poster.getUnclippedBoundsInRoot().let { it.bottom - it.top } < initial)
        compose.onNodeWithContentDescription("Cartaz: Avatar 3").assertExists()
        compose.onNodeWithContentDescription("Assento A1").reveal().performClick()
        compose.onNodeWithText("Criar pedido").assertIsDisplayed().assertIsEnabled()
        compose.onNodeWithText("R$ 44,00").assertIsDisplayed()
        savePreview("resumo-tablet-selecao.png")
    }

    /** Revela conteúdo nos eixos de ancestrais distintos; ações fixas não precisam rolar. */
    private fun SemanticsNodeInteraction.reveal(): SemanticsNodeInteraction = apply {
        val density = InstrumentationRegistry.getInstrumentation().targetContext.resources.displayMetrics.density
        val ancestors = onAncestors().filter(hasScrollAction()).fetchSemanticsNodes().map { it.id }.reversed()
        ancestors.forEach { id ->
            val parent = compose.onNode(SemanticsMatcher("Ancestral rolável $id") { it.id == id })
            val viewport = parent.getUnclippedBoundsInRoot()
            val target = getUnclippedBoundsInRoot()
            val config = parent.fetchSemanticsNode().config
            val horizontal = config.contains(androidx.compose.ui.semantics.SemanticsProperties.HorizontalScrollAxisRange)
            val vertical = config.contains(androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange)
            val dx = if (!horizontal) 0.dp else when {
                target.left < viewport.left -> target.left - viewport.left
                target.right > viewport.right -> target.right - viewport.right
                else -> 0.dp
            }
            val dy = if (!vertical) 0.dp else when {
                target.top < viewport.top -> target.top - viewport.top
                target.bottom > viewport.bottom -> target.bottom - viewport.bottom
                else -> 0.dp
            }
            parent.performSemanticsAction(SemanticsActions.ScrollBy) { it(dx.value * density, dy.value * density) }
        }
    }

    /** Renderiza estado controlado e catálogo local sem host ou hardware. */
    private fun showScreen(
        state: CinemaState, width: Dp, fontScale: Float = 1f,
        catalog: CinemaCatalog = DemoCinemaCatalog.catalog, height: Dp? = null, onSelect: (String) -> Unit = {},
    ) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                Box(Modifier.width(width).then(if (height == null) Modifier.fillMaxHeight() else Modifier.height(height))) {
                    CinemaScreen(state, catalog, {}, onSelect, {}, {}, {}, Modifier.testTag("cinema-preview"))
                }
            }
        }
    }

    /** Salva a superfície sintética para inspeção de composição, fonte e dados pré-configurados. */
    private fun savePreview(name: String) {
        val bitmap = compose.onNodeWithTag("cinema-preview").captureToImage().asAndroidBitmap()
        InstrumentationRegistry.getInstrumentation().targetContext.cacheDir.resolve(name).outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private val previewState: CinemaState
        get() = CinemaJourney().apply { selectMovie("batman") }.state
}
