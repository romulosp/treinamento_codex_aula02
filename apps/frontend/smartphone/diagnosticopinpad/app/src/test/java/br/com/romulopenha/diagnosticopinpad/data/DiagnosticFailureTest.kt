package br.com.romulopenha.diagnosticopinpad.data

import org.junit.Assert.*
import org.junit.Test

/** Regressão da fronteira de erro sem executar JNI em testes JVM. */
class DiagnosticFailureTest {
    @Test fun `codigo e fase conhecidos preservados sem mensagem remota`() {
        val safe = DiagnosticFailure.from(IllegalStateException("SERIAL_UNAVAILABLE:serial_open"))
        assertEquals("SERIAL_UNAVAILABLE", safe.code)
        assertEquals("serial_open", safe.phase)
        assertTrue(safe.message.contains("PORTA_PINPAD"))
    }
    @Test fun `dados sinteticos de terceiro nao sao ecoados`() {
        val marker = "SEGREDO_SINTETICO"
        val safe = DiagnosticFailure.from(IllegalStateException("SERIAL_UNAVAILABLE:$marker\nPAN"))
        assertEquals("binding", safe.phase)
        assertFalse(safe.message.contains(marker))
        assertFalse(DiagnosticFailure.from(Exception(marker)).message.contains(marker))
    }
    @Test fun `todas as categorias tem orientacao nao vazia`() {
        listOf("BRIDGE_UNREACHABLE", "TIMEOUT", "PROTOCOL_ERROR", "BUSY", "OWNERSHIP_ERROR",
            "SERIAL_UNAVAILABLE", "CANCELED", "DISCONNECTED", "PINPAD_ERROR", "PINPAD_CLOSED", "BINDING_ERROR")
            .forEach { assertTrue(DiagnosticFailure.messageFor(it).isNotBlank()) }
    }
}
