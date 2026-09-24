package br.com.romulopenha.sistemaprototipoandroid

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
                logoutInProgress = false,
                onLogout = { logoutCount++ },
            )
        }

        composeRule.onNodeWithText("Nenhum plugin de negócio carregado.").assertIsDisplayed()
        composeRule.onNodeWithText("Sair").performClick()
        composeRule.runOnIdle { assertEquals(1, logoutCount) }
    }
}
