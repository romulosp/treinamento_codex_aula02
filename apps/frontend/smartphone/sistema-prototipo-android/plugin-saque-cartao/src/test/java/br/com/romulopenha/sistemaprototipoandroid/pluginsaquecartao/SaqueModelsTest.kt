package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import java.math.BigDecimal
import org.junit.Assert.*
import org.junit.Test

/** Dinheiro exato e limites do contrato, usando exclusivamente perfil sintético. */
class SaqueModelsTest {
    @Test fun `taxas de todas as parcelas recalculam compra mais tarifa`() {
        val expected = listOf("5.34", "6.14", "6.94", "7.74", "8.54", "9.34", "10.14", "10.94", "11.74", "12.54", "13.34", "14.14")
        expected.forEachIndexed { index, percent ->
            val summary = calculateSaque("100", SaqueModalidade.CREDITO, "${index + 1}")
            assertEquals(BigDecimal(percent), summary.feePercent)
            assertEquals(BigDecimal(percent).movePointRight(2).longValueExact(), summary.feeCents)
            assertEquals(10000 + summary.feeCents, summary.totalCents)
        }
        assertEquals(10534L, calculateSaque("100", SaqueModalidade.DEBITO, "1").totalCents)
    }

    @Test fun `arredonda HALF UP e congela total N12`() {
        val summary = calculateSaque("0,10", SaqueModalidade.CREDITO, "12", testProfile())
        assertEquals(1L, summary.feeCents); assertEquals(11L, summary.totalCents)
        assertEquals("000000000011", summary.amountN12())
        assertEquals("1.234.567,89", formatCents(123456789))
    }

    @Test fun `bloqueia entradas invalidas limite parcelas e overflow`() {
        for (input in listOf("", "0", "-1", "1.001", "1,", "NaN", "1001", "99999999999999999")) {
            assertTrue(input, runCatching { calculateSaque(input, SaqueModalidade.CREDITO, "1", testProfile()) }.isFailure)
        }
        for (count in listOf("0", "13", "x", "")) assertTrue(runCatching {
            calculateSaque("1", SaqueModalidade.CREDITO, count, testProfile()) }.isFailure)
        assertTrue(runCatching { calculateSaque("1", SaqueModalidade.DEBITO, "2", testProfile()) }.isFailure)
        assertTrue(runCatching { calculateSaque("9999999999.99", SaqueModalidade.CREDITO, "1",
            testProfile().copy(maxBaseCents = MaxAmountCents)) }.isFailure)
    }

    @Test fun `perfil rejeita producao filtros duplicados e chave DATA implicita`() {
        testProfile().validate()
        val invalid = listOf(testProfile().copy(environment = "PRD"), testProfile().copy(host = "example.org"),
            testProfile().copy(port = 0), testProfile().copy(timeoutMillis = 1), testProfile().copy(timeoutSeconds = 0),
            testProfile().copy(tableAcquirer = "00"), testProfile().copy(tableVersion = "short"),
            testProfile().copy(creditApplicationTypes = "0101"), testProfile().copy(debitApplicationTypes = "01"),
            testProfile().copy(maxBaseCents = 0), testProfile().copy(gtkDataMethod = "00"), testProfile().copy(gtkKeyIndex = 100))
        invalid.forEach { assertTrue(runCatching(it::validate).isFailure) }
        assertEquals("01", testProfile().applicationTypes(SaqueModalidade.CREDITO))
        assertEquals("02", testProfile().applicationTypes(SaqueModalidade.DEBITO))
        listOf("", "0", "00", "99", "aa", "0101", "0102030405060708091011").forEach { assertFalse(validApplicationTypes(it)) }
        assertTrue(validApplicationTypes("0198"))
    }
}
