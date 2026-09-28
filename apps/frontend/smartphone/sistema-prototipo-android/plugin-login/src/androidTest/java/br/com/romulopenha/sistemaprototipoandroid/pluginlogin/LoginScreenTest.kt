package br.com.romulopenha.sistemaprototipoandroid.pluginlogin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

/** Valida a semântica observável da tela sem criar o grafo do host. */
class LoginScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun primeiroCaractereInvalidoNaoAlteraUsuario() {
        var observedUser: String? = null
        composeRule.setContent {
            var state by remember { mutableStateOf(LoginUiState()) }
            LoginScreen(
                state = state,
                onEvent = {
                    state = LoginReducer.reduce(state, it)
                    observedUser = state.user
                },
            )
        }

        composeRule.onNodeWithTag("user-field").performTextInput("A")

        composeRule.runOnIdle { assertEquals("", observedUser) }
    }

    @Test
    fun naoMostraEnterNemCancelar() {
        composeRule.setContent {
            LoginScreen(LoginUiState(), onEvent = {})
        }

        composeRule.onNodeWithText("ENTER").assertDoesNotExist()
        composeRule.onAllNodesWithText("SAIR/CANCELAR").assertCountEquals(0)
    }

    @Test
    fun mostraIdentidadeVisualEMensagemInicial() {
        composeRule.setContent {
            LoginScreen(LoginUiState(), onEvent = {})
        }

        composeRule.onNodeWithText("Buy More").assertDoesNotExist()
        composeRule.onNodeWithText("POS - COMPRAS").assertDoesNotExist()
        composeRule.onNodeWithText("v1.0.0.0").assertDoesNotExist()
        composeRule.onNodeWithText("Digite seu usuário e senha para continuar").assertIsDisplayed()
        composeRule.onNodeWithText("Loterias CAIXA").assertDoesNotExist()
    }

    @Test
    fun autenticacaoEmAndamentoBloqueiaNovaConfirmacao() {
        composeRule.setContent {
            LoginScreen(LoginUiState(isAuthenticating = true), onEvent = {})
        }

        composeRule.onNodeWithText("AGUARDE").assertIsNotEnabled()
    }
}
