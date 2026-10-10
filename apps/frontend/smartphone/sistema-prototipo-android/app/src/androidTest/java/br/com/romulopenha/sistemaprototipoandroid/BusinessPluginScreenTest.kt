package br.com.romulopenha.sistemaprototipoandroid

import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Prova que o host libera a área da View e mantém retorno pelo Voltar Android. */
class BusinessPluginScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun hospedaSemFaixaEVoltarRetornaAoMenu() {
        var returned = 0
        compose.setContent {
            BusinessPluginScreen(
                factory = { context -> TextView(context).apply { text = "Tela do plugin" } },
                onBack = { returned++ },
                onRuntimeFailure = { throw AssertionError(it) },
            )
        }
        compose.onNodeWithText("Voltar ao menu").assertDoesNotExist()
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        assertEquals(1, returned)
    }
}
