package br.com.romulopenha.nomedaapigerada.api;

import br.com.romulopenha.nomedaapigerada.application.CategoriaApplicationService;
import br.com.romulopenha.nomedaapigerada.domain.Categoria;
import br.com.romulopenha.nomedaapigerada.infrastructure.security.ClienteAutorizado;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/categorias")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Categorias", description = "Operações para categorias mantidas em memória")
@Authenticated
@ClienteAutorizado
public class CategoriaResource {

    private static final String MENSAGEM_NAO_ENCONTRADA = "Categoria não encontrada";

    @Inject
    CategoriaApplicationService service;

    @GET
    @Operation(summary = "Lista as categorias")
        @APIResponses({
            @APIResponse(responseCode = "200", description = "Lista de categorias", content = @Content(schema = @Schema(implementation = CategoriaListaResponse.class))),
            @APIResponse(responseCode = "401", description = "Token Bearer ausente ou inválido"),
            @APIResponse(responseCode = "403", description = "Cliente não autorizado", content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
        })
    public CategoriaListaResponse listar() {
        List<CategoriaResponse> categorias = service.listar().stream()
                .map(CategoriaResponse::de)
                .toList();
        return new CategoriaListaResponse(categorias);
    }

    @GET
    @Path("/{id_categoria}")
    @Operation(summary = "Detalha uma categoria")
        @APIResponses({
            @APIResponse(responseCode = "200", description = "Categoria encontrada", content = @Content(schema = @Schema(implementation = CategoriaResponse.class))),
            @APIResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content(schema = @Schema(implementation = MensagemResponse.class))),
            @APIResponse(responseCode = "401", description = "Token Bearer ausente ou inválido"),
            @APIResponse(responseCode = "403", description = "Cliente não autorizado", content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
        })
    public Response detalhar(@PathParam("id_categoria") long idCategoria) {
        return service.detalhar(idCategoria)
                .map(CategoriaResource::ok)
                .orElseGet(() -> naoEncontrada());
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Adiciona uma categoria")
        @APIResponses({
            @APIResponse(responseCode = "201", description = "Categoria criada", content = @Content(schema = @Schema(implementation = CategoriaResponse.class))),
            @APIResponse(responseCode = "400", description = "Entrada inválida", content = @Content(schema = @Schema(implementation = MensagemResponse.class))),
            @APIResponse(responseCode = "401", description = "Token Bearer ausente ou inválido"),
            @APIResponse(responseCode = "403", description = "Cliente não autorizado", content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
        })
    public Response adicionar(CategoriaRequest request) {
        MensagemResponse erro = validar(request);
        if (erro != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(erro).build();
        }
        Categoria categoria = service.adicionar(request.nomeCategoria().trim(), request.quantidadeProdutos());
        return Response.status(Response.Status.CREATED).entity(CategoriaResponse.de(categoria)).build();
    }

    @PUT
    @Path("/{id_categoria}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Atualiza uma categoria")
        @APIResponses({
            @APIResponse(responseCode = "200", description = "Categoria atualizada", content = @Content(schema = @Schema(implementation = CategoriaResponse.class))),
            @APIResponse(responseCode = "400", description = "Entrada inválida", content = @Content(schema = @Schema(implementation = MensagemResponse.class))),
            @APIResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content(schema = @Schema(implementation = MensagemResponse.class))),
            @APIResponse(responseCode = "401", description = "Token Bearer ausente ou inválido"),
            @APIResponse(responseCode = "403", description = "Cliente não autorizado", content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
        })
    public Response atualizar(@PathParam("id_categoria") long idCategoria, CategoriaRequest request) {
        MensagemResponse erro = validar(request);
        if (erro != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(erro).build();
        }
        return service.atualizar(idCategoria, request.nomeCategoria().trim(), request.quantidadeProdutos())
                .map(CategoriaResource::ok)
                .orElseGet(() -> naoEncontrada());
    }

    @DELETE
    @Path("/deletar/{id_categoria}")
    @Operation(summary = "Exclui uma categoria")
        @APIResponses({
            @APIResponse(responseCode = "200", description = "Categoria excluída", content = @Content(schema = @Schema(implementation = ResultadoExclusaoResponse.class))),
            @APIResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content(schema = @Schema(implementation = MensagemResponse.class))),
            @APIResponse(responseCode = "401", description = "Token Bearer ausente ou inválido"),
            @APIResponse(responseCode = "403", description = "Cliente não autorizado", content = @Content(schema = @Schema(implementation = MensagemResponse.class)))
        })
    public Response excluir(@PathParam("id_categoria") long idCategoria) {
        if (!service.excluir(idCategoria)) {
            return naoEncontrada();
        }
        return Response.ok(new ResultadoExclusaoResponse("CATEGORIA EXCLUIDA COM SUCESSO")).build();
    }

    private static Response ok(Categoria categoria) {
        return Response.ok(CategoriaResponse.de(categoria)).build();
    }

    private static Response naoEncontrada() {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new MensagemResponse(MENSAGEM_NAO_ENCONTRADA))
                .build();
    }

    private static MensagemResponse validar(CategoriaRequest request) {
        if (request == null || request.nomeCategoria() == null || request.nomeCategoria().isBlank()) {
            return new MensagemResponse("nome_categoria é obrigatório e não pode ser vazio");
        }
        if (request.quantidadeProdutos() == null || request.quantidadeProdutos() < 0) {
            return new MensagemResponse("quantidade_produtos é obrigatória e deve ser maior ou igual a zero");
        }
        return null;
    }
}
