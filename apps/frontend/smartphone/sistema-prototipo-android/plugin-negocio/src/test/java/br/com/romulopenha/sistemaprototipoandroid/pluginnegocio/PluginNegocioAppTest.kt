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

        assertEquals(listOf("operacoes", "relatorios", "configuracoes", "indisponivel"), items.map { it.id })
        assertTrue(items.none { it.titulo.contains("senha", ignoreCase = true) })
    }
}
