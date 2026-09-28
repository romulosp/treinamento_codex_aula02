package br.com.romulopenha.diagnosticopinpad.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifica a matriz funcional apresentada pelo laboratório Android. */
class CatalogActionTest {
    @Test
    fun `catalogo possui as 28 opcoes numeradas sem duplicidade`() {
        val actions = CatalogAction.values()
        assertEquals(28, actions.size)
        assertEquals((1..28).toList(), actions.map { it.number })
    }

    @Test
    fun `opcoes reservadas ficam desabilitadas e campos sensiveis sao marcados`() {
        assertFalse(CatalogAction.UNAVAILABLE.enabled)
        assertFalse(CatalogAction.RESERVED.enabled)
        assertTrue(CatalogAction.GPN_MKWK.fields.first { it.key == "pan" }.secret)
        assertTrue(CatalogAction.GOX.fields.first { it.key == "emvDataHex" }.secret)
    }

    @Test
    fun `entrada numerica e hexadecimal invalida e rejeitada antes do AAR`() {
        assertEquals(
            "Tamanho em pixels deve ser numérico",
            CatalogInput(mapOf("size" to "x")).validate(CatalogAction.LOAD_QR_MEDIA),
        )
        assertEquals(
            "Dados EMV em hexadecimal deve ser hexadecimal par",
            CatalogInput(mapOf("emvDataHex" to "abc")).validate(CatalogAction.GOX),
        )
    }
}
