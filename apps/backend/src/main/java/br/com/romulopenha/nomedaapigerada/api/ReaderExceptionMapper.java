package br.com.romulopenha.nomedaapigerada.api;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.spi.ReaderException;

/** Padroniza o contrato de erro quando o JSON não pode ser convertido para o DTO. */
@Provider
public class ReaderExceptionMapper implements ExceptionMapper<ReaderException> {

    @Override
    public Response toResponse(ReaderException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new MensagemResponse("JSON inválido para o contrato de categoria"))
                .build();
    }
}