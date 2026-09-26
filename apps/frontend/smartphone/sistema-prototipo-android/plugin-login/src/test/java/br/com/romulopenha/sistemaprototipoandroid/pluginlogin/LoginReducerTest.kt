package br.com.romulopenha.sistemaprototipoandroid.pluginlogin

import org.junit.Assert.assertEquals
import org.junit.Test

/** Testa as regras de entrada puras do terminal de login. */
class LoginReducerTest {
    @Test
    fun `recusa primeiro caractere diferente de L`() {
        val state = LoginReducer.reduce(LoginUiState(), LoginEvent.UserChanged("A123"))

        assertEquals("", state.user)
    }

    @Test
    fun `normaliza primeiro caractere aceito para L maiusculo`() {
        val state = LoginReducer.reduce(LoginUiState(), LoginEvent.UserChanged("l099264"))

        assertEquals("L099264", state.user)
    }

    @Test
    fun `tecla virtual respeita regra L no usuario vazio`() {
        val rejected = LoginReducer.reduce(LoginUiState(), LoginEvent.KeyPressed("1"))
        val accepted = LoginReducer.reduce(LoginUiState(), LoginEvent.KeyPressed("L"))

        assertEquals("", rejected.user)
        assertEquals("L", accepted.user)
    }
}
