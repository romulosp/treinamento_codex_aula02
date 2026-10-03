package br.com.romulopenha.diagnosticopinpad.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.romulopenha.diagnosticopinpad.data.CatalogAction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Regressões visuais de estado/erro em Compose, independentes de hardware. */
@RunWith(AndroidJUnit4::class)
class DiagnosticRegressionTest {
    @get:Rule val compose = createComposeRule()

    private fun show(state: DiagnosticUiState) = compose.setContent {
        DiagnosticScreenContent(state, {}, {}, {}, {}, {}, {}, {}, {}, {}, { _, _ -> }, {})
    }

    @Test fun pongWithClosedSessionDoesNotEnableGetInfo() {
        show(DiagnosticUiState(actionStatus = ActionStatus.SUCCESS, result = "PONG", bridgeStatus = BridgeStatus.REACHABLE))
        compose.onNodeWithText("Sessão: CLOSED").assertIsDisplayed()
        compose.onNodeWithText("GetInfo").assertIsNotEnabled()
    }

    @Test fun openSessionEnablesPhysicalActions() {
        show(DiagnosticUiState(sessionStatus = SessionStatus.OPEN, actionStatus = ActionStatus.SUCCESS))
        compose.onNodeWithText("GetInfo").assertIsEnabled()
    }

    @Test fun connectionActionsAreOnlyShownInQuickControls() {
        show(DiagnosticUiState())

        compose.onNodeWithText("Abrir").assertIsDisplayed()
        compose.onNodeWithText("Fechar").assertIsDisplayed()
        compose.onNodeWithText("Abrir conexão").assertDoesNotExist()
        compose.onNodeWithText("Fechar conexão").assertDoesNotExist()
    }

    @Test fun failureIsVisibleAfterScrollingToEndOfCatalog() {
        val state = mutableStateOf(DiagnosticUiState(sessionStatus = SessionStatus.OPEN))
        compose.setContent {
            DiagnosticScreenContent(state.value, {}, {}, {}, {}, {}, {}, {}, {}, {}, { _, _ -> }, {})
        }
        // A tela rola automaticamente quando a operação termina; a asserção
        // abaixo valida a visibilidade do erro independentemente do item final.
        compose.onNodeWithText("Catálogo funcional").assertExists()
        compose.runOnIdle {
            state.value = state.value.copy(actionStatus = ActionStatus.ERROR, sessionStatus = SessionStatus.CLOSED,
                error = "Serial indisponível. Confira PORTA_PINPAD.", errorCode = "SERIAL_UNAVAILABLE",
                errorPhase = "serial_open", lastOperationId = "visual-072")
        }
        compose.waitForIdle()
        compose.onNodeWithText("Erro: SERIAL_UNAVAILABLE (serial_open)").assertIsDisplayed()
        compose.onNodeWithContentDescription("Painel de diagnóstico").assertIsFocused()
        compose.onNodeWithContentDescription("Erro do diagnóstico: Serial indisponível. Confira PORTA_PINPAD.").assertIsDisplayed()
    }
}
