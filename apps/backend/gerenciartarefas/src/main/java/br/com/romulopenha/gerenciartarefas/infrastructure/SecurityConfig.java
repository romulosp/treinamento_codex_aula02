package br.com.romulopenha.gerenciartarefas.infrastructure;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Valida a configuracao obrigatoria do OIDC.
 * A autenticacao e a validacao criptografica do JWT sao executadas pela extensao quarkus-oidc.
 */
@ApplicationScoped
public class SecurityConfig {

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String authServerUrl;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String clientSecret;

    @PostConstruct
    void validate() {
        requireValue("quarkus.oidc.auth-server-url", authServerUrl);
        requireValue("quarkus.oidc.client-id", clientId);
        requireValue("quarkus.oidc.credentials.secret", clientSecret);
    }

    private void requireValue(String property, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Configuracao obrigatoria ausente: " + property);
        }
    }
}
