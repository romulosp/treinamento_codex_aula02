package br.com.romulopenha.sistemaprototipoandroid.sharedapi

import org.junit.Assert.*
import org.junit.Test

/** Valida o contrato monetário sem dependência Android ou dados sensíveis. */
class CardPaymentRequestTest {
    @Test fun apresentacaoPadraoPreservaLegadoEFacetasSaoAditivas() {
        assertEquals(CardPaymentResultPresentation.COMPONENT, CardPaymentOptions().resultPresentation)
        assertTrue(ICardPaymentHostContext::class.java.isAssignableFrom(ICardPaymentPresentationHostContext::class.java))
        assertTrue(ICardPaymentComponent::class.java.isAssignableFrom(ICardPaymentPresentationComponent::class.java))
        val constructor = CardPaymentRequest::class.java.getConstructor(String::class.java, java.lang.Long.TYPE)
        val request = constructor.newInstance("old-consumer", 3000L)
        assertEquals(CardPaymentRequest("old-consumer", 3000), request.copy())
    }
    @Test fun valorEmCentavosPreservado() {
        val request = CardPaymentRequest("pedido-1", 3000)
        assertEquals(3000L, request.amountCents)
        listOf(0L, -1L, 1000000000000L).forEach {
            assertTrue(runCatching { CardPaymentRequest("pedido", it) }.isFailure)
        }
        assertTrue(runCatching { CardPaymentRequest(" ", 3000) }.isFailure)
        assertTrue(runCatching { CardPaymentRequest("x".repeat(129), 3000) }.isFailure)
    }
}
