package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import org.junit.Assert.assertEquals
import org.junit.Test

/** Confirma o contrato declarativo público do plugin de pagamento com cartão. */
class PluginSaqueCartaoAppTest {
    @Test fun `declara caminho item e nome de exibicao`() {
        val plugin = PluginSaqueCartaoApp()
        assertEquals("Principal > Outros Serviços > Pagamento com Cartão", plugin.getCaminhoMenu())
        assertEquals(listOf("pagamento-cartao"), plugin.businessMenuItems.map { it.id })
        assertEquals(listOf("Pagamento com Cartão"), plugin.businessMenuItems.map { it.titulo })
        assertEquals("Pagamento com Cartão", plugin.manifest.displayName)
    }
}
