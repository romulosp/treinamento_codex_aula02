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
                fatalMessage = null,
                logoutInProgress = false,
                onItemSelected = {},
                onLogout = { logoutCount++ },
                onFatalAcknowledged = {},
            )
        }

        composeRule.onNodeWithText("Nenhum plugin de negócio carregado.").assertIsDisplayed()
        composeRule.onNodeWithText("Sair").performClick()
        composeRule.runOnIdle { assertEquals(1, logoutCount) }
    }

    @Test
    fun folhaDoMenuEncaminhaSelecaoDoOperador() {
        var selected: String? = null
        composeRule.setContent {
            BusinessMenuScreen(
                items = listOf(br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem("saque", "Saque Cartão", "saque", 1)),
                fatalMessage = null,
                logoutInProgress = false,
                onItemSelected = { selected = it },
                onLogout = {},
                onFatalAcknowledged = {},
            )
        }

        composeRule.onNodeWithText("Saque Cartão").performClick()
        composeRule.runOnIdle { assertEquals("saque", selected) }
    }
}
