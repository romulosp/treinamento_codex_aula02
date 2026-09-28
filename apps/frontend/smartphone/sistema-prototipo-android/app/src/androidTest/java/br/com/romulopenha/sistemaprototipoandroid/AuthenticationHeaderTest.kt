package br.com.romulopenha.sistemaprototipoandroid

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.AuthenticatedProfile
import org.junit.Rule
import org.junit.Test

/** Comprova as duas variantes e a permanência do conteúdo no shell global. */
class AuthenticationHeaderTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun varianteDeslogadaMostraSomenteIdentidadeBase() {
        composeRule.setContent {
            CoreShell(AuthenticationHeaderState.LoggedOut) { Text("LOGIN") }
        }

        composeRule.onNodeWithText("Buy More").assertIsDisplayed()
        composeRule.onNodeWithText("POS - COMPRAS").assertIsDisplayed()
        composeRule.onNodeWithText("v1.0.0.0").assertIsDisplayed()
        composeRule.onNodeWithText("LOGIN").assertIsDisplayed()
        composeRule.onNodeWithText("TERMINAL").assertDoesNotExist()
    }

    @Test
    fun varianteLogadaMostraPerfilTerminalSemLotericaEConteudo() {
        composeRule.setContent {
            CoreShell(
                AuthenticationHeaderState.LoggedIn(
                    AuthenticatedProfile("L09264", "Rômulo", "PROPRIETARIO"),
                ),
            ) { Text("MENU") }
        }

        composeRule.onNodeWithText("PROPRIETÁRIO: L09264 - Rômulo").assertIsDisplayed()
        composeRule.onNodeWithText("TERMINAL").assertIsDisplayed()
        composeRule.onNodeWithText("LOTÉRICA").assertDoesNotExist()
        composeRule.onNodeWithText("MENU").assertIsDisplayed()
    }
}
