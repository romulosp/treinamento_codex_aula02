package br.com.romulopenha.nomedaapigerada;

import br.com.romulopenha.nomedaapigerada.infrastructure.client.FiservFiltroAutenticacao;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class FiservFiltroAutenticacaoTest {

    @Inject
    FiservFiltroAutenticacao filtro;

    @Test
    void deveAdicionarCabecalhosDaIntegracaoExterna() throws IOException {
        ClientRequestContext contexto = mock(ClientRequestContext.class);
        MultivaluedMap<String, Object> cabecalhos = new MultivaluedHashMap<>();
        when(contexto.getHeaders()).thenReturn(cabecalhos);

        filtro.filter(contexto);

        assertEquals("application/json", cabecalhos.getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals("local", cabecalhos.getFirst("Capture-Network-Code"));
    }
}