package br.com.romulopenha.diagnosticopinpad

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.romulopenha.libpinpadabecsgo.mobile.Mobile
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Fluxo JNI/Go/PBRG real; opt-in explícito evita acessar hardware por acidente. */
@RunWith(AndroidJUnit4::class)
class BridgeRegressionTest {
    @Test fun openInfoDisplayCloseAndReopenThroughRealBinding() {
        val arguments = InstrumentationRegistry.getArguments()
        val mode = arguments.getString("bridgeMode")
        assumeTrue("Requer Bridge declarado scripted ou physical", mode in setOf("scripted", "physical"))
        val pinpadPort = requireNotNull(arguments.getString("pinpadPort")) {
            "pinpadPort deve vir da variável PORTA_PINPAD do processo de teste"
        }
        val client = Mobile.newClient("localhost", arguments.getString("bridgePort", "39100").toLong(), 10000)
        assertEquals("CLOSED", client.state)
        client.ping("072-${mode}-ping")
        assertEquals("CLOSED", client.state)
        try {
            client.open("072-${mode}-open")
            assertEquals("OPEN", client.state)
            val info = client.getInfoJSON("072-${mode}-gix")
            assertTrue(info.contains("schemaVersion"))
            client.displayDSP("072-${mode}-dsp", "TESTE ANDROID", "HOST $pinpadPort")
            client.close("072-${mode}-close")
            assertEquals("CLOSED", client.state)
            client.open("072-${mode}-reopen")
            assertEquals("OPEN", client.state)
            client.close("072-${mode}-reclose")
            assertEquals("CLOSED", client.state)
        } finally {
            client.close("072-${mode}-cleanup")
        }
    }
}
