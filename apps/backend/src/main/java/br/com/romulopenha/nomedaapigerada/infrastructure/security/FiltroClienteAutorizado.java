package br.com.romulopenha.nomedaapigerada.infrastructure.security;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.io.IOException;
import java.util.Map;

@Provider
@ClienteAutorizado
@Priority(Priorities.AUTHORIZATION)
@ApplicationScoped
public class FiltroClienteAutorizado implements ContainerRequestFilter {

    private static final String CLAIM_CLIENTE = "azp";

    @Inject
    SecurityIdentity identidade;

    @Inject
    ValidadorClienteAutorizado validador;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String cliente = clienteChamador();
        if (!validador.estaAutorizado(cliente)) {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("mensagem", "Cliente não autorizado"))
                    .build());
        }
    }

    private String clienteChamador() {
        if (identidade.getPrincipal() instanceof JsonWebToken token) {
            return token.getClaim(CLAIM_CLIENTE);
        }
        return null;
    }
}