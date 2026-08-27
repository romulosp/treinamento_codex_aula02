package br.com.romulopenha.nomedaapigerada.infrastructure.client;

import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
@ApplicationScoped
public class FiservFiltroAutenticacao implements ClientRequestFilter {

    @ConfigProperty(name = "ambiente")
    public String ambiente;

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        Log.debug("FiservFiltroAutenticacao - filter");
        Log.debug("add - HttpHeaders.AUTHORIZATION");
        requestContext.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        requestContext.getHeaders().add("Capture-Network-Code", ambiente);
    }
}