package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import org.junit.Assert.*
import org.junit.Test

/** Parser Android testado em execução, sem ler arquivos privados ou tabela DES. */
class SaqueProfileTest {
    private val profile = """{"schemaVersion":1,"environment":"DES","host":"localhost","port":39100,
        "timeoutMillis":5000,"timeoutSeconds":5,"tableAcquirer":"01","tableVersion":"TEST000001",
        "creditApplicationTypes":"01","debitApplicationTypes":"02","feePercent":"5.00",
        "maxBaseCents":100000,"gtkDataMethod":"30","gtkKeyIndex":7,"scenario":"APPROVE"}"""

    @Test fun perfilFisicoNaoDefineTaxaDaCompra() {
        val withoutFee = profile.replace("\"feePercent\":\"5.00\",", "")
        assertEquals(parseSaqueProfile(profile), parseSaqueProfile(withoutFee))
        assertEquals(parseSaqueProfile(profile), parseSaqueProfile(profile.replace("5.00", "-1")))
        for (invalid in listOf(profile.replace("DES", "PRD"),
            profile.replace("\"gtkKeyIndex\":7,", ""),
            profile.replace("\"port\":39100", "\"port\":39100.5"),
            profile.replace("\"gtkKeyIndex\":7", "\"gtkKeyIndex\":\"7\""),
            profile.replace("\"schemaVersion\":1", "\"schemaVersion\":1.5"))) {
            assertTrue(runCatching { parseSaqueProfile(invalid) }.isFailure)
        }
    }
}
