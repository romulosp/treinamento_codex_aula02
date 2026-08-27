package br.com.romulopenha.nomedaapigerada;

import br.com.romulopenha.nomedaapigerada.infrastructure.security.ConfiguracaoSeguranca;
import br.com.romulopenha.nomedaapigerada.infrastructure.security.ValidadorClienteAutorizado;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorClienteAutorizadoTest {

    @Test
    void deveAceitarClientesConfiguradosIgnorandoEspacosEItensVazios() {
        ConfiguracaoSeguranca configuracao = () -> Optional.of(" cliente-autorizado, ,cliente-secundario ");
        ValidadorClienteAutorizado validador = new ValidadorClienteAutorizado(configuracao);

        assertTrue(validador.estaAutorizado("cliente-autorizado"));
        assertTrue(validador.estaAutorizado("cliente-secundario"));
        assertFalse(validador.estaAutorizado("CLIENTE-AUTORIZADO"));
        assertFalse(validador.estaAutorizado(""));
    }

    @Test
    void deveNegarQuandoNaoHaClientesConfigurados() {
        ValidadorClienteAutorizado validador = new ValidadorClienteAutorizado(Optional::empty);

        assertFalse(validador.estaAutorizado("cliente-autorizado"));
    }
}