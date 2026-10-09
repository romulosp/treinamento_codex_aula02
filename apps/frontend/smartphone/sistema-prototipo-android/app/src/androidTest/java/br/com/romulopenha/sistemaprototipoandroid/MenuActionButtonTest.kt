package br.com.romulopenha.sistemaprototipoandroid

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Verifica as cores resultantes de interações controladas, sem depender de ponteiro físico. */
class MenuActionButtonTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun coresDeHoverPressEDisabledRespeitamPrioridadeSemEscalaQuandoMovimentoReduzido() {
        val source = MutableInteractionSource()
        val enabled = mutableStateOf(true)
        composeRule.setContent {
            MenuActionButton("AÇÃO", {}, Modifier.size(240.dp, 64.dp).testTag("action"),
                enabled = enabled.value, motionAllowed = false, interactionSource = source)
        }
        fun colorAtEdge() = composeRule.onNodeWithTag("action").captureToImage().let {
            it.toPixelMap()[it.width / 8, it.height / 2]
        }
        assertEquals(MenuDesignTokens.primary, colorAtEdge())
        val initialWidth = composeRule.onNodeWithTag("action").fetchSemanticsNode().boundsInRoot.width
        source.tryEmit(HoverInteraction.Enter())
        composeRule.waitForIdle()
        assertEquals(MenuDesignTokens.primaryHover, colorAtEdge())
        source.tryEmit(PressInteraction.Press(Offset.Zero))
        composeRule.waitForIdle()
        assertEquals(MenuDesignTokens.primaryPressed, colorAtEdge())
        assertEquals(initialWidth,
            composeRule.onNodeWithTag("action").fetchSemanticsNode().boundsInRoot.width, 0.1f)
        composeRule.runOnIdle { enabled.value = false }
        assertEquals(MenuDesignTokens.disabledBackground, colorAtEdge())
    }

    @Test
    fun focoDesenhaContornoDouradoExterno() {
        val source = MutableInteractionSource()
        composeRule.setContent {
            Box(Modifier.padding(8.dp).testTag("focus-frame")) {
                MenuActionButton("AÇÃO", {}, Modifier.size(240.dp, 64.dp),
                    interactionSource = source, motionAllowed = false)
            }
        }
        source.tryEmit(FocusInteraction.Focus())
        composeRule.waitForIdle()
        val pixels = composeRule.onNodeWithTag("focus-frame").captureToImage().toPixelMap()
        var gold = 0
        for (x in 0 until pixels.width) for (y in 0 until pixels.height) {
            if (pixels[x, y] == MenuDesignTokens.focus) gold++
        }
        assertTrue("O foco deve produzir pixels dourados visíveis", gold > 0)
    }
}
