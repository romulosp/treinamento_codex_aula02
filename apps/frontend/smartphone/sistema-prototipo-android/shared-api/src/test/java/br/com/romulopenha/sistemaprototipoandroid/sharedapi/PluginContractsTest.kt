package br.com.romulopenha.sistemaprototipoandroid.sharedapi

import org.junit.Assert.assertEquals
import org.junit.Test

/** Garante os invariantes mínimos do contrato binário publicado pelo AAR. */
class PluginContractsTest {
    /** A URI mantém plugin, rota e versão como identificadores independentes de segredo. */
    @Test
    fun `cria uri de rota com a identidade do plugin`() {
        val route = PluginRoute(
            pluginId = "br.com.romulopenha.exemplo",
            path = "inicio",
            contractVersion = SharedApi.version,
        )

        assertEquals("plugin://br.com.romulopenha.exemplo/inicio", route.asUri())
    }

    /** Não permite representar uma versão negativa no contrato público. */
    @Test(expected = IllegalArgumentException::class)
    fun `rejeita versao negativa`() {
        SharedApiVersion(-1, 0, 0)
    }
}
