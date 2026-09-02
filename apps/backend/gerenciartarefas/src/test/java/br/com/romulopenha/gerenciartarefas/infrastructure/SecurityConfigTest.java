package br.com.romulopenha.gerenciartarefas.infrastructure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Conjunto de testes automatizados para SecurityConfigTest.
 */
class SecurityConfigTest {
    @Test
    void aceitaConfiguracaoCompleta() {
        var config = new SecurityConfig();
        config.authServerUrl = "https://idp.example/realms/app";
        config.clientId = "gerenciar-tarefas";
        config.clientSecret = "secret-from-environment";

        assertDoesNotThrow(config::validate);
    }

    @Test
    void rejeitaConfiguracaoOidcAusente() {
        var config = new SecurityConfig();
        config.authServerUrl = " ";
        config.clientId = "gerenciar-tarefas";
        config.clientSecret = "secret-from-environment";

        assertThrows(IllegalStateException.class, config::validate);
    }

    @Test
    void rejeitaClientIdAusente() {
        var config = new SecurityConfig();
        config.authServerUrl = "https://idp.example/realms/app";
        config.clientId = " ";
        config.clientSecret = "secret-from-environment";

        assertThrows(IllegalStateException.class, config::validate);
    }

    @Test
    void rejeitaClientSecretAusente() {
        var config = new SecurityConfig();
        config.authServerUrl = "https://idp.example/realms/app";
        config.clientId = "gerenciar-tarefas";
        config.clientSecret = " ";

        assertThrows(IllegalStateException.class, config::validate);
    }
}
