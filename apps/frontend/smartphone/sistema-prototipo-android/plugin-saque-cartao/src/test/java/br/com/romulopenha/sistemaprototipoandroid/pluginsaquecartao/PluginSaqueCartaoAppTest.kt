package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import org.junit.Assert.assertEquals
import org.junit.Test

/** Confirma o contrato declarativo público do plugin de pagamento com cartão. */
class PluginSaqueCartaoAppTest {
    @Test fun `declara componente sem capacidade de menu`() {
        val plugin = PluginSaqueCartaoApp::class.java.getConstructor().newInstance()
        assertEquals(setOf("card-withdrawal-component"), plugin.manifest.capabilities)
        assertEquals(5, plugin.manifest.requiredSharedApiMinor)
        assertEquals("Pagamento com Cartão", plugin.manifest.displayName)
    }
}
