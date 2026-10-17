package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import org.junit.Assert.assertEquals
import org.junit.Test

/** Confirma o contrato declarativo público do plugin de saque. */
class PluginSaqueCartaoAppTest {
    @Test fun `declara caminho e item de menu`() {
        val plugin = PluginSaqueCartaoApp()
        assertEquals("Principal > Outros Serviços > Saque Cartão", plugin.getCaminhoMenu())
        assertEquals(listOf("saque-cartao"), plugin.businessMenuItems.map { it.id })
    }
}
