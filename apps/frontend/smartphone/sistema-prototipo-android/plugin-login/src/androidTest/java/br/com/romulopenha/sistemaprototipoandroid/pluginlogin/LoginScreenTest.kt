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
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalView
import android.view.WindowInsets
import android.os.Build
import android.view.View
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull

/** Valida a semântica observável da tela sem criar o grafo do host. */
class LoginScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    /** Usa eventos físicos e toque para verificar edição com a IME invisível. */
    @Test
    @android.annotation.TargetApi(30)
    fun tecladoFisicoEProprioFuncionamSemTecladoAndroid() {
        org.junit.Assume.assumeTrue(Build.VERSION.SDK_INT >= 30)
        lateinit var root: View
        var observed = LoginUiState()
        composeRule.setContent {
            root = LocalView.current
            var state by remember { mutableStateOf(LoginUiState()) }
            LoginScreen(state, onEvent = {
                state = LoginReducer.reduce(state, it)
                observed = state
            })
        }
        composeRule.onNodeWithTag("user-field").performClick().assertIsFocused()
        composeRule.onNodeWithTag("user-field").performKeyInput {
            pressKey(Key.L)
            pressKey(Key.One)
        }
        composeRule.runOnIdle { assertEquals("L1", observed.user) }
        composeRule.onNodeWithText("L").performClick()
        composeRule.runOnIdle { assertEquals("L1L", observed.user) }
        composeRule.onNodeWithTag("password-field").performClick().assertIsFocused()
        composeRule.onNodeWithTag("password-field").performKeyInput {
            pressKey(Key.A)
            pressKey(Key.Two)
            pressKey(Key.Backspace)
        }
        composeRule.runOnIdle { assertEquals("a", observed.password) }
        composeRule.onNodeWithText("L").performClick()
        composeRule.runOnIdle {
            assertEquals("aL", observed.password)
            val insets = root.rootWindowInsets
            assertNotNull(insets)
            assertFalse(insets!!.isVisible(WindowInsets.Type.ime()))
        }
    }

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
