package br.com.romulopenha.sistemaprototipoandroid.pluginnegocio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifica o contrato declarativo e local do menu demonstrativo. */
class PluginNegocioAppTest {
    /** O plugin fornece itens ordenáveis e sem dados de autenticação. */
    @Test
    fun `publica menu demonstrativo neutro`() {
        val items = PluginNegocioApp().businessMenuItems

        assertEquals(listOf("operacoes"), items.map { it.id })
        assertEquals("Principal > Operações", PluginNegocioApp().getCaminhoMenu())
        assertTrue(items.none { it.titulo.contains("senha", ignoreCase = true) })
    }
}
