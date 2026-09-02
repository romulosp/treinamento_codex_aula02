package br.com.romulopenha.gerenciartarefas.api;

import br.com.romulopenha.gerenciartarefas.domain.TarefaInvalidaException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Converte erros de validação de tarefas em respostas HTTP 400.
 */
@Provider
public class TarefaInvalidaExceptionMapper implements ExceptionMapper<TarefaInvalidaException> {
    @Override
    public Response toResponse(TarefaInvalidaException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(new MensagemResponse(exception.getMessage())).build();
    }
}
