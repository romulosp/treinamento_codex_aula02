package br.com.romulopenha.gerenciartarefas.api;

import br.com.romulopenha.gerenciartarefas.domain.TarefaNaoEncontradaException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TarefaNaoEncontradaExceptionMapper implements ExceptionMapper<TarefaNaoEncontradaException> {
    @Override
    public Response toResponse(TarefaNaoEncontradaException exception) {
        return Response.status(Response.Status.NOT_FOUND).entity(new MensagemResponse(exception.getMessage())).build();
    }
}
