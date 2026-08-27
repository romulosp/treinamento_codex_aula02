package br.com.romulopenha.nomedaapigerada.infrastructure.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ValidadorClienteAutorizado {

    private final ConfiguracaoSeguranca configuracao;

    @Inject
    public ValidadorClienteAutorizado(ConfiguracaoSeguranca configuracao) {
        this.configuracao = configuracao;
    }

    public boolean estaAutorizado(String cliente) {
        if (cliente == null || cliente.isBlank()) {
            return false;
        }

        return clientesAutorizados().contains(cliente);
    }

    private Set<String> clientesAutorizados() {
        return Arrays.stream(configuracao.clientsAuthorized().orElse("").split(","))
                .map(String::trim)
                .filter(cliente -> !cliente.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}