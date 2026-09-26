package br.com.romulopenha.sistemaprototipoandroid.platform

import org.junit.Assert.assertEquals
import org.junit.Test

/** Exercita a política de atualização sem instanciar nem descarregar classes do APK. */
class PluginUpdatePolicyTest {
    /** Artefato inédito pode ser ativado quando o processo não mantém instância. */
    @Test
    fun `ativa quando nao existe plugin carregado`() {
        assertEquals(
            PluginActivationDecision.ACTIVATE,
            PluginUpdatePolicy.decide(activeDigestSha256 = null, candidateDigestSha256 = "novo"),
        )
    }

    /** Reentrega idêntica não altera a instância ativa. */
    @Test
    fun `ignora artefato igual ao ativo`() {
        assertEquals(
            PluginActivationDecision.UNCHANGED,
            PluginUpdatePolicy.decide(activeDigestSha256 = "atual", candidateDigestSha256 = "atual"),
        )
    }

    /** Atualização válida de instância ativa é adiada e não permite hot swap. */
    @Test
    fun `solicita reinicio para revisao diferente da ativa`() {
        assertEquals(
            PluginActivationDecision.PENDING_RESTART,
            PluginUpdatePolicy.decide(activeDigestSha256 = "atual", candidateDigestSha256 = "nova"),
        )
    }
}
