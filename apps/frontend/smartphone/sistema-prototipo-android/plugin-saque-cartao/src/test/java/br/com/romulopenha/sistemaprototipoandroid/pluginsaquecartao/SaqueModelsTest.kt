package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import java.math.BigDecimal
import org.junit.Assert.*
import org.junit.Test

/** Dinheiro exato e limites do contrato, usando exclusivamente perfil sintético. */
class SaqueModelsTest {
    @Test fun `credito aplica percentual por parcela e debito tarifa fixa`() {
        val expected = listOf("5.34", "6.14", "6.94", "7.74", "8.54", "9.34", "10.14", "10.94", "11.74", "12.54", "13.34", "14.14")
        expected.forEachIndexed { index, percent ->
            val summary = calculateSaque(10000, SaqueModalidade.CREDITO, index + 1)
            assertEquals(BigDecimal(percent), summary.feePercent)
            assertEquals(BigDecimal(percent).movePointRight(2).longValueExact(), summary.feeCents)
            assertEquals(10000 + summary.feeCents, summary.totalCents)
        }
        val debit = calculateSaque(10000, SaqueModalidade.DEBITO, 1)
        assertNull(debit.feePercent)
        assertEquals(DebitFeeCents, debit.feeCents)
        assertEquals(10100L, debit.totalCents)
        assertEquals("R$ 1,00", feeLabelFor(SaqueModalidade.DEBITO, 1))
        assertEquals("6,14%", feeLabelFor(SaqueModalidade.CREDITO, 2))
        assertEquals("—", feeLabelFor(SaqueModalidade.CREDITO, 0))
    }

    @Test fun `arredonda HALF UP e congela total N12`() {
        val summary = calculateSaque(10, SaqueModalidade.CREDITO, 12, testProfile())
        assertEquals(1L, summary.feeCents); assertEquals(11L, summary.totalCents)
        assertEquals("000000000011", summary.amountN12())
        assertEquals("1.234.567,89", formatCents(123456789))
    }

    @Test fun `digitos monetarios formatam em centavos exatos`() {
        assertEquals(1L, purchaseCents("1"))
        assertEquals(10534L, purchaseCents("10534"))
        assertEquals("0,01", formatPurchaseDigits("1"))
        assertEquals("1,00", formatPurchaseDigits("100"))
        assertEquals("105,34", formatPurchaseDigits("10534"))
        assertEquals("", formatPurchaseDigits(""))
        assertEquals("150,00", formatPurchaseDigits("150,00"))
        assertEquals("1234567890123", formatPurchaseDigits("1234567890123"))
        for (digits in listOf("", "0", "000", "-1", "1.001", "1,", "NaN", "abc", "9999999999999")) {
            assertTrue(digits, runCatching { purchaseCents(digits) }.isFailure)
        }
    }

    @Test fun `bloqueia limites parcelas e overflow`() {
        for (count in listOf(0, 13, -1)) assertTrue(runCatching {
            calculateSaque(100, SaqueModalidade.CREDITO, count, testProfile()) }.isFailure)
        assertTrue(runCatching { calculateSaque(100, SaqueModalidade.DEBITO, 2, testProfile()) }.isFailure)
        assertTrue(runCatching { calculateSaque(100001, SaqueModalidade.CREDITO, 1, testProfile()) }.isFailure)
        assertTrue(runCatching { calculateSaque(MaxAmountCents, SaqueModalidade.DEBITO, 1,
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

    @Test fun `GTK pode ser em claro ou DUKPT DATA pelo perfil`() {
        val clear = testProfile().copy(gtkDataMethod = "", gtkKeyIndex = 63)
        clear.validate()
        assertEquals("", clear.gtkDataMethod)
        assertEquals(63, clear.gtkKeyIndex)
        val encrypted = clear.copy(gtkDataMethod = "30")
        encrypted.validate()
        assertEquals(63, encrypted.gtkKeyIndex)
        listOf("00", "50", "CLEAR", " ", "030").forEach { method ->
            assertTrue(runCatching { clear.copy(gtkDataMethod = method).validate() }.isFailure)
        }
    }
}
