package br.com.romulopenha.sistemaprototipoandroid.pluginlogin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testa a conversão do contrato HTTP sem realizar rede. */
class CredentialAuthenticationClientTest {
    @Test
    fun `converte resposta positiva em sessao opaca`() {
        val result = parseAuthenticationResponse(
            200,
            """{"autenticado":true,"sessaoId":"sessao","expiraEmEpochMillis":123000}""",
        )

        assertEquals(AuthenticationResult.Success("sessao", 123000), result)
    }

    @Test
    fun `converte 401 em credenciais invalidas`() {
        assertTrue(parseAuthenticationResponse(401, "{}") is AuthenticationResult.InvalidCredentials)
    }

    @Test
    fun `recusa resposta positiva sem sessao`() {
        assertTrue(
            parseAuthenticationResponse(200, """{"autenticado":true}""") is
                AuthenticationResult.Unavailable,
        )
    }
}
