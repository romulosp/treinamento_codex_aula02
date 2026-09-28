package br.com.romulopenha.diagnosticopinpad.data

import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

/** Testes JVM dos limites de configuração antes da fronteira gomobile. */
class EndpointConfigTest {
    @Test
    fun `configuracao padrao e valida`() {
        assertNull(EndpointConfig().validate())
    }

    @Test
    fun `host vazio e rejeitado`() {
        assertNotNull(EndpointConfig(host = " ").validate())
    }

    @Test
    fun `porta e timeout fora dos limites sao rejeitados`() {
        assertNotNull(EndpointConfig(port = 0).validate())
        assertNotNull(EndpointConfig(port = 65536).validate())
        assertNotNull(EndpointConfig(timeoutMillis = 99).validate())
        assertNotNull(EndpointConfig(timeoutMillis = 120001).validate())
    }
}
