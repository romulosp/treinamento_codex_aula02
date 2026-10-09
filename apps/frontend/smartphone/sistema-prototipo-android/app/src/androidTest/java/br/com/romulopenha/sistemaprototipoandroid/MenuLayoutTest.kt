package br.com.romulopenha.sistemaprototipoandroid

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.AuthenticatedProfile
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Matriz de áreas úteis em dp: geometria, dados longos, scroll e capturas para teste humano. */
@RunWith(Parameterized::class)
class MenuLayoutTest(private val width: Int, private val height: Int,
    private val density: Float, private val fontScale: Float) {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun larguraUtilPreservaHeaderCardSaidaEScroll() {
        val longName = "Nome de usuário extenso para verificar leitura integral e truncamento"
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                Box(Modifier.requiredSize(width.dp, height.dp).testTag("fixture")) {
                    CoreShell(AuthenticationHeaderState.LoggedIn(AuthenticatedProfile("TESTE", longName,
                        "PROPRIETARIO")), menuAppearance = true) {
                        BusinessMenuScreen((0..29).map {
                            BusinessMenuItem("id-$it", "Funcionalidade $it", "route-$it", it)
                        }, null, false, {}, {}, {})
                    }
                }
            }
        }
        composeRule.onNodeWithText("Buy More").assertIsDisplayed()
        if (width >= 1200) composeRule.onNodeWithText("POS - COMPRAS").assertIsDisplayed()
        else composeRule.onNodeWithText("POS - COMPRAS").assertDoesNotExist()
        composeRule.onNodeWithText("PROPRIETÁRIO: TESTE - $longName").assertIsDisplayed()
        val card = composeRule.onNodeWithText("Funcionalidade 0").fetchSemanticsNode().boundsInRoot
        assertEquals(240f * density, card.width, 1f)
        assertEquals(64f * density, card.height, 1f)
        val exitBefore = composeRule.onNodeWithText("SAIR").fetchSemanticsNode().boundsInRoot
        val root = composeRule.onNodeWithTag("fixture").fetchSemanticsNode().boundsInRoot
        assertEquals(72f * density, root.bottom - exitBefore.bottom, 1f)
        assertEquals((if (width < 1024) 16f else 24f) * density, root.right - exitBefore.right, 1f)
        saveFixture("initial")
        composeRule.onNodeWithText("Funcionalidade 29").performScrollTo().assertIsDisplayed()
        val exitAfter = composeRule.onNodeWithText("SAIR").fetchSemanticsNode().boundsInRoot
        assertEquals(exitBefore, exitAfter)
        assertTrue(card.left >= root.left)
        saveFixture("scrolled")
    }

    private fun saveFixture(state: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val target = instrumentation.targetContext
        val directory = File(target.getExternalFilesDir(null), "menu-10010").apply { mkdirs() }
        val image = File(directory, "menu10010-${width}x${height}-d${density}-f${fontScale}-$state.png")
        image.outputStream().use {
            composeRule.onNodeWithTag("fixture").captureToImage().asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        // Preserve a captura após a desinstalação automática do APK de teste pelo Gradle.
        android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
            "cp ${image.absolutePath} /data/local/tmp/${image.name}",
        )).use { it.readBytes() }
    }

    companion object {
        @JvmStatic @Parameterized.Parameters(name = "{0}x{1}dp density={2} fontScale={3}")
        fun sizes() = listOf(
            arrayOf(1024, 768, 1f, 1f), arrayOf(1366, 768, 1f, 1f),
            arrayOf(1920, 1080, 1f, 1f), arrayOf(1366, 768, 0.75f, 1f),
            arrayOf(360, 640, 2f, 1.3f),
        )
    }
}
