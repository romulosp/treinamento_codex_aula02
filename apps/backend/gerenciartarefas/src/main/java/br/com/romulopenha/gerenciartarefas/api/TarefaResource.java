package br.com.romulopenha.gerenciartarefas.api;

import br.com.romulopenha.gerenciartarefas.application.TarefaService;
import br.com.romulopenha.gerenciartarefas.domain.TarefaInvalidaException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/tarefas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TarefaResource {
    private final TarefaService service;

    public TarefaResource(TarefaService service) { this.service = service; }

    @GET
    public ListaTarefasResponse listar() {
        return new ListaTarefasResponse(service.listar().stream().map(TarefaResponse::from).toList());
    }

    @GET @Path("/{id}")
    public TarefaResponse buscar(@PathParam("id") Long id) { return TarefaResponse.from(service.buscar(id)); }

    @POST
    @RolesAllowed("ADMIN")
    public Response criar(@Valid TarefaRequest request) {
        validarCorpo(request);
        var tarefa = service.criar(request.titulo(), request.descricao(), request.status());
        return Response.status(Response.Status.CREATED).entity(TarefaResponse.from(tarefa)).build();
    }

    @PUT @Path("/{id}")
    @RolesAllowed("ADMIN")
    public TarefaResponse atualizar(@PathParam("id") Long id, @Valid TarefaRequest request) {
        validarCorpo(request);
        return TarefaResponse.from(service.atualizar(id, request.titulo(), request.descricao(), request.status()));
    }

    @DELETE @Path("/{id}")
    @RolesAllowed("ADMIN")
    public ResultadoExclusaoResponse excluir(@PathParam("id") Long id) {
        service.excluir(id);
        return new ResultadoExclusaoResponse("TAREFA EXCLUIDA COM SUCESSO");
    }

    private void validarCorpo(TarefaRequest request) {
        if (request == null) throw new TarefaInvalidaException("O corpo da requisição é obrigatório");
    }
}
