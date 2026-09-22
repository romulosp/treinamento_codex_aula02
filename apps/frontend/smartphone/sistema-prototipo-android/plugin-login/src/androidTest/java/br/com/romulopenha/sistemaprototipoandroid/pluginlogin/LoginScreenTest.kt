package br.com.romulopenha.sistemaprototipoandroid.pluginlogin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
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
                onSessionEstablished = {},
            )
        }

        composeRule.onNodeWithTag("user-field").performTextInput("A")

        composeRule.runOnIdle { assertEquals("", observedUser) }
    }

    @Test
    fun mostraEnterENaoMostraCancelar() {
        composeRule.setContent {
            LoginScreen(LoginUiState(), onEvent = {}, onSessionEstablished = {})
        }

        composeRule.onNodeWithText("ENTER").performClick()
        composeRule.onAllNodesWithText("SAIR/CANCELAR").assertCountEquals(0)
    }
}
