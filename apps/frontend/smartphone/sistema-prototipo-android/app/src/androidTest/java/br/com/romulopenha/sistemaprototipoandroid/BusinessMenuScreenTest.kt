package br.com.romulopenha.sistemaprototipoandroid

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalInputModeManager
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Valida o menor contrato visual da tela inicial desta fase. */
class BusinessMenuScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun mostraEstadoVazioEEncaminhaLogout() {
        var logoutCount = 0
        composeRule.setContent {
            BusinessMenuScreen(
                items = emptyList(),
                fatalMessage = null,
                logoutInProgress = false,
                onItemSelected = {},
                onLogout = { logoutCount++ },
                onFatalAcknowledged = {},
            )
        }

        composeRule.onNodeWithText("Nenhum plugin de negócio carregado.").assertIsDisplayed()
        composeRule.onNodeWithText("SAIR").performClick()
        composeRule.runOnIdle { assertEquals(1, logoutCount) }
    }

    @Test
    fun folhaDoMenuEncaminhaSelecaoDoOperador() {
        var selected: String? = null
        composeRule.setContent {
            BusinessMenuScreen(
                items = listOf(br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem("pagamento", "Pagamento com Cartão", "pagamento", 1)),
                fatalMessage = null,
                logoutInProgress = false,
                onItemSelected = { selected = it },
                onLogout = {},
                onFatalAcknowledged = {},
            )
        }

        composeRule.onNodeWithText("Pagamento com Cartão").performClick()
        composeRule.runOnIdle { assertEquals("pagamento", selected) }
    }

    @Test
    fun logoutEmAndamentoPreservaEstadoDesabilitado() {
        var logoutCount = 0
        composeRule.setContent {
            BusinessMenuScreen(emptyList(), null, true, {}, { logoutCount++ }, {})
        }
        composeRule.onNodeWithText("Saindo...").assertIsNotEnabled()
        composeRule.onNodeWithText("Saindo...").performClick()
        composeRule.runOnIdle { assertEquals(0, logoutCount) }
    }

    @Test
    fun erroFatalPreservaMensagemEReconhecimento() {
        var count = 0
        composeRule.setContent {
            BusinessMenuScreen(emptyList(), "Falha controlada", false, {}, {}, { count++ })
        }
        composeRule.onNodeWithText("Erro crítico no menu").assertIsDisplayed()
        composeRule.onNodeWithText("Falha controlada").assertIsDisplayed()
        composeRule.onNodeWithText("Fechar").performClick()
        composeRule.runOnIdle { assertEquals(1, count) }
    }

    @Test
    fun agrupadoresAninhadosPreservamOrdemSemAcaoESelecionamFolha() {
        var selected: String? = null
        composeRule.setContent {
            BusinessMenuScreen(listOf(
                BusinessMenuItem("grupo", "Grupo principal", "grupo", 0, listOf(
                    BusinessMenuItem("sub", "Subgrupo", "sub", 0, listOf(
                        BusinessMenuItem("b", "Segunda", "b", 1),
                        BusinessMenuItem("a", "Primeira", "a", 1),
                    )),
                )),
            ), null, false, { selected = it }, {}, {})
        }
        composeRule.onNodeWithText("Grupo principal").assertHasNoClickAction()
        composeRule.onNodeWithText("Subgrupo").assertHasNoClickAction()
        val first = composeRule.onNodeWithText("Primeira").fetchSemanticsNode().boundsInRoot
        val second = composeRule.onNodeWithText("Segunda").fetchSemanticsNode().boundsInRoot
        assertTrue(first.top < second.top || (first.top == second.top && first.left < second.left))
        composeRule.onNodeWithText("Primeira").performClick()
        composeRule.runOnIdle { assertEquals("a", selected) }
    }

    @Test
    fun tabEnterEEspacoAtivamUmaVezNaOrdemVisual() {
        val selected = mutableListOf<String>()
        lateinit var inputModeManager: InputModeManager
        composeRule.setContent {
            inputModeManager = LocalInputModeManager.current
            BusinessMenuScreen(listOf(
                BusinessMenuItem("b", "Segunda", "b", 1),
                BusinessMenuItem("a", "Primeira", "a", 1),
            ), null, false, { selected += it }, {}, {})
        }
        composeRule.runOnIdle { inputModeManager.requestInputMode(InputMode.Keyboard) }
        composeRule.onNodeWithText("Primeira").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        composeRule.onNodeWithText("Primeira").assertIsFocused()
        composeRule.onNodeWithText("Primeira").performKeyInput { pressKey(Key.Enter); pressKey(Key.Tab) }
        composeRule.onNodeWithText("Segunda").assertIsFocused()
        composeRule.onNodeWithText("Segunda").performKeyInput { pressKey(Key.Spacebar) }
        composeRule.runOnIdle { assertEquals(listOf("a", "b"), selected) }
    }
}
