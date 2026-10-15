package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import br.com.romulopenha.libpinpadabecsgo.mobile.Mobile
import org.junit.Assert.*
import org.junit.Test

/** Exercita símbolo JNI novo sem abrir TCP/serial ou capturar cartão/PIN. */
class PaymentPINDiscoveryTest {
    /** Descoberta exige sessão aberta; erro é categoria/fase, sem resposta raw GIX. */
    @Test fun descobertaExigeSessaoAberta() {
        val client = Mobile.newClient("127.0.0.1", 39100, 1000)
        try {
            val failure = runCatching { client.resolvePaymentPINConfiguration("closed", "3", 8, "") }.exceptionOrNull()
            assertNotNull(failure)
            assertEquals("BINDING_ERROR:preflight", failure!!.message)
            assertEquals("CLOSED", client.state)
            assertEquals("PIN_KEY_MISSING", Mobile.errorCode("PIN_KEY_MISSING:preflight"))
        } finally { client.close("cleanup") }
    }
}
